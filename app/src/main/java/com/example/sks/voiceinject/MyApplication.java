package com.example.sks.voiceinject;

import android.app.Application;
import android.app.Instrumentation;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sks on 2016/7/25.
 */
public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "this app is " + this);
        try {
            Class<?> activityThreadClz = getClassLoader().loadClass("android.app.ActivityThread");
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
                appContextField.set(instrumentation, getApplicationContext());
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            SoloInstance.initSoloInstance(this, instrumentation);
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
