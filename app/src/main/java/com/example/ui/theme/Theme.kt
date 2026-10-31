package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LuxuryCharcoalGoldColorScheme =
  darkColorScheme(
      primary = WarmGold,
      onPrimary = CharcoalBlack,
      primaryContainer = WarmGoldDark,
      onPrimaryContainer = CharcoalBlack,
      secondary = MutedGoldText,
      onSecondary = CharcoalBlack,
      tertiary = WarmGoldLight,
      background = CharcoalBlack,
      onBackground = CreamText,
      surface = CharcoalSurface,
      onSurface = CreamText,
      surfaceVariant = CharcoalVariant,
      onSurfaceVariant = MutedGoldText,
      error = AlertRed,
      onError = Color.White
  )

@Composable
fun QuickBillTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Always use the stunning Charcoal Black & Warm Gold luxury palette as requested
  val colorScheme = LuxuryCharcoalGoldColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
