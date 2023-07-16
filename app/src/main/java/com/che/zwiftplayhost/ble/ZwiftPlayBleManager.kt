package com.che.zwiftplayhost.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.APPEARANCE_CHARACTERISTIC_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.BATTERY_LEVEL_CHARACTERISTIC_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.BATTERY_SERVICE_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.DEVICE_INFORMATION_SERVICE_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.DEVICE_NAME_CHARACTERISTIC_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.FIRMWARE_REVISION_STRING_CHARACTERISTIC_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.GENERIC_ACCESS_SERVICE_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.GENERIC_ATTRIBUTE_SERVICE_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.HARDWARE_REVISION_STRING_CHARACTERISTIC_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.MANUFACTURER_NAME_STRING_CHARACTERISTIC_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.PLAY_CONTROLLER_SERVICE_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_2_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_3_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_4_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_6_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.SERIAL_NUMBER_STRING_CHARACTERISTIC_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.SERVICE_CHANGED_CHARACTERISTIC_UUID
import com.che.zwiftplayhost.utils.Logger
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import java.nio.ByteBuffer

class ZwiftPlayBleManager(context: Context) : BleManager(context) {

    companion object {
        private const val TAG = "ZwiftPlayBleManager"
    }

    private var deviceNameCharacteristic: BluetoothGattCharacteristic? = null
    private var appearanceCharacteristic: BluetoothGattCharacteristic? = null

    private var serviceChangedCharacteristic: BluetoothGattCharacteristic? = null

    private var manufacturerCharacteristic: BluetoothGattCharacteristic? = null
    private var serialCharacteristic: BluetoothGattCharacteristic? = null
    private var hardwareRevisionCharacteristic: BluetoothGattCharacteristic? = null
    private var softwareRevisionCharacteristic: BluetoothGattCharacteristic? = null

    private var controller2Characteristic: BluetoothGattCharacteristic? = null
    private var controller3Characteristic: BluetoothGattCharacteristic? = null
    private var controller4Characteristic: BluetoothGattCharacteristic? = null
    private var controller6Characteristic: BluetoothGattCharacteristic? = null

    private var batteryCharacteristic: BluetoothGattCharacteristic? = null

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {

        val genericAccessService = gatt.getService(GENERIC_ACCESS_SERVICE_UUID)
        if (genericAccessService != null) {
            deviceNameCharacteristic = genericAccessService.getCharacteristic(DEVICE_NAME_CHARACTERISTIC_UUID)
            appearanceCharacteristic = genericAccessService.getCharacteristic(APPEARANCE_CHARACTERISTIC_UUID)
        }

        val genericAttributeService = gatt.getService(GENERIC_ATTRIBUTE_SERVICE_UUID)
        if (genericAttributeService != null) {
            serviceChangedCharacteristic = genericAttributeService.getCharacteristic(SERVICE_CHANGED_CHARACTERISTIC_UUID)
        }

        val deviceInfoService = gatt.getService(DEVICE_INFORMATION_SERVICE_UUID)
        if (deviceInfoService != null) {
            manufacturerCharacteristic = deviceInfoService.getCharacteristic(MANUFACTURER_NAME_STRING_CHARACTERISTIC_UUID)
            serialCharacteristic = deviceInfoService.getCharacteristic(SERIAL_NUMBER_STRING_CHARACTERISTIC_UUID)
            hardwareRevisionCharacteristic = deviceInfoService.getCharacteristic(HARDWARE_REVISION_STRING_CHARACTERISTIC_UUID)
            softwareRevisionCharacteristic = deviceInfoService.getCharacteristic(FIRMWARE_REVISION_STRING_CHARACTERISTIC_UUID)
        }

        val controllerService = gatt.getService(PLAY_CONTROLLER_SERVICE_UUID)
        if (controllerService != null) {
            //debugPrintBluetoothService("PlayController", controllerService)
            controller2Characteristic = controllerService.getCharacteristic(PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_2_UUID)
            controller3Characteristic = controllerService.getCharacteristic(PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_3_UUID)
            controller4Characteristic = controllerService.getCharacteristic(PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_4_UUID)
            controller6Characteristic = controllerService.getCharacteristic(PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_6_UUID)
        }

        val batteryService = gatt.getService(BATTERY_SERVICE_UUID)
        if (batteryService != null) {
            batteryCharacteristic = batteryService.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
        }

        val serviceChangedValid = serviceChangedCharacteristic != null && (serviceChangedCharacteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0)

        val controller2Valid = controller2Characteristic != null && (controller2Characteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)
        val controller4Valid = controller4Characteristic != null && (controller4Characteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0)
        val controller6Valid = controller6Characteristic != null && (controller6Characteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0)

        val batteryValid = batteryCharacteristic != null && (batteryCharacteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)

        return deviceNameCharacteristic != null && appearanceCharacteristic != null
                && serviceChangedValid
                && manufacturerCharacteristic != null && serialCharacteristic != null
                && hardwareRevisionCharacteristic != null && softwareRevisionCharacteristic != null
                && controller2Valid && controller3Characteristic != null && controller4Valid
                && controller6Valid && batteryValid
    }

