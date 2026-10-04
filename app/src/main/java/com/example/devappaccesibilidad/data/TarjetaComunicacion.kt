package com.example.devappaccesibilidad.data

import java.util.UUID

/**
 * Modelo de datos que representa una tarjeta o frase rápida de comunicación
 * diseñada para personas con discapacidad sensorial auditiva.
 *
 * Facilita que el usuario exprese mensajes cotidianos de forma inmediata
 * tanto visualmente (modo pantalla completa) como por voz (Text-to-Speech).
 */
data class TarjetaComunicacion(
    val id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val mensaje: String,
    val categoria: String, // "Emergencia", "Salud", "Transporte", "Compras", "General"
    val esFavorita: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis()
)
