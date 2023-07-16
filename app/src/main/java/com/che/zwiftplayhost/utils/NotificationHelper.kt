package com.che.zwiftplayhost.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.che.zwiftplayhost.R
import com.che.zwiftplayhost.ble.BlePermissions
import com.che.zwiftplayhost.ui.MainActivity

object NotificationHelper {

    const val SERVICE_CHANNEL_ID = "service"

    // need a notification channel for any notifications on modern Android N+?
    fun createChannel(context: Context) {
        val notificationService = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            context.resources.getString(R.string.service_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        notificationChannel.enableVibration(false)
        notificationService.createNotificationChannel(notificationChannel)
    }

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