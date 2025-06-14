package com.cubex.contactsapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import com.cubex.contactsapp.R


val RedditSans = FontFamily(
    Font(R.font.reddit_sans_regular, FontWeight.Normal),
    Font(R.font.reddit_sans_semibold, FontWeight.Medium),
    Font(R.font.reddit_sans_bold, FontWeight.Bold)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = RedditSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = RedditSans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    labelSmall = TextStyle(
        fontFamily = RedditSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
)