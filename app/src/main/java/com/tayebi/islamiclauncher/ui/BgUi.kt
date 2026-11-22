package com.tayebi.islamiclauncher.ui

import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.tayebi.islamiclauncher.Engine

@Composable
fun bgLabel(e: Engine): String {
    val k = e.s.bg.value
    return when {
        k == 12 && e.s.bgAsset.value.isNotEmpty() -> prettyName(e.s.bgAsset.value)
        k == 8 -> s("bg_8")
        else -> s("bg_${k.coerceIn(0, 11)}")
    }
}

private fun prettyName(f: String) = f.substringBeforeLast('.').replace('_', ' ').replace('-', ' ')

@Composable
fun BgScreen(e: Engine) {
    val ctx = LocalContext.current
    val assets = androidx.compose.runtime.remember { listGalleryAssets(ctx) }
    val defaults = listOf(11, 0, 1, 9, 10, 2, 3, 4, 5, 6, 7)

    ScreenFrame(s("change_bg")) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(32.u)) {
            // ---- left: live preview + options + actions
            Column(Modifier.width(620.u), verticalArrangement = Arrangement.spacedBy(10.u)) {
                T(s("preview"), 24, C.GoldSoft)
                Box(
                    Modifier.width(620.u).height(349.u).clip(RoundedCornerShape(16.u))
                        .border(3.u, C.Gold, RoundedCornerShape(16.u))
                ) {
                    AppBackground(e.pvKind, e.pvCustom, e.pvAsset, e.pvMode, e.pvDim)
                }
                Button(s("bg_mode") + ": " + s("bg_mode_${e.pvMode}"), Modifier.fillMaxWidth()) { e.pvMode = (e.pvMode + 1) % 3 }
                Button(s("bg_dim") + ": " + s("bg_dim_${e.pvDim}"), Modifier.fillMaxWidth()) { e.pvDim = (e.pvDim + 1) % 4 }
                Row(horizontalArrangement = Arrangement.spacedBy(10.u)) {
                    Button(s("bg_apply"), Modifier.weight(1f)) { e.applyBg() }
                    Button(s("cancel"), Modifier.weight(1f)) { e.cancelBg() }
                }
                Button(s("bg_restore"), Modifier.fillMaxWidth()) { e.restoreBg() }
            }
            // ---- right: choices
            LazyColumn(Modifier.weight(1f).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.u)) {
                item { Button(s("pick_device"), Modifier.fillMaxWidth()) { e.pickImage() } }
                item { Button(s("bg_url"), Modifier.fillMaxWidth()) { e.dlg = com.tayebi.islamiclauncher.Dlg.BgUrl } }
                item { T(s("bg_defaults"), 26, C.Gold, FontWeight.Bold, Modifier.padding(top = 8.u)) }
                items(defaults) { k ->
                    Choice(s("bg_$k"), e.pvKind == k) { e.pvKind = k; e.pvAsset = "" }
                }
                item { T(s("bg_gallery"), 26, C.Gold, FontWeight.Bold, Modifier.padding(top = 8.u)) }
                if (assets.isEmpty()) item { T(s("gallery_empty"), 22, C.Muted) }
                items(assets) { f ->
                    Choice(prettyName(f), e.pvKind == 12 && e.pvAsset == f) { e.pvKind = 12; e.pvAsset = f }
                }
                if (e.pvCustom != null) item {
                    Choice(s("bg_8"), e.pvKind == 8) { e.pvKind = 8 }
                }
            }
        }
    }
}

@Composable
private fun Choice(label: String, selected: Boolean, onClick: () -> Unit) {
    FocusBox(Modifier.fillMaxWidth().height(68.u), focusScale = 1f, onClick = onClick) {
        Row(Modifier.fillMaxSize().padding(horizontal = 24.u), verticalAlignment = Alignment.CenterVertically) {
            T(label, 26, if (selected) C.Gold else C.White, if (selected) FontWeight.Bold else FontWeight.Medium, Modifier.weight(1f), maxLines = 1)
            if (selected) T("✔", 28, C.Gold)
        }
    }
}
