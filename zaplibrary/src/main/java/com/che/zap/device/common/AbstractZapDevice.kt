package com.che.zap.device.common

import com.che.zap.crypto.EncryptionUtils
import com.che.zap.crypto.LocalKeyProvider
import com.che.zap.crypto.ZapCrypto
import com.che.zap.device.common.ZapConstants.BATTERY_LEVEL_TYPE
import com.che.zap.device.common.ZapConstants.EMPTY_MESSAGE_TYPE
import com.che.zap.proto.BatteryStatus
import com.che.zap.utils.Logger
import com.che.zap.utils.startsWith
import com.che.zap.utils.toHexString
import timber.log.Timber

abstract class AbstractZapDevice {

    companion object {
        internal const val LOG_RAW = false

        // As cagnulein found out you can bypass all the crypto and just get the protocol buffer data.
        // useful if you are using the Zwift hardware for something else.
        internal const val ENCRYPTED = true
    }

    // only if encrypted = true
    private var devicePublicKeyBytes: ByteArray? = null
    private var localKeyProvider = LocalKeyProvider()
    private var zapEncryption = ZapCrypto(localKeyProvider)

    open fun supportsEncryption(): Boolean {
        return true
    }

    // you get battery level in a BLE characteristic and via a ZAP message.
    // technically all zap devices might not have batteries. but this is just a test app
    private var batteryLevel = 0

    fun processCharacteristic(characteristicName: String, bytes: ByteArray?) {
        if (bytes == null) return

        if (LOG_RAW) Timber.d("$characteristicName ${bytes.toHexString()}")

        when {
            bytes.startsWith(ZapConstants.RIDE_ON.plus(ZapConstants.RESPONSE_START)) -> processDevicePublicKeyResponse(bytes)
            bytes.startsWith(ZapConstants.RIDE_ON) -> Logger.d("Empty RideOn response - unencrypted mode")
            !ENCRYPTED || !supportsEncryption() || (bytes.size > Int.SIZE_BYTES + EncryptionUtils.MAC_LENGTH) -> processData(bytes)
            bytes[0] == ZapConstants.DISCONNECT_MESSAGE_TYPE -> Logger.d("Disconnect message")
            else -> Logger.e("Unprocessed - Data Type: ${bytes.toHexString()}")
        }
    }

    private fun processData(bytes: ByteArray) {
        try {

            if (LOG_RAW) Timber.d("Data: ${bytes.toHexString()}")

            val type: Byte
            val message: ByteArray

            if (supportsEncryption() && ENCRYPTED) {
                val counter = bytes.copyOfRange(0, Int.SIZE_BYTES)
                val payload = bytes.copyOfRange(Int.SIZE_BYTES, bytes.size)

                val data = zapEncryption.decrypt(counter, payload)
                type = data[0]
                message = data.copyOfRange(1, data.size)
            } else {
                type = bytes[0]
                message = bytes.copyOfRange(1, bytes.size)
            }

            when (type) {
                EMPTY_MESSAGE_TYPE -> if (LOG_RAW) Logger.d("Empty Message") // expected when nothing happening
                BATTERY_LEVEL_TYPE -> {
                    val notification = BatteryStatus(message)
                    if (batteryLevel != notification.level) {
                        batteryLevel = notification.level
                        Logger.d("Battery level update: $batteryLevel")
                    }
                }
                else -> processInnerDataType(type, message)
            }

        } catch (ex: Exception) {
            Logger.e("Data processing failed: " + ex.message)
        }
    }

    abstract fun processInnerDataType(type: Byte, message: ByteArray)

    fun buildHandshakeStart(): ByteArray {
        if (supportsEncryption() && ENCRYPTED)
            return ZapConstants.RIDE_ON.plus(ZapConstants.REQUEST_START).plus(localKeyProvider.getPublicKeyBytes())
        return ZapConstants.RIDE_ON
    }

    private fun processDevicePublicKeyResponse(bytes: ByteArray) {
        // only if encryption enabled
        devicePublicKeyBytes = bytes.copyOfRange(ZapConstants.RIDE_ON.size + ZapConstants.RESPONSE_START.size, bytes.size)
        zapEncryption.initialise(devicePublicKeyBytes!!)
        if (LOG_RAW) Logger.d("Device Public Key - ${devicePublicKeyBytes!!.toHexString()}")
    }

}