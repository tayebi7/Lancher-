package com.tayebi.islamiclauncher.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Hadith text is loaded verbatim from the open hadith dataset (fawazahmed0/hadith-api), which is
 * derived from sunnah.com and other classical sources. Nothing is generated or edited here.
 * Each book is downloaded once and kept on disk for offline use.
 */
class HadithRepo(private val ctx: Context) {
    private val dir = File(ctx.filesDir, "hadith").apply { mkdirs() }
    private val cdn = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions"

    private suspend fun ensure(name: String, onProgress: (Int) -> Unit): File = withContext(Dispatchers.IO) {
        val f = File(dir, "$name.json")
        if (!f.exists() || f.length() < 1000) Http.download("$cdn/$name.min.json", f, onProgress)
        f
    }

    suspend fun load(ed: Edition, title: String, onProgress: (Int) -> Unit): HadithBook {
        val f = ensure("ara-${ed.id}", onProgress)
        return withContext(Dispatchers.IO) {
            val root = JSONObject(f.readText())
            val names = HashMap<Int, String>()
            root.optJSONObject("metadata")?.optJSONObject("section")?.let { s ->
                s.keys().forEach { k -> k.toIntOrNull()?.let { names[it] = s.optString(k) } }
            }
            val arr: JSONArray = root.getJSONArray("hadiths")
            val items = ArrayList<Hadith>(arr.length())
            for (i in 0 until arr.length()) {
                val h = arr.getJSONObject(i)
                val book = h.optJSONObject("reference")?.optInt("book", 0) ?: 0
                val grades = h.optJSONArray("grades")
                val g = if (grades == null) "" else (0 until grades.length()).joinToString(" • ") {
                    val gg = grades.getJSONObject(it)
                    val n = gg.optString("name")
                    val v = gg.optString("grade")
                    if (n.isBlank()) v else "$n: $v"
                }
                items.add(Hadith(h.optDouble("hadithnumber").toInt(), book, h.optString("text"), g))
            }
            val order = LinkedHashSet<Int>()
            items.forEach { order.add(it.book) }
            val sections = order.map { b -> b to (names[b]?.takeIf { it.isNotBlank() } ?: "") }
            HadithBook(ed.id, title, sections, items)
        }
    }

    /** English translation (used to show the narrator name when the source provides it). */
    suspend fun english(ed: Edition, onProgress: (Int) -> Unit): Map<Int, String> {
        val f = ensure("eng-${ed.id}", onProgress)
        return withContext(Dispatchers.IO) {
            val arr = JSONObject(f.readText()).getJSONArray("hadiths")
            val m = HashMap<Int, String>()
            for (i in 0 until arr.length()) {
                val h = arr.getJSONObject(i)
                m[h.optDouble("hadithnumber").toInt()] = h.optString("text")
            }
            m
        }
    }

    companion object {
        val EDITIONS = listOf(
            Edition("bukhari", "صحيح البخاري", "Sahih al-Bukhari"),
            Edition("muslim", "صحيح مسلم", "Sahih Muslim"),
            Edition("abudawud", "سنن أبي داود", "Sunan Abi Dawud"),
            Edition("tirmidhi", "جامع الترمذي", "Jami at-Tirmidhi"),
            Edition("nasai", "سنن النسائي", "Sunan an-Nasa'i"),
            Edition("ibnmajah", "سنن ابن ماجه", "Sunan Ibn Majah"),
            Edition("malik", "موطأ مالك", "Muwatta Malik"),
            Edition("nawawi", "الأربعون النووية", "Forty Hadith an-Nawawi")
        )
        private val narr = Regex("^\\s*Narrated\\s+(.{2,70}?)\\s*:")
        fun narrator(en: String?): String? = en?.let { narr.find(it)?.groupValues?.get(1)?.trim() }
    }
}
