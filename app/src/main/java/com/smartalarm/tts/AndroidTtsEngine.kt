package com.smartalarm.tts

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidTtsEngine(private val context: Context) : TtsEngine {

    override val name: String = "Android"

    private val initialized = AtomicBoolean(false)
    private val initResult = CompletableDeferred<Boolean>()
    private val ttsRef = AtomicReference<TextToSpeech?>()

    override suspend fun initialize() {
        if (initialized.get()) return

        val existing = ttsRef.get()
        if (existing == null) {
            var createdTts: TextToSpeech? = null
            val tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val defaultLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        context.resources.configuration.locales[0]
                    } else {
                        @Suppress("DEPRECATION")
                        context.resources.configuration.locale
                    }
                    val engine = createdTts
                    val result = if (engine != null) setLocale(engine, defaultLocale) else false
                    initialized.set(true)
                    initResult.complete(result)
                } else {
                    Log.e(TAG, "TextToSpeech init failed with status=$status")
                    initResult.complete(false)
                }
            }
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {}
                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "TextToSpeech onError id=$utteranceId")
                }
                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.e(TAG, "TextToSpeech onError id=$utteranceId code=$errorCode")
                }
            })
            createdTts = tts
            ttsRef.set(tts)
        }

        val success = initResult.await()
        if (!success) {
            throw IllegalStateException("TextToSpeech initialization failed")
        }
    }

    private fun setLocale(tts: TextToSpeech, locale: Locale): Boolean {
        return when (val result = tts.setLanguage(locale)) {
            TextToSpeech.LANG_AVAILABLE, TextToSpeech.LANG_COUNTRY_AVAILABLE, TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> true
            else -> {
                Log.w(TAG, "Requested locale $locale unavailable (code=$result), falling back to default")
                tts.setLanguage(Locale.US)
                true
            }
        }
    }

    override suspend fun speak(text: String) {
        val sanitized = text.trim()
        if (sanitized.isEmpty()) return
        val tts = ttsRef.get() ?: throw IllegalStateException("TextToSpeech not initialized")
        withContext(Dispatchers.Main) {
            val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Bundle().apply { putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f) }
            } else {
                null
            }
            val utteranceId = System.currentTimeMillis().toString()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(sanitized, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            } else {
                @Suppress("DEPRECATION")
                tts.speak(sanitized, TextToSpeech.QUEUE_FLUSH, hashMapOf(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to utteranceId))
            }
        }
    }

    override fun stop() {
        ttsRef.get()?.stop()
    }

    override fun close() {
        ttsRef.getAndSet(null)?.shutdown()
    }

    companion object {
        private const val TAG = "AndroidTtsEngine"
    }
}
