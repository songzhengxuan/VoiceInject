package com.example.sks.voiceservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sks on 2016/9/12.
 */
public class DebugCommandHelper extends BroadcastReceiver {
    public static final String ACTION = "com.voice.test";
    public static final String TAG = "voicectrl_debugcmd";

    public void register(Context context) {
        IntentFilter filter = new IntentFilter(ACTION);
        context.getApplicationContext().registerReceiver(this, filter);
    }

    public void unregister(Context context){
        context.getApplicationContext().unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            handleReceiveAction(intent);
        } catch (JSONException e) {
            Log.d(TAG, "Failed to handle received action");
            e.printStackTrace();
        }
    }

    private void handleReceiveAction(Intent intent) throws JSONException {
        String func = intent.getStringExtra("method");
        String processName = intent.getStringExtra("process");
        String arg = intent.getStringExtra("arg");
        String arg1 = intent.getStringExtra("arg1");
        String arg2 = intent.getStringExtra("arg2");
        String cmdString = null;
        if (TextUtils.equals("clickOnText", func)
                || TextUtils.equals("longClickOnText", func)) {
                JSONObject cmdObj = new JSONObject();
                cmdObj.put("method", func);
                cmdObj.put("params", "String");
                cmdObj.put("param0", arg);
                cmdString = cmdObj.toString();
        } else {
            Log.e(TAG, "unknown quick command");
        }
        Log.d(TAG, "going to send process " + processName + " with command " + cmdString);
        if (TextUtils.isEmpty(cmdString)) {
            Log.d(TAG, "failed to build command");
            return;
        }
        if (TextUtils.isEmpty(processName)) {
            Log.d(TAG, "empty process name");
            return;
        }
        MainControlCenter.getInstance().executeCommand(cmdString, processName);
    }
}
