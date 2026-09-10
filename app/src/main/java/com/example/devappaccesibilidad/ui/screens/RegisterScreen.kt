package com.example.devappaccesibilidad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.devappaccesibilidad.data.RepositorioUsuarios
import com.example.devappaccesibilidad.data.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistro(alVolver: () -> Unit) {

    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var mostrarContrasena by remember { mutableStateOf(false) }
    var mostrarConfirmar by remember { mutableStateOf(false) }

    // Combo box — selección de país
    val paisesDisponibles = listOf("Chile", "Argentina", "Colombia", "México", "Perú", "Bolivia", "Ecuador", "Venezuela", "Uruguay", "Paraguay")
    var paisSeleccionado by remember { mutableStateOf("Chile") }
    var expandirPaises by remember { mutableStateOf(false) }

    // Radio buttons — selección de género
    val opcionesGenero = listOf("Masculino", "Femenino", "Prefiero no decir")
    var generoSeleccionado by remember { mutableStateOf("Masculino") }

    // Checkboxes — preferencias de accesibilidad
    val opcionesPreferencias = listOf("Subtítulos", "Vibraciones", "Texto a voz")
    val preferenciasSeleccionadas = remember { mutableStateListOf<String>() }

    var mensajeError by remember { mutableStateOf("") }
    var mensajeExito by remember { mutableStateOf("") }

    var listaUsuarios by remember { mutableStateOf(RepositorioUsuarios.obtenerTodos()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Botón de volver (IconButton) + título
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = alVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card con inputs de datos personales (OutlinedTextField)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Datos personales", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(14.dp))

                // Input: nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        mensajeError = ""
                        mensajeExito = ""
                    },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Input: email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        mensajeError = ""
                        mensajeExito = ""
                    },
                    label = { Text("Correo electrónico") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Input: contraseña con toggle de visibilidad
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = {
                        contrasena = it
                        mensajeError = ""
                        mensajeExito = ""
                    },
                    label = { Text("Contraseña") },
                    trailingIcon = {
                        IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                            Icon(
                                imageVector = if (mostrarContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Input: confirmar contraseña
                OutlinedTextField(
                    value = confirmarContrasena,
                    onValueChange = {
                        confirmarContrasena = it
                        mensajeError = ""
                        mensajeExito = ""
                    },
                    label = { Text("Confirmar contraseña") },
                    trailingIcon = {
                        IconButton(onClick = { mostrarConfirmar = !mostrarConfirmar }) {
                            Icon(
                                imageVector = if (mostrarConfirmar) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (mostrarConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card con combo box (ExposedDropdownMenuBox) para seleccionar país
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("País de residencia", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(14.dp))

                ExposedDropdownMenuBox(
                    expanded = expandirPaises,
                    onExpandedChange = { expandirPaises = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = paisSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selecciona tu país") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirPaises) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandirPaises,
                        onDismissRequest = { expandirPaises = false }
                    ) {
                        paisesDisponibles.forEach { pais ->
                            DropdownMenuItem(
                                text = { Text(pais) },
                                onClick = {
                                    paisSeleccionado = pais
                                    expandirPaises = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card con radio buttons para seleccionar género
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Género", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(10.dp))

                opcionesGenero.forEach { opcion ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = generoSeleccionado == opcion,
                            onClick = { generoSeleccionado = opcion }
                        )
                        Text(
                            text = opcion,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card con checkboxes para preferencias de comunicación
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Preferencias de comunicación", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(10.dp))

                opcionesPreferencias.forEach { preferencia ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Checkbox(
                            checked = preferenciasSeleccionadas.contains(preferencia),
                            onCheckedChange = { marcado ->
                                if (marcado) preferenciasSeleccionadas.add(preferencia)
                                else preferenciasSeleccionadas.remove(preferencia)
                            }
                        )
                        Text(
                            text = preferencia,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (mensajeExito.isNotEmpty()) {
            Text(
                text = mensajeExito,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Botón principal de registro
        Button(
            onClick = {
                mensajeError = ""
                mensajeExito = ""
                when {
                    nombre.isBlank() || email.isBlank() || contrasena.isBlank() || confirmarContrasena.isBlank() -> {
                        mensajeError = "Por favor completa todos los campos obligatorios."
                    }
                    !email.contains("@") -> {
                        mensajeError = "Ingresa un correo electrónico válido."
                    }
                    contrasena != confirmarContrasena -> {
                        mensajeError = "Las contraseñas no coinciden."
                    }
                    contrasena.length < 6 -> {
                        mensajeError = "La contraseña debe tener al menos 6 caracteres."
                    }
                    else -> {
                        val nuevo = Usuario(
                            nombre = nombre.trim(),
                            email = email.trim(),
                            contrasena = contrasena,
                            genero = generoSeleccionado,
                            pais = paisSeleccionado,
                            preferencias = preferenciasSeleccionadas.toList()
                        )
                        if (RepositorioUsuarios.agregar(nuevo)) {
                            mensajeExito = "✓ Usuario registrado correctamente."
                            listaUsuarios = RepositorioUsuarios.obtenerTodos()
                            nombre = ""
                            email = ""
                            contrasena = ""
                            confirmarContrasena = ""
                            preferenciasSeleccionadas.clear()
                        } else {
                            mensajeError = "El correo electrónico ya está registrado."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vínculo para volver al login (TextButton)
        TextButton(onClick = alVolver, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tabla/grilla de usuarios registrados en el sistema
        Text(
            text = "Usuarios registrados",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

                // Encabezados de columna
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Nombre",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Correo",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "País",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                listaUsuarios.forEachIndexed { index, usuario ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (index % 2 == 0) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = usuario.nombre,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = usuario.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = usuario.pais,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "⚠️ Trabajo universitario en desarrollo — No corresponde a un producto final.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
