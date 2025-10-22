package com.smartalarm.tts

interface TtsEngine : AutoCloseable {
    val name: String

    suspend fun initialize()

    suspend fun speak(text: String)

    fun stop()

    override fun close()
}
