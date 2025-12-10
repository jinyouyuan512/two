package com.example.healthapp.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import com.example.healthapp.data.repository.DailyTipsRepository

class DailyTipWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        NotificationHelper.ensureChannel(applicationContext)
        if (!ReminderScheduler.tryAcquireTipLock(applicationContext)) return Result.success()

        val tipText: String = runBlocking {
            runCatching { DailyTipsRepository().getTodayTip() }
                .getOrNull()
                ?: "记得今天记录运动、饮水与心情"
        }

        val builder = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("每日小贴士")
            .setContentText(tipText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(tipText))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        try {
            val notificationManager = NotificationManagerCompat.from(applicationContext)
            if (androidx.core.content.ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(NotificationHelper.NOTIF_ID, builder.build())
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        // 安排下一次任务（明天同一时间）
        ReminderScheduler.scheduleWorkNext(applicationContext)
        return Result.success()
    }
}
