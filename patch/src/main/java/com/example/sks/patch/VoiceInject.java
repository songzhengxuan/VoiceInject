package com.example.sks.patch;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;

import com.example.sks.voiceinject.SoloInstance;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private Instrumentation mInstrumentation;

    public void onAppCreate(Application app) {
        Log.d(TAG, "this app is " + this);
        try {
            Class<?> activityThreadClz = app.getClassLoader().loadClass("android.app.ActivityThread");
            Log.d(TAG, "at class is " + activityThreadClz);
            Method getMethod = activityThreadClz.getMethod("currentActivityThread", null);
            Log.d(TAG, "method is " + getMethod);
            getMethod.setAccessible(true);
            Object atThreadObj = getMethod.invoke(null, null);
            Log.d(TAG, "activityThread obj is " + atThreadObj);
            Method getInstrumentationMethod = activityThreadClz.getMethod("getInstrumentation", null);
            Log.d(TAG, "getInstrumentationMethod is " + getInstrumentationMethod);
            Instrumentation instrumentation = (Instrumentation) getInstrumentationMethod.invoke(atThreadObj, null);
            Log.d(TAG, "instrumentation is " + instrumentation);
            try {
                Field appContextField = Instrumentation.class.getDeclaredField("mAppContext");
                appContextField.setAccessible(true);
                appContextField.set(instrumentation, app.getApplicationContext());
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            SoloInstance.initSoloInstance(app.getApplicationContext(), instrumentation);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
