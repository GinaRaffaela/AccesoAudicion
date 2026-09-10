package com.example.devappaccesibilidad.data

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
