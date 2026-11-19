package com.tayebi.islamiclauncher.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object C {
    val Navy = Color(0xFF061431)
    val Navy2 = Color(0xFF0B2250)
    val Emerald = Color(0xFF0E7A55)
    val EmeraldDark = Color(0xFF0A4D3A)
    val Gold = Color(0xFFD9B44A)
    val GoldSoft = Color(0xFFF1DC9A)
    val White = Color(0xFFFFFFFF)
    val Panel = Color(0xCC081A3C)
    val PanelFocus = Color(0xEE12306A)
    val Muted = Color(0xFFB9C4DA)
}

/** 1 unit = 1/1920 of the screen width, so the layout scales from 1080p to 4K. */
val LocalU = compositionLocalOf { 1.dp }
val LocalLang = compositionLocalOf { "ar" }

val Int.u: Dp @Composable get() = LocalU.current * this.toFloat()
val Int.fs: TextUnit @Composable get() = (LocalU.current.value * this).sp

@Composable
fun s(key: String): String = com.tayebi.islamiclauncher.i18n.tr(key, LocalLang.current)

@Composable
fun sf(key: String, vararg a: Any): String = com.tayebi.islamiclauncher.i18n.trf(key, LocalLang.current, *a)

@Composable
fun T(
    text: String,
    size: Int,
    color: Color = C.White,
    weight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.fs,
        fontWeight = weight,
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        lineHeight = (size * 1.4f).let { (LocalU.current.value * it).sp }
    )
}

/** A D-pad friendly container with the golden focus effect. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.u),
    bg: Color = C.Panel,
    focusBg: Color = C.PanelFocus,
    focusScale: Float = 1.05f,
    requester: FocusRequester? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    content: @Composable BoxScope.(Boolean) -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) focusScale else 1f, label = "focusScale")
    val borderW = if (focused) 3.u else 1.u
    val borderC = if (focused) C.Gold else C.Gold.copy(alpha = 0.28f)
    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(if (focused) focusBg else bg)
            .border(borderW, borderC, shape)
            .then(if (requester != null) Modifier.focusRequester(requester) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .onKeyEvent {
                if (onLongClick != null && it.type == KeyEventType.KeyUp && it.key == Key.Menu) {
                    onLongClick(); true
                } else false
            }
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) { content(focused) }
}
