package com.che.common

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat

object NotificationUtils {

    fun notificationsEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationService = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationService.areNotificationsEnabled()
        }
        return true
    }


    fun requestPermission(activity: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                requestCode
            )
        }
    }

}