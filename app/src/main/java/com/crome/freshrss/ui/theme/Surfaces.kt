package com.crome.freshrss.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

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

/**
 * Card background: solid with a shadow, or for glass palettes translucent with
 * a hairline accent edge (shadows look muddy under see-through cards).
 */
@Composable
fun Modifier.cardSurface(shape: Shape = CardShape, elevation: Dp = 8.dp): Modifier {
    val scheme = MaterialTheme.colorScheme
    return if (LocalLook.current.glass) {
        this
            .clip(shape)
            .background(scheme.card().copy(alpha = 0.66f))
            .border(1.dp, scheme.primary.copy(alpha = 0.22f), shape)
    } else {
        this
            .shadow(elevation, shape)
            .clip(shape)
            .background(scheme.card())
    }
}

/**
 * Page background. Glass palettes get the tint with soft accent and alert glows,
 * standing in for the wallpaper the quickshell glass shows through.
 */
@Composable
fun Modifier.canvasBackground(): Modifier {
    val scheme = MaterialTheme.colorScheme
    val look = LocalLook.current
    val glow = look.palette.glow(look.dark) ?: return this.background(scheme.canvas())
    val (accent, tint) = glow
    val base = if (look.dark) tint else scheme.background
    return this
        .background(base)
        .drawBehind {
            drawRect(
                Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = if (look.dark) 0.20f else 0.14f), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.05f),
                    radius = size.maxDimension * 0.75f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(scheme.secondary.copy(alpha = if (look.dark) 0.12f else 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.95f, size.height * 0.9f),
                    radius = size.maxDimension * 0.6f,
                ),
            )
        }
}
