package com.tayebi.islamiclauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.tayebi.islamiclauncher.Engine
import com.tayebi.islamiclauncher.MainActivity
import com.tayebi.islamiclauncher.Screen

@Composable
fun LauncherApp(e: Engine, activity: MainActivity?) {
    val lang = e.s.lang.value
    val dir = if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
    BackHandler(enabled = true) { e.back() }
    BoxWithConstraints(Modifier.fillMaxSize().background(C.Navy)) {
        val u = maxWidth / 1920f
        CompositionLocalProvider(LocalU provides u, LocalLang provides lang, LocalLayoutDirection provides dir, LocalFont provides Fonts.all[e.s.font.value.coerceIn(0, Fonts.COUNT - 1)]) {
            AppBackground(e.s.bg.value, e.customBg, e.s.bgAsset.value, e.s.bgMode.value, e.s.bgDim.value)
            Row(Modifier.fillMaxSize()) {
                Sidebar(e)
                Box(Modifier.weight(1f).fillMaxHeight().then(if (e.screen != Screen.Home) Modifier.background(Color(0xE6051029)) else Modifier)) {
                    when (e.screen) {
                        Screen.Home -> HomeScreen(e)
                        Screen.Apps -> AppsScreen(e)
                        Screen.Favorites -> FavoritesScreen(e)
                        Screen.Prayer -> PrayerScreen(e)
                        Screen.Quran -> QuranScreen(e)
                        Screen.Hadith -> HadithScreen(e)
                        Screen.Radio -> RadioScreen(e)
                        Screen.Reciters -> RecitersScreen(e)
                        Screen.Names -> NamesScreen(e)
                        Screen.Adhkar -> AdhkarScreen(e)
                        Screen.Scores -> ScoresScreen(e)
                        Screen.News -> NewsScreen(e)
                        Screen.Currency -> CurrencyScreen(e)
                        Screen.Settings -> SettingsScreen(e)
                        Screen.Background -> BgScreen(e)
                        Screen.About -> AboutScreen(e)
                    }
                }
            }
            OrnamentFrame()
            Dialogs(e, activity)
            e.adhanBanner?.let { msg ->
                Box(Modifier.fillMaxSize().padding(top = 40.u), contentAlignment = Alignment.TopCenter) {
                    FocusBox(Modifier, shape = RoundedCornerShape(40.u), bg = Color(0xF00E7A55), onClick = { e.stopAdhan() }) {
                        Row(Modifier.padding(horizontal = 40.u, vertical = 18.u), verticalAlignment = Alignment.CenterVertically) {
                            T("🕌  $msg   •   ${s("stop")}", 36, C.White, androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }
            e.news.reading?.let { NewsReaderDialog(e, it) }
            e.alertBanner?.let { msg ->
                Box(Modifier.fillMaxSize().padding(top = 18.u), contentAlignment = Alignment.TopCenter) {
                    Box(Modifier.background(Color(0xF2B3261E), RoundedCornerShape(26.u)).padding(horizontal = 30.u, vertical = 10.u)) {
                        T(msg, 24, C.White, androidx.compose.ui.text.font.FontWeight.Bold, maxLines = 2)
                    }
                }
            }
            e.toast?.let { msg ->
                Box(Modifier.fillMaxSize().padding(bottom = 60.u), contentAlignment = Alignment.BottomCenter) {
                    Box(Modifier.background(Color(0xEE000000), RoundedCornerShape(30.u)).padding(horizontal = 36.u, vertical = 16.u)) {
                        T(msg, 30, C.White)
                    }
                }
            }
        }
    }
}
