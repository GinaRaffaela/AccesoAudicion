package com.example.devappaccesibilidad.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaColores = lightColorScheme(
    primary = Indigo700,
    onPrimary = Blanco,
    primaryContainer = Indigo200,
    onPrimaryContainer = Negro,
    secondary = Teal600,
    onSecondary = Blanco,
    secondaryContainer = Teal200,
    onSecondaryContainer = Negro,
    background = GrisClaro,
    onBackground = Negro,
    surface = Blanco,
    onSurface = Negro,
    surfaceVariant = GrisClaro,
    onSurfaceVariant = GrisOscuro,
    error = Rojo,
    onError = Blanco
)

@Composable
fun DevAppAccesibilidadTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaColores,
        typography = Tipografia,
        content = content
    )
}