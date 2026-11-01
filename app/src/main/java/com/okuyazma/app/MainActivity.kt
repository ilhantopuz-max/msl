package com.okuyazma.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.okuyazma.app.audio.Reader
import com.okuyazma.app.audio.Speaker
import com.okuyazma.app.data.Store
import com.okuyazma.app.ui.App

class MainActivity : ComponentActivity() {
    private lateinit var speaker: Speaker
    private lateinit var reader: Reader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speaker = Speaker(this)
        reader = Reader(this)
        val store = Store(this)
        setContent { App(store, speaker, reader) }
    }

    override fun onStop() {
        speaker.stop(); reader.stop()
        super.onStop()
    }

    override fun onDestroy() {
        speaker.shutdown(); reader.stop()
        super.onDestroy()
    }
}
