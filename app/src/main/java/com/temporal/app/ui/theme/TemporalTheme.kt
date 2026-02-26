package com.temporal.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Dark = darkColorScheme(
  primary = Color(0xFF7AA7FF),
  secondary = Color(0xFF7CDAFF),
  tertiary = Color(0xFFB69CFF),
  background = Color(0xFF0B0F16),
  surface = Color(0xFF0F1622),
  onBackground = Color(0xFFE7ECF5),
  onSurface = Color(0xFFE7ECF5)
)

@Composable
fun TemporalTheme(content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = Dark, typography = Typography(), content = content)
}