    override fun initialize() {

        Logger.d(TAG, "Initialize")

        setIndicationCallback(serviceChangedCharacteristic).with { _, data ->
            getHexStringValue(data)?.let {
                Logger.d(TAG, "Service Changed $it")
            }
        }

        setNotificationCallback(controller2Characteristic).with { _, data ->
            getHexStringValue(data)?.let {
                Logger.d(TAG, "2 $it")
            }
        }

        setIndicationCallback(controller4Characteristic).with { _, data ->
            getHexStringValue(data)?.let {
                Logger.d(TAG, "4 $it")
            }
        }
        setIndicationCallback(controller6Characteristic).with { _, data ->
            getHexStringValue(data)?.let {
                Logger.d(TAG, "6 $it")
            }
        }

        setNotificationCallback(batteryCharacteristic).with { _, data ->
            getIntValue(data)?.let {
                Logger.d(TAG, "Battery: $it")
            }
        }

        beginAtomicRequestQueue()
            .add(enableIndications(serviceChangedCharacteristic)
                .fail { _: BluetoothDevice?, status: Int -> failCallback(status) }
            )
            .add(enableNotifications(controller2Characteristic)
                .fail { _: BluetoothDevice?, status: Int -> failCallback(status) }
            )
            .add(enableIndications(controller4Characteristic)
                .fail { _: BluetoothDevice?, status: Int -> failCallback(status) }
            )
            .add(enableIndications(controller6Characteristic)
                .fail { _: BluetoothDevice?, status: Int -> failCallback(status) }
            )
            .add(enableNotifications(batteryCharacteristic)
                .fail { _: BluetoothDevice?, status: Int -> failCallback(status) }
            )
            .add(readCharacteristic(deviceNameCharacteristic).with { _, data ->
                getStringValue(data)?.let {
                    Logger.d(TAG, "Device Name: $it")
                }
            })
            .add(readCharacteristic(appearanceCharacteristic).with { _, data ->
                getHexStringValue(data)?.let {
                    Logger.d(TAG, "Appearance: $it")
                }
            })
            .add(readCharacteristic(manufacturerCharacteristic).with { _, data ->
                getStringValue(data)?.let {
                    Logger.d(TAG, "Manufacturer: $it")
                }
            })
            .add(readCharacteristic(serialCharacteristic).with { _, data ->
                getStringValue(data)?.let {
                    Logger.d(TAG, "Serial: $it")
                }
            })
            .add(readCharacteristic(hardwareRevisionCharacteristic).with { _, data ->
                getStringValue(data)?.let {
                    Logger.d(TAG, "Hardware: $it")
                }
            })
            .add(readCharacteristic(softwareRevisionCharacteristic).with { _, data ->
                getStringValue(data)?.let {
                    Logger.d(TAG, "Software: $it")
                }
            })
            .add(readCharacteristic(batteryCharacteristic).with { _, data ->
                getIntValue(data)?.let {
                    Logger.d(TAG, "Battery: $it")
                }
            })

            .add(readCharacteristic(controller4Characteristic).with { _, data ->
                getHexStringValue(data)?.let {
                    Logger.d(TAG, "4: $it")
                }
            })
            .add(readCharacteristic(controller6Characteristic).with { _, data ->
                getHexStringValue(data)?.let {
                    Logger.d(TAG, "6: $it")
                }
            })

            .done {
                Logger.d(TAG, "Initialisation Complete")
            }
            .enqueue()
    }

    private fun getHexStringValue(data: Data): String? {
        data.value?.let {
            return it.toHexString()
        }
        return null
    }

    private fun getStringValue(data: Data): String? {
        data.value?.let {
            return String(data.value!!, Charsets.UTF_8)
        }
        return null
    }

    private fun getIntValue(data: Data): Int? {
        data.value?.let {
            if (it.size == 1)
                return it[0].toInt()
            if (it.size == 2)
                return ByteBuffer.wrap(it).short.toInt()
            if (it.size == 4)
                return ByteBuffer.wrap(it).int
        }
        return null
    }

    private fun failCallback(status: Int) {
        log(Log.ERROR, "Could not subscribe: $status")
        disconnect().enqueue()
    }

    override fun onServicesInvalidated() {

        deviceNameCharacteristic = null
        appearanceCharacteristic = null

        serviceChangedCharacteristic = null

        manufacturerCharacteristic = null
        serialCharacteristic = null
        hardwareRevisionCharacteristic = null
        softwareRevisionCharacteristic = null

        controller2Characteristic = null
        controller3Characteristic = null
        controller4Characteristic = null
        controller6Characteristic = null

        batteryCharacteristic = null
    }
}

const val PREFIX = "0x"
private fun ByteArray.toHexString(): String {
    val result = asUByteArray().joinToString(" $PREFIX", prefix = PREFIX, postfix = " ") { it.toString(16).uppercase().padStart(2, '0') }
    if (result != "$PREFIX ")
        return result
    return ""
}
