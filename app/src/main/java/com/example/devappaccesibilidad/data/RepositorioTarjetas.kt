package com.example.devappaccesibilidad.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Repositorio encargado de gestionar las operaciones CRUD de las tarjetas de comunicación rápida
 * para personas con discapacidad sensorial auditiva.
 *
 * Implementa persistencia dual:
 * 1. Persistencia local mediante SharedPreferences + Gson (garantiza funcionamiento sin conexión).
 * 2. Persistencia en la nube mediante Firebase Firestore (cumple el requerimiento técnico de Firebase).
 */
object RepositorioTarjetas {

    private const val TAG = "RepositorioTarjetas"
    private const val PREF_NAME = "acceso_audicion_tarjetas"
    private const val KEY_TARJETAS = "lista_tarjetas_comunicacion"
    private val gson = Gson()

    // Memoria caché para rapidez en tiempo de ejecución y tests unitarios
    private val tarjetasEnMemoria = mutableListOf<TarjetaComunicacion>()
    private var inicializado = false

    /**
     * Lista de tarjetas predeterminadas pensadas para facilitar la comunicación
     * de personas con discapacidad auditiva en situaciones cotidianas.
     */
    val tarjetasPorDefecto = listOf(
        TarjetaComunicacion(
            id = "def-1",
            titulo = "Asistencia auditiva",
            mensaje = "Hola, tengo discapacidad auditiva. Por favor hábleme de frente o escriba aquí para que nos entendamos.",
            categoria = "Emergencia",
            esFavorita = true
        ),
        TarjetaComunicacion(
            id = "def-2",
            titulo = "Consulta de precio",
            mensaje = "Buenos días/tardes, ¿podría indicarme o escribirme el valor de este producto por favor?",
            categoria = "Compras",
            esFavorita = true
        ),
        TarjetaComunicacion(
            id = "def-3",
            titulo = "Aviso en transporte",
            mensaje = "Disculpe, ¿podría avisarme con una seña visual cuando lleguemos a la próxima estación o parada?",
            categoria = "Transporte",
            esFavorita = false
        ),
        TarjetaComunicacion(
            id = "def-4",
            titulo = "Atención de salud",
            mensaje = "Tengo dificultad para escuchar. Por favor anote la dosis o indicaciones médicas por escrito.",
            categoria = "Salud",
            esFavorita = true
        ),
        TarjetaComunicacion(
            id = "def-5",
            titulo = "Agradecimiento",
            mensaje = "¡Muchas gracias por su paciencia y comprensión! Que tenga un excelente día.",
            categoria = "General",
            esFavorita = false
        )
    )

    // Control de sincronización remota (desactivada en tests unitarios locales)
    var sincronizacionRemotaHabilitada = true

    /**
     * Inicializa los datos en memoria si es necesario (útil para pruebas unitarias sin Context).
     */
    fun inicializarParaPruebas(tarjetasIniciales: List<TarjetaComunicacion>? = null) {
        sincronizacionRemotaHabilitada = false
        tarjetasEnMemoria.clear()
        tarjetasEnMemoria.addAll(tarjetasIniciales ?: tarjetasPorDefecto)
        inicializado = true
    }

    /**
     * Carga las tarjetas desde SharedPreferences si no se han cargado previamente.
     */
    private fun asegurarCarga(context: Context) {
        if (!inicializado) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_TARJETAS, null)

