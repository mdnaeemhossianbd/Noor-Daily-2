package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.Ringtone
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.CityLocation
import com.example.ui.components.CitySelectionDialog
import com.example.ui.theme.*
import com.example.util.IslamicDateHelper
import com.example.util.LocationAndNotificationHelper
import com.example.util.PrayerTimeCalculator
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
  currentLang: String,
  selectedCity: CityLocation,
  isHanafi: Boolean,
  onCitySelected: (CityLocation) -> Unit,
  onMadhhabToggled: (Boolean) -> Unit,
) {
  val context = LocalContext.current
  val locationPrefs = remember { context.getSharedPreferences("noor_daily_prefs", android.content.Context.MODE_PRIVATE) }

  // User location & city selection state, persisted across tab/app restarts.
  var isUsingGps by remember { mutableStateOf(locationPrefs.getBoolean("use_gps_location", true)) }
  var currentSelectedCity by remember { mutableStateOf(selectedCity) }
  var showCityDialog by remember { mutableStateOf(false) }

  val (gregFormatted, hijriFormatted) = remember(currentLang) {
    IslamicDateHelper.getFormattedDates(currentLang)
  }
  val englishIntlDate = remember(currentLang) {
    IslamicDateHelper.getEnglishIntlDate()
  }
  val banglaSolarDate = remember {
    IslamicDateHelper.getBanglaSolarDate()
  }

  var userLocationInfo by remember {
    mutableStateOf(LocationAndNotificationHelper.getCurrentDeviceLocation(context))
  }

  // Refresh coordinates when this screen opens; keep cached coordinates as an immediate fallback.
  LaunchedEffect(Unit) {
    LocationAndNotificationHelper.requestFreshDeviceLocation(context) { freshLocation ->
      userLocationInfo = freshLocation
      com.example.util.PrayerAlarmScheduler.scheduleNextPrayerAlarm(context)
    }
  }

  // Location Permission Launcher
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) {
      userLocationInfo = LocationAndNotificationHelper.getCurrentDeviceLocation(context)
      LocationAndNotificationHelper.requestFreshDeviceLocation(context) { freshLocation ->
        userLocationInfo = freshLocation
        com.example.util.PrayerAlarmScheduler.scheduleNextPrayerAlarm(context)
      }
    }
  }

  // Notification Permission Launcher
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) {}

  // Azan Alert State
  var isAzanPlaying by remember { mutableStateOf(false) }
  var azanSecondsLeft by remember { mutableIntStateOf(20) }
  var currentMediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

  LaunchedEffect(isAzanPlaying) {
    if (isAzanPlaying) {
      currentMediaPlayer = LocationAndNotificationHelper.triggerAzanAlert(
        context = context,
        prayerName = when (currentLang) { "bn" -> "নামাজ"; "ar" -> "الصلاة"; else -> "Prayer" },
        durationSeconds = 20
      )
      azanSecondsLeft = 20
      while (azanSecondsLeft > 0) {
        delay(1000)
        azanSecondsLeft--
      }
      LocationAndNotificationHelper.stopAzan(context)
      currentMediaPlayer = null
      isAzanPlaying = false
    } else {
      LocationAndNotificationHelper.stopAzan(context)
      currentMediaPlayer = null
    }
  }

  // Active coordinates & timezone based on GPS vs manual city selection
  val activeLat = if (isUsingGps) userLocationInfo.latitude else currentSelectedCity.latitude
  val activeLng = if (isUsingGps) userLocationInfo.longitude else currentSelectedCity.longitude
  val activeTz = if (isUsingGps) null else currentSelectedCity.timeZoneId

  // Live Astronomical Calculation
  var prayerCalculation by remember(activeLat, activeLng, isHanafi, activeTz) {
    mutableStateOf(
      PrayerTimeCalculator.calculate(
        latitude = activeLat,
        longitude = activeLng,
        isHanafi = isHanafi,
        timeZoneId = activeTz
      )
    )
  }

  // Refresh the displayed times when Admin Panel settings change in the existing RTDB sync.
  var settingsRevision by remember { mutableIntStateOf(PrayerTimeCalculator.settingsRevision) }
  LaunchedEffect(Unit) {
    while (true) {
      delay(5000)
      val latestRevision = PrayerTimeCalculator.settingsRevision
      if (latestRevision != settingsRevision) {
        settingsRevision = latestRevision
        prayerCalculation = PrayerTimeCalculator.calculate(
          latitude = activeLat,
          longitude = activeLng,
          isHanafi = isHanafi,
          timeZoneId = activeTz
        )
      }
    }
  }

  // Countdown seconds ticker
  var secondsRemaining by remember(prayerCalculation) {
    mutableLongStateOf(prayerCalculation.remainingSecondsToNext)
  }
  var sehriSecondsRemaining by remember(prayerCalculation) {
    mutableLongStateOf(prayerCalculation.remainingSecondsToSehri)
  }
  var iftarSecondsRemaining by remember(prayerCalculation) {
    mutableLongStateOf(prayerCalculation.remainingSecondsToIftar)
  }

  LaunchedEffect(prayerCalculation) {
    secondsRemaining = prayerCalculation.remainingSecondsToNext
    sehriSecondsRemaining = prayerCalculation.remainingSecondsToSehri
    iftarSecondsRemaining = prayerCalculation.remainingSecondsToIftar
    while (true) {
      delay(1000)
      if (secondsRemaining > 0) {
        secondsRemaining--
      } else {
        isAzanPlaying = true
        prayerCalculation = PrayerTimeCalculator.calculate(
          latitude = activeLat,
          longitude = activeLng,
          isHanafi = isHanafi,
          timeZoneId = activeTz
        )
      }
      if (sehriSecondsRemaining > 0) sehriSecondsRemaining--
      if (iftarSecondsRemaining > 0) iftarSecondsRemaining--
    }
  }

  val hours = secondsRemaining / 3600
  val minutes = (secondsRemaining % 3600) / 60
  val seconds = secondsRemaining % 60
  val countdownFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

  val sehriH = sehriSecondsRemaining / 3600
  val sehriM = (sehriSecondsRemaining % 3600) / 60
  val sehriRemainingFormatted = if (currentLang == "bn") "${sehriH} ঘণ্টা ${sehriM} মি." else "${sehriH}h ${sehriM}m"

  val iftarH = iftarSecondsRemaining / 3600
  val iftarM = (iftarSecondsRemaining % 3600) / 60
  val iftarRemainingFormatted = if (currentLang == "bn") "${iftarH} ঘণ্টা ${iftarM} মি." else "${iftarH}h ${iftarM}m"

  val prayersList = listOf(
    Triple("Fajr", when (currentLang) { "bn" -> "ফজর"; "ar" -> "الفجر"; else -> "Fajr" }, prayerCalculation.fajr),
    Triple("Sunrise", when (currentLang) { "bn" -> "সূর্যোদয়"; "ar" -> "الشروق"; else -> "Sunrise" }, prayerCalculation.sunrise),
    Triple("Dhuhr", when (currentLang) { "bn" -> "যোহর"; "ar" -> "الظهر"; else -> "Dhuhr" }, prayerCalculation.dhuhr),
    Triple("Asr", when (currentLang) { "bn" -> "আসর"; "ar" -> "العصر"; else -> "Asr" }, prayerCalculation.asr),
    Triple("Maghrib", when (currentLang) { "bn" -> "মাগরিব"; "ar" -> "المغرب"; else -> "Maghrib" }, prayerCalculation.maghrib),
    Triple("Isha", when (currentLang) { "bn" -> "এশা"; "ar" -> "العشاء"; else -> "Isha" }, prayerCalculation.isha),
  )

  val activePrayer = prayerCalculation.activePrayerNameEn

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
  ) {

    // Header & Azan Action
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = when (currentLang) {
              "bn" -> "নামাজের সময়সূচি"
              "ar" -> "مواقيت الصلاة"
              else -> "Prayer Times"
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = when (currentLang) {
              "bn" -> "সঠিক সময় ও আযানের অনুস্মারক"
              "ar" -> "مواقيت دقيقة ومنبه الأذان"
              else -> "Accurate Astronomical Schedule"
            },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        FilledTonalButton(
          onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            isAzanPlaying = true
          },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            when (currentLang) {
              "ar" -> "اختبار الأذان"
              "en" -> "Test Azan"
              else -> "আযান টেস্ট"
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // 20-Second Azan Alert Banner
    item {
      AnimatedVisibility(
        visible = isAzanPlaying,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = IslamicEmeraldPrimary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .background(IslamicGoldAccent, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = when (currentLang) {
                  "ar" -> "دخل وقت الصلاة • الأذان يعمل"
                  "en" -> "Prayer time has arrived • Azan playing"
                  else -> "নামাজের ওয়াক্ত হয়েছে • আযান বাজছে"
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              Text(
                text = when (currentLang) {
                  "ar" -> "متبقي $azanSecondsLeft ثانية (الصوت والاهتزاز يعملان)"
                  "en" -> "$azanSecondsLeft seconds remaining (sound and vibration active)"
                  else -> "আর $azanSecondsLeft সেকেন্ড বাকি (সাউন্ড ও ভাইব্রেশন চলছে)"
                },
                color = IslamicGoldLight,
                fontSize = 12.sp
              )
            }
            IconButton(onClick = { isAzanPlaying = false }) {
              Icon(Icons.Default.Close, contentDescription = "Stop", tint = Color.White)
            }
          }
        }
      }
    }

    // Location Info & Multi-Calendar Card (English & Hijri formatted)
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(14.dp)
        ) {
          // Row 1: Location & Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  if (isUsingGps) Icons.Default.LocationOn else Icons.Default.Public,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = if (isUsingGps) userLocationInfo.addressName else (if (currentLang == "bn") currentSelectedCity.nameBn else currentSelectedCity.nameEn),
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = if (isUsingGps) {
                    when (currentLang) {
                      "bn" -> "ডিভাইসের অবস্থান অনুযায়ী সময় সমন্বিত"
                      else -> "Synchronized with device location"
                    }
                  } else {
                    when (currentLang) {
                      "bn" -> "নির্বাচিত শহর: ${if (currentLang == "bn") currentSelectedCity.nameBn else currentSelectedCity.nameEn}"
                      else -> "Selected City: ${currentSelectedCity.nameEn}"
                    }
                  },
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
              Surface(
                onClick = { showCityDialog = true },
                shape = RoundedCornerShape(10.dp),
                color = if (!isUsingGps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.Public, contentDescription = "City", modifier = Modifier.size(13.dp), tint = if (!isUsingGps) Color.White else MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = when (currentLang) { "bn" -> "দেশ/শহর"; else -> "City" },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isUsingGps) Color.White else MaterialTheme.colorScheme.primary
                  )
                }
              }

              Surface(
                onClick = {
                  isUsingGps = true
                  locationPrefs.edit().putBoolean("use_gps_location", true).apply()
                  val hasPermission =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                  if (!hasPermission) {
                    locationPermissionLauncher.launch(
                      arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                  } else {
                    userLocationInfo = LocationAndNotificationHelper.getCurrentDeviceLocation(context)
                    LocationAndNotificationHelper.requestFreshDeviceLocation(context) { freshLocation ->
                      userLocationInfo = freshLocation
                      com.example.util.PrayerAlarmScheduler.scheduleNextPrayerAlarm(context)
                    }
                  }
                },
                shape = RoundedCornerShape(10.dp),
                color = if (isUsingGps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, IslamicEmeraldLight.copy(alpha = 0.3f))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "GPS",
                    tint = if (isUsingGps) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "GPS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUsingGps) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }

          HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            modifier = Modifier.padding(vertical = 10.dp)
          )

          // 1. হিজরির উপরে: ইংরেজি ক্যালেন্ডার (English Calendar Bar)
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.Today,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = when (currentLang) {
                    "bn" -> "ইংরেজি ক্যালেন্ডার:"
                    "ar" -> "التقويم الميلادي:"
                    else -> "English Calendar:"
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
              Text(
                text = gregFormatted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // 2. মাঝে: হিজরি ক্যালেন্ডার (Hijri Calendar Bar)
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.CalendarMonth,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = when (currentLang) {
                    "bn" -> "হিজরি ক্যালেন্ডার:"
                    "ar" -> "التقويم الهجري:"
                    else -> "Hijri Calendar:"
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
              Text(
                text = hijriFormatted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // 3. হিজরির নিচে: আন্তর্জাতিক ইংরেজি ক্যালেন্ডার (International English Calendar Bar)
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.Event,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.tertiary,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = when (currentLang) {
                    "bn" -> "আন্তর্জাতিক ইংরেজি:"
                    "ar" -> "التاريخ الدولي:"
                    else -> "International (Intl):"
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.tertiary
                )
              }
              Text(
                text = englishIntlDate,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          if (currentLang == "bn") {
            Spacer(modifier = Modifier.height(6.dp))
            // ৪. বাংলা ক্যালেন্ডার (বঙ্গাব্দ)
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
              border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "বাংলা ক্যালেন্ডার (বঙ্গাব্দ):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                  )
                }
                Text(
                  text = banglaSolarDate,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }
    }

    // Sehri & Iftar Card
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Sehri
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Bedtime, contentDescription = null, tint = IslamicEmeraldPrimary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = when (currentLang) { "bn" -> "সেহরির শেষ সময়"; else -> "Sehri Ends" },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicEmeraldPrimary
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = prayerCalculation.sehriEndTime,
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) {
              Text(
                text = "বাকি: $sehriRemainingFormatted",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          VerticalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.height(52.dp)
          )

          // Iftar
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Restaurant, contentDescription = null, tint = IslamicGoldAccent, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = when (currentLang) { "bn" -> "ইফতারের সময়"; else -> "Iftar Time" },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicGoldAccent
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = prayerCalculation.iftarTime,
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = IslamicGoldAccent.copy(alpha = 0.2f)
            ) {
              Text(
                text = "বাকি: $iftarRemainingFormatted",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      }
    }

    // Hero: Next Prayer Countdown Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.linearGradient(
                colors = listOf(
                  Color(0xFF074328),
                  Color(0xFF032213)
                )
              )
            )
            .padding(22.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = when (currentLang) {
                "bn" -> "পরবর্তী নামাজ: ${prayerCalculation.nextPrayerNameBn}"
                "ar" -> "الصلاة القادمة: ${prayerCalculation.nextPrayerNameAr}"
                else -> "Next Prayer: ${prayerCalculation.nextPrayerNameEn}"
              },
              color = IslamicGoldLight,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = countdownFormatted,
              color = Color.White,
              fontSize = 38.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = when (currentLang) {
                "bn" -> "বর্তমান ওয়াক্ত: ${prayerCalculation.activePrayerNameBn}"
                "ar" -> "الوقت الحالي: ${prayerCalculation.activePrayerNameAr}"
                else -> "Current Active: ${prayerCalculation.activePrayerNameEn}"
              },
              color = Color(0xFFC7E2D3),
              fontSize = 12.sp
            )
          }
        }
      }
    }

    // Madhhab Indicator (Hanafi Calculation)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = when (currentLang) {
            "bn" -> "মাযহাব (আসর নামাজের ওয়াক্ত)"
            "ar" -> "المذهب الفقهي (صلاة العصر)"
            else -> "Calculation Method (Asr)"
          },
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
          Text(
            text = when (currentLang) {
              "bn" -> "হানাফী"
              "ar" -> "حنفي"
              else -> "Hanafi"
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
          )
        }
      }
    }

    // 6 Daily Prayer Rows
    items(prayersList.size) { index ->
      val (code, name, time) = prayersList[index]
      val isActive = code.equals(activePrayer, ignoreCase = true)

      Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        border = if (isActive) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(
                  if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                when (code) {
                  "Fajr" -> Icons.Default.WbTwilight
                  "Sunrise" -> Icons.Default.WbSunny
                  "Dhuhr" -> Icons.Default.WbSunny
                  "Asr" -> Icons.Default.Brightness5
                  "Maghrib" -> Icons.Default.NightsStay
                  else -> Icons.Default.NightlightRound
                },
                contentDescription = null,
                tint = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = name,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 15.sp,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
              )
              if (isActive) {
                Text(
                  text = when (currentLang) {
                    "bn" -> "চলমান ওয়াক্ত"
                    "ar" -> "الوقت الحالي"
                    else -> "Active Now"
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }

          Text(
            text = time,
            fontSize = 16.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }

  if (showCityDialog) {
    CitySelectionDialog(
      currentLang = currentLang,
      selectedCity = currentSelectedCity,
      isUsingGps = isUsingGps,
      onSelectGps = {
        isUsingGps = true
        locationPrefs.edit().putBoolean("use_gps_location", true).apply()
        userLocationInfo = LocationAndNotificationHelper.getCurrentDeviceLocation(context)
        LocationAndNotificationHelper.requestFreshDeviceLocation(context) { freshLocation ->
          userLocationInfo = freshLocation
          com.example.util.PrayerAlarmScheduler.scheduleNextPrayerAlarm(context)
        }
      },
      onSelectCity = { city ->
        isUsingGps = false
        currentSelectedCity = city
        locationPrefs.edit()
          .putBoolean("use_gps_location", false)
          .putString("selected_city_name_en", city.nameEn)
          .putString("selected_city_latitude", city.latitude.toString())
          .putString("selected_city_longitude", city.longitude.toString())
          .putString("selected_city_timezone", city.timeZoneId)
          .apply()
        onCitySelected(city)
        com.example.util.PrayerAlarmScheduler.scheduleNextPrayerAlarm(context)
      },
      onDismiss = { showCityDialog = false }
    )
  }
}
