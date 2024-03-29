package com.che.zap.device

import com.che.zap.device.common.AbstractZapDevice
import com.che.zap.device.common.ZapConstants.CLICK_NOTIFICATION_MESSAGE_TYPE
import com.che.zap.proto.ClickNotification
import com.che.zap.utils.Logger
import com.che.zap.utils.toHexString

class ZwiftClick : AbstractZapDevice() {

    private var lastButtonState: ClickNotification? = null

    override fun processInnerDataType(type: Byte, message: ByteArray) {
        when (type) {
            CLICK_NOTIFICATION_MESSAGE_TYPE -> processButtonNotification(ClickNotification(message))
            else -> Logger.e("Unprocessed - Type: ${type.toUByte().toHexString()} Data: ${message.toHexString()}")
        }
    }

    private fun processButtonNotification(notification: ClickNotification) {
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