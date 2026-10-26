package com.example.asilo360.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// PALETA FUTURISTA Y PREMIUM - ASILO 360
// ============================================================

// Colores Primarios y Tecnológicos
val DeepNightBlue = Color(0xFF101A2C)      // Azul noche profundo
val TechBlue = Color(0xFF2563EB)           // Azul tecnológico
val LuminousTurquoise = Color(0xFF21D4C2)  // Turquesa luminoso
val CyberTealDark = Color(0xFF0D9488)      // Turquesa oscuro
val SlateGrey = Color(0xFF8793A8)          // Gris azulado
val SoftWhite = Color(0xFFF7F9FC)          // Blanco suave
val PureWhite = Color(0xFFFFFFFF)

// Estados y Semántica
val SuccessGreen = Color(0xFF22C58B)       // Verde éxito
val WarningAmber = Color(0xFFF5B942)       // Ámbar advertencias
val CoralRed = Color(0xFFF06464)           // Rojo coral
val CoralRedDark = Color(0xFFDC2626)

// Gradientes Futuristas Prominentes
val TechGradientPrimary = Brush.horizontalGradient(
    colors = listOf(TechBlue, LuminousTurquoise)
)

val NightGradientDeep = Brush.verticalGradient(
    colors = listOf(DeepNightBlue, Color(0xFF18263E))
)

val GlowTurquoiseGradient = Brush.horizontalGradient(
    colors = listOf(LuminousTurquoise, Color(0xFF0EA5E9))
)

// Retrocompatibilidad con componentes existentes
val LightPastelBlue = Color(0xFFE1F5FE)
val SoftLavender = Color(0xFFD1C4E9)
val SoftLavenderDark = Color(0xFF512DA8)
val MintGreen = Color(0xFF21D4C2)
val MintGreenDark = Color(0xFF0F766E)
val MintGreenContainer = Color(0xFFCCFBF1)
val SoftYellow = Color(0xFFFFFBEB)
val CoralRedVariant = Color(0xFFFCA5A5)
val DarkGrey = Color(0xFF101A2C)
val Dark360 = Color(0xFF0B1320)
val White = Color(0xFFFFFFFF)
val SurfaceBorder = Color(0xFFE2E8F0)
val TextMuted = Color(0xFF64748B)
