package com.billcraft.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BillCraftBlue = Color(0xFF1565C0)
private val BillCraftLightBlue = Color(0xFF1E88E5)
private val BillCraftAccent = Color(0xFF0288D1)
private val BillCraftGreen = Color(0xFF2E7D32)
private val BillCraftError = Color(0xFFD32F2F)

private val LightColorScheme = lightColorScheme(
    primary = BillCraftBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = BillCraftAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDE5FF),
    tertiary = BillCraftGreen,
    background = Color(0xFFF8FAFB),
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F4F8),
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E),
    error = BillCraftError
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF003355),
    tertiary = Color(0xFF81C784),
    background = Color(0xFF1A1C1E),
    surface = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6),
    error = Color(0xFFFFB4AB)
)

@Composable
fun BillCraftTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
