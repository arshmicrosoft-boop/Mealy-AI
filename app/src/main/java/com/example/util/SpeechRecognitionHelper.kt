package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * State of the speech recognizer
 */
data class SpeechState(
    val isListening: Boolean = false,
    val rmsLevel: Float = 0f,
    val partialText: String = "",
    val lastResultText: String = "",
    val error: String? = null
)

/**
 * Helper class managing Android SpeechRecognizer for interactive hands-free voice cooking.
 */
class SpeechRecognitionHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isAvailable = false

    private val _state = MutableStateFlow(SpeechState())
    val state: StateFlow<SpeechState> = _state.asStateFlow()

    private var onCommandRecognizedListener: ((String) -> Unit)? = null

    init {
        try {
            isAvailable = SpeechRecognizer.isRecognitionAvailable(context)
            if (isAvailable) {
                initRecognizer()
            }
        } catch (e: Exception) {
            Log.e("MealioSpeech", "Failed to check speech recognition availability", e)
        }
    }

    private fun initRecognizer() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _state.value = _state.value.copy(
                            isListening = true,
                            error = null,
                            partialText = ""
                        )
                    }

                    override fun onBeginningOfSpeech() {
                        _state.value = _state.value.copy(isListening = true)
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS dB typically ranging from -2 to 10
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
                        _state.value = _state.value.copy(rmsLevel = normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _state.value = _state.value.copy(isListening = false)
                    }

                    override fun onError(errorCode: Int) {
                        val errorMsg = when (errorCode) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out"
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_NETWORK -> "Network issue"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission needed"
                            else -> "Voice recognition issue ($errorCode)"
                        }
                        _state.value = _state.value.copy(
                            isListening = false,
                            rmsLevel = 0f,
                            error = errorMsg
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        _state.value = _state.value.copy(
                            isListening = false,
                            rmsLevel = 0f,
                            lastResultText = text,
                            partialText = ""
                        )
                        if (text.isNotBlank()) {
                            onCommandRecognizedListener?.invoke(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        _state.value = _state.value.copy(partialText = text)
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            Log.e("MealioSpeech", "Failed to initialize SpeechRecognizer", e)
        }
    }

    fun setOnCommandRecognizedListener(listener: (String) -> Unit) {
        this.onCommandRecognizedListener = listener
    }

    fun startListening() {
        try {
            if (speechRecognizer == null) {
                initRecognizer()
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }
            _state.value = _state.value.copy(
                isListening = true,
                error = null,
                partialText = ""
            )
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("MealioSpeech", "Error starting listening", e)
            _state.value = _state.value.copy(
                isListening = false,
                error = "Cannot access speech engine"
            )
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _state.value = _state.value.copy(isListening = false, rmsLevel = 0f)
        } catch (e: Exception) {
            Log.e("MealioSpeech", "Error stopping listening", e)
        }
    }

    fun cancel() {
        try {
            speechRecognizer?.cancel()
            _state.value = _state.value.copy(isListening = false, rmsLevel = 0f)
        } catch (e: Exception) {
            Log.e("MealioSpeech", "Error canceling listening", e)
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e("MealioSpeech", "Error destroying SpeechRecognizer", e)
        }
    }
}
