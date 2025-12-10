package com.example.healthapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.runBlocking
import com.example.healthapp.data.repository.DailyTipsRepository

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.ensureChannel(context)
        if (!ReminderScheduler.tryAcquireTipLock(context)) return
        val tipText: String = runBlocking {
            runCatching { DailyTipsRepository().getTodayTip() }
                .getOrNull()
                ?: "记得今天记录运动、饮水与心情"
        }

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("每日小贴士")
            .setContentText(tipText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(tipText))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(NotificationHelper.NOTIF_ID, builder.build())
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        
        // 检查精确闹钟权限并重新安排明天的提醒
        try {
            if (ReminderScheduler.hasExactAlarmPermission(context)) {
                ReminderScheduler.scheduleNextDay(context)
            }
            ReminderScheduler.scheduleWorkNext(context)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
