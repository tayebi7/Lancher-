package com.tayebi.islamiclauncher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tayebi.islamiclauncher.data.Edition
import com.tayebi.islamiclauncher.data.FavHadith
import com.tayebi.islamiclauncher.data.Hadith
import com.tayebi.islamiclauncher.data.HadithBook
import com.tayebi.islamiclauncher.data.HadithRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class HView { Books, Sections, List, Favs, Search }

class HadithState(private val e: Engine) {
    val repo = HadithRepo(e.app)

    var view by mutableStateOf(HView.Books)
    var edition by mutableStateOf<Edition?>(null)
    var book by mutableStateOf<HadithBook?>(null)
    var section by mutableIntStateOf(0)
    var loading by mutableStateOf(false)
    var progress by mutableIntStateOf(0)
    var failed by mutableStateOf(false)
    var showEn by mutableStateOf(false)
    var english by mutableStateOf<Map<Int, String>>(emptyMap())
    var results by mutableStateOf<List<Hadith>>(emptyList())
    var favs by mutableStateOf(readFavs())

    private fun readFavs(): List<FavHadith> = try {
        val a = JSONArray(e.s.hadithFavs.value)
        (0 until a.length()).map {
            val o = a.getJSONObject(it)
            FavHadith(o.getString("e"), o.optString("b"), o.getInt("n"), o.optString("t"), o.optString("g"))
        }
    } catch (ex: Exception) {
        emptyList()
    }

    fun isFav(ed: String, n: Int) = favs.any { it.edition == ed && it.number == n }

    fun toggleFav(ed: String, bookTitle: String, h: Hadith) {
        favs = if (isFav(ed, h.number)) favs.filterNot { it.edition == ed && it.number == h.number }
        else favs + FavHadith(ed, bookTitle, h.number, h.text, h.grade)
        val arr = JSONArray()
        favs.forEach {
            arr.put(JSONObject().put("e", it.edition).put("b", it.bookTitle).put("n", it.number).put("t", it.text).put("g", it.grade))
        }
        e.s.hadithFavs.set(arr.toString())
    }

    fun openEdition(ed: Edition) {
        edition = ed
        book = null
        failed = false
        loading = true
        progress = 0
        view = HView.Sections
        val title = if (e.s.lang.value == "ar") ed.ar else ed.en
        e.scope.launch {
            try {
                book = repo.load(ed, title) { progress = it }
                english = emptyMap()
                if (showEn) loadEnglish()
            } catch (ex: Exception) {
                failed = true
            }
            loading = false
        }
    }

    fun openSection(b: Int) {
        section = b
        view = HView.List
    }

    fun toggleEnglish() {
        showEn = !showEn
        if (showEn) loadEnglish()
    }

    private fun loadEnglish() {
        val ed = edition ?: return
        if (english.isNotEmpty()) return
        e.scope.launch {
            try {
                english = repo.english(ed) { }
            } catch (ex: Exception) {
                showEn = false
                e.toast(e.t("error_generic"))
            }
        }
    }

    private val diacritics = Regex("[\\u064B-\\u065F\\u0670\\u0640]")
    private fun norm(s: String) = s.replace(diacritics, "").replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
        .replace('ى', 'ي').replace('ة', 'ه')

    fun search(q: String) {
        val b = book ?: return
        if (q.isBlank()) return
        val nq = norm(q.trim())
        e.scope.launch {
            results = withContext(Dispatchers.Default) {
                b.items.asSequence().filter { norm(it.text).contains(nq) }.take(60).toList()
            }
            view = HView.Search
        }
    }

    fun back() {
        view = when (view) {
            HView.List -> HView.Sections
            HView.Search -> if (book != null) HView.Sections else HView.Books
            else -> HView.Books
        }
    }
}
