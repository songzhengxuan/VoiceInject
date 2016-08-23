package com.example.sks.patch;

import android.content.Context;
import android.util.Log;

import java.util.Objects;

/**
 * Created by sks on 2016/8/20.
 */
public class VoiceInject {

    private static final String TAG = "VoiceInject";

    public void foo() {
        Log.d(TAG, "Hello world from " + Objects.hashCode(this));
    }

    public static void bar(Context context) {
        Log.d(TAG, "Hello context is " + context.getPackageName());
    }
}
