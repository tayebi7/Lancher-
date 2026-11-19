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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.tayebi.islamiclauncher.Dlg
import com.tayebi.islamiclauncher.Engine
import com.tayebi.islamiclauncher.CityTarget
import com.tayebi.islamiclauncher.i18n.LANGS
import com.tayebi.islamiclauncher.i18n.methodLabel
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ScreenFrame(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().padding(start = 40.u, end = 40.u, top = 40.u, bottom = 40.u)) {
        T(title, 56, C.Gold, FontWeight.Bold)
        Spacer(Modifier.height(18.u))
        content()
    }
}

@Composable
fun AppsScreen(e: Engine) {
    ScreenFrame(s("apps")) {
        T(s("hint_long"), 22, C.Muted)
        Spacer(Modifier.height(10.u))
        LazyVerticalGrid(GridCells.Fixed(6), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.u), verticalArrangement = Arrangement.spacedBy(16.u)) {
            items(e.installed, key = { it.pkg }) { a ->
                AppTile(a, Modifier.height(170.u), onLong = { e.dlg = Dlg.AppMenu(a, e.favPkgs().contains(a.pkg)) }) { e.launch(a) }
            }
        }
    }
}

@Composable
fun FavoritesScreen(e: Engine) {
    ScreenFrame(s("favorites")) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.u)) {
            Button(s("add_app"), Modifier.width(420.u)) { e.dlg = Dlg.PickFav }
            Spacer(Modifier.width(10.u))
            T(s("hint_long"), 22, C.Muted, modifier = Modifier.padding(top = 20.u))
        }
        Spacer(Modifier.height(16.u))
        val favs = e.favApps()
        if (favs.isEmpty()) T(s("empty_fav"), 32, C.GoldSoft)
        LazyVerticalGrid(GridCells.Fixed(6), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.u), verticalArrangement = Arrangement.spacedBy(16.u)) {
            items(favs, key = { it.pkg }) { a ->
                AppTile(a, Modifier.height(170.u), onLong = { e.dlg = Dlg.AppMenu(a, true) }) { e.launch(a) }
            }
        }
    }
}

@Composable
fun PrayerScreen(e: Engine) {
    val lang = LocalLang.current
    ScreenFrame(s("prayer") + " — " + e.prayerPlace().full()) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(30.u)) {
            PrayerPanel(e, Modifier.weight(1f).height(640.u))
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.u)) {
                item { Button(s("prayer_city") + ": " + e.prayerPlace().short(), Modifier.fillMaxWidth()) { e.dlg = Dlg.City(CityTarget.Prayer) } }
                item { Button(s("calc_method") + ": " + methodLabel(e.s.method.value, lang), Modifier.fillMaxWidth()) { e.dlg = Dlg.Method } }
                item { Button(s("school") + ": " + s("school_${e.s.school.value}"), Modifier.fillMaxWidth()) { e.dlg = Dlg.School } }
                item { Button(s("tune"), Modifier.fillMaxWidth()) { e.dlg = Dlg.Tune } }
                item { Button(s("adhan") + ": " + (if (e.s.adhanOn.value) s("on") else s("off")), Modifier.fillMaxWidth()) { e.s.adhanOn.set(!e.s.adhanOn.value) } }
                item { Button(s("test_adhan"), Modifier.fillMaxWidth()) { e.testAdhan() } }
            }
        }
    }
}

private class Row3(val title: String, val value: String, val action: () -> Unit)

@Composable
fun SettingsScreen(e: Engine) {
    val lang = LocalLang.current
    val net = openNetworkSettings()
    val isHome = e.isDefaultHome()
    val rows = listOf(
        Row3(s("launcher_status"), if (isHome) s("launcher_on") else s("launcher_off")) { e.openHomeChooser() },
        Row3(s("prayer_city"), e.prayerPlace().full()) { e.dlg = Dlg.City(CityTarget.Prayer) },
        Row3(s("weather_city"), if (e.s.weatherSame.value) s("same_as_prayer") else e.weatherPlace().full()) { e.dlg = Dlg.City(CityTarget.Weather) },
        Row3(s("same_as_prayer"), if (e.s.weatherSame.value) s("on") else s("off")) { e.setWeatherSame(!e.s.weatherSame.value) },
        Row3(s("calc_method"), methodLabel(e.s.method.value, lang)) { e.dlg = Dlg.Method },
        Row3(s("school"), s("school_${e.s.school.value}")) { e.dlg = Dlg.School },
        Row3(s("tune"), e.s.tuneList().joinToString(" / ")) { e.dlg = Dlg.Tune },
        Row3(s("adhan"), if (e.s.adhanOn.value) s("on") else s("off")) { e.s.adhanOn.set(!e.s.adhanOn.value) },
        Row3(s("adhan_url"), e.s.adhanUrl.value) { e.dlg = Dlg.AdhanUrl },
        Row3(s("test_adhan"), "▶") { e.testAdhan() },
        Row3(s("hijri_adj"), e.s.hijriAdj.value.toString()) { e.dlg = Dlg.HijriAdj },
        Row3(s("volume"), "${e.s.volume.value}%") { e.dlg = Dlg.Volume },
        Row3(s("manage_fav"), e.favPkgs().size.toString()) { e.go(com.tayebi.islamiclauncher.Screen.Favorites) },
        Row3(s("background"), s("bg_${e.s.bg.value.coerceIn(0, 8)}")) { e.dlg = Dlg.Bg },
        Row3(s("language"), LANGS.first { it.first == lang }.second) { e.dlg = Dlg.Lang },
        Row3(s("internet"), if (e.online) s("connected") else s("disconnected")) { net() },
        Row3(s("refresh_now"), "⟳") { e.refreshAll() },
        Row3(s("more_apps"), "Tayebi Abdelghani") { e.dlg = Dlg.Developer }
    )
    ScreenFrame(s("settings")) {
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.u)) {
            items(rows) { r ->
                FocusBox(Modifier.fillMaxWidth().height(84.u), focusScale = 1f, onClick = r.action) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 28.u), verticalAlignment = Alignment.CenterVertically) {
                        T(r.title, 30, C.White, FontWeight.Bold, Modifier.weight(1f), maxLines = 1)
                        T(r.value, 26, C.GoldSoft, maxLines = 1, modifier = Modifier.weight(1.3f))
                    }
                }
            }
        }
    }
}
