package com.example.cornerrace.feedback

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

enum class Sfx { SELECT, STEP, JUMP, TARGET, WIN }

/**
 * Tiny synthesized sound set so the app ships without audio assets.
 * Each effect is rendered once into a static [AudioTrack] and replayed on a background thread.
 */
class SoundEngine {
    private val sampleRate = 44_100
    private val executor = Executors.newSingleThreadExecutor()
    private val tracks = HashMap<Sfx, AudioTrack>()

    @Volatile
    private var released = false

    fun play(sfx: Sfx) {
        if (released) return
        executor.execute {
            if (released) return@execute
            runCatching {
                val track = tracks.getOrPut(sfx) { buildTrack(render(sfx)) }
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) track.stop()
                track.reloadStaticData()
                track.play()
            }
        }
    }

    fun release() {
        released = true
        executor.execute {
            tracks.values.forEach { runCatching { it.release() } }
            tracks.clear()
        }
        executor.shutdown()
    }

    private fun buildTrack(samples: ShortArray): AudioTrack {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(samples, 0, samples.size)
        return track
    }

    private fun render(sfx: Sfx): ShortArray = when (sfx) {
        Sfx.SELECT -> tone(durationMs = 55, volume = 0.16f) { t -> partials(t, 1320.0, 2640.0 to 0.2) }
        Sfx.STEP -> tone(durationMs = 80, volume = 0.24f, decay = 38.0) { t -> partials(t, 330.0, 660.0 to 0.35, 1210.0 to 0.12) }
        Sfx.JUMP -> tone(durationMs = 170, volume = 0.22f, decay = 16.0) { t ->
            // Upward glide: phase is the integral of a linearly rising frequency.
            val f0 = 380.0
            val f1 = 720.0
            val dur = 0.17
            val phase = 2 * PI * (f0 * t + (f1 - f0) * t * t / (2 * dur))
            sin(phase) + 0.25 * sin(2 * phase)
        }
        Sfx.TARGET -> sequence(listOf(660.0, 990.0), noteMs = 110, volume = 0.2f)
        Sfx.WIN -> sequence(listOf(523.25, 659.25, 783.99, 1046.5), noteMs = 150, volume = 0.22f, tailMs = 260)
    }

    private fun partials(t: Double, base: Double, vararg overtones: Pair<Double, Double>): Double {
        var v = sin(2 * PI * base * t)
        for ((freq, amp) in overtones) v += amp * sin(2 * PI * freq * t)
        return v
    }

    private inline fun tone(
        durationMs: Int,
        volume: Float,
        decay: Double = 30.0,
        wave: (Double) -> Double
    ): ShortArray {
        val count = sampleRate * durationMs / 1000
        val attack = sampleRate * 0.004
        return ShortArray(count) { i ->
            val t = i.toDouble() / sampleRate
            val env = min(1.0, i / attack) * exp(-decay * t)
            (wave(t) * env * volume * Short.MAX_VALUE).toInt().coerceIn(-32767, 32767).toShort()
        }
    }

    private fun sequence(notes: List<Double>, noteMs: Int, volume: Float, tailMs: Int = 120): ShortArray {
        val noteSamples = sampleRate * noteMs / 1000
        val total = noteSamples * (notes.size - 1) + sampleRate * (noteMs + tailMs) / 1000
        val out = DoubleArray(total)
        notes.forEachIndexed { index, freq ->
            val start = index * noteSamples
            for (i in start until total) {
                val t = (i - start).toDouble() / sampleRate
                val env = min(1.0, t / 0.005) * exp(-9.0 * t)
                out[i] += (sin(2 * PI * freq * t) + 0.18 * sin(4 * PI * freq * t)) * env
            }
        }
        return ShortArray(total) { i ->
            (out[i] * volume * 0.8 * Short.MAX_VALUE).toInt().coerceIn(-32767, 32767).toShort()
        }
    }
}
