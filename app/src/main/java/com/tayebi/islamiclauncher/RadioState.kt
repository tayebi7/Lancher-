package com.tayebi.islamiclauncher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tayebi.islamiclauncher.data.RadioCategory
import com.tayebi.islamiclauncher.data.RadioRepo
import com.tayebi.islamiclauncher.data.Station
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RadioState(private val e: Engine) {
    var group by mutableIntStateOf(0)   // 0 Arabic, 1 Foreign, 2 Favourites
    var tab by mutableIntStateOf(0)
    private val cache = HashMap<String, List<Station>>()
    var list by mutableStateOf<List<Station>>(emptyList())
    var loading by mutableStateOf(false)
    var failed by mutableStateOf(false)
    var current by mutableStateOf<Station?>(null)
    var playError by mutableStateOf(false)

    fun categories(): List<RadioCategory> = if (group == 1) RadioRepo.FOREIGN else RadioRepo.ARABIC

    fun favorites(): List<Station> = e.s.radioFavs.value.split("\n").mapNotNull {
        val p = it.split("|")
        if (p.size >= 2 && p[1].startsWith("http")) Station(p[0], p[1], p.getOrElse(2) { "" }) else null
    }

    fun isFav(st: Station) = favorites().any { it.url == st.url }

    fun toggleFav(st: Station) {
        val l = favorites().toMutableList()
        if (!l.removeAll { it.url == st.url }) l.add(0, st)
        e.s.radioFavs.set(l.joinToString("\n") { it.name.replace("|", " ").replace("\n", " ") + "|" + it.url + "|" + it.logo })
    }

    fun select(g: Int, t: Int = 0) {
        group = g; tab = t
        load()
    }

    fun load(force: Boolean = false) {
        failed = false
        if (group == 2) { list = favorites(); loading = false; return }
        val cat = categories().getOrNull(tab) ?: return
        if (!force && cache[cat.key] != null) { list = cache[cat.key]!!; loading = false; return }
        list = emptyList(); loading = true
        val g = group; val t = tab
        e.scope.launch {
            try {
                val r = withContext(Dispatchers.IO) { cat.loader() }
                cache[cat.key] = r
                if (group == g && tab == t) { list = r; failed = r.isEmpty() }
            } catch (ex: Exception) {
                if (group == g && tab == t) failed = true
            }
            if (group == g && tab == t) loading = false
        }
    }

    fun play(st: Station) {
        current = st
        playError = false
        e.audio.playStream(android.net.Uri.parse(st.url))
    }

    fun stop() { current = null; playError = false; e.audio.stop() }

    fun volume(delta: Int) {
        e.s.volume.set((e.s.volume.value + delta).coerceIn(0, 100))
        e.audio.applyVolume()
    }
}
