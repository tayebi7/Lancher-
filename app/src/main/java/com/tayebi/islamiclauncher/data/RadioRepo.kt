package com.tayebi.islamiclauncher.data

import org.json.JSONArray
import org.json.JSONObject

data class Station(val name: String, val url: String)

/** Real live radio directories: mp3quran.net (Quran radios) and radio-browser.info (community directory). */
object RadioRepo {
    private val BROWSER_HOSTS = listOf("de1", "nl1", "at1", "all")

    fun quran(): List<Station> {
        val root = JSONObject(Http.get("https://www.mp3quran.net/api/v3/radios?language=ar"))
        val arr = root.optJSONArray("radios") ?: JSONArray()
        return (0 until arr.length()).mapNotNull {
            val o = arr.getJSONObject(it)
            val url = o.optString("url").trim()
            val name = o.optString("name").trim()
            if (url.startsWith("http") && name.isNotEmpty()) Station(name, url) else null
        }
    }

    private fun browser(query: String): List<Station> {
        var last: Exception? = null
        for (h in BROWSER_HOSTS) {
            try {
                val arr = JSONArray(Http.get("https://$h.api.radio-browser.info/json/stations/search?hidebroken=true&order=clickcount&reverse=true&limit=60&$query"))
                return (0 until arr.length()).mapNotNull {
                    val o = arr.getJSONObject(it)
                    val url = o.optString("url_resolved").ifBlank { o.optString("url") }.trim()
                    val name = o.optString("name").trim().replace(Regex("\\s+"), " ")
                    if (url.startsWith("http") && name.isNotEmpty()) Station(name, url) else null
                }.distinctBy { it.name.lowercase() }
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: java.io.IOException("no host")
    }

    fun algeria(): List<Station> = browser("countrycode=DZ")
    fun islamic(): List<Station> = browser("tag=islam&language=arabic")
}
