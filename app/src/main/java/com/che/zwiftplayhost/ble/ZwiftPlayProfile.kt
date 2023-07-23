package com.che.zwiftplayhost.ble

import android.util.Log
import com.che.zwiftplayhost.ble.BleUuids.uuidFromShortString
import com.che.zwiftplayhost.utils.Logger
import com.che.zwiftplayhost.utils.startsWith
import com.che.zwiftplayhost.utils.toHexString
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

/*
All the services / characteristics discovered using Nordic nRF Connect. Comments show values returned

The Zwift custom characteristic details were found from decompiling the Zwift Companion app and searching for the service UUID
 */
object ZwiftPlayProfile {

    val GENERIC_ACCESS_SERVICE_UUID = uuidFromShortString("1800")
    val DEVICE_NAME_CHARACTERISTIC_UUID =  uuidFromShortString("2A00") // Zwift Play
    val APPEARANCE_CHARACTERISTIC_UUID =  uuidFromShortString("2A01") // [964] Gamepad (HID Subtype)

    val GENERIC_ATTRIBUTE_SERVICE_UUID = uuidFromShortString("1801")
    val SERVICE_CHANGED_CHARACTERISTIC_UUID =  uuidFromShortString("2A05")

    val DEVICE_INFORMATION_SERVICE_UUID =  uuidFromShortString("180A")
    val MANUFACTURER_NAME_STRING_CHARACTERISTIC_UUID =  uuidFromShortString("2A29")  // Zwift Inc.
    val SERIAL_NUMBER_STRING_CHARACTERISTIC_UUID =  uuidFromShortString("2A25") // 02-1[MAC]
    val HARDWARE_REVISION_STRING_CHARACTERISTIC_UUID =  uuidFromShortString("2A27") // B.0
    val FIRMWARE_REVISION_STRING_CHARACTERISTIC_UUID =  uuidFromShortString("2A26") // 1.1.0

    val ZWIFT_CUSTOM_SERVICE_UUID: UUID = UUID.fromString("00000001-19CA-4651-86E5-FA29DCDD09D1")
    val ZWIFT_ASYNC_CHARACTERISTIC_UUID: UUID = UUID.fromString("00000002-19CA-4651-86E5-FA29DCDD09D1")
    val ZWIFT_SYNC_RX_CHARACTERISTIC_UUID: UUID = UUID.fromString("00000003-19CA-4651-86E5-FA29DCDD09D1")
    val ZWIFT_SYNC_TX_CHARACTERISTIC_UUID: UUID = UUID.fromString("00000004-19CA-4651-86E5-FA29DCDD09D1")
    // This doesn't appear in the real hardware but is found in the companion app code.
    // val ZWIFT_DEBUG_CHARACTERISTIC_UUID: UUID = UUID.fromString("00000005-19CA-4651-86E5-FA29DCDD09D1")
    // I have not seen this characteristic used. Guess it could be for Device Firmware Update (DFU)? it is a chip from Nordic.
    val ZWIFT_UNKNOWN_6_CHARACTERISTIC_UUID: UUID = UUID.fromString("00000006-19CA-4651-86E5-FA29DCDD09D1")

    val BATTERY_SERVICE_UUID = uuidFromShortString("180F")
    val BATTERY_LEVEL_CHARACTERISTIC_UUID = uuidFromShortString("2A19") // 89
}

object BleUuids {

    private const val BT_SIG_UUID_PREFIX = "0000"
    private const val BT_SIG_UUID_SUFFIX = "-0000-1000-8000-00805F9B34FB"

    fun uuidFromShortString(shortUuid: String): UUID {
        return UUID.fromString("$BT_SIG_UUID_PREFIX$shortUuid$BT_SIG_UUID_SUFFIX")
    }
}

object ZwiftData {

    private const val TAG = "ZwiftData"

    private val RIDE_ON = byteArrayOf(82, 105, 100, 101, 79, 110)
    private val RIDE_ON_REQUEST_START = byteArrayOf(1, 2)
    private val RIDE_ON_RESPONSE_START = byteArrayOf(1, 1) // from device

    fun buildHandshakeStart(): ByteArray {
        return RIDE_ON.plus(RIDE_ON_REQUEST_START).plus(publicKeyData)
    }

    fun processCharacteristic(characteristicName: String, bytes: ByteArray?) {
        if (bytes == null) return

        Log.d(TAG, "$characteristicName ${bytes.toHexString()}")

        var stringValue: String

        if (bytes.startsWith(RIDE_ON.plus(RIDE_ON_RESPONSE_START))) {
            // the starting handshake
            stringValue = "RideOn " + bytes.copyOfRange(RIDE_ON.size + RIDE_ON_RESPONSE_START.size, bytes.size).toHexString()
        } else {
            // any other data appears to start with a sequence number
            val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            stringValue = "Seq " + bb.int

            /*stringValue += " " + bb.get()
            stringValue += " " + bb.short
            if (bb.remaining() >= 2)
                stringValue += " " + bb.short*/

            val data = ByteArray(bb.remaining())
            bb.get(data)
            stringValue += " Data " + data.toHexString()
        }

        Logger.d(TAG, "$characteristicName: $stringValue")
    }

    // in case this includes my zwift ID or the real hardware ID I've not included this
    private val publicKeyData = byteArrayOf()
}

