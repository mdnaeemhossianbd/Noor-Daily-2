package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldPrimary
import com.example.ui.theme.IslamicGoldAccent
import com.example.ui.theme.IslamicGoldLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onAnimationFinished: () -> Unit
) {
  val scale = remember { Animatable(0.7f) }
  val alpha = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    scale.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
    alpha.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 600)
    )
    delay(1200)
    alpha.animateTo(
      targetValue = 0f,
      animationSpec = tween(durationMillis = 300)
    )
    onAnimationFinished()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            IslamicEmeraldDark,
            Color(0xFF041E12),
            Color(0xFF02100A)
          )
        )
      )
      .alpha(alpha.value),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.scale(scale.value)
    ) {
      // Outer Glowing Ring with Mosque Logo
      Box(
        modifier = Modifier
          .size(130.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              listOf(
                IslamicEmeraldPrimary,
                IslamicEmeraldDark
              )
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(IslamicEmeraldDark),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Default.Mosque,
            contentDescription = "Noor Daily Logo",
            tint = IslamicGoldAccent,
            modifier = Modifier.size(64.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "Noor Daily",
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        letterSpacing = 1.sp
      )

      Text(
        text = "نور اليومي • নূর ডেইলি",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = IslamicGoldLight
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          Icons.Default.Star,
          contentDescription = null,
          tint = IslamicGoldAccent,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "দৈনন্দিন ইবাদত ও সঠিক নামাজের সময়সূচি",
          fontSize = 12.sp,
          color = Color(0xFFD1FAE5),
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}
