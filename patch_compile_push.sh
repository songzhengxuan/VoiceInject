./gradlew :patch:assembleDebug
if [ $? -eq 0 ];then
    echo "start push"
    adb push patch/build/outputs/apk/patch-debug.apk /sdcard/patch.apk
fi

if [ ! -z $1 ];then
    echo "start push $1"
    adb push testscripts/$1 /sdcard/test.txt
fi

