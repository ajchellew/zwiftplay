package com.che.zwiftplayhost.ble

import com.che.zwiftplayhost.ble.BleUuids.uuidFromShortString
import java.util.UUID

/*
All the services / characteristics discovered using Nordic nRF Connect. Comments show values returned
 */
object ZwiftPlayProfile {

    val GENERIC_ACCESS_SERVICE_UUID = uuidFromShortString("1800")
    val DEVICE_NAME_CHARACTERISTIC_UUID =  uuidFromShortString("2A00") // Zwift Play
    val APPEARANCE_CHARACTERISTIC_UUID =  uuidFromShortString("2A01") // [964] Gamepad (HID Subtype)

    val DEVICE_INFORMATION_SERVICE_UUID =  uuidFromShortString("180A")
    val MANUFACTURER_NAME_STRING_CHARACTERISTIC_UUID =  uuidFromShortString("2A29")  // Zwift Inc.
    val SERIAL_NUMBER_STRING_CHARACTERISTIC_UUID =  uuidFromShortString("2A25") // 02-1[MAC]
    val HARDWARE_REVISION_STRING_CHARACTERISTIC_UUID =  uuidFromShortString("2A27") // B.0
    val FIRMWARE_REVISION_STRING_CHARACTERISTIC_UUID =  uuidFromShortString("2A26") // 1.1.0

    val PLAY_CONTROLLER_SERVICE_UUID: UUID = UUID.fromString("00000001-19CA-4651-86E5-FA29DCDD09D1")
    val PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_2_UUID: UUID = UUID.fromString("00000002-19CA-4651-86E5-FA29DCDD09D1")
    val PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_3_UUID: UUID = UUID.fromString("00000003-19CA-4651-86E5-FA29DCDD09D1")
    val PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_4_UUID: UUID = UUID.fromString("00000004-19CA-4651-86E5-FA29DCDD09D1")
    val PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_6_UUID: UUID = UUID.fromString("00000006-19CA-4651-86E5-FA29DCDD09D1")

    val BATTERY_SERVICE_UUID = uuidFromShortString("180F")
    val BATTERY_LEVEL_CHARACTERISTIC_UUID = uuidFromShortString("2A19") // 89
}

object BleUuids {

    private const val STANDARD_BT_UUID_PREFIX = "0000"
    private const val STANDARD_BT_UUID_SUFFIX = "-0000-1000-8000-00805F9B34FB"

    fun uuidFromShortString(shortUuid: String): UUID {
        return UUID.fromString("$STANDARD_BT_UUID_PREFIX$shortUuid$STANDARD_BT_UUID_SUFFIX")
    }
}
