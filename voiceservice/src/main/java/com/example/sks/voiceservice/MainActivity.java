package com.example.sks.voiceservice;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.sks.voiceservice.util.FucUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.TextUnderstander;
import com.iflytek.cloud.TextUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;
import com.iflytek.cloud.util.ResourceUtil;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "voicectrl_MainActivity";
    private String mLocalGrammar;
    private SpeechRecognizer mAsr;
    private  final String GRAMMAR_TYPE_BNF = "bnf";
    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.e(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码："+code);
            } else {
                Log.e(TAG, "init succeed");
            }
        }
    };
    /**
     * 构建语法监听器。
     */
    private GrammarListener grammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if(error == null){
                showTip("语法构建成功：" + grammarId);
            }else{
                showTip("2.语法构建失败,错误码：" + error.getErrorCode());
                showTip("builderror" + error.toString());
            }
        }
    };

    /**
     * 初始化监听器（文本到语义）。
     */
    private InitListener textUnderstanderListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "textUnderstanderListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码："+code);
            }
        }
    };

    // 本地语法构建路径
    private String grmPath = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/msc/test";

    private Button mButton;
    private TextUnderstander mTextUnderstander;
    private TextUnderstanderListener textListener = new TextUnderstanderListener() {

        @Override
        public void onResult(final UnderstanderResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (null != result) {
                        // 显示
                        Log.d(TAG, "understander result：" + result.getResultString());
                        String text = result.getResultString();
                    } else {
                        Log.d(TAG, "understander result:null");
                        showTip("识别结果不正确。");
                    }
                }
            });
        }

        @Override
        public void onError(SpeechError error) {
            // 文本语义不能使用回调错误码14002，请确认您下载sdk时是否勾选语义场景和私有语义的发布
            showTip("onError Code："	+ error.getErrorCode());
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.btn);
        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "before mLocalGrammar is " + mLocalGrammar);
                buildGrammar();
                startRecog();
                final String text = "北京今天天气怎么样";
                if (mTextUnderstander.isUnderstanding()) {
                    mTextUnderstander.cancel();
                    showTip("cancel");
                } else {
                    int ret = mTextUnderstander.understandText(text, textListener);
                }
            }
        });
        mAsr = SpeechRecognizer.createRecognizer(this, mInitListener);
        mTextUnderstander =  TextUnderstander.createTextUnderstander(this, textUnderstanderListener);
    }

    private void startRecog() {
        mAsr.setParameter(SpeechConstant.PARAMS, null);
        // 设置本地识别资源
        mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
        // 设置语法构建路径
        mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
        // 设置返回结果格式
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");
        // 设置本地识别使用语法id
        mAsr.setParameter(SpeechConstant.LOCAL_GRAMMAR, "call");
        // 设置识别的门限值
        mAsr.setParameter(SpeechConstant.MIXED_THRESHOLD, "30");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mAsr.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/asr.wav");
    }

    private void buildGrammar() {
        Log.e(TAG, "start buildGrammar");
        mLocalGrammar = FucUtil.readFile(this, "linkvr.bnf", "utf-8");
        Log.e(TAG, "mLocalGrammar is " + mLocalGrammar.length());
        mAsr.setParameter(SpeechConstant.PARAMS, null);
        // 设置文本编码格式
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING,"utf-8");
        // 设置引擎类型
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        // 设置语法构建路径
        Log.e(TAG, "grammar build path is " + grmPath);
        mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
        //使用8k音频的时候请解开注释
//					mAsr.setParameter(SpeechConstant.SAMPLE_RATE, "8000");
        // 设置资源路径
        mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
        String mContent = new String(mLocalGrammar);
        int ret = mAsr.buildGrammar(GRAMMAR_TYPE_BNF, mContent, grammarListener);
        Log.e(TAG, "buildGrammar ret is " + ret);
        if(ret != ErrorCode.SUCCESS){
            showTip("1:语法构建失败,错误码：" + ret);
        }
    }

    private void showTip(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, str);
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
            }
        });
    }

    //获取识别资源路径
    private String getResourcePath(){
        StringBuffer tempBuffer = new StringBuffer();
        //识别通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet"));
        return tempBuffer.toString();
    }
}
