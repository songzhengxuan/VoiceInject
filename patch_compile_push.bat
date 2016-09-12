call ./gradlew :patch:assembleDebug
call adb push patch/build/outputs/apk/patch-debug.apk /sdcard/patch.apk
