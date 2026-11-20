package com.tayebi.islamiclauncher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView

/** Runs in its own process with plain Views, so it can always display why the launcher failed. */
class CrashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trace = intent.getStringExtra("trace") ?: "unknown error"
        val tv = TextView(this).apply {
            text = "Islamic Launcher – error\n(please send a photo of this screen to the developer)\n\n$trace"
            setTextColor(Color.WHITE)
            textSize = 15f
            setPadding(48, 48, 48, 48)
        }
        setContentView(ScrollView(this).apply {
            setBackgroundColor(Color.BLACK)
            isFocusable = true
            isFocusableInTouchMode = true
            addView(tv)
        })
    }

    companion object {
        fun start(ctx: Context, trace: String) {
            ctx.startActivity(
                Intent(ctx, CrashActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra("trace", trace.take(6000))
            )
        }
    }
}
