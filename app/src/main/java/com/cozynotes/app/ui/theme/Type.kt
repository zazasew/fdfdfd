package com.cozynotes.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cozynotes.app.R

/**
 * Title font — Poppins. A clean, warm geometric sans used for note titles,
 * the app's own title bar, and section headings. Pairs with the serif body
 * font below the way a good cappuccino (rich, rounded) sits next to a
 * papyrus page (soft, readable) — related but distinct roles.
 */
val TitleFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

/**
 * Body font — PT Serif. A gentle print-style serif for the note body text,
 * chosen for that "papyrus / journal page" reading feel instead of a plain
 * default system sans.
 */
val BodyFontFamily = FontFamily(
    Font(R.font.pt_serif_regular, FontWeight.Normal),
    Font(R.font.pt_serif_bold, FontWeight.Bold),
    Font(R.font.pt_serif_italic, FontWeight.Normal, androidx.compose.ui.text.font.FontStyle.Italic)
)

/**
 * Builds the app's Typography, scaled by the user's Settings > Font Size
 * choice ([fontScaleFor]). Title-ish styles use [TitleFontFamily]; body/label
 * styles use [BodyFontFamily].
 */
fun cozyTypography(scale: Float = 1f): Typography = Typography(
    displayLarge = TextStyle(fontFamily = TitleFontFamily, fontWeight = FontWeight.Bold, fontSize = (34 * scale).sp, lineHeight = (42 * scale).sp),
    headlineLarge = TextStyle(fontFamily = TitleFontFamily, fontWeight = FontWeight.Bold, fontSize = (28 * scale).sp, lineHeight = (34 * scale).sp),
    headlineMedium = TextStyle(fontFamily = TitleFontFamily, fontWeight = FontWeight.SemiBold, fontSize = (24 * scale).sp, lineHeight = (30 * scale).sp),
    titleLarge = TextStyle(fontFamily = TitleFontFamily, fontWeight = FontWeight.SemiBold, fontSize = (20 * scale).sp, lineHeight = (26 * scale).sp),
    titleMedium = TextStyle(fontFamily = TitleFontFamily, fontWeight = FontWeight.Medium, fontSize = (17 * scale).sp, lineHeight = (22 * scale).sp),
    bodyLarge = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal, fontSize = (16 * scale).sp, lineHeight = (25 * scale).sp),
    bodyMedium = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal, fontSize = (14 * scale).sp, lineHeight = (21 * scale).sp),
    labelLarge = TextStyle(fontFamily = TitleFontFamily, fontWeight = FontWeight.Medium, fontSize = (14 * scale).sp),
    labelMedium = TextStyle(fontFamily = TitleFontFamily, fontWeight = FontWeight.Medium, fontSize = (12 * scale).sp),
    labelSmall = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal, fontSize = (11 * scale).sp)
)

/** Multiplier applied to base type sizes based on the user's Settings > Font Size choice. */
fun fontScaleFor(fontSize: com.cozynotes.app.data.preferences.FontSize): Float = when (fontSize) {
    com.cozynotes.app.data.preferences.FontSize.SMALL -> 0.9f
    com.cozynotes.app.data.preferences.FontSize.MEDIUM -> 1.0f
    com.cozynotes.app.data.preferences.FontSize.LARGE -> 1.15f
}
