package com.example.sks.voiceservice;

import android.app.Application;
import android.content.Intent;

/**
 * Created by sks on 2016/9/9.
 */
public class MainServiceApp extends Application {
    private DebugCommandHelper mCommandHelper = new DebugCommandHelper();
    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, MainService.class);
        startService(intent);

        mCommandHelper.register(getApplicationContext());
    }
}
