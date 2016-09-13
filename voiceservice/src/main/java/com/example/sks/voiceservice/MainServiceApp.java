package com.example.sks.voiceservice;

import android.app.Application;
import android.content.Intent;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

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

        StringBuffer param = new StringBuffer();
        param.append("appid="+getString(R.string.app_id));
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(MainServiceApp.this, param.toString());

        mCommandHelper.register(getApplicationContext());
    }
}
