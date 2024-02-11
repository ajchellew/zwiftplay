package com.che.zap

import com.che.zap.proto.ClickNotification
import org.junit.Test

class ZwiftClickTest {

    // Data Provided by cagnulein on github
    // https://github.com/ajchellew/zwiftplay/pull/3#issuecomment-1929016996
    // I don't have a click to test so this was very helpful

    //"37 08 01 10 01"
    //plus pressed
    //"37 08 00 10 01"
    //minus pressed
    //"37 08 01 10 00"

    @Test
    fun testProvidedClickData() {

        val normal = byteArrayOf(0x08, 0x01, 0x10, 0x01)
        var notification = ClickNotification(normal)
        println(notification.toString())
        assert(!notification.buttonDownPressed)
        assert(!notification.buttonUpPressed)

        val plusPressed = byteArrayOf(0x08, 0x00, 0x10, 0x01)
        notification = ClickNotification(plusPressed)
        println(notification.toString())
        assert(notification.buttonUpPressed)

        val minusPressed = byteArrayOf(0x08, 0x01, 0x10, 0x00)
        notification = ClickNotification(minusPressed)
        println(notification.toString())
        assert(notification.buttonDownPressed)

    }

}

