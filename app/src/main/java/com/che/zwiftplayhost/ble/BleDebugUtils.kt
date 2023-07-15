package com.che.zwiftplayhost.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.util.Log

object BleDebugUtils {

    // Whilst nRF is good, this prints all the characteristics for a copy/paste
    fun debugPrintBluetoothService(name: String, service: BluetoothGattService) {
        val tag = "DEBUG"
        Log.e(tag, "Service: $name UUID: ${service.uuid}")
        for (characteristic in service.characteristics) {
            Log.e(tag, "- Characteristic: ${characteristic.uuid} Properties: ${buildCharacteristicProperties(characteristic)}")
            for (descriptor in characteristic.descriptors) {
                var descriptorsString = "  - Descriptor: ${descriptor.uuid}"
                val permissions = descriptor.permissions
                if (permissions != 0)
                    descriptorsString += " Permissions: ${buildDescriptorPermissions(permissions)}"
                Log.e(tag, descriptorsString)
            }
        }
    }

    private fun buildCharacteristicProperties(characteristic: BluetoothGattCharacteristic): String {
        var props = ""
        val properties = characteristic.properties
        if (properties and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0)
            props += "I "
        if (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0)
            props += "N "
        if (properties and BluetoothGattCharacteristic.PROPERTY_READ > 0)
            props += "R "
        if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0)
            props += "W "
        if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0)
            props += "W-NR "
        return props
    }

    private fun buildDescriptorPermissions(permissions: Int): String {
        var perms = ""
        if (permissions and BluetoothGattDescriptor.PERMISSION_READ > 0)
            perms += "R "
        if (permissions and BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED > 0)
            perms += "RE "
        if (permissions and BluetoothGattDescriptor.PERMISSION_WRITE > 0)
            perms += "W "
        if (permissions and BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED > 0)
            perms += "WS "
        if (permissions and BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED > 0)
            perms += "WE "
        if (permissions and BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM > 0)
            perms += "WEM "
        return perms
    }
}