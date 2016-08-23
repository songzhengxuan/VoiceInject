package com.voiceinject.patcher;

import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;

import com.example.sks.voiceinject.ReflectUtils;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class Patcher {
    private static final String TAG = "Patcher";
    private static volatile Patcher sInstance = null;

    private Context mContext;
    private Instrumentation mInstrumentation;
    private DexClassLoader mClassLoader;

    private Patcher(Context context, Instrumentation app) {
        mContext = context;
        mInstrumentation = app;
    }

    public static Patcher initInstance(Context context, Instrumentation app) {
        if (sInstance != null) {
            throw new RuntimeException("initInstance called twice");
        }
        synchronized (Patcher.class) {
            if (sInstance == null) {
                sInstance = new Patcher(context, app);
                return sInstance;
            }
        }
        return sInstance;
    }

    public static Patcher getsInstance() {
        return sInstance;
    }

    public synchronized boolean loadPatch(String apkPath) {
        if (mClassLoader != null) {
            throw new RuntimeException("loadPatch called twice");
        }
        final File optimizedDexOutputPath = mContext.getCodeCacheDir();
        DexClassLoader cl = new DexClassLoader(apkPath, optimizedDexOutputPath.getAbsolutePath(), null, mContext.getClassLoader());
        Class libPatcherClazz = null;
        try {
            libPatcherClazz = cl.loadClass("com.example.sks.patch.VoiceInject");
            Log.d(TAG, "class is " + libPatcherClazz);
            Object obj = libPatcherClazz.newInstance();
            Method foo = ReflectUtils.findMethod(libPatcherClazz, "foo");
            foo.invoke(obj);

            Method bar = ReflectUtils.findMethod(libPatcherClazz, "bar", Context.class);
            bar.invoke(null, mContext);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
