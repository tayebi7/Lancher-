package com.tayebi.islamiclauncher.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.tayebi.islamiclauncher.Engine
import com.tayebi.islamiclauncher.R

private class Pay(val icon: Int, val name: String, val label: String, val value: String, val note: String?)

private val PAYMENTS = listOf(
    Pay(R.drawable.pay_baridimob, "BaridiMob — بريدي موب", "رقم الحساب (RIP) • Account number (RIP)", "00799999000848709078", null),
    Pay(R.drawable.pay_paypal, "PayPal — باي بال", "البريد الإلكتروني • PayPal email", "tayebiabdelghani5@gmail.com", null),
    Pay(
        R.drawable.pay_binance, "Binance — USDT (BSC)", "عنوان المحفظة • Wallet address",
        "0xf319f4cc61e8c15891ce53aa7f61794929ac7c28",
        "أرسل USDT عبر شبكة BSC (BEP20) فقط • Send USDT on the BSC (BEP20) network only"
    )
)

@Composable
fun AboutScreen(e: Engine) {
    val ctx = LocalContext.current
    val clip = LocalClipboardManager.current
    val tCopied = s("copied")
    val tNoMail = sf("no_mail_app", DEV_EMAIL)
    ScreenFrame("عن البرنامج • About") {
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.u)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.banner), null, Modifier.size(width = 380.u, height = 214.u))
                    Spacer(Modifier.width(30.u))
                    Column(Modifier.weight(1f)) {
                        T("Islamic Smart TV Launcher", 34, C.GoldSoft, FontWeight.Bold)
                        T("v1.0.0", 22, C.Muted)
                        Spacer(Modifier.height(8.u))
                        T("Tayebi Abdelghani", 52, C.White, FontWeight.Bold)
                        T("🇩🇿 Algeria  •  الجزائر", 30, C.White)
                        T(DEV_EMAIL, 30, C.Gold)
                        Spacer(Modifier.height(10.u))
                        Button(s("contact"), Modifier.width(520.u)) {
                            try {
                                ctx.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$DEV_EMAIL")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (ex: Exception) {
                                e.toast(tNoMail)
                            }
                        }
                    }
                }
            }
            item {
                Column(Modifier.fillMaxWidth().padding(top = 8.u)) {
                    T("ادعم البرنامج  •  Support the app", 44, C.Gold, FontWeight.Bold)
                    Spacer(Modifier.height(6.u))
                    T("إن أعجبك البرنامج ويسرّك أن يستمر تطويره، فيمكنك دعمه بأي مبلغ تيسّر. جزاك الله خيراً.", 28, C.White)
                    T("If you enjoy this app and would like to help keep it improving, you can support it with any amount. Thank you!", 26, C.GoldSoft)
                }
            }
            items(PAYMENTS) { p ->
                FocusBox(Modifier.fillMaxWidth(), focusScale = 1f, onClick = {
                    clip.setText(AnnotatedString(p.value))
                    e.toast(tCopied)
                }) {
                    Row(Modifier.fillMaxWidth().padding(22.u), verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(p.icon), p.name, Modifier.size(120.u))
                        Spacer(Modifier.width(26.u))
                        Column(Modifier.weight(1f)) {
                            T(p.name, 36, C.White, FontWeight.Bold)
                            T(p.label, 24, C.Muted)
                            Spacer(Modifier.height(4.u))
                            T(p.value, 32, C.Gold, FontWeight.Bold)
                            p.note?.let { T(it, 22, Color(0xFFFFB4A9)) }
                        }
                        Spacer(Modifier.width(20.u))
                        T(s("copy"), 26, C.GoldSoft, FontWeight.Medium)
                    }
                }
            }
        }
    }
}
