package com.che.zwiftplayhost.ble.zap

import com.che.zwiftplayhost.ble.zap.utils.EncryptionUtils
import com.che.zwiftplayhost.ble.zap.utils.LocalKeyProvider
import com.che.zwiftplayhost.ble.zap.utils.ZapCrypto
import com.che.zwiftplayhost.utils.Logger
import com.che.zwiftplayhost.utils.startsWith
import com.che.zwiftplayhost.utils.toHexString
import timber.log.Timber

abstract class AbstractZapDevice {

    companion object {
        internal const val LOG_RAW = false
    }

    private var devicePublicKeyBytes: ByteArray? = null
    private var localKeyProvider = LocalKeyProvider()
    protected var zapEncryption = ZapCrypto(localKeyProvider)

    fun processCharacteristic(characteristicName: String, bytes: ByteArray?) {
        if (bytes == null) return

        if (LOG_RAW) Timber.d("$characteristicName ${bytes.toHexString()}")

        when {
            bytes.startsWith(ZapConstants.RIDE_ON.plus(ZapConstants.RESPONSE_START)) -> processDevicePublicKeyResponse(bytes)
            bytes.size > Int.SIZE_BYTES + EncryptionUtils.MAC_LENGTH -> processEncryptedData(bytes)
            else -> Logger.e("Unprocessed - Data Type: ${bytes.toHexString()}")
        }
    }

    abstract fun processEncryptedData(bytes: ByteArray)

    fun buildHandshakeStart(): ByteArray {
        return ZapConstants.RIDE_ON.plus(ZapConstants.REQUEST_START).plus(localKeyProvider.getPublicKeyBytes())
    }

    private fun processDevicePublicKeyResponse(bytes: ByteArray) {
        devicePublicKeyBytes = bytes.copyOfRange(ZapConstants.RIDE_ON.size + ZapConstants.RESPONSE_START.size, bytes.size)
        zapEncryption.initialise(devicePublicKeyBytes!!)
        if (LOG_RAW) Logger.d("Device Public Key - ${devicePublicKeyBytes!!.toHexString()}")
    }

}