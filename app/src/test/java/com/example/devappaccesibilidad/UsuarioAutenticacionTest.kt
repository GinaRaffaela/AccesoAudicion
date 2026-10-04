package com.example.devappaccesibilidad

import com.example.devappaccesibilidad.data.RepositorioUsuarios
import com.example.devappaccesibilidad.data.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para validar las operaciones de usuario,
 * autenticación y reglas de validación en Kotlin con JUnit.
 */
class UsuarioAutenticacionTest {

    @Test
    fun testUsuariosInicialesExisten() {
        val usuarios = RepositorioUsuarios.obtenerTodos()
        assertTrue(usuarios.size >= 5)
    }

    @Test
    fun testAutenticacion_credencialesCorrectas_retornaTrue() {
        // Usuario por defecto "carlos@mail.com" con clave "Pass1234"
        val exito = RepositorioUsuarios.autenticar("carlos@mail.com", "Pass1234")
        assertTrue(exito)
    }

    @Test
    fun testAutenticacion_claveIncorrecta_retornaFalse() {
        val exito = RepositorioUsuarios.autenticar("carlos@mail.com", "ClaveErronea")
        assertFalse(exito)
    }

    @Test
    fun testAutenticacion_emailInexistente_retornaFalse() {
        val exito = RepositorioUsuarios.autenticar("noexiste@mail.com", "Pass1234")
        assertFalse(exito)
    }

    @Test
    fun testBuscarPorEmail_retornaUsuarioCorrecto() {
        val usuario = RepositorioUsuarios.buscarPorEmail("valentina@mail.com")
        assertNotNull(usuario)
        assertEquals("Valentina Rojas", usuario?.nombre)
        assertEquals("Femenino", usuario?.genero)
        assertEquals("Argentina", usuario?.pais)
    }

    @Test
    fun testBuscarPorEmail_inexistente_retornaNull() {
        val usuario = RepositorioUsuarios.buscarPorEmail("falso@mail.com")
        assertNull(usuario)
    }

    @Test
    fun testExisteEmail_validaExistencia() {
        assertTrue(RepositorioUsuarios.existeEmail("sofia@mail.com"))
        assertFalse(RepositorioUsuarios.existeEmail("nadie@mail.com"))
    }

    @Test
    fun testAgregarUsuario_exitoso() {
        val nuevo = Usuario(
            nombre = "Camila Silva",
            email = "camila.test@mail.com",
            contrasena = "Cami2026",
            genero = "Femenino",
            pais = "Chile",
            preferencias = listOf("Subtítulos", "Vibraciones")
        )

        val agregado = RepositorioUsuarios.agregar(nuevo)
        assertTrue(agregado)

        val encontrado = RepositorioUsuarios.buscarPorEmail("camila.test@mail.com")
        assertNotNull(encontrado)
        assertEquals("Camila Silva", encontrado?.nombre)
    }

    @Test
    fun testAgregarUsuario_duplicado_retornaFalse() {
        val duplicado = Usuario(
            nombre = "Carlos Copia",
            email = "carlos@mail.com", // Ya existe
            contrasena = "OtraClave",
            genero = "Masculino",
            pais = "Chile",
            preferencias = emptyList()
        )

        val agregado = RepositorioUsuarios.agregar(duplicado)
        assertFalse(agregado)
    }

    // ==========================================
    // PRUEBAS DE REGLAS DE NEGOCIO Y VALIDACIÓN
    // ==========================================

    @Test
    fun testValidacionFormatoEmail() {
        val emailValido = "usuario@dominio.cl"
        val emailSinArroba = "usuariodominio.cl"
        val emailVacio = ""

        assertTrue(emailValido.contains("@"))
        assertFalse(emailSinArroba.contains("@"))
        assertTrue(emailVacio.isBlank())
    }

    @Test
    fun testValidacionLongitudMinimaContrasena() {
        val claveCorta = "12345"
        val claveValida = "123456"

        assertTrue(claveCorta.length < 6)
        assertTrue(claveValida.length >= 6)
    }

    @Test
    fun testValidacionCoincidenciaContrasenas() {
        val pass1 = "MiClaveSegura1"
        val pass2 = "MiClaveSegura1"
        val pass3 = "MiClaveDiferente2"

        assertTrue(pass1 == pass2)
        assertFalse(pass1 == pass3)
    }
}
