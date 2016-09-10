package com.example.sks.voiceservice;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by sks on 2016/9/9.
 */
public class ServiceProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Nullable
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if ("getService".equals(method)) {
            if ("voice".equals(arg)) {
                IBinder binder = MainControlCenter.getInstance().getService().asBinder();
                Bundle result = new Bundle();
                result.putBinder("binder", binder);
                return result;
            }
        }
        return super.call(method, arg, extras);
    }
}
