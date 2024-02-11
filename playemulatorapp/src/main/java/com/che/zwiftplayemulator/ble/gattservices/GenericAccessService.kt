package com.che.zwiftplayemulator.ble.gattservices

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import com.che.zap.device.common.GenericBleUuids.APPEARANCE_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.CENTRAL_ADDRESS_RESOLUTION_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.DEVICE_NAME_CHARACTERISTIC_UUID
import com.che.zap.device.common.GenericBleUuids.GENERIC_ACCESS_SERVICE_UUID
import com.che.zap.device.common.GenericBleUuids.PREFERRED_CONNECTION_PARAMS_CHARACTERISTIC_UUID

class GenericAccessService : BluetoothGattService(GENERIC_ACCESS_SERVICE_UUID, SERVICE_TYPE_PRIMARY) {

    init {

        addCharacteristic(
            BluetoothGattCharacteristic(
                DEVICE_NAME_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                0
            )
        )

        addCharacteristic(
            BluetoothGattCharacteristic(
                APPEARANCE_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                0
            )
        )

        addCharacteristic(
            BluetoothGattCharacteristic(
                PREFERRED_CONNECTION_PARAMS_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                0
            )
        )

        addCharacteristic(
            BluetoothGattCharacteristic(
                CENTRAL_ADDRESS_RESOLUTION_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                0
            )
        )


    }
}