package com.okuyazma.app.data

import java.util.Locale

val TR: Locale = Locale("tr", "TR")

data class LetterItem(val upper: String, val lower: String, val word: String, val emoji: String)
data class WordItem(val text: String, val emoji: String)
data class Lesson(val title: String, val items: List<String>)
/** Kelime dersi: words dolu. Cümle dersi: sentences dolu. */
data class ReadLesson(val title: String, val words: List<WordItem>, val sentences: List<String>)

/** 29 harflik Türk alfabesi (sıra: A B C Ç D E F G Ğ H I İ J K L M N O Ö P R S Ş T U Ü V Y Z). */
val LETTERS = listOf(
    LetterItem("A", "a", "Arı", "🐝"),
    LetterItem("B", "b", "Balık", "🐟"),
    LetterItem("C", "c", "Civciv", "🐥"),
    LetterItem("Ç", "ç", "Çiçek", "🌸"),
    LetterItem("D", "d", "Dondurma", "🍦"),
    LetterItem("E", "e", "Elma", "🍎"),
    LetterItem("F", "f", "Fil", "🐘"),
    LetterItem("G", "g", "Gül", "🌹"),
    LetterItem("Ğ", "ğ", "Dağ", "🏔️"),      // Ğ ile başlayan kelime yok: harf kelimenin ortasında
    LetterItem("H", "h", "Havuç", "🥕"),
    LetterItem("I", "ı", "Işık", "💡"),
    LetterItem("İ", "i", "İnek", "🐄"),
    LetterItem("J", "j", "Jöle", "🍮"),
    LetterItem("K", "k", "Kedi", "🐱"),
    LetterItem("L", "l", "Limon", "🍋"),
    LetterItem("M", "m", "Muz", "🍌"),
    LetterItem("N", "n", "Nane", "🌿"),
    LetterItem("O", "o", "Okul", "🏫"),
    LetterItem("Ö", "ö", "Ördek", "🦆"),
    LetterItem("P", "p", "Pasta", "🍰"),
    LetterItem("R", "r", "Robot", "🤖"),
    LetterItem("S", "s", "Sincap", "🐿️"),
    LetterItem("Ş", "ş", "Şemsiye", "☂️"),
    LetterItem("T", "t", "Tavşan", "🐰"),
    LetterItem("U", "u", "Uçak", "✈️"),
    LetterItem("Ü", "ü", "Üzüm", "🍇"),
    LetterItem("V", "v", "Vapur", "🚢"),
    LetterItem("Y", "y", "Yıldız", "⭐"),
    LetterItem("Z", "z", "Zürafa", "🦒"),
)

val VOWELS = listOf("a", "e", "ı", "i", "o", "ö", "u", "ü")
private val OPEN_CONS = listOf("b", "c", "ç", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "r", "s", "ş", "t", "v", "y", "z")
private val CLOSED_CONS = listOf("l", "m", "n", "r", "s", "t", "k", "z")

/** Açık hece (ünsüz+ünlü) → kapalı hece (ünlü+ünsüz) → 3 harfli → 4 harfli. */
val SYLLABLE_LESSONS: List<Lesson> = buildList {
    OPEN_CONS.forEach { c -> add(Lesson("Açık hece: ${c.uppercase(TR)}", VOWELS.map { c + it })) }
    VOWELS.forEach { v -> add(Lesson("Kapalı hece: ${v.uppercase(TR)}", CLOSED_CONS.map { v + it })) }
    add(Lesson("3 harfli 1", listOf("bal", "kal", "mal", "sal", "dal", "tel", "gel", "sel")))
    add(Lesson("3 harfli 2", listOf("kil", "dil", "bil", "gül", "kul", "yol", "kol", "dol")))
    add(Lesson("3 harfli 3", listOf("kum", "sun", "gün", "bun", "ton", "tün", "kur", "mor")))
    add(Lesson("3 harfli 4", listOf("kar", "sar", "yer", "ter", "kır", "sır", "kör", "dür")))
    add(Lesson("4 harfli 1", listOf("kral", "tren", "plan", "blok", "kart", "bant", "tost", "post")))
    add(Lesson("4 harfli 2", listOf("elma", "kapı", "masa", "kedi", "lale", "dede", "anne", "baba")))
}

/** Okuma bölümü, bu kadar açık hece dersi (ilk 20) bitince açılır. */
const val OPEN_LESSON_COUNT = 20

val READ_LESSONS = listOf(
    ReadLesson("Kelimeler 1", listOf(WordItem("elma", "🍎"), WordItem("kedi", "🐱"), WordItem("kuş", "🐦"), WordItem("balık", "🐟"), WordItem("araba", "🚗")), emptyList()),
    ReadLesson("Kelimeler 2", listOf(WordItem("kitap", "📖"), WordItem("kalem", "✏️"), WordItem("okul", "🏫"), WordItem("çanta", "🎒"), WordItem("top", "⚽")), emptyList()),
    ReadLesson("Kelimeler 3", listOf(WordItem("güneş", "☀️"), WordItem("ay", "🌙"), WordItem("yıldız", "⭐"), WordItem("bulut", "☁️"), WordItem("yağmur", "🌧️")), emptyList()),
    ReadLesson("Kelimeler 4", listOf(WordItem("süt", "🥛"), WordItem("ekmek", "🍞"), WordItem("muz", "🍌"), WordItem("üzüm", "🍇"), WordItem("portakal", "🍊")), emptyList()),
    ReadLesson("Kelimeler 5", listOf(WordItem("kuzu", "🐑"), WordItem("inek", "🐄"), WordItem("at", "🐴"), WordItem("arı", "🐝"), WordItem("fil", "🐘")), emptyList()),
    ReadLesson("Cümleler 1", emptyList(), listOf("Ela ata bak.", "Ali ip al.", "Anne süt al.", "Baba kitap al.", "Kedi süt içti.")),
    ReadLesson("Cümleler 2", emptyList(), listOf("Ben okula gittim.", "Ela top oynadı.", "Ali elma yedi.", "Kuş uçtu.", "Hava çok güzel.")),
)

val ALL_WORDS: List<WordItem> = READ_LESSONS.flatMap { it.words }

/**
 * Ses dosyası anahtarı. res/raw içine bu adla mp3/ogg/wav koyarsanız TTS yerine o çalınır.
 * Örn: audioKey("l","ç") = "l_cc"  → res/raw/l_cc.mp3
 * Türkçe karakterler: ç=cc ğ=gg ı=ii ö=oo ş=ss ü=uu
 */
fun audioKey(prefix: String, text: String): String {
    val sb = StringBuilder(prefix).append('_')
    text.lowercase(TR).forEach { ch ->
        when (ch) {
            'ç' -> sb.append("cc")
            'ğ' -> sb.append("gg")
            'ı' -> sb.append("ii")
            'ö' -> sb.append("oo")
            'ş' -> sb.append("ss")
            'ü' -> sb.append("uu")
            ' ' -> sb.append('_')
            in 'a'..'z', in '0'..'9' -> sb.append(ch)
            else -> {}
        }
    }
    return sb.toString()
}
