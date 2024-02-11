package com.che.zap.device

import com.che.zap.device.common.AbstractZapDevice
import com.che.zap.utils.Logger
import com.che.zap.utils.ProtoDecode
import com.che.zap.utils.toHexString

class KickrCore : AbstractZapDevice() {

    companion object {

        const val BLUETOOTH_PREFIX = "KICKR CORE"

        const val TRAINER_STATE_TYPE = 3.toByte() // TrainerNotification type?
        const val TWO_A_TYPE = 42.toByte() // this seems like a generic thing, RIDE_ON(0) in data.
    }

    // at least it doesn't seem to
    override fun supportsEncryption(): Boolean {
        return false
    }

    override fun processInnerDataType(type: Byte, message: ByteArray) {
        when (type) {
            TRAINER_STATE_TYPE -> Logger.d("State - Data: ${ProtoDecode.decodeProto(message)}")
            TWO_A_TYPE -> Logger.d("TwoA - Data: ${ProtoDecode.decodeProto(message)}")
            else -> Logger.e("Unprocessed - Type: ${type.toUByte().toHexString()} - ${ProtoDecode.decodeProto(message)}")
        }
    }

}