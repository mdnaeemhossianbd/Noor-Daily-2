package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.AppThemeMode

private const val APP_WEBSITE_URL = "https://noordaily.app"
private const val SUPPORT_EMAIL = "support@noordaily.com"

@Composable
fun SettingsScreen(
  currentLang: String,
  currentThemeMode: AppThemeMode,
  onThemeModeSelected: (AppThemeMode) -> Unit,
  onLanguageSelected: (String) -> Unit,
) {
  val context = LocalContext.current
  var soundEnabled by remember { mutableStateOf(true) }

  var showAboutDialog by remember { mutableStateOf(false) }

  var hasNotificationPermission by remember {
    mutableStateOf(
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
      } else {
        true
      }
    )
  }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasNotificationPermission = isGranted
    if (isGranted) {
      Toast.makeText(context, "আযান নোটিফিকেশন চালু করা হয়েছে", Toast.LENGTH_SHORT).show()
    }
  }

  // Open browser helper safely
  fun openWebUrl(url: String) {
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: Exception) {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
      clipboard?.setPrimaryClip(ClipData.newPlainText("URL", url))
      Toast.makeText(context, "লিংক কপি করা হয়েছে: $url", Toast.LENGTH_SHORT).show()
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
  ) {

    // Header
    item {
      Text(
        text = when (currentLang) {
          "bn" -> "সেটিংস ও পছন্দসমূহ"
          "ar" -> "الإعدادات والتفضيلات"
          else -> "Settings & Preferences"
        },
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    // Section 1: App Theme (Dark Mode Default)
    item {
      Text(
        text = when (currentLang) {
          "bn" -> "অ্যাপ থিম নির্বাচন (ডিফল্ট ডার্ক মোড)"
          "ar" -> "مظهر التطبيق (الوضع الداكن افتراضياً)"
          else -> "App Theme (Default Dark Mode)"
        },
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column {
          ListItem(
            headlineContent = {
              Text(
                when (currentLang) {
                  "bn" -> "ডার্ক মিডনাইট এমারেল্ড (ডিফল্ট ডার্ক মোড)"
                  "ar" -> "الزمرد الليلي الداكن (الوضع الليلي)"
                  else -> "Dark Midnight Emerald (Default Dark Mode)"
                },
                fontWeight = if (currentThemeMode == AppThemeMode.DARK_MIDNIGHT) FontWeight.Bold else FontWeight.SemiBold
              )
            },
            supportingContent = {
              Text(
                when (currentLang) {
                  "bn" -> "রাতের আরামদায়ক চোখ-বান্ধব ডার্ক মোড"
                  "ar" -> "وضع داكن مريح للعين في الليل"
                  else -> "Comfortable eye-friendly dark mode for night reading"
                }
              )
            },
            leadingContent = {
              RadioButton(
                selected = currentThemeMode == AppThemeMode.DARK_MIDNIGHT,
                onClick = { onThemeModeSelected(AppThemeMode.DARK_MIDNIGHT) }
              )
            },
            trailingContent = {
              Icon(
                Icons.Default.DarkMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onThemeModeSelected(AppThemeMode.DARK_MIDNIGHT) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
          ListItem(
            headlineContent = {
              Text(
                when (currentLang) {
                  "bn" -> "ক্লাসিক পিওর এমারেল্ড গ্রিন"
                  "ar" -> "الأخضر الزمردي الكلاسيكي"
                  else -> "Classic Pure Emerald Green"
                },
                fontWeight = if (currentThemeMode == AppThemeMode.CLASSIC_EMERALD) FontWeight.Bold else FontWeight.SemiBold
              )
            },
            supportingContent = {
              Text(
                when (currentLang) {
                  "bn" -> "খাঁটি ইসলামিক সবুজ ও উজ্জ্বল সাদা থিম"
                  "ar" -> "مظهر إسلامي أخضر نقي ومشرق"
                  else -> "Pure Islamic green & bright theme"
                }
              )
            },
            leadingContent = {
              RadioButton(
                selected = currentThemeMode == AppThemeMode.CLASSIC_EMERALD,
                onClick = { onThemeModeSelected(AppThemeMode.CLASSIC_EMERALD) }
              )
            },
            trailingContent = {
              Icon(
                Icons.Default.LightMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onThemeModeSelected(AppThemeMode.CLASSIC_EMERALD) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
          ListItem(
            headlineContent = {
              Text(
                when (currentLang) {
                  "bn" -> "ওয়ার্ম ক্রিম ও এমারেল্ড (Warm Cream)"
                  "ar" -> "كريم دافئ وزمردي"
                  else -> "Warm Cream & Emerald"
                },
                fontWeight = if (currentThemeMode == AppThemeMode.WARM_CREAM) FontWeight.Bold else FontWeight.SemiBold
              )
            },
            supportingContent = {
              Text(
                when (currentLang) {
                  "bn" -> "নরম ক্রিম সারফেস ও গাঢ় সবুজ অ্যাকসেন্ট"
                  "ar" -> "سطح كريمي ناعم مع لمسات خضراء عميقة"
                  else -> "Soft cream surface with deep green accents"
                }
              )
            },
            leadingContent = {
              RadioButton(
                selected = currentThemeMode == AppThemeMode.WARM_CREAM,
                onClick = { onThemeModeSelected(AppThemeMode.WARM_CREAM) }
              )
            },
            trailingContent = {
              Icon(
                Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onThemeModeSelected(AppThemeMode.WARM_CREAM) }
          )
        }
      }
    }

    // Section 2: Notifications & Azan
    item {
      Text(
        text = when (currentLang) {
          "bn" -> "নামাজের সময় আযান ও নোটিফিকেশন"
          "ar" -> "تنبيهات الأذان والصلوات"
          else -> "Prayer Notifications"
        },
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column {
          ListItem(
            headlineContent = {
              Text(
                when (currentLang) {
                  "bn" -> "সিস্টেম নোটিফিকেশন অনুমতি"
                  "ar" -> "إذن إشعارات النظام"
                  else -> "System Notification Permission"
                },
                fontWeight = FontWeight.SemiBold
              )
            },
            supportingContent = {
              Text(
                when (currentLang) {
                  "bn" -> if (hasNotificationPermission) "অনুমতি সক্রিয় আছে (আযান অ্যালার্ট পাওয়া যাবে)"
                          else "অনুমতি বন্ধ আছে — ওয়াক্তে আযান পেতে অনুমতি দিন"
                  "ar" -> if (hasNotificationPermission) "الإذن مفعل (سيتم تلقي تنبيهات الأذان)"
                          else "الإذن معطل — اسمح بالإشعارات لتلقي تنبيهات الأذان"
                  else -> if (hasNotificationPermission) "Permission active (Azan alerts enabled)"
                          else "Permission disabled — allow notifications to receive Azan alerts"
                }
              )
            },
            leadingContent = {
              Icon(
                if (hasNotificationPermission) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                contentDescription = null,
                tint = if (hasNotificationPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
              )
            },
            trailingContent = {
              if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Button(
                  onClick = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Text(
                    when (currentLang) {
                      "bn" -> "অনুমতি দিন"
                      "ar" -> "منح الإذن"
                      else -> "Grant"
                    },
                    fontSize = 11.sp
                  )
                }
              }
            }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
          ListItem(
            headlineContent = {
              Text(
                when (currentLang) {
                  "bn" -> "ওয়াক্তে ২০ সেকেন্ড আযান বাজানো"
                  "ar" -> "تشغيل الأذان لمدة ٢٠ ثانية عند دخول الوقت"
                  else -> "Play 20-Second Azan on Prayer Waqt"
                },
                fontWeight = FontWeight.SemiBold
              )
            },
            supportingContent = {
              Text(
                when (currentLang) {
                  "bn" -> "ওয়াক্ত শুরু হওয়া মাত্র ২০ সেকেন্ড সুরের আযান বাজবে"
                  "ar" -> "يبدأ تنبيه الأذان العذب فور دخول وقت الصلاة"
                  else -> "Plays a 20-second melodious Azan alert as soon as prayer time starts"
                }
              )
            },
            leadingContent = {
              Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingContent = {
              Switch(
                checked = soundEnabled,
                onCheckedChange = {
                  soundEnabled = it
                  val prefs = context.getSharedPreferences("noor_daily_prefs", android.content.Context.MODE_PRIVATE)
                  prefs.edit().putBoolean("azan_notifications_enabled", it).apply()
                  if (it) {
                    com.example.util.PrayerAlarmScheduler.scheduleNextPrayerAlarm(context)
                  } else {
                    com.example.util.PrayerAlarmScheduler.cancelAlarms(context)
                  }
                }
              )
            }
          )
        }
      }
    }

    // Section 3: Language
    item {
      Text(
        text = when (currentLang) {
          "bn" -> "ভাষা নির্বাচন"
          "ar" -> "اختيار اللغة"
          else -> "Language Selection"
        },
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column {
          ListItem(
            headlineContent = { Text("বাংলা (Bangla)") },
            supportingContent = { Text("বাংলা ভাষায় সকল কনটেন্ট ও মেন্যু") },
            leadingContent = {
              RadioButton(
                selected = currentLang == "bn",
                onClick = { onLanguageSelected("bn") }
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onLanguageSelected("bn") }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
          ListItem(
            headlineContent = { Text("English") },
            supportingContent = { Text("English language interface") },
            leadingContent = {
              RadioButton(
                selected = currentLang == "en",
                onClick = { onLanguageSelected("en") }
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onLanguageSelected("en") }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
          ListItem(
            headlineContent = { Text("العربية (Arabic)") },
            supportingContent = { Text("اللغة العربية مع دعم المحاذاة") },
            leadingContent = {
              RadioButton(
                selected = currentLang == "ar",
                onClick = { onLanguageSelected("ar") }
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onLanguageSelected("ar") }
          )
        }
      }
    }

    // Section 4: App About
    item {
      Text(
        text = when (currentLang) {
          "bn" -> "অ্যাপ পরিচিতি"
          "ar" -> "حول التطبيق"
          else -> "About Noor Daily"
        },
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column {
          // App About Opener
          ListItem(
            headlineContent = {
              Text(
                when (currentLang) {
                  "bn" -> "অ্যাপ সম্পর্কে (About Noor Daily)"
                  "ar" -> "حول التطبيق"
                  else -> "About Noor Daily"
                },
                fontWeight = FontWeight.SemiBold
              )
            },
            supportingContent = {
              Text("Noor Daily • v1.2.0 • মিশন ও উৎসসমূহ")
            },
            leadingContent = {
              Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
            },
            trailingContent = {
              Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showAboutDialog = true }
          )
        }
      }
    }

    // Minimal Clean Footer (No Firebase numbers, no unwanted text)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Noor Daily (نور اليومي) • Version 1.2.0",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          fontWeight = FontWeight.Medium
        )
      }
    }
  }

  // --- ABOUT APP DIALOG ---
  if (showAboutDialog) {
    AlertDialog(
      onDismissRequest = { showAboutDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.Mosque,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Noor Daily (نূর ডেইলি)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
        }
      },
      text = {
        val scrollState = rememberScrollState()
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp)
            .verticalScroll(scrollState),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = when (currentLang) {
              "bn" -> "সংস্করণ ১.২.০ (Build 2026)"
              "ar" -> "الإصدار ١.٢.٠ (Build 2026)"
              else -> "Version 1.2.0 (Build 2026)"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          Text(
            text = when (currentLang) {
              "bn" -> "নূর ডেইলি বিশ্বজুড়ে মুসলিম উম্মাহর দৈনন্দিন ইবাদত ও দ্বীনি দিকনির্দেশনার জন্য একটি আধুনিক ও নির্ভরযোগ্য ডিজিটাল মাধ্যম।"
              "ar" -> "نور ديلي وسيلة رقمية حديثة وموثوقة لمساعدة المسلمين حول العالم في عباداتهم اليومية وإرشاداتهم الإسلامية."
              else -> "Noor Daily is a modern, reliable digital companion for Muslims worldwide, supporting daily worship and Islamic guidance."
            },
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

          Text(
            text = when (currentLang) {
              "bn" -> "প্রধান বৈশিষ্ট্যসমূহ:"
              "ar" -> "الميزات الرئيسية:"
              else -> "Key Features:"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
          Text(
            text = when (currentLang) {
              "bn" -> "• জিপিএস লোকেশন অনুযায়ী রিয়েল-টাইম নামাজের ওয়াক্ত\n• ওয়াক্তের শুরুতে ২০ সেকেন্ড আযান অ্যালার্ট\n• সহীহ বুখারী, মুসলিম ও আবু দাউদ থেকে সংগৃহীত সহীহ হাদিস\n• বিষয়ভিত্তিক পবিত্র কুরআনের আয়াত ও বঙ্গানুবাদ\n• তাহাজ্জুদ ও রোজা রাখার বিশেষ আমল ও রুটিন"
              "ar" -> "• أوقات الصلاة حسب موقع GPS\n• تنبيه الأذان لمدة ٢٠ ثانية عند دخول الوقت\n• أحاديث من صحيح البخاري ومسلم وأبي داود\n• آيات قرآنية مع الترجمة البنغالية حسب الموضوع\n• إرشادات للتهجد والصيام"
              else -> "• Real-time prayer times based on GPS location\n• 20-second Adhan alert at the start of prayer time\n• Authentic Hadith from Sahih Bukhari, Muslim and Abu Dawud\n• Topic-based Quran verses with Bangla translations\n• Guidance for Tahajjud and fasting"
            },
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

          Text(
            text = when (currentLang) {
              "bn" -> "যোগাযোগ ও ওয়েবসাইট:"
              "ar" -> "التواصل والموقع الإلكتروني:"
              else -> "Contact & Website:"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
          Text(
            text = when (currentLang) {
              "bn" -> "ওয়েবসাইট: $APP_WEBSITE_URL\nইমেইল: $SUPPORT_EMAIL"
              "ar" -> "الموقع: $APP_WEBSITE_URL\nالبريد الإلكتروني: $SUPPORT_EMAIL"
              else -> "Website: $APP_WEBSITE_URL\nEmail: $SUPPORT_EMAIL"
            },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { openWebUrl(APP_WEBSITE_URL) },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Text(when (currentLang) { "bn" -> "ওয়েবসাইট ভিজিট"; "ar" -> "زيارة الموقع"; else -> "Visit Website" }, fontSize = 12.sp)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAboutDialog = false }) {
          Text(when (currentLang) { "bn" -> "বন্ধ করুন"; "ar" -> "إغلاق"; else -> "Close" })
        }
      }
    )
  }
}
