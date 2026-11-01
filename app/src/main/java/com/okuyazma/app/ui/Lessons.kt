@file:OptIn(ExperimentalLayoutApi::class)

package com.okuyazma.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.okuyazma.app.audio.Reader
import com.okuyazma.app.audio.Speaker
import com.okuyazma.app.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil

/**
 * Tüm derslerin ortak akışı: öğren → alıştırma → (sesli okuma) → bitiş.
 * Bitişte "Tekrar et", "Önceki derse dön", "Sonraki ders" butonları gösterilir.
 */
@Composable
fun LessonFlow(
    title: String, lessonId: String, store: Store, speaker: Speaker, reader: Reader,
    learn: @Composable (next: () -> Unit) -> Unit,
    makeQuestions: (() -> List<Question>)?, readItems: List<String>?,
    hasPrev: Boolean, hasNext: Boolean, onPrev: () -> Unit, onNext: () -> Unit, onBack: () -> Unit,
) {
    var run by remember { mutableIntStateOf(0) }
    var phase by remember { mutableIntStateOf(0) }   // 0 öğren, 1 alıştırma, 2 sesli okuma, 3 bitiş
    var stars by remember { mutableIntStateOf(0) }
    val questions = remember(run) { makeQuestions?.invoke() }
    val maxStars = (questions?.size ?: 0) + (readItems?.size ?: 0)

    fun advance(from: Int) {
        phase = when {
            from < 1 && questions != null -> 1
            from < 2 && readItems != null -> 2
            else -> 3
        }
    }
    LaunchedEffect(phase) { if (phase == 3) store.finish(lessonId, stars) }

    Column(Modifier.fillMaxSize()) {
        TopBar(title, onBack) {
            Text("⭐ ${store.todayStars}/${store.goal}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (phase) {
                0 -> learn { advance(0) }
                1 -> QuizRunner(questions!!, speaker) { s -> stars += s; advance(1) }
                2 -> ReadAloud(readItems!!, reader, store, speaker, onStar = { stars++ }) { advance(2) }
                else -> FinishPanel(
                    stars, maxStars, speaker, hasPrev, hasNext,
                    onRepeat = { run++; stars = 0; phase = 0 }, onPrev = onPrev, onNext = onNext, onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun FinishPanel(
    stars: Int, max: Int, speaker: Speaker, hasPrev: Boolean, hasNext: Boolean,
    onRepeat: () -> Unit, onPrev: () -> Unit, onNext: () -> Unit, onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { speaker.say("Aferin! Dersi bitirdin!") }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
        ) {
            Text("🎉", fontSize = 80.sp)
            Text("Aferin!", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text("⭐ $stars / $max", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            KidButton("🔁 Tekrar et", onRepeat, Modifier.fillMaxWidth(0.8f), Pastel.Sky)
            if (hasNext) KidButton("Sonraki ders ➡", onNext, Modifier.fillMaxWidth(0.8f), Pastel.Mint)
            if (hasPrev) KidButton("⬅ Önceki derse dön", onPrev, Modifier.fillMaxWidth(0.8f), Pastel.Peach)
            KidButton("🏠 Derslere dön", onBack, Modifier.fillMaxWidth(0.8f), Pastel.Lavender)
        }
        RewardOverlay()
    }
}

// ---------------------------------------------------------------- sesli okuma

private fun norm(s: String) = s.lowercase(TR).filter { it.isLetter() || it == ' ' }.trim()

private fun matches(target: String, heard: List<String>): Boolean {
    val t = norm(target).split(" ").filter { it.isNotEmpty() }
    if (t.isEmpty()) return true
    val need = ceil(t.size * 0.6).toInt().coerceAtLeast(1)
    return heard.any { h ->
        val hw = norm(h).split(" ")
        t.count { it in hw } >= need
    }
}

@Composable
fun ReadAloud(
    items: List<String>, reader: Reader, store: Store, speaker: Speaker,
    onStar: () -> Unit, onFinished: () -> Unit,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var idx by remember { mutableIntStateOf(0) }
    var tries by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf("") }
    var reward by remember { mutableStateOf(false) }
    val target = items[idx]

    fun success() {
        if (reward) return
        reward = true
        onStar()
        speaker.praise()
        scope.launch {
            delay(1300)
            reward = false; tries = 0; msg = ""
            if (idx + 1 < items.size) idx++ else onFinished()
        }
    }
    fun listen() {
        speaker.stop()
        reader.start(
            onResult = { res ->
                if (matches(target, res)) success()
                else { tries++; msg = "Bir daha dene! 🎤"; speaker.say("Bir daha dene") }
            },
            onFail = { tries++; msg = "Seni duyamadım, tekrar dene 🎤" },
        )
    }
    val perm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) listen() else msg = "Mikrofon izni gerekli"
    }
    LaunchedEffect(idx) { speaker.say("Şimdi sen oku. $target") }
    DisposableEffect(Unit) { onDispose { reader.stop() } }

    val micReady = store.micOn && reader.available
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text("Şimdi sen oku! ${idx + 1}/${items.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                target, fontSize = if (target.length > 12) 34.sp else 56.sp, fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                KidButton("🔊 Dinle", { speaker.say(target) }, color = Pastel.Sky)
                if (micReady) {
                    KidButton(if (reader.listening) "🎤 Dinliyorum..." else "🎤 Oku", {
                        val ok = ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        if (ok) listen() else perm.launch(Manifest.permission.RECORD_AUDIO)
                    }, color = Pastel.Pink)
                }
                if (!micReady || tries >= 2) KidButton("✅ Okudum", { success() }, color = Pastel.Mint)
            }
            if (msg.isNotEmpty()) Text(msg, fontSize = 20.sp, textAlign = TextAlign.Center)
        }
        if (reward) RewardOverlay()
    }
}

