package com.che.zwiftplayemulator.ble

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object AdvertisingBlePermissions {

    fun hasRequiredPermissions(context: Context): Boolean {
        return requiredPermissions(context).size == 0
    }

    private fun requiredPermissions(context: Context): ArrayList<String> {
        val required = arrayListOf<String>()

        if (permissionNotGranted(context, Manifest.permission.BLUETOOTH_ADVERTISE))
            required.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        if (permissionNotGranted(context, Manifest.permission.BLUETOOTH_CONNECT))
            required.add(Manifest.permission.BLUETOOTH_CONNECT)
        return required
    }

    fun requestPermissions(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            requiredPermissions(activity).toTypedArray(),
            requestCode
        )
    }

    private fun permissionNotGranted(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) != PackageManager.PERMISSION_GRANTED
    }

}