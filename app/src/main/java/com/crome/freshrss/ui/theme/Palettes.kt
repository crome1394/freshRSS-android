package com.crome.freshrss.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Color themes (Settings → Appearance). Each has a light and a dark variant;
 * Light / Dark / System picks between them. [DYNAMIC] uses the wallpaper
 * colors on Android 12+ and falls back to [TOKYO_NIGHT] before that.
 */
enum class AppPalette(val storageValue: String, val label: String) {
    TOKYO_NIGHT("tokyo_night", "Tokyo Night"),
    CATPPUCCIN("catppuccin", "Catppuccin"),
    NORD("nord", "Nord"),
    GRUVBOX("gruvbox", "Gruvbox"),
    ROSE_PINE("rose_pine", "Rosé Pine"),
    SOLARIZED("solarized", "Solarized"),
    DYNAMIC("dynamic", "Wallpaper");

    companion object {
        fun fromStorage(value: String?): AppPalette =
            entries.firstOrNull { it.storageValue == value?.lowercase() } ?: TOKYO_NIGHT
    }
}

/** The few colors a palette defines; the rest of the scheme derives from them. */
private data class Swatch(
    val background: Long,
    val surface: Long,
    val surfaceVariant: Long,
    val onSurface: Long,
    val muted: Long,
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val secondary: Long,
    val error: Long,
    val outline: Long,
)

private fun Swatch.dark(): ColorScheme = darkColorScheme(
    primary = Color(primary),
    onPrimary = Color(onPrimary),
    primaryContainer = Color(primaryContainer),
    onPrimaryContainer = Color(onPrimaryContainer),
    secondary = Color(secondary),
    onSecondary = Color(background),
    secondaryContainer = Color(surfaceVariant),
    onSecondaryContainer = Color(onSurface),
    background = Color(background),
    onBackground = Color(onSurface),
    surface = Color(surface),
    onSurface = Color(onSurface),
    surfaceVariant = Color(surfaceVariant),
    onSurfaceVariant = Color(muted),
    surfaceContainerLowest = Color(background),
    surfaceContainerLow = Color(surface),
    surfaceContainer = Color(surface),
    surfaceContainerHigh = Color(surfaceVariant),
    surfaceContainerHighest = Color(surfaceVariant),
    error = Color(error),
    outline = Color(outline),
    outlineVariant = Color(surfaceVariant),
)

private fun Swatch.light(): ColorScheme = lightColorScheme(
    primary = Color(primary),
    onPrimary = Color(onPrimary),
    primaryContainer = Color(primaryContainer),
    onPrimaryContainer = Color(onPrimaryContainer),
    secondary = Color(secondary),
    onSecondary = Color(surface),
    secondaryContainer = Color(surfaceVariant),
    onSecondaryContainer = Color(onSurface),
    background = Color(background),
    onBackground = Color(onSurface),
    surface = Color(surface),
    onSurface = Color(onSurface),
    surfaceVariant = Color(surfaceVariant),
    onSurfaceVariant = Color(muted),
    surfaceContainerLowest = Color(surface),
    surfaceContainerLow = Color(background),
    surfaceContainer = Color(background),
    surfaceContainerHigh = Color(surfaceVariant),
    surfaceContainerHighest = Color(surfaceVariant),
    error = Color(error),
    outline = Color(outline),
    outlineVariant = Color(surfaceVariant),
)

// Muted text colors are picked to stay ≥ 4.5:1 against their surface.

private val TokyoNightDark = Swatch(
    background = 0xFF1A1B26, surface = 0xFF1F2335, surfaceVariant = 0xFF2F334D,
    onSurface = 0xFFC0CAF5, muted = 0xFF9AA5CE, primary = 0xFF7AA2F7, onPrimary = 0xFF1A1B26,
    primaryContainer = 0xFF3D59A1, onPrimaryContainer = 0xFFDDE4FF, secondary = 0xFFBB9AF7,
    error = 0xFFF7768E, outline = 0xFF565F89,
)
private val TokyoNightLight = Swatch( // "Day"
    background = 0xFFE1E2E7, surface = 0xFFF2F3F7, surfaceVariant = 0xFFD5D6DB,
    onSurface = 0xFF3760BF, muted = 0xFF4E5579, primary = 0xFF2E7DE9, onPrimary = 0xFFFFFFFF,
    primaryContainer = 0xFFC8D7F5, onPrimaryContainer = 0xFF1F3A73, secondary = 0xFF9854F1,
    error = 0xFFC64343, outline = 0xFF8990B3,
)

private val CatppuccinDark = Swatch( // Mocha
    background = 0xFF1E1E2E, surface = 0xFF24243A, surfaceVariant = 0xFF313244,
    onSurface = 0xFFCDD6F4, muted = 0xFFA6ADC8, primary = 0xFFCBA6F7, onPrimary = 0xFF1E1E2E,
    primaryContainer = 0xFF45475A, onPrimaryContainer = 0xFFF5E0DC, secondary = 0xFF89B4FA,
    error = 0xFFF38BA8, outline = 0xFF6C7086,
)
private val CatppuccinLight = Swatch( // Latte
    background = 0xFFEFF1F5, surface = 0xFFFFFFFF, surfaceVariant = 0xFFDCE0E8,
    onSurface = 0xFF4C4F69, muted = 0xFF5C5F77, primary = 0xFF8839EF, onPrimary = 0xFFFFFFFF,
    primaryContainer = 0xFFE6D8FB, onPrimaryContainer = 0xFF3B1470, secondary = 0xFF1E66F5,
    error = 0xFFD20F39, outline = 0xFF9CA0B0,
)

