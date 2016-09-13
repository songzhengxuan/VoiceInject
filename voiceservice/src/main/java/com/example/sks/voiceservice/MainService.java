package com.example.sks.voiceservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by sks on 2016/9/9.
 * 测试阶段，主要用于保活
 */
public class MainService extends Service {
    public static final String EXTRA_KEY_STRING_TEXT_INPUT = "voice_input";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY_COMPATIBILITY;
    }
}
