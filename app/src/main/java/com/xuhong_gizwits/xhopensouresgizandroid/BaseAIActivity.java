package com.xuhong_gizwits.xhopensouresgizandroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.UnderstanderResult;
import com.iflytek.sunflower.FlowerCollector;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.JsonParser;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.WakeUpUtil;
import com.xuhong_gizwits.xhopensouresgizandroid.bean.MainBean;
import com.xuhong_gizwits.xhopensouresgizandroid.ui.DevicesControlActivity.DeviceLightActivity;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class BaseAIActivity extends AppCompatActivity {
    public static final String PREFER_NAME = "com.voice.recognition";
    // 用HashMap存储听写结果
    public HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    public SpeechUnderstander mSpeechUnderstander;

    public WakeUpUtil wakeUpUtil;
    public MainBean mMainBean;
    public SharedPreferences mSharePreferences;
    public int ret = 0; //函数调用返回值
    // 引擎类型
    public String mEngineType = SpeechConstant.TYPE_CLOUD;
    public SpeechSynthesizer mTts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initAI();
    }

    public void initData() {
        mSharePreferences = getSharedPreferences(BaseAIActivity.PREFER_NAME,
                Activity.MODE_PRIVATE);
    }
    public void initAI() {
        mTts = SpeechSynthesizer.createSynthesizer(getApplicationContext(), mInitListener);
        if (mTts != null) {
            mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoxin");
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, "80");
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, "60");
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, "50");
        } else {
            Toast.makeText(getApplicationContext(),"mTts为null",Toast.LENGTH_SHORT).show();
        }

        mSpeechUnderstander = SpeechUnderstander.createUnderstander(getApplicationContext(), mInitListener);

        mSharePreferences = getSharedPreferences(MainActivity.PREFER_NAME,
                Activity.MODE_PRIVATE);
        wakeUpUtil = new WakeUpUtil(this) {
            @Override
            public void wakeUp() {
                speakAnswer("我在呢");
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //showTip("1");
                wakeupthings();
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mSpeechUnderstander.stopUnderstanding();
                wakeUpUtil.wake();
            }
        };
        wakeUpUtil.wake();
    }

    @Override
    protected void onStart() {
        super.onStart();
        wakeUpUtil.wake();
    }

    public InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            //LogUtil.L("SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(getApplicationContext(), "初始化失败，错误码：" + code,Toast.LENGTH_SHORT).show();
            }
        }
    };
    public void speakAnswer(String text) {
        // 移动数据分析，收集开始合成事件
        FlowerCollector.onEvent(getApplicationContext(), "tts_play");

        int code = mTts.startSpeaking(text, mTtsListener);
        //mFiveLine.setVisibility(View.VISIBLE);
        //mUnderstanderText.setText(text);
        /*int code = mTts.synthesizeToUri(text, Environment.getExternalStorageDirectory() + "/msc/tts.pcm", mTtsListener);*/
        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                //未安装则跳转到提示安装页面
                Toast.makeText(getApplicationContext(), "请安装语记！",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "语音合成失败，错误码：" + code,Toast.LENGTH_SHORT).show();
            }
        }
    }
    public SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            //showTip("开始播放");
            //speakflag = true;
        }

        @Override
        public void onSpeakPaused() {
            //showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            //showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            //mPercentForBuffering = percent;
            //showTip(String.format(getString(R.string.tts_toast_format),
            //      mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            //mPercentForPlaying = percent;
            //showTip(String.format(getString(R.string.tts_toast_format),
            //       mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                //showTip("播放完成");
                //mFiveLine.setVisibility(View.INVISIBLE);
                //speakflag = false;
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };
    public void wakeupthings() {
        //etContent.setText(null);// 清空显示内容
        mIatResults.clear();
        // 设置参数
        /*boolean isShowDialog = mSharedPreferences.getBoolean(
                getString(R.string.pref_key_iat_show), true);*/
                /*if (isShowDialog) {
                    // 显示听写对话框
                    mIatDialog.setListener(mRecognizerDialogListener);
                    mIatDialog.show();
                    showTip(getString(R.string.text_begin));
                } else {
                    // 不显示听写对话框
                    ret = mIat.startListening(mRecognizerListener);
                    if (ret != ErrorCode.SUCCESS) {
                        showTip("听写失败,错误码：" + ret);
                    } else {
                        showTip(getString(R.string.text_begin));
                    }
                }*/
        mTts.stopSpeaking();

        if (mSpeechUnderstander.isUnderstanding()) {// 开始前检查状态
            mSpeechUnderstander.stopUnderstanding();
            //showTip("停止录音");
        }
        ret = mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
        if (ret != 0) {
            Toast.makeText(getApplicationContext(), "语义理解失败，错误码：" + ret, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "请开始说话...", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 语义理解回调。
     */
    public SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {

        @Override
        public void onResult(final UnderstanderResult result) {
            if (null != result) {
                //Log.d(TAG, result.getResultString());

                // 显示
                String text = result.getResultString();
                //Log.e(TAG, text);
                mMainBean = JsonParser.parseIatResult(text);


                if (!TextUtils.isEmpty(text)) {
                    int i = ledstatus(mMainBean.getText());
                    if (i == 1) {
                        speakAnswer("好的 灯已经打开啦");
                        //sendMessage("ledon");
                    } else if (i == 0) {
                        speakAnswer("好的 灯已经关上啦");
                        //DeviceLightActivity.mLedonoff.setChecked(false);
                        //sendMessage("ledoff");
                    } else {
                        speakAnswer("听不清楚 请再说一次");
                    }
                }

            } else {
                Toast.makeText(getApplicationContext(),"识别结果不正确",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            //showTip("当前正在说话，音量大小：" + volume);
            //Log.d(TAG, data.length + "");
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            //showTip("结束说话");
            //heartProgressBar.dismiss();
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            //showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            //showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };
    private int ledstatus(String speak) {
        String on = ".*开.*";
        String off = ".*关.*";
        String led = ".*灯.*";
        if (speak.matches(on)&&speak.matches(led)) {
            return 1;
        } else if (speak.matches(off)&&speak.matches(led)) {
            return 0;
        }else
            return 2;
    }
}
