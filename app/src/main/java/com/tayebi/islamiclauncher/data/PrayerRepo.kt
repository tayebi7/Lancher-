package com.tayebi.islamiclauncher.data

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Prayer times come from the Aladhan API (api.aladhan.com) – a real calculation engine
 * (no hard-coded times). A whole month is fetched and cached so the launcher keeps
 * working offline. Manual minute offsets are applied locally on top of the cached data.
 */
class PrayerRepo(private val sp: SharedPreferences) {
    private val keys = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
    private val fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    private fun key(p: Place, m: Int, s: Int, ym: YearMonth) =
        "pt_%.3f_%.3f_%d_%d_%s".format(Locale.US, p.lat, p.lon, m, s, ym.toString())

    private fun parse(json: String): Map<LocalDate, List<LocalTime>> {
        val arr = JSONArray(json)
        val out = HashMap<LocalDate, List<LocalTime>>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val g = o.getJSONObject("date").getJSONObject("gregorian").getString("date")
            val tm = o.getJSONObject("timings")
            out[LocalDate.parse(g, fmt)] = keys.map { LocalTime.parse(tm.getString(it).trim().substring(0, 5)) }
        }
        return out
    }

    fun lastUpdated(): Long = sp.getLong("prayer_updated", 0L)

    suspend fun get(p: Place, d: LocalDate, method: Int, school: Int, tune: List<Int>): DayTimes? =
        withContext(Dispatchers.IO) {
            val ym = YearMonth.from(d)
            val k = key(p, method, school, ym)
            var map = sp.getString(k, null)?.let { runCatching { parse(it) }.getOrNull() }
            if (map == null || d !in map) {
                try {
                    val tz = URLEncoder.encode(p.tz, "UTF-8")
                    val url = "https://api.aladhan.com/v1/calendar/${ym.year}/${ym.monthValue}" +
                        "?latitude=${"%.5f".format(Locale.US, p.lat)}&longitude=${"%.5f".format(Locale.US, p.lon)}" +
                        "&method=$method&school=$school&timezonestring=$tz"
                    val data = JSONObject(Http.get(url, 20000)).getJSONArray("data").toString()
                    map = parse(data)
                    sp.edit().putString(k, data).putLong("prayer_updated", System.currentTimeMillis()).apply()
                } catch (e: Exception) {
                    if (map == null) return@withContext null
                }
            }
            val base = map[d] ?: return@withContext null
            DayTimes(d, base.mapIndexed { i, t -> t.plusMinutes(tune.getOrElse(i) { 0 }.toLong()) })
        }
}
