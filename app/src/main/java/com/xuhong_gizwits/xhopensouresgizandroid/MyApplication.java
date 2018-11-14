package com.xuhong_gizwits.xhopensouresgizandroid;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        SpeechUtility.createUtility(MyApplication.this, "appid=" + getString(R.string.app_id));
        super.onCreate();
    }
}
