package com.tayebi.islamiclauncher.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

data class Dhikr(val text: String, val count: Int)
data class AdhkarCategory(val title: String, val items: List<Dhikr>)

/** Adhkar from the open "Adhkar-json" collection (Hisn al-Muslim text), cached for offline use. */
class AdhkarRepo(ctx: Context) {
    private val f = File(ctx.filesDir, "adhkar.json")

    suspend fun load(): List<AdhkarCategory> = withContext(Dispatchers.IO) {
        val text = if (f.exists() && f.length() > 1000) f.readText() else try {
            Http.get("https://cdn.jsdelivr.net/gh/rn0x/Adhkar-json@main/adhkar.json", 30000).also { f.writeText(it) }
        } catch (e: Exception) {
            Http.get("https://raw.githubusercontent.com/rn0x/Adhkar-json/main/adhkar.json", 30000).also { f.writeText(it) }
        }
        val a = JSONArray(text)
        (0 until a.length()).mapNotNull { i ->
            val c = a.getJSONObject(i)
            val arr = c.optJSONArray("array") ?: return@mapNotNull null
            val items = (0 until arr.length()).mapNotNull { j ->
                val d = arr.getJSONObject(j)
                val t = d.optString("text").trim()
                if (t.isEmpty()) null else Dhikr(t, d.optString("count", "1").toIntOrNull() ?: 1)
            }
            if (items.isEmpty()) null else AdhkarCategory(c.optString("category").trim(), items)
        }
    }
}
