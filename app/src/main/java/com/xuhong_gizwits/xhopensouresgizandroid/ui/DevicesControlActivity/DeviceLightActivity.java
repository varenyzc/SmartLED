package com.xuhong_gizwits.xhopensouresgizandroid.ui.DevicesControlActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;
import com.xuhong_gizwits.xhopensouresgizandroid.R;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.JsonParser;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.L;

import java.util.concurrent.ConcurrentHashMap;

public class DeviceLightActivity extends BaseDeviceControlActivity implements SeekBar.OnSeekBarChangeListener,CheckBox.OnCheckedChangeListener {

    private TextView mTvHumid;
    private TextView mTvTemp;
    private SeekBar mSbLumin;
    private SeekBar mSbWarmLight;
    public static CheckBox mLedonoff;

    //数据点相关
    private static final String KEY_LEDONOFF="Led_OnOff_Change"; //总开关
    private static final String KEY_LEDT = "Led_T"; //色温
    private static final String KEY_LEDS = "Led_S"; //亮度
    private static final String KEY_TEMP = "temperature"; //温度
    private static final String KEY_HUMID = "Humidity"; //湿度

    //数据点临时存储的数值
    private boolean tempLedOnoff=false;
    private int tempLEDTempure = 0;
    private int tempLEDLight = 0;
    private int tempTempure = 0;
    private int tempHumidity = 0;
    //Code
    private static final int CODE_HANDLER_UI = 105;


    @SuppressLint("HandlerLeak")
    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CODE_HANDLER_UI:
                    updateUI();
                    break;
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_light);
        initView();
        bindViews();
        //请求设备的最新状态
    }

    private void initView() {
        //显示状态返回箭头
        //设置标题，如果备注名为空则显示产品云端注册名字，否则显示备注名
        qmuiTopBar = findViewById(R.id.topBar);
        qmuiTopBar.setTitle(gizWifiDevice.getAlias().isEmpty() ? gizWifiDevice.getProductName() : gizWifiDevice.getAlias());
        qmuiTopBar.addLeftImageButton(R.mipmap.ic_back, R.id.topbar_left_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    //ui初始化
    private void bindViews() {
        mSbLumin = (SeekBar) findViewById(R.id.sbLumin);
        mSbLumin.setOnSeekBarChangeListener(this);

        mSbWarmLight = (SeekBar) findViewById(R.id.sbWarmLight);
        mSbWarmLight.setOnSeekBarChangeListener(this);

        mLedonoff = (CheckBox) findViewById(R.id.cb_ledonoff);
        mLedonoff.setOnCheckedChangeListener(this);

        mTvHumid = (TextView) findViewById(R.id.tvHumid);
        mTvTemp = (TextView) findViewById(R.id.tvTemp);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.cb_ledonoff) {
            sendCommand(KEY_LEDONOFF,mLedonoff.isChecked());
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(20);
        }
    }

    //拖动条点击事件
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.sbWarmLight:
                sendCommand(KEY_LEDT,seekBar.getProgress());
                break;
            case R.id.sbLumin:
                sendCommand(KEY_LEDS, seekBar.getProgress());
                break;
        }

    }

    private void parseReceiveData(ConcurrentHashMap<String, Object> dataMap) {
        L.d("==w", "云端下发数据：" + dataMap);
        //寻找对应的数据点
        if (dataMap.get("data") != null) {
            ConcurrentHashMap<String, Object> map = (ConcurrentHashMap<String, Object>) dataMap.get("data");

            for (String dataKey : map.keySet()) {
                //总开关
                if (dataKey.equals(KEY_LEDONOFF)) {
                    tempLedOnoff = (Boolean) map.get(dataKey);
                }
                if (dataKey.equals(KEY_LEDT)) {
                    tempLEDTempure = (int) map.get(dataKey);
                }
                if (dataKey.equals(KEY_LEDS)) {
                    tempLEDLight =(int) map.get(dataKey);
                }
                if (dataKey.equals(KEY_HUMID)) {
                    tempHumidity = (int) map.get(dataKey);
                }
                if (dataKey.equals(KEY_TEMP)) {
                    tempTempure = (int) map.get(dataKey);
                }
            }
            mHandler.sendEmptyMessage(CODE_HANDLER_UI);
        }

    }

    /**
     * Description:根据保存的的数据点的值来更新UI
     */
    protected void updateUI() {
        //总开关
        mLedonoff.setChecked(tempLedOnoff);
        mSbWarmLight.setProgress(tempLEDTempure);
        mSbLumin.setProgress(tempLEDLight);
        mTvTemp.setText(tempTempure+"℃");
        mTvHumid.setText(tempHumidity+"%");
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }


    @Override
    protected void didReceiveData(GizWifiErrorCode result, GizWifiDevice device, ConcurrentHashMap<String, Object> dataMap, int sn) {
        super.didReceiveData(result, device, dataMap, sn);
        if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
            if (dataMap != null) {
                if (!dataMap.isEmpty()) {
                    parseReceiveData(dataMap);
                }
            }
        }
    }

    @Override
    public void wakeupthings() {
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
                        mLedonoff.setChecked(true);
                        //sendMessage("ledon");
                    } else if (i == 0) {
                        speakAnswer("好的 灯已经关上啦");
                        mLedonoff.setChecked(false);
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
