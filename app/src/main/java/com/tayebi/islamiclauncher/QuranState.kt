package com.tayebi.islamiclauncher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tayebi.islamiclauncher.data.AyahHit
import com.tayebi.islamiclauncher.data.Chapter
import com.tayebi.islamiclauncher.data.QuranRepo
import com.tayebi.islamiclauncher.data.Verse
import kotlinx.coroutines.launch

class QuranState(private val e: Engine) {
    val repo = QuranRepo(e.app)

    var chapters by mutableStateOf<List<Chapter>>(emptyList())
    var chaptersFailed by mutableStateOf(false)
    var open by mutableIntStateOf(0) // 0 = surah list
    var verses by mutableStateOf<List<Verse>>(emptyList())
    var translations by mutableStateOf<List<String>>(emptyList())
    var tafsir by mutableStateOf<Map<String, String>>(emptyMap())
    var loading by mutableStateOf(false)
    var failed by mutableStateOf(false)
    var startAyah by mutableIntStateOf(0)
    var hits by mutableStateOf<List<AyahHit>?>(null)
    var searching by mutableStateOf(false)
    var dlProgress by mutableIntStateOf(-1)
    var dlSurah by mutableIntStateOf(0)

    init {
        e.audio.onEnded = {
            val next = e.audio.surah + 1
            if (e.audio.surah in 1..113) {
                play(next)
                true
            } else false
        }
    }

    fun loadChapters() {
        if (chapters.isNotEmpty()) return
        chaptersFailed = false
        e.scope.launch {
            try {
                chapters = repo.chapters()
            } catch (ex: Exception) {
                chaptersFailed = true
            }
        }
    }

    fun openSurah(n: Int, ayahIndex: Int = 0) {
        open = n
        startAyah = ayahIndex
        verses = emptyList()
        translations = emptyList()
        tafsir = emptyMap()
        failed = false
        loading = true
        e.scope.launch {
            try {
                verses = repo.verses(n)
                loading = false
                e.s.lastSurah.set(n)
                loadExtras()
            } catch (ex: Exception) {
                loading = false
                failed = true
            }
        }
    }

    fun loadExtras() {
        val n = open
        if (n == 0) return
        e.scope.launch {
            try {
                if (e.s.showTr.value && translations.isEmpty()) {
                    val id = if (e.s.lang.value == "fr") QuranRepo.TR_FR else QuranRepo.TR_EN
                    translations = repo.translation(n, id)
                }
                if (e.s.showTafsir.value && tafsir.isEmpty()) {
                    tafsir = repo.tafsir(n, QuranRepo.TAFSIR_MUYASSAR, verses.map { it.key })
                }
            } catch (ex: Exception) {
                e.toast(e.t("error_generic"))
            }
        }
    }

    fun toggleTr() {
        e.s.showTr.set(!e.s.showTr.value)
        loadExtras()
    }

    fun toggleTafsir() {
        e.s.showTafsir.set(!e.s.showTafsir.value)
        loadExtras()
    }

    fun savePosition(index: Int) {
        if (open != 0) {
            e.s.lastSurah.set(open)
            e.s.lastAyah.set(index)
        }
    }

    fun close() {
        open = 0
    }

    fun searchAyahs(q: String) {
        if (q.isBlank()) return
        searching = true
        e.scope.launch {
            try {
                hits = repo.search(q.trim())
            } catch (ex: Exception) {
                hits = emptyList()
                e.toast(e.t("error_generic"))
            }
            searching = false
        }
    }

    fun play(surah: Int = open) {
        if (surah !in 1..114) return
        e.scope.launch {
            try {
                val srv = e.s.reciterServer.value
                if (srv.isNotBlank()) {
                    val avail = e.s.reciterSurahs.value.split(",").mapNotNull { it.toIntOrNull() }
                    if (avail.isNotEmpty() && surah !in avail) { e.toast(e.t("surah_unavailable")); return@launch }
                    val f = repo.localAudio(srv.hashCode(), surah)
                    val uri = if (f.exists() && f.length() > 0) android.net.Uri.fromFile(f) else android.net.Uri.parse(com.tayebi.islamiclauncher.data.ReciterRepo.audioUrl(srv, surah))
                    e.audio.play(uri, surah)
                } else {
                    val uri = repo.audioUri(e.s.reciter.value, surah)
                    e.audio.play(uri, surah)
                }
            } catch (ex: Exception) {
                e.toast(e.t("audio_error"))
            }
        }
    }

    fun togglePlay() {
        if (e.audio.surah == open && (e.audio.isPlaying || e.audio.player.playbackState != androidx.media3.common.Player.STATE_IDLE)) {
            e.audio.toggle()
        } else play(open)
    }

    fun download() {
        val n = open
        if (n == 0 || dlProgress >= 0) return
        dlSurah = n
        dlProgress = 0
        e.scope.launch {
            try {
                val srv = e.s.reciterServer.value
                if (srv.isNotBlank()) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        com.tayebi.islamiclauncher.data.Http.download(com.tayebi.islamiclauncher.data.ReciterRepo.audioUrl(srv, n), repo.localAudio(srv.hashCode(), n)) { dlProgress = it }
                    }
                } else repo.download(e.s.reciter.value, n) { dlProgress = it }
                e.toast(e.t("downloaded"))
            } catch (ex: Exception) {
                e.toast(e.t("error_generic"))
            }
            dlProgress = -1
        }
    }
}
