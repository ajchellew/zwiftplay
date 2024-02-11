package com.che.zap.device

import com.che.zap.device.common.AbstractZapDevice
import com.che.zap.device.common.ZapConstants.CONTROLLER_NOTIFICATION_MESSAGE_TYPE
import com.che.zap.proto.ControllerNotification
import com.che.zap.utils.Logger
import com.che.zap.utils.toHexString

class ZwiftPlay : AbstractZapDevice() {

    private var lastButtonState: ControllerNotification? = null

    override fun processInnerDataType(type: Byte, message: ByteArray) {
        when (type) {
            CONTROLLER_NOTIFICATION_MESSAGE_TYPE -> processButtonNotification(ControllerNotification(message))
            else -> Logger.e("Unprocessed - Type: ${type.toUByte().toHexString()} Data: ${message.toHexString()}")
        }
    }

    private fun processButtonNotification(notification: ControllerNotification) {
        if (lastButtonState == null)
            Logger.d(notification.toString())
        else {
            val diff = notification.diff(lastButtonState!!)
            if (diff.isNotBlank()) // get repeats of the same state
                Logger.d(diff)
        }
        lastButtonState = notification
    }
}

