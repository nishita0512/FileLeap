package com.example.fileleap.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.fileleap.R

val clashGrotesk = FontFamily(
    Font(R.font.clashgrotesk_bold, FontWeight.Bold),
    Font(R.font.clashgrotesk_semibold, FontWeight.SemiBold),
    Font(R.font.clashgrotesk_medium, FontWeight.Medium),
    Font(R.font.clashgrotesk_regular, FontWeight.Normal),
    Font(R.font.clashgrotesk_light, FontWeight.Light),
    Font(R.font.clashgrotesk_extralight, FontWeight.ExtraLight),
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = clashGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.7.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = clashGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = clashGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodySmall = TextStyle(
        fontFamily = clashGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = clashGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = clashGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )
)