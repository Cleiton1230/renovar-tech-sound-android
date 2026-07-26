package com.renovartech.soundapp.karaoke

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Motor de audio do Karaoke.
 *
 * O problema que este motor resolve: no site (rodando no navegador), ao pedir acesso
 * ao microfone via getUserMedia, o Chrome no Android solicita foco de audio de forma
 * que faz o sistema PAUSAR outros apps (YouTube, Spotify, etc.). Isso e um
 * comportamento do navegador/SO e nao pode ser mudado por JavaScript.
 *
 * Aqui, por ser um app nativo, pedimos o foco de audio como
 * AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK: isso informa ao Android que outros apps
 * podem apenas ABAIXAR o volume (duck) em vez de pausar. Apps bem comportados
 * como YouTube e Spotify respeitam esse pedido e continuam tocando, so mais baixo.
 */
class KaraokeEngine(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var job: Job? = null
    private var focusRequest: AudioFocusRequest? = null
    private var running = false

    private val sampleRate = 44100
    private val recordChannel = AudioFormat.CHANNEL_IN_MONO
    private val playChannel = AudioFormat.CHANNEL_OUT_MONO
    private val audioEncoding = AudioFormat.ENCODING_PCM_16BIT

    val isActive: Boolean
        get() = running

    @SuppressLint("MissingPermission")
    fun start() {
        if (running) return

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes)
            .setOnAudioFocusChangeListener { }
            .build()
        focusRequest = request
        audioManager.requestAudioFocus(request)

        val minBufferSizeRecord = AudioRecord.getMinBufferSize(sampleRate, recordChannel, audioEncoding)
        val minBufferSizePlay = AudioTrack.getMinBufferSize(sampleRate, playChannel, audioEncoding)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            recordChannel,
            audioEncoding,
            minBufferSizeRecord * 2
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(audioEncoding)
                    .setChannelMask(playChannel)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSizePlay * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioRecord?.startRecording()
        audioTrack?.play()
        running = true

        job = CoroutineScope(Dispatchers.Default).launch {
            val buffer = ShortArray(minBufferSizeRecord)
            while (running) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    audioTrack?.write(buffer, 0, read)
                }
            }
        }
    }

    fun stop() {
        if (!running) return
        running = false
        job?.cancel()
        job = null

        audioRecord?.apply {
            stop()
            release()
        }
        audioTrack?.apply {
            stop()
            release()
        }
        audioRecord = null
        audioTrack = null

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }
}
