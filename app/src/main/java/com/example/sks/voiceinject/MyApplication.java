package com.example.sks.voiceinject;

import android.app.Application;
import android.util.Log;

import com.voiceinject.patcher.Patcher;

/**
 * Created by sks on 2016/7/25.
 */
public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    private Patcher mPatcher;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "this app is " + this);
        Patcher.initInstance(this);
        Patcher.getsInstance().loadPatch("/sdcard/patch.apk");
        Patcher.getsInstance().onAppCreate(this);
    }
}
