package com.crome.freshrss.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import android.os.Build

/** User-selectable appearance (Settings → Appearance). */
enum class AppThemeMode(val storageValue: String, val label: String) {
    SYSTEM("system", "System"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark");

    companion object {
        fun fromStorage(value: String?): AppThemeMode =
            entries.firstOrNull { it.storageValue == value?.lowercase() } ?: SYSTEM
    }
}

/** The palette in use, for looks a ColorScheme can't carry (glass). */
data class Look(val palette: AppPalette, val dark: Boolean) {
    val glass: Boolean get() = palette.glass
}

val LocalLook = staticCompositionLocalOf { Look(AppPalette.TOKYO_NIGHT, dark = true) }

@Composable
fun FreshRssTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    palette: AppPalette = AppPalette.TOKYO_NIGHT,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val scheme = if (palette == AppPalette.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        palette.scheme(darkTheme)
    }
    // Status and navigation bar icons follow the app's theme, not the phone's.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    CompositionLocalProvider(LocalLook provides Look(palette, darkTheme)) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
