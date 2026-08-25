package com.example.devappaccesibilidad.data

data class Usuario(
    val nombre: String,
    val email: String,
    val contrasena: String,
    val genero: String,
    val pais: String,
    val preferencias: List<String>
)

object RepositorioUsuarios {

    private val usuarios = mutableListOf(
        Usuario("Carlos Mendoza", "carlos@mail.com", "Pass1234", "Masculino", "Chile", listOf("Subtítulos", "Vibraciones")),
        Usuario("Valentina Rojas", "valentina@mail.com", "Segura99", "Femenino", "Argentina", listOf("Texto a voz")),
        Usuario("Luis Herrera", "luis@mail.com", "Clave2024", "Masculino", "Colombia", listOf("Subtítulos")),
        Usuario("Sofía Castro", "sofia@mail.com", "Sofia#01", "Femenino", "México", listOf("Vibraciones", "Texto a voz")),
        Usuario("Andrés Pino", "andres@mail.com", "Andres77", "Prefiero no decir", "Perú", listOf("Subtítulos", "Vibraciones", "Texto a voz"))
    )

    fun obtenerTodos(): List<Usuario> = usuarios.toList()

    fun autenticar(email: String, contrasena: String): Boolean {
        return usuarios.any { it.email == email && it.contrasena == contrasena }
    }

    fun existeEmail(email: String): Boolean {
        return usuarios.any { it.email == email }
    }

    fun agregar(usuario: Usuario): Boolean {
        if (existeEmail(usuario.email)) return false
        usuarios.add(usuario)
        return true
    }

    fun buscarPorEmail(email: String): Usuario? {
        return usuarios.find { it.email == email }
    }
}
