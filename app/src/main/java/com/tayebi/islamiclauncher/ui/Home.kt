package com.tayebi.islamiclauncher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.tayebi.islamiclauncher.CityTarget
import com.tayebi.islamiclauncher.Dlg
import com.tayebi.islamiclauncher.Engine
import com.tayebi.islamiclauncher.Screen
import com.tayebi.islamiclauncher.data.AppInfo
import com.tayebi.islamiclauncher.i18n.gregorianLine
import com.tayebi.islamiclauncher.i18n.hijriLine
import com.tayebi.islamiclauncher.i18n.weatherEmoji
import com.tayebi.islamiclauncher.i18n.weatherText
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val PRAYER_KEYS = listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")
val PRAYER_EMOJI = listOf("🌄", "🌅", "☀️", "🌤️", "🌇", "🌙")
private val HM = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

fun fmtCountdown(sec: Long): String {
    val s = sec.coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60)
}

@Composable
fun Sidebar(e: Engine) {
    val items = listOf(
        Triple(Screen.Home, "🏠", "home"),
        Triple(Screen.Apps, "▦", "apps"),
        Triple(Screen.Favorites, "⭐", "favorites"),
        Triple(Screen.Prayer, "🕌", "prayer"),
        Triple(Screen.Quran, "📖", "quran"),
        Triple(Screen.Hadith, "📜", "hadith"),
        Triple(Screen.Radio, "📻", "radio"),
        Triple(Screen.Settings, "⚙️", "settings")
    )
    Column(
        Modifier.width(210.u).fillMaxHeight().padding(vertical = 36.u, horizontal = 14.u),
        verticalArrangement = Arrangement.spacedBy(10.u)
    ) {
        for ((sc, icon, key) in items) {
            val sel = e.screen == sc
            FocusBox(
                Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(26.u),
                bg = if (sel) C.Gold.copy(alpha = 0.92f) else Color(0x99081A3C),
                focusBg = if (sel) C.GoldSoft else C.PanelFocus,
                focusScale = 1.04f,
                onClick = { e.go(sc) }
            ) {
                Column(Modifier.fillMaxSize().padding(6.u), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    T(icon, 44, if (sel) C.Navy else C.GoldSoft)
                    T(s(key), 22, if (sel) C.Navy else C.White, FontWeight.Bold, align = TextAlign.Center, maxLines = 2)
                }
            }
        }
    }
}

@Composable
fun Clock(e: Engine) {
    val z = ZoneId.systemDefault()
    val t = Instant.ofEpochMilli(e.nowMs).atZone(z)
    T(t.format(HM), 92, C.White, FontWeight.Bold)
}

@Composable
fun TopBar(e: Engine) {
    val lang = LocalLang.current
    val today = Instant.ofEpochMilli(e.nowMs).atZone(ZoneId.systemDefault()).toLocalDate()
    Row(Modifier.fillMaxWidth().height(110.u), verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.Start) {
            T("📅  " + gregorianLine(today, lang), 30, C.White, FontWeight.Medium)
            Spacer(Modifier.height(4.u))
            T("🌙  " + hijriLine(today, e.s.hijriAdj.value, lang), 30, C.GoldSoft, FontWeight.Medium)
        }
        Spacer(Modifier.width(24.u))
        Box(Modifier.width(2.u).height(70.u).background(C.Gold.copy(alpha = 0.6f)))
        Spacer(Modifier.width(24.u))
        Clock(e)
        Spacer(Modifier.weight(1f))
        FocusBox(
            Modifier.height(64.u).width(380.u),
            shape = RoundedCornerShape(32.u),
            onClick = { e.dlg = Dlg.City(CityTarget.Prayer) }
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = 22.u), verticalAlignment = Alignment.CenterVertically) {
                T("📍", 28)
                Spacer(Modifier.width(10.u))
                T(e.prayerPlace().short(), 28, C.White, FontWeight.Medium, Modifier.weight(1f), maxLines = 1)
                T("▾", 26, C.Gold)
            }
        }
        Spacer(Modifier.width(18.u))
        FocusBox(Modifier.size(64.u), shape = RoundedCornerShape(32.u), onClick = { e.go(Screen.Settings) }) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { T("⚙️", 34) }
        }
        Spacer(Modifier.width(18.u))
        Box(Modifier.size(64.u), contentAlignment = Alignment.Center) {
            T(if (e.online) "📶" else "🚫", 38, if (e.online) C.White else Color(0xFFFF8A80))
        }
    }
}

