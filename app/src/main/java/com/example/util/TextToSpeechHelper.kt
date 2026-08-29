package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var isSpeaking = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    val result = engine.setLanguage(Locale.US)
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.setPitch(1.05f)
                        engine.setSpeechRate(0.95f)
                        isInitialized = true
                        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                isSpeaking = true
                            }
                            override fun onDone(utteranceId: String?) {
                                isSpeaking = false
                            }
                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                isSpeaking = false
                            }
                        })
                    }
                }
            }
        }
    }

    fun speak(text: String, utteranceId: String = "mealy_step") {
        if (!isInitialized) return
        stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            if (tts?.isSpeaking == true) {
                tts?.stop()
            }
            isSpeaking = false
        } catch (e: Exception) {
            Log.e("MealyTTS", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e("MealyTTS", "Error shutting down TTS", e)
        }
    }
}
