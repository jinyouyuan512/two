package com.example.healthapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("prefs_profile", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("pref_notifications", true)
        if (enabled) {
            val t = prefs.getString("pref_notif_time", "09:00") ?: "09:00"
            val parts = t.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            ReminderScheduler.scheduleDaily(context, h, m)
            ReminderScheduler.scheduleWorkNext(context)
        }
    }
}
