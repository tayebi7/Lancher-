package com.tayebi.islamiclauncher.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class StrPref(private val sp: SharedPreferences, private val key: String, def: String) {
    var value by mutableStateOf(sp.getString(key, def) ?: def)
        private set

    fun set(v: String) {
        value = v
        sp.edit().putString(key, v).apply()
    }
}

class IntPref(private val sp: SharedPreferences, private val key: String, def: Int) {
    var value by mutableStateOf(sp.getInt(key, def))
        private set

    fun set(v: Int) {
        value = v
        sp.edit().putInt(key, v).apply()
    }
}

class BoolPref(private val sp: SharedPreferences, private val key: String, def: Boolean) {
    var value by mutableStateOf(sp.getBoolean(key, def))
        private set

    fun set(v: Boolean) {
        value = v
        sp.edit().putBoolean(key, v).apply()
    }
}

class Settings(ctx: Context) {
    val sp: SharedPreferences = ctx.getSharedPreferences("islamic_launcher", Context.MODE_PRIVATE)

    val lang = StrPref(sp, "lang", "ar")
    val bg = IntPref(sp, "bg", 11)
    val bgAsset = StrPref(sp, "bg_asset", "")
    val bgMode = IntPref(sp, "bg_mode", 0)
    val bgDim = IntPref(sp, "bg_dim", 0)
    val font = IntPref(sp, "font", 0)
    val volume = IntPref(sp, "volume", 80)

    // Quran reciter chosen from the mp3quran.net directory (empty server = legacy Quran.com reciter)
    val reciterName = StrPref(sp, "reciter_name", "")
    val reciterServer = StrPref(sp, "reciter_server", "")
    val reciterSurahs = StrPref(sp, "reciter_surahs", "")
    val reciterFavs = StrPref(sp, "reciter_favs", "")   // lines: name|server|surahs

    // Radio / news / scores / currencies
    val radioFavs = StrPref(sp, "radio_favs", "")       // lines: name|url|logo
    val newsCat = IntPref(sp, "news_cat", 0)
    val scoreLeagues = StrPref(sp, "score_leagues", "uefa.champions,eng.1,esp.1,ita.1,ger.1,fra.1,ksa.1,caf.nations")
    val scoreTeams = StrPref(sp, "score_teams", "")     // favourite team names, one per line
    val goalAlerts = BoolPref(sp, "goal_alerts", true)
    val goalSound = BoolPref(sp, "goal_sound", true)

    // Prayer
    val method = IntPref(sp, "method", 19)          // 19 = Algeria (Aladhan)
    val school = IntPref(sp, "school", 0)           // 0 = Shafi/Maliki/Hanbali, 1 = Hanafi
    val tune = StrPref(sp, "tune", "0,0,0,0,0,0")   // Fajr,Sunrise,Dhuhr,Asr,Maghrib,Isha (minutes)
    val adhanOn = BoolPref(sp, "adhan_on", true)
    val adhanUrl = StrPref(sp, "adhan_url", DEFAULT_ADHAN_URL)
    val hijriAdj = IntPref(sp, "hijri_adj", 0)

    // Places
    val prayerPlace = StrPref(sp, "prayer_place", Place.DEFAULT.toJson())
    val weatherSame = BoolPref(sp, "weather_same", true)
    val weatherPlace = StrPref(sp, "weather_place", Place.DEFAULT.toJson())

    // Apps
    val favorites = StrPref(sp, "favorites", UNSET)
    val favVer = IntPref(sp, "fav_ver", 0)

    // Quran
    val reciter = IntPref(sp, "reciter", 7)
    val showTr = BoolPref(sp, "show_tr", false)
    val showTafsir = BoolPref(sp, "show_tafsir", false)
    val lastSurah = IntPref(sp, "last_surah", 0)
    val lastAyah = IntPref(sp, "last_ayah", 0)

    // Hadith
    val hadithFavs = StrPref(sp, "hadith_favs", "[]")

    fun tuneList(): List<Int> {
        val l = tune.value.split(",").map { it.trim().toIntOrNull() ?: 0 }
        return List(6) { l.getOrElse(it) { 0 } }
    }

    companion object {
        const val UNSET = "__unset__"
        // Editable in Settings. If it cannot be loaded the app falls back to the system notification sound.
        const val DEFAULT_ADHAN_URL = "https://www.islamcan.com/audio/adhan/azan1.mp3"
    }
}
