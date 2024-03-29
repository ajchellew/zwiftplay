package com.che.zwiftplayhost.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.content.Context
import com.che.zap.device.DeviceType
import com.che.zap.device.KickrCore
import com.che.zap.device.ZwiftClick
import com.che.zap.device.common.GenericBleUuids.APPEARANCE_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.BATTERY_LEVEL_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.BATTERY_SERVICE_UUID
import com.che.zap.device.common.GenericBleUuids.DEVICE_INFORMATION_SERVICE_UUID
import com.che.zap.device.common.GenericBleUuids.DEVICE_NAME_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.FIRMWARE_REVISION_STRING_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.GENERIC_ACCESS_SERVICE_UUID
import com.che.zap.device.common.GenericBleUuids.GENERIC_ATTRIBUTE_SERVICE_UUID
import com.che.zap.device.common.GenericBleUuids.HARDWARE_REVISION_STRING_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.MANUFACTURER_NAME_STRING_CHARACTERISTIC_UUID
import com.che.zap.device.common.ZapBleUuids.ZWIFT_CUSTOM_SERVICE_UUID
import com.che.zap.device.common.ZapBleUuids.ZWIFT_ASYNC_CHARACTERISTIC_UUID
import com.che.zap.device.common.ZapBleUuids.ZWIFT_SYNC_RX_CHARACTERISTIC_UUID
import com.che.zap.device.common.ZapBleUuids.ZWIFT_SYNC_TX_CHARACTERISTIC_UUID
import com.che.zap.device.common.ZapBleUuids.ZWIFT_UNKNOWN_6_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.SERIAL_NUMBER_STRING_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.SERVICE_CHANGED_CHARACTERISTIC_UUID
import com.che.zap.device.common.AbstractZapDevice
import com.che.zap.device.ZwiftPlay
import com.che.zap.utils.Logger
import com.che.zap.utils.toHexString
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class ZwiftAccessoryBleManager(context: Context, val type: DeviceType) : BleManager(context) {

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
            //debugPrintBluetoothService("GenericAttributeService", genericAttributeService)
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
        val unknown6Valid = unknown6Characteristic != null && (unknown6Characteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0)  || type == DeviceType.WAHOO_KICKR_CORE

        val batteryValid = batteryCharacteristic != null && (batteryCharacteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) || type == DeviceType.WAHOO_KICKR_CORE

        return deviceNameCharacteristic != null && appearanceCharacteristic != null && serviceChangedValid
                && manufacturerCharacteristic != null && serialCharacteristic != null
                && hardwareRevisionCharacteristic != null && softwareRevisionCharacteristic != null
                && asyncValid && syncRxCharacteristic != null && syncTxValid && unknown6Valid && batteryValid
    }

    override fun initialize() {

        Logger.d("Initialize ${type.description}")

        zapDevice = when (type) {
            DeviceType.ZWIFT_PLAY_LEFT, DeviceType.ZWIFT_PLAY_RIGHT -> ZwiftPlay()
            DeviceType.ZWIFT_CLICK -> ZwiftClick()
            DeviceType.WAHOO_KICKR_CORE -> KickrCore()
            else -> throw Exception("Unknown device type")
        }

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

        if (unknown6Characteristic != null) {
            setIndicationCallback(unknown6Characteristic).with { _, data ->
                getHexStringValue(data)?.let {
                    Logger.d("6 $it")
                }
            }
        }

        if (batteryCharacteristic != null) {
            setNotificationCallback(batteryCharacteristic).with { _, data ->
                batteryLevel = byteToInt(data)
            }
        }

        val requestQueue = beginAtomicRequestQueue()
            .add(enableIndications(serviceChangedCharacteristic)
                .fail { _: BluetoothDevice?, status: Int -> failCallback(serviceChangedCharacteristic, status) }
            )
            .add(enableNotifications(asyncCharacteristic)
                .fail { _: BluetoothDevice?, status: Int -> failCallback(asyncCharacteristic, status) }
            )
            .add(enableIndications(syncTxCharacteristic)
                .fail { _: BluetoothDevice?, status: Int -> failCallback(syncTxCharacteristic, status) }
            )

            if (unknown6Characteristic != null) {
                requestQueue.add(enableIndications(unknown6Characteristic)
                    .fail { _: BluetoothDevice?, status: Int -> failCallback(unknown6Characteristic, status) }
                )
            }
            if (batteryCharacteristic != null) {
                requestQueue.add(enableNotifications(batteryCharacteristic)
                    .fail { _: BluetoothDevice?, status: Int -> failCallback(batteryCharacteristic, status) }
                )
            }

            requestQueue.add(readCharacteristic(deviceNameCharacteristic).with { _, data ->
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

    private fun failCallback(characteristic: BluetoothGattCharacteristic?, status: Int) {

        if (characteristic == null) return

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


