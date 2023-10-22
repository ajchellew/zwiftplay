package com.che.zwiftplayemulator.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.che.zwiftplayemulator.R

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
}