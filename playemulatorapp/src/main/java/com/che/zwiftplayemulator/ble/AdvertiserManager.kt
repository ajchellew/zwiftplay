package com.che.zwiftplayemulator.ble

import android.annotation.SuppressLint
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import com.che.zap.device.common.ZapBleUuids
import com.che.zap.device.common.ZapConstants
import com.che.zap.utils.Logger
import java.nio.ByteBuffer

@SuppressLint("MissingPermission")
class AdvertiserManager(private val bluetoothLeAdvertiser: BluetoothLeAdvertiser) {

    fun start() {

        // wouldn't start advertising with this
        /*previousName = bluetoothManager.adapter.name
        Timber.d("Old Bluetooth Name '$previousName'")
        val isNameChanged = bluetoothManager.adapter.setName("Zwift Play *Wink*")
        if (isNameChanged) Timber.d("New Bluetooth Name '${bluetoothManager.adapter.name}'")*/
        val isNameChanged = false

        start(true, isNameChanged)
        //start(false, isNameChanged)
    }

    private fun start(isRight: Boolean, broadcastName: Boolean) {

        val manufacturerData = ByteBuffer
            .allocate(5)
            .put(if (isRight) ZapConstants.RC1_RIGHT_SIDE else ZapConstants.RC1_LEFT_SIDE)
            .putShort(if (isRight) 1 else 2) // 4 bytes for end of device MAC?
            .array()

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(broadcastName)
            .setIncludeTxPowerLevel(false)
            .addManufacturerData(ZapConstants.ZWIFT_MANUFACTURER_ID, manufacturerData)
            .addServiceUuid(ParcelUuid(ZapBleUuids.ZWIFT_CUSTOM_SERVICE_UUID))
            .build()

        bluetoothLeAdvertiser.startAdvertising(settings, data, if (isRight) rightAdvertiseCallback else leftAdvertiseCallback)
    }

    private val rightAdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Logger.i("Right LE Advertise Started")
        }

        override fun onStartFailure(errorCode: Int) {
            Logger.e("Right LE Advertise Failed: $errorCode")
        }
    }

    private val leftAdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Logger.i("Left LE Advertise Started")
        }

        override fun onStartFailure(errorCode: Int) {
            Logger.e("Left LE Advertise Failed: $errorCode")
        }
    }

    fun stop(right: Boolean?) {
        val bluetoothLeAdvertiser: BluetoothLeAdvertiser? = bluetoothLeAdvertiser
        bluetoothLeAdvertiser?.let {
            if (right == true || right == null)
                it.stopAdvertising(rightAdvertiseCallback)
            if (right == false || right == null)
                it.stopAdvertising(leftAdvertiseCallback)
        } ?: Logger.e("Failed to stop advertiser")
    }

}

