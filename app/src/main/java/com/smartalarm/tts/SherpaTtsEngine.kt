package com.smartalarm.tts

import android.content.Context
import android.util.Log

/**
 * Placeholder implementation that wires up once SherpaTTS dependency is added.
 * For now this class throws during initialization, forcing the manager to fall back
 * to Android's built-in TextToSpeech engine.
 */
class SherpaTtsEngine(private val context: Context) : TtsEngine {

    override val name: String = "SherpaTTS"
    private var initialized = false

    override suspend fun initialize() {
        Log.w(TAG, "SherpaTTS integration is not yet implemented; falling back")
        throw UnsupportedOperationException("SherpaTTS not bundled")
    }

    override suspend fun speak(text: String) {
        if (!initialized) throw IllegalStateException("SherpaTTS not initialized")
    }

    override fun stop() {}

    override fun close() {}

    companion object {
        private const val TAG = "SherpaTtsEngine"
    }
}
