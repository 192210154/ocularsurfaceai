package com.simats.ocularsurfaceai.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(
            "Time for your treatment!",
            "Don't forget to take your $medicineName."
        )
    }
}
