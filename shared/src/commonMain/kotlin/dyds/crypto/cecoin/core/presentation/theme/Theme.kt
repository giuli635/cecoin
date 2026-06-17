package dyds.crypto.cecoin.core.presentation.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

object NordColors {
    val polarNight1 = Color(0xFF2E3440)
    val polarNight2 = Color(0xFF3B4252)
    val polarNight3 = Color(0xFF434C5E)
    val polarNight4 = Color(0xFF4C566A)

    val snowStorm1 = Color(0xFFD8DEE9)
    val snowStorm2 = Color(0xFFE5E9F0)
    val snowStorm3 = Color(0xFFECEFF4)

    val frost1 = Color(0xFF8FBCBB)
    val frost2 = Color(0xFF88C0D0)
    val frost3 = Color(0xFF81A1C1)
    val frost4 = Color(0xFF5E81AC)

    val auroraRed = Color(0xFFBF616A)
    val auroraOrange = Color(0xFFD08770)
    val auroraYellow = Color(0xFFEBCB8B)
    val auroraGreen = Color(0xFFA3BE8C)
    val auroraPurple = Color(0xFFB48EAD)
}

private val NordColorScheme = darkColorScheme(
    primary = NordColors.frost2,
    onPrimary = NordColors.polarNight1,
    primaryContainer = NordColors.frost2.copy(alpha = 0.2f),
    onPrimaryContainer = NordColors.snowStorm3,
    secondary = NordColors.frost3,
    onSecondary = NordColors.polarNight1,
    secondaryContainer = NordColors.frost3.copy(alpha = 0.2f),
    onSecondaryContainer = NordColors.snowStorm3,
    tertiary = NordColors.frost4,
    onTertiary = NordColors.polarNight1,
    background = NordColors.polarNight1,
    onBackground = NordColors.snowStorm3,
    surface = NordColors.polarNight2,
    onSurface = NordColors.snowStorm2,
    surfaceVariant = NordColors.polarNight3,
    onSurfaceVariant = NordColors.snowStorm1,
    outline = NordColors.polarNight4,
    outlineVariant = NordColors.polarNight4,
    error = NordColors.auroraRed,
    onError = NordColors.snowStorm3,
    errorContainer = NordColors.auroraRed.copy(alpha = 0.2f),
    onErrorContainer = NordColors.auroraRed,
)

@Composable
fun NordTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NordColorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content,
        )
    }
}
