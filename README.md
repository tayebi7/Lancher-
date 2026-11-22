# Islamic Smart TV Launcher – Premium 4K

واجهة (Launcher) إسلامية لأجهزة Android TV / Google TV: ساعة وتاريخ هجري، مواقيت الصلاة، الطقس، القرآن الكريم، الأحاديث النبوية، والتطبيقات المثبتة. عربية RTL مع الفرنسية والإنجليزية.

Built with Kotlin + Jetpack Compose. minSdk 26.

## Data sources (real APIs, no fake data)
- Prayer times: Aladhan API (calculation method / school / manual minute offsets), cached monthly for offline use
- Weather & city search: Open-Meteo
- Quran text, tafsir, translation, audio: Quran.com API v4
- Hadith: open hadith dataset derived from sunnah.com (cached offline)
- Hijri date: computed on-device (Umm al-Qura) with manual ±2 day adjustment

## Build the APK
GitHub Actions builds it on every push: **Actions → Build APK → Artifacts**.
Locally: `gradle :app:assembleRelease` → `app/build/outputs/apk/release/`.

To use a real mosque photo as background, add `app/src/main/res/drawable/bg_mosque.jpg`.

## Developer
Tayebi Abdelghani – tayebi02@gmail.com