@Composable
fun PrayerPanel(e: Engine, modifier: Modifier = Modifier) {
    val lang = LocalLang.current
    val next = e.nextPrayer()
    val t = e.today
    FocusBox(
        modifier,
        shape = RoundedCornerShape(30.u),
        bg = Color(0xD9062A22),
        focusBg = Color(0xF00A3D31),
        focusScale = 1.02f,
        onClick = { e.go(Screen.Prayer) }
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 26.u, vertical = 18.u)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                T("🕌", 40)
                Spacer(Modifier.width(12.u))
                T(s("prayer"), 40, C.Gold, FontWeight.Bold)
            }
            Spacer(Modifier.height(10.u))
            for (i in 0..5) {
                val hi = next?.first == i
                Row(
                    Modifier.weight(1f).fillMaxWidth().padding(vertical = 2.u)
                        .background(if (hi) C.Emerald else Color(0x55000000), RoundedCornerShape(12.u))
                        .padding(horizontal = 18.u),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    T(s(PRAYER_KEYS[i]), 32, C.White, if (hi) FontWeight.Bold else FontWeight.Medium, Modifier.weight(1f))
                    T(t?.t?.get(i)?.format(HM) ?: "--:--", 32, C.White, FontWeight.Bold)
                    Spacer(Modifier.width(16.u))
                    T(PRAYER_EMOJI[i], 30)
                }
            }
            Spacer(Modifier.height(8.u))
            Box(Modifier.fillMaxWidth().height(2.u).background(C.Gold.copy(alpha = 0.4f)))
            Spacer(Modifier.height(6.u))
            if (next != null) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        T(com.tayebi.islamiclauncher.i18n.trf("remaining_to", lang, s(PRAYER_KEYS[next.first])), 26, C.GoldSoft)
                        T(fmtCountdown((next.second - e.nowMs) / 1000), 46, C.Gold, FontWeight.Bold)
                    }
                    T("⏱️", 40)
                }
            } else {
                T(if (e.prayerFailed) s("no_data") else s("loading"), 26, C.GoldSoft)
            }
            if (!e.online && t != null) StaleLine(e.prayerUpdatedMs())
        }
    }
}

@Composable
fun StaleLine(ms: Long) {
    val txt = if (ms <= 0) s("never") else Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM HH:mm", Locale.US))
    T(sf("stale", txt), 20, Color(0xFFFFB4A9))
}

@Composable
fun WeatherCard(e: Engine, modifier: Modifier = Modifier) {
    val lang = LocalLang.current
    val w = e.weather
    FocusBox(
        modifier,
        shape = RoundedCornerShape(30.u),
        bg = Color(0xCC0A2350),
        focusBg = Color(0xEE123B7A),
        focusScale = 1.02f,
        onClick = { e.dlg = Dlg.City(CityTarget.Weather) }
    ) {
        Column(Modifier.fillMaxSize().padding(24.u)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                T(e.weatherPlace().name + "  ▾", 30, C.White, FontWeight.Medium, maxLines = 1)
            }
            if (w == null) {
                Spacer(Modifier.weight(1f))
                T(if (e.weatherFailed) s("weather_none") else s("loading"), 30, C.GoldSoft, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f))
            } else {
                Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    T(weatherEmoji(w.code, w.isDay), 120)
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        T("${Math.round(w.temp)}°C", 96, C.White, FontWeight.Bold)
                        T(weatherText(w.code, lang), 34, C.White)
                    }
                }
                Box(Modifier.fillMaxWidth().height(2.u).background(C.Gold.copy(alpha = 0.35f)))
                Spacer(Modifier.height(10.u))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    T("🌡️ ${Math.round(w.hi)}°↑ ${Math.round(w.lo)}°↓", 26)
                    T("💧 ${w.humidity}%", 26)
                    T("💨 ${Math.round(w.wind)} ${s("wind")}", 26)
                }
                if (e.weatherFailed || !e.online) {
                    Spacer(Modifier.height(4.u))
                    StaleLine(w.updated)
                }
            }
        }
    }
}

