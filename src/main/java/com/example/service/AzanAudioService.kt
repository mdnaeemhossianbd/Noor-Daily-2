package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.PrayerAlarmReceiver

class AzanAudioService : Service() {

  companion object {
    const val CHANNEL_ID = "noor_daily_azan_channel_v3"
    const val NOTIFICATION_ID = 78601
    const val ACTION_START_AZAN = "com.example.ACTION_START_AZAN"
    const val ACTION_STOP_AZAN = "com.example.ACTION_STOP_AZAN"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"

    @Volatile
    var isPlaying: Boolean = false
      private set

    private var activePlayer: MediaPlayer? = null

    fun stopCurrentAzan() {
      try {
        activePlayer?.let {
          if (it.isPlaying) it.stop()
          it.release()
        }
      } catch (_: Exception) {}
      activePlayer = null
      isPlaying = false
    }
  }

  private var mediaPlayer: MediaPlayer? = null
  private var wakeLock: PowerManager.WakeLock? = null

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val action = intent?.action ?: ACTION_START_AZAN
    if (action == ACTION_STOP_AZAN) {
      stopAzanAndSelf()
      return START_NOT_STICKY
    }

    val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "নামাজ"
    startAzan(prayerName)
    return START_NOT_STICKY
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = "আযান ও নামাজের ওয়াক্ত (Azan Reminders)"
      val descriptionText = "নামাজের ওয়াক্তে সুমধুর আযান বাজবে"
      val importance = NotificationManager.IMPORTANCE_HIGH

      val soundUri = Uri.parse("android.resource://$packageName/${R.raw.azan}")
      val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_ALARM)
        .build()

      val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 800, 400, 800, 400, 1200)
        setSound(soundUri, audioAttributes)
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
      }

      val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      nm.createNotificationChannel(channel)
    }
  }

  private fun buildNotification(prayerName: String): Notification {
    val openAppIntent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val openPendingIntent = PendingIntent.getActivity(
      this, 0, openAppIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = Intent(this, PrayerAlarmReceiver::class.java).apply {
      action = PrayerAlarmReceiver.ACTION_STOP_AZAN
    }
    val stopPendingIntent = PendingIntent.getBroadcast(
      this, 1, stopIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle("الله أكبر • নামাজের ওয়াক্ত হয়েছে ($prayerName)")
      .setContentText("$prayerName নামাজের সময় হয়েছে। সুমধুর আযান বাজছে...")
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setContentIntent(openPendingIntent)
      .addAction(android.R.drawable.ic_media_pause, "আযান থামান (Stop Azan)", stopPendingIntent)
      .setOngoing(true)
      .setAutoCancel(false)
      .build()
  }

  private fun startAzan(prayerName: String) {
    stopCurrentAzan()

    val notification = buildNotification(prayerName)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
    } else {
      startForeground(NOTIFICATION_ID, notification)
    }

    // Acquire WakeLock
    try {
      val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
      wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NoorDaily:AzanWakeLock").apply {
        acquire(3 * 60 * 1000L) // max 3 mins safety
      }
    } catch (_: Exception) {}

    // Vibrate
    vibrateDevice()

    // Play Azan MP3
    try {
      mediaPlayer = MediaPlayer.create(this, R.raw.azan).apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        )
        isLooping = false
        setOnCompletionListener {
          stopAzanAndSelf()
        }
        setOnErrorListener { _, _, _ ->
          stopAzanAndSelf()
          true
        }
        start()
      }
      activePlayer = mediaPlayer
      isPlaying = true
    } catch (e: Exception) {
      e.printStackTrace()
      stopAzanAndSelf()
    }
  }

  private fun vibrateDevice() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 800, 400, 800), -1))
      } else {
        @Suppress("DEPRECATION")
        val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          v?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 800, 400, 800), -1))
        } else {
          @Suppress("DEPRECATION")
          v?.vibrate(longArrayOf(0, 800, 400, 800), -1)
        }
      }
    } catch (_: Exception) {}
  }

  private fun stopAzanAndSelf() {
    try {
      mediaPlayer?.let {
        if (it.isPlaying) it.stop()
        it.release()
      }
    } catch (_: Exception) {}
    mediaPlayer = null
    activePlayer = null
    isPlaying = false

    try {
      if (wakeLock?.isHeld == true) wakeLock?.release()
    } catch (_: Exception) {}
    wakeLock = null

    stopForeground(STOP_FOREGROUND_REMOVE)
    stopSelf()
  }

  override fun onDestroy() {
    stopAzanAndSelf()
    super.onDestroy()
  }
}
