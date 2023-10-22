package com.che.zwiftplayemulator.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.che.zap.utils.Logger
import com.che.zwiftplayemulator.ble.gattservices.BatteryService
import com.che.zwiftplayemulator.ble.gattservices.DeviceInformationService
import com.che.zwiftplayemulator.ble.gattservices.GenericAccessService
import com.che.zwiftplayemulator.ble.gattservices.GenericAttributeService
import com.che.zwiftplayemulator.ble.gattservices.ZapService

@SuppressLint("MissingPermission")
class GattServerManager(context: Context, bluetoothManager: BluetoothManager) {

    private var leftGattServer: ControllerGattServer = ControllerGattServer(context, bluetoothManager)
    private var rightGattServer: ControllerGattServer = ControllerGattServer(context, bluetoothManager)

    fun start() {
        leftGattServer.start()
        rightGattServer.start()
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
        bluetoothGattServer?.addService(genericAccessService) ?: Logger.e("Failed to add GattService")
        bluetoothGattServer?.addService(genericAttributeService) ?: Logger.e("Failed to add GattService")
        bluetoothGattServer?.addService(deviceInformationService) ?: Logger.e("Failed to add GattService")
        bluetoothGattServer?.addService(zapService) ?: Logger.e("Failed to add GattService")
        bluetoothGattServer?.addService(batteryService) ?: Logger.e("Failed to add GattService")
        Logger.d("Started GattServer")
    }

    /**
     * Callback to handle incoming requests to the GATT server.
     * All read/write requests for characteristics and descriptors are handled here.
     */
    private val gattServerCallback = object : BluetoothGattServerCallback() {

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