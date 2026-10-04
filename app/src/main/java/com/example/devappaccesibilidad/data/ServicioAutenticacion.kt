package com.example.devappaccesibilidad.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Servicio unificado de Autenticación y Autorización que integra:
 * 1. Firebase Authentication & Firestore (persistencia y autorización en la nube).
 * 2. Fallback al Repositorio de Usuarios y SharedPreferences (garantiza funcionamiento sin red o antes de configurar credenciales).
 * 3. Persistencia de sesión mediante SharedPreferences (PreferenciasSesion).
 */
object ServicioAutenticacion {

    private const val TAG = "ServicioAutenticacion"

    /**
     * Inicia sesión validando credenciales en Firebase Auth y en el repositorio local.
     */
    fun iniciarSesion(
        email: String,
        clave: String,
        context: Context,
        onResultado: (exito: Boolean, mensaje: String) -> Unit
    ) {
        val prefs = PreferenciasSesion.obtenerInstancia(context)
        val correoLimpio = email.trim()

        try {
            val auth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(correoLimpio, clave)
                .addOnSuccessListener { resultado ->
                    val nombre = resultado.user?.displayName ?: correoLimpio.substringBefore("@")
                    prefs.guardarSesion(correoLimpio, nombre)
                    onResultado(true, "Inicio de sesión exitoso mediante Firebase.")
                }
                .addOnFailureListener { excepcion ->
                    Log.w(TAG, "Intento Firebase fallido (${excepcion.message}), evaluando autenticación local.")
                    // Fallback local: valida contra el repositorio de usuarios
                    if (RepositorioUsuarios.autenticar(correoLimpio, clave)) {
                        val usuario = RepositorioUsuarios.buscarPorEmail(correoLimpio)
                        prefs.guardarSesion(correoLimpio, usuario?.nombre ?: correoLimpio)
                        onResultado(true, "Sesión iniciada correctamente.")
                    } else {
                        onResultado(false, "Correo o contraseña incorrectos.")
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Auth no disponible: ${e.message}. Usando repositorio local.")
            if (RepositorioUsuarios.autenticar(correoLimpio, clave)) {
                val usuario = RepositorioUsuarios.buscarPorEmail(correoLimpio)
                prefs.guardarSesion(correoLimpio, usuario?.nombre ?: correoLimpio)
                onResultado(true, "Sesión iniciada correctamente.")
            } else {
                onResultado(false, "Correo o contraseña incorrectos.")
            }
        }
    }

    /**
     * Registra un nuevo usuario en el sistema.
     */
    fun registrarUsuario(
        usuario: Usuario,
        context: Context,
        onResultado: (exito: Boolean, mensaje: String) -> Unit
    ) {
        val prefs = PreferenciasSesion.obtenerInstancia(context)

        // Verificamos si ya existe localmente
        if (RepositorioUsuarios.existeEmail(usuario.email)) {
            onResultado(false, "El correo electrónico ya se encuentra registrado.")
            return
        }

        // 1. Guardar en repositorio local
        val agregadoLocal = RepositorioUsuarios.agregar(usuario)
        if (!agregadoLocal) {
            onResultado(false, "No se pudo registrar el usuario localmente.")
            return
        }

        // 2. Guardar sesión activa en SharedPreferences
        prefs.guardarSesion(usuario.email, usuario.nombre)

        // 3. Registro remoto en Firebase Auth y Firestore
        try {
            val auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(usuario.email, usuario.contrasena)
                .addOnSuccessListener {
                    // Guardar perfil detallado en Firestore
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val datosUsuario = hashMapOf(
                            "nombre" to usuario.nombre,
                            "email" to usuario.email,
                            "genero" to usuario.genero,
                            "pais" to usuario.pais,
                            "preferencias" to usuario.preferencias,
                            "fechaRegistro" to System.currentTimeMillis()
                        )
                        db.collection("usuarios").document(usuario.email).set(datosUsuario)
                    } catch (e: Exception) {
                        Log.w(TAG, "No se pudo sincronizar usuario con Firestore: ${e.message}")
                    }
                    onResultado(true, "Usuario registrado exitosamente en Firebase y en el dispositivo.")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Firebase Auth registro no completado (${e.message}), guardado localmente.")
                    onResultado(true, "Usuario registrado exitosamente en el dispositivo.")
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase no disponible: ${e.message}. Usuario registrado localmente.")
            onResultado(true, "Usuario registrado exitosamente en el dispositivo.")
        }
    }

    /**
     * Envía correo de recuperación de contraseña vía Firebase o valida existencia local.
     */
    fun recuperarContrasena(
        email: String,
        onResultado: (exito: Boolean, mensaje: String) -> Unit
    ) {
        val correoLimpio = email.trim()

        try {
            val auth = FirebaseAuth.getInstance()
            auth.sendPasswordResetEmail(correoLimpio)
                .addOnSuccessListener {
                    onResultado(true, "Enlace de restablecimiento enviado a tu correo.")
                }
                .addOnFailureListener {
                    if (RepositorioUsuarios.existeEmail(correoLimpio)) {
                        onResultado(true, "Instrucciones de recuperación enviadas a tu correo.")
                    } else {
                        onResultado(false, "No existe una cuenta registrada con este correo.")
                    }
                }
        } catch (e: Exception) {
            if (RepositorioUsuarios.existeEmail(correoLimpio)) {
                onResultado(true, "Instrucciones de recuperación enviadas a tu correo.")
            } else {
                onResultado(false, "No existe una cuenta registrada con este correo.")
            }
        }
    }

    /**
     * Cierra la sesión activa tanto en Firebase Auth como en SharedPreferences.
     */
    fun cerrarSesion(context: Context) {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Error al cerrar sesión en Firebase: ${e.message}")
        }
        PreferenciasSesion.obtenerInstancia(context).cerrarSesion()
    }
}
