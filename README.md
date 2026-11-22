<p align="center"><img src="docs/icon.png" width="140" alt="Islamic TV Launcher"></p>

# Islamic TV Launcher
**واجهة إسلامية (Launcher) لأجهزة Android TV / Google TV** — عربية RTL مع الفرنسية والإنجليزية.
An Islamic launcher for Android TV / Google TV box. Kotlin + Jetpack Compose, minSdk 26.

## ⬇️ تحميل / Download (بدون GitHub Actions)
- **الرابط المباشر / Direct APK:** https://github.com/tayebi7/Lancher-/releases/download/latest/app-release.apk
- **صفحة التحميل / Download page:** https://tayebi7.github.io/Lancher-/
- الإصدارات / Releases: https://github.com/tayebi7/Lancher-/releases/tag/latest

يمكنك وضع الرابط المباشر في تطبيق **Downloader** على التلفاز أو نشره في أي موقع/قناة.

## المزايا / Features
- ساعة كبيرة، التاريخ الميلادي والهجري (مع تعديل ±2 يوم)
- مواقيت الصلاة الحقيقية: عدّ تنازلي، أذان، المدينة بالاسم أو الموقع، طريقة الحساب، المذهب، تعديل بالدقائق
- طقس مباشر (Open-Meteo)
- القرآن الكريم (نص، ترجمة، تفسير، تلاوة، بحث) — Quran.com API
- الأحاديث النبوية (مصادر مشتقة من sunnah.com، تُخزَّن للعمل دون اتصال)
- **الراديو**: إذاعات القرآن (mp3quran.net) وإذاعات إسلامية وجزائرية (radio-browser.info)
- التطبيقات المثبتة + المفضلة، زخارف ذهبية
- **تغيير خلفية الشاشة** (الإعدادات): خلفيات افتراضية، معرض صور المساجد (`app/src/main/assets/mosques/`)، اختيار صورة من الجهاز/USB عبر Android Storage Access Framework، معاينة قبل الاعتماد، اعتماد/إلغاء/استعادة الافتراضية، أوضاع العرض (اقتصاص / احتواء / تمديد) وتعتيم، ويُحفظ الاختيار بعد إعادة التشغيل
- **الخطوط** (الإعدادات ← نوع الخط): نسخ (افتراضي)، كوفي، ريم كوفي، المسيري، تجوّل، القاهرة، أميري
- يعمل كـ Launcher افتراضي (Home) أو كتطبيق عادي
- بلا إعلانات، بلا بيانات وهمية، يتعامل مع غياب الإنترنت

## التثبيت وتفعيل اللانشر / Install & activate
1. نزّل الـ APK وثبّته (اسمح بالتثبيت من مصادر غير معروفة).
2. افتح التطبيق ← الإعدادات ← **حالة اللانشر** لتعيينه كواجهة افتراضية أو إلغائه.

## مصادر البيانات / Data sources
Aladhan · Open-Meteo · BigDataCloud · Quran.com v4 · fawazahmed0/hadith-api · mp3quran.net · radio-browser.info

## الخطوط والتراخيص / Fonts & licenses
Noto Naskh Arabic، Noto Kufi Arabic، Reem Kufi، El Messiri، Tajawal، Cairo، Amiri — رخصة SIL OFL 1.1 (انظر مجلد `licenses/`).

## المطوّر / Developer
**Tayebi Abdelghani** — 🇩🇿 Algeria — tayebi02@gmail.com

## ادعم البرنامج / Support the app
إذا أعجبك البرنامج فيمكنك دعمه، جزاك الله خيراً.
If you like this app, you can support its development. Thank you!

| الوسيلة | الحساب |
|---|---|
| BaridiMob (بريدي موب) | `00799999000848709078` |
| PayPal | `tayebiabdelghani5@gmail.com` |
| Binance — USDT (BSC / BEP20 فقط) | `0xf319f4cc61e8c15891ce53aa7f61794929ac7c28` |

## البناء / Build
GitHub Actions يبني ويُنشر APK تلقائياً في Releases عند كل push. محلياً: `gradle :app:assembleRelease`.
