package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.service.AzanAudioService
import com.example.util.PrayerAlarmScheduler

class PrayerAlarmReceiver : BroadcastReceiver() {

  companion object {
    const val ACTION_PRAYER_ALARM = "com.example.ACTION_PRAYER_ALARM"
    const val ACTION_STOP_AZAN = "com.example.ACTION_STOP_AZAN"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_PRAYER_KEY = "extra_prayer_key"
  }

  override fun onReceive(context: Context, intent: Intent) {
    val action = intent.action ?: return

    when (action) {
      ACTION_STOP_AZAN -> {
        AzanAudioService.stopCurrentAzan()
        val stopServiceIntent = Intent(context, AzanAudioService::class.java).apply {
          this.action = AzanAudioService.ACTION_STOP_AZAN
        }
        context.stopService(stopServiceIntent)
      }

      ACTION_PRAYER_ALARM -> {
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "নামাজ"
        val prayerKey = intent.getStringExtra(EXTRA_PRAYER_KEY)?.lowercase()

        // Re-check both master and individual switches at fire time to avoid stale alarms.
        val prefs = context.getSharedPreferences("noor_daily_prefs", Context.MODE_PRIVATE)
        val isAzanEnabled = prefs.getBoolean("azan_notifications_enabled", true)
        val isPrayerEnabled = prayerKey == null || prefs.getBoolean("azan_${prayerKey}_enabled", true)
        if (!isAzanEnabled || !isPrayerEnabled) {
          // Even if sound disabled, schedule next
          PrayerAlarmScheduler.scheduleNextPrayerAlarm(context)
          return
        }

        // Start Azan Foreground Service
        val serviceIntent = Intent(context, AzanAudioService::class.java).apply {
          this.action = AzanAudioService.ACTION_START_AZAN
          putExtra(AzanAudioService.EXTRA_PRAYER_NAME, prayerName)
        }

        try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent)
          } else {
            context.startService(serviceIntent)
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }

        // Re-arm for the next prayer
        PrayerAlarmScheduler.scheduleNextPrayerAlarm(context)
      }
    }
  }
}
