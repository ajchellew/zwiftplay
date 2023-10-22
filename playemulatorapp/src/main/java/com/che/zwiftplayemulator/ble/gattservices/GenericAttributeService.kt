package com.che.zwiftplayemulator.ble.gattservices

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.che.zap.device.GenericBleUuids
import com.che.zap.device.GenericBleUuids.DEFAULT_DESCRIPTOR_UUID
import com.che.zap.device.GenericBleUuids.GENERIC_ATTRIBUTE_SERVICE_UUID
import com.che.zap.device.GenericBleUuids.SERVICE_CHANGED_CHARACTERISTIC_UUID

class GenericAttributeService : BluetoothGattService(GENERIC_ATTRIBUTE_SERVICE_UUID, SERVICE_TYPE_PRIMARY) {

    init {
        addCharacteristic(
            BluetoothGattCharacteristic(
                SERVICE_CHANGED_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_INDICATE,
                0
            ).let {
                it.addDescriptor(BluetoothGattDescriptor(DEFAULT_DESCRIPTOR_UUID, 0))
                it
            }
        )
    }
}