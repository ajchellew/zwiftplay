package com.che.zap.device.common

object ZapConstants {

    const val ZWIFT_MANUFACTURER_ID = 2378 // Zwift, Inc

    // Zwift Play = RC1
    const val RC1_LEFT_SIDE: Byte = 3
    const val RC1_RIGHT_SIDE: Byte = 2

    // Zwift Click = BC1
    const val BC1: Byte = 9

    val RIDE_ON = byteArrayOf(82, 105, 100, 101, 79, 110)

    // these don't actually seem to matter, its just the header has to be 7 bytes RIDEON + 2
    val REQUEST_START = byteArrayOf(0, 9) //byteArrayOf(1, 2)
    val RESPONSE_START = byteArrayOf(1, 3) // from device

    // Message types received from device
    const val CONTROLLER_NOTIFICATION_MESSAGE_TYPE = 7.toByte()
    const val EMPTY_MESSAGE_TYPE = 21.toByte()
    const val BATTERY_LEVEL_TYPE = 25.toByte()

    // not figured out the protobuf type this really is, the content is just two varints.
    const val CLICK_NOTIFICATION_MESSAGE_TYPE = 55.toByte()

    // see this if connected to Core then Zwift connects to it. just one byte
    const val DISCONNECT_MESSAGE_TYPE = 0xFE.toByte()
}