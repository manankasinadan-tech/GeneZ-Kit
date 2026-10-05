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

enum class AppThemeMode {
  SYSTEM,
  LIGHT,
  DARK
}

private val DarkColorScheme =
  darkColorScheme(
    primary = M3TealPrimaryDark,
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFF73F8E5),
    secondary = M3SecondaryDark,
    onSecondary = Color(0xFF1C3531),
    secondaryContainer = Color(0xFF334B47),
    onSecondaryContainer = Color(0xFFCCE8E2),
    tertiary = M3TertiaryDark,
    onTertiary = Color(0xFF133349),
    tertiaryContainer = Color(0xFF2C4A60),
    onTertiaryContainer = Color(0xFFCDE5FF),
    background = M3DarkSurface,
    onBackground = Color(0xFFE0E3E8),
    surface = M3DarkSurfaceContainer,
    onSurface = Color(0xFFE0E3E8),
    surfaceVariant = M3DarkSurfaceContainerHigh,
    onSurfaceVariant = Color(0xFFBFC9C6),
    outline = M3DarkOutlineVariant,
    outlineVariant = Color(0xFF2F353A)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = M3TealPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF73F8E5),
    onPrimaryContainer = Color(0xFF00201D),
    secondary = M3SecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCE8E2),
    onSecondaryContainer = Color(0xFF05201C),
    tertiary = M3TertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCDE5FF),
    onTertiaryContainer = Color(0xFF001D32),
    background = M3LightSurface,
    onBackground = Color(0xFF181C20),
    surface = M3LightSurfaceContainerLowest,
    onSurface = Color(0xFF181C20),
    surfaceVariant = M3LightSurfaceContainer,
    onSurfaceVariant = Color(0xFF3F4947),
    outline = M3LightOutlineVariant,
    outlineVariant = Color(0xFFE0E4E8)
  )

@Composable
fun GenesisKitchenTheme(
  themeMode: AppThemeMode = AppThemeMode.SYSTEM,
  dynamicColor: Boolean = true, // Android 16/17 Material You Dynamic Color supported!
  content: @Composable () -> Unit,
) {
  val isDark = when (themeMode) {
    AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
  }

  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    isDark -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
