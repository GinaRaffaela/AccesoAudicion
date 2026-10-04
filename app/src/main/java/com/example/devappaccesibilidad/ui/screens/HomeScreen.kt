package com.example.devappaccesibilidad.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devappaccesibilidad.data.PreferenciasSesion
import com.example.devappaccesibilidad.data.RepositorioTarjetas
import com.example.devappaccesibilidad.data.RepositorioUsuarios
import com.example.devappaccesibilidad.data.ServicioAutenticacion
import com.example.devappaccesibilidad.data.TarjetaComunicacion
import com.example.devappaccesibilidad.ui.components.DialogoConfirmarEliminar
import com.example.devappaccesibilidad.ui.components.DialogoEditarCrearTarjeta
import com.example.devappaccesibilidad.ui.components.DialogoModoCartel
import com.example.devappaccesibilidad.ui.components.ItemTarjetaComunicacion
import com.example.devappaccesibilidad.util.TextToSpeechHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla principal (Home) de AccesoAudición.
 *
 * Integra:
 * 1. Pestaña Comunicador CRUD: Registrar, consultar, modificar y eliminar frases rápidas.
 * 2. Pestaña Escribir y Hablar (TTS + Modo Cartel): Síntesis de voz accesible y transcripción.
 * 3. Pestaña Perfil y Usuarios: Información de la sesión (SharedPreferences) y tabla de usuarios.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    email: String,
    alCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val prefs = remember { PreferenciasSesion.obtenerInstancia(context) }
    val usuario = RepositorioUsuarios.buscarPorEmail(email)
    val todosLosUsuarios = RepositorioUsuarios.obtenerTodos()

    // Inicialización del motor Text-to-Speech nativo accesible
    val ttsHelper = remember { TextToSpeechHelper(context) }
    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.liberar()
        }
    }

    // Función de vibración accesible para personas sordas
    fun emitirVibracionAccesible() {
        try {
            if (prefs.esVibracionHabilitada()) {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(120)
                }
            }
        } catch (_: Exception) {}
    }

    // Estados de navegación inferior (3 pestañas)
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }

    // Estados para la pestaña CRUD de Tarjetas
    var listaTarjetas by remember { mutableStateOf(RepositorioTarjetas.obtenerTodas(context)) }
    var categoriaFiltro by remember { mutableStateOf("Todas") }
    var consultaBusqueda by remember { mutableStateOf("") }

    // Diálogos modales
    var tarjetaParaEditar by remember { mutableStateOf<TarjetaComunicacion?>(null) }
    var mostrarDialogoCrearEditar by remember { mutableStateOf(false) }
    var tarjetaParaEliminar by remember { mutableStateOf<TarjetaComunicacion?>(null) }
    var tarjetaParaCartel by remember { mutableStateOf<TarjetaComunicacion?>(null) }

    // Estados para la pestaña de Escribir y Hablar
    var textoLibre by remember { mutableStateOf("") }
    var reproduciendoVoz by remember { mutableStateOf(false) }
    var transcripcionSimulada by remember { mutableStateOf("") }

    // Función para refrescar la lista de tarjetas
    fun actualizarLista() {
        listaTarjetas = RepositorioTarjetas.obtenerTodas(context)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                shadowElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HearingDisabled,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AccesoAudición",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = usuario?.nombre ?: email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            ServicioAutenticacion.cerrarSesion(context)
                            alCerrarSesion()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pestanaSeleccionada == 0,
                    onClick = { pestanaSeleccionada = 0 },
                    icon = { Icon(Icons.Default.ViewAgenda, contentDescription = null) },
                    label = { Text("Frases Rápidas") }
                )
                NavigationBarItem(
                    selected = pestanaSeleccionada == 1,
                    onClick = { pestanaSeleccionada = 1 },
                    icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null) },
                    label = { Text("Escribir y Hablar") }
                )
                NavigationBarItem(
                    selected = pestanaSeleccionada == 2,
                    onClick = { pestanaSeleccionada = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Perfil") }
                )
            }
        },
        floatingActionButton = {
            if (pestanaSeleccionada == 0) {
                FloatingActionButton(
                    onClick = {
                        tarjetaParaEditar = null
                        mostrarDialogoCrearEditar = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar nueva frase")
                }
            }
        }
    ) { paddingValores ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (pestanaSeleccionada) {
                // ==========================================
                // PESTAÑA 0: COMUNICADOR RÁPIDO (OPERACIONES CRUD)
                // ==========================================
                0 -> {
                    val categorias = listOf("Todas", "Emergencia", "Salud", "Transporte", "Compras", "General")

                    val tarjetasFiltradas = listaTarjetas.filter { tarjeta ->
                        val coincideCat = if (categoriaFiltro == "Todas") true else tarjeta.categoria.equals(categoriaFiltro, ignoreCase = true)
                        val coincideBusqueda = if (consultaBusqueda.isBlank()) true else {
                            tarjeta.titulo.contains(consultaBusqueda, ignoreCase = true) ||
                                    tarjeta.mensaje.contains(consultaBusqueda, ignoreCase = true)
                        }
                        coincideCat && coincideBusqueda
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Barra de búsqueda en tiempo real (READ / Consulta)
                        OutlinedTextField(
                            value = consultaBusqueda,
                            onValueChange = { consultaBusqueda = it },
                            placeholder = { Text("Buscar frase por título o mensaje...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (consultaBusqueda.isNotBlank()) {
                                    IconButton(onClick = { consultaBusqueda = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Filtro de categorías (READ / Consulta por categoría)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categorias) { cat ->
                                FilterChip(
                                    selected = categoriaFiltro == cat,
                                    onClick = { categoriaFiltro = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tarjetas disponibles (${tarjetasFiltradas.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (tarjetasFiltradas.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No se encontraron frases para los filtros seleccionados.\nToca '+' para registrar una nueva frase.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Lista interactiva de tarjetas (CRUD: Read, Update, Delete)
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                items(tarjetasFiltradas, key = { it.id }) { tarjeta ->
                                    ItemTarjetaComunicacion(
                                        tarjeta = tarjeta,
                                        alHablar = {
                                            emitirVibracionAccesible()
                                            ttsHelper.hablar(tarjeta.mensaje)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("🔊 Reproduciendo: \"${tarjeta.titulo}\"")
                                            }
                                        },
                                        alAbrirCartel = {
                                            tarjetaParaCartel = tarjeta
                                        },
                                        alEditar = {
                                            tarjetaParaEditar = tarjeta
                                            mostrarDialogoCrearEditar = true
                                        },
                                        alEliminar = {
                                            tarjetaParaEliminar = tarjeta
                                        },
                                        alAlternarFavorita = {
                                            RepositorioTarjetas.alternarFavorita(tarjeta.id, context)
                                            actualizarLista()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // PESTAÑA 1: ESCRIBIR Y HABLAR (TTS & MODO CARTEL)
                // ==========================================
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Escribir para Hablar",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Escribe cualquier frase y el dispositivo la pronunciará en voz alta.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = textoLibre,
                                    onValueChange = { textoLibre = it },
                                    label = { Text("Escribe el mensaje aquí...") },
                                    minLines = 4,
                                    maxLines = 7,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Animación visual de sonido para retroalimentación accesible
                                AnimatedVisibility(visible = reproduciendoVoz) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "🔊 Reproduciendo mensaje en voz alta...",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (textoLibre.isNotBlank()) {
                                                emitirVibracionAccesible()
                                                reproduciendoVoz = true
                                                ttsHelper.hablar(textoLibre.trim())
                                                coroutineScope.launch {
                                                    delay(2500)
                                                    reproduciendoVoz = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1.3f),
                                        enabled = textoLibre.isNotBlank()
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Hablar voz alta")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (textoLibre.isNotBlank()) {
                                                tarjetaParaCartel = TarjetaComunicacion(
                                                    titulo = "Mensaje escrito",
                                                    mensaje = textoLibre.trim(),
                                                    categoria = "General"
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = textoLibre.isNotBlank()
                                    ) {
                                        Icon(Icons.Default.Fullscreen, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cartel")
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = {
                                        if (textoLibre.isNotBlank()) {
                                            val nueva = TarjetaComunicacion(
                                                titulo = textoLibre.take(24).trim() + "...",
                                                mensaje = textoLibre.trim(),
                                                categoria = "General"
                                            )
                                            RepositorioTarjetas.registrarTarjeta(nueva, context)
                                            actualizarLista()
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("✓ Frase guardada en tus tarjetas rápidas")
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = textoLibre.isNotBlank()
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Guardar en mis Frases Rápidas (CRUD)")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Sección: Transcripción / Voz a Texto para el oyente
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Hablar para Escribir (Voz a Texto)",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Pídele al interlocutor oyente que hable al micrófono para que leas su mensaje en pantalla grande.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        emitirVibracionAccesible()
                                        transcripcionSimulada = "Entendido, ya voy a preparar el pedido. Por favor tome asiento y le avisaremos visualmente cuando esté listo."
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Voz captada y transcrita a texto")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Escuchar respuesta del oyente")
                                }

                                if (transcripcionSimulada.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = "Transcripción en pantalla:",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = transcripcionSimulada,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontSize = 18.sp,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // PESTAÑA 2: PERFIL DE USUARIO Y USUARIOS REGISTRADOS
                // ==========================================
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(54.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Sesión activa (SharedPreferences)",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (usuario != null) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = usuario.nombre.first().uppercaseChar().toString(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = usuario.nombre,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "Datos del usuario",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    FilaDato("Correo", usuario.email)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    FilaDato("Género", usuario.genero)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    FilaDato("País", usuario.pais)

                                    if (usuario.preferencias.isNotEmpty()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        Text(
                                            text = "Preferencias de accesibilidad",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = usuario.preferencias.joinToString(", "),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Ajustes rápidos de accesibilidad
                        var vibracionActiva by remember { mutableStateOf(prefs.esVibracionHabilitada()) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Ajustes de accesibilidad",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Retroalimentación háptica (vibración)")
                                    Switch(
                                        checked = vibracionActiva,
                                        onCheckedChange = { nuevoEstado ->
                                            vibracionActiva = nuevoEstado
                                            prefs.guardarPreferenciasAccesibilidad(
                                                prefs.esContrasteAlto(),
                                                prefs.esTextoGrande(),
                                                nuevoEstado
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Tabla de todos los usuarios registrados (Requisito mantenido de semanas anteriores)
                        Text(
                            text = "Usuarios registrados en el sistema",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Nombre",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    Text(
                                        text = "País",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(0.8f)
                                    )
                                    Text(
                                        text = "Preferencias",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1.5f)
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                todosLosUsuarios.forEachIndexed { index, u ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (index % 2 == 0) MaterialTheme.colorScheme.surface
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = u.nombre,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1.2f)
                                        )
                                        Text(
                                            text = u.pais,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(0.8f)
                                        )
                                        Text(
                                            text = if (u.preferencias.isNotEmpty()) u.preferencias.joinToString(", ") else "—",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1.5f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                ServicioAutenticacion.cerrarSesion(context)
                                alCerrarSesion()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Cerrar sesión")
                        }
                    }
                }
            }
        }
    }

    // Modal para CREAR o EDITAR tarjeta (Create & Update)
    if (mostrarDialogoCrearEditar) {
        DialogoEditarCrearTarjeta(
            tarjetaExistente = tarjetaParaEditar,
            alGuardar = { tarjetaModificada ->
                if (tarjetaParaEditar == null) {
                    // CREATE
                    RepositorioTarjetas.registrarTarjeta(tarjetaModificada, context)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("✓ Frase registrada correctamente")
                    }
                } else {
                    // UPDATE
                    RepositorioTarjetas.modificarTarjeta(tarjetaModificada, context)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("✓ Frase actualizada correctamente")
                    }
                }
                actualizarLista()
                mostrarDialogoCrearEditar = false
                tarjetaParaEditar = null
            },
            alDescartar = {
                mostrarDialogoCrearEditar = false
                tarjetaParaEditar = null
            }
        )
    }

    // Modal para CONFIRMAR ELIMINACIÓN (Delete)
    tarjetaParaEliminar?.let { tarjeta ->
        DialogoConfirmarEliminar(
            tarjeta = tarjeta,
            alConfirmar = {
                RepositorioTarjetas.eliminarTarjeta(tarjeta.id, context)
                actualizarLista()
                tarjetaParaEliminar = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("✓ Frase eliminada")
                }
            },
            alCancelar = {
                tarjetaParaEliminar = null
            }
        )
    }

    // Modal para MODO CARTEL GIGANTE (Visualización accesible de alto contraste)
    tarjetaParaCartel?.let { tarjeta ->
        DialogoModoCartel(
            tarjeta = tarjeta,
            alHablarTexto = { texto ->
                emitirVibracionAccesible()
                ttsHelper.hablar(texto)
            },
            alCerrar = {
                tarjetaParaCartel = null
            }
        )
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.5f)
        )
    }
}
