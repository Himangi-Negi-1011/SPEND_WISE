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

private val DarkFintechColorScheme = darkColorScheme(
  primary = EmeraldPrimary,
  onPrimary = OnEmerald,
  primaryContainer = EmeraldContainer,
  onPrimaryContainer = EmeraldLight,
  secondary = TealSecondary,
  onSecondary = Color.White,
  secondaryContainer = TealContainer,
  onSecondaryContainer = Color(0xFF99F6E4),
  tertiary = AmberWarning,
  onTertiary = Color.Black,
  tertiaryContainer = AmberContainer,
  onTertiaryContainer = Color(0xFFFDE68A),
  background = DarkBackground,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = TextSecondary,
  outline = DarkBorder,
  error = RedRisk,
  onError = Color.White,
  errorContainer = RedContainer,
  onErrorContainer = Color(0xFFFECACA),
)

private val LightFintechColorScheme = lightColorScheme(
  primary = EmeraldDark,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD1FAE5),
  onPrimaryContainer = EmeraldContainer,
  secondary = TealSecondary,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFCCFBF1),
  onSecondaryContainer = TealContainer,
  tertiary = AmberWarning,
  onTertiary = Color.White,
  background = LightBackground,
  onBackground = LightTextPrimary,
  surface = LightSurface,
  onSurface = LightTextPrimary,
  surfaceVariant = LightSurfaceCard,
  onSurfaceVariant = LightTextSecondary,
  outline = LightBorder,
  error = RedRisk,
  onError = Color.White,
)

@Composable
fun SpendWiseTheme(
  darkTheme: Boolean = true, // Default to premium dark fintech look
  dynamicColor: Boolean = false, // Keep consistent fintech branding
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkFintechColorScheme
    else -> LightFintechColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  SpendWiseTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

