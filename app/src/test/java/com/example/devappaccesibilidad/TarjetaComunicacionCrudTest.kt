package com.example.devappaccesibilidad

import com.example.devappaccesibilidad.data.RepositorioTarjetas
import com.example.devappaccesibilidad.data.TarjetaComunicacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias con JUnit para verificar las operaciones CRUD
 * (Create, Read, Update, Delete) de las tarjetas de comunicación de accesibilidad.
 */
class TarjetaComunicacionCrudTest {

    @Before
    fun prepararEntorno() {
        // Inicializa el repositorio con datos limpios para cada prueba
        val tarjetasIniciales = listOf(
            TarjetaComunicacion(
                id = "test-1",
                titulo = "Emergencia médica",
                mensaje = "Necesito asistencia médica urgente.",
                categoria = "Emergencia",
                esFavorita = true
            ),
            TarjetaComunicacion(
                id = "test-2",
                titulo = "Comprar pan",
                mensaje = "Quiero comprar pan por favor.",
                categoria = "Compras",
                esFavorita = false
            ),
            TarjetaComunicacion(
                id = "test-3",
                titulo = "Parada de metro",
                mensaje = "¿Esta es la estación Baquedano?",
                categoria = "Transporte",
                esFavorita = false
            )
        )
        RepositorioTarjetas.inicializarParaPruebas(tarjetasIniciales)
    }

    // ==========================================
    // PRUEBAS DE LECTURA (READ)
    // ==========================================

    @Test
    fun testConsultarTodasLasTarjetas() {
        val tarjetas = RepositorioTarjetas.obtenerTodas()
        assertEquals(3, tarjetas.size)
        // La favorita (test-1) debe estar primera
        assertEquals("test-1", tarjetas.first().id)
    }

    @Test
    fun testConsultarPorCategoria_filtroCorrecto() {
        val emergencias = RepositorioTarjetas.obtenerPorCategoria("Emergencia")
        assertEquals(1, emergencias.size)
        assertEquals("Emergencia médica", emergencias.first().titulo)

        val compras = RepositorioTarjetas.obtenerPorCategoria("Compras")
        assertEquals(1, compras.size)
        assertEquals("Comprar pan", compras.first().titulo)

        val todas = RepositorioTarjetas.obtenerPorCategoria("Todas")
        assertEquals(3, todas.size)
    }

    @Test
    fun testBuscarPorId_existenteEInexistente() {
        val encontrada = RepositorioTarjetas.buscarPorId("test-2")
        assertNotNull(encontrada)
        assertEquals("Comprar pan", encontrada?.titulo)

        val noExiste = RepositorioTarjetas.buscarPorId("id-invalido")
        assertNull(noExiste)
    }

    // ==========================================
    // PRUEBAS DE CREACIÓN (CREATE)
    // ==========================================

    @Test
    fun testRegistrarTarjeta_exitoso() {
        val nueva = TarjetaComunicacion(
            id = "test-4",
            titulo = "Pedir la cuenta",
            mensaje = "Por favor tráigame la cuenta para pagar con tarjeta.",
            categoria = "General",
            esFavorita = false
        )

        val resultado = RepositorioTarjetas.registrarTarjeta(nueva)
        assertTrue(resultado)

        val todas = RepositorioTarjetas.obtenerTodas()
        assertEquals(4, todas.size)

        val buscada = RepositorioTarjetas.buscarPorId("test-4")
        assertNotNull(buscada)
        assertEquals("Pedir la cuenta", buscada?.titulo)
    }

    @Test
    fun testRegistrarTarjeta_datosVacios_retornaFalse() {
        val tarjetaInvalida = TarjetaComunicacion(
            id = "test-invalida",
            titulo = "",
            mensaje = "Mensaje sin título",
            categoria = "General"
        )
        val resultado = RepositorioTarjetas.registrarTarjeta(tarjetaInvalida)
        assertFalse(resultado)

        val tarjetaSinMensaje = TarjetaComunicacion(
            id = "test-invalida-2",
            titulo = "Título válido",
            mensaje = "   ",
            categoria = "General"
        )
        val resultado2 = RepositorioTarjetas.registrarTarjeta(tarjetaSinMensaje)
        assertFalse(resultado2)
    }

    // ==========================================
    // PRUEBAS DE MODIFICACIÓN (UPDATE)
    // ==========================================

    @Test
    fun testModificarTarjeta_actualizaDatosCorrectamente() {
        val tarjetaActualizada = TarjetaComunicacion(
            id = "test-2",
            titulo = "Comprar pan y leche",
            mensaje = "Quiero comprar pan integral y 1 litro de leche.",
            categoria = "Compras",
            esFavorita = true
        )

        val exito = RepositorioTarjetas.modificarTarjeta(tarjetaActualizada)
        assertTrue(exito)

        val consultada = RepositorioTarjetas.buscarPorId("test-2")
        assertNotNull(consultada)
        assertEquals("Comprar pan y leche", consultada?.titulo)
        assertEquals("Quiero comprar pan integral y 1 litro de leche.", consultada?.mensaje)
        assertTrue(consultada?.esFavorita == true)
    }

    @Test
    fun testModificarTarjeta_idInexistente_retornaFalse() {
        val tarjetaFantasma = TarjetaComunicacion(
            id = "no-existe",
            titulo = "Fantasma",
            mensaje = "No debería actualizar",
            categoria = "General"
        )
        val exito = RepositorioTarjetas.modificarTarjeta(tarjetaFantasma)
        assertFalse(exito)
    }

    @Test
    fun testAlternarFavorita_cambiaEstadoBooleano() {
        val antes = RepositorioTarjetas.buscarPorId("test-3")
        assertFalse(antes?.esFavorita ?: true)

        val cambio = RepositorioTarjetas.alternarFavorita("test-3")
        assertTrue(cambio)

        val despues = RepositorioTarjetas.buscarPorId("test-3")
        assertTrue(despues?.esFavorita == true)
    }

    // ==========================================
    // PRUEBAS DE ELIMINACIÓN (DELETE)
    // ==========================================

    @Test
    fun testEliminarTarjeta_exitoso() {
        val inicial = RepositorioTarjetas.obtenerTodas().size
        assertEquals(3, inicial)

        val eliminado = RepositorioTarjetas.eliminarTarjeta("test-2")
        assertTrue(eliminado)

        val posterior = RepositorioTarjetas.obtenerTodas().size
        assertEquals(2, posterior)

        val buscada = RepositorioTarjetas.buscarPorId("test-2")
        assertNull(buscada)
    }

    @Test
    fun testEliminarTarjeta_idInexistente_retornaFalse() {
        val resultado = RepositorioTarjetas.eliminarTarjeta("id-que-no-existe")
        assertFalse(resultado)
    }
}
