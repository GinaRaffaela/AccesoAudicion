package com.example.devappaccesibilidad.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.devappaccesibilidad.data.TarjetaComunicacion

/**
 * Diálogo modal para las operaciones de CREAR (Create) y MODIFICAR (Update)
 * de tarjetas de comunicación rápida accesibles.
 */
@Composable
fun DialogoEditarCrearTarjeta(
    tarjetaExistente: TarjetaComunicacion? = null,
    alGuardar: (TarjetaComunicacion) -> Unit,
    alDescartar: () -> Unit
) {
    var titulo by remember { mutableStateOf(tarjetaExistente?.titulo ?: "") }
    var mensaje by remember { mutableStateOf(tarjetaExistente?.mensaje ?: "") }
    var categoria by remember { mutableStateOf(tarjetaExistente?.categoria ?: "General") }
    var errorMensaje by remember { mutableStateOf("") }

    val categoriasDisponibles = listOf("Emergencia", "Salud", "Transporte", "Compras", "General")

    AlertDialog(
        onDismissRequest = alDescartar,
        title = {
            Text(
                text = if (tarjetaExistente == null) "Nueva frase de comunicación" else "Editar frase",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Categoría:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoriasDisponibles) { cat ->
                        FilterChip(
                            selected = categoria == cat,
                            onClick = { categoria = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it
                        errorMensaje = ""
                    },
                    label = { Text("Título corto (ej. Pedir agua, Cuenta)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = mensaje,
                    onValueChange = {
                        mensaje = it
                        errorMensaje = ""
                    },
                    label = { Text("Mensaje a comunicar o reproducir en voz alta") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMensaje.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isBlank() || mensaje.isBlank()) {
                        errorMensaje = "Por favor completa el título y el mensaje."
                    } else {
                        val tarjetaFinal = tarjetaExistente?.copy(
                            titulo = titulo.trim(),
                            mensaje = mensaje.trim(),
                            categoria = categoria
                        ) ?: TarjetaComunicacion(
                            titulo = titulo.trim(),
                            mensaje = mensaje.trim(),
                            categoria = categoria
                        )
                        alGuardar(tarjetaFinal)
                    }
                }
            ) {
                Text(if (tarjetaExistente == null) "Guardar frase" else "Actualizar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = alDescartar) {
                Text("Cancelar")
            }
        }
    )
}
