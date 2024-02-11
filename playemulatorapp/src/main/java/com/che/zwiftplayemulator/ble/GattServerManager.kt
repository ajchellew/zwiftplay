package com.che.zwiftplayemulator.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.che.zap.device.common.GenericBleUuids.BATTERY_LEVEL_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.BATTERY_SERVICE_UUID
import com.che.zap.device.common.GenericBleUuids.DEFAULT_DESCRIPTOR_UUID
import com.che.zap.device.common.GenericBleUuids.DEVICE_NAME_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.GENERIC_ACCESS_SERVICE_UUID
import com.che.zap.utils.Logger
import com.che.zwiftplayemulator.ble.gattservices.BatteryService
import com.che.zwiftplayemulator.ble.gattservices.DeviceInformationService
import com.che.zwiftplayemulator.ble.gattservices.GenericAccessService
import com.che.zwiftplayemulator.ble.gattservices.GenericAttributeService
import com.che.zwiftplayemulator.ble.gattservices.ZapService
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.BleServerManager
import no.nordicsemi.android.ble.observer.ServerObserver
import java.nio.charset.StandardCharsets

private class ControllerServerManager(val context: Context) : BleServerManager(context), ServerObserver {

    private val batteryCharacteristic = sharedCharacteristic(
        BATTERY_LEVEL_CHARACTERISTIC_UUID,
        BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
        BluetoothGattCharacteristic.PERMISSION_READ,
        descriptor(
            DEFAULT_DESCRIPTOR_UUID,
            0,
            byteArrayOf(0)
        )
        //description("A characteristic to be read", false) // descriptors
    )

    private val deviceNameCharacteristic = sharedCharacteristic(
        DEVICE_NAME_CHARACTERISTIC_UUID,
        BluetoothGattCharacteristic.PROPERTY_READ,
        BluetoothGattCharacteristic.PERMISSION_READ
    )

    private val genericAccessServiceGattService = service(GENERIC_ACCESS_SERVICE_UUID, deviceNameCharacteristic)
    private val batteryGattService = service(BATTERY_SERVICE_UUID, batteryCharacteristic)

    private val myGattServices = arrayListOf(genericAccessServiceGattService, batteryGattService)

    private val serverConnections = mutableMapOf<String, ServerConnection>()


    fun setBatteryCharacteristicValue(value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        serverConnections.values.forEach { serverConnection ->
            serverConnection.sendNotificationForBatteryLevelCharacteristic(bytes)
        }
    }

    override fun initializeServer(): List<BluetoothGattService> {
        setServerObserver(this)
        return myGattServices
    }

    override fun onServerReady() {
        Logger.d( "Gatt server ready")
    }

    override fun onDeviceConnectedToServer(device: BluetoothDevice) {
        Logger.d( "Device connected ${device.address}")

        // A new device connected to the phone. Connect back to it, so it could be used
        // both as server and client. Even if client mode will not be used, currently this is
        // required for the server-only use.
        serverConnections[device.address] = ServerConnection().apply {
            useServer(this@ControllerServerManager)
            //attachClientConnection(device)
            connect(device).enqueue()
        }
    }

    override fun onDeviceDisconnectedFromServer(device: BluetoothDevice) {
        Logger.d("Device disconnected ${device.address}")

        // The device has disconnected. Forget it and close.
        serverConnections.remove(device.address)?.close()
    }

    /*
     * Manages the state of an individual server connection (there can be many of these)
     */
    inner class ServerConnection : BleManager(context) {

        fun sendNotificationForBatteryLevelCharacteristic(value: ByteArray) {
            sendNotification(batteryCharacteristic, value).enqueue()
        }

        override fun log(priority: Int, message: String) {
            Logger.d(message)
        }

        // There are no services that we need from the connecting device, but
        // if there were, we could specify them here.
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            return true
        }

        override fun onServicesInvalidated() {
            // This is the place to nullify characteristics obtained above.
        }
    }
}

