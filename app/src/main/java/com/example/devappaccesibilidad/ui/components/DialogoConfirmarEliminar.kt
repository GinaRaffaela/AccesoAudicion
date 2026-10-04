package com.example.devappaccesibilidad.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.devappaccesibilidad.data.TarjetaComunicacion

/**
 * Diálogo accesible de confirmación para la operación DELETE de tarjetas.
 */
@Composable
fun DialogoConfirmarEliminar(
    tarjeta: TarjetaComunicacion,
    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = alCancelar,
        title = {
            Text(text = "¿Eliminar frase?")
        },
        text = {
            Text(
                text = "Se eliminará permanentemente la frase \"${tarjeta.titulo}\". Esta acción no se puede deshacer.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = alConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = alCancelar) {
                Text("Cancelar")
            }
        }
    )
}
