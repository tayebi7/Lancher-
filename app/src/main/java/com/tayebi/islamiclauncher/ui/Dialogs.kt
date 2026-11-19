package com.tayebi.islamiclauncher.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tayebi.islamiclauncher.CityTarget
import com.tayebi.islamiclauncher.Dlg
import com.tayebi.islamiclauncher.Engine
import com.tayebi.islamiclauncher.MainActivity
import com.tayebi.islamiclauncher.data.Geo
import com.tayebi.islamiclauncher.data.Place
import com.tayebi.islamiclauncher.data.QuranRepo
import com.tayebi.islamiclauncher.data.Settings as AppSettings
import com.tayebi.islamiclauncher.i18n.LANGS
import com.tayebi.islamiclauncher.i18n.METHODS
import com.tayebi.islamiclauncher.i18n.methodLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DialogShell(title: String, onDismiss: () -> Unit, width: Int = 900, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.width(width.u).heightIn(max = 940.u)
                .clip(RoundedCornerShape(28.u))
                .background(Color(0xF4071533))
                .border(3.u, C.Gold, RoundedCornerShape(28.u))
                .padding(36.u)
        ) {
            T(title, 40, C.Gold, FontWeight.Bold)
            Spacer(Modifier.height(18.u))
            content()
        }
    }
}

@Composable
fun Button(label: String, modifier: Modifier = Modifier, requester: FocusRequester? = null, onClick: () -> Unit) {
    FocusBox(modifier.height(72.u), shape = RoundedCornerShape(36.u), requester = requester, onClick = onClick) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 28.u).height(72.u), contentAlignment = Alignment.Center) {
            T(label, 28, C.White, FontWeight.Bold, align = TextAlign.Center, maxLines = 1)
        }
    }
}

@Composable
fun TextInput(value: String, onValue: (String) -> Unit, hint: String, modifier: Modifier = Modifier, requester: FocusRequester? = null, onSearch: () -> Unit = {}) {
    var focused by remember { mutableStateOf(false) }
    val base = Modifier.fillMaxWidth().height(76.u)
        .clip(RoundedCornerShape(18.u))
        .background(Color(0x66000000))
        .border(if (focused) 3.u else 1.u, if (focused) C.Gold else C.Gold.copy(alpha = 0.3f), RoundedCornerShape(18.u))
    var m = modifier.then(base)
    if (requester != null) m = m.then(Modifier.focusRequesterCompat(requester))
    BasicTextField(
        value = value,
        onValueChange = onValue,
        singleLine = true,
        textStyle = TextStyle(color = C.White, fontSize = 30.fs),
        cursorBrush = SolidColor(C.Gold),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        modifier = m.onFocusChanged { focused = it.isFocused },
        decorationBox = { inner ->
            Box(Modifier.padding(horizontal = 24.u), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) T(hint, 28, C.Muted)
                inner()
            }
        }
    )
}

private fun Modifier.focusRequesterCompat(r: FocusRequester): Modifier = this.then(androidx.compose.ui.focus.focusRequester(r))

