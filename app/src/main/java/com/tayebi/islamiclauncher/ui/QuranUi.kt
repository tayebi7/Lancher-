package com.tayebi.islamiclauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.tayebi.islamiclauncher.Dlg
import com.tayebi.islamiclauncher.Engine
import com.tayebi.islamiclauncher.data.QuranRepo
import com.tayebi.islamiclauncher.i18n.arabicDigits

@Composable
fun QuranScreen(e: Engine) {
    if (e.q.open == 0) SurahList(e) else Reader(e)
}

@Composable
private fun SurahList(e: Engine) {
    val q = e.q
    var query by remember { mutableStateOf("") }
    val list = if (query.isBlank()) q.chapters else q.chapters.filter {
        it.nameAr.contains(query.trim()) || it.nameEn.contains(query.trim(), true) || it.id.toString() == query.trim()
    }
    ScreenFrame(s("quran")) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.u), verticalAlignment = Alignment.CenterVertically) {
            TextInput(query, { query = it }, s("search_surah"), Modifier.width(520.u), onSearch = {})
            Button(s("search_ayah"), Modifier.width(300.u)) { q.searchAyahs(query) }
            Button("🎙 " + s("search_reciter"), Modifier.width(360.u)) { e.go(com.tayebi.islamiclauncher.Screen.Reciters) }
            if (e.s.lastSurah.value > 0) {
                Button(s("continue_reading"), Modifier.width(300.u)) { q.openSurah(e.s.lastSurah.value, e.s.lastAyah.value) }
            }
        }
        Spacer(Modifier.height(14.u))
        val hits = q.hits
        if (hits != null) {
            T(s("ayah_search_title"), 30, C.Gold)
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.u)) {
                items(hits.size) { i ->
                    val h = hits[i]
                    FocusBox(Modifier.fillMaxWidth(), focusScale = 1f, onClick = {
                        val p = h.key.split(":")
                        q.hits = null
                        q.openSurah(p[0].toInt(), (p.getOrNull(1)?.toIntOrNull() ?: 1) - 1)
                    }) {
                        Column(Modifier.padding(20.u)) {
                            T(h.key, 24, C.Gold, FontWeight.Bold)
                            T(h.text, 32, C.White)
                        }
                    }
                }
                item { Button(s("back"), Modifier.width(300.u)) { q.hits = null } }
            }
        } else if (q.chapters.isEmpty()) {
            T(if (q.chaptersFailed) s("no_data") else s("loading"), 30, C.GoldSoft)
            if (q.chaptersFailed) Button(s("retry"), Modifier.width(300.u)) { q.loadChapters() }
        } else {
            LazyVerticalGrid(GridCells.Fixed(3), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.u), verticalArrangement = Arrangement.spacedBy(14.u)) {
                items(list, key = { it.id }) { c ->
                    FocusBox(Modifier.height(110.u), onClick = { q.openSurah(c.id) }) {
                        Row(Modifier.fillMaxSize().padding(horizontal = 22.u), verticalAlignment = Alignment.CenterVertically) {
                            T(c.id.toString(), 30, C.Gold, FontWeight.Bold, Modifier.width(70.u))
                            Column(Modifier.weight(1f)) {
                                T(c.nameAr, 36, C.White, FontWeight.Bold, maxLines = 1)
                                T(c.nameEn, 20, C.Muted, maxLines = 1)
                            }
                            T("${c.verses} ${s("ayahs")}", 22, C.GoldSoft, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Reader(e: Engine) {
    val q = e.q
    val chapter = q.chapters.firstOrNull { it.id == q.open }
    val state: LazyListState = rememberLazyListState()
    LaunchedEffect(q.verses.size) {
        if (q.verses.isNotEmpty() && q.startAyah in q.verses.indices) state.scrollToItem(q.startAyah)
    }
    val playingThis = e.audio.surah == q.open && e.audio.isPlaying
    val downloaded = q.repo.localAudio(e.s.reciterServer.value.takeIf { it.isNotBlank() }?.hashCode() ?: e.s.reciter.value, q.open).exists()
    ScreenFrame((chapter?.nameAr ?: "") + "  •  " + q.open) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.u), modifier = Modifier.fillMaxWidth()) {
            Button(s("back"), Modifier.weight(0.7f)) { q.close() }
            Button(if (playingThis) "⏸ " + s("pause") else "▶ " + s("play"), Modifier.weight(1f)) { q.togglePlay() }
            Button("🎙 " + (e.s.reciterName.value.ifBlank { QuranRepo.RECITERS.firstOrNull { it.first == e.s.reciter.value }?.second ?: s("reciter") }), Modifier.weight(1.6f)) { e.go(com.tayebi.islamiclauncher.Screen.Reciters) }
            Button(if (q.dlProgress >= 0 && q.dlSurah == q.open) sf("downloading", q.dlProgress) else if (downloaded) "✓ " + s("downloaded") else "⬇ " + s("download"), Modifier.weight(1.2f)) { q.download() }
            Button(s("translation") + if (e.s.showTr.value) " ✓" else "", Modifier.weight(0.9f)) { q.toggleTr() }
            Button(s("tafsir") + if (e.s.showTafsir.value) " ✓" else "", Modifier.weight(0.8f)) { q.toggleTafsir() }
        }
        if (e.audio.surah == q.open && e.audio.durMs > 0) {
            Spacer(Modifier.height(10.u))
            Box(Modifier.fillMaxWidth().height(6.u).background(Color(0x44FFFFFF))) {
                Box(Modifier.fillMaxWidth((e.audio.posMs.toFloat() / e.audio.durMs).coerceIn(0f, 1f)).height(6.u).background(C.Gold))
            }
        }
        Spacer(Modifier.height(10.u))
        when {
            q.loading -> T(s("loading"), 30, C.GoldSoft)
            q.failed -> {
                T(s("no_data"), 30, C.GoldSoft)
                Button(s("retry"), Modifier.width(300.u)) { q.openSurah(q.open) }
            }
            else -> LazyColumn(Modifier.fillMaxSize(), state = state, verticalArrangement = Arrangement.spacedBy(10.u)) {
                itemsIndexed(q.verses) { i, v ->
                    val num = v.key.substringAfter(":").toIntOrNull() ?: (i + 1)
                    FocusBox(Modifier.fillMaxWidth(), focusScale = 1f, bg = Color(0x77081A3C), onClick = { q.togglePlay() }) { f ->
                        if (f) q.savePosition(i)
                        Column(Modifier.fillMaxWidth().padding(24.u)) {
                            T(v.text + "  ﴿" + arabicDigits(num) + "﴾", 44, C.White, FontWeight.Medium, Modifier.fillMaxWidth(), font = Fonts.Quran)
                            q.translations.getOrNull(i)?.takeIf { e.s.showTr.value && it.isNotBlank() }?.let {
                                Spacer(Modifier.height(8.u)); T(it, 26, C.GoldSoft, modifier = Modifier.fillMaxWidth(), align = TextAlign.Start)
                            }
                            q.tafsir[v.key]?.takeIf { e.s.showTafsir.value && it.isNotBlank() }?.let {
                                Spacer(Modifier.height(8.u)); T(it, 26, C.Muted, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}
