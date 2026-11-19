package com.tayebi.islamiclauncher.data

import androidx.compose.ui.graphics.ImageBitmap
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

data class Place(
    val name: String,
    val admin: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val tz: String
) {
    fun short(): String = listOf(name, country).filter { it.isNotBlank() }.joinToString(" - ")
    fun full(): String = listOf(name, admin, country).filter { it.isNotBlank() }.distinct().joinToString("، ")
    fun toJson(): String = JSONObject()
        .put("n", name).put("a", admin).put("c", country)
        .put("lat", lat).put("lon", lon).put("tz", tz).toString()

    companion object {
        val DEFAULT = Place("بشار", "بشار", "الجزائر", 31.6167, -2.2167, "Africa/Algiers")
        fun parse(s: String?): Place = try {
            val o = JSONObject(s!!)
            Place(o.getString("n"), o.optString("a"), o.optString("c"), o.getDouble("lat"), o.getDouble("lon"), o.optString("tz", "UTC"))
        } catch (e: Exception) {
            DEFAULT
        }
    }
}

/** Six entries in this order: Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha. */
class DayTimes(val date: LocalDate, val t: List<LocalTime>)

data class Weather(
    val temp: Double, val code: Int, val isDay: Boolean,
    val hi: Double, val lo: Double, val humidity: Int, val wind: Double, val updated: Long
)

data class AppInfo(val pkg: String, val label: String, val icon: ImageBitmap?)

data class Chapter(val id: Int, val nameAr: String, val nameEn: String, val verses: Int, val place: String)
data class Verse(val key: String, val text: String)
data class AyahHit(val key: String, val text: String)

data class Hadith(val number: Int, val book: Int, val text: String, val grade: String)
data class HadithBook(val edition: String, val title: String, val sections: List<Pair<Int, String>>, val items: List<Hadith>)
data class FavHadith(val edition: String, val bookTitle: String, val number: Int, val text: String, val grade: String)
data class Edition(val id: String, val ar: String, val en: String)