// ---------------------------------------------------------------- ders ekranları

@Composable
fun LetterScreen(i: Int, store: Store, speaker: Speaker, reader: Reader, go: (Screen) -> Unit, back: () -> Unit) {
    val item = LETTERS[i]
    LessonFlow(
        title = "Harf ${item.upper}", lessonId = "L$i", store = store, speaker = speaker, reader = reader,
        learn = { next ->
            var replay by remember { mutableIntStateOf(1) }
            LaunchedEffect(Unit) { speaker.say("${item.upper} harfi. ${item.word}", audioKey("l", item.lower)) }
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("${item.upper} ${item.lower}", fontSize = 88.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LetterTracer(item.upper, replay, MaterialTheme.colorScheme.secondary)
                    LetterTracer(item.lower, replay, MaterialTheme.colorScheme.tertiary)
                }
                Text(item.emoji, fontSize = 84.sp)
                Text(item.word, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KidButton("🔊 Dinle", { speaker.say("${item.upper} harfi. ${item.word}", audioKey("l", item.lower)) }, color = Pastel.Sky)
                    KidButton("✍️ Yazılışı gör", { replay++ }, color = Pastel.Yellow)
                }
                KidButton("Alıştırmaya geç ➡", next, Modifier.fillMaxWidth(0.8f), Pastel.Mint)
            }
        },
        makeQuestions = { letterQuestions(item) }, readItems = null,
        hasPrev = i > 0, hasNext = i + 1 < LETTERS.size,
        onPrev = { go(Screen.Letter(i - 1)) }, onNext = { go(Screen.Letter(i + 1)) }, onBack = back,
    )
}

@Composable
fun SyllableScreen(i: Int, store: Store, speaker: Speaker, reader: Reader, go: (Screen) -> Unit, back: () -> Unit) {
    val lesson = SYLLABLE_LESSONS[i]
    LessonFlow(
        title = lesson.title, lessonId = "S$i", store = store, speaker = speaker, reader = reader,
        learn = { next ->
            LaunchedEffect(Unit) { speaker.say("Hecelere dokun, dinle ve oku!") }
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Hecelere dokun, dinle ve oku!", fontSize = 22.sp, textAlign = TextAlign.Center)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    lesson.items.forEach { s ->
                        Box(
                            Modifier.size(width = 104.dp, height = 84.dp).clip(RoundedCornerShape(24.dp)).background(Pastel.Sky)
                                .clickable { speaker.say(s, audioKey("s", s)) },
                            contentAlignment = Alignment.Center,
                        ) { Text(s, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Pastel.Ink) }
                    }
                }
                KidButton("Alıştırmaya geç ➡", next, Modifier.fillMaxWidth(0.8f), Pastel.Mint)
            }
        },
        makeQuestions = { syllableQuestions(lesson.items) }, readItems = null,
        hasPrev = i > 0, hasNext = i + 1 < SYLLABLE_LESSONS.size,
        onPrev = { go(Screen.Syl(i - 1)) }, onNext = { go(Screen.Syl(i + 1)) }, onBack = back,
    )
}

@Composable
fun ReadScreen(i: Int, store: Store, speaker: Speaker, reader: Reader, go: (Screen) -> Unit, back: () -> Unit) {
    val lesson = READ_LESSONS[i]
    val isWords = lesson.words.isNotEmpty()
    LessonFlow(
        title = lesson.title, lessonId = "R$i", store = store, speaker = speaker, reader = reader,
        learn = { next ->
            LaunchedEffect(Unit) { speaker.say("Dokun, dinle ve oku!") }
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (isWords) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        lesson.words.forEach { w ->
                            Column(
                                Modifier.width(150.dp).clip(RoundedCornerShape(24.dp)).background(Pastel.Peach)
                                    .clickable { speaker.say(w.text, audioKey("w", w.text)) }.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(w.emoji, fontSize = 56.sp)
                                Text(w.text, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Pastel.Ink)
                            }
                        }
                    }
                } else {
                    lesson.sentences.forEach { s ->
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Pastel.Peach)
                                .clickable { speaker.say(s) }.padding(16.dp),
                        ) { Text("🔊  $s", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Pastel.Ink) }
                    }
                }
                KidButton("Devam ➡", next, Modifier.fillMaxWidth(0.8f), Pastel.Mint)
            }
        },
        makeQuestions = if (isWords) ({ wordQuestions(lesson.words) }) else null,
        readItems = if (isWords) lesson.words.map { it.text } else lesson.sentences,
        hasPrev = i > 0, hasNext = i + 1 < READ_LESSONS.size,
        onPrev = { go(Screen.Read(i - 1)) }, onNext = { go(Screen.Read(i + 1)) }, onBack = back,
    )
}
