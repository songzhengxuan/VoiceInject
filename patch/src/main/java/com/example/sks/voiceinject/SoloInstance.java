package com.example.sks.voiceinject;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.sks.patch.BuildConfig;
import com.robotium.solo.Solo;

import org.xutils.common.util.IOUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Created by sks on 2016/8/10.
 */
public class SoloInstance {
    static final String TAG = "SoloInstance";
    private static Uri VOICE_SERVICE_URI = Uri.parse("content://com.example.sks.voiceinject.provider");
    private static Solo solo;
    private static Context appContext;
    private static SoloInstance sInstance;
    private ScriptEngine mScriptEngine;
    private Context mContext;
    private IService mVoiceService;
    private HandlerThread mWorkerThread;
    private H mH;
    private IClient.Stub mClientImpl = new IClient.Stub() {

        @Override
        public String handleCmd(String cmd) throws RemoteException {
            Log.d(TAG, "receive handleCmd from voiceservice with cmd " + cmd);
            return doHandleCmd(cmd);
        }
    };

    private String doHandleCmd(String cmd) {
        Message msg = mH.obtainMessage(H.MSG_HANDLE_CMD, cmd);
        mH.sendMessage(msg);
        return "succeed";
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.song.test".equals(intent.getAction())) {
                final Intent zIntent = new Intent(intent);
                Thread t = new Thread() {
                    public void run() {
                        handleTest(zIntent);
                    }
                };
                t.start();
            }
        }
    };

    private void handleTest(Intent intent) {
        String extra = intent.getStringExtra("func");
        if ("getCurrentActivity".equals(extra)) {
            Activity currentActivity = solo.getCurrentActivity();
            Log.d(TAG, "getCurrentActivity is " + currentActivity);
        } else if ("click".equals(extra)) {
            Log.d(TAG, "before execute click");
            solo.clickOnText("Hello");
            Log.d(TAG, "execute click");
        } else if ("getAllViews".equals(extra)) {
            Log.d(TAG, "before getAllViews ");
            List<View> views = solo.getCurrentViews();
            Log.d(TAG, "getAllViews " + views.size());
        } else if ("exec".equals(extra)) {
            String script = null;
            if (intent.hasExtra("script"))  {
                script = intent.getStringExtra("script");
            } else if (intent.hasExtra("scriptPath")) {
                String scriptPath = intent.getStringExtra("scriptPath");
                script = readFileToString(scriptPath);
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "execute script on /sdcard/test.txt");
                    String scriptPath = "/sdcard/test.txt";
                    script = readFileToString(scriptPath);
                }
            }
            if (!TextUtils.isEmpty(script)) {
                mScriptEngine.startExecuteScript(script);
            }
        }
    }

    public static void initSoloInstance(Context context, Instrumentation inst) {
        if (sInstance != null) {
            return;
        }
        sInstance = new SoloInstance();
        Solo.Config config = new Solo.Config();
        config.commandLogging = true;
        config.commandLoggingTag = "voicectrl_solo";
        Log.d(TAG, "initSoloInstance called with version " + BuildConfig.VERSION_CODE);
        solo = new Solo(inst, config);
        appContext = context.getApplicationContext();
        IntentFilter filter = new IntentFilter("com.song.test");
        appContext.registerReceiver(sInstance.receiver, filter);
        sInstance.init(appContext, solo);
    }

    private void init(Context appContext, Solo solo) {
        mContext = appContext;
        mScriptEngine = new ScriptEngine(solo);
        connectToVoiceService();
        startWorkerThread();
    }

    private void startWorkerThread() {
        mWorkerThread = new HandlerThread("vj_worker");
        mWorkerThread.start();
        mH = new H(mWorkerThread.getLooper());
    }

    public static Solo getSolo() {
        return solo;
    }

    private static String readFileToString(String filePath) {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(filePath);
            String result = IOUtil.readStr(fin);
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeQuietly(fin);
        }
        return null;
    }

    private void connectToVoiceService() {
        try {
            Bundle result = mContext.getContentResolver().call(VOICE_SERVICE_URI, "getService", "voice", null);
            if (result == null) {
                Log.e(TAG, "Failed to getVoiceService(null call result). Need install voiceservice first");
                return;
            }

            IBinder binder = result.getBinder("binder");
            if (binder == null) {
                Log.e(TAG, "Failed to get voiceservice binder from result bundle");
            } else {
                mVoiceService = IService.Stub.asInterface(binder);
                mVoiceService.registerClient(mClientImpl);
                Log.d(TAG, "connectToVoiceService succeed with result binder " + mVoiceService.asBinder());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class H extends Handler {
        static final int MSG_HANDLE_CMD = 0;
        public H(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_CMD:
                    mScriptEngine.startExecuteScript((String) msg.obj);
                    break;
            }
        }
    }
}
