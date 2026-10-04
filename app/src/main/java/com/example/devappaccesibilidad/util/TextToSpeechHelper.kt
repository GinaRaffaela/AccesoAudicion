package com.example.devappaccesibilidad.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Administrador de Síntesis de Voz (Text-to-Speech) diseñado para permitir
 * que una persona con discapacidad auditiva o del habla escriba cualquier texto
 * y el teléfono lo reproduzca con voz audible clara para sus interlocutores.
 */
class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var estaListo = false

    init {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d("TextToSpeechHelper", "Inicio de reproducción de voz")
            }

            override fun onDone(utteranceId: String?) {
                Log.d("TextToSpeechHelper", "Fin de reproducción de voz")
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.e("TextToSpeechHelper", "Error en reproducción de voz")
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val resultado = tts?.setLanguage(Locale.forLanguageTag("es-ES"))
            if (resultado == TextToSpeech.LANG_MISSING_DATA || resultado == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(0.95f)
            tts?.setPitch(1.0f)
            estaListo = true
        } else {
            Log.e("TextToSpeechHelper", "Fallo al inicializar TextToSpeech (status: $status)")
            estaListo = false
        }
    }

    /**
     * Pronuncia en voz alta el texto solicitado.
     */
    fun hablar(texto: String) {
        if (estaListo && texto.isNotBlank()) {
            tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "acceso_audicion_utterance")
        }
    }

    /**
     * Detiene cualquier locución en curso.
     */
    fun detener() {
        if (estaListo) {
            tts?.stop()
        }
    }

    /**
     * Libera recursos al destruir la vista o actividad.
     */
    fun liberar() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("TextToSpeechHelper", "Error al liberar TTS: ${e.message}")
        }
        tts = null
        estaListo = false
    }
}
