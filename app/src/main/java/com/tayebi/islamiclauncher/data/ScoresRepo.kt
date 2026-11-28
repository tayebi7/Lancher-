package com.tayebi.islamiclauncher.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class MatchEv(val minute: String, val kind: String, val teamId: String, val player: String)

data class Match(
    val id: String, val leagueSlug: String, val league: String,
    val homeId: String, val home: String, val homeLogo: String, val homeScore: Int?,
    val awayId: String, val away: String, val awayLogo: String, val awayScore: Int?,
    val state: String,            // pre | in | post
    val status: String, val startMs: Long, val events: List<MatchEv>
)

data class League(val slug: String, val ar: String, val fr: String, val en: String)

/** Live football data from ESPN's public scoreboard feed (no API key required). */
object ScoresRepo {
    val LEAGUES = listOf(
        League("uefa.champions", "دوري أبطال أوروبا", "Ligue des champions", "Champions League"),
        League("uefa.europa", "الدوري الأوروبي", "Ligue Europa", "Europa League"),
        League("eng.1", "الدوري الإنجليزي", "Premier League", "Premier League"),
        League("esp.1", "الدوري الإسباني", "LaLiga", "LaLiga"),
        League("ita.1", "الدوري الإيطالي", "Serie A", "Serie A"),
        League("ger.1", "الدوري الألماني", "Bundesliga", "Bundesliga"),
        League("fra.1", "الدوري الفرنسي", "Ligue 1", "Ligue 1"),
        League("ksa.1", "الدوري السعودي", "Saudi Pro League", "Saudi Pro League"),
        League("caf.nations", "كأس أمم أفريقيا", "CAN", "Africa Cup of Nations"),
        League("fifa.world", "كأس العالم", "Coupe du monde", "World Cup")
    )

    private val isoNoSec = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

    private fun parseTime(s: String): Long = try {
        LocalDateTime.parse(s.removeSuffix("Z").take(16), isoNoSec).toInstant(ZoneOffset.UTC).toEpochMilli()
    } catch (e: Exception) { 0L }

    /** date = yyyyMMdd or null for "today" (ESPN's default). */
    suspend fun fetch(slug: String, date: String?): List<Match> = withContext(Dispatchers.IO) {
        val url = "https://site.api.espn.com/apis/site/v2/sports/soccer/$slug/scoreboard" + (if (date != null) "?dates=$date" else "")
        val root = JSONObject(Http.get(url, 15000))
        val leagueName = root.optJSONArray("leagues")?.optJSONObject(0)?.optString("name").orEmpty()
        val ev = root.optJSONArray("events") ?: return@withContext emptyList()
        val out = ArrayList<Match>()
        for (i in 0 until ev.length()) {
            try {
                val e = ev.getJSONObject(i)
                val comp = e.getJSONArray("competitions").getJSONObject(0)
                val st = comp.getJSONObject("status")
                val type = st.getJSONObject("type")
                val cs = comp.getJSONArray("competitors")
                var h: JSONObject? = null; var a: JSONObject? = null
                for (k in 0 until cs.length()) {
                    val c = cs.getJSONObject(k)
                    if (c.optString("homeAway") == "home") h = c else a = c
                }
                if (h == null || a == null) continue
                val evs = ArrayList<MatchEv>()
                val det = comp.optJSONArray("details")
                if (det != null) for (k in 0 until det.length()) {
                    val d = det.getJSONObject(k)
                    val kind = when {
                        d.optBoolean("redCard") -> "red"
                        d.optBoolean("ownGoal") && d.optBoolean("scoringPlay") -> "own"
                        d.optBoolean("scoringPlay") && d.optBoolean("penaltyKick") -> "pen"
                        d.optBoolean("scoringPlay") -> "goal"
                        d.optBoolean("penaltyKick") -> "penmiss"
                        else -> continue
                    }
                    evs.add(MatchEv(
                        d.optJSONObject("clock")?.optString("displayValue").orEmpty(), kind,
                        d.optJSONObject("team")?.optString("id").orEmpty(),
                        d.optJSONArray("athletesInvolved")?.optJSONObject(0)?.optString("displayName").orEmpty()
                    ))
                }
                fun score(c: JSONObject) = c.optString("score").toIntOrNull()
                val ht = h.getJSONObject("team"); val at = a.getJSONObject("team")
                val state = type.optString("state", "pre")
                out.add(Match(
                    e.getString("id"), slug, leagueName,
                    ht.optString("id"), ht.optString("displayName"), ht.optString("logo"), score(h),
                    at.optString("id"), at.optString("displayName"), at.optString("logo"), score(a),
                    state,
                    if (state == "in") st.optString("displayClock") else type.optString("shortDetail"),
                    parseTime(e.optString("date")), evs
                ))
            } catch (ex: Exception) { /* skip one malformed event */ }
        }
        out
    }
}
