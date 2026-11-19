package com.tayebi.islamiclauncher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.tayebi.islamiclauncher.Engine
import com.tayebi.islamiclauncher.HView
import com.tayebi.islamiclauncher.data.Hadith
import com.tayebi.islamiclauncher.data.HadithRepo

@Composable
fun HadithScreen(e: Engine) {
    val h = e.h
    val lang = LocalLang.current
    ScreenFrame(s("hadith")) {
        when (h.view) {
            HView.Books -> {
                Button("★ " + s("fav_hadiths") + " (${h.favs.size})", Modifier.width(520.u)) { h.view = HView.Favs }
                Spacer(Modifier.height(14.u))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.u)) {
                    items(HadithRepo.EDITIONS) { ed ->
                        FocusBox(Modifier.fillMaxWidth().height(90.u), focusScale = 1f, onClick = { h.openEdition(ed) }) {
                            Row(Modifier.fillMaxSize().padding(horizontal = 28.u), verticalAlignment = Alignment.CenterVertically) {
                                T("📜", 40); Spacer(Modifier.width(18.u))
                                T(if (lang == "ar") ed.ar else ed.en, 36, C.White, FontWeight.Bold)
                            }
                        }
                    }
                    item { T(s("hadith_src"), 20, C.Muted) }
                }
            }
            HView.Sections -> {
                var query by remember { mutableStateOf("") }
                val b = h.book
                if (h.loading) {
                    T(s("first_download") + " ${h.progress}%", 28, C.GoldSoft)
                } else if (b == null) {
                    T(s("no_data"), 30, C.GoldSoft)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.u)) {
                        Button(s("retry"), Modifier.width(300.u)) { h.edition?.let { h.openEdition(it) } }
                        Button(s("back"), Modifier.width(300.u)) { h.back() }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.u)) {
                        Button(s("back"), Modifier.width(240.u)) { h.back() }
                        TextInput(query, { query = it }, s("search_hadith"), Modifier.width(900.u), onSearch = { h.search(query) })
                        Button(s("search_ayah").let { "🔍" }, Modifier.width(120.u)) { h.search(query) }
                    }
                    T(b.title, 34, C.GoldSoft)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.u)) {
                        items(b.sections) { (no, name) ->
                            FocusBox(Modifier.fillMaxWidth().height(76.u), focusScale = 1f, onClick = { h.openSection(no) }) {
                                Row(Modifier.fillMaxSize().padding(horizontal = 24.u), verticalAlignment = Alignment.CenterVertically) {
                                    T(no.toString(), 28, C.Gold, FontWeight.Bold, Modifier.width(80.u))
                                    T(name.ifBlank { "—" }, 28, C.White, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
            HView.List -> {
                val b = h.book
                if (b != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.u)) {
                        Button(s("back"), Modifier.width(240.u)) { h.back() }
                        Button("EN " + s("translation") + if (h.showEn) " ✓" else "", Modifier.width(420.u)) { h.toggleEnglish() }
                    }
                    Spacer(Modifier.height(10.u))
                    HadithList(e, b.items.filter { it.book == h.section }, b.edition, b.title)
                }
            }
            HView.Search -> {
                val b = h.book
                Button(s("back"), Modifier.width(240.u)) { h.back() }
                Spacer(Modifier.height(10.u))
                if (b != null) HadithList(e, h.results, b.edition, b.title)
            }
            HView.Favs -> {
                Button(s("back"), Modifier.width(240.u)) { h.view = HView.Books }
                Spacer(Modifier.height(10.u))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.u)) {
                    items(h.favs) { f ->
                        HadithCard(e, Hadith(f.number, 0, f.text, f.grade), f.edition, f.bookTitle)
                    }
                }
            }
        }
    }
}

@Composable
private fun HadithList(e: Engine, list: List<Hadith>, ed: String, title: String) {
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.u)) {
        items(list) { HadithCard(e, it, ed, title) }
    }
}

@Composable
private fun HadithCard(e: Engine, hd: Hadith, ed: String, title: String) {
    val h = e.h
    val fav = h.isFav(ed, hd.number)
    val en = if (h.showEn) h.english[hd.number] else null
    FocusBox(Modifier.fillMaxWidth(), focusScale = 1f, bg = Color(0x88081A3C), onClick = { h.toggleFav(ed, title, hd) }) {
        Column(Modifier.fillMaxWidth().padding(24.u)) {
            T(hd.text, 34, C.White, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.u))
            com.tayebi.islamiclauncher.data.HadithRepo.narrator(en)?.let { T(s("narrator") + ": " + it, 24, C.GoldSoft) }
            if (en != null) T(en, 24, C.Muted)
            T(s("source") + ": " + title + " • " + s("hadith_no") + " " + hd.number, 22, C.Gold)
            if (hd.grade.isNotBlank()) T(s("grade") + ": " + hd.grade, 22, C.GoldSoft)
            T(if (fav) "★ " + s("saved") else "☆ " + s("save"), 24, if (fav) C.Gold else C.Muted)
        }
    }
}
