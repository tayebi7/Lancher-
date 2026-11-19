package com.tayebi.islamiclauncher.data

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale

/** Live weather from Open-Meteo (free, no API key). Last result is cached for offline display. */
class WeatherRepo(private val sp: SharedPreferences) {
    private fun key(p: Place) = "w_%.2f_%.2f".format(Locale.US, p.lat, p.lon)

    private fun parse(body: String, ts: Long): Weather {
        val o = JSONObject(body)
        val c = o.getJSONObject("current")
        val d = o.getJSONObject("daily")
        return Weather(
            temp = c.getDouble("temperature_2m"),
            code = c.getInt("weather_code"),
            isDay = c.optInt("is_day", 1) == 1,
            hi = d.getJSONArray("temperature_2m_max").getDouble(0),
            lo = d.getJSONArray("temperature_2m_min").getDouble(0),
            humidity = c.optInt("relative_humidity_2m"),
            wind = c.optDouble("wind_speed_10m"),
            updated = ts
        )
    }

    fun cached(p: Place): Weather? {
        val b = sp.getString(key(p), null) ?: return null
        return runCatching { parse(b, sp.getLong(key(p) + "_ts", 0L)) }.getOrNull()
    }

    suspend fun fetch(p: Place): Weather = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=${"%.4f".format(Locale.US, p.lat)}" +
            "&longitude=${"%.4f".format(Locale.US, p.lon)}" +
            "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,is_day" +
            "&daily=temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=1"
        val body = Http.get(url)
        val now = System.currentTimeMillis()
        val w = parse(body, now)
        sp.edit().putString(key(p), body).putLong(key(p) + "_ts", now).apply()
        w
    }
}

object Geo {
    suspend fun search(q: String, lang: String): List<Place> = withContext(Dispatchers.IO) {
        val url = "https://geocoding-api.open-meteo.com/v1/search?name=${URLEncoder.encode(q, "UTF-8")}" +
            "&count=12&language=$lang&format=json"
        val arr = JSONObject(Http.get(url)).optJSONArray("results") ?: return@withContext emptyList()
        (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            Place(
                o.optString("name"), o.optString("admin1"), o.optString("country"),
                o.getDouble("latitude"), o.getDouble("longitude"), o.optString("timezone", "UTC")
            )
        }
    }

    suspend fun fromCoords(lat: Double, lon: Double, lang: String): Place = withContext(Dispatchers.IO) {
        var tz = "UTC"
        try {
            tz = JSONObject(
                Http.get("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m&timezone=auto&forecast_days=1")
            ).optString("timezone", "UTC")
        } catch (_: Exception) {
        }
        var name = ""
        var admin = ""
        var country = ""
        try {
            val o = JSONObject(
                Http.get("https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$lat&longitude=$lon&localityLanguage=$lang")
            )
            name = o.optString("city").ifBlank { o.optString("locality") }
            admin = o.optString("principalSubdivision")
            country = o.optString("countryName")
        } catch (_: Exception) {
        }
        if (name.isBlank()) name = "%.2f, %.2f".format(Locale.US, lat, lon)
        Place(name, admin, country, lat, lon, tz)
    }
}
