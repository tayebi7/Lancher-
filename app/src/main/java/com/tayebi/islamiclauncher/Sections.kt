package com.tayebi.islamiclauncher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.RingtoneManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tayebi.islamiclauncher.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Quran reciter directory + selection + favourites. */
class ReciterState(private val e: Engine) {
    private val repo = ReciterRepo(e.app)
    var all by mutableStateOf<List<Reciter>>(emptyList())
    var loading by mutableStateOf(false)
    var failed by mutableStateOf(false)
    var query by mutableStateOf("")
    var favsOnly by mutableStateOf(false)

    fun load(force: Boolean = false) {
        if (all.isNotEmpty() && !force) return
        loading = true; failed = false
        e.scope.launch {
            try { all = repo.load() } catch (ex: Exception) { failed = true }
            loading = false
        }
    }

    private fun key(r: Reciter) = r.name + "|" + r.server

    fun favorites(): List<Reciter> = e.s.reciterFavs.value.split("\n").mapNotNull {
        val p = it.split("|")
        if (p.size >= 3) Reciter(p[0], "", p[1], p[2].split(",").mapNotNull { n -> n.toIntOrNull() }.toSet()) else null
    }

    fun isFav(r: Reciter) = favorites().any { it.server == r.server }

    fun toggleFav(r: Reciter) {
        val l = favorites().toMutableList()
        if (!l.removeAll { it.server == r.server }) l.add(0, r)
        e.s.reciterFavs.set(l.joinToString("\n") { it.name.replace("|", " ") + "|" + it.server + "|" + it.surahs.joinToString(",") })
    }

    fun select(r: Reciter) {
        e.s.reciterName.set(r.name + if (r.moshaf.isNotBlank()) " — " + r.moshaf else "")
        e.s.reciterServer.set(r.server)
        e.s.reciterSurahs.set(r.surahs.joinToString(","))
        e.toast(e.t("reciter_selected") + ": " + r.name)
    }

    fun currentReciter(): Reciter? = e.s.reciterServer.value.takeIf { it.isNotBlank() }?.let {
        Reciter(e.s.reciterName.value, "", it, e.s.reciterSurahs.value.split(",").mapNotNull { n -> n.toIntOrNull() }.toSet())
    }

    fun filtered(): List<Reciter> {
        val src = if (favsOnly) favorites() else all
        val q = query.trim()
        return if (q.isEmpty()) src else src.filter { it.name.contains(q, true) || it.moshaf.contains(q, true) }
    }
}

class AdhkarState(private val e: Engine) {
    private val repo = AdhkarRepo(e.app)
    var cats by mutableStateOf<List<AdhkarCategory>>(emptyList())
    var open by mutableIntStateOf(-1)
    var loading by mutableStateOf(false)
    var failed by mutableStateOf(false)

    fun load() {
        if (cats.isNotEmpty()) return
        loading = true; failed = false
        e.scope.launch {
            try { cats = repo.load() } catch (ex: Exception) { failed = true }
            loading = false
        }
    }
}

class NewsState(private val e: Engine) {
    private val repo = NewsRepo(e.app)
    var items by mutableStateOf<List<NewsItem>>(emptyList())
    var loading by mutableStateOf(false)
    var failed by mutableStateOf(false)
    var updatedMs by mutableStateOf(0L)
    var reading by mutableStateOf<NewsItem?>(null)

    fun load(force: Boolean = false) {
        val cat = NewsRepo.CATEGORIES.getOrNull(e.s.newsCat.value) ?: return
        loading = true; failed = false
        e.scope.launch {
            try {
                val r = repo.load(cat.second, force)
                items = r; failed = r.isEmpty(); updatedMs = System.currentTimeMillis()
            } catch (ex: Exception) { failed = true }
            loading = false
        }
    }

    fun selectCat(i: Int) { e.s.newsCat.set(i); items = emptyList(); load() }
}

class CurrencyState(private val e: Engine) {
    private val repo = CurrencyRepo(e.app)
    var rates by mutableStateOf<Rates?>(null)
    var loading by mutableStateOf(false)
    var failed by mutableStateOf(false)

    fun load(force: Boolean = false) {
        loading = true; failed = false
        e.scope.launch {
            try { rates = repo.load(force) } catch (ex: Exception) { failed = rates == null }
            loading = false
        }
    }
}

