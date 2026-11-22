package com.tayebi.islamiclauncher.i18n

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun L(ar: String, fr: String, en: String) = arrayOf(ar, fr, en)

private val S: Map<String, Array<String>> = mapOf(
    "home" to L("الرئيسية", "Accueil", "Home"),
    "apps" to L("التطبيقات", "Applications", "Apps"),
    "favorites" to L("المفضلة", "Favoris", "Favorites"),
    "prayer" to L("مواقيت الصلاة", "Heures de prière", "Prayer times"),
    "quran" to L("القرآن الكريم", "Le Saint Coran", "Holy Quran"),
    "radio" to L("الراديو", "Radio", "Radio"),
    "radio_quran" to L("إذاعات القرآن", "Radios coraniques", "Quran radio"),
    "radio_islamic" to L("إذاعات إسلامية", "Radios islamiques", "Islamic radio"),
    "radio_algeria" to L("إذاعات الجزائر", "Radios d'Algérie", "Algeria radio"),
    "now_playing" to L("يُبث الآن", "En cours", "Now playing"),
    "hadith" to L("الأحاديث النبوية", "Hadiths", "Hadith"),
    "settings" to L("الإعدادات", "Paramètres", "Settings"),
    "fajr" to L("الفجر", "Fajr", "Fajr"),
    "sunrise" to L("الشروق", "Lever du soleil", "Sunrise"),
    "dhuhr" to L("الظهر", "Dhuhr", "Dhuhr"),
    "asr" to L("العصر", "Asr", "Asr"),
    "maghrib" to L("المغرب", "Maghrib", "Maghrib"),
    "isha" to L("العشاء", "Isha", "Isha"),
    "remaining_to" to L("المتبقي على %s", "Temps restant avant %s", "Time until %s"),
    "all_apps" to L("كل التطبيقات", "Toutes les apps", "All apps"),
    "more_apps" to L("المزيد من التطبيقات", "Plus d'applications", "More apps"),
    "read_listen" to L("قراءة واستماع", "Lire et écouter", "Read & listen"),
    "read_search" to L("قراءة وبحث", "Lire et chercher", "Read & search"),
    "dev_title" to L("صانع التطبيق", "Créateur de l'application", "App developer"),
    "contact" to L("مراسلة المطوّر", "Contacter le développeur", "Contact developer"),
    "close" to L("إغلاق", "Fermer", "Close"),
    "no_data" to L("لا توجد بيانات — تحقق من الاتصال", "Aucune donnée — vérifiez la connexion", "No data — check connection"),
    "stale" to L("غير متصل — آخر تحديث %s", "Hors ligne — dernière mise à jour %s", "Offline — last updated %s"),
    "never" to L("لم يتم التحديث بعد", "jamais", "never"),
    "retry" to L("إعادة المحاولة", "Réessayer", "Retry"),
    "loading" to L("جارٍ التحميل…", "Chargement…", "Loading…"),
    "wind" to L("كم/س", "km/h", "km/h"),
    "weather_none" to L("الطقس غير متاح", "Météo indisponible", "Weather unavailable"),
    "prayer_city" to L("مدينة الصلاة", "Ville de prière", "Prayer city"),
    "weather_city" to L("مدينة الطقس", "Ville météo", "Weather city"),
    "same_as_prayer" to L("نفس مدينة الصلاة", "Identique à la prière", "Same as prayer city"),
    "calc_method" to L("طريقة حساب المواقيت", "Méthode de calcul", "Calculation method"),
    "school" to L("المذهب الفقهي (العصر)", "École juridique (Asr)", "Juristic school (Asr)"),
    "school_0" to L("الجمهور (مالكي/شافعي/حنبلي)", "Majorité (Malikite/Chafiite/Hanbalite)", "Standard (Maliki/Shafi'i/Hanbali)"),
    "school_1" to L("حنفي", "Hanafite", "Hanafi"),
    "tune" to L("ضبط فروق الدقائق", "Ajustement des minutes", "Minute adjustments"),
    "adhan" to L("الأذان والتنبيه الصوتي", "Adhan et alerte sonore", "Adhan & sound alert"),
    "on" to L("مُفعّل", "Activé", "On"),
    "off" to L("متوقف", "Désactivé", "Off"),
    "adhan_url" to L("رابط صوت الأذان", "Lien audio de l'adhan", "Adhan audio URL"),
    "test_adhan" to L("تجربة الأذان", "Tester l'adhan", "Test adhan"),
    "hijri_adj" to L("تعديل التاريخ الهجري", "Ajuster la date hégirienne", "Hijri date adjustment"),
    "day_unit" to L("يوم", "jour", "day"),
    "volume" to L("مستوى صوت التطبيق", "Volume de l'application", "App volume"),
    "manage_fav" to L("إدارة التطبيقات المفضلة", "Gérer les favoris", "Manage favorite apps"),
    "background" to L("الخلفية", "Arrière-plan", "Background"),
    "bg_11" to L("المسجد الذهبي ليلاً (صورتك)", "Mosquée dorée de nuit", "Golden night mosque"),
    "change_bg" to L("تغيير خلفية الشاشة", "Changer le fond d'écran", "Change screen background"),
    "preview" to L("معاينة", "Aperçu", "Preview"),
    "bg_mode" to L("طريقة العرض", "Affichage", "Display"),
    "bg_mode_0" to L("اقتصاص (ملء دون تشويه)", "Recadrer", "Crop to fill"),
    "bg_mode_1" to L("احتواء الصورة", "Ajuster", "Fit"),
    "bg_mode_2" to L("ملء الشاشة (تمديد)", "Étirer", "Stretch"),
    "bg_dim" to L("تعتيم الخلفية", "Assombrir", "Dim"),
    "bg_dim_0" to L("بدون", "Aucun", "None"),
    "bg_dim_1" to L("خفيف", "Léger", "Light"),
    "bg_dim_2" to L("متوسط", "Moyen", "Medium"),
    "bg_dim_3" to L("قوي", "Fort", "Strong"),
    "bg_apply" to L("اعتماد كخلفية", "Appliquer", "Apply"),
    "bg_restore" to L("استعادة الخلفية الافتراضية", "Fond par défaut", "Restore default"),
    "pick_device" to L("اختيار صورة من الجهاز / USB", "Choisir une image (appareil / USB)", "Pick image from device / USB"),
    "bg_defaults" to L("الخلفيات الافتراضية", "Fonds par défaut", "Default backgrounds"),
    "bg_gallery" to L("معرض صور المساجد", "Galerie de mosquées", "Mosque gallery"),
    "gallery_empty" to L("لا توجد صور في المعرض", "Galerie vide", "Gallery is empty"),
    "bg_saved" to L("تم حفظ الخلفية", "Fond enregistré", "Background saved"),
    "bg_unsupported" to L("الملف غير مدعوم (PNG/JPG/WEBP فقط)", "Fichier non pris en charge", "Unsupported file (PNG/JPG/WEBP only)"),
    "no_file_browser" to L("لا يوجد متصفح ملفات في الجهاز — استخدم رابط صورة", "Pas de gestionnaire de fichiers — utilisez un lien", "No file browser on this device — use an image link"),
    "font" to L("نوع الخط", "Police", "Font"),
    "font_0" to L("نسخ (Noto Naskh)", "Naskh", "Naskh"),
    "font_1" to L("كوفي (Noto Kufi)", "Coufique", "Kufi"),
    "font_2" to L("كوفي ريم (Reem Kufi)", "Reem Kufi", "Reem Kufi"),
    "font_3" to L("المسيري (El Messiri)", "El Messiri", "El Messiri"),
    "font_4" to L("تجوّل (Tajawal)", "Tajawal", "Tajawal"),
    "font_5" to L("القاهرة (Cairo)", "Cairo", "Cairo"),
    "font_6" to L("أميري (Amiri)", "Amiri", "Amiri"),
    "bg_0" to L("أزرق وذهبي مع مسجد", "Bleu & or avec mosquée", "Blue & gold mosque"),
    "bg_1" to L("زمردي مع مسجد", "Émeraude avec mosquée", "Emerald mosque"),
    "bg_9" to L("مسجد ليلي (مرسوم)", "Mosquée de nuit", "Night mosque (drawn)"),
    "bg_10" to L("مسجد زمردي (مرسوم)", "Mosquée émeraude", "Emerald mosque (drawn)"),
    "bg_2" to L("أزرق ليلي هندسي", "Bleu nuit géométrique", "Geometric night blue"),
    "bg_3" to L("مسجد بنفسجي عند الغروب", "Mosquée violette au crépuscule", "Purple dusk mosque"),
    "bg_4" to L("مسجد عند الفجر الذهبي", "Mosquée à l'aube dorée", "Golden dawn mosque"),
    "bg_5" to L("كثبان صحراوية ليلاً", "Dunes du désert la nuit", "Desert dunes at night"),
    "bg_6" to L("زخارف زمردية", "Motifs émeraude", "Emerald pattern"),
    "bg_7" to L("أسود وذهبي", "Noir et or", "Black & gold"),
    "bg_8" to L("صورتي المخصصة", "Mon image", "My custom image"),
    "bg_url" to L("＋ خلفية من رابط صورة…", "＋ Fond depuis un lien image…", "＋ Background from image URL…"),
    "bg_url_title" to L("رابط صورة الخلفية (jpg/png)", "Lien de l'image (jpg/png)", "Background image URL (jpg/png)"),
    "bg_failed" to L("تعذّر تحميل الصورة", "Échec du chargement de l'image", "Could not load image"),
    "launcher_status" to L("الواجهة الرئيسية الافتراضية", "Lanceur par défaut", "Default launcher"),
    "launcher_on" to L("مفعّلة ✓ — اضغط لتغييرها أو تعطيلها", "Activé ✓ — appuyez pour changer/désactiver", "Enabled ✓ — press to change/disable"),
    "launcher_off" to L("غير مفعّلة — اضغط لتفعيلها", "Désactivé — appuyez pour activer", "Not enabled — press to enable"),
    "launcher_hint" to L("اختر «Islamic Smart TV Launcher» ثم «دائماً» للتفعيل، أو اختر واجهة أخرى للتعطيل", "Choisissez « Islamic Smart TV Launcher » puis « Toujours » pour activer, ou un autre lanceur pour désactiver", "Choose “Islamic Smart TV Launcher” then “Always” to enable, or another launcher to disable"),
    "about_app" to L("عن البرنامج", "À propos", "About"),
    "copied" to L("تم النسخ • Copied", "Copié • Copied", "Copied • تم النسخ"),
    "copy" to L("نسخ • Copy", "Copier • Copy", "Copy • نسخ"),
    "language" to L("اللغة", "Langue", "Language"),
    "internet" to L("الإنترنت", "Internet", "Internet"),
    "connected" to L("متصل", "Connecté", "Connected"),
    "disconnected" to L("غير متصل", "Hors ligne", "Offline"),
    "wifi_settings" to L("فتح إعدادات الشبكة", "Ouvrir les réglages réseau", "Open network settings"),
    "refresh_now" to L("تحديث البيانات الآن", "Actualiser maintenant", "Refresh data now"),
    "search_city" to L("ابحث عن مدينة بالاسم…", "Rechercher une ville…", "Search a city…"),
    "use_location" to L("استخدام موقعي الحالي", "Utiliser ma position", "Use my location"),
    "locating" to L("جارٍ تحديد الموقع…", "Localisation…", "Locating…"),
    "loc_failed" to L("تعذّر تحديد الموقع", "Position introuvable", "Could not get location"),
    "no_results" to L("لا توجد نتائج", "Aucun résultat", "No results"),
    "open" to L("فتح", "Ouvrir", "Open"),
    "add_fav" to L("إضافة إلى المفضلة", "Ajouter aux favoris", "Add to favorites"),
    "remove_fav" to L("إزالة من المفضلة", "Retirer des favoris", "Remove from favorites"),
    "move_up" to L("تقديم في الترتيب", "Avancer", "Move earlier"),
    "move_down" to L("تأخير في الترتيب", "Reculer", "Move later"),
    "add_app" to L("إضافة تطبيق", "Ajouter une app", "Add app"),
    "hint_long" to L("اضغط مطولاً على OK أو زر القائمة لخيارات التطبيق", "Appui long sur OK ou touche menu pour les options", "Long-press OK or press Menu for options"),
    "empty_fav" to L("لا توجد تطبيقات مفضلة بعد", "Aucun favori pour l'instant", "No favorites yet"),
    "search_surah" to L("ابحث عن سورة…", "Rechercher une sourate…", "Search a surah…"),
    "search_ayah" to L("بحث في الآيات", "Chercher dans les versets", "Search verses"),
    "continue_reading" to L("متابعة القراءة", "Reprendre la lecture", "Continue reading"),
    "ayahs" to L("آية", "versets", "verses"),
    "reciter" to L("القارئ", "Récitateur", "Reciter"),
    "play" to L("تشغيل", "Lecture", "Play"),
    "pause" to L("إيقاف مؤقت", "Pause", "Pause"),
    "translation" to L("الترجمة", "Traduction", "Translation"),
    "tafsir" to L("التفسير", "Tafsir", "Tafsir"),
    "download" to L("تحميل الصوت", "Télécharger l'audio", "Download audio"),
    "downloaded" to L("الصوت محمّل", "Audio téléchargé", "Audio saved"),
    "downloading" to L("جارٍ التحميل %s%%", "Téléchargement %s%%", "Downloading %s%%"),
    "back" to L("رجوع", "Retour", "Back"),
    "books" to L("الكتب", "Livres", "Books"),
    "sections" to L("الأبواب", "Chapitres", "Chapters"),
    "fav_hadiths" to L("الأحاديث المحفوظة", "Hadiths enregistrés", "Saved hadiths"),
    "search_hadith" to L("ابحث في نص الأحاديث…", "Rechercher dans les hadiths…", "Search hadith text…"),
    "narrator" to L("الراوي", "Narrateur", "Narrator"),
    "source" to L("المصدر", "Source", "Source"),
    "hadith_no" to L("حديث رقم", "Hadith n°", "Hadith no."),
    "grade" to L("الدرجة", "Degré", "Grade"),
    "save" to L("حفظ", "Enregistrer", "Save"),
    "saved" to L("محفوظ", "Enregistré", "Saved"),
    "first_download" to L("جارٍ تحميل الكتاب لأول مرة (للاستعمال دون إنترنت لاحقاً)…", "Téléchargement du livre (première fois)…", "Downloading book (first time, kept offline)…"),
    "hadith_src" to L("المصدر: sunnah.com عبر مجموعة hadith-api المفتوحة", "Source : sunnah.com via le jeu de données hadith-api", "Source: sunnah.com via the open hadith-api dataset"),
    "cancel" to L("إلغاء", "Annuler", "Cancel"),
    "reset" to L("إعادة الضبط", "Réinitialiser", "Reset"),
    "done" to L("تم", "Terminé", "Done"),
    "now_prayer" to L("حان الآن وقت صلاة %s", "C'est l'heure de la prière : %s", "It is time for %s prayer"),
    "stop" to L("إيقاف", "Arrêter", "Stop"),
    "audio_error" to L("تعذّر تشغيل الصوت", "Lecture audio impossible", "Audio playback failed"),
    "error_generic" to L("حدث خطأ — تحقق من الاتصال بالإنترنت", "Erreur — vérifiez Internet", "Error — check your internet connection"),
    "updated" to L("آخر تحديث", "Dernière mise à jour", "Last updated"),
    "tune_hint" to L("± دقيقة لكل صلاة", "± minutes par prière", "± minutes per prayer"),
    "toggle_on" to L("تم التفعيل", "Activé", "Enabled"),
    "toggle_off" to L("تم الإيقاف", "Désactivé", "Disabled"),
    "email" to L("البريد الإلكتروني", "E-mail", "Email"),
    "no_mail_app" to L("لا يوجد تطبيق بريد — راسل المطوّر على: %s", "Aucune app mail — écrivez à : %s", "No mail app — write to: %s"),
    "ayah_search_title" to L("نتائج البحث في الآيات", "Résultats de recherche", "Verse search results")
)

