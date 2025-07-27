package com.example.grabee.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Make sure you import the colors from Color.kt
// import com.example.grabee.ui.theme.GrabGreen
// import com.example.grabee.ui.theme.GrabDarkGreen
// import com.example.grabee.ui.theme.GrabLightGreen
// import com.example.grabee.ui.theme.GrabWhite
// import com.example.grabee.ui.theme.GrabTextDark

private val DarkColorScheme = darkColorScheme(
    primary = GrabGreen,
    onPrimary = GrabWhite,
    secondary = GrabDarkGreen,
    onSecondary = GrabWhite,
    background = GrabLightGreen,
    onBackground = GrabTextDark,
    surface = GrabWhite,
    onSurface = GrabTextDark
)

private val LightColorScheme = lightColorScheme(
    primary = GrabGreen,
    onPrimary = GrabWhite,
    secondary = GrabDarkGreen,
    onSecondary = GrabWhite,
    background = GrabLightGreen,
    onBackground = GrabTextDark,
    surface = GrabWhite,
    onSurface = GrabTextDark
)

@Composable
fun GraBeeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
