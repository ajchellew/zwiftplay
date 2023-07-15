package com.che.zwiftplayhost.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.che.zwiftplayhost.utils.BaseObservable
import com.che.zwiftplayhost.utils.Logger

class BleControllerScanner(context: Context) : BaseObservable<BleControllerScanner.ScannerCallback>() {

    companion object {
        private const val TAG = "Scanner"
    }

    private val bluetoothLeScanner: BluetoothLeScanner

    interface ScannerCallback {
        fun onResult(scanResult: ScanResult)
        fun onError(errorCode: Int)
    }

    init {
        // get BLE scanner
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }

    @SuppressLint("MissingPermission") // permission handled by UI
    fun start() {

        // only scan for the service unique to the controllers
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(ZwiftPlayProfile.PLAY_CONTROLLER_SERVICE_UUID))
            .build()
        val filters = listOf<ScanFilter>(filter)

        val settings = ScanSettings.Builder().build()

        Logger.d(TAG, "Start Scan")
        bluetoothLeScanner.startScan(filters, settings, scanCallback)
    }

    @SuppressLint("MissingPermission") // permission handled by UI
    fun stop() {
        Logger.d(TAG, "Stop Scan")
        bluetoothLeScanner.stopScan(scanCallback)
    }

    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            if (result == null) return

            for (listener in listeners) {
                listener.onResult(result)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)

            if (results == null) return

            for (listener in listeners) {
                for (result in results)
                    listener.onResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            for (listener in listeners) {
                listener.onError(errorCode)
            }
        }
    }

}