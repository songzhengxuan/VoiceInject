package com.voiceinject.patcher;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.sks.voiceinject.ReflectUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import dalvik.system.DexClassLoader;

public class Patcher {
    private static final String TAG = "Patcher";
    private static volatile Patcher sInstance = null;

    private Context mContext;
    private DexClassLoader mClassLoader;
    private Object mVoiceInjectObj;
    private HashMap<String, Method> mReflectMethods = new HashMap<String, Method>();

    private Patcher(Context context) {
        mContext = context;
    }

    public static Patcher initInstance(Context context) {
        if (sInstance != null) {
            throw new RuntimeException("initInstance called twice");
        }
        synchronized (Patcher.class) {
            if (sInstance == null) {
                sInstance = new Patcher(context);
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
            mVoiceInjectObj = obj;
            Method foo = ReflectUtils.findMethod(libPatcherClazz, "foo");
            foo.invoke(obj);
            mReflectMethods.put("foo", foo);

            Method bar = ReflectUtils.findMethod(libPatcherClazz, "bar", Context.class);
            mReflectMethods.put("bar", bar);
            bar.invoke(null, mContext);

            Method onAppCreate = ReflectUtils.findMethod(libPatcherClazz, "onAppCreate", Application.class);
            mReflectMethods.put("onAppCreate", onAppCreate);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean onAppCreate(Application app) {
        Method method = mReflectMethods.get("onAppCreate");
        if (method == null) {
            throw new RuntimeException("onAppCreate called but cannot find valid method");
        }
        try {
            method.invoke(mVoiceInjectObj, app);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
