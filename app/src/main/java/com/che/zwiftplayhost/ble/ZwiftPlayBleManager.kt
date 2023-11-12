package com.che.zwiftplayhost.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.content.Context
import android.util.Log
import com.che.common.ble.BleDebugUtils.debugPrintBluetoothService
import com.che.zap.device.GenericBleUuids.APPEARANCE_CHARACTERISTIC_UUID
import com.che.zap.device.GenericBleUuids.BATTERY_LEVEL_CHARACTERISTIC_UUID
import com.che.zap.device.GenericBleUuids.BATTERY_SERVICE_UUID
import com.che.zap.device.GenericBleUuids.DEVICE_INFORMATION_SERVICE_UUID
import com.che.zap.device.GenericBleUuids.DEVICE_NAME_CHARACTERISTIC_UUID
import com.che.zap.device.GenericBleUuids.FIRMWARE_REVISION_STRING_CHARACTERISTIC_UUID
import com.che.zap.device.GenericBleUuids.GENERIC_ACCESS_SERVICE_UUID
import com.che.zap.device.GenericBleUuids.GENERIC_ATTRIBUTE_SERVICE_UUID
import com.che.zap.device.GenericBleUuids.HARDWARE_REVISION_STRING_CHARACTERISTIC_UUID
import com.che.zap.device.GenericBleUuids.MANUFACTURER_NAME_STRING_CHARACTERISTIC_UUID
import com.che.zap.device.ZapBleUuids.ZWIFT_CUSTOM_SERVICE_UUID
import com.che.zap.device.ZapBleUuids.ZWIFT_ASYNC_CHARACTERISTIC_UUID
import com.che.zap.device.ZapBleUuids.ZWIFT_SYNC_RX_CHARACTERISTIC_UUID
import com.che.zap.device.ZapBleUuids.ZWIFT_SYNC_TX_CHARACTERISTIC_UUID
import com.che.zap.device.ZapBleUuids.ZWIFT_UNKNOWN_6_CHARACTERISTIC_UUID
import com.che.zap.device.GenericBleUuids.SERIAL_NUMBER_STRING_CHARACTERISTIC_UUID
import com.che.zap.device.GenericBleUuids.SERVICE_CHANGED_CHARACTERISTIC_UUID
import com.che.zap.device.AbstractZapDevice
import com.che.zap.play.ZwiftPlayDevice
import com.che.zap.utils.Logger
import com.che.zap.utils.toHexString
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class ZwiftPlayBleManager(context: Context, val isLeft: Boolean) : BleManager(context) {

    private lateinit var zapDevice: AbstractZapDevice

    // region Characteristics

    private var deviceNameCharacteristic: BluetoothGattCharacteristic? = null
    private var appearanceCharacteristic: BluetoothGattCharacteristic? = null

    private var serviceChangedCharacteristic: BluetoothGattCharacteristic? = null

    private var manufacturerCharacteristic: BluetoothGattCharacteristic? = null
    private var serialCharacteristic: BluetoothGattCharacteristic? = null
    private var hardwareRevisionCharacteristic: BluetoothGattCharacteristic? = null
    private var softwareRevisionCharacteristic: BluetoothGattCharacteristic? = null

    private var asyncCharacteristic: BluetoothGattCharacteristic? = null
    private var syncRxCharacteristic: BluetoothGattCharacteristic? = null
    private var syncTxCharacteristic: BluetoothGattCharacteristic? = null
    private var unknown6Characteristic: BluetoothGattCharacteristic? = null

    private var batteryCharacteristic: BluetoothGattCharacteristic? = null

    // endregion

    // region Properties

    private var _serialNumber = ""

    var serialNumber
        get() = _serialNumber
        set(value) {
            Logger.d("Serial: $value")
            _serialNumber = value
        }

    private var _batteryLevel = Int.MIN_VALUE

    var batteryLevel
        get() = _batteryLevel
        set(value) {
            Logger.d("Battery: $value")
            _batteryLevel = value
        }

    // endregion

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {

        val genericAccessService = gatt.getService(GENERIC_ACCESS_SERVICE_UUID)
        if (genericAccessService != null) {
            //debugPrintBluetoothService("GenericAccessService", genericAccessService)
            deviceNameCharacteristic = genericAccessService.getCharacteristic(DEVICE_NAME_CHARACTERISTIC_UUID)
            appearanceCharacteristic = genericAccessService.getCharacteristic(APPEARANCE_CHARACTERISTIC_UUID)
        }

        val genericAttributeService = gatt.getService(GENERIC_ATTRIBUTE_SERVICE_UUID)
        if (genericAttributeService != null) {
            debugPrintBluetoothService("GenericAttributeService", genericAttributeService)
            serviceChangedCharacteristic = genericAttributeService.getCharacteristic(SERVICE_CHANGED_CHARACTERISTIC_UUID)
        }

        val deviceInfoService = gatt.getService(DEVICE_INFORMATION_SERVICE_UUID)
        if (deviceInfoService != null) {
            //debugPrintBluetoothService("DeviceInfo", deviceInfoService)
            manufacturerCharacteristic = deviceInfoService.getCharacteristic(MANUFACTURER_NAME_STRING_CHARACTERISTIC_UUID)
            serialCharacteristic = deviceInfoService.getCharacteristic(SERIAL_NUMBER_STRING_CHARACTERISTIC_UUID)
            hardwareRevisionCharacteristic = deviceInfoService.getCharacteristic(HARDWARE_REVISION_STRING_CHARACTERISTIC_UUID)
            softwareRevisionCharacteristic = deviceInfoService.getCharacteristic(FIRMWARE_REVISION_STRING_CHARACTERISTIC_UUID)
        }

        val controllerService = gatt.getService(ZWIFT_CUSTOM_SERVICE_UUID)
        if (controllerService != null) {
            //debugPrintBluetoothService("PlayController", controllerService)
            asyncCharacteristic = controllerService.getCharacteristic(ZWIFT_ASYNC_CHARACTERISTIC_UUID)
            syncRxCharacteristic = controllerService.getCharacteristic(ZWIFT_SYNC_RX_CHARACTERISTIC_UUID)
            syncTxCharacteristic = controllerService.getCharacteristic(ZWIFT_SYNC_TX_CHARACTERISTIC_UUID)
            unknown6Characteristic = controllerService.getCharacteristic(ZWIFT_UNKNOWN_6_CHARACTERISTIC_UUID)
        }

        val batteryService = gatt.getService(BATTERY_SERVICE_UUID)
        if (batteryService != null) {
            //debugPrintBluetoothService("BatteryService", batteryService)
            batteryCharacteristic = batteryService.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
        }

        val serviceChangedValid = serviceChangedCharacteristic != null && (serviceChangedCharacteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0)

        val asyncValid = asyncCharacteristic != null && (asyncCharacteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)
        val syncTxValid = syncTxCharacteristic != null && (syncTxCharacteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0)
        val unknown6Valid = unknown6Characteristic != null && (unknown6Characteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0)

        val batteryValid = batteryCharacteristic != null && (batteryCharacteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)

        return deviceNameCharacteristic != null && appearanceCharacteristic != null && serviceChangedValid
                && manufacturerCharacteristic != null && serialCharacteristic != null
                && hardwareRevisionCharacteristic != null && softwareRevisionCharacteristic != null
                && asyncValid && syncRxCharacteristic != null && syncTxValid && unknown6Valid && batteryValid
    }

    override fun initialize() {

        Logger.d("Initialize ${if (isLeft) "Left" else "Right"} Controller")

        zapDevice = ZwiftPlayDevice()

        setIndicationCallback(serviceChangedCharacteristic).with { _, data ->
            getHexStringValue(data)?.let {
                Logger.d("Service Changed $it")
            }
        }

        setNotificationCallback(asyncCharacteristic).with { _, data ->
            zapDevice.processCharacteristic("Async", data.value)
        }

        setIndicationCallback(syncTxCharacteristic).with { _, data ->
            zapDevice.processCharacteristic("SyncTx", data.value)
        }
        setIndicationCallback(unknown6Characteristic).with { _, data ->
            getHexStringValue(data)?.let {
                Logger.d("6 $it")
            }
        }

        setNotificationCallback(batteryCharacteristic).with { _, data ->
            batteryLevel = byteToInt(data)
        }

        beginAtomicRequestQueue()
            .add(enableIndications(serviceChangedCharacteristic)
                .fail { _: BluetoothDevice?, status: Int -> failCallback(serviceChangedCharacteristic!!, status) }
            )
            .add(enableNotifications(asyncCharacteristic)
                .fail { _: BluetoothDevice?, status: Int -> failCallback(asyncCharacteristic!!, status) }
            )
            .add(enableIndications(syncTxCharacteristic)
                .fail { _: BluetoothDevice?, status: Int -> failCallback(syncTxCharacteristic!!, status) }
            )
            .add(enableIndications(unknown6Characteristic)
                .fail { _: BluetoothDevice?, status: Int -> failCallback(unknown6Characteristic!!, status) }
            )
            .add(enableNotifications(batteryCharacteristic)
                .fail { _: BluetoothDevice?, status: Int -> failCallback(batteryCharacteristic!!, status) }
            )
            .add(readCharacteristic(deviceNameCharacteristic).with { _, data ->
                getStringValue(data)?.let {
                    Logger.d("Device Name: $it")
                }
            })
            .add(readCharacteristic(appearanceCharacteristic).with { _, data ->
                getHexStringValue(data)?.let {
                    Logger.d("Appearance: $it")
                }
            })
            .add(readCharacteristic(manufacturerCharacteristic).with { _, data ->
                getStringValue(data)?.let {
                    Logger.d("Manufacturer: $it")
                }
            })
            .add(readCharacteristic(serialCharacteristic).with { _, data ->
                getStringValue(data)?.let {
                    serialNumber = it
                }
            })
            .add(readCharacteristic(hardwareRevisionCharacteristic).with { _, data ->
                getStringValue(data)?.let {
                    Logger.d("Hardware: $it")
                }
            })
            .add(readCharacteristic(softwareRevisionCharacteristic).with { _, data ->
                getStringValue(data)?.let {
                    Logger.d("Software: $it")
                }
            })
            .add(readCharacteristic(batteryCharacteristic).with { _, data ->
                batteryLevel = byteToInt(data)
            })
            .add(writeCharacteristic(syncRxCharacteristic, zapDevice.buildHandshakeStart(), WRITE_TYPE_DEFAULT).with { _, data ->
                Logger.d("Written ${data.value?.toHexString()}")
            })
            .done {
                for (listener in listeners) {
                    listener.initialised(bluetoothDevice!!.address)
                }
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

    private fun byteToInt(data: Data) = data.getByte(0)?.toInt() ?: Int.MIN_VALUE

    private fun failCallback(characteristic: BluetoothGattCharacteristic, status: Int) {
        Logger.e("Could not subscribe: ${characteristic.uuid} $status")
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

        asyncCharacteristic = null
        syncRxCharacteristic = null
        syncTxCharacteristic = null
        unknown6Characteristic = null

        batteryCharacteristic = null
    }

    // region Listener

    interface Callback {
        fun initialised(address: String)
        fun batteryLevelUpdate(address: String, level: Int)
    }

    // thread-safe set of listeners
    private val mListeners =
        Collections.newSetFromMap(
            ConcurrentHashMap<Callback, Boolean>(1)
        )

    fun registerListener(listener: Callback) {
        mListeners.add(listener)
    }

    fun unregisterListener(listener: Callback) {
        mListeners.remove(listener)
    }

    /**
     * Get a reference to the unmodifiable set containing all the registered listeners.
     */
    private val listeners: Set<Callback>
        get() = Collections.unmodifiableSet(mListeners)

    // endregion
}


