package com.tayebi.islamiclauncher.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

data class Reciter(val name: String, val moshaf: String, val server: String, val surahs: Set<Int>)

/** Real reciter directory from the mp3quran.net public API (cached on disk for offline use). */
class ReciterRepo(ctx: Context) {
    private val f = File(ctx.filesDir, "reciters.json")

    suspend fun load(): List<Reciter> = withContext(Dispatchers.IO) {
        val fresh = f.exists() && System.currentTimeMillis() - f.lastModified() < 3L * 24 * 3600 * 1000
        val text = if (fresh) f.readText() else try {
            Http.get("https://www.mp3quran.net/api/v3/reciters?language=ar", 30000).also { f.writeText(it) }
        } catch (e: Exception) {
            if (f.exists()) f.readText() else throw e
        }
        val arr = JSONObject(text).getJSONArray("reciters")
        val out = ArrayList<Reciter>()
        for (i in 0 until arr.length()) {
            val r = arr.getJSONObject(i)
            val name = r.optString("name").trim()
            val ms = r.optJSONArray("moshaf") ?: continue
            for (j in 0 until ms.length()) {
                val m = ms.getJSONObject(j)
                val server = m.optString("server")
                if (name.isEmpty() || !server.startsWith("http")) continue
                val surahs = m.optString("surah_list").split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                out.add(Reciter(name, m.optString("name"), if (server.endsWith("/")) server else "$server/", surahs))
            }
        }
        out
    }

    companion object {
        fun audioUrl(server: String, surah: Int) = server + String.format("%03d", surah) + ".mp3"
    }
}