@Composable
fun AppTile(app: AppInfo, modifier: Modifier, requester: FocusRequester? = null, onLong: (() -> Unit)? = null, onClick: () -> Unit) {
    FocusBox(modifier, shape = RoundedCornerShape(22.u), requester = requester, onLongClick = onLong, onClick = onClick) {
        Column(Modifier.fillMaxSize().padding(8.u), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            val ic = app.icon
            val bn = app.banner
            if (bn != null) {
                Image(bn, app.label, Modifier.weight(1f).fillMaxWidth(), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
            } else {
                if (ic != null) Image(ic, app.label, Modifier.weight(1f).fillMaxWidth()) else T("📦", 60)
                Spacer(Modifier.height(4.u))
                T(app.label, 22, C.White, FontWeight.Medium, align = TextAlign.Center, maxLines = 1)
            }
        }
    }
}

@Composable
fun AppsRow(e: Engine, firstFocus: FocusRequester) {
    val favs = e.favApps().take(6)
    // Keep the tile order left-to-right regardless of the UI language direction.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(Modifier.fillMaxWidth().height(150.u), horizontalArrangement = Arrangement.spacedBy(16.u)) {
            favs.forEachIndexed { i, a ->
                AppTile(
                    a, Modifier.weight(1f).fillMaxHeight(),
                    requester = if (i == 0) firstFocus else null,
                    onLong = { e.dlg = Dlg.AppMenu(a, true) },
                    onClick = { e.launch(a) }
                )
            }
            repeat(6 - favs.size) { Spacer(Modifier.weight(1f)) }
            FocusBox(
                Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(22.u),
                requester = if (favs.isEmpty()) firstFocus else null,
                onClick = { e.go(Screen.Apps) }
            ) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    T("▦", 64, C.GoldSoft)
                    T(s("all_apps"), 24, C.White, FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun FeatureCard(title: String, sub: String, icon: String, brush: Brush, modifier: Modifier, extra: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    FocusBox(modifier, shape = RoundedCornerShape(26.u), onClick = onClick) {
        Box(Modifier.fillMaxSize().background(brush))
        Row(Modifier.fillMaxSize().padding(horizontal = 30.u, vertical = 16.u), verticalAlignment = Alignment.CenterVertically) {
            T(icon, 90)
            Spacer(Modifier.width(22.u))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                T(title, 42, C.White, FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(6.u))
                if (extra != null) extra() else T(sub, 26, C.GoldSoft, maxLines = 1)
            }
        }
    }
}

@Composable
fun BottomCards(e: Engine) {
    val quranBrush = Brush.horizontalGradient(listOf(Color(0xE60A3D31), Color(0xE6071A3E)))
    val hadithBrush = Brush.horizontalGradient(listOf(Color(0xE6341A5E), Color(0xE6071A3E)))
    val moreBrush = Brush.horizontalGradient(listOf(Color(0xE65A3A0E), Color(0xE6071A3E)))
    Row(Modifier.fillMaxWidth().height(170.u), horizontalArrangement = Arrangement.spacedBy(20.u)) {
        FeatureCard(s("about_app"), "", "ℹ️", moreBrush, Modifier.weight(1f).fillMaxHeight()) { e.go(Screen.About) }
        FeatureCard(s("hadith"), s("read_search"), "📜", hadithBrush, Modifier.weight(1f).fillMaxHeight()) { e.go(Screen.Hadith) }
        FeatureCard(s("quran"), s("read_listen"), "📖", quranBrush, Modifier.weight(1f).fillMaxHeight()) { e.go(Screen.Quran) }
    }
}

@Composable
fun HomeScreen(e: Engine) {
    val first = remember { FocusRequester() }
    LaunchedEffect(e.screen) {
        delay(150)
        runCatching { first.requestFocus() }
    }
    Column(Modifier.fillMaxSize().padding(start = 34.u, end = 34.u, top = 52.u, bottom = 58.u)) {
        TopBar(e)
        Row(Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.u), horizontalArrangement = Arrangement.spacedBy(20.u)) {
            Column(Modifier.weight(0.28f).fillMaxHeight()) {
                WeatherCard(e, Modifier.fillMaxWidth().weight(0.78f))
                Spacer(Modifier.weight(0.22f))
            }
            Spacer(Modifier.weight(0.42f))
            PrayerPanel(e, Modifier.weight(0.30f).fillMaxHeight())
        }
        AppsRow(e, first)
        Spacer(Modifier.height(14.u))
        BottomCards(e)
    }
}

const val DEV_NAME = "Tayebi Abdelghani"
const val DEV_EMAIL = "tayebi02@gmail.com"
