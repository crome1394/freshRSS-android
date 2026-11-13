package com.crome.freshrss.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Color themes (Settings → Appearance). Each has a light and a dark variant;
 * Light / Dark / System picks between them. [DYNAMIC] uses the wallpaper
 * colors on Android 12+ and falls back to [TOKYO_NIGHT] before that.
 */
enum class AppPalette(
    val storageValue: String,
    val label: String,
    /** Liquid look: accent glow on the canvas, translucent cards with a hairline edge. */
    val glass: Boolean = false,
) {
    // From the quickshell bar (Settings → Appearance → Theme), adapted for a phone.
    LIQUID_AMBER("liquid_amber", "Liquid amber", glass = true),
    LIQUID_GLASS("liquid_glass", "Liquid glass", glass = true),
    LIQUID_ICE("liquid_ice", "Liquid ice", glass = true),
    INK("ink", "Ink", glass = true),
    SOLID_DARK("solid_dark", "Solid dark"),
    DEEP_OCEAN("deep_ocean", "Deep ocean", glass = true),
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

/*
 * Quickshell presets are dark glass over the wallpaper: a tint, an accent, an
 * alert color, and sometimes their own text colors. On a phone the tint
 * becomes the dark base, cards are lifted a step toward the accent, and the
 * light variant is paper washed with the accent, the accent darkened until
 * it reads on that paper.
 */
private fun mix(a: Color, b: Color, t: Float): Color = androidx.compose.ui.graphics.lerp(a, b, t)

private fun readableOn(color: Color, bg: Color, darken: Boolean): Color {
    var c = color
    var i = 0
    while (contrastRatio(c, bg) < 4.5f && i < 20) {
        c = mix(c, if (darken) Color.Black else Color.White, 0.08f)
        i++
    }
    return c
}

private fun Color.argbLong(): Long = (0xFF000000L or (toArgb().toLong() and 0xFFFFFFL))

private fun quickshell(
    accent: Long,
    alert: Long,
    tint: Long,
    dark: Boolean,
    fg: Long? = null,
    muted: Long? = null,
): Swatch {
    val a = Color(accent)
    val al = Color(alert)
    val t = Color(tint)
    return if (dark) {
        val bg = t
        val surface = mix(t, Color.White, 0.05f).let { mix(it, a, 0.03f) }
        val variant = mix(t, Color.White, 0.11f).let { mix(it, a, 0.06f) }
        val on = fg?.let { Color(it) } ?: mix(Color(0xFFF2F2F4), a, 0.06f)
        val mu = muted?.let { Color(it) } ?: readableOn(mix(on, surface, 0.38f), surface, darken = false)
        Swatch(
            background = bg.argbLong(), surface = surface.argbLong(), surfaceVariant = variant.argbLong(),
            onSurface = on.argbLong(), muted = mu.argbLong(),
            primary = readableOn(a, surface, darken = false).argbLong(), onPrimary = t.argbLong(),
            primaryContainer = mix(variant, a, 0.28f).argbLong(), onPrimaryContainer = on.argbLong(),
            secondary = al.argbLong(), error = al.argbLong(),
            outline = mix(variant, on, 0.25f).argbLong(),
        )
    } else {
        val surface = mix(Color.White, a, 0.025f)
        val bg = mix(Color.White, a, 0.07f)
        val variant = mix(Color.White, a, 0.14f)
        val on = mix(t, Color.Black, 0.2f).let { if (contrastRatio(it, surface) < 7f) Color(0xFF15171C) else it }
        val mu = readableOn(mix(on, surface, 0.4f), surface, darken = true)
        val primary = readableOn(a, surface, darken = true)
        Swatch(
            background = bg.argbLong(), surface = surface.argbLong(), surfaceVariant = variant.argbLong(),
            onSurface = on.argbLong(), muted = mu.argbLong(),
            primary = primary.argbLong(), onPrimary = 0xFFFFFFFF,
            primaryContainer = mix(Color.White, a, 0.24f).argbLong(), onPrimaryContainer = on.argbLong(),
            secondary = readableOn(al, surface, darken = true).argbLong(),
            error = readableOn(al, surface, darken = true).argbLong(),
            outline = mix(variant, on, 0.3f).argbLong(),
        )
    }
}

private fun AppPalette.quickshellSwatch(dark: Boolean): Swatch? = when (this) {
    AppPalette.LIQUID_AMBER -> quickshell(0xFFFBBF24, 0xFFFB7185, 0xFF17120A, dark)
    AppPalette.LIQUID_GLASS -> quickshell(0xFF00F0E0, 0xFFFF3D8A, 0xFF0A0F1A, dark)
    AppPalette.LIQUID_ICE -> quickshell(0xFF7DD3FC, 0xFFA5B4FC, 0xFF0A121F, dark)
    AppPalette.INK -> quickshell(0xFFE4C58A, 0xFFD4A0A0, 0xFF0D0B0A, dark)
    AppPalette.SOLID_DARK -> quickshell(0xFF00C4F5, 0xFFE85D6F, 0xFF000000, dark, fg = 0xFFECECEE, muted = 0xFFB0B0B2)
        .let { if (dark) it.copy(surface = 0xFF121214, surfaceVariant = 0xFF1E1E21) else it }
    AppPalette.DEEP_OCEAN -> quickshell(0xFF3DB8C9, 0xFFE06C75, 0xFF0B1219, dark, fg = 0xFFE6F0FA, muted = 0xFF8FA3B8)
    else -> null
}

/** Accent glow for glass palettes: (accent, tint) to paint the canvas with. */
fun AppPalette.glow(dark: Boolean): Pair<Color, Color>? {
    if (!glass) return null
    val sw = quickshellSwatch(dark) ?: return null
    return Color(sw.primary) to Color(sw.background)
}

/** Color scheme for a fixed palette ([AppPalette.DYNAMIC] is resolved by the caller). */
fun AppPalette.scheme(dark: Boolean): ColorScheme {
    quickshellSwatch(dark)?.let { return if (dark) it.dark() else it.light() }
    val (d, l) = when (this) {
        AppPalette.TOKYO_NIGHT, AppPalette.DYNAMIC -> TokyoNightDark to TokyoNightLight
        AppPalette.CATPPUCCIN -> CatppuccinDark to CatppuccinLight
        AppPalette.NORD -> NordDark to NordLight
        AppPalette.GRUVBOX -> GruvboxDark to GruvboxLight
        AppPalette.ROSE_PINE -> RosePineDark to RosePineLight
        AppPalette.SOLARIZED -> SolarizedDark to SolarizedLight
        else -> TokyoNightDark to TokyoNightLight
    }
    return if (dark) d.dark() else l.light()
}
