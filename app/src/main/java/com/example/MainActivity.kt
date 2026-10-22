package com.example
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.SampleIslamicData
import com.example.model.IslamicPost
import com.example.ui.components.PostDetailDialog
import com.example.ui.screens.*
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.IslamicEmeraldPrimary
import com.example.ui.theme.NoorDailyTheme
import com.example.util.LocationAndNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

enum class NoorTab {
  HOME,
  CATEGORIES,
  PRAYERS,
  SAVED,
  SETTINGS,
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    LocationAndNotificationHelper.initNotificationChannel(this)
    com.example.util.PrayerAlarmScheduler.scheduleNextPrayerAlarm(this)

    setContent {
      val context = LocalContext.current
      var currentThemeMode by remember { mutableStateOf(AppThemeMode.DARK_MIDNIGHT) } // Dark Mode Default
      val appPreferences = remember { context.getSharedPreferences("noor_daily_prefs", android.content.Context.MODE_PRIVATE) }
      var currentLang by remember {
        mutableStateOf(appPreferences.getString("app_language", "bn") ?: "bn")
      }
      fun updateLanguage(language: String) {
        currentLang = language
        appPreferences.edit().putString("app_language", language).apply()
      }
      var currentTab by remember { mutableStateOf(NoorTab.HOME) }
      var selectedCity by remember { mutableStateOf(SampleIslamicData.cities.first()) }
      var isHanafi by remember { mutableStateOf(true) }
      val bookmarkedIds = remember { mutableStateListOf("post_ayat_today", "post_tahajjud") }
      var activePost by remember { mutableStateOf<IslamicPost?>(null) }

      // Notification permission launcher
      val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
      ) {}

      LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          }
        }
      }

      // Live Background Firebase Realtime Database Sync
      LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
          while (isActive) {
            SampleIslamicData.syncFromFirebase()
            delay(5000)
          }
        }
      }

      var showSplash by remember { mutableStateOf(true) }

      NoorDailyTheme(themeMode = currentThemeMode) {
        if (showSplash) {
          SplashScreen(onAnimationFinished = { showSplash = false })
        } else {
          // Back handler: return to Home if on another tab
          BackHandler(enabled = currentTab != NoorTab.HOME) {
            currentTab = NoorTab.HOME
          }

          Scaffold(
            modifier = Modifier.fillMaxSize(),
          topBar = {
            Surface(
              color = MaterialTheme.colorScheme.surface,
              tonalElevation = 2.dp
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .statusBarsPadding()
                  .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .background(IslamicEmeraldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      Icons.Default.Mosque,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = when (currentLang) {
                        "bn" -> "নূর ডেইলি"
                        "ar" -> "نور ديلي"
                        else -> "Noor Daily"
                      },
                      fontSize = 17.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                      text = when (currentLang) {
                        "bn" -> "দৈনন্দিন আমল ও নামাজের সময়সূচি"
                        "ar" -> "الإرشاد الإسلامي ومواقيت الصلاة"
                        else -> "Islamic Guidance & Prayer Times"
                      },
                      fontSize = 10.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                // Quick Language Switcher Dropdown / Pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  LanguageChip(
                    label = "বাং",
                    isSelected = currentLang == "bn",
                    onClick = { updateLanguage("bn") }
                  )
                  LanguageChip(
                    label = "EN",
                    isSelected = currentLang == "en",
                    onClick = { updateLanguage("en") }
                  )
                  LanguageChip(
                    label = "عربي",
                    isSelected = currentLang == "ar",
                    onClick = { updateLanguage("ar") }
                  )
                }
              }
            }
          },
          bottomBar = {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 6.dp
            ) {
              NavigationBarItem(
                selected = currentTab == NoorTab.HOME,
                onClick = { currentTab = NoorTab.HOME },
                icon = {
                  Icon(
                    if (currentTab == NoorTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                  )
                },
                label = {
                  Text(
                    when (currentLang) {
                      "bn" -> "হোম"
                      "ar" -> "الرئيسية"
                      else -> "Home"
                    },
                    fontSize = 11.sp
                  )
                }
              )

              NavigationBarItem(
                selected = currentTab == NoorTab.CATEGORIES,
                onClick = { currentTab = NoorTab.CATEGORIES },
                icon = {
                  Icon(
                    if (currentTab == NoorTab.CATEGORIES) Icons.Filled.Category else Icons.Outlined.Category,
                    contentDescription = "Categories"
                  )
                },
                label = {
                  Text(
                    when (currentLang) {
                      "bn" -> "ক্যাটাগরি"
                      "ar" -> "الأقسام"
                      else -> "Topics"
                    },
                    fontSize = 11.sp
                  )
                }
              )

              NavigationBarItem(
                selected = currentTab == NoorTab.PRAYERS,
                onClick = { currentTab = NoorTab.PRAYERS },
                icon = {
                  Icon(
                    if (currentTab == NoorTab.PRAYERS) Icons.Filled.AccessTime else Icons.Outlined.AccessTime,
                    contentDescription = "Prayer Times"
                  )
                },
                label = {
                  Text(
                    when (currentLang) {
                      "bn" -> "নামাজ"
                      "ar" -> "الصلاة"
                      else -> "Prayers"
                    },
                    fontSize = 11.sp
                  )
                }
              )

              NavigationBarItem(
                selected = currentTab == NoorTab.SAVED,
                onClick = { currentTab = NoorTab.SAVED },
                icon = {
                  Icon(
                    if (currentTab == NoorTab.SAVED) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Saved"
                  )
                },
                label = {
                  Text(
                    when (currentLang) {
                      "bn" -> "সংরক্ষিত"
                      "ar" -> "المحفوظات"
                      else -> "Saved"
                    },
                    fontSize = 11.sp
                  )
                }
              )

              NavigationBarItem(
                selected = currentTab == NoorTab.SETTINGS,
                onClick = { currentTab = NoorTab.SETTINGS },
                icon = {
                  Icon(
                    if (currentTab == NoorTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                  )
                },
                label = {
                  Text(
                    when (currentLang) {
                      "bn" -> "সেটিংস"
                      "ar" -> "الإعدادات"
                      else -> "Settings"
                    },
                    fontSize = 11.sp
                  )
                }
              )
            }
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            when (currentTab) {
              NoorTab.HOME -> HomeScreen(
                currentLang = currentLang,
                selectedCity = selectedCity,
                isHanafi = isHanafi,
                bookmarkedIds = bookmarkedIds.toSet(),
                onCitySelected = { selectedCity = it },
                onMadhhabToggled = { isHanafi = it },
                onPostClick = { activePost = it },
                onNavigateToCategories = { currentTab = NoorTab.CATEGORIES }
              )

              NoorTab.CATEGORIES -> CategoriesScreen(
                currentLang = currentLang,
                onPostClick = { activePost = it }
              )

              NoorTab.PRAYERS -> PrayerTimesScreen(
                currentLang = currentLang,
                selectedCity = selectedCity,
                isHanafi = isHanafi,
                onCitySelected = { selectedCity = it },
                onMadhhabToggled = { isHanafi = it }
              )

              NoorTab.SAVED -> SavedScreen(
                currentLang = currentLang,
                bookmarkedIds = bookmarkedIds.toSet(),
                onPostClick = { activePost = it },
                onRemoveBookmark = { id -> bookmarkedIds.remove(id) }
              )

              NoorTab.SETTINGS -> SettingsScreen(
                currentLang = currentLang,
                currentThemeMode = currentThemeMode,
                onThemeModeSelected = { currentThemeMode = it },
                onLanguageSelected = { updateLanguage(it) }
              )
            }
          }

          // Reader Dialog Modal
          activePost?.let { post ->
            val isBookmarked = bookmarkedIds.contains(post.id)
            PostDetailDialog(
              post = post,
              currentLang = currentLang,
              isBookmarked = isBookmarked,
              onToggleBookmark = {
                if (isBookmarked) {
                  bookmarkedIds.remove(post.id)
                } else {
                  bookmarkedIds.add(post.id)
                }
              },
              onDismiss = { activePost = null }
            )
          }
        }
      }
    }
  }
  }
}

@Composable
fun LanguageChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  Surface(
    onClick = onClick,
    shape = CircleShape,
    color = if (isSelected) IslamicEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier.height(28.dp)
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.padding(horizontal = 8.dp)
    ) {
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
