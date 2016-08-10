package com.example.sks.voiceinject;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;

import com.robotium.solo.Solo;

import java.util.List;

/**
 * Created by sks on 2016/8/10.
 */
public class SoloInstance {
    static final String TAG = "SoloInstance";
    private static Solo solo;
    private static Context appContext;
    private static BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.song.test".equals(intent.getAction())) {
                String extra = intent.getStringExtra("func");
                if ("getCurrentActivity".equals(extra)) {
                    Activity currentActivity = solo.getCurrentActivity();
                    Log.d(TAG, "getCurrentActivity is " + currentActivity);
                } else if ("click".equals(extra)) {
                    Log.d(TAG, "before execute click");
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            solo.clickOnText("Hello World!");
                        }
                    };
                    t.start();
                    Log.d(TAG, "execute click");
                } else if ("getAllViews".equals(extra)) {
                    Log.d(TAG, "before getAllViews ");
                    List<View> views = solo.getCurrentViews();
                    Log.d(TAG, "getAllViews " + views.size());
                }
            }
        }
    };

    public static void initSoloInstance(Context context, Instrumentation inst) {
        solo = new Solo(inst);
        appContext = context.getApplicationContext();
        IntentFilter filter = new IntentFilter("com.song.test");
        appContext.registerReceiver(receiver, filter);
    }

    public static Solo getSolo() {
        return solo;
    }
}
