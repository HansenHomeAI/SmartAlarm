package com.smartalarm.tts

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TtsManager(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AutoCloseable {

    private val mutex = Mutex()
    private var engine: TtsEngine? = null
    private val _activeEngine = MutableStateFlow(TtsEngineType.None)

    val activeEngine: StateFlow<TtsEngineType> = _activeEngine

    suspend fun speak(text: String) {
        val sanitized = text.trim()
        if (sanitized.isEmpty()) return

        val engine = ensureEngine()
        withContext(ioDispatcher) {
            engine.speak(sanitized)
        }
    }

    suspend fun stop() {
        mutex.withLock {
            engine?.stop()
        }
    }

    override fun close() {
        val toClose = runBlocking {
            mutex.withLock {
                val current = engine
                engine = null
                _activeEngine.value = TtsEngineType.None
                current
            }
        }
        toClose?.close()
    }

    private suspend fun ensureEngine(): TtsEngine {
        return mutex.withLock {
            engine?.let { return it }

            val sherpa = SherpaTtsEngine(context)
            try {
                sherpa.initialize()
                _activeEngine.value = TtsEngineType.Sherpa
                engine = sherpa
                sherpa
            } catch (t: Throwable) {
                sherpa.close()
                val androidEngine = AndroidTtsEngine(context)
                androidEngine.initialize()
                _activeEngine.value = TtsEngineType.AndroidFallback
                engine = androidEngine
                androidEngine
            }
        }
    }

    enum class TtsEngineType { None, Sherpa, AndroidFallback }
}