fun langIndex(l: String) = when (l) { "fr" -> 1; "en" -> 2; else -> 0 }
fun tr(key: String, lang: String): String = S[key]?.get(langIndex(lang)) ?: key
fun trf(key: String, lang: String, vararg a: Any): String = String.format(Locale.US, tr(key, lang), *a)

val LANGS = listOf("ar" to "العربية", "fr" to "Français", "en" to "English")

/** Calculation methods offered by the Aladhan engine (id -> label). */
val METHODS = listOf(
    19 to ("الجزائر – وزارة الشؤون الدينية" to "Algeria – Ministry of Religious Affairs"),
    21 to ("المغرب" to "Morocco"),
    18 to ("تونس" to "Tunisia"),
    5 to ("الهيئة المصرية العامة للمساحة" to "Egyptian General Authority of Survey"),
    4 to ("أم القرى – مكة المكرمة" to "Umm Al-Qura – Makkah"),
    3 to ("رابطة العالم الإسلامي" to "Muslim World League"),
    1 to ("جامعة العلوم الإسلامية – كراتشي" to "University of Islamic Sciences, Karachi"),
    2 to ("أمريكا الشمالية (ISNA)" to "Islamic Society of North America"),
    12 to ("فرنسا – UOIF" to "France – UOIF"),
    13 to ("تركيا – ديانت" to "Turkey – Diyanet"),
    23 to ("الأردن" to "Jordan"),
    9 to ("الكويت" to "Kuwait"),
    10 to ("قطر" to "Qatar"),
    8 to ("الخليج" to "Gulf Region"),
    16 to ("دبي" to "Dubai"),
    15 to ("رؤية الهلال" to "Moonsighting Committee")
)

