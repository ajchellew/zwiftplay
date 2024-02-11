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
import com.che.zap.device.ZapBleUuids
import com.che.zap.utils.BaseObservable
import com.che.zap.utils.Logger

class BleControllerScanner(context: Context) : BaseObservable<BleControllerScanner.ScannerCallback>() {

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

        val zwiftAccessoryFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(ZapBleUuids.ZWIFT_CUSTOM_SERVICE_UUID))
            .build()
        val kickrCoreFilter = ScanFilter.Builder()
            .setServiceData(ParcelUuid(ZapBleUuids.ZWIFT_CUSTOM_SERVICE_UUID), byteArrayOf(1))
            .build()
        val filters = listOf<ScanFilter>(zwiftAccessoryFilter, kickrCoreFilter)

        val settings = ScanSettings.Builder().build()

        Logger.d("Start Scan")
        bluetoothLeScanner.startScan(filters, settings, scanCallback)
    }

    @SuppressLint("MissingPermission") // permission handled by UI
    fun stop() {
        Logger.d("Stop Scan")
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