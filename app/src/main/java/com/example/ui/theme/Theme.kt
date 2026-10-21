package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
  DARK_MIDNIGHT,   // Dark emerald midnight (Default)
  CLASSIC_EMERALD, // Classic Pure Islamic emerald green with crisp surfaces
  WARM_CREAM       // Warm cream & soft emerald
}

// 1. Dark Midnight Emerald (Default Theme)
private val DarkMidnightColorScheme =
  darkColorScheme(
    primary = IslamicEmeraldNeon,
    onPrimary = Color(0xFF042E1B),
    primaryContainer = Color(0xFF0E3825),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = IslamicGoldLight,
    onSecondary = Color.Black,
    surface = IslamicSurfaceDark,
    onSurface = IslamicTextLight,
    surfaceContainerHighest = IslamicSurfaceDarkElevated,
    background = IslamicBgDark,
    onBackground = IslamicTextLight,
    outline = Color(0xFF1E4D38),
    outlineVariant = Color(0xFF143828)
  )

// 2. Classic Emerald (Pure Green)
private val ClassicEmeraldColorScheme =
  lightColorScheme(
    primary = IslamicEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = IslamicEmeraldContainer,
    onPrimaryContainer = IslamicEmeraldDark,
    secondary = IslamicGoldAccent,
    onSecondary = Color.Black,
    surface = Color(0xFFFFFFFF),
    onSurface = IslamicTextDark,
    surfaceContainerHighest = Color(0xFFF2F7F4),
    background = Color(0xFFF8FAF9),
    onBackground = IslamicTextDark,
    outline = Color(0xFFD6E4DB),
    outlineVariant = Color(0xFFE2ECE5)
  )

// 3. Warm Cream & Emerald
private val WarmCreamColorScheme =
  lightColorScheme(
    primary = IslamicEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = IslamicEmeraldContainer,
    onPrimaryContainer = IslamicEmeraldDark,
    secondary = IslamicGoldAccent,
    onSecondary = Color.Black,
    surface = IslamicWarmCreamSurface,
    onSurface = IslamicTextDark,
    surfaceContainerHighest = Color(0xFFF1F5F2),
    background = IslamicWarmCreamBg,
    onBackground = IslamicTextDark,
    outline = IslamicWarmCreamBorder,
    outlineVariant = Color(0xFFDFE8E2)
  )

@Composable
fun NoorDailyTheme(
  themeMode: AppThemeMode = AppThemeMode.DARK_MIDNIGHT, // Dark Mode by default
  content: @Composable () -> Unit,
) {
  val colorScheme = when (themeMode) {
    AppThemeMode.DARK_MIDNIGHT -> DarkMidnightColorScheme
    AppThemeMode.CLASSIC_EMERALD -> ClassicEmeraldColorScheme
    AppThemeMode.WARM_CREAM -> WarmCreamColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
