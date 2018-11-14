package com.xuhong_gizwits.xhopensouresgizandroid.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiConfigureMode;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.enumration.GizWifiGAgentType;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.xuhong_gizwits.xhopensouresgizandroid.R;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.L;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.WifiAdminUtils;

import java.util.ArrayList;
import java.util.List;

public class NetConfigActivity extends AppCompatActivity implements View.OnClickListener {


    //ui控件
    private TextView mTvApSsid;
    private EditText mEdtApPassword;
    private Button btAdd;
    private CheckBox cbPaw;

    //弹窗配网进度条
    private ProgressDialog dialog;

    //获取手机当前网络wifi
    private WifiAdminUtils mWifiAdmin;

    //标题控件
    private QMUITopBar qmuiTopBar;

    //handler机制
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 105) {
                dialog.setMessage("配网成功...");
            } else if (msg.what == 106) {
                dialog.setMessage("配网失败...");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_config);
        //显示状态返回箭头
        //标题
        qmuiTopBar = findViewById(R.id.topBar);
        qmuiTopBar.setTitle("添加设备");
        qmuiTopBar.addLeftImageButton(R.mipmap.ic_back, R.id.topbar_left_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initView();
    }

    //初始化View
    private void initView() {

        mWifiAdmin = new WifiAdminUtils(this);
        mTvApSsid = (TextView) findViewById(R.id.tvApSsid);
        mEdtApPassword = (EditText) findViewById(R.id.edApPassword);

        cbPaw = (CheckBox) findViewById(R.id.cbPaw);
        cbPaw.setVisibility(View.GONE);
        cbPaw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mEdtApPassword.setInputType(0x90);
                } else {
                    mEdtApPassword.setInputType(0x81);
                }
            }
        });

        btAdd = (Button) findViewById(R.id.btAdd);
        btAdd.setOnClickListener(this);

        //密码的输入的动态监听
        mEdtApPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().isEmpty()) {
                    cbPaw.setVisibility(View.VISIBLE);
                } else {
                    cbPaw.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    //点击事件
    @Override
    public void onClick(View view) {
        //点击了搜索按钮将触发下面的事件代码
        if (view == btAdd) {
            String apSsid = mTvApSsid.getText().toString().intern();
            String apPassword = mEdtApPassword.getText().toString().intern();
            if (!apSsid.isEmpty()) {

                dialog = new ProgressDialog(NetConfigActivity.this);
                dialog.setMessage("努力配网中...");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        //停止配网
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        finish();
                    }
                });
                dialog.show();

                //未成功前按钮将不再显示
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
                //dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);

                //开始一键配网
                startAirlink(apSsid, apPassword);
            }
        }

    }

    //页面打开前，获取到手机已经链接的WIFi名字
    @Override
    protected void onResume() {
        super.onResume();
        // 展示当前显示已经连接的wifi
        String apSsid = mWifiAdmin.getWifiConnectedSsid();
        if (apSsid != null) {
            mTvApSsid.setText(apSsid);
        } else {
            mTvApSsid.setText("");
        }
        // check whether the wifi is connected
        boolean isApSsidEmpty = TextUtils.isEmpty(apSsid);
        btAdd.setEnabled(!isApSsidEmpty);
    }


    private void startAirlink(String ssid, String name) {


        //这里我仅仅用ESp8266模块,所以固定
        List<GizWifiGAgentType> types = new ArrayList<>();
        types.add(GizWifiGAgentType.GizGAgentESP);

        L.e("配置的etSSID:" + ssid);
        L.e("配置的etPas:" + name);

        //开始一键配网
        GizWifiSDK.sharedInstance().setListener(mListener);
        GizWifiSDK.sharedInstance().setDeviceOnboarding(ssid, name, GizWifiConfigureMode.GizWifiAirLink, null, 60, types);
        //下面是新版本的SDK的配网方式，如果兼容旧版本的8266，请使用以上方法。
        // GizWifiSDK.sharedInstance().setDeviceOnboardingDeploy(ssid, name, GizWifiConfigureMode.GizWifiAirLink, null, 60, types, false);
    }


    // 实现陪配网回调
    private GizWifiSDKListener mListener = new GizWifiSDKListener() {
        @Override
        public void didSetDeviceOnboarding(GizWifiErrorCode result, GizWifiDevice device) {
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 配置成功
                mHandler.sendEmptyMessage(105);
            } else {
                // 配置失败
                mHandler.sendEmptyMessage(106);
            }

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
        }
    };


    //返回箭头点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }


}