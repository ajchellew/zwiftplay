package com.che.zap.click

import com.che.zap.device.AbstractZapDevice
import com.che.zap.device.ZapConstants.CLICK_NOTIFICATION_MESSAGE_TYPE
import com.che.zap.proto.ClickNotification
import com.che.zap.utils.Logger
import com.che.zap.utils.toHexString

class ZwiftClickDevice : AbstractZapDevice() {

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