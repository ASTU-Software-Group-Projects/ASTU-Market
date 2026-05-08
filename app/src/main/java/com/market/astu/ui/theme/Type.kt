package com.market.astu.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val EditorialFontFamily = FontFamily.Serif
private val InterfaceFontFamily = FontFamily.SansSerif

private fun typeStyle(
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    fontSize: Int,
    lineHeight: Int,
    letterSpacing: Double = 0.0
) = TextStyle(
    fontFamily = fontFamily,
    fontWeight = fontWeight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

val Typography = Typography(
    displayLarge = typeStyle(EditorialFontFamily, FontWeight.Bold, 42, 48, -0.6),
    displayMedium = typeStyle(EditorialFontFamily, FontWeight.Bold, 34, 40, -0.4),
    headlineLarge = typeStyle(EditorialFontFamily, FontWeight.SemiBold, 30, 36, -0.3),
    headlineMedium = typeStyle(EditorialFontFamily, FontWeight.SemiBold, 26, 32, -0.2),
    headlineSmall = typeStyle(EditorialFontFamily, FontWeight.SemiBold, 22, 28, -0.1),
    titleLarge = typeStyle(InterfaceFontFamily, FontWeight.SemiBold, 20, 26),
    titleMedium = typeStyle(InterfaceFontFamily, FontWeight.SemiBold, 17, 22),
    titleSmall = typeStyle(InterfaceFontFamily, FontWeight.Medium, 15, 20),
    bodyLarge = typeStyle(InterfaceFontFamily, FontWeight.Normal, 16, 24, 0.1),
    bodyMedium = typeStyle(InterfaceFontFamily, FontWeight.Normal, 14, 21, 0.1),
    bodySmall = typeStyle(InterfaceFontFamily, FontWeight.Normal, 12, 18, 0.15),
    labelLarge = typeStyle(InterfaceFontFamily, FontWeight.SemiBold, 13, 18, 0.2),
    labelMedium = typeStyle(InterfaceFontFamily, FontWeight.Medium, 12, 16, 0.25),
    labelSmall = typeStyle(InterfaceFontFamily, FontWeight.Medium, 11, 14, 0.3)
)
