package com.smartalarm.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.k2fsa.sherpa.onnx.GeneratedAudio
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.getOfflineTtsConfig
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SherpaTtsEngine(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TtsEngine {

    override val name: String = "SherpaTTS"

    private val initGuard = AtomicBoolean(false)
    private val engineMutex = Mutex()
    private val playbackMutex = Mutex()

    private var offlineTts: OfflineTts? = null
    private var audioTrack: AudioTrack? = null

    override suspend fun initialize() {
        ensureEngine()
    }

    override suspend fun speak(text: String) {
        val sanitized = text.trim()
        if (sanitized.isEmpty()) return

        val engine = ensureEngine()

        // stop any active playback before generating a new utterance
        stopPlayback()

        val audio = withContext(ioDispatcher) {
            engine.generate(text = sanitized)
        }

        playAudio(audio)
    }

    override fun stop() {
        runBlocking { stopPlayback() }
    }

    override fun close() {
        runBlocking { stop() }
        runBlocking {
            engineMutex.withLock {
                offlineTts?.free()
                offlineTts = null
                initGuard.set(false)
            }
        }
    }

    private suspend fun ensureEngine(): OfflineTts = engineMutex.withLock {
        if (!initGuard.get()) {
            Log.i(TAG, "Initializing SherpaTTS engine")
            val config = getOfflineTtsConfig(
                modelDir = MODEL_DIR,
                modelName = MODEL_NAME,
                acousticModelName = "",
                vocoder = "",
                voices = "",
                lexicon = "",
                dataDir = DATA_DIR,
                dictDir = "",
                ruleFsts = "",
                ruleFars = "",
                numThreads = 2,
                isKitten = false
            )
            offlineTts = OfflineTts(context.assets, config)
            initGuard.set(true)
            Log.i(TAG, "SherpaTTS engine ready")
        }
        offlineTts!!
    }

    private suspend fun playAudio(audio: GeneratedAudio) {
        val (track, durationMs) = playbackMutex.withLock {
            releaseAudioTrackLocked()

            val samples = audio.samples
            val bufferSize = samples.size * Float.SIZE_BYTES

            val newTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(audio.sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(bufferSize)
                .build()

            val written = newTrack.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
            if (written <= 0) {
                newTrack.release()
                throw IllegalStateException("Failed to buffer audio for playback (code=$written)")
            }

            newTrack.play()
            audioTrack = newTrack

            val duration = (samples.size.toDouble() / audio.sampleRate.toDouble()) * 1000.0
            newTrack to duration
        }

        withContext(ioDispatcher) {
            val totalDelay = (durationMs + PLAYBACK_MARGIN_MS).toLong()
            kotlinx.coroutines.delay(totalDelay)
            playbackMutex.withLock {
                // release only if still current
                if (audioTrack === track) {
                    releaseAudioTrackLocked()
                } else {
                    try {
                        track.stop()
                    } catch (_: IllegalStateException) {
                    }
                    track.flush()
                    track.release()
                }
            }
        }
    }

    private suspend fun stopPlayback() {
        playbackMutex.withLock {
            releaseAudioTrackLocked()
        }
    }

    private fun releaseAudioTrackLocked() {
        audioTrack?.let { track ->
            try {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
            } catch (ignored: IllegalStateException) {
                // no-op
            }
            track.flush()
            track.release()
        }
        audioTrack = null
    }

    companion object {
        private const val TAG = "SherpaTtsEngine"
        private const val MODEL_DIR = "tts/vits-icefall-en_US-ljspeech-low"
        private const val MODEL_NAME = "model.onnx"
        private const val DATA_DIR = "$MODEL_DIR/espeak-ng-data"
        private const val PLAYBACK_MARGIN_MS = 120L
    }
}
