package com.che.zwiftplayemulator.ble.gattservices

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.che.zap.device.GenericBleUuids
import com.che.zap.device.GenericBleUuids.DEFAULT_DESCRIPTOR_UUID
import com.che.zap.device.ZapBleUuids.ZWIFT_ASYNC_CHARACTERISTIC_UUID
import com.che.zap.device.ZapBleUuids.ZWIFT_CUSTOM_SERVICE_UUID
import com.che.zap.device.ZapBleUuids.ZWIFT_SYNC_RX_CHARACTERISTIC_UUID
import com.che.zap.device.ZapBleUuids.ZWIFT_SYNC_TX_CHARACTERISTIC_UUID
import com.che.zap.device.ZapBleUuids.ZWIFT_UNKNOWN_6_CHARACTERISTIC_UUID

class ZapService : BluetoothGattService(ZWIFT_CUSTOM_SERVICE_UUID, SERVICE_TYPE_PRIMARY) {

    init {

        addCharacteristic(
            BluetoothGattCharacteristic(
                ZWIFT_ASYNC_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                0
            ).let {
                it.addDescriptor(BluetoothGattDescriptor(DEFAULT_DESCRIPTOR_UUID, 0))
                it
            }
        )

        addCharacteristic(
            BluetoothGattCharacteristic(
                ZWIFT_SYNC_RX_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                0
            )
        )

        addCharacteristic(
            BluetoothGattCharacteristic(
                ZWIFT_SYNC_TX_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_INDICATE or BluetoothGattCharacteristic.PROPERTY_READ,
                0
            ).let {
                it.addDescriptor(BluetoothGattDescriptor(DEFAULT_DESCRIPTOR_UUID, 0))
                it
            }
        )

        addCharacteristic(
            BluetoothGattCharacteristic(
                ZWIFT_UNKNOWN_6_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_INDICATE or BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                0
            ).let {
                it.addDescriptor(BluetoothGattDescriptor(DEFAULT_DESCRIPTOR_UUID, 0))
                it
            }
        )
    }
}