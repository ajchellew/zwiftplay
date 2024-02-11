package com.che.zwiftplayemulator.ble.gattservices

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import com.che.zap.device.common.GenericBleUuids.DEVICE_INFORMATION_SERVICE_UUID
import com.che.zap.device.common.GenericBleUuids.FIRMWARE_REVISION_STRING_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.HARDWARE_REVISION_STRING_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.MANUFACTURER_NAME_STRING_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.SERIAL_NUMBER_STRING_CHARACTERISTIC_UUID

class DeviceInformationService : BluetoothGattService(DEVICE_INFORMATION_SERVICE_UUID, SERVICE_TYPE_PRIMARY) {

    init {
        addCharacteristic(
            BluetoothGattCharacteristic(
                MANUFACTURER_NAME_STRING_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                0
            )
        )

        addCharacteristic(
            BluetoothGattCharacteristic(
                SERIAL_NUMBER_STRING_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                0
            )
        )

        addCharacteristic(
            BluetoothGattCharacteristic(
                HARDWARE_REVISION_STRING_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                0
            )
        )

        addCharacteristic(
            BluetoothGattCharacteristic(
                FIRMWARE_REVISION_STRING_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                0
            )
        )
    }
}