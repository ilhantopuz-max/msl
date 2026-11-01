package com.okuyazma.app.audio

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.okuyazma.app.data.TR

private val PRAISES = listOf("Aferin!", "Harikasın!", "Bravo!", "Süpersin!", "Çok güzel!", "Mükemmel!")

/**
 * Seslendirme. Önce res/raw içinde ilgili anahtarla kayıtlı ses dosyası aranır (profesyonel çocuk sesi),
 * bulunamazsa cihazın Türkçe Text-to-Speech motoru kullanılır (çevrimdışı çalışır).
 */
class Speaker(private val ctx: Context) {
    private var tts: TextToSpeech? = null
    private var player: MediaPlayer? = null
    var enabled = true
    var greeted = false
    var turkishOk by mutableStateOf(true)

    init {
        tts = TextToSpeech(ctx) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val r = tts?.setLanguage(TR)
                turkishOk = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
                tts?.setSpeechRate(0.8f)
                tts?.setPitch(1.15f)
            } else turkishOk = false
        }
    }

    fun say(text: String, key: String? = null) {
        if (!enabled) return
        stop()
        val id = if (key != null) ctx.resources.getIdentifier(key, "raw", ctx.packageName) else 0
        if (id != 0) {
            player = MediaPlayer.create(ctx, id)?.apply {
                setOnCompletionListener { try { it.release() } catch (e: Exception) {} }
                start()
            }
        } else {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "okuyazma")
        }
    }

    fun praise() {
        chime()
        say(PRAISES.random(), "aferin")
    }

    private fun chime() {
        if (!enabled) return
        try {
            val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
            tg.startTone(ToneGenerator.TONE_PROP_ACK, 250)
            Handler(Looper.getMainLooper()).postDelayed({ tg.release() }, 600)
        } catch (e: Exception) {}
    }

    fun stop() {
        tts?.stop()
        try { player?.release() } catch (e: Exception) {}
        player = null
    }

    fun shutdown() { stop(); tts?.shutdown(); tts = null }
}

/** Basit ses tanıma (sesli okuma takibi). Ebeveyn panelinden açılmadıkça kullanılmaz. */
class Reader(private val ctx: Context) {
    private var rec: SpeechRecognizer? = null
    var listening by mutableStateOf(false)
    val available: Boolean get() = SpeechRecognizer.isRecognitionAvailable(ctx)

    fun start(onResult: (List<String>) -> Unit, onFail: () -> Unit) {
        stop()
        val r = SpeechRecognizer.createSpeechRecognizer(ctx)
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onError(error: Int) { listening = false; onFail() }
            override fun onResults(results: Bundle?) {
                listening = false
                onResult(results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty())
            }
        })
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }
        rec = r
        listening = true
        r.startListening(i)
    }

    fun stop() {
        try { rec?.destroy() } catch (e: Exception) {}
        rec = null
        listening = false
    }
}
