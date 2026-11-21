
package com.example.expensemanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object ExmanColors {
    val Emerald = Color(0xFF087F69)
    val DeepEmerald = Color(0xFF075746)
    val Navy = Color(0xFF101D2D)
    val Canvas = Color(0xFFF6F8FA)
    val Income = Color(0xFF129E77)
    val Expense = Color(0xFFE66B73)
}

private val LightPalette = lightColorScheme(
    primary = ExmanColors.Emerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7F4E9),
    onPrimaryContainer = ExmanColors.DeepEmerald,
    secondary = ExmanColors.Navy,
    background = ExmanColors.Canvas,
    onBackground = ExmanColors.Navy,
    surface = Color.White,
    onSurface = ExmanColors.Navy,
    surfaceVariant = Color(0xFFEAF0EE),
    onSurfaceVariant = Color(0xFF687A77),
    error = ExmanColors.Expense
)

private val DarkPalette = darkColorScheme(
    primary = Color(0xFF55DABB),
    onPrimary = Color(0xFF00382D),
    primaryContainer = ExmanColors.DeepEmerald,
    onPrimaryContainer = Color(0xFFB5EFDE),
    secondary = Color(0xFFB9C8D0),
    background = Color(0xFF0D151A),
    onBackground = Color(0xFFF0F5F4),
    surface = Color(0xFF172329),
    onSurface = Color(0xFFF0F5F4),
    surfaceVariant = Color(0xFF26343A),
    onSurfaceVariant = Color(0xFFB1C0BE),
    error = Color(0xFFFF8C91)
)

@Composable
fun ExmanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkPalette else LightPalette,
        typography = Typography(),
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(24.dp)
        ),
        content = content
    )
}
