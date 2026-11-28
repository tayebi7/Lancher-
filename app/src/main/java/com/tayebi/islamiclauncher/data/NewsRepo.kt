package com.tayebi.islamiclauncher.data

import android.content.Context
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.security.MessageDigest

data class NewsItem(val title: String, val summary: String, val link: String, val image: String, val source: String, val dateMs: Long)
data class NewsFeed(val ar: String, val fr: String, val en: String, val url: String, val source: String)

/** Real RSS feeds of established news organisations (BBC, France 24, Al Jazeera). */
class NewsRepo(private val ctx: Context) {
    private val dir = File(ctx.filesDir, "news").apply { mkdirs() }
    private val tag = Regex("<[^>]*>")

    suspend fun load(feeds: List<NewsFeed>, force: Boolean): List<NewsItem> = withContext(Dispatchers.IO) {
        val all = ArrayList<NewsItem>()
        for (fd in feeds) {
            val f = File(dir, md5(fd.url) + ".xml")
            val fresh = f.exists() && System.currentTimeMillis() - f.lastModified() < 8 * 60 * 1000
            val xml = if (!force && fresh) f.readText() else try {
                Http.get(fd.url, 15000).also { f.writeText(it) }
            } catch (e: Exception) {
                if (f.exists()) f.readText() else continue
            }
            try { all += parse(xml, fd.source) } catch (e: Exception) { /* skip a broken feed */ }
        }
        all.sortedByDescending { it.dateMs }
    }

    private fun md5(s: String) = MessageDigest.getInstance("MD5").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

    private fun parse(xml: String, source: String): List<NewsItem> {
        val p = Xml.newPullParser()
        p.setInput(xml.reader())
        val out = ArrayList<NewsItem>()
        var ev = p.eventType
        var inItem = false
        var title = ""; var desc = ""; var link = ""; var img = ""; var date = 0L
        while (ev != XmlPullParser.END_DOCUMENT) {
            when (ev) {
                XmlPullParser.START_TAG -> {
                    val n = p.name
                    if (n == "item") { inItem = true; title = ""; desc = ""; link = ""; img = ""; date = 0L }
                    else if (inItem) when (n) {
                        "title" -> title = p.nextText().trim()
                        "description" -> desc = clean(p.nextText())
                        "link" -> link = p.nextText().trim()
                        "pubDate" -> date = parseDate(p.nextText().trim())
                        "thumbnail", "content", "enclosure" -> {
                            val u = p.getAttributeValue(null, "url").orEmpty()
                            val type = p.getAttributeValue(null, "type").orEmpty()
                            if (u.isNotBlank() && (n != "enclosure" || type.startsWith("image")) && (n != "content" || type.isEmpty() || type.startsWith("image"))) {
                                if (img.isEmpty() || n == "content") img = u
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> if (p.name == "item") {
                    inItem = false
                    if (title.isNotEmpty() && link.startsWith("http")) out.add(NewsItem(title, desc, link, img, source, date))
                }
            }
            ev = p.next()
        }
        return out
    }

    private fun clean(s: String) = s.replace(tag, "").replace("&nbsp;", " ").replace("&amp;", "&").trim()

    private fun parseDate(s: String): Long = try {
        java.time.ZonedDateTime.parse(s, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
    } catch (e: Exception) { 0L }

    companion object {
        private const val BBC = "https://feeds.bbci.co.uk/arabic"
        /** One entry per category tab. */
        val CATEGORIES: List<Pair<String, List<NewsFeed>>> = listOf(
            "news_general" to listOf(
                NewsFeed("", "", "", "$BBC/rss.xml", "BBC عربي"),
                NewsFeed("", "", "", "https://www.france24.com/ar/rss", "فرانس 24"),
                NewsFeed("", "", "", "https://www.aljazeera.net/aljazeerarss/a7c186be-1baa-4bd4-9d80-a84db769f779/73d0e1b4-532f-45ef-b135-bfdff8b8cab9", "الجزيرة")
            ),
            "news_middleeast" to listOf(NewsFeed("", "", "", "$BBC/middleeast/rss.xml", "BBC عربي")),
            "news_world" to listOf(NewsFeed("", "", "", "$BBC/world/rss.xml", "BBC عربي")),
            "news_business" to listOf(NewsFeed("", "", "", "$BBC/business/rss.xml", "BBC عربي")),
            "news_sport" to listOf(NewsFeed("", "", "", "$BBC/sports/rss.xml", "BBC عربي")),
            "news_tech" to listOf(NewsFeed("", "", "", "$BBC/science-and-tech/rss.xml", "BBC عربي")),
            "news_fr" to listOf(NewsFeed("", "", "", "https://www.france24.com/fr/rss", "France 24")),
            "news_en" to listOf(
                NewsFeed("", "", "", "https://www.france24.com/en/rss", "France 24"),
                NewsFeed("", "", "", "https://feeds.bbci.co.uk/news/world/rss.xml", "BBC News")
            )
        )
    }
}