private val NordDark = Swatch( // Polar Night + Frost
    background = 0xFF2E3440, surface = 0xFF3B4252, surfaceVariant = 0xFF434C5E,
    onSurface = 0xFFECEFF4, muted = 0xFFD8DEE9, primary = 0xFF88C0D0, onPrimary = 0xFF2E3440,
    primaryContainer = 0xFF5E81AC, onPrimaryContainer = 0xFFECEFF4, secondary = 0xFFB48EAD,
    error = 0xFFBF616A, outline = 0xFF4C566A,
)
private val NordLight = Swatch( // Snow Storm + Frost
    background = 0xFFE5E9F0, surface = 0xFFECEFF4, surfaceVariant = 0xFFD8DEE9,
    onSurface = 0xFF2E3440, muted = 0xFF4C566A, primary = 0xFF5E81AC, onPrimary = 0xFFFFFFFF,
    primaryContainer = 0xFFCAD7E8, onPrimaryContainer = 0xFF2E3440, secondary = 0xFF8C6A87,
    error = 0xFFA5424B, outline = 0xFF9AA4B6,
)

private val GruvboxDark = Swatch(
    background = 0xFF282828, surface = 0xFF32302F, surfaceVariant = 0xFF3C3836,
    onSurface = 0xFFEBDBB2, muted = 0xFFBDAE93, primary = 0xFFFABD2F, onPrimary = 0xFF282828,
    primaryContainer = 0xFF504945, onPrimaryContainer = 0xFFFBF1C7, secondary = 0xFF8EC07C,
    error = 0xFFFB4934, outline = 0xFF665C54,
)
private val GruvboxLight = Swatch(
    background = 0xFFF2E5BC, surface = 0xFFFBF1C7, surfaceVariant = 0xFFEBDBB2,
    onSurface = 0xFF3C3836, muted = 0xFF665C54, primary = 0xFFAF3A03, onPrimary = 0xFFFBF1C7,
    primaryContainer = 0xFFF4D7A8, onPrimaryContainer = 0xFF3C1E05, secondary = 0xFF427B58,
    error = 0xFF9D0006, outline = 0xFFA89984,
)

private val RosePineDark = Swatch( // Main
    background = 0xFF191724, surface = 0xFF1F1D2E, surfaceVariant = 0xFF26233A,
    onSurface = 0xFFE0DEF4, muted = 0xFF908CAA, primary = 0xFFEBBCBA, onPrimary = 0xFF191724,
    primaryContainer = 0xFF403D52, onPrimaryContainer = 0xFFE0DEF4, secondary = 0xFFC4A7E7,
    error = 0xFFEB6F92, outline = 0xFF6E6A86,
)
private val RosePineLight = Swatch( // Dawn
    background = 0xFFFAF4ED, surface = 0xFFFFFAF3, surfaceVariant = 0xFFF2E9E1,
    onSurface = 0xFF575279, muted = 0xFF6E6A86, primary = 0xFFB4637A, onPrimary = 0xFFFFFAF3,
    primaryContainer = 0xFFF0D9DD, onPrimaryContainer = 0xFF4A2030, secondary = 0xFF286983,
    error = 0xFFB4637A, outline = 0xFF9893A5,
)

private val SolarizedDark = Swatch(
    background = 0xFF002B36, surface = 0xFF073642, surfaceVariant = 0xFF0E4452,
    onSurface = 0xFFEEE8D5, muted = 0xFF93A1A1, primary = 0xFF268BD2, onPrimary = 0xFFFDF6E3,
    primaryContainer = 0xFF0B4F6C, onPrimaryContainer = 0xFFEEE8D5, secondary = 0xFF2AA198,
    error = 0xFFDC322F, outline = 0xFF586E75,
)
private val SolarizedLight = Swatch(
    background = 0xFFEEE8D5, surface = 0xFFFDF6E3, surfaceVariant = 0xFFE4DDC8,
    onSurface = 0xFF073642, muted = 0xFF586E75, primary = 0xFF1A6FA8, onPrimary = 0xFFFDF6E3,
    primaryContainer = 0xFFD3E4EE, onPrimaryContainer = 0xFF073642, secondary = 0xFF1E7D76,
    error = 0xFFC0262A, outline = 0xFF93A1A1,
)

/** Color scheme for a fixed palette ([AppPalette.DYNAMIC] is resolved by the caller). */
fun AppPalette.scheme(dark: Boolean): ColorScheme {
    val (d, l) = when (this) {
        AppPalette.TOKYO_NIGHT, AppPalette.DYNAMIC -> TokyoNightDark to TokyoNightLight
        AppPalette.CATPPUCCIN -> CatppuccinDark to CatppuccinLight
        AppPalette.NORD -> NordDark to NordLight
        AppPalette.GRUVBOX -> GruvboxDark to GruvboxLight
        AppPalette.ROSE_PINE -> RosePineDark to RosePineLight
        AppPalette.SOLARIZED -> SolarizedDark to SolarizedLight
    }
    return if (dark) d.dark() else l.light()
}