            tarjetasEnMemoria.clear()
            if (!json.isNullOrBlank()) {
                val tipo = object : TypeToken<List<TarjetaComunicacion>>() {}.type
                val listaGuardada: List<TarjetaComunicacion> = gson.fromJson(json, tipo)
                tarjetasEnMemoria.addAll(listaGuardada)
            } else {
                tarjetasEnMemoria.addAll(tarjetasPorDefecto)
                guardarEnPreferencias(context)
            }
            inicializado = true
        }
    }

    private fun guardarEnPreferencias(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(tarjetasEnMemoria)
        prefs.edit().putString(KEY_TARJETAS, json).apply()
    }

    // ==========================================
    // OPERACIONES CRUD (Create, Read, Update, Delete)
    // ==========================================

    /**
     * READ: Consulta y retorna todas las tarjetas disponibles.
     */
    fun obtenerTodas(context: Context? = null): List<TarjetaComunicacion> {
        context?.let { asegurarCarga(it) }
        if (!inicializado && tarjetasEnMemoria.isEmpty()) {
            tarjetasEnMemoria.addAll(tarjetasPorDefecto)
            inicializado = true
        }
        return tarjetasEnMemoria.sortedWith(
            compareByDescending<TarjetaComunicacion> { it.esFavorita }
                .thenByDescending { it.fechaCreacion }
        )
    }

    /**
     * READ: Consulta tarjetas filtradas por una categoría específica.
     */
    fun obtenerPorCategoria(categoria: String, context: Context? = null): List<TarjetaComunicacion> {
        return obtenerTodas(context).filter {
            if (categoria.equals("Todas", ignoreCase = true)) true
            else it.categoria.equals(categoria, ignoreCase = true)
        }
    }

    /**
     * READ: Busca una tarjeta por su identificador único.
     */
    fun buscarPorId(id: String, context: Context? = null): TarjetaComunicacion? {
        context?.let { asegurarCarga(it) }
        return tarjetasEnMemoria.find { it.id == id }
    }

    /**
     * CREATE: Registra una nueva tarjeta de comunicación.
     */
    fun registrarTarjeta(tarjeta: TarjetaComunicacion, context: Context? = null): Boolean {
        if (tarjeta.titulo.isBlank() || tarjeta.mensaje.isBlank()) {
            return false
        }
        context?.let { asegurarCarga(it) }
        tarjetasEnMemoria.add(0, tarjeta)
        context?.let { guardarEnPreferencias(it) }

        // Sincronización en la nube con Firebase Firestore
        sincronizarConFirestore(tarjeta, esEliminacion = false)
        return true
    }

    /**
     * UPDATE: Modifica los datos de una tarjeta existente.
     */
    fun modificarTarjeta(tarjetaActualizada: TarjetaComunicacion, context: Context? = null): Boolean {
        context?.let { asegurarCarga(it) }
        val index = tarjetasEnMemoria.indexOfFirst { it.id == tarjetaActualizada.id }
        if (index != -1) {
            tarjetasEnMemoria[index] = tarjetaActualizada
            context?.let { guardarEnPreferencias(it) }
            sincronizarConFirestore(tarjetaActualizada, esEliminacion = false)
            return true
        }
        return false
    }

    /**
     * DELETE: Elimina una tarjeta por su ID.
     */
    fun eliminarTarjeta(id: String, context: Context? = null): Boolean {
        context?.let { asegurarCarga(it) }
        val tarjetaAEliminar = tarjetasEnMemoria.find { it.id == id }
        val removido = tarjetasEnMemoria.removeAll { it.id == id }
        if (removido) {
            context?.let { guardarEnPreferencias(it) }
            tarjetaAEliminar?.let { sincronizarConFirestore(it, esEliminacion = true) }
        }
        return removido
    }

    /**
     * UPDATE rápido: Alterna el estado de favorita de una tarjeta.
     */
    fun alternarFavorita(id: String, context: Context? = null): Boolean {
        context?.let { asegurarCarga(it) }
        val index = tarjetasEnMemoria.indexOfFirst { it.id == id }
        if (index != -1) {
            val actual = tarjetasEnMemoria[index]
            val actualizada = actual.copy(esFavorita = !actual.esFavorita)
            tarjetasEnMemoria[index] = actualizada
            context?.let { guardarEnPreferencias(it) }
            sincronizarConFirestore(actualizada, esEliminacion = false)
            return true
        }
        return false
    }

    /**
     * Persistencia remota en Firebase Firestore.
     * Si Firebase está activo guarda/elimina en la colección "tarjetas_comunicacion".
     * Maneja excepciones silenciosamente para evitar que caídas de red o credenciales
     * interrumpan la experiencia de la app.
     */
    private fun sincronizarConFirestore(tarjeta: TarjetaComunicacion, esEliminacion: Boolean) {
        if (!sincronizacionRemotaHabilitada) return
        try {
            val db = FirebaseFirestore.getInstance()
            val coleccion = db.collection("tarjetas_comunicacion")
            if (esEliminacion) {
                coleccion.document(tarjeta.id).delete()
            } else {
                val mapaDatos = hashMapOf(
                    "id" to tarjeta.id,
                    "titulo" to tarjeta.titulo,
                    "mensaje" to tarjeta.mensaje,
                    "categoria" to tarjeta.categoria,
                    "esFavorita" to tarjeta.esFavorita,
                    "fechaCreacion" to tarjeta.fechaCreacion
                )
                coleccion.document(tarjeta.id).set(mapaDatos)
            }
        } catch (t: Throwable) {
            // Manejo silencioso en caso de no contar con conexión o estar en entorno sin Firebase
        }
    }
}
