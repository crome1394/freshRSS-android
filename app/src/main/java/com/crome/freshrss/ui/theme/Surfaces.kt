package com.crome.freshrss.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/*
 * Card-on-canvas look borrowed from BlurtText's inbox: a page one step lighter
 * (dark themes) or deeper (light themes) than the palette surface, solid cards
 * on it, a pill search field, and ink that stays readable on both.
 */

private fun Color.toward(other: Color, amount: Float): Color = lerp(this, other, amount)

/** Page behind the cards. */
fun ColorScheme.canvas(): Color {
    val base = surface
    return if (base.luminance() < 0.4f) base.toward(Color.White, 0.12f) else base.toward(Color.Black, 0.07f)
}

/** Solid card on [canvas]. */
fun ColorScheme.card(): Color = surface

/** Search pill, one step lighter or deeper than the page. */
fun ColorScheme.searchPill(): Color {
    val c = canvas()
    return if (c.luminance() < 0.5f) c.toward(Color.White, 0.10f) else c.toward(Color.Black, 0.05f)
}

/** Text and icons on cards and canvas; falls back to white/black if the palette ink would fade. */
fun ColorScheme.ink(): Color {
    val ink = onSurface
    if (contrastRatio(ink, card()) >= 4.5f && contrastRatio(ink, canvas()) >= 4.5f) return ink
    return if (card().luminance() < 0.4f) Color.White else Color.Black
}

fun contrastRatio(foreground: Color, background: Color): Float {
    val lighter = maxOf(foreground.luminance(), background.luminance())
    val darker = minOf(foreground.luminance(), background.luminance())
    return (lighter + 0.05f) / (darker + 0.05f)
}

val CardShape = RoundedCornerShape(20.dp)
val PillShape = RoundedCornerShape(24.dp)
