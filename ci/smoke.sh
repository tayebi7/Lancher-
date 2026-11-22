#!/usr/bin/env bash
# Runs inside the Android TV emulator: install, launch, capture screenshot + logs.
set -x
PKG=com.tayebi.islamiclauncher
adb wait-for-device
adb install -r apk/app-release.apk
adb shell pm grant $PKG android.permission.ACCESS_COARSE_LOCATION || true
adb logcat -c
adb shell am start -n $PKG/.MainActivity
sleep 40
mkdir -p out
adb shell dumpsys activity activities | grep -E "mResumedActivity|topResumedActivity|ResumedActivity" > out/resumed.txt
cat out/resumed.txt
adb shell pidof $PKG > out/pid.txt || true
adb exec-out screencap -p > out/screenshot-home.png
adb logcat -d > out/logcat_full.txt
echo "=========== FATAL / ERRORS ==========="
grep -E "FATAL|AndroidRuntime|Engine|islamiclauncher" out/logcat_full.txt | head -80
echo "=========== PID ==========="
cat out/pid.txt
# move focus around and capture more screens
for sc in About Prayer Quran Hadith Radio Settings Background Apps; do
  adb shell am start -n $PKG/.MainActivity --es screen $sc
  sleep 8
  adb exec-out screencap -p > out/screenshot-$sc.png
done
exit 0
