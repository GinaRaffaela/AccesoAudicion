package com.example.devappaccesibilidad.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de persistencia de sesión y preferencias de accesibilidad
 * implementado mediante SharedPreferences de Android.
 *
 * Cumple con el requerimiento de persistencia local y manejo de sesión.
 */
class PreferenciasSesion(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        NOMBRE_PREFERENCIAS,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val NOMBRE_PREFERENCIAS = "acceso_audicion_sesion"
        private const val CLAVE_SESION_ACTIVA = "clave_sesion_activa"
        private const val CLAVE_EMAIL_USUARIO = "clave_email_usuario"
        private const val CLAVE_NOMBRE_USUARIO = "clave_nombre_usuario"
        private const val CLAVE_CONTRASTE_ALTO = "clave_contraste_alto"
        private const val CLAVE_TEXTO_GRANDE = "clave_texto_grande"
        private const val CLAVE_VIBRACION_HABILITADA = "clave_vibracion_habilitada"

        @Volatile
        private var INSTANCIA: PreferenciasSesion? = null

        fun obtenerInstancia(context: Context): PreferenciasSesion {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: PreferenciasSesion(context.applicationContext).also { INSTANCIA = it }
            }
        }
    }

    /**
     * Guarda la sesión del usuario al autenticarse exitosamente.
     */
    fun guardarSesion(email: String, nombre: String = "") {
        prefs.edit()
            .putBoolean(CLAVE_SESION_ACTIVA, true)
            .putString(CLAVE_EMAIL_USUARIO, email)
            .putString(CLAVE_NOMBRE_USUARIO, nombre)
            .apply()
    }

    /**
     * Retorna si existe una sesión activa actualmente.
     */
    fun estaSesionIniciada(): Boolean {
        return prefs.getBoolean(CLAVE_SESION_ACTIVA, false)
    }

    /**
     * Obtiene el correo electrónico del usuario con sesión activa.
     */
    fun obtenerEmailUsuario(): String? {
        return prefs.getString(CLAVE_EMAIL_USUARIO, null)
    }

    /**
     * Obtiene el nombre del usuario con sesión activa.
     */
    fun obtenerNombreUsuario(): String? {
        return prefs.getString(CLAVE_NOMBRE_USUARIO, null)
    }

    /**
     * Cierra la sesión activa y elimina las credenciales almacenadas en SharedPreferences.
     */
    fun cerrarSesion() {
        prefs.edit()
            .remove(CLAVE_SESION_ACTIVA)
            .remove(CLAVE_EMAIL_USUARIO)
            .remove(CLAVE_NOMBRE_USUARIO)
            .apply()
    }

    /**
     * Guarda preferencias de interfaz accesibles para personas con discapacidad auditiva.
     */
    fun guardarPreferenciasAccesibilidad(contrasteAlto: Boolean, textoGrande: Boolean, vibracion: Boolean) {
        prefs.edit()
            .putBoolean(CLAVE_CONTRASTE_ALTO, contrasteAlto)
            .putBoolean(CLAVE_TEXTO_GRANDE, textoGrande)
            .putBoolean(CLAVE_VIBRACION_HABILITADA, vibracion)
            .apply()
    }

    fun esContrasteAlto(): Boolean = prefs.getBoolean(CLAVE_CONTRASTE_ALTO, false)
    fun esTextoGrande(): Boolean = prefs.getBoolean(CLAVE_TEXTO_GRANDE, false)
    fun esVibracionHabilitada(): Boolean = prefs.getBoolean(CLAVE_VIBRACION_HABILITADA, true)
}
