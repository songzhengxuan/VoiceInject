package com.example.sks.voiceinject;

import android.app.Application;
import android.util.Log;

/**
 * Created by sks on 2016/7/25.
 */
public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "this app is " + this);
    }
}
