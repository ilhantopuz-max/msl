@file:OptIn(ExperimentalLayoutApi::class)

package com.okuyazma.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okuyazma.app.audio.Speaker
import com.okuyazma.app.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Option(val label: String, val sp: Int = 36)
data class Question(
    val prompt: String,
    val display: String? = null,
    val speak: String? = null,
    val speakKey: String? = null,
    val options: List<Option>,
    val answer: Int,
)

private fun q(prompt: String, display: String?, speak: String?, key: String?, correct: Option, wrongs: List<Option>): Question {
    val opts = (wrongs.filter { it != correct } + correct).distinct().shuffled()
    return Question(prompt, display, speak, key, opts, opts.indexOf(correct))
}

/** Harf dersi: 1) dinle→harfi seç  2) resim→harf  3) harf→resim */
fun letterQuestions(item: LetterItem): List<Question> {
    val others = LETTERS.filter { it != item }.shuffled()
    fun l(x: LetterItem) = Option("${x.upper} ${x.lower}", 40)
    fun e(x: LetterItem) = Option(x.emoji, 60)
    val starts = item.lower != "ğ"
    val q1 = q("Dinle! Bu hangi harf?", "🔊", item.lower, audioKey("l", item.lower), l(item), others.take(2).map { l(it) })
    val q2 = if (starts) {
        q("Bu resim hangi harfle başlıyor?", "${item.emoji}\n${item.word}", null, null, l(item), others.take(2).map { l(it) })
    } else {
        q("Dağ kelimesinin ortasındaki harfi bul", "${item.emoji}\n${item.word}", null, null, l(item),
            others.filter { it.lower !in "dağ" }.take(2).map { l(it) })
    }
    val pics = LETTERS.filter {
        it != item && if (starts) !it.word.lowercase(TR).startsWith(item.lower) else !it.word.lowercase(TR).contains("ğ")
    }.shuffled().take(2).map { e(it) }
    val q3 = q(
        if (starts) "${item.upper} harfiyle başlayan resmi seç" else "${item.upper} harfi olan resmi seç",
        "${item.upper} ${item.lower}", null, null, e(item), pics,
    )
    return listOf(q1, q2, q3)
}

/** Hece dersi: "dinle→heceyi seç" ve "birleştir (b + a = ?)" soruları karışık, 5 soru. */
fun syllableQuestions(items: List<String>): List<Question> =
    items.shuffled().take(5).mapIndexed { i, s ->
        fun o(x: String) = Option(x, 40)
        val wrongs = items.filter { it != s }.shuffled().take(2).map { o(it) }
        if (i % 2 == 0) q("Dinle! Hangi hece?", "🔊", s, audioKey("s", s), o(s), wrongs)
        else {
            val eq = s.map { it.toString() }.joinToString(" + ") + " = ?"
            q("Harfleri birleştir!", eq, null, null, o(s), wrongs)
        }
    }

/** Kelime dersi: resim→kelime ve dinle→resim soruları. */
fun wordQuestions(words: List<WordItem>): List<Question> =
    words.shuffled().mapIndexed { i, w ->
        val wrongs = ALL_WORDS.filter { it.text != w.text }.shuffled().take(2)
        if (i % 2 == 0) q("Bu resim hangisi?", w.emoji, null, null, Option(w.text, 28), wrongs.map { Option(it.text, 28) })
        else q("Dinle! Hangi resim?", "🔊", w.text, audioKey("w", w.text), Option(w.emoji, 60), wrongs.map { Option(it.emoji, 60) })
    }

@Composable
private fun OptionCard(o: Option, state: Int, enabled: Boolean, onClick: () -> Unit) {
    val bg = when (state) { 1 -> Color(0xFFFFB4B4); 2 -> Color(0xFFB8F0C8); else -> Pastel.Lavender }
    Box(
        Modifier.size(width = 150.dp, height = 120.dp).clip(RoundedCornerShape(28.dp)).background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(o.label, fontSize = o.sp.sp, fontWeight = FontWeight.Bold, color = Pastel.Ink, textAlign = TextAlign.Center)
    }
}

/** Soruları sırayla sorar. Doğru bulunana kadar devam edilir; ilk denemede doğru = 1 yıldız. */
@Composable
fun QuizRunner(questions: List<Question>, speaker: Speaker, onFinished: (Int) -> Unit) {
    var idx by remember { mutableIntStateOf(0) }
    var stars by remember { mutableIntStateOf(0) }
    var wrong by remember { mutableStateOf(setOf<Int>()) }
    var picked by remember { mutableIntStateOf(-1) }
    var reward by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val q = questions[idx]

    LaunchedEffect(idx) {
        wrong = emptySet(); picked = -1
        speaker.say(q.speak ?: q.prompt, q.speakKey)
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LinearProgressIndicator(
                progress = { idx / questions.size.toFloat() },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
            )
            Text(q.prompt, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            q.display?.let { Text(it, fontSize = 60.sp, textAlign = TextAlign.Center) }
            KidButton("🔊 Tekrar dinle", { speaker.say(q.speak ?: q.prompt, q.speakKey) }, color = Pastel.Sky)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                q.options.forEachIndexed { i, o ->
                    OptionCard(o, state = if (picked == i) 2 else if (i in wrong) 1 else 0, enabled = !reward && i !in wrong) {
                        if (i == q.answer) {
                            if (wrong.isEmpty()) stars++
                            picked = i; reward = true
                            speaker.praise()
                            scope.launch {
                                delay(1300)
                                reward = false
                                if (idx + 1 < questions.size) idx++ else onFinished(stars)
                            }
                        } else {
                            wrong = wrong + i
                            speaker.say("Tekrar dene")
                        }
                    }
                }
            }
        }
        if (reward) RewardOverlay()
    }
}
