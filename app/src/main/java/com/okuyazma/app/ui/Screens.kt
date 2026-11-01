@file:OptIn(ExperimentalLayoutApi::class)

package com.okuyazma.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okuyazma.app.audio.Speaker
import com.okuyazma.app.data.*

enum class Section { LETTERS, SYLLABLES, READING }

fun sectionOpen(store: Store, s: Section): Boolean = store.unlockAll || when (s) {
    Section.LETTERS -> true
    Section.SYLLABLES -> LETTERS.indices.all { store.isDone("L$it") }
    Section.READING -> (0 until OPEN_LESSON_COUNT).all { store.isDone("S$it") }
}

fun lessonOpen(store: Store, prefix: String, i: Int): Boolean =
    store.unlockAll || i == 0 || store.isDone("$prefix${i - 1}")

// ---------------------------------------------------------------- ana ekran

@Composable
fun HomeScreen(store: Store, speaker: Speaker, onOpen: (Section) -> Unit, onParent: () -> Unit) {
    LaunchedEffect(Unit) {
        if (!speaker.greeted) { speaker.greeted = true; speaker.say("Masal'ın Dünyası'na hoş geldin! Bugün hangi dersi çalışalım?") }
    }
    val lettersDone = LETTERS.indices.count { store.isDone("L$it") }
    val sylDone = SYLLABLE_LESSONS.indices.count { store.isDone("S$it") }
    val readDone = READ_LESSONS.indices.count { store.isDone("R$it") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Masal'ın Dünyası 🌸", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            TextButton(onClick = onParent) { Text("👪", fontSize = 30.sp) }
        }
        Text("Bugünkü hedef: ⭐ ${store.todayStars} / ${store.goal}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(
            progress = { (store.todayStars / store.goal.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)),
        )
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val cards: @Composable (Modifier) -> Unit = { m ->
                SectionCard(m, "🔤", "Harfler", "$lettersDone / ${LETTERS.size}", !sectionOpen(store, Section.LETTERS), Pastel.Pink, speaker) { onOpen(Section.LETTERS) }
                SectionCard(m, "🧩", "Heceler", "$sylDone / ${SYLLABLE_LESSONS.size}", !sectionOpen(store, Section.SYLLABLES), Pastel.Sky, speaker) { onOpen(Section.SYLLABLES) }
                SectionCard(m, "📖", "Okuma", "$readDone / ${READ_LESSONS.size}", !sectionOpen(store, Section.READING), Pastel.Mint, speaker) { onOpen(Section.READING) }
            }
            if (maxWidth > maxHeight) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) { cards(Modifier.weight(1f).fillMaxHeight()) }
            } else {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) { cards(Modifier.weight(1f).fillMaxWidth()) }
            }
        }
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier, emoji: String, title: String, sub: String, locked: Boolean,
    color: Color, speaker: Speaker, onClick: () -> Unit,
) {
    Box(
        modifier.clip(RoundedCornerShape(32.dp)).background(if (locked) MaterialTheme.colorScheme.surfaceVariant else color)
            .clickable { if (locked) speaker.say("Bu bölüm kilitli. Önce önceki dersleri bitir.") else onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (locked) "🔒" else emoji, fontSize = 56.sp)
            Text(title, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = if (locked) MaterialTheme.colorScheme.onSurface else Pastel.Ink)
            Text(sub, fontSize = 18.sp, color = if (locked) MaterialTheme.colorScheme.onSurface else Pastel.Ink)
        }
    }
}

// ---------------------------------------------------------------- ders haritası

@Composable
fun MapScreen(
    title: String, labels: List<String>, prefix: String, store: Store, speaker: Speaker,
    onOpen: (Int) -> Unit, onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TopBar(title, onBack)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(96.dp), contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(labels) { i, label ->
                val open = lessonOpen(store, prefix, i)
                val done = store.isDone("$prefix$i")
                Box(
                    Modifier.aspectRatio(1f).clip(RoundedCornerShape(24.dp))
                        .background(if (done) Pastel.Mint else if (open) Pastel.Pink else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { if (open) onOpen(i) else speaker.say("Önce önceki dersi bitir") },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                        color = if (open) Pastel.Ink else MaterialTheme.colorScheme.onSurface)
                    if (done) Text("⭐", Modifier.align(Alignment.TopEnd).padding(6.dp), fontSize = 18.sp)
                    if (!open) Text("🔒", Modifier.align(Alignment.TopEnd).padding(6.dp), fontSize = 18.sp)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- ebeveyn paneli

@Composable
fun ParentScreen(store: Store, onBack: () -> Unit) {
    var passed by remember { mutableStateOf(false) }
    val a = remember { (3..9).random() }
    val b = remember { (3..9).random() }
    var input by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopBar("Ebeveyn Paneli", onBack)
        if (!passed) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                Text("Yetişkin doğrulaması", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("$a + $b = ?", fontSize = 36.sp)
                OutlinedTextField(
                    value = input, onValueChange = { input = it.filter(Char::isDigit).take(3) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                KidButton("Giriş", { if (input == "${a + b}") passed = true else input = "" })
            }
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("İlerleme raporu", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Stat("Öğrenilen harf", "${LETTERS.indices.count { store.isDone("L$it") }} / ${LETTERS.size}")
                Stat("Tamamlanan hece dersi", "${SYLLABLE_LESSONS.indices.count { store.isDone("S$it") }} / ${SYLLABLE_LESSONS.size}")
                Stat("Tamamlanan okuma dersi", "${READ_LESSONS.indices.count { store.isDone("R$it") }} / ${READ_LESSONS.size}")
                Stat("Toplam yıldız", "⭐ ${store.totalStars}")
                Stat("Bugünkü yıldız", "⭐ ${store.todayStars} / ${store.goal}")

                Text("Günlük hedef (yıldız)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 20, 30).forEach { g ->
                        KidButton("$g", { store.updateGoal(g) }, color = if (store.goal == g) Pastel.Pink else Pastel.Lavender)
                    }
                }
                Text("Tema", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("☀️ Açık", "🌙 Koyu", "📱 Sistem").forEachIndexed { i, n ->
                        KidButton(n, { store.setTheme(i) }, color = if (store.themeMode == i) Pastel.Pink else Pastel.Lavender)
                    }
                }
                ToggleRow("Sesler açık", store.soundOn) { store.setSound(it) }
                ToggleRow("Tüm dersleri aç (kilitleri kaldır)", store.unlockAll) { store.updateUnlockAll(it) }
                ToggleRow("Sesli okuma kontrolü (mikrofon)", store.micOn) { store.setMic(it) }
                Text(
                    "Not: Mikrofon kontrolü cihazınızın konuşma tanıma servisini kullanır. Cihazda çevrimdışı Türkçe " +
                        "konuşma paketi yüklü değilse ses, servis sağlayıcısına (ör. Google) gönderilebilir. " +
                        "Kapalıyken çocuk \"Okudum\" düğmesiyle devam eder ve hiçbir ses kaydedilmez.",
                    fontSize = 14.sp,
                )
                KidButton("İlerlemeyi sıfırla", { confirm = true }, Modifier.fillMaxWidth(), Color(0xFFFFB4B4))
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Emin misiniz?") },
            text = { Text("Tüm ilerleme ve yıldızlar silinecek.") },
            confirmButton = { TextButton({ store.reset(); confirm = false }) { Text("Evet, sıfırla") } },
            dismissButton = { TextButton({ confirm = false }) { Text("Vazgeç") } },
        )
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 18.sp)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 18.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
