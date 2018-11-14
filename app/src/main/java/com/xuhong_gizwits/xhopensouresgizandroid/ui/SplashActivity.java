package com.xuhong_gizwits.xhopensouresgizandroid.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.xuhong_gizwits.xhopensouresgizandroid.MainActivity;
import com.xuhong_gizwits.xhopensouresgizandroid.R;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.L;

import java.util.ArrayList;
import java.util.List;

/*
 * 项目名：XHOpenSouresGizAndroid
 * 包名：com.xuhong_gizwits.xhopensouresgizandroid.ui
 * 文件名：${DOCUMENT_NAME}
 * 创建时间：2018/3/16
 * 创建者： 徐宏  
 * CSDN:http://blog.csdn.net/xh870189248
 * GitHub:https://github.com/xuhongv
 * 描述：TODO
 */
public class SplashActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 205;
    private String[] manifest = {Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.RECORD_AUDIO};

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 105) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        checkAndroidPermission();
    }


    //检查是否大于6.0系统，并且动态授权
    private void checkAndroidPermission() {

        //是否大于6.0版本，需要动态授权
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestRuntimePermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION
                    , Manifest.permission.ACCESS_WIFI_STATE
                    , Manifest.permission.RECORD_AUDIO});
        } else {
            mHandler.sendEmptyMessageDelayed(105, 3000);
        }

    }

    //开始请求权限
    private void requestRuntimePermission(String[] permissions) {
        int status = 0;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
            } else {
                status++;
            }
        }
        //全部权限已经通过
        if (status == 6) {
            mHandler.sendEmptyMessageDelayed(105, 1500);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 103:
                if (grantResults.length > 0) {
                    List<String> deniedPermissions = new ArrayList<>();
                    for (int i = 0; i < grantResults.length; i++) {
                        int grantResult = grantResults[i];
                        String permission = permissions[i];
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            deniedPermissions.add(permission);
                        }
                    }
                    //权限全部通过
                    if (deniedPermissions.isEmpty()) {
                        L.e("权限全部通过");
                        mHandler.sendEmptyMessage(105);
                        //被拒绝权限
                    } else {
                        // L.e("被拒绝权限:" + deniedPermissions.toArray());
                        Toast.makeText(this, "您拒绝了部分权限！可以在设置—应用详情授权，否则无法正常使用哦。", Toast.LENGTH_LONG).show();
                        mHandler.sendEmptyMessageDelayed(105, 3000);
                    }
                }
                break;
        }
    }
}
