package com.simats.ocularsurfaceai.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.simats.ocularsurfaceai.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "medicine_reminders"
        const val CHANNEL_NAME = "Medicine Reminders"
    }

    fun showNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(android.R.drawable.ic_popup_reminder)
            setContentTitle(title)
            setContentText(message)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(true)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
