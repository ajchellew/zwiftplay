package com.che.zwiftplayhost.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.BATTERY_LEVEL_CHARACTERISTIC_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.BATTERY_SERVICE_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.DEVICE_INFORMATION_SERVICE_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.FIRMWARE_REVISION_STRING_CHARACTERISTIC_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.HARDWARE_REVISION_STRING_CHARACTERISTIC_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.MANUFACTURER_NAME_STRING_CHARACTERISTIC_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.PLAY_CONTROLLER_SERVICE_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_2_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_3_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_4_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.PLAY_CONTROLLER_UNKNOWN_CHARACTERISTIC_6_UUID
import com.che.zwiftplayhost.ble.ZwiftPlayProfile.SERIAL_NUMBER_STRING_CHARACTERISTIC_UUID
import com.che.zwiftplayhost.utils.Logger
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import java.nio.ByteBuffer

class ZwiftPlayBleManager(context: Context) : BleManager(context) {

    companion object {
        private const val TAG = "ZwiftPlayBleManager"
    }

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

        // ignoring generic service at least for now

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

        val batteryValid = batteryCharacteristic != null && (batteryCharacteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)

        return manufacturerCharacteristic != null && serialCharacteristic != null
                && hardwareRevisionCharacteristic != null && softwareRevisionCharacteristic != null
                && controller2Characteristic != null && controller3Characteristic != null && controller4Characteristic != null
                && controller6Characteristic != null
                && batteryValid

        /*val myCharacteristicProperties = myCharacteristic?.properties ?: 0
        return (myCharacteristicProperties and BluetoothGattCharacteristic.PROPERTY_READ != 0) &&
                (myCharacteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)*/
    }

    override fun initialize() {

        Logger.d(TAG, "Initialize")

        setNotificationCallback(controller2Characteristic).with { _, data ->
            getStringValue(data)?.let {
                Logger.d(TAG, "2 $it")
            }
        }

        setIndicationCallback(controller4Characteristic).with { _, data ->
            getStringValue(data)?.let {
                Logger.d(TAG, "4 $it")
            }
        }
        setIndicationCallback(controller6Characteristic).with { _, data ->
            getStringValue(data)?.let {
                Logger.d(TAG, "6 $it")
            }
        }

        setNotificationCallback(batteryCharacteristic).with { _, data ->
            getIntValue(data)?.let {
                Logger.d(TAG, "Battery: $it")
            }
        }

        beginAtomicRequestQueue()
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
                getStringValue(data)?.let {
                    Logger.d(TAG, "4: $it")
                }
            })
            .add(readCharacteristic(controller6Characteristic).with { _, data ->
                getStringValue(data)?.let {
                    Logger.d(TAG, "6: $it")
                }
            })

            .done {
                Logger.d(TAG, "Initialisation Complete")
            }
            .enqueue()
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