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

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF042111),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = Color(0xFFB7E4C7),
    secondary = DarkSecondary,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = DarkTertiary,
    onTertiary = Color(0xFF4A1800),
    tertiaryContainer = Color(0xFF7C2D12),
    onTertiaryContainer = Color(0xFFFFDBCF),
    background = DarkBackground,
    onBackground = Color(0xFFECE5DD),
    surface = DarkSurface,
    onSurface = Color(0xFFECE5DD),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC7C0B7),
    outline = Color(0xFF5A665F),
    error = ExpiringRed,
    errorContainer = Color(0xFF7F1D1D)
)

private val LightColorScheme = lightColorScheme(
    primary = SageGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = SageContainer,
    onPrimaryContainer = OnSageContainer,
    secondary = ToastedAmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = OnAmberContainer,
    tertiary = PaprikaTertiary,
    onTertiary = Color.White,
    tertiaryContainer = PaprikaContainer,
    onTertiaryContainer = OnPaprikaContainer,
    background = CreamBackground,
    onBackground = OnWarmSurface,
    surface = WarmSurface,
    onSurface = OnWarmSurface,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = OnWarmSurfaceVariant,
    outline = OutlineBorder,
    error = ExpiringRed,
    errorContainer = ExpiringRedContainer
)

@Composable
fun MealyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional culinary branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
