package com.example.ui.screens

import android.Manifest
import android.content.Context
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.SampleIslamicData
import com.example.model.CityLocation
import com.example.model.IslamicPost
import com.example.ui.components.CitySelectionDialog
import com.example.ui.components.IslamicCalendarDialog
import com.example.ui.components.IslamicQuizDialog
import com.example.ui.components.NamesOfAllahDialog
import com.example.ui.components.TasbihDialog
import com.example.ui.theme.*
import com.example.util.IslamicDateHelper
import com.example.util.LocationAndNotificationHelper
import com.example.util.PrayerTimeCalculator
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  currentLang: String,
  selectedCity: CityLocation,
  isHanafi: Boolean,
  bookmarkedIds: Set<String>,
  onCitySelected: (CityLocation) -> Unit,
  onMadhhabToggled: (Boolean) -> Unit,
  onPostClick: (IslamicPost) -> Unit,
  onNavigateToCategories: () -> Unit,
) {
  val context = LocalContext.current
  val currentPosts = SampleIslamicData.posts
  val ayatOfTheDay = currentPosts.firstOrNull { it.isAyatOfTheDay } ?: currentPosts.firstOrNull() ?: SampleIslamicData.defaultPosts.first()
  val dailyHadith = currentPosts.firstOrNull { it.isDailyHadith } ?: currentPosts.getOrNull(1) ?: ayatOfTheDay
  val allPosts = currentPosts.filter { it.id != ayatOfTheDay.id }

  // Dialog states for features
  var showTasbihDialog by remember { mutableStateOf(false) }
  var showNamesOfAllahDialog by remember { mutableStateOf(false) }
  var showIslamicCalendarDialog by remember { mutableStateOf(false) }
  var showIslamicQuizDialog by remember { mutableStateOf(false) }
  var showCityDialog by remember { mutableStateOf(false) }

  // Country & City Selection State (GPS vs Manual City)
  var isUsingGps by remember { mutableStateOf(true) }
  var currentSelectedCity by remember { mutableStateOf(selectedCity) }

  // Dynamic Location State
  var userLocationInfo by remember {
    mutableStateOf(LocationAndNotificationHelper.getCurrentDeviceLocation(context))
  }

  // Live Clock & Hijri Date (Timezone aware)
  var liveTimeText by remember { mutableStateOf(IslamicDateHelper.getLiveTimeString(currentLang)) }
  var cityLiveClockText by remember(isUsingGps, currentSelectedCity, currentLang) {
    mutableStateOf(
      if (isUsingGps) IslamicDateHelper.getLiveTimeString(currentLang)
      else PrayerTimeCalculator.getCityLiveTime(currentSelectedCity.timeZoneId, currentLang)
    )
  }

  LaunchedEffect(isUsingGps, currentSelectedCity, currentLang) {
    while (true) {
      delay(1000)
      liveTimeText = IslamicDateHelper.getLiveTimeString(currentLang)
      cityLiveClockText = if (isUsingGps) IslamicDateHelper.getLiveTimeString(currentLang)
      else PrayerTimeCalculator.getCityLiveTime(currentSelectedCity.timeZoneId, currentLang)
    }
  }

  val (gregFormatted, hijriFormatted) = remember(currentLang) {
    IslamicDateHelper.getFormattedDates(currentLang)
  }
  val banglaSolarDate = remember {
    IslamicDateHelper.getBanglaSolarDate()
  }

  // Notification Permission State
  var hasNotificationPermission by remember {
    mutableStateOf(
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
      } else {
        true
      }
    )
  }

  // Location Permission State
  var hasLocationPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    )
  }

  // Notification Permission Launcher
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasNotificationPermission = isGranted
  }

  // Location Permission Launcher
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    hasLocationPermission = granted
    if (granted) {
      userLocationInfo = LocationAndNotificationHelper.getCurrentDeviceLocation(context)
      LocationAndNotificationHelper.requestFreshDeviceLocation(context) { freshLocation ->
        userLocationInfo = freshLocation
      }
    }
  }

  // Azan Reminder Alert State
  var isAzanPlaying by remember { mutableStateOf(false) }
  var azanSecondsLeft by remember { mutableIntStateOf(20) }
  var currentMediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

  // 20-second timer countdown when Azan triggers
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

  // Active coordinates & timezone based on GPS vs manual selection
  val activeLat = if (isUsingGps) userLocationInfo.latitude else currentSelectedCity.latitude
  val activeLng = if (isUsingGps) userLocationInfo.longitude else currentSelectedCity.longitude
  val activeTz = if (isUsingGps) null else currentSelectedCity.timeZoneId

  // Live Dynamic Prayer Times Calculation based on user location coordinates and timezone
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

  // Live second-by-second countdown for next prayer
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

  // Find active prayer index for single line grid auto-scroll to center
  val activeIndex = remember(activePrayer) {
    val idx = prayersList.indexOfFirst { it.first.equals(activePrayer, ignoreCase = true) }
    if (idx >= 0) idx else 2
  }

  val prayerListState = rememberLazyListState()
  LaunchedEffect(activeIndex) {
    prayerListState.animateScrollToItem(activeIndex)
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
  ) {

    // SECTION 1: Address Bar with Auto Live Time, Date, Month, Day Name & Arabic (Hijri) Date
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Row 1: Detected Address & Buttons (Country/City change & GPS)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
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
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = if (isUsingGps) {
                    when (currentLang) {
                      "bn" -> "স্বয়ংক্রিয় জিপিএস অবস্থান"
                      "ar" -> "موقع GPS تلقائي"
                      else -> "Auto GPS Location"
                    }
                  } else {
                    when (currentLang) {
                      "bn" -> "নির্বাচিত শহর ও দেশ"
                      "ar" -> "المدينة المختارة"
                      else -> "Selected City"
                    }
                  },
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            // Buttons: Country/City Selector & GPS
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
              // 1. Country / City Selector Button
              Surface(
                onClick = { showCityDialog = true },
                shape = RoundedCornerShape(12.dp),
                color = if (!isUsingGps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    Icons.Default.Public,
                    contentDescription = "Country/City",
                    modifier = Modifier.size(13.dp),
                    tint = if (!isUsingGps) Color.White else MaterialTheme.colorScheme.primary
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = when (currentLang) { "bn" -> "দেশ/শহর"; else -> "City" },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isUsingGps) Color.White else MaterialTheme.colorScheme.primary
                  )
                }
              }

              // 2. GPS Button
              Surface(
                onClick = {
                  isUsingGps = true
                  if (!hasLocationPermission) {
                    locationPermissionLauncher.launch(
                      arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                  } else {
                    userLocationInfo = LocationAndNotificationHelper.getCurrentDeviceLocation(context)
                    LocationAndNotificationHelper.requestFreshDeviceLocation(context) { freshLocation ->
                      userLocationInfo = freshLocation
                    }
                  }
                },
                shape = RoundedCornerShape(12.dp),
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
                    modifier = Modifier.size(13.dp),
                    tint = if (isUsingGps) Color.White else MaterialTheme.colorScheme.primary
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = when (currentLang) {
                      "bn" -> if (hasLocationPermission) "জিপিএস" else "অনুমতি"
                      else -> if (hasLocationPermission) "GPS" else "Permission"
                    },
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
            modifier = Modifier.padding(vertical = 12.dp)
          )

          // Row 2: Live Clock Time & Status Badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${if (isUsingGps) (if (currentLang == "bn") "স্থানীয় সময়: " else "Time: ") else (if (currentLang == "bn") "ঘড়ির সময়: " else "Clock: ")}$cityLiveClockText",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
              Text(
                text = when (currentLang) { "bn" -> "লাইভ ঘড়ি"; "ar" -> "مباشر"; else -> "Live Clock" },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 1. হিজরির উপরে: ইংরেজি ক্যালেন্ডার (English Calendar Bar - styled like Hijri)
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

          if (currentLang == "bn") {
            Spacer(modifier = Modifier.height(6.dp))
            // ৩. বাংলা ক্যালেন্ডার (বঙ্গাব্দ)
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

    // SECTION 1.5: Prominent Sehri & Iftar Time Card with Live Countdowns
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
          // Sehri Column
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Bedtime,
                contentDescription = null,
                tint = IslamicEmeraldPrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = when (currentLang) {
                  "bn" -> "সেহরির শেষ সময়"
                  "ar" -> "نهاية السحور"
                  else -> "Sehri Ends"
                },
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
            Spacer(modifier = Modifier.height(2.dp))
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

          // Iftar Column
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Restaurant,
                contentDescription = null,
                tint = IslamicGoldAccent,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = when (currentLang) {
                  "bn" -> "ইফতারের সময়"
                  "ar" -> "وقت الإفطار"
                  else -> "Iftar Time"
                },
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
            Spacer(modifier = Modifier.height(2.dp))
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

    // Notification Permission Request Card
    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
          border = BorderStroke(1.dp, IslamicEmeraldPrimary.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .background(IslamicEmeraldPrimary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = when (currentLang) {
                  "bn" -> "আযান নোটিফিকেশন চালু করুন"
                  "ar" -> "تفعيل تنبيهات الأذان"
                  else -> "Enable Azan Notifications"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = when (currentLang) {
                  "bn" -> "ওয়াক্ত শুরু হলে ২০ সেকেন্ড আযান ও নোটিফিকেশন পেতে অনুমতি দিন।"
                  "ar" -> "اسمح بالإشعارات لتلقي تنبيه الأذان لمدة ٢٠ ثانية عند دخول الوقت."
                  else -> "Allow notifications to receive a 20-second Azan alert when prayer time starts."
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              },
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(
                text = when (currentLang) {
                  "bn" -> "অনুমতি দিন"
                  "ar" -> "سماح"
                  else -> "Allow"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // 20-Second Azan Notification Alert Banner
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
                text = "الله أكبر، الله أكبر • নামাজের ওয়াক্ত হয়েছে",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              Text(
                text = "আযান বাজছে... আর $azanSecondsLeft সেকেন্ড বাকি",
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

    // SECTION 2: Prayer Times Single Line Grid (এক লাইনে সব, রানিং ওয়াক্ত মাঝে ফোকাসে)
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {

          // Countdown Top Banner
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(
                Brush.linearGradient(
                  listOf(
                    IslamicEmeraldPrimary,
                    IslamicEmeraldDark
                  )
                )
              )
              .padding(16.dp)
          ) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = when (currentLang) {
                    "bn" -> "পরবর্তী নামাজ: ${prayerCalculation.nextPrayerNameBn}"
                    "ar" -> "الصلاة القادمة: ${prayerCalculation.nextPrayerNameAr}"
                    else -> "Next Prayer: ${prayerCalculation.nextPrayerNameEn}"
                  },
                  color = IslamicGoldLight,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )

                // 20-Second Azan Test Button
                Surface(
                  onClick = {
                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    isAzanPlaying = true
                  },
                  shape = RoundedCornerShape(20.dp),
                  color = IslamicGoldAccent.copy(alpha = 0.25f),
                  border = BorderStroke(1.dp, IslamicGoldLight)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      Icons.Default.NotificationsActive,
                      contentDescription = null,
                      tint = IslamicGoldLight,
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = when (currentLang) {
                        "bn" -> "আযান টেস্ট"
                        "ar" -> "تجربة الأذان"
                        else -> "Test Azan"
                      },
                      color = IslamicGoldLight,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = countdownFormatted,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
              )

              Spacer(modifier = Modifier.height(2.dp))

              Text(
                text = when (currentLang) {
                  "bn" -> "চলমান ওয়াক্ত: ${prayerCalculation.activePrayerNameBn}"
                  "ar" -> "الوقت الحالي: ${prayerCalculation.activePrayerNameAr}"
                  else -> "Current Waqt: ${prayerCalculation.activePrayerNameEn}"
                },
                color = Color(0xFFD1FAE5),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Prayer Times Section Header with Hanafi Badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = when (currentLang) {
                "bn" -> "নামাজের সময়সূচি"
                "ar" -> "مواقيت الصلاة"
                else -> "Prayer Schedule"
              },
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
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
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // SINGLE LINE HORIZONTAL GRID WITH ACTIVE WAQT IN MIDDLE & FOCUSED
          LazyRow(
            state = prayerListState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(prayersList) { (code, name, time) ->
              val isActive = code.equals(activePrayer, ignoreCase = true)

              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isActive) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                ),
                border = BorderStroke(
                  if (isActive) 2.dp else 1.dp,
                  if (isActive) IslamicGoldLight else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(if (isActive) 6.dp else 0.dp),
                modifier = Modifier
                  .width(if (isActive) 130.dp else 105.dp)
                  .height(115.dp)
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.SpaceBetween
                ) {
                  // Active Badge or Icon
                  if (isActive) {
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = IslamicGoldAccent,
                      modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                      Text(
                        text = when (currentLang) {
                          "bn" -> "চলমান ওয়াক্ত"
                          "ar" -> "الوقت الحالي"
                          else -> "Active Now"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  } else {
                    Icon(
                      when (code) {
                        "Fajr" -> Icons.Default.WbTwilight
                        "Sunrise" -> Icons.Default.WbSunny
                        "Dhuhr" -> Icons.Default.LightMode
                        "Asr" -> Icons.Default.Brightness5
                        "Maghrib" -> Icons.Default.NightsStay
                        else -> Icons.Default.Bedtime
                      },
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp)
                    )
                  }

                  // Prayer Name
                  Text(
                    text = name,
                    fontSize = if (isActive) 14.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                  )

                  // Prayer Time
                  Text(
                    text = time,
                    fontSize = if (isActive) 16.sp else 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) IslamicGoldLight else MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                  )
                }
              }
            }
          }
        }
      }
    }

    // SECTION 3: Quick Islamic Tools (Tasbih, 99 Names, Islamic Calendar)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Tool 1: Digital Tasbih
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .weight(1f)
            .clickable { showTasbihDialog = true }
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = when (currentLang) {
                "bn" -> "ডিজিটাল তাসবীহ"
                "ar" -> "السبحة الإلكترونية"
                else -> "Digital Tasbih"
              },
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center
            )
            Text(
              text = when (currentLang) {
                "bn" -> "দোয়া ও জিকির"
                "ar" -> "الأذكار والأدعية"
                else -> "Dua & Dhikr"
              },
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        // Tool 2: 99 Names of Allah
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .weight(1f)
            .clickable { showNamesOfAllahDialog = true }
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .background(IslamicEmeraldPrimary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Star, contentDescription = null, tint = IslamicGoldAccent, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = when (currentLang) {
                "bn" -> "আল্লাহর ৯৯ নাম"
                "ar" -> "أسماء الله الحسنى"
                else -> "99 Names"
              },
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center
            )
            Text(
              text = when (currentLang) {
                "bn" -> "আসমাউল হুসনা"
                "ar" -> "الأسماء الحسنى"
                else -> "Asmaul Husna"
              },
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        // Tool 3: Islamic Calendar
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .weight(1f)
            .clickable { showIslamicCalendarDialog = true }
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .background(MaterialTheme.colorScheme.secondary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = when (currentLang) {
                "bn" -> "হিজরি ক্যালেন্ডার"
                "ar" -> "التقويم الهجري"
                else -> "Calendar"
              },
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center
            )
            Text(
              text = when (currentLang) {
                "bn" -> "ইসলামিক দিবস"
                "ar" -> "المناسبات"
                else -> "Islamic Events"
              },
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }

    // SECTION 3.5: Islamic Quiz Banner
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, IslamicEmeraldPrimary.copy(alpha = 0.4f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showIslamicQuizDialog = true }
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .background(IslamicEmeraldPrimary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.HelpOutline,
                contentDescription = "Quiz",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = when (currentLang) {
                    "bn" -> "ইসলামিক কুইজ খেলুন"
                    "ar" -> "المسابقة الإسلامية"
                    else -> "Play Islamic Quiz"
                  },
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = IslamicGoldAccent
                ) {
                  Text(
                    text = "NEW",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = when (currentLang) {
                  "bn" -> "কুরআন, হাদিস ও নবীদের জীবন সম্পর্কিত জ্ঞান যাচাই করুন"
                  "ar" -> "اختبر معلوماتك في القرآن والحديث والسيرة"
                  else -> "Test your knowledge on Quran, Hadith & Seerah"
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }
    }

    // SECTION 4: Ayat of the Day
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onPostClick(ayatOfTheDay) }
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = when (currentLang) {
                  "bn" -> "আজকের আয়াত"
                  "ar" -> "آية اليوم"
                  else -> "Ayat of the Day"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = ayatOfTheDay.localizedTitle(currentLang),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = ayatOfTheDay.localizedSubtitle(currentLang),
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // SECTION 5: Daily Hadith
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onPostClick(dailyHadith) }
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = when (currentLang) {
                  "bn" -> "দৈনিক হাদিস"
                  "ar" -> "حديث اليوم"
                  else -> "Daily Hadith"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
              )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = dailyHadith.localizedTitle(currentLang),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = dailyHadith.localizedSubtitle(currentLang),
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // SECTION 6: Islamic Articles Feed
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = when (currentLang) {
            "bn" -> "দ্বীনি প্রবন্ধ ও আলোচনা"
            "ar" -> "مقالات إسلامية"
            else -> "Islamic Articles"
          },
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        TextButton(onClick = onNavigateToCategories) {
          Text(
            text = when (currentLang) {
              "bn" -> "সব দেখুন"
              "ar" -> "عرض الكل"
              else -> "View All"
            },
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    items(allPosts.take(4)) { post ->
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onPostClick(post) }
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.Article,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = post.localizedTitle(currentLang),
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = post.localizedSubtitle(currentLang),
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
          Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }

  // Active Dialogs for Tasbih, 99 Names, Calendar
  if (showTasbihDialog) {
    TasbihDialog(onDismiss = { showTasbihDialog = false })
  }

  if (showNamesOfAllahDialog) {
    NamesOfAllahDialog(onDismiss = { showNamesOfAllahDialog = false })
  }

  if (showIslamicCalendarDialog) {
    IslamicCalendarDialog(currentLang = currentLang, onDismiss = { showIslamicCalendarDialog = false })
  }

  if (showIslamicQuizDialog) {
    IslamicQuizDialog(currentLang = currentLang, onDismiss = { showIslamicQuizDialog = false })
  }

  if (showCityDialog) {
    CitySelectionDialog(
      currentLang = currentLang,
      selectedCity = currentSelectedCity,
      isUsingGps = isUsingGps,
      onSelectGps = {
        isUsingGps = true
        userLocationInfo = LocationAndNotificationHelper.getCurrentDeviceLocation(context)
        if (hasLocationPermission) {
          LocationAndNotificationHelper.requestFreshDeviceLocation(context) { freshLocation ->
            userLocationInfo = freshLocation
          }
        }
      },
      onSelectCity = { city ->
        isUsingGps = false
        currentSelectedCity = city
        onCitySelected(city)
      },
      onDismiss = { showCityDialog = false }
    )
  }
}
