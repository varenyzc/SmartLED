package com.xuhong_gizwits.xhopensouresgizandroid.ui.DevicesControlActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.xuhong_gizwits.xhopensouresgizandroid.R;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.TimersUtils;

import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

/*
 * 项目名：XHOpenSouresGizAndroid
 * 包名：com.xuhong_gizwits.xhopensouresgizandroid.ui.DevicesControlActivity
 * 文件名：${DOCUMENT_NAME}
 * 创建时间：2018/4/12
 * 创建者： 徐宏  
 * CSDN:http://blog.csdn.net/xh870189248
 * GitHub:https://github.com/xuhongv
 * 描述：定时开关灯
 */


public class DevicesTimerLight extends BaseDeviceControlActivity implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {


    //数据点相关
    private static final String KEY_TIMER_OPEN = "timerOpen";
    private static final String KEY_LIGHT_ON_OFF = "lightOnOff";
    private static final String KEY_IS_TIR_OPEN = "isTimerOpen";

    //数据点临时存储的数值
    private boolean tempIsTimerOpen = false;
    private boolean tempIsLightOpenOff = false;
    private int tempTimerOpen = 0;


    private Switch mSwIsOpen;
    private Switch mSwIsTimer;
    private RelativeLayout mRlTimes;


    private Calendar nowTimes;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 152) {
                UpdataUI();
            }
        }


    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_light);
        initView();
        bindViews();
    }

    private void bindViews() {
        mSwIsOpen = (Switch) findViewById(R.id.swIsOpen);
        mSwIsOpen.setOnClickListener(this);
        mSwIsTimer = (Switch) findViewById(R.id.swIsTimer);
        mSwIsTimer.setOnClickListener(this);
        mRlTimes = (RelativeLayout) findViewById(R.id.rlTimes);
        mRlTimes.setOnClickListener(this);

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


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            //开关灯指令
            case R.id.swIsOpen:
                sendCommand(KEY_LIGHT_ON_OFF, mSwIsOpen.isChecked());
                break;

            //定时器使能
            case R.id.swIsTimer:
                sendCommand(KEY_IS_TIR_OPEN, mSwIsTimer.isChecked());
                break;

            case R.id.rlTimes:
                //显示时间倒计时采集弹窗
                showDiaogTimesPicker();
                break;
        }

    }


    //显示时间倒计时采集弹窗
    private void showDiaogTimesPicker() {
        nowTimes = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                DevicesTimerLight.this,
                nowTimes.get(Calendar.HOUR_OF_DAY),
                nowTimes.get(Calendar.MINUTE),
                true
        );
        tpd.setTitle("选择一个时间点开灯");
        tpd.show(getFragmentManager(), "what");
    }


    //时间点弹窗回调方法
    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        //小时数值
        String hourString = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
        //分钟数值
        String minuteString = minute < 10 ? "0" + minute : "" + minute;

        int times = Integer.parseInt(hourString) * 100 + Integer.parseInt(minuteString);
        //拿到当前到用户想要的时间差
        int sendTimes = TimersUtils.creatTimers(times + "");

        sendCommand(KEY_TIMER_OPEN, sendTimes);


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


    //剖析云端数据
    private void parseReceiveData(ConcurrentHashMap<String, Object> dataMap) {
        if (dataMap != null) {
            if (!dataMap.isEmpty()) {
                ConcurrentHashMap<String, Object> map = (ConcurrentHashMap<String, Object>) dataMap.get("data");
                for (String dataKey : map.keySet()) {

                    //总开关
                    if (dataKey.equals(KEY_LIGHT_ON_OFF)) {
                        tempIsLightOpenOff = (Boolean) map.get(dataKey);
                    }


                    //定时器使能
                    if (dataKey.equals(KEY_IS_TIR_OPEN)) {
                        tempIsTimerOpen = (Boolean) map.get(dataKey);
                    }
                }
            }
            mHandler.sendEmptyMessage(152);
        }
    }


    //更新UI
    private void UpdataUI() {
        mSwIsTimer.setChecked(tempIsTimerOpen);
        mSwIsOpen.setChecked(tempIsLightOpenOff);
    }


}
