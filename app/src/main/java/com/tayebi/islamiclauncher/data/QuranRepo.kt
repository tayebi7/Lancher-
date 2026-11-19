package com.tayebi.islamiclauncher.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder

/**
 * Real Quran content from the Quran.com public API (api.quran.com/api/v4).
 * Text is never generated – it is fetched (Uthmani script) and cached on disk for offline reading.
 */
class QuranRepo(private val ctx: Context) {
    private val dir = File(ctx.filesDir, "quran").apply { mkdirs() }
    private val audioDir = File(ctx.filesDir, "quran_audio").apply { mkdirs() }
    private val base = "https://api.quran.com/api/v4"
    private val tag = Regex("<[^>]*>")

    private fun clean(s: String) = s.replace(tag, "").replace("&nbsp;", " ").trim()

    private suspend fun cached(name: String, preferCache: Boolean, url: String): String =
        withContext(Dispatchers.IO) {
            val f = File(dir, name)
            if (preferCache && f.exists()) return@withContext f.readText()
            try {
                val b = Http.get(url, 25000)
                f.writeText(b)
                b
            } catch (e: Exception) {
                if (f.exists()) f.readText() else throw e
            }
        }

    suspend fun chapters(): List<Chapter> {
        val o = JSONObject(cached("chapters.json", true, "$base/chapters?language=en"))
        val a = o.getJSONArray("chapters")
        return (0 until a.length()).map {
            val c = a.getJSONObject(it)
            Chapter(c.getInt("id"), c.getString("name_arabic"), c.optString("name_simple"), c.getInt("verses_count"), c.optString("revelation_place"))
        }
    }

    suspend fun verses(ch: Int): List<Verse> {
        val o = JSONObject(cached("ch_${ch}_uthmani.json", true, "$base/quran/verses/uthmani?chapter_number=$ch"))
        val a = o.getJSONArray("verses")
        return (0 until a.length()).map {
            val v = a.getJSONObject(it)
            Verse(v.getString("verse_key"), v.getString("text_uthmani"))
        }
    }

    suspend fun translation(ch: Int, id: Int): List<String> {
        val o = JSONObject(cached("ch_${ch}_tr_$id.json", true, "$base/quran/translations/$id?chapter_number=$ch"))
        val a = o.getJSONArray("translations")
        return (0 until a.length()).map { clean(a.getJSONObject(it).optString("text")) }
    }

    suspend fun tafsir(ch: Int, id: Int, keys: List<String>): Map<String, String> {
        val o = JSONObject(cached("ch_${ch}_tafsir_$id.json", true, "$base/quran/tafsirs/$id?chapter_number=$ch"))
        val a = o.getJSONArray("tafsirs")
        val out = HashMap<String, String>()
        for (i in 0 until a.length()) {
            val t = a.getJSONObject(i)
            val k = t.optString("verse_key").ifBlank { keys.getOrNull(i) ?: "" }
            if (k.isNotBlank()) out[k] = clean(t.optString("text"))
        }
        return out
    }

    suspend fun search(q: String): List<AyahHit> = withContext(Dispatchers.IO) {
        val o = JSONObject(Http.get("$base/search?q=${URLEncoder.encode(q, "UTF-8")}&size=20&language=ar", 20000))
        val a = o.getJSONObject("search").optJSONArray("results") ?: return@withContext emptyList()
        (0 until a.length()).map {
            val r = a.getJSONObject(it)
            AyahHit(r.getString("verse_key"), clean(r.optString("text")))
        }
    }

    fun localAudio(reciter: Int, ch: Int) = File(audioDir, "${reciter}_$ch.mp3")

    private suspend fun audioUrl(reciter: Int, ch: Int): String = withContext(Dispatchers.IO) {
        JSONObject(Http.get("$base/chapter_recitations/$reciter/$ch", 20000))
            .getJSONObject("audio_file").getString("audio_url")
    }

    suspend fun audioUri(reciter: Int, ch: Int): Uri {
        val f = localAudio(reciter, ch)
        if (f.exists() && f.length() > 0) return Uri.fromFile(f)
        return Uri.parse(audioUrl(reciter, ch))
    }

    suspend fun download(reciter: Int, ch: Int, onProgress: (Int) -> Unit) {
        val url = audioUrl(reciter, ch)
        withContext(Dispatchers.IO) { Http.download(url, localAudio(reciter, ch), onProgress) }
    }

    companion object {
        /** Quran.com recitation ids (chapter recitations). */
        val RECITERS = listOf(
            7 to "مشاري راشد العفاسي",
            2 to "عبد الباسط عبد الصمد (مرتل)",
            3 to "عبد الرحمن السديس",
            10 to "سعود الشريم",
            6 to "محمود خليل الحصري",
            9 to "محمد صديق المنشاوي",
            5 to "هاني الرفاعي",
            4 to "أبو بكر الشاطري"
        )
        const val TAFSIR_MUYASSAR = 16
        const val TR_EN = 131
        const val TR_FR = 136
    }
}
