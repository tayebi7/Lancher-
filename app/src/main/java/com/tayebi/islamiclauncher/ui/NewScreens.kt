package com.tayebi.islamiclauncher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.tayebi.islamiclauncher.Dlg
import com.tayebi.islamiclauncher.Engine
import com.tayebi.islamiclauncher.data.*
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RemoteImage(url: String, modifier: Modifier, fit: ContentScale = ContentScale.Fit, fallback: String = "") {
    val bmp by produceState<ImageBitmap?>(null, url) { value = Img.load(url, 400) }
    val b = bmp
    if (b != null) Image(b, null, modifier, contentScale = fit)
    else Box(modifier, contentAlignment = Alignment.Center) { if (fallback.isNotEmpty()) T(fallback, 30) }
}

@Composable
private fun Tabs(labels: List<String>, selected: Int, onPick: (Int) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.u), modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(labels) { i, l ->
            FocusBox(Modifier.height(64.u), shape = RoundedCornerShape(32.u),
                bg = if (i == selected) C.Gold.copy(alpha = 0.28f) else C.Panel, onClick = { onPick(i) }) {
                Box(Modifier.padding(horizontal = 30.u).height(64.u), contentAlignment = Alignment.Center) {
                    T((if (i == selected) "● " else "") + l, 26, if (i == selected) C.Gold else C.White, FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

private fun fmtTime(ms: Long, pattern: String = "dd/MM HH:mm") =
    Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(pattern))

// ───────────────────────── Reciters ─────────────────────────
@Composable
fun RecitersScreen(e: Engine) {
    val r = e.reciters
    val fr = remember { FocusRequester() }
    val list = r.filtered()
    ScreenFrame(s("reciters")) {
        r.currentReciter()?.let { T("🎙 " + s("current_reciter") + ": " + it.name, 28, C.Gold, FontWeight.Bold, maxLines = 1) }
        Spacer(Modifier.height(10.u))
        Row(horizontalArrangement = Arrangement.spacedBy(12.u), verticalAlignment = Alignment.CenterVertically) {
            TextInput(r.query, { r.query = it }, s("search_reciter"), Modifier.weight(1f), fr)
            Button(if (r.favsOnly) "★ " + s("favorites") else "☆ " + s("favorites"), Modifier.width(300.u)) { r.favsOnly = !r.favsOnly }
        }
        Spacer(Modifier.height(12.u))
        when {
            r.loading && r.all.isEmpty() -> T(s("loading"), 30, C.GoldSoft)
            r.failed && r.all.isEmpty() -> {
                T(s("no_data"), 30, C.GoldSoft)
                Button(s("retry"), Modifier.width(300.u)) { r.load(true) }
            }
            list.isEmpty() -> T(s("no_results"), 30, C.GoldSoft)
            else -> LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.u)) {
                items(list.take(300)) { rc ->
                    val sel = rc.server == e.s.reciterServer.value
                    Row(horizontalArrangement = Arrangement.spacedBy(10.u), modifier = Modifier.fillMaxWidth()) {
                        FocusBox(Modifier.weight(1f).height(80.u), focusScale = 1f, onClick = { r.select(rc); e.back() }) {
                            Row(Modifier.fillMaxSize().padding(horizontal = 24.u), verticalAlignment = Alignment.CenterVertically) {
                                T(rc.name, 28, if (sel) C.Gold else C.White, FontWeight.Bold, Modifier.weight(1f), maxLines = 1)
                                T(rc.moshaf, 22, C.GoldSoft, modifier = Modifier.weight(1f), maxLines = 1)
                                if (sel) T("✓", 30, C.Gold)
                            }
                        }
                        FocusBox(Modifier.width(90.u).height(80.u), focusScale = 1f, onClick = { r.toggleFav(rc) }) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { T(if (r.isFav(rc)) "★" else "☆", 38, C.Gold) }
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────── Names of Allah ─────────────────────────
@Composable
fun NamesScreen(e: Engine) {
    val ar = LocalLang.current == "ar"
    var sel by remember { mutableStateOf<Int?>(null) }
    ScreenFrame(s("names")) {
        LazyVerticalGrid(GridCells.Fixed(3), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.u), verticalArrangement = Arrangement.spacedBy(14.u)) {
            itemsIndexed(NamesOfAllah.ALL) { i, n ->
                FocusBox(Modifier.height(190.u), onClick = { sel = if (sel == i) null else i }) {
                    Column(Modifier.fillMaxSize().padding(14.u), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        T("${i + 1}", 20, C.GoldSoft)
                        T(n.ar, 44, C.Gold, FontWeight.Bold, align = TextAlign.Center, maxLines = 1, font = Fonts.Naskh)
                        T(if (ar) n.meaningAr else n.meaningEn, 20, C.White, align = TextAlign.Center, maxLines = if (sel == i) 6 else 3)
                    }
                }
            }
        }
    }
}

// ───────────────────────── Adhkar ─────────────────────────
@Composable
fun AdhkarScreen(e: Engine) {
    val a = e.adhkar
    val cat = a.cats.getOrNull(a.open)
    ScreenFrame(if (cat != null) cat.title else s("adhkar")) {
        when {
            a.loading && a.cats.isEmpty() -> T(s("loading"), 30, C.GoldSoft)
            a.failed && a.cats.isEmpty() -> {
                T(s("no_data"), 30, C.GoldSoft)
                Button(s("retry"), Modifier.width(300.u)) { a.load() }
            }
            cat != null -> {
                Button(s("back"), Modifier.width(300.u)) { a.open = -1 }
                Spacer(Modifier.height(10.u))
                LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.u)) {
                    items(cat.items) { d ->
                        FocusBox(Modifier.fillMaxWidth(), focusScale = 1f, onClick = {}) {
                            Column(Modifier.fillMaxWidth().padding(22.u)) {
                                T(d.text, 30, C.White, font = Fonts.Naskh)
                                if (d.count > 1) T("× ${d.count}", 26, C.Gold, FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            else -> LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.u), verticalArrangement = Arrangement.spacedBy(14.u)) {
                itemsIndexed(a.cats) { i, c ->
                    FocusBox(Modifier.height(90.u), onClick = { a.open = i }) {
                        Row(Modifier.fillMaxSize().padding(horizontal = 24.u), verticalAlignment = Alignment.CenterVertically) {
                            T("📿", 32, modifier = Modifier.width(56.u))
                            T(c.title, 26, C.White, FontWeight.Medium, Modifier.weight(1f), maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────── Scores ─────────────────────────
@Composable
private fun MatchRow(m: Match, fav: Boolean) {
    FocusBox(Modifier.fillMaxWidth(), focusScale = 1f, onClick = {}) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.u, vertical = 12.u)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                T((if (fav) "★ " else "") + m.league, 20, C.GoldSoft, modifier = Modifier.weight(1f), maxLines = 1)
                val live = m.state == "in"
                T(if (live) "🔴 " + m.status else if (m.state == "post") m.status.ifBlank { "FT" } + "  " + fmtTime(m.startMs) else fmtTime(m.startMs), 22, if (live) C.Gold else C.GoldSoft, FontWeight.Bold, maxLines = 1)
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.u), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.u)) {
                    RemoteImage(m.homeLogo, Modifier.size(56.u), fallback = "⚽")
                    T(m.home, 28, C.White, FontWeight.Bold, maxLines = 1)
                }
                T(if (m.state == "pre") "vs" else "${m.homeScore ?: 0} - ${m.awayScore ?: 0}", 40, C.Gold, FontWeight.Bold, Modifier.width(190.u), align = TextAlign.Center, maxLines = 1)
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.u)) {
                    RemoteImage(m.awayLogo, Modifier.size(56.u), fallback = "⚽")
                    T(m.away, 28, C.White, FontWeight.Bold, maxLines = 1)
                }
            }
            if (m.events.isNotEmpty() && m.state != "pre") {
                val line = m.events.joinToString("   ") {
                    (when (it.kind) { "red" -> "🟥"; "pen" -> "⚽(P)"; "penmiss" -> "❌(P)"; "own" -> "⚽(OG)"; else -> "⚽" }) + " " + it.player + " " + it.minute
                }
                T(line, 20, C.GoldSoft, maxLines = 2)
            }
        }
    }
}

@Composable
fun ScoresScreen(e: Engine) {
    val sc = e.scores
    val favT = sc.favTeams()
    val list = sc.matches.filter {
        when (sc.tab) { 0 -> it.state == "in"; 1 -> it.state == "pre"; else -> it.state == "post" }
    }.sortedWith(compareByDescending<Match> { it.home in favT || it.away in favT }.thenBy { if (sc.tab == 2) -it.startMs else it.startMs })
    ScreenFrame(s("scores")) {
        Tabs(listOf(s("live"), s("upcoming"), s("finished")), sc.tab) { sc.tab = it }
        Spacer(Modifier.height(10.u))
        Row(horizontalArrangement = Arrangement.spacedBy(10.u), modifier = Modifier.fillMaxWidth()) {
            Button(s("leagues"), Modifier.weight(1f)) { e.dlg = Dlg.MultiLeagues }
            Button(s("fav_teams"), Modifier.weight(1f)) { e.dlg = Dlg.MultiTeams }
            Button(s("goal_alerts") + ": " + if (e.s.goalAlerts.value) s("on") else s("off"), Modifier.weight(1.2f)) {
                val on = !e.s.goalAlerts.value; e.s.goalAlerts.set(on); if (on) e.notifPermLauncher?.invoke()
            }
            Button(s("sound") + ": " + if (e.s.goalSound.value) s("on") else s("off"), Modifier.weight(0.9f)) { e.s.goalSound.set(!e.s.goalSound.value) }
            Button("⟳", Modifier.width(110.u)) { sc.load() }
        }
        Spacer(Modifier.height(6.u))
        T(s("source") + ": ESPN" + if (sc.updatedMs > 0) "  •  " + s("updated") + " " + fmtTime(sc.updatedMs, "HH:mm") else "", 20, C.GoldSoft, maxLines = 1)
        Spacer(Modifier.height(8.u))
        when {
            sc.loading && sc.matches.isEmpty() -> T(s("loading"), 30, C.GoldSoft)
            sc.failed && sc.matches.isEmpty() -> T(s("no_data"), 30, C.GoldSoft)
            list.isEmpty() -> T(s("no_matches"), 30, C.GoldSoft)
            else -> LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.u)) {
                items(list, key = { it.id }) { m -> MatchRow(m, m.home in favT || m.away in favT) }
            }
        }
    }
}

// ───────────────────────── News ─────────────────────────
@Composable
fun NewsScreen(e: Engine) {
    val n = e.news
    val labels = NewsRepo.CATEGORIES.map { s(it.first) }
    ScreenFrame(s("news")) {
        Tabs(labels, e.s.newsCat.value) { n.selectCat(it) }
        Spacer(Modifier.height(6.u))
        T(s("source") + ": BBC / France 24 / Al Jazeera" + if (n.updatedMs > 0) "  •  " + s("updated") + " " + fmtTime(n.updatedMs, "HH:mm") else "", 20, C.GoldSoft, maxLines = 1)
        Spacer(Modifier.height(8.u))
        when {
            n.loading && n.items.isEmpty() -> T(s("loading"), 30, C.GoldSoft)
            n.failed && n.items.isEmpty() -> {
                T(s("no_data"), 30, C.GoldSoft)
                Button(s("retry"), Modifier.width(300.u)) { n.load(true) }
            }
            else -> LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.u), verticalArrangement = Arrangement.spacedBy(14.u)) {
                items(n.items.take(40)) { it ->
                    FocusBox(Modifier.height(170.u), onClick = { n.reading = it }) {
                        Row(Modifier.fillMaxSize().padding(12.u), horizontalArrangement = Arrangement.spacedBy(14.u)) {
                            RemoteImage(it.image, Modifier.width(220.u).fillMaxHeight(), ContentScale.Crop, "📰")
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
                                T(it.title, 24, C.White, FontWeight.Bold, maxLines = 4)
                                T(it.source + if (it.dateMs > 0) "  •  " + fmtTime(it.dateMs) else "", 18, C.GoldSoft, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsReaderDialog(e: Engine, item: NewsItem) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    DialogShell(item.source, { e.news.reading = null }, 1100) {
        RemoteImage(item.image, Modifier.fillMaxWidth().height(260.u), ContentScale.Crop)
        Spacer(Modifier.height(12.u))
        T(item.title, 32, C.White, FontWeight.Bold, maxLines = 4)
        Spacer(Modifier.height(8.u))
        T(item.summary, 24, C.GoldSoft, maxLines = 6)
        Spacer(Modifier.height(16.u))
        Row(horizontalArrangement = Arrangement.spacedBy(12.u)) {
            Button(s("open_article"), Modifier.weight(1f)) {
                try {
                    ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(item.link)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (ex: Exception) { e.toast(s_("no_browser", e)) }
            }
            Button(s("close"), Modifier.weight(1f)) { e.news.reading = null }
        }
    }
}

private fun s_(k: String, e: Engine) = e.t(k)

// ───────────────────────── Currency ─────────────────────────
@Composable
fun CurrencyScreen(e: Engine) {
    val c = e.currency
    val ar = LocalLang.current == "ar"
    val df = DecimalFormat("#,##0.00")
    ScreenFrame(s("currency")) {
        val r = c.rates
        Row(horizontalArrangement = Arrangement.spacedBy(12.u), verticalAlignment = Alignment.CenterVertically) {
            T(s("currency_sub"), 24, C.GoldSoft, Modifier.weight(1f), maxLines = 2)
            Button("⟳ " + s("refresh_now"), Modifier.width(320.u)) { c.load(true) }
        }
        if (r != null) T(s("updated") + ": " + r.updatedUtc.take(25) + "  •  " + s("source") + ": ExchangeRate-API", 20, C.GoldSoft, maxLines = 1)
        Spacer(Modifier.height(8.u))
        when {
            r == null && c.loading -> T(s("loading"), 30, C.GoldSoft)
            r == null -> T(s("no_data"), 30, C.GoldSoft)
            else -> LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.u)) {
                items(r.list) { rt ->
                    FocusBox(Modifier.fillMaxWidth().height(72.u), focusScale = 1f, onClick = {}) {
                        Row(Modifier.fillMaxSize().padding(horizontal = 26.u), verticalAlignment = Alignment.CenterVertically) {
                            T(rt.code, 30, C.Gold, FontWeight.Bold, Modifier.width(120.u))
                            T(s("cur_" + rt.code), 24, C.White, modifier = Modifier.weight(1f), maxLines = 1)
                            T("1 = ${df.format(rt.dzdPerUnit)} DZD", 30, C.White, FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
                item { T(s("currency_note"), 20, C.GoldSoft) }
            }
        }
    }
}

// ───────────────────────── Radio ─────────────────────────
@Composable
fun RadioScreen(e: Engine) {
    val r = e.radio
    LaunchedEffect(Unit) { r.load() }
    val groups = listOf(s("radio_arabic"), s("radio_foreign"), s("favorites"))
    ScreenFrame(s("radio")) {
        Tabs(groups, r.group) { r.select(it) }
        if (r.group != 2) {
            Spacer(Modifier.height(8.u))
            Tabs(r.categories().map { s(it.key) }, r.tab) { r.select(r.group, it) }
        }
        Spacer(Modifier.height(10.u))
        r.current?.let { st ->
            FocusBox(Modifier.fillMaxWidth(), focusScale = 1f, bg = C.Gold.copy(alpha = 0.14f), onClick = { e.audio.toggle() }) {
                Row(Modifier.fillMaxWidth().padding(10.u), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.u)) {
                    RemoteImage(st.logo, Modifier.size(64.u), fallback = "📻")
                    Column(Modifier.weight(1f)) {
                        T(s("now_playing") + ": " + st.name, 26, C.Gold, FontWeight.Bold, maxLines = 1)
                        T(if (r.playError) s("radio_error") else if (e.audio.isPlaying) "▶" else "⏸", 20, if (r.playError) C.GoldSoft else C.White, maxLines = 1)
                    }
                    Button(if (e.audio.isPlaying) "⏸" else "▶", Modifier.width(110.u)) { e.audio.toggle() }
                    Button("■", Modifier.width(110.u)) { r.stop() }
                    Button("🔉", Modifier.width(110.u)) { r.volume(-10) }
                    T("${e.s.volume.value}%", 22, C.White, modifier = Modifier.width(80.u), align = TextAlign.Center)
                    Button("🔊", Modifier.width(110.u)) { r.volume(10) }
                    Button(if (r.isFav(st)) "★" else "☆", Modifier.width(110.u)) { r.toggleFav(st) }
                }
            }
            Spacer(Modifier.height(10.u))
        }
        when {
            r.loading -> T(s("loading"), 30, C.GoldSoft)
            r.failed -> {
                T(s("no_data"), 30, C.GoldSoft)
                Button(s("retry"), Modifier.width(300.u)) { r.load(true) }
            }
            r.list.isEmpty() -> T(if (r.group == 2) s("no_radio_favs") else s("no_data"), 28, C.GoldSoft)
            else -> LazyVerticalGrid(GridCells.Fixed(3), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.u), verticalArrangement = Arrangement.spacedBy(14.u)) {
                items(r.list, key = { it.url }) { st ->
                    FocusBox(Modifier.height(100.u), onClick = { r.play(st) }, onLongClick = { r.toggleFav(st) }) {
                        Row(Modifier.fillMaxSize().padding(horizontal = 16.u), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.u)) {
                            RemoteImage(st.logo, Modifier.size(64.u), fallback = "📻")
                            T(st.name, 24, if (r.current?.url == st.url) C.Gold else C.White, FontWeight.Medium, Modifier.weight(1f), maxLines = 2)
                            if (r.isFav(st)) T("★", 26, C.Gold)
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────── Multi-select dialogs ─────────────────────────
@Composable
fun ToggleDialog(title: String, options: List<Pair<String, String>>, checked: Set<String>, onToggle: (String) -> Unit, onDismiss: () -> Unit) {
    val fr = remember { FocusRequester() }
    DialogShell(title, onDismiss) {
        if (options.isEmpty()) T(s("no_data"), 26, C.GoldSoft)
        LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(8.u)) {
            itemsIndexed(options) { i, (k, label) ->
                FocusBox(Modifier.fillMaxWidth().height(70.u), focusScale = 1f, requester = if (i == 0) fr else null, onClick = { onToggle(k) }) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 24.u), verticalAlignment = Alignment.CenterVertically) {
                        T(label, 26, C.White, modifier = Modifier.weight(1f), maxLines = 1)
                        T(if (k in checked) "☑" else "☐", 32, C.Gold)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.u))
        Button(s("close"), Modifier.fillMaxWidth(), null, onDismiss)
    }
    LaunchedEffect(Unit) { delay(120); runCatching { fr.requestFocus() } }
}
