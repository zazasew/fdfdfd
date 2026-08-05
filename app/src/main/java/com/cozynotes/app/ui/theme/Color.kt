package com.cozynotes.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

// Soft, muted palette — warm and cozy without being loud. These are
// deliberately lower-saturation than a "kid app" palette: they read as
// gentle tints, not solid blocks of bright color.
val Lavender = Color(0xFFA891D6)
val LavenderDeep = Color(0xFF6C4FA6)
val PastelPink = Color(0xFFE3AEBD)
val PastelPeach = Color(0xFFE8BE93)
val PastelMint = Color(0xFFA9CBAE)
val PastelYellow = Color(0xFFE0CE8E)
val PastelBlue = Color(0xFFA6C2DE)

val CreamBackground = Color(0xFFFAF6F0)
val CreamSurface = Color(0xFFFFFFFF)
val WarmTextPrimary = Color(0xFF4A4458)
val WarmTextSecondary = Color(0xFF8B8398)

// Dark theme — soft charcoal, never pure black, keeps the cozy feel.
val DarkBackground = Color(0xFF1E1B26)
val DarkSurface = Color(0xFF2A2633)
val DarkSurfaceVariant = Color(0xFF352F42)
val DarkTextPrimary = Color(0xFFF1EDF7)
val DarkTextSecondary = Color(0xFFB7AFC6)

// Note accent colors offered in the color picker. These are used as subtle
// tints blended into the card/editor surface (see noteAccentBackground
// below) — never as a full-strength background — so body text always stays
// legible no matter which accent or theme is active.
val NoteColorLavender = Lavender
val NoteColorPink = PastelPink
val NoteColorPeach = PastelPeach
val NoteColorMint = PastelMint
val NoteColorYellow = PastelYellow
val NoteColorBlue = PastelBlue

val noteColorPalette = listOf(
    NoteColorLavender,
    NoteColorPink,
    NoteColorPeach,
    NoteColorMint,
    NoteColorYellow,
    NoteColorBlue
)

// --- Title / body text colors -------------------------------------------
//
// Title text: a rich "cappuccino" espresso-brown.
// Body text: a softer "papyrus" parchment-brown from the same warm family,
// so the two always read as a matched, coordinated pair regardless of which
// note accent color or theme is active.
val CappuccinoLight = Color(0xFF6B4423)
val CappuccinoDark = Color(0xFFE3BD93)
val PapyrusLight = Color(0xFF6E5A44)
val PapyrusDark = Color(0xFFD9C9AC)

/** True when the currently-applied Material color scheme is the dark variant. */
@Composable
fun isAppInDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

/** Warm espresso-brown used for note titles, in both editor and note cards. */
@Composable
fun cappuccinoTitleColor(): Color = if (isAppInDarkTheme()) CappuccinoDark else CappuccinoLight

/** Soft parchment-brown used for note body text, coordinated with the title color. */
@Composable
fun papyrusBodyColor(): Color = if (isAppInDarkTheme()) PapyrusDark else PapyrusLight

/**
 * Blends a note's chosen accent color gently into the current surface color
 * instead of using it at full strength. This is what actually fixes
 * "background is too colorful / text is unreadable": no matter how bright
 * the picked accent is, the result stays close to the neutral surface color
 * (and gets even more muted in dark mode), so title/body text always has
 * enough contrast on top of it.
 */
@Composable
fun noteAccentBackground(accent: Color?): Color {
    val surface = MaterialTheme.colorScheme.surface
    if (accent == null) return surface
    val strength = if (isAppInDarkTheme()) 0.14f else 0.20f
    return lerp(surface, accent, strength)
}

/** Darkens a pastel note color into a readable accent-strip tint. */
fun Color.titleTint(): Color {
    val r = red * 0.45f
    val g = green * 0.45f
    val b = blue * 0.45f
    return Color(r, g, b, 1f)
}
