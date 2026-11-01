package com.okuyazma.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.okuyazma.app.audio.Reader
import com.okuyazma.app.audio.Speaker
import com.okuyazma.app.data.*

sealed interface Screen {
    data object Home : Screen
    data class Map(val section: Section) : Screen
    data class Letter(val i: Int) : Screen
    data class Syl(val i: Int) : Screen
    data class Read(val i: Int) : Screen
    data object Parent : Screen
}

private fun parentOf(s: Screen): Screen = when (s) {
    is Screen.Letter -> Screen.Map(Section.LETTERS)
    is Screen.Syl -> Screen.Map(Section.SYLLABLES)
    is Screen.Read -> Screen.Map(Section.READING)
    else -> Screen.Home
}

@Composable
fun App(store: Store, speaker: Speaker, reader: Reader) {
    SideEffect { speaker.enabled = store.soundOn }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    fun go(s: Screen) { speaker.stop(); reader.stop(); screen = s }

    KidTheme(store.themeMode) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize()) {
                BackHandler(enabled = screen != Screen.Home) { go(parentOf(screen)) }
                key(screen) {
                    when (val s = screen) {
                        Screen.Home -> HomeScreen(store, speaker, onOpen = { go(Screen.Map(it)) }, onParent = { go(Screen.Parent) })
                        Screen.Parent -> ParentScreen(store) { go(Screen.Home) }
                        is Screen.Map -> when (s.section) {
                            Section.LETTERS -> MapScreen("Harfler", LETTERS.map { "${it.upper} ${it.lower}" }, "L", store, speaker,
                                { go(Screen.Letter(it)) }, { go(Screen.Home) })
                            Section.SYLLABLES -> MapScreen("Heceler", SYLLABLE_LESSONS.map { it.items.take(2).joinToString(" ") }, "S", store, speaker,
                                { go(Screen.Syl(it)) }, { go(Screen.Home) })
                            Section.READING -> MapScreen("Okuma", READ_LESSONS.map { l -> l.words.firstOrNull()?.emoji ?: "💬" }, "R", store, speaker,
                                { go(Screen.Read(it)) }, { go(Screen.Home) })
                        }
                        is Screen.Letter -> LetterScreen(s.i, store, speaker, reader, ::go) { go(parentOf(s)) }
                        is Screen.Syl -> SyllableScreen(s.i, store, speaker, reader, ::go) { go(parentOf(s)) }
                        is Screen.Read -> ReadScreen(s.i, store, speaker, reader, ::go) { go(parentOf(s)) }
                    }
                }
            }
        }
    }
}
