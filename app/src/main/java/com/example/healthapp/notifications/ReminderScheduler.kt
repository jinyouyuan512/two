package com.example.healthapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import java.util.Calendar

object ReminderScheduler {
    fun scheduleDaily(context: Context, hour: Int = 9, minute: Int = 0) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        
        // 取消已有的闹钟，确保不会有重复的闹钟
        am.cancel(pi)
        
        val cal = Calendar.getInstance()
        // 设置为当前日期的指定时间
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        var trigger = cal.timeInMillis
        // 如果指定时间已过，设置为明天的同一时间
        if (trigger <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            trigger = cal.timeInMillis
        }
        
        try {
            // 检查是否有权限设置精确闹钟
            if (hasExactAlarmPermission(context)) {
                // 根据 Android 版本选择合适的闹钟设置方法
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    am.setExact(AlarmManager.RTC_WAKEUP, trigger, pi)
                } else {
                    am.set(AlarmManager.RTC_WAKEUP, trigger, pi)
                }
            } else {
                // 没有精确闹钟权限时的回退：使用不精确的闹钟，仍可在后台触发
                am.set(AlarmManager.RTC_WAKEUP, trigger, pi)
            }
        } catch (e: SecurityException) {
            // 处理权限不足的情况
            e.printStackTrace()
        }
    }

    fun scheduleNextDay(context: Context) {
        val prefs = context.getSharedPreferences("prefs_profile", Context.MODE_PRIVATE)
        val t = prefs.getString("pref_notif_time", "09:00") ?: "09:00"
        val parts = t.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        scheduleDaily(context, h, m)
        scheduleWorkNext(context)
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.cancel(pi)
        cancelWork(context)
    }
    
    /**
     * 检查是否有权限设置精确闹钟
     */
    fun hasExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleWorkNext(context: Context) {
        val prefs = context.getSharedPreferences("prefs_profile", Context.MODE_PRIVATE)
        val t = prefs.getString("pref_notif_time", "09:00") ?: "09:00"
        val parts = t.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, h)
        cal.set(Calendar.MINUTE, m)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        var trigger = cal.timeInMillis
        if (trigger <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            trigger = cal.timeInMillis
        }
        val delay = trigger - System.currentTimeMillis()

        val req = OneTimeWorkRequestBuilder<DailyTipWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("daily_tip")
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("daily_tip_unique", ExistingWorkPolicy.REPLACE, req)
    }

    fun cancelWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("daily_tip_unique")
    }

    fun tryAcquireTipLock(context: Context, windowMs: Long = 120000L): Boolean {
        val prefs = context.getSharedPreferences("prefs_profile", Context.MODE_PRIVATE)
        val last = prefs.getLong("last_tip_ts", 0L)
        val now = System.currentTimeMillis()
        if (now - last < windowMs) return false
        return prefs.edit().putLong("last_tip_ts", now).commit()
    }
}
