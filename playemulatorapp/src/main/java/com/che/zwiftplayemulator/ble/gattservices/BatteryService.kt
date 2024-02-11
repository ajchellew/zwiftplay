package com.che.zwiftplayemulator.ble.gattservices

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.che.zap.device.common.GenericBleUuids.BATTERY_LEVEL_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.BATTERY_SERVICE_UUID
import com.che.zap.device.common.GenericBleUuids.DEFAULT_DESCRIPTOR_UUID

class BatteryService : BluetoothGattService(BATTERY_SERVICE_UUID, SERVICE_TYPE_PRIMARY) {

    init {
        BluetoothGattCharacteristic(
            BATTERY_LEVEL_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            0
        ).let {
            it.addDescriptor(BluetoothGattDescriptor(DEFAULT_DESCRIPTOR_UUID, 0))
            addCharacteristic(it)
        }
    }
}