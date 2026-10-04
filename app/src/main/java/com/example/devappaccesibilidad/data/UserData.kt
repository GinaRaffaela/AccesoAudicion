package com.example.devappaccesibilidad.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Clase de datos que representa a un usuario registrado en la aplicación
data class Usuario(
    val nombre: String,
    val email: String,
    val contrasena: String,
    val genero: String,
    val pais: String,
    val preferencias: List<String>
)

/*
 * Repositorio de usuarios de la aplicación.
 * Contiene un array con 5 usuarios registrados previamente,
 * junto con sus contraseñas y preferencias de accesibilidad.
 */
object RepositorioUsuarios {

    // Array con los 5 usuarios pre-registrados del sistema
    private val usuarios = mutableListOf(
        Usuario(
            nombre = "Carlos Mendoza",
            email = "carlos@mail.com",
            contrasena = "Pass1234",
            genero = "Masculino",
            pais = "Chile",
            preferencias = listOf("Subtítulos", "Vibraciones")
        ),
        Usuario(
            nombre = "Valentina Rojas",
            email = "valentina@mail.com",
            contrasena = "Segura99",
            genero = "Femenino",
            pais = "Argentina",
            preferencias = listOf("Texto a voz")
        ),
        Usuario(
            nombre = "Luis Herrera",
            email = "luis@mail.com",
            contrasena = "Clave2024",
            genero = "Masculino",
            pais = "Colombia",
            preferencias = listOf("Subtítulos")
        ),
        Usuario(
            nombre = "Sofía Castro",
            email = "sofia@mail.com",
            contrasena = "Sofia#01",
            genero = "Femenino",
            pais = "México",
            preferencias = listOf("Vibraciones", "Texto a voz")
        ),
        Usuario(
            nombre = "Andrés Pino",
            email = "andres@mail.com",
            contrasena = "Andres77",
            genero = "Prefiero no decir",
            pais = "Perú",
            preferencias = listOf("Subtítulos", "Vibraciones", "Texto a voz")
        )
    )

    private const val PREFS_USUARIOS = "acceso_audicion_usuarios"
    private const val KEY_LISTA_USUARIOS = "clave_lista_usuarios"
    private var cargado = false

    fun cargarPersistencia(context: Context) {
        if (!cargado) {
            val prefs = context.getSharedPreferences(PREFS_USUARIOS, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_LISTA_USUARIOS, null)
            if (!json.isNullOrBlank()) {
                try {
                    val tipo = object : TypeToken<List<Usuario>>() {}.type
                    val guardados: List<Usuario> = Gson().fromJson(json, tipo)
                    guardados.forEach { u ->
                        if (!existeEmail(u.email)) {
                            usuarios.add(u)
                        }
                    }
                } catch (e: Exception) {
                    // Ignora si el formato difiere
                }
            }
            cargado = true
        }
    }

    private fun persistir(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_USUARIOS, Context.MODE_PRIVATE)
            val json = Gson().toJson(usuarios)
            prefs.edit().putString(KEY_LISTA_USUARIOS, json).apply()
        } catch (e: Exception) {
            // Manejo silencioso en test
        }
    }

    fun obtenerTodos(): List<Usuario> = usuarios.toList()

    fun autenticar(email: String, contrasena: String): Boolean {
        return usuarios.any { it.email.equals(email.trim(), ignoreCase = true) && it.contrasena == contrasena }
    }

    fun existeEmail(email: String): Boolean {
        return usuarios.any { it.email.equals(email.trim(), ignoreCase = true) }
    }

    fun agregar(usuario: Usuario, context: Context? = null): Boolean {
        if (existeEmail(usuario.email)) return false
        usuarios.add(usuario)
        context?.let { persistir(it) }
        return true
    }

    fun buscarPorEmail(email: String): Usuario? {
        return usuarios.find { it.email.equals(email.trim(), ignoreCase = true) }
    }
}
