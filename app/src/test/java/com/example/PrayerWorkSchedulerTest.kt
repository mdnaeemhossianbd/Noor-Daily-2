package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.worker.PrayerWorkScheduler
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PrayerWorkSchedulerTest {

  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext<Context>()
    try {
      val config = Configuration.Builder().build()
      WorkManager.initialize(context, config)
    } catch (_: Exception) {}
  }

  @Test
  fun testEnqueuePrayerReminders() {
    // Enqueue reminders based on calculated location
    PrayerWorkScheduler.enqueuePrayerReminders(context)

    val workManager = WorkManager.getInstance(context)
    assertNotNull(workManager)

    // Cancel reminders cleanly
    PrayerWorkScheduler.cancelAllPrayerReminders(context)
  }
}