fun methodLabel(id: Int, lang: String): String {
    val p = METHODS.firstOrNull { it.first == id }?.second ?: return "#$id"
    return if (lang == "ar") p.first else p.second
}

private val AR_DAYS = listOf("الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")
private val AR_MONTHS = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")
private val HIJRI_AR = listOf("محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة")
private val HIJRI_LAT = listOf("Muharram", "Safar", "Rabi' al-awwal", "Rabi' al-thani", "Jumada al-ula", "Jumada al-thani", "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah")

fun gregorianLine(d: LocalDate, lang: String): String =
    if (lang == "ar") "${AR_DAYS[d.dayOfWeek.value - 1]} ${d.dayOfMonth} ${AR_MONTHS[d.monthValue - 1]} ${d.year}"
    else d.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale(lang))).replaceFirstChar { it.uppercase() }

/** Hijri date, computed on-device; adj = manual day shift. Never throws. */
private fun hijriParts(d: LocalDate): Triple<Int, Int, Int> {
    try {
        val cal = android.icu.util.IslamicCalendar()
        cal.calculationType = android.icu.util.IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
        cal.timeInMillis = d.atTime(12, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        return Triple(cal.get(android.icu.util.Calendar.DAY_OF_MONTH), cal.get(android.icu.util.Calendar.MONTH) + 1, cal.get(android.icu.util.Calendar.YEAR))
    } catch (e: Throwable) {
        // tabular civil algorithm fallback
        val jd = d.toEpochDay() + 2440588L
        var l = jd - 1948440L + 10632L
        val n = (l - 1) / 10631L
        l = l - 10631L * n + 354L
        val j = ((10985L - l) / 5316L) * ((50L * l) / 17719L) + (l / 5670L) * ((43L * l) / 15238L)
        l = l - ((30L - j) / 15L) * ((17719L * j) / 50L) - (j / 16L) * ((15238L * j) / 43L) + 29L
        val m = (24L * l) / 709L
        val day = l - (709L * m) / 24L
        val year = 30L * n + j - 30L
        return Triple(day.toInt(), m.toInt().coerceIn(1, 12), year.toInt())
    }
}

fun hijriLine(d: LocalDate, adj: Int, lang: String): String {
    val (day, m, y) = hijriParts(d.plusDays(adj.toLong()))
    val mm = m.coerceIn(1, 12)
    return if (lang == "ar") "$day ${HIJRI_AR[mm - 1]} $y هـ" else "$day ${HIJRI_LAT[mm - 1]} $y AH"
}

fun weatherText(code: Int, lang: String): String {
    val i = langIndex(lang)
    val t = when (code) {
        0 -> L("صافٍ", "Dégagé", "Clear")
        1 -> L("صافٍ غالباً", "Plutôt dégagé", "Mainly clear")
        2 -> L("غائم جزئياً", "Partiellement nuageux", "Partly cloudy")
        3 -> L("غائم", "Couvert", "Overcast")
        45, 48 -> L("ضباب", "Brouillard", "Fog")
        in 51..57 -> L("رذاذ", "Bruine", "Drizzle")
        in 61..67 -> L("مطر", "Pluie", "Rain")
        in 71..77 -> L("ثلوج", "Neige", "Snow")
        in 80..82 -> L("زخات مطر", "Averses", "Rain showers")
        85, 86 -> L("زخات ثلج", "Averses de neige", "Snow showers")
        in 95..99 -> L("عاصفة رعدية", "Orage", "Thunderstorm")
        else -> L("—", "—", "—")
    }
    return t[i]
}

fun weatherEmoji(code: Int, day: Boolean): String = when (code) {
    0, 1 -> if (day) "☀️" else "🌙"
    2 -> if (day) "⛅" else "☁️"
    3 -> "☁️"
    45, 48 -> "🌫️"
    in 51..67, in 80..82 -> "🌧️"
    in 71..77, 85, 86 -> "❄️"
    in 95..99 -> "⛈️"
    else -> "🌡️"
}

fun arabicDigits(n: Int): String = n.toString().map { ('٠' + (it - '0')) }.joinToString("")
