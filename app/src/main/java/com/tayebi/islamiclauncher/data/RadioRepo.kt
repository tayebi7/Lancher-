package com.tayebi.islamiclauncher.data

import org.json.JSONArray
import org.json.JSONObject

data class Station(val name: String, val url: String, val logo: String = "")

class RadioCategory(val key: String, val loader: () -> List<Station>)

/** Real live radio directories: mp3quran.net (Quran radios) and radio-browser.info (community directory with logos). */
object RadioRepo {
    private val HOSTS = listOf("de1", "nl1", "at1", "all")

    private fun mp3quran(): List<Station> {
        val root = JSONObject(Http.get("https://www.mp3quran.net/api/v3/radios?language=ar"))
        val arr = root.optJSONArray("radios") ?: JSONArray()
        return (0 until arr.length()).mapNotNull {
            val o = arr.getJSONObject(it)
            val url = o.optString("url").trim()
            val name = o.optString("name").trim()
            if (url.startsWith("http") && name.isNotEmpty()) Station(name, url) else null
        }
    }

    private fun browser(query: String, limit: Int = 60): List<Station> {
        var last: Exception? = null
        for (h in HOSTS) {
            try {
                val arr = JSONArray(Http.get("https://$h.api.radio-browser.info/json/stations/search?hidebroken=true&order=clickcount&reverse=true&limit=$limit&$query"))
                return (0 until arr.length()).mapNotNull {
                    val o = arr.getJSONObject(it)
                    val url = o.optString("url_resolved").ifBlank { o.optString("url") }.trim()
                    val name = o.optString("name").trim().replace(Regex("\\s+"), " ")
                    if (url.startsWith("http") && name.isNotEmpty()) Station(name, url, o.optString("favicon").trim()) else null
                }.distinctBy { it.name.lowercase() }
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: java.io.IOException("no host")
    }

    private fun multi(vararg codes: String): List<Station> {
        val out = ArrayList<Station>()
        var ok = false
        for (c in codes) try { out += browser("countrycode=$c", 14); ok = true } catch (e: Exception) { }
        if (!ok) throw java.io.IOException("none")
        return out.distinctBy { it.url }
    }

    val ARABIC = listOf(
        RadioCategory("radio_dz") { browser("countrycode=DZ") },
        RadioCategory("radio_arab") { browser("language=arabic") },
        RadioCategory("radio_quran") { mp3quran() },
        RadioCategory("radio_islamic") { browser("tag=islam&language=arabic") },
        RadioCategory("radio_newsculture") { browser("tag=news&language=arabic") }
    )
    val FOREIGN = listOf(
        RadioCategory("radio_french") { browser("language=french") },
        RadioCategory("radio_english") { browser("language=english") },
        RadioCategory("radio_intl") { multi("DE", "ES", "IT", "TR") }
    )
}
