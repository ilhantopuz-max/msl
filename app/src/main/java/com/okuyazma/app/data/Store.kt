package com.okuyazma.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** İlerleme ve ayarlar. Tamamen cihazda (SharedPreferences) saklanır, dışarı hiçbir şey gönderilmez. */
class Store(ctx: Context) {
    private val p = ctx.getSharedPreferences("okuyazma", Context.MODE_PRIVATE)

    var done by mutableStateOf<Set<String>>(p.getStringSet("done", emptySet())!!.toSet()); private set
    var totalStars by mutableIntStateOf(p.getInt("stars", 0)); private set
    var todayStars by mutableIntStateOf(0); private set
    var goal by mutableIntStateOf(p.getInt("goal", 10)); private set
    var soundOn by mutableStateOf(p.getBoolean("sound", true)); private set
    var micOn by mutableStateOf(p.getBoolean("mic", false)); private set
    var themeMode by mutableIntStateOf(p.getInt("theme", 0)); private set   // 0 açık, 1 koyu, 2 sistem
    var unlockAll by mutableStateOf(p.getBoolean("unlock", false)); private set

    init { todayStars = readToday() }

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private fun readToday(): Int = if (p.getString("day", "") == today()) p.getInt("dayStars", 0) else 0

    fun isDone(id: String) = id in done

    fun finish(id: String, stars: Int) {
        done = done + id
        p.edit().putStringSet("done", done).apply()
        addStars(stars)
    }

    fun addStars(n: Int) {
        if (n <= 0) return
        val t = readToday() + n
        todayStars = t
        totalStars += n
        p.edit().putString("day", today()).putInt("dayStars", t).putInt("stars", totalStars).apply()
    }

    fun updateGoal(v: Int) { goal = v; p.edit().putInt("goal", v).apply() }
    fun setSound(v: Boolean) { soundOn = v; p.edit().putBoolean("sound", v).apply() }
    fun setMic(v: Boolean) { micOn = v; p.edit().putBoolean("mic", v).apply() }
    fun setTheme(v: Int) { themeMode = v; p.edit().putInt("theme", v).apply() }
    fun updateUnlockAll(v: Boolean) { unlockAll = v; p.edit().putBoolean("unlock", v).apply() }

    fun reset() {
        done = emptySet(); totalStars = 0; todayStars = 0
        p.edit().remove("done").remove("stars").remove("day").remove("dayStars").apply()
    }
}