class ScoresState(private val e: Engine) {
    var matches by mutableStateOf<List<Match>>(emptyList())
    var loading by mutableStateOf(false)
    var failed by mutableStateOf(false)
    var tab by mutableIntStateOf(0)   // 0 live, 1 upcoming, 2 finished
    var updatedMs by mutableStateOf(0L)
    private val seen = HashSet<String>()
    private val known = HashSet<String>()
    private var lastPollMs = 0L

    fun leagues(): List<String> = e.s.scoreLeagues.value.split(",").filter { it.isNotBlank() }
    fun favTeams(): Set<String> = e.s.scoreTeams.value.split("\n").filter { it.isNotBlank() }.toSet()

    private val dfmt = DateTimeFormatter.ofPattern("yyyyMMdd")
    private fun day(offset: Long) = LocalDate.now().plusDays(offset).format(dfmt)

    private suspend fun fetchLeagues(dates: List<String?>): List<Match> = withContext(Dispatchers.IO) {
        leagues().flatMap { slug -> dates.map { d -> async { try { ScoresRepo.fetch(slug, d) } catch (ex: Exception) { null } } } }
            .awaitAll()
    }.let { res -> res.filterNotNull().flatten().distinctBy { it.id } }

    fun load() {
        loading = true; failed = false
        e.scope.launch {
            val r = fetchLeagues(listOf(day(-1), null, day(1), day(2)))
            if (r.isEmpty() && matches.isEmpty()) failed = true else { matches = r; updatedMs = System.currentTimeMillis(); detect(r, silent = true) }
            loading = false
        }
    }

    /** Called by the engine ticker: cheap poll of today's matches to find new goals / cards. */
    fun poll(now: Long) {
        if (!e.s.goalAlerts.value || !e.online || now - lastPollMs < 90_000) return
        lastPollMs = now
        e.scope.launch {
            val r = fetchLeagues(listOf(null))
            if (r.isEmpty()) return@launch
            val merged = (r + matches).distinctBy { it.id }
            matches = merged
            updatedMs = System.currentTimeMillis()
            detect(r, silent = false)
        }
    }

    private fun relevant(m: Match): Boolean {
        val f = favTeams()
        return f.isEmpty() || m.home in f || m.away in f
    }

    private fun detect(list: List<Match>, silent: Boolean) {
        for (m in list) {
            val first = known.add(m.id)
            for (ev in m.events) {
                val k = m.id + "|" + ev.minute + "|" + ev.kind + "|" + ev.player
                if (seen.add(k) && !first && !silent && m.state == "in" && relevant(m)) alert(m, ev)
            }
        }
    }

    private fun alert(m: Match, ev: MatchEv) {
        val team = if (ev.teamId == m.homeId) m.home else m.away
        val score = "${m.home} ${m.homeScore ?: 0} - ${m.awayScore ?: 0} ${m.away}"
        val head = when (ev.kind) {
            "red" -> e.t("alert_red")
            "pen" -> e.t("alert_pen_goal")
            "penmiss" -> e.t("alert_pen")
            "own" -> e.t("alert_own")
            else -> e.t("alert_goal")
        }
        val who = if (ev.player.isNotBlank()) " — ${ev.player}" else ""
        val msg = "$head  $team$who ${ev.minute}\n$score"
        e.showAlert(msg)
        notify(head, "$team$who ${ev.minute}  •  $score")
    }

    private fun notify(title: String, text: String) {
        try {
            val nm = e.app.getSystemService(NotificationManager::class.java) ?: return
            val ch = NotificationChannel("goals", "Goals", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(ch)
            val n = android.app.Notification.Builder(e.app, "goals")
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title).setContentText(text)
                .setAutoCancel(true).build()
            nm.notify((System.currentTimeMillis() % 100000).toInt(), n)
        } catch (ex: Throwable) { }
        if (e.s.goalSound.value) try {
            RingtoneManager.getRingtone(e.app, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))?.play()
        } catch (ex: Throwable) { }
    }

    fun toggleLeague(slug: String) {
        val l = leagues().toMutableList()
        if (!l.remove(slug)) l.add(slug)
        e.s.scoreLeagues.set(l.joinToString(","))
    }

    fun toggleTeam(name: String) {
        val l = favTeams().toMutableSet()
        if (!l.remove(name)) l.add(name)
        e.s.scoreTeams.set(l.joinToString("\n"))
    }
}
