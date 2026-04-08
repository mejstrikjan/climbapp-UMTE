package cz.climb.semester.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Clay,
    secondary = Moss,
    tertiary = Slate,
    background = Stone,
    surface = Sand,
    surfaceVariant = SandDark,
    primaryContainer = ClaySoft,
    secondaryContainer = MossSoft,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = MutedInk,
)

private val DarkColors = darkColorScheme(
    primary = ClaySoft,
    secondary = Moss,
    tertiary = Slate,
    background = StoneDark,
    surface = Color(0xFF2B2522),
    surfaceVariant = Color(0xFF362F2B),
    primaryContainer = ClayDeep,
    secondaryContainer = Color(0xFF334731),
    onBackground = Color(0xFFF5EDE4),
    onSurface = Color(0xFFF5EDE4),
    onSurfaceVariant = Color(0xFFD1C3B6),
)

@Composable
fun SemesterClimbTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
