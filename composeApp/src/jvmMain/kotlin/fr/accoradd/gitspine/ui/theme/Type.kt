package fr.accoradd.gitspine.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import gitspine.composeapp.generated.resources.*
import org.jetbrains.compose.resources.Font

val InterFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.Inter_Regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.Inter_Italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.Inter_Thin, FontWeight.Thin, FontStyle.Normal),
        Font(Res.font.Inter_ThinItalic, FontWeight.Thin, FontStyle.Italic),
        Font(Res.font.Inter_ExtraLight, FontWeight.ExtraLight, FontStyle.Normal),
        Font(Res.font.Inter_ExtraLightItalic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(Res.font.Inter_Light, FontWeight.Light, FontStyle.Normal),
        Font(Res.font.Inter_LightItalic, FontWeight.Light, FontStyle.Italic),
        Font(Res.font.Inter_Medium, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.Inter_MediumItalic, FontWeight.Medium, FontStyle.Italic),
        Font(Res.font.Inter_SemiBold, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.Inter_SemiBoldItalic, FontWeight.SemiBold, FontStyle.Italic),
        Font(Res.font.Inter_Bold, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.Inter_BoldItalic, FontWeight.Bold, FontStyle.Italic),
        Font(Res.font.Inter_ExtraBold, FontWeight.ExtraBold, FontStyle.Normal),
        Font(Res.font.Inter_ExtraBoldItalic, FontWeight.ExtraBold, FontStyle.Italic),
        Font(Res.font.Inter_Black, FontWeight.Black, FontStyle.Normal),
        Font(Res.font.Inter_BlackItalic, FontWeight.Black, FontStyle.Italic)
    )

val JetBrainsMonoFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.JetBrainsMono_Regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.JetBrainsMono_Italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.JetBrainsMono_Thin, FontWeight.Thin, FontStyle.Normal),
        Font(Res.font.JetBrainsMono_ThinItalic, FontWeight.Thin, FontStyle.Italic),
        Font(Res.font.JetBrainsMono_ExtraLight, FontWeight.ExtraLight, FontStyle.Normal),
        Font(Res.font.JetBrainsMono_ExtraLightItalic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(Res.font.JetBrainsMono_Light, FontWeight.Light, FontStyle.Normal),
        Font(Res.font.JetBrainsMono_LightItalic, FontWeight.Light, FontStyle.Italic),
        Font(Res.font.JetBrainsMono_Medium, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.JetBrainsMono_MediumItalic, FontWeight.Medium, FontStyle.Italic),
        Font(Res.font.JetBrainsMono_SemiBold, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.JetBrainsMono_SemiBoldItalic, FontWeight.SemiBold, FontStyle.Italic),
        Font(Res.font.JetBrainsMono_Bold, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.JetBrainsMono_BoldItalic, FontWeight.Bold, FontStyle.Italic),
        Font(Res.font.JetBrainsMono_ExtraBold, FontWeight.ExtraBold, FontStyle.Normal),
        Font(Res.font.JetBrainsMono_ExtraBoldItalic, FontWeight.ExtraBold, FontStyle.Italic)
    )

val AppTypography: Typography
    @Composable
    get() = Typography(
        displayLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp),
        displayMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp),
        displaySmall = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
        headlineLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp),
        headlineMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp),
        headlineSmall = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp),
        titleLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 22.sp),
        titleMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp),
        titleSmall = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp),
        bodyLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
        bodyMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
        bodySmall = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
        labelLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp),
        labelMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp),
        labelSmall = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    )