@SuppressLint("MissingPermission")
class GattServerManager(context: Context, bluetoothManager: BluetoothManager) {

    private var leftGattServer: ControllerServerManager = ControllerServerManager(context)
    private var rightGattServer: ControllerServerManager = ControllerServerManager(context)

    fun start() {
        //leftGattServer.start()
        //rightGattServer.start()
        leftGattServer.open()
        rightGattServer.open()
    }
}

@SuppressLint("MissingPermission")
class ControllerGattServer(private val context: Context, private val bluetoothManager: BluetoothManager) {

    private var bluetoothGattServer: BluetoothGattServer? = null

    private val registeredDevices = mutableSetOf<BluetoothDevice>()

    fun start() {

        val genericAccessService = GenericAccessService()
        val genericAttributeService = GenericAttributeService()
        val deviceInformationService = DeviceInformationService()
        val zapService = ZapService()
        val batteryService = BatteryService()

        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        bluetoothGattServer?.let {

            it.addService(genericAccessService)
            it.addService(genericAttributeService)
            it.addService(deviceInformationService)
            it.addService(zapService)
            it.addService(batteryService)
        }
        Logger.d("Started GattServer")
    }

    /**
     * Callback to handle incoming requests to the GATT server.
     * All read/write requests for characteristics and descriptors are handled here.
     */
    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)
            Logger.i("onServiceAdded $status ${service?.uuid ?: ""}")
        }

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Logger.i("BluetoothDevice CONNECTED: $device")
                registeredDevices.add(device)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Logger.i("BluetoothDevice DISCONNECTED: $device")
                registeredDevices.remove(device)
            }
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int,
                                                 characteristic: BluetoothGattCharacteristic
        ) {
            val now = System.currentTimeMillis()
            when {
                /*TimeProfile.CURRENT_TIME == characteristic.uuid -> {
                    Log.i(TAG, "Read CurrentTime")
                    bluetoothGattServer?.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        TimeProfile.getExactTime(now, TimeProfile.ADJUST_NONE))
                }
                TimeProfile.LOCAL_TIME_INFO == characteristic.uuid -> {
                    Log.i(TAG, "Read LocalTimeInfo")
                    bluetoothGattServer?.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        TimeProfile.getLocalTimeInfo(now))
                }*/
                else -> {
                    // Invalid characteristic
                    Logger.e("Invalid Characteristic Read: " + characteristic.uuid)
                    bluetoothGattServer?.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null)
                }
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            Logger.e("Unknown onCharacteristicWriteRequest")
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice, requestId: Int, offset: Int,
                                             descriptor: BluetoothGattDescriptor
        ) {
            /*if (TimeProfile.CLIENT_CONFIG == descriptor.uuid) {
                Logger.d("Config descriptor read")
                val returnValue = if (registeredDevices.contains(device)) {
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                } else {
                    BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                }
                bluetoothGattServer?.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    returnValue)
            } else {*/
            Logger.e("Unknown descriptor read request")
            bluetoothGattServer?.sendResponse(device,
                requestId,
                BluetoothGatt.GATT_FAILURE,
                0, null)
            //}
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice, requestId: Int,
                                              descriptor: BluetoothGattDescriptor,
                                              preparedWrite: Boolean, responseNeeded: Boolean,
                                              offset: Int, value: ByteArray) {
            /*if (TimeProfile.CLIENT_CONFIG == descriptor.uuid) {
                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                    Logger.d("Subscribe device to notifications: $device")
                    registeredDevices.add(device)
                } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
                    Logger.d("Unsubscribe device from notifications: $device")
                    registeredDevices.remove(device)
                }

                if (responseNeeded) {
                    bluetoothGattServer?.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0, null)
                }
            } else {*/
            Logger.e("Unknown descriptor write request")
            if (responseNeeded) {
                bluetoothGattServer?.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_FAILURE,
                    0, null)
            }
            //}
        }


    }

}