@Composable
fun ChoiceDialog(title: String, options: List<Pair<String, String>>, selected: String?, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val fr = remember { FocusRequester() }
    DialogShell(title, onDismiss) {
        LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(10.u)) {
            itemsIndexed(options) { i, pair ->
                val (k, label) = pair
                FocusBox(
                    Modifier.fillMaxWidth().height(76.u),
                    focusScale = 1f,
                    requester = if (k == selected || (selected == null && i == 0)) fr else null,
                    onClick = { onPick(k) }
                ) {
                    Row(Modifier.fillMaxWidth().height(76.u).padding(horizontal = 26.u), verticalAlignment = Alignment.CenterVertically) {
                        T(label, 28, C.White, if (k == selected) FontWeight.Bold else FontWeight.Normal, Modifier.weight(1f), maxLines = 1)
                        if (k == selected) T("✓", 30, C.Gold)
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        delay(120)
        runCatching { fr.requestFocus() }
    }
}

@Composable
fun CityDialog(e: Engine, target: CityTarget, activity: MainActivity?) {
    val lang = LocalLang.current
    var q by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Place>>(emptyList()) }
    var msg by remember { mutableStateOf<String?>(null) }
    val scope = e.scope
    val tNoRes = s("no_results")
    val tErr = s("error_generic")
    val tLocating = s("locating")
    val tLocFail = s("loc_failed")
    val fr = remember { FocusRequester() }

    LaunchedEffect(q) {
        if (q.trim().length < 2) {
            results = emptyList(); msg = null
            return@LaunchedEffect
        }
        delay(500)
        try {
            results = Geo.search(q.trim(), lang)
            msg = if (results.isEmpty()) tNoRes else null
        } catch (ex: Exception) {
            msg = tErr
        }
    }
    val title = if (target == CityTarget.Prayer) s("prayer_city") else s("weather_city")
    DialogShell(title, { e.dlg = null }, width = 1100) {
        TextInput(q, { q = it }, s("search_city"), requester = fr)
        Spacer(Modifier.height(12.u))
        if (activity != null) {
            Button(s("use_location"), Modifier.fillMaxWidth()) {
                activity.ensureLocationPermission { ok ->
                    if (!ok) {
                        msg = tLocFail
                    } else {
                        msg = tLocating
                        scope.launch {
                            val p = e.locate()
                            if (p != null) {
                                e.pickPlace(target, p); e.dlg = null
                            } else msg = tLocFail
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.u))
        }
        if (msg != null) T(msg!!, 26, C.GoldSoft)
        LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(8.u)) {
            itemsIndexed(results) { _, p ->
                FocusBox(Modifier.fillMaxWidth().height(84.u), focusScale = 1f, onClick = { e.pickPlace(target, p); e.dlg = null }) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 26.u).height(84.u), verticalArrangement = Arrangement.Center) {
                        T(p.name, 30, C.White, FontWeight.Bold, maxLines = 1)
                        T(listOf(p.admin, p.country).filter { it.isNotBlank() }.joinToString("، "), 22, C.Muted, maxLines = 1)
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        delay(150)
        runCatching { fr.requestFocus() }
    }
}

@Composable
fun TuneDialog(e: Engine) {
    var v by remember { mutableStateOf(e.s.tuneList()) }
    val fr = remember { FocusRequester() }
    DialogShell(s("tune") + "  (" + s("tune_hint") + ")", { e.dlg = null }, width = 1000) {
        for (i in 0..5) {
            Row(Modifier.fillMaxWidth().padding(vertical = 5.u), verticalAlignment = Alignment.CenterVertically) {
                T(s(PRAYER_KEYS[i]), 30, C.White, FontWeight.Medium, Modifier.weight(1f))
                Button("−", Modifier.width(110.u), requester = if (i == 0) fr else null) {
                    v = v.toMutableList().also { it[i] = (it[i] - 1).coerceIn(-60, 60) }
                    e.s.tune.set(v.joinToString(","))
                    e.refreshPrayer()
                }
                T((if (v[i] > 0) "+" else "") + v[i], 34, C.Gold, FontWeight.Bold, Modifier.width(130.u), align = TextAlign.Center)
                Button("+", Modifier.width(110.u)) {
                    v = v.toMutableList().also { it[i] = (it[i] + 1).coerceIn(-60, 60) }
                    e.s.tune.set(v.joinToString(","))
                    e.refreshPrayer()
                }
            }
        }
        Spacer(Modifier.height(12.u))
        Row(horizontalArrangement = Arrangement.spacedBy(16.u)) {
            Button(s("reset"), Modifier.weight(1f)) {
                v = List(6) { 0 }; e.s.tune.set("0,0,0,0,0,0"); e.refreshPrayer()
            }
            Button(s("done"), Modifier.weight(1f)) { e.dlg = null }
        }
    }
    LaunchedEffect(Unit) {
        delay(150)
        runCatching { fr.requestFocus() }
    }
}

@Composable
fun AdhanUrlDialog(e: Engine) {
    var url by remember { mutableStateOf(e.s.adhanUrl.value) }
    val fr = remember { FocusRequester() }
    DialogShell(s("adhan_url"), { e.dlg = null }, width = 1300) {
        TextInput(url, { url = it }, "https://…", requester = fr)
        Spacer(Modifier.height(16.u))
        Row(horizontalArrangement = Arrangement.spacedBy(16.u)) {
            Button(s("save"), Modifier.weight(1f)) { e.s.adhanUrl.set(url.trim()); e.dlg = null }
            Button(s("reset"), Modifier.weight(1f)) { url = AppSettings.DEFAULT_ADHAN_URL }
            Button(s("test_adhan"), Modifier.weight(1f)) { e.s.adhanUrl.set(url.trim()); e.testAdhan() }
        }
    }
    LaunchedEffect(Unit) {
        delay(150)
        runCatching { fr.requestFocus() }
    }
}

@Composable
fun DeveloperDialog(e: Engine) {
    val ctx = LocalContext.current
    val fr = remember { FocusRequester() }
    val tNoMail = sf("no_mail_app", DEV_EMAIL)
    DialogShell(s("dev_title"), { e.dlg = null }, width = 1000) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            T("🧩", 100)
            Spacer(Modifier.height(10.u))
            T(DEV_NAME, 52, C.White, FontWeight.Bold, align = TextAlign.Center)
            Spacer(Modifier.height(8.u))
            T(DEV_EMAIL, 34, C.Gold, align = TextAlign.Center)
            Spacer(Modifier.height(10.u))
            T("Islamic Smart TV Launcher • v1.0.0", 24, C.Muted, align = TextAlign.Center)
            Spacer(Modifier.height(24.u))
            Row(horizontalArrangement = Arrangement.spacedBy(16.u)) {
                Button(s("contact"), Modifier.width(420.u), requester = fr) {
                    try {
                        val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$DEV_EMAIL"))
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ctx.startActivity(i)
                    } catch (ex: Exception) {
                        e.toast(tNoMail)
                    }
                }
                Button(s("close"), Modifier.width(300.u)) { e.dlg = null }
            }
        }
    }
    LaunchedEffect(Unit) {
        delay(150)
        runCatching { fr.requestFocus() }
    }
}

@Composable
fun PickFavDialog(e: Engine) {
    val favs = e.favPkgs()
    val options = e.installed.filter { it.pkg !in favs }.map { it.pkg to it.label }
    ChoiceDialog(s("add_app"), options, null, { pkg -> e.toggleFav(pkg); e.dlg = null }, { e.dlg = null })
}

@Composable
fun Dialogs(e: Engine, activity: MainActivity?) {
    val lang = LocalLang.current
    when (val d = e.dlg) {
        null -> {}
        is Dlg.City -> CityDialog(e, d.target, activity)
        Dlg.Method -> ChoiceDialog(
            s("calc_method"), METHODS.map { it.first.toString() to methodLabel(it.first, lang) }, e.s.method.value.toString(),
            { e.s.method.set(it.toInt()); e.refreshPrayer(); e.dlg = null }, { e.dlg = null }
        )
        Dlg.School -> ChoiceDialog(
            s("school"), listOf("0" to s("school_0"), "1" to s("school_1")), e.s.school.value.toString(),
            { e.s.school.set(it.toInt()); e.refreshPrayer(); e.dlg = null }, { e.dlg = null }
        )
        Dlg.Tune -> TuneDialog(e)
        Dlg.Lang -> ChoiceDialog(
            s("language"), LANGS, e.s.lang.value,
            { e.s.lang.set(it); e.q.translations = emptyList(); e.h.english = emptyMap(); e.dlg = null }, { e.dlg = null }
        )
        Dlg.Bg -> ChoiceDialog(
            s("background"), (0..2).map { it.toString() to s("bg_$it") }, e.s.bg.value.toString(),
            { e.s.bg.set(it.toInt()); e.dlg = null }, { e.dlg = null }
        )
        Dlg.HijriAdj -> ChoiceDialog(
            s("hijri_adj"), (-2..2).map { it.toString() to ((if (it > 0) "+" else "") + it + " " + s("day_unit")) }, e.s.hijriAdj.value.toString(),
            { e.s.hijriAdj.set(it.toInt()); e.dlg = null }, { e.dlg = null }
        )
        Dlg.Volume -> ChoiceDialog(
            s("volume"), (1..10).map { (it * 10).toString() to "${it * 10}%" }, e.s.volume.value.toString(),
            { e.s.volume.set(it.toInt()); e.audio.applyVolume(); e.dlg = null }, { e.dlg = null }
        )
        Dlg.AdhanUrl -> AdhanUrlDialog(e)
        Dlg.Developer -> DeveloperDialog(e)
        Dlg.Reciter -> ChoiceDialog(
            s("reciter"), QuranRepo.RECITERS.map { it.first.toString() to it.second }, e.s.reciter.value.toString(),
            { e.s.reciter.set(it.toInt()); e.dlg = null }, { e.dlg = null }
        )
        Dlg.PickFav -> PickFavDialog(e)
        is Dlg.AppMenu -> {
            val a = d.app
            val inFav = e.favPkgs().contains(a.pkg)
            val opts = buildList {
                add("open" to s("open"))
                add("toggle" to (if (inFav) s("remove_fav") else s("add_fav")))
                if (inFav) {
                    add("up" to s("move_up"))
                    add("down" to s("move_down"))
                }
            }
            ChoiceDialog(a.label, opts, null, { k ->
                when (k) {
                    "open" -> e.launch(a)
                    "toggle" -> e.toggleFav(a.pkg)
                    "up" -> e.moveFav(a.pkg, -1)
                    "down" -> e.moveFav(a.pkg, 1)
                }
                e.dlg = null
            }, { e.dlg = null })
        }
    }
}

@Composable
fun openNetworkSettings(): () -> Unit {
    val ctx = LocalContext.current
    return {
        try {
            ctx.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (ex: Exception) {
            try {
                ctx.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: Exception) {
            }
        }
    }
}
