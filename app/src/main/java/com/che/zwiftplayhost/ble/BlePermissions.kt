package com.che.zwiftplayhost.ble

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

object BlePermissions {

    fun hasRequiredPermissions(context: Context): Boolean {
        return requiredPermissions(context).size == 0
    }

    private fun requiredPermissions(context: Context): ArrayList<String> {
        val required = arrayListOf<String>()

        // have to deal with Location permission separate to bluetooth permissions
        if (permissionNotGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            required.add(Manifest.permission.ACCESS_FINE_LOCATION)
            return required
        }

        if (permissionNotGranted(context, Manifest.permission.BLUETOOTH_SCAN))
            required.add(Manifest.permission.BLUETOOTH_SCAN)
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