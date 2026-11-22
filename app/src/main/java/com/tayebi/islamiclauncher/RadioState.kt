package com.tayebi.islamiclauncher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tayebi.islamiclauncher.data.RadioRepo
import com.tayebi.islamiclauncher.data.Station
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RadioState(private val e: Engine) {
    var tab by mutableIntStateOf(0) // 0 Quran, 1 Islamic, 2 Algeria
    private val cache = HashMap<Int, List<Station>>()
    var list by mutableStateOf<List<Station>>(emptyList())
    var loading by mutableStateOf(false)
    var failed by mutableStateOf(false)
    var current by mutableStateOf<Station?>(null)

    fun load(t: Int = tab, force: Boolean = false) {
        tab = t
        if (!force && cache[t] != null) { list = cache[t]!!; failed = false; return }
        list = emptyList(); failed = false; loading = true
        e.scope.launch {
            try {
                val r = withContext(Dispatchers.IO) { when (t) { 0 -> RadioRepo.quran(); 1 -> RadioRepo.islamic(); else -> RadioRepo.algeria() } }
                cache[t] = r
                if (tab == t) { list = r; failed = r.isEmpty() }
            } catch (ex: Exception) {
                if (tab == t) failed = true
            }
            loading = false
        }
    }

    fun play(st: Station) {
        current = st
        e.audio.playStream(android.net.Uri.parse(st.url))
    }

    fun stop() { current = null; e.audio.stop() }
}
