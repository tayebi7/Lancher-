package com.tayebi.islamiclauncher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tayebi.islamiclauncher.Engine

@Composable
fun RadioScreen(e: Engine) {
    val r = e.radio
    LaunchedEffect(Unit) { r.load(r.tab) }
    ScreenFrame(s("radio")) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.u), modifier = Modifier.fillMaxWidth()) {
            listOf("radio_quran", "radio_islamic", "radio_algeria").forEachIndexed { i, k ->
                Button((if (r.tab == i) "● " else "") + s(k), Modifier.weight(1f)) { r.load(i) }
            }
        }
        Spacer(Modifier.height(14.u))
        r.current?.let { st ->
            Row(Modifier.fillMaxWidth().padding(bottom = 14.u), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.u)) {
                val playing = e.audio.isPlaying
                T((if (playing) "▶ " else "⏸ ") + s("now_playing") + ": " + st.name, 28, C.Gold, FontWeight.Bold, Modifier.weight(1f), maxLines = 1)
                Button(if (playing) s("pause") else s("play"), Modifier.width(240.u)) { e.audio.toggle() }
                Button(s("stop"), Modifier.width(240.u)) { r.stop() }
            }
        }
        when {
            r.loading -> T(s("loading"), 30, C.GoldSoft)
            r.failed -> {
                T(s("no_data"), 30, C.GoldSoft)
                Spacer(Modifier.height(12.u))
                Button(s("retry"), Modifier.width(300.u)) { r.load(r.tab, true) }
            }
            else -> LazyVerticalGrid(GridCells.Fixed(3), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.u), verticalArrangement = Arrangement.spacedBy(14.u)) {
                items(r.list, key = { it.url }) { st ->
                    FocusBox(Modifier.height(100.u), onClick = { r.play(st) }) {
                        Row(Modifier.fillMaxSize().padding(horizontal = 22.u), verticalAlignment = Alignment.CenterVertically) {
                            T("📻", 34, C.Gold, modifier = Modifier.width(60.u))
                            T(st.name, 26, if (r.current?.url == st.url) C.Gold else C.White, FontWeight.Medium, Modifier.weight(1f), maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}
