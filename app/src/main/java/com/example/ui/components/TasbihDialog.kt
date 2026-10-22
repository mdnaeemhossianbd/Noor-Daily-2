package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldPrimary
import com.example.ui.theme.IslamicGoldAccent
import com.example.ui.theme.IslamicGoldLight
import com.example.util.AppStrings

data class TasbihDua(
  val id: String,
  val arabic: String,
  val transliteration: String,
  val meaningBn: String,
  val targetCount: Int
)

val defaultTasbihDuas = listOf(
  TasbihDua("subhanallah", "سُبْحَانَ اللَّهِ", "সুবহানাল্লাহ", "আল্লাহ মহাপবিত্র ও ত্রুটিমুক্ত", 33),
  TasbihDua("alhamdulillah", "الْحَمْدُ لِلَّهِ", "আলহামদুলিল্লাহ", "সকল প্রশংসা কেবল আল্লাহর জন্য", 33),
  TasbihDua("allahuakbar", "اللَّهُ أَكْبَرُ", "আল্লাহু আকবার", "আল্লাহ সর্বশ্রেষ্ঠ ও মহান", 34),
  TasbihDua("lailaha", "لَا إِلَٰهَ إِلَّا اللَّهُ", "লা ইলাহা ইল্লাল্লাহ", "আল্লাহ ছাড়া কোনো সত্য উপাস্য নেই", 100),
  TasbihDua("astaghfirullah", "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", "আস্তাগফিরুল্লাহ ওয়া আতূবু ইলাইহি", "আমি আল্লাহর কাছে ক্ষমা চাই ও তওবা করছি", 100),
  TasbihDua("subhanallah_bihamdihi", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ", "সুবহানাল্লাহি ওয়া বিহামদিহি, সুবহানাল্লাহিল আজিম", "আল্লাহর প্রশংসাসহ পবিত্রতা ও মহত্ব ঘোষণা করছি", 100),
  TasbihDua("lahawla", "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "লা হাওলা ওয়ালা কুওয়াতা ইল্লা বিল্লাহ", "আল্লাহর সাহায্য ছাড়া কোনো শক্তি ও সামর্থ্য নেই", 100),
  TasbihDua("durood", "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ", "আল্লাহুম্মা সাল্লি আলা মুহাম্মাদ", "হে আল্লাহ, মুহাম্মদ (সা.) এর উপর রহমত বর্ষণ করুন", 100),
  TasbihDua("hasbunallah", "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ", "হাসবুনাল্লাহু ওয়া নি'মাল ওয়াকিল", "আল্লাহই আমাদের জন্য যথেষ্ট, তিনি কতই না উত্তম অভিভাবক", 100)
)

@Composable
fun TasbihDialog(
  currentLang: String,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var currentCount by remember { mutableIntStateOf(0) }
  var totalCount by remember { mutableIntStateOf(0) }
  var loopCount by remember { mutableIntStateOf(0) }
  var selectedDua by remember { mutableStateOf(defaultTasbihDuas.first()) }
  var isTargetReached by remember { mutableStateOf(false) }
  var isPressed by remember { mutableStateOf(false) }

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.92f else 1f,
    label = "tapScale"
  )

  fun triggerHaptic() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(30)
        }
      }
    } catch (_: Exception) {}
  }

  fun handleCountIncrement() {
    triggerHaptic()
    currentCount++
    totalCount++
    if (selectedDua.targetCount > 0 && currentCount >= selectedDua.targetCount) {
      loopCount++
      currentCount = 0
      isTargetReached = true
    } else {
      isTargetReached = false
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.75f))
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.TouchApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = AppStrings.text(currentLang, "tasbih.title"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = AppStrings.text(currentLang, "common.close"), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Dua Selection Chips
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
          ) {
            items(defaultTasbihDuas) { dua ->
              val isSelected = dua.id == selectedDua.id
              Surface(
                onClick = {
                  selectedDua = dua
                  currentCount = 0
                },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
              ) {
                Text(
                  text = dua.transliteration,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Selected Dua Display Banner
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = selectedDua.arabic,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = selectedDua.meaningBn,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${AppStrings.text(currentLang, "tasbih.target")}: ${AppStrings.digits(currentLang, selectedDua.targetCount)} • ${AppStrings.text(currentLang, "tasbih.completed")}: ${AppStrings.digits(currentLang, loopCount)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Big Interactive Tap Counter Bead Button
          Box(
            modifier = Modifier
              .scale(scale)
              .size(190.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  colors = listOf(
                    IslamicEmeraldPrimary,
                    IslamicEmeraldDark
                  )
                )
              )
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) {
                isPressed = true
                handleCountIncrement()
                isPressed = false
              },
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Text(
                text = AppStrings.digits(currentLang, currentCount),
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
              Text(
                text = AppStrings.text(currentLang, "tasbih.tap"),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicGoldLight
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Total and Reset Actions
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "${AppStrings.text(currentLang, "tasbih.total")}: ${AppStrings.digits(currentLang, totalCount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = AppStrings.text(currentLang, "tasbih.quote"),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            TextButton(
              onClick = {
                currentCount = 0
                loopCount = 0
              },
              colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = AppStrings.text(currentLang, "common.reset"), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(AppStrings.text(currentLang, "common.reset"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}
