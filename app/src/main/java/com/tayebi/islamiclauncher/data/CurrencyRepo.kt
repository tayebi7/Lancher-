package com.tayebi.islamiclauncher.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

data class Rate(val code: String, val dzdPerUnit: Double)
data class Rates(val list: List<Rate>, val updatedUtc: String, val fetchedMs: Long)

/** Reference exchange rates against the Algerian dinar (ExchangeRate-API open endpoint, cached). */
class CurrencyRepo(ctx: Context) {
    private val f = File(ctx.filesDir, "rates_usd.json")

    suspend fun load(force: Boolean): Rates = withContext(Dispatchers.IO) {
        val fresh = f.exists() && System.currentTimeMillis() - f.lastModified() < 30L * 60 * 1000
        val text = if (!force && fresh) f.readText() else try {
            Http.get("https://open.er-api.com/v6/latest/USD", 20000).also { f.writeText(it) }
        } catch (e: Exception) {
            if (f.exists()) f.readText() else throw e
        }
        val o = JSONObject(text)
        if (o.optString("result") != "success") throw java.io.IOException("bad result")
        val r = o.getJSONObject("rates")
        val dzd = r.getDouble("DZD")
        val list = CODES.mapNotNull { c -> if (r.has(c)) Rate(c, dzd / r.getDouble(c)) else null }
        Rates(list, o.optString("time_last_update_utc"), f.lastModified())
    }

    companion object {
        val CODES = listOf("USD", "EUR", "GBP", "SAR", "AED", "CAD", "CHF", "TRY", "MAD", "TND", "EGP", "QAR", "KWD", "JPY", "CNY")
    }
}
