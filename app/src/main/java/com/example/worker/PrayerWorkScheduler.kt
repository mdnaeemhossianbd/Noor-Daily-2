package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.util.LocationAndNotificationHelper
import com.example.util.PrayerTimeCalculator
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Manages WorkManager requests for prayer time reminders (Azan).
 * Schedules background work calculated accurately against the user's GPS/network or selected city coordinates.
 */
object PrayerWorkScheduler {

  private const val TAG = "PrayerWorkScheduler"
  const val TAG_PRAYER_WORK = "prayer_reminder_worker_tag"
  const val UNIQUE_PERIODIC_SYNC = "prayer_periodic_sync_work"
  private const val UNIQUE_WORK_PREFIX = "prayer_work_"

  /**
   * Enqueues WorkManager requests for all 5 obligatory daily prayers based on the user's
   * current calculated location (GPS or selected city). Also enqueues a periodic synchronization
   * worker that runs in the background to continuously keep times and locations fresh.
   */
  fun enqueuePrayerReminders(context: Context) {
    try {
      val workManager = WorkManager.getInstance(context)
      val prefs = context.getSharedPreferences("noor_daily_prefs", Context.MODE_PRIVATE)

      val masterEnabled = prefs.getBoolean("azan_notifications_enabled", true)
      if (!masterEnabled) {
        cancelAllPrayerReminders(context)
        return
      }

      // 1. Resolve calculated location coordinates
      val useGps = prefs.getBoolean("use_gps_location", true)
      val locationInfo = LocationAndNotificationHelper.getCurrentDeviceLocation(context)
      val latitude = if (!useGps) {
        prefs.getString("selected_city_latitude", null)?.toDoubleOrNull() ?: locationInfo.latitude
      } else {
        prefs.getString("gps_latitude", null)?.toDoubleOrNull() ?: locationInfo.latitude
      }
      val longitude = if (!useGps) {
        prefs.getString("selected_city_longitude", null)?.toDoubleOrNull() ?: locationInfo.longitude
      } else {
        prefs.getString("gps_longitude", null)?.toDoubleOrNull() ?: locationInfo.longitude
      }
      val timeZoneId = if (!useGps) prefs.getString("selected_city_timezone", null) else null
      val isHanafi = prefs.getBoolean("is_hanafi_madhhab", true)

      // Ensure admin adjustments and methods are synced
      PrayerTimeCalculator.updateAdminSettings(
        method = prefs.getString("prayer_method", "Umm al-Qura") ?: "Umm al-Qura",
        madhhab = prefs.getString("prayer_madhhab", "Hanafi") ?: "Hanafi",
        adjustments = mapOf(
          "fajr" to prefs.getInt("prayer_adjust_fajr", 0),
          "sunrise" to prefs.getInt("prayer_adjust_sunrise", 0),
          "dhuhr" to prefs.getInt("prayer_adjust_dhuhr", 0),
          "asr" to prefs.getInt("prayer_adjust_asr", 0),
          "maghrib" to prefs.getInt("prayer_adjust_maghrib", 0),
          "isha" to prefs.getInt("prayer_adjust_isha", 0)
        )
      )

      val nowCal = Calendar.getInstance()
      val nowMillis = System.currentTimeMillis()

      // Calculate today and tomorrow's prayer times for the calculated location
      val todayTimes = PrayerTimeCalculator.calculate(latitude, longitude, isHanafi, nowCal, timeZoneId)
      val tomorrowCal = (nowCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
      val tomorrowTimes = PrayerTimeCalculator.calculate(latitude, longitude, isHanafi, tomorrowCal, timeZoneId)

      val prayers = listOf(
        PrayerTarget("Fajr", "ফজর", "الفجر", todayTimes.fajr, tomorrowTimes.fajr),
        PrayerTarget("Dhuhr", "যোহর", "الظهر", todayTimes.dhuhr, tomorrowTimes.dhuhr),
        PrayerTarget("Asr", "আসর", "العصر", todayTimes.asr, tomorrowTimes.asr),
        PrayerTarget("Maghrib", "মাগরিব", "المغرب", todayTimes.maghrib, tomorrowTimes.maghrib),
        PrayerTarget("Isha", "এশা", "العشاء", todayTimes.isha, tomorrowTimes.isha)
      )

      for (prayer in prayers) {
        val prayerKey = prayer.nameEn.lowercase()
        val isEnabled = prefs.getBoolean("azan_${prayerKey}_enabled", true)
        val uniqueWorkName = "$UNIQUE_WORK_PREFIX$prayerKey"

        if (!isEnabled) {
          workManager.cancelUniqueWork(uniqueWorkName)
          continue
        }

        // Calculate trigger timestamp
        val todayPrayerCal = PrayerTimeCalculator.parseTimeToCalendar(prayer.timeToday, nowCal)
        val triggerCal = if (todayPrayerCal.timeInMillis > nowMillis + 5000L) {
          todayPrayerCal
        } else {
          // If already passed today, target tomorrow's time
          PrayerTimeCalculator.parseTimeToCalendar(prayer.timeTomorrow, tomorrowCal)
        }

        val triggerMillis = triggerCal.timeInMillis
        val delayMillis = max(0L, triggerMillis - nowMillis)

        val inputData = workDataOf(
          PrayerReminderWorker.KEY_ACTION to PrayerReminderWorker.ACTION_TRIGGER_REMINDER,
          PrayerReminderWorker.KEY_PRAYER_NAME to prayer.nameBn,
          PrayerReminderWorker.KEY_PRAYER_KEY to prayer.nameEn,
          PrayerReminderWorker.KEY_TARGET_MILLIS to triggerMillis
        )

        val workRequest = OneTimeWorkRequestBuilder<PrayerReminderWorker>()
          .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
          .setInputData(inputData)
          .addTag(TAG_PRAYER_WORK)
          .addTag("TAG_${prayer.nameEn.uppercase()}")
          .build()

        workManager.enqueueUniqueWork(
          uniqueWorkName,
          ExistingWorkPolicy.REPLACE,
          workRequest
        )

        Log.d(TAG, "Enqueued WorkManager reminder for ${prayer.nameEn} in ${delayMillis / 1000 / 60} minutes at ($latitude, $longitude)")
      }

      // Enqueue periodic background sync worker every 6 hours
      val periodicSyncRequest = PeriodicWorkRequestBuilder<PrayerReminderWorker>(6, TimeUnit.HOURS)
        .setInputData(workDataOf(PrayerReminderWorker.KEY_ACTION to PrayerReminderWorker.ACTION_SYNC_AND_SCHEDULE))
        .addTag(TAG_PRAYER_WORK)
        .build()

      workManager.enqueueUniquePeriodicWork(
        UNIQUE_PERIODIC_SYNC,
        ExistingPeriodicWorkPolicy.KEEP,
        periodicSyncRequest
      )

    } catch (e: Exception) {
      Log.e(TAG, "Failed to schedule WorkManager prayer reminders", e)
    }
  }

  fun cancelAllPrayerReminders(context: Context) {
    try {
      val workManager = WorkManager.getInstance(context)
      workManager.cancelAllWorkByTag(TAG_PRAYER_WORK)
      val prayers = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
      for (key in prayers) {
        workManager.cancelUniqueWork("$UNIQUE_WORK_PREFIX$key")
      }
      workManager.cancelUniqueWork(UNIQUE_PERIODIC_SYNC)
      Log.d(TAG, "Cancelled all WorkManager prayer reminders")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to cancel WorkManager reminders", e)
    }
  }

  fun triggerImmediateTestReminder(context: Context, prayerName: String = "ফজর", prayerKey: String = "Fajr") {
    val workManager = WorkManager.getInstance(context)
    val inputData = workDataOf(
      PrayerReminderWorker.KEY_ACTION to PrayerReminderWorker.ACTION_TRIGGER_REMINDER,
      PrayerReminderWorker.KEY_PRAYER_NAME to prayerName,
      PrayerReminderWorker.KEY_PRAYER_KEY to prayerKey,
      PrayerReminderWorker.KEY_TARGET_MILLIS to System.currentTimeMillis()
    )
    val testWork = OneTimeWorkRequestBuilder<PrayerReminderWorker>()
      .setInputData(inputData)
      .addTag(TAG_PRAYER_WORK)
      .build()

    workManager.enqueue(testWork)
  }

  private data class PrayerTarget(
    val nameEn: String,
    val nameBn: String,
    val nameAr: String,
    val timeToday: String,
    val timeTomorrow: String
  )
}
