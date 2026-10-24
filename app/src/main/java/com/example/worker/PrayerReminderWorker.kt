package com.example.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.receiver.PrayerAlarmReceiver
import com.example.service.AzanAudioService
import com.example.util.LocationAndNotificationHelper

/**
 * Background WorkManager worker responsible for triggering local prayer notification
 * reminders and Azan alerts based on user location calculations.
 */
class PrayerReminderWorker(
  private val context: Context,
  workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

  companion object {
    private const val TAG = "PrayerReminderWorker"
    const val KEY_ACTION = "key_action"
    const val KEY_PRAYER_NAME = "key_prayer_name"
    const val KEY_PRAYER_KEY = "key_prayer_key"
    const val KEY_TARGET_MILLIS = "key_target_millis"

    const val ACTION_TRIGGER_REMINDER = "action_trigger_reminder"
    const val ACTION_SYNC_AND_SCHEDULE = "action_sync_and_schedule"
  }

  override suspend fun doWork(): Result {
    val action = inputData.getString(KEY_ACTION) ?: ACTION_SYNC_AND_SCHEDULE
    Log.d(TAG, "PrayerReminderWorker executing with action: $action")

    return try {
      val prefs = context.getSharedPreferences("noor_daily_prefs", Context.MODE_PRIVATE)
      val masterEnabled = prefs.getBoolean("azan_notifications_enabled", true)

      if (!masterEnabled) {
        Log.d(TAG, "Azan notifications disabled by user preferences.")
        return Result.success()
      }

      when (action) {
        ACTION_TRIGGER_REMINDER -> {
          val prayerName = inputData.getString(KEY_PRAYER_NAME) ?: "নামাজ"
          val prayerKey = inputData.getString(KEY_PRAYER_KEY)?.lowercase() ?: "fajr"

          // Verify if individual prayer notification is enabled
          val isPrayerEnabled = prefs.getBoolean("azan_${prayerKey}_enabled", true)
          if (isPrayerEnabled) {
            triggerPrayerLocalNotification(context, prayerName, prayerKey)
          } else {
            Log.d(TAG, "Notification for $prayerKey is individually disabled.")
          }

          // Reschedule next cycle to keep future prayer reminders populated
          PrayerWorkScheduler.enqueuePrayerReminders(context)
          Result.success()
        }

        ACTION_SYNC_AND_SCHEDULE -> {
          // Recalculate upcoming prayer times for the user's latest calculated location
          PrayerWorkScheduler.enqueuePrayerReminders(context)
          Result.success()
        }

        else -> {
          PrayerWorkScheduler.enqueuePrayerReminders(context)
          Result.success()
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error executing PrayerReminderWorker", e)
      Result.retry()
    }
  }

  private fun triggerPrayerLocalNotification(context: Context, prayerName: String, prayerKey: String) {
    LocationAndNotificationHelper.initNotificationChannel(context)

    val currentLang = context.getSharedPreferences("noor_daily_prefs", Context.MODE_PRIVATE)
      .getString("app_language", "bn") ?: "bn"

    val title = when (currentLang) {
      "ar" -> "الله أكبر • حان الآن وقت صلاة ($prayerName)"
      "en" -> "Allahu Akbar • Prayer Time for $prayerName"
      else -> "الله أكبر • নামাজের ওয়াক্ত হয়েছে ($prayerName)"
    }

    val content = when (currentLang) {
      "ar" -> "حان الآن دخول وقت $prayerName. حي على الصلاة، حي على الفلاح."
      "en" -> "It is now time for $prayerName prayer. Hasten to prayer and success."
      else -> "$prayerName নামাজের ওয়াক্ত শুরু হয়েছে। দ্রুত জামায়াতে শরিক হোন।"
    }

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      System.currentTimeMillis().toInt(),
      openAppIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      this.action = PrayerAlarmReceiver.ACTION_STOP_AZAN
    }
    val stopPendingIntent = PendingIntent.getBroadcast(
      context,
      101,
      stopIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.azan}")
    val audioAttributes = AudioAttributes.Builder()
      .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
      .setUsage(AudioAttributes.USAGE_ALARM)
      .build()

    val notificationId = 1000 + (prayerKey.hashCode() % 1000)

    val builder = NotificationCompat.Builder(context, LocationAndNotificationHelper.AZAN_CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle(title)
      .setContentText(content)
      .setStyle(NotificationCompat.BigTextStyle().bigText(content))
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setContentIntent(contentPendingIntent)
      .setSound(soundUri)
      .setAutoCancel(true)
      .addAction(
        android.R.drawable.ic_media_pause,
        when (currentLang) {
          "ar" -> "إيقاف التنبيه"
          "en" -> "Stop Reminder"
          else -> "আযান থামান"
        },
        stopPendingIntent
      )

    try {
      if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to post prayer notification", e)
    }

    // Launch Azan Audio Service for rich audio reminder
    try {
      val serviceIntent = Intent(context, AzanAudioService::class.java).apply {
        this.action = AzanAudioService.ACTION_START_AZAN
        putExtra(AzanAudioService.EXTRA_PRAYER_NAME, prayerName)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        androidx.core.content.ContextCompat.startForegroundService(context, serviceIntent)
      } else {
        context.startService(serviceIntent)
      }
    } catch (_: Exception) {
      // In-process fallback alert
      LocationAndNotificationHelper.triggerAzanAlert(context, prayerName)
    }
  }
}
