package com.example.healthapp.data.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class AudioRecorder(
    private val sampleRate: Int = 16000
) {
    private var record: AudioRecord? = null
    private var job: Job? = null
    @Volatile private var recording = false
    private var baos: ByteArrayOutputStream? = null

    fun start(scope: CoroutineScope) {
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        record = audioRecord
        baos = ByteArrayOutputStream()
        recording = true
        audioRecord.startRecording()
        job = scope.launch(Dispatchers.IO) {
            val buf = ByteArray(bufferSize)
            while (recording) {
                val read = audioRecord.read(buf, 0, buf.size)
                if (read > 0) baos?.write(buf, 0, read)
            }
        }
    }

    suspend fun stopAndGetPcm(): ByteArray {
        recording = false
        job?.cancel()
        try {
            record?.stop()
        } catch (_: Throwable) {}
        record?.release()
        record = null
        val out = baos?.toByteArray() ?: ByteArray(0)
        baos?.reset()
        baos = null
        return out
    }
}

