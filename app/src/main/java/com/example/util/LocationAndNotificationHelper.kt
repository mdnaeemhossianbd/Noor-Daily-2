package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Locale

import android.media.MediaPlayer
import android.net.Uri
import com.example.R
import com.example.service.AzanAudioService

object LocationAndNotificationHelper {

  const val AZAN_CHANNEL_ID = "noor_daily_azan_reminder_v3"
  private const val AZAN_NOTIFICATION_ID = 786
  private var activeMediaPlayer: MediaPlayer? = null

  data class UserLocationInfo(
    val latitude: Double,
    val longitude: Double,
    val addressName: String,
    val isAutoDetected: Boolean,
  )

  fun initNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = "আযান ও নামাজের ওয়াক্ত সতর্কতা (Azan & Prayer Reminders)"
      val descriptionText = "নামাজের সঠিক সময়ে সুমধুর আযান বাজবে ও নোটিফিকেশন পাঠাবে"
      val importance = NotificationManager.IMPORTANCE_HIGH

      val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.azan}")
      val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_ALARM)
        .build()

      val channel = NotificationChannel(AZAN_CHANNEL_ID, name, importance).apply {
        description = descriptionText
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 1000)
        setSound(soundUri, audioAttributes)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun triggerAzanAlert(
    context: Context,
    prayerName: String,
    durationSeconds: Int = 20,
    onFinish: () -> Unit = {}
  ): MediaPlayer? {
    initNotificationChannel(context)

    // Stop any previous playing azan
    stopAzan(context)

    // System Notification with Azan
    val title = "الله أكبر • নামাজের ওয়াক্ত হয়েছে ($prayerName)"
    val message = "$prayerName নামাজের সময় শুরু হয়েছে। সুমধুর আযান বাজছে..."

    val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.azan}")

    val builder = NotificationCompat.Builder(context, AZAN_CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle(title)
      .setContentText(message)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setSound(soundUri)
      .setAutoCancel(true)

    try {
      if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
        NotificationManagerCompat.from(context).notify(AZAN_NOTIFICATION_ID, builder.build())
      }
    } catch (_: Exception) {}

    // Play Authentic Makkah Azan MP3
    try {
      val player = MediaPlayer.create(context.applicationContext, R.raw.azan).apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        )
        isLooping = false
        setOnCompletionListener {
          it.release()
          if (activeMediaPlayer == it) activeMediaPlayer = null
          onFinish()
        }
        start()
      }
      activeMediaPlayer = player
    } catch (e: Exception) {
      e.printStackTrace()
    }

    // Vibrate device
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(1000)
        }
      }
    } catch (_: Exception) {}

    return activeMediaPlayer
  }

  fun stopAzan(context: Context) {
    try {
      activeMediaPlayer?.let {
        if (it.isPlaying) it.stop()
        it.release()
      }
    } catch (_: Exception) {}
    activeMediaPlayer = null

    AzanAudioService.stopCurrentAzan()
    cancelNotification(context)
  }

  fun cancelNotification(context: Context) {
    try {
      NotificationManagerCompat.from(context).cancel(AZAN_NOTIFICATION_ID)
    } catch (_: Exception) {}
  }

  /**
   * Reads real location from device GPS / Network or returns geocoded current location.
   */
  fun getCurrentDeviceLocation(context: Context): UserLocationInfo {
    val hasFine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    if (hasFine || hasCoarse) {
      val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
      if (lm != null) {
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        var bestLocation: Location? = null
        for (provider in providers) {
          try {
            val loc = lm.getLastKnownLocation(provider)
            if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
              bestLocation = loc
            }
          } catch (_: SecurityException) {}
        }

        if (bestLocation != null) {
          val addressText = geocodeLocation(context, bestLocation.latitude, bestLocation.longitude)
          return UserLocationInfo(
            latitude = bestLocation.latitude,
            longitude = bestLocation.longitude,
            addressName = addressText,
            isAutoDetected = true
          )
        }
      }
    }

    // Default location before GPS permission or detection
    return UserLocationInfo(
      latitude = 23.8103,
      longitude = 90.4125,
      addressName = "ঢাকা, বাংলাদেশ",
      isAutoDetected = false
    )
  }

  /**
   * Request a fresh device location after using the last-known location for immediate UI.
   * The callback can be invoked more than once when multiple providers return a fix.
   */
  @Suppress("DEPRECATION")
  fun requestFreshDeviceLocation(context: Context, onLocation: (UserLocationInfo) -> Unit) {
    val hasFine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!hasFine && !hasCoarse) return

    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
    val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
    for (provider in providers) {
      try {
        if (!lm.isProviderEnabled(provider)) continue
        lm.requestSingleUpdate(provider, object : android.location.LocationListener {
          override fun onLocationChanged(location: Location) {
            val addressText = geocodeLocation(context, location.latitude, location.longitude)
            context.getSharedPreferences("noor_daily_prefs", Context.MODE_PRIVATE).edit()
              .putString("gps_latitude", location.latitude.toString())
              .putString("gps_longitude", location.longitude.toString())
              .putString("gps_address_name", addressText)
              .apply()
            onLocation(
              UserLocationInfo(
                latitude = location.latitude,
                longitude = location.longitude,
                addressName = addressText,
                isAutoDetected = true
              )
            )
          }

          override fun onProviderEnabled(provider: String) = Unit
          override fun onProviderDisabled(provider: String) = Unit
          @Deprecated("Deprecated in Android")
          override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
        }, Looper.getMainLooper())
      } catch (_: SecurityException) {
        // Permission may have changed while requesting the update.
      } catch (_: IllegalArgumentException) {
        // Provider is unavailable on this device.
      }
    }
  }

  data class KnownCity(
    val nameBn: String,
    val nameEn: String,
    val divisionBn: String,
    val latitude: Double,
    val longitude: Double,
  )

  private val KNOWN_CITIES = listOf(
    // 8 Divisions & Key Metropolises
    KnownCity("ঢাকা", "Dhaka", "ঢাকা বিভাগ", 23.8103, 90.4125),
    KnownCity("চট্টগ্রাম", "Chattogram", "চট্টগ্রাম বিভাগ", 22.3569, 91.7832),
    KnownCity("সিলেট", "Sylhet", "সিলেট বিভাগ", 24.8949, 91.8687),
    KnownCity("রাজশাহী", "Rajshahi", "রাজশাহী বিভাগ", 24.3745, 88.6042),
    KnownCity("খুলনা", "Khulna", "খুলনা বিভাগ", 22.8456, 89.5403),
    KnownCity("বরিশাল", "Barishal", "বরিশাল বিভাগ", 22.7010, 90.3535),
    KnownCity("রংপুর", "Rangpur", "রংপুর বিভাগ", 25.7439, 89.2752),
    KnownCity("ময়মনসিংহ", "Mymensingh", "ময়মনসিংহ বিভাগ", 24.7471, 90.4203),
    // Major Districts & Urban Hubs
    KnownCity("গাজীপুর", "Gazipur", "ঢাকা বিভাগ", 24.0023, 90.4264),
    KnownCity("নারায়ণগঞ্জ", "Narayanganj", "ঢাকা বিভাগ", 23.6238, 90.5000),
    KnownCity("কুমিল্লা", "Cumilla", "চট্টগ্রাম বিভাগ", 23.4682, 91.1788),
    KnownCity("কক্সবাজার", "Cox's Bazar", "চট্টগ্রাম বিভাগ", 21.4272, 92.0058),
    KnownCity("বগুড়া", "Bogura", "রাজশাহী বিভাগ", 24.8465, 89.3777),
    KnownCity("টাঙ্গাইল", "Tangail", "ঢাকা বিভাগ", 24.2513, 89.9167),
    KnownCity("যশোর", "Jashore", "খুলনা বিভাগ", 23.1664, 89.2081),
    KnownCity("দিনাজপুর", "Dinajpur", "রংপুর বিভাগ", 25.6217, 88.6354),
    KnownCity("ব্রাহ্মণবাড়িয়া", "Brahmanbaria", "চট্টগ্রাম বিভাগ", 23.9571, 91.1119),
    KnownCity("নরসিংদী", "Narsingdi", "ঢাকা বিভাগ", 23.9193, 90.7202),
    KnownCity("পাবনা", "Pabna", "রাজশাহী বিভাগ", 24.0064, 89.2372),
    KnownCity("কুষ্টিয়া", "Kushtia", "খুলনা বিভাগ", 23.9013, 89.1204),
    KnownCity("জামালপুর", "Jamalpur", "ময়মনসিংহ বিভাগ", 24.9375, 89.9378),
    KnownCity("ফরিদপুর", "Faridpur", "ঢাকা বিভাগ", 23.6071, 89.8426),
    KnownCity("ফেনী", "Feni", "চট্টগ্রাম বিভাগ", 23.0186, 91.3966),
    KnownCity("নোয়াখালী", "Noakhali", "চট্টগ্রাম বিভাগ", 22.8696, 91.0994),
    KnownCity("চাঁদপুর", "Chandpur", "চট্টগ্রাম বিভাগ", 23.2333, 90.6667),
    KnownCity("সিরাজগঞ্জ", "Sirajganj", "রাজশাহী বিভাগ", 24.4534, 89.7008),
    KnownCity("নাটোর", "Natore", "রাজশাহী বিভাগ", 24.4206, 88.9324),
    KnownCity("চাঁপাইনবাবগঞ্জ", "Chapainawabganj", "রাজশাহী বিভাগ", 24.5965, 88.2775),
    KnownCity("নওগাঁ", "Naogaon", "রাজশাহী বিভাগ", 24.8103, 88.9324),
    KnownCity("হবিগঞ্জ", "Habiganj", "সিলেট বিভাগ", 24.3749, 91.4155),
    KnownCity("মৌলভীবাজার", "Moulvibazar", "সিলেট বিভাগ", 24.4829, 91.7774),
    KnownCity("সুনামগঞ্জ", "Sunamganj", "সিলেট বিভাগ", 25.0658, 91.3950),
    KnownCity("পটুয়াখালী", "Patuakhali", "বরিশাল বিভাগ", 22.3596, 90.3299),
    KnownCity("ভোলা", "Bhola", "বরিশাল বিভাগ", 22.6859, 90.6481),
    KnownCity("বাগেরহাট", "Bagerhat", "খুলনা বিভাগ", 22.6516, 89.7859),
    KnownCity("সাতক্ষীরা", "Satkhira", "খুলনা বিভাগ", 22.7185, 89.0705),
    KnownCity("ঝিনাইদহ", "Jhenaidah", "খুলনা বিভাগ", 23.5450, 89.1526),
    KnownCity("চুয়াডাঙ্গা", "Chuadanga", "খুলনা বিভাগ", 23.6402, 88.8418),
    KnownCity("মেহেরপুর", "Meherpur", "খুলনা বিভাগ", 23.7622, 88.6318),
    KnownCity("মাগুরা", "Magura", "খুলনা বিভাগ", 23.4873, 89.4199),
    KnownCity("নড়াইল", "Narail", "খুলনা বিভাগ", 23.1725, 89.5127),
    KnownCity("পিরোজপুর", "Pirojpur", "বরিশাল বিভাগ", 22.5841, 89.9720),
    KnownCity("ঝালকাঠি", "Jhalokathi", "বরিশাল বিভাগ", 22.6406, 90.1987),
    KnownCity("বরগুনা", "Barguna", "বরিশাল বিভাগ", 22.0953, 90.0768),
    KnownCity("পঞ্চগড়", "Panchagarh", "রংপুর বিভাগ", 26.3411, 88.5542),
    KnownCity("ঠাকুরগাঁও", "Thakurgaon", "রংপুর বিভাগ", 26.0337, 88.4617),
    KnownCity("নীলফামারী", "Nilphamari", "রংপুর বিভাগ", 25.9318, 88.8560),
    KnownCity("লালমনিরহাট", "Lalmonirhat", "রংপুর বিভাগ", 25.9923, 89.2847),
    KnownCity("কুড়িগ্রাম", "Kurigram", "রংপুর বিভাগ", 25.8054, 89.6362),
    KnownCity("গাইবান্ধা", "Gaibandha", "রংপুর বিভাগ", 25.3288, 89.5408),
    KnownCity("শেরপুর", "Sherpur", "ময়মনসিংহ বিভাগ", 25.0205, 90.0153),
    KnownCity("নেত্রকোণা", "Netrokona", "ময়মনসিংহ বিভাগ", 24.8709, 90.7279),
    KnownCity("কিশোরগঞ্জ", "Kishoreganj", "ঢাকা বিভাগ", 24.4449, 90.7766),
    KnownCity("মানিকগঞ্জ", "Manikganj", "ঢাকা বিভাগ", 23.8644, 90.0047),
    KnownCity("মুন্সিগঞ্জ", "Munshiganj", "ঢাকা বিভাগ", 23.5422, 90.5305),
    KnownCity("মাদারীপুর", "Madaripur", "ঢাকা বিভাগ", 23.1641, 90.1897),
    KnownCity("গোপালগঞ্জ", "Gopalganj", "ঢাকা বিভাগ", 23.0051, 89.8266),
    KnownCity("শরীয়তপুর", "Shariatpur", "ঢাকা বিভাগ", 23.2423, 90.4348),
    KnownCity("রাজবাড়ী", "Rajbari", "ঢাকা বিভাগ", 23.7574, 89.6445),
    KnownCity("বান্দরবান", "Bandarban", "চট্টগ্রাম বিভাগ", 22.1953, 92.2184),
    KnownCity("রাঙ্গামাটি", "Rangamati", "চট্টগ্রাম বিভাগ", 22.6533, 92.1753),
    KnownCity("খাগড়াছড়ি", "Khagrachari", "চট্টগ্রাম বিভাগ", 23.1193, 91.9847),
    KnownCity("লক্ষ্মীপুর", "Lakshmipur", "চট্টগ্রাম বিভাগ", 22.9425, 90.8412),
    // Holy and Global Metropolises
    KnownCity("মক্কা", "Makkah", "সৌদি আরব", 21.4225, 39.8262),
    KnownCity("মদিনা", "Madinah", "সৌদি আরব", 24.4672, 39.6111),
    KnownCity("রিয়াদ", "Riyadh", "সৌদি আরব", 24.7136, 46.6753),
    KnownCity("জেদ্দা", "Jeddah", "সৌদি আরব", 21.5433, 39.1728),
    KnownCity("দুবাই", "Dubai", "সংযুক্ত আরব আমিরাত", 25.2048, 55.2708),
    KnownCity("লন্ডন", "London", "যুক্তরাজ্য", 51.5074, -0.1278),
    KnownCity("নিউ ইয়র্ক", "New York", "যুক্তরাষ্ট্র", 40.7128, -74.0060),
    KnownCity("কুয়ালালামপুর", "Kuala Lumpur", "মালয়েশিয়া", 3.1390, 101.6869)
  )

  private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return 6371.0 * c
  }

  private fun geocodeLocation(context: Context, lat: Double, lon: Double): String {
    // 1. First attempt: Device Native Geocoder for micro Area + City (e.g. Mirpur, Dhaka)
    try {
      if (Geocoder.isPresent()) {
        val geocoder = Geocoder(context, Locale.getDefault())
        @Suppress("DEPRECATION")
        val addresses: List<Address>? = geocoder.getFromLocation(lat, lon, 1)
        if (!addresses.isNullOrEmpty()) {
          val address = addresses[0]
          val area = address.subLocality?.trim()?.takeIf { it.isNotBlank() }
            ?: address.thoroughfare?.trim()?.takeIf { it.isNotBlank() && !it.matches(Regex("^[0-9+-.\\s]+$")) }
            ?: address.subAdminArea?.trim()?.takeIf { it.isNotBlank() }
          val city = address.locality?.trim()?.takeIf { it.isNotBlank() }
            ?: address.adminArea?.trim()?.takeIf { it.isNotBlank() }
          val country = address.countryName?.trim()?.takeIf { it.isNotBlank() }

          val formatted = when {
            !area.isNullOrBlank() && !city.isNullOrBlank() && area != city -> "$area, $city"
            !city.isNullOrBlank() && !country.isNullOrBlank() -> "$city, $country"
            !city.isNullOrBlank() -> city
            !area.isNullOrBlank() -> area
            !country.isNullOrBlank() -> country
            else -> null
          }
          if (formatted != null) {
            return formatted
          }
        }
      }
    } catch (_: Exception) {}

    // 2. Second attempt: Find nearest city and division from geographic database
    var closestCity: KnownCity? = null
    var minDistance = Double.MAX_VALUE
    for (city in KNOWN_CITIES) {
      val dist = calculateDistanceKm(lat, lon, city.latitude, city.longitude)
      if (dist < minDistance) {
        minDistance = dist
        closestCity = city
      }
    }

    val isBn = Locale.getDefault().language == "bn"
    if (closestCity != null && minDistance <= 40.0) {
      val cityName = if (isBn) closestCity.nameBn else closestCity.nameEn
      val regionName = if (isBn) closestCity.divisionBn else "Bangladesh"
      return "$cityName, $regionName"
    }

    // Saudi city fallback prevents a failed geocoder from incorrectly labeling Saudi Arabia as Bangladesh.
    val saudiCities = listOf(
      Triple("الطائف", "Taif", 21.2703 to 40.4158),
      Triple("مكة المكرمة", "Makkah", 21.3891 to 39.8579),
      Triple("جدة", "Jeddah", 21.4858 to 39.1925),
      Triple("الرياض", "Riyadh", 24.7136 to 46.6753),
      Triple("المدينة المنورة", "Madinah", 24.5247 to 39.5692),
      Triple("الدمام", "Dammam", 26.4207 to 50.0888)
    )
    val nearestSaudi = saudiCities.minByOrNull { (_, _, coords) ->
      calculateDistanceKm(lat, lon, coords.first, coords.second)
    }
    if (nearestSaudi != null) {
      val distance = calculateDistanceKm(lat, lon, nearestSaudi.third.first, nearestSaudi.third.second)
      if (distance <= 100.0) {
        val cityName = if (isBn) when (nearestSaudi.second) {
          "Taif" -> "তায়েফ"
          "Makkah" -> "মক্কা"
          "Jeddah" -> "জেদ্দা"
          "Riyadh" -> "রিয়াদ"
          "Madinah" -> "মদিনা"
          else -> "দাম্মাম"
        } else nearestSaudi.second
        val countryName = if (isBn) "সৌদি আরব" else "Saudi Arabia"
        return "$cityName, $countryName"
      }
    }

    // Never claim that a location outside Bangladesh is in Bangladesh when reverse geocoding fails.
    return if (isBn) "GPS অবস্থান" else "GPS location"
  }
}
