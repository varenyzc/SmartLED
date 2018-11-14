package com.xuhong_gizwits.xhopensouresgizandroid.ui.DevicesControlActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.xuhong_gizwits.xhopensouresgizandroid.R;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.L;

import java.util.concurrent.ConcurrentHashMap;

public class DevicePetActivity extends BaseDeviceControlActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {


    private com.qmuiteam.qmui.widget.QMUITopBar mTopBar;
    private Switch mSw_bool_LED_OnOff;
    private Spinner mSp_enum_LED_Color;
    private TextView mTv_data_LED_R;
    private SeekBar mSb_data_LED_R;
    private TextView mTv_data_LED_G;
    private SeekBar mSb_data_LED_G;
    private TextView mTv_data_LED_B;
    private SeekBar mSb_data_LED_B;
    private TextView mTv_data_Motor_Speed;
    private SeekBar mSb_data_Motor_Speed;
    private TextView mTv_data_Temperature;
    private TextView mTv_data_Humidity;
    private Switch mSw_data_Infra;


    //数据点相关
    private static final String KEY_SWITCH = "LED_OnOff";
    private static final String KEY_LIGHT_R = "LED_R";
    private static final String KEY_LIGHT_B = "LED_B";
    private static final String KEY_LIGHT_G = "LED_G";
    private static final String KEY_HUMIDITY = "Humidity";
    private static final String KEY_MOTOR = "Motor_Speed";
    private static final String KEY_INFRARED = "Infrared";
    private static final String KEY_LED_COLOR = "LED_Color";
    private static final String KEY_TEMPERTURE = "Temperature";


    //数据点临时存储的数值
    private boolean tempSwitch = false;
    private int tempLightType = 0;
    private int tempLightRed = 0;
    private int tempLightGreen = 0;
    private int tempLightBlue = 0;
    private int tempTemperture = 0;
    private int tempHumidity = 0;
    private boolean tempIsInfrared = false;
    private int tempMotorSpeed = 0;

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
        setContentView(R.layout.activity_device_pet);
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

        mSw_bool_LED_OnOff = (Switch) findViewById(R.id.sw_bool_LED_OnOff);
        mSw_bool_LED_OnOff.setOnClickListener(this);

        mSp_enum_LED_Color = (Spinner) findViewById(R.id.sp_enum_LED_Color);
        mSp_enum_LED_Color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        sendCommand(KEY_LED_COLOR, 1);
                        break;
                    case 2:
                        sendCommand(KEY_LED_COLOR, 2);
                        break;
                    case 3:
                        sendCommand(KEY_LED_COLOR, 3);
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mTv_data_LED_R = (TextView) findViewById(R.id.tv_data_LED_R);
        mSb_data_LED_R = (SeekBar) findViewById(R.id.sb_data_LED_R);
        mTv_data_LED_G = (TextView) findViewById(R.id.tv_data_LED_G);
        mSb_data_LED_G = (SeekBar) findViewById(R.id.sb_data_LED_G);
        mTv_data_LED_B = (TextView) findViewById(R.id.tv_data_LED_B);
        mSb_data_LED_B = (SeekBar) findViewById(R.id.sb_data_LED_B);
        mTv_data_Motor_Speed = (TextView) findViewById(R.id.tv_data_Motor_Speed);

        //马达设置最大数值为10，范围-5到5
        mSb_data_Motor_Speed = (SeekBar) findViewById(R.id.sb_data_Motor_Speed);
        mSb_data_Motor_Speed.setMax(10);

        mTv_data_Temperature = (TextView) findViewById(R.id.tv_data_Temperature);
        mTv_data_Humidity = (TextView) findViewById(R.id.tv_data_Humidity);
        mSw_data_Infra = (Switch) findViewById(R.id.sw_data_Infra);
        mSw_data_Infra.setFocusable(false);

        //电机拖动事件
        mSb_data_Motor_Speed.setOnSeekBarChangeListener(this);

        mSb_data_LED_R.setMax(254);
        mSb_data_LED_G.setMax(254);
        mSb_data_LED_B.setMax(254);

        mSb_data_LED_R.setOnSeekBarChangeListener(this);
        mSb_data_LED_G.setOnSeekBarChangeListener(this);
        mSb_data_LED_B.setOnSeekBarChangeListener(this);


    }


    //按钮点击事件
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sw_bool_LED_OnOff:
                sendCommand(KEY_SWITCH, mSw_bool_LED_OnOff.isChecked());
                break;
        }
    }

    //拖动条点击事件
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.sb_data_LED_R:
                sendCommand(KEY_LIGHT_R, seekBar.getProgress());
                break;
            case R.id.sb_data_LED_G:
                sendCommand(KEY_LIGHT_G, seekBar.getProgress());
                break;
            case R.id.sb_data_LED_B:
                sendCommand(KEY_LIGHT_B, seekBar.getProgress());
                break;
            case R.id.sb_data_Motor_Speed:
                sendCommand(KEY_MOTOR, seekBar.getProgress() - 5);
                break;
            default:
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
                if (dataKey.equals(KEY_SWITCH)) {
                    tempSwitch = (Boolean) map.get(dataKey);
                }
                if (dataKey.equals(KEY_LED_COLOR)) {
                    tempLightType = (Integer) map.get(dataKey);
                }

                //红灯
                if (dataKey.equals(KEY_LIGHT_R)) {
                    tempLightRed = (Integer) map.get(dataKey);
                }

                //绿灯
                if (dataKey.equals(KEY_LIGHT_G)) {
                    tempLightGreen = (Integer) map.get(dataKey);
                }

                //蓝灯
                if (dataKey.equals(KEY_LIGHT_B)) {
                    tempLightBlue = (Integer) map.get(dataKey);
                }

                if (dataKey.equals(KEY_TEMPERTURE)) {
                    tempTemperture = (Integer) map.get(dataKey);
                }

                if (dataKey.equals(KEY_HUMIDITY)) {
                    tempHumidity = (Integer) map.get(dataKey);
                }

                if (dataKey.equals(KEY_INFRARED)) {
                    tempIsInfrared = (boolean) map.get(dataKey);
                }

                if (dataKey.equals(KEY_MOTOR)) {
                    tempMotorSpeed = (Integer) map.get(dataKey);
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
        mSw_bool_LED_OnOff.setChecked(tempSwitch);

        //颜色
        mSp_enum_LED_Color.setSelection(tempLightType);

        //湿度
        mTv_data_Temperature.setText("" + tempHumidity);
        //温度
        mTv_data_Humidity.setText("" + tempTemperture);
        //红外
        mSw_data_Infra.setChecked(tempIsInfrared);

        //马达
        mSb_data_Motor_Speed.setProgress(tempMotorSpeed + 5);
        mTv_data_Motor_Speed.setText(tempMotorSpeed + "");

        //rgb
        mSb_data_LED_R.setProgress(tempLightRed);
        mSb_data_LED_G.setProgress(tempLightGreen);
        mSb_data_LED_B.setProgress(tempLightBlue);

        mTv_data_LED_R.setText(tempLightRed + "");
        mTv_data_LED_G.setText(tempLightGreen + "");
        mTv_data_LED_B.setText(tempLightBlue + "");


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

}
