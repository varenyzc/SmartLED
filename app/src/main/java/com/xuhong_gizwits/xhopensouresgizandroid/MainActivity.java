package com.xuhong_gizwits.xhopensouresgizandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizEventType;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.Constant;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.L;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.SharePreUtils;
import com.xuhong_gizwits.xhopensouresgizandroid.adapter.LVDevicesListAdapter;
import com.xuhong_gizwits.xhopensouresgizandroid.ui.DevicesControlActivity.DevicePetActivity;
import com.xuhong_gizwits.xhopensouresgizandroid.ui.DevicesControlActivity.DevicesTimerLight;
import com.xuhong_gizwits.xhopensouresgizandroid.ui.Fragment.DataFragment;
import com.xuhong_gizwits.xhopensouresgizandroid.ui.Fragment.HomeFragment;
import com.xuhong_gizwits.xhopensouresgizandroid.ui.Fragment.SettingFragment;
import com.xuhong_gizwits.xhopensouresgizandroid.ui.NetConfigActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.github.xudaojie.qrcodelib.CaptureActivity;


public class MainActivity extends BaseAIActivity {
    private ViewPager viewPager;
    //底栏按钮
    private BottomNavigationView navigation;
    //顶栏
    private QMUITopBar qmuiTopBar;


    //上下文
    private Context mContext;

    //更新ui的状态码
    private final int updataUICode = 109;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == updataUICode) {
                HomeFragment.adapter.notifyDataSetChanged();
            }

        }
    };


    private static final int REQUEST_QR_CODE = 115;


    private int viewPagerSelected = 0;
    private String uid;
    private String token;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    qmuiTopBar.setTitle(R.string.app_name);
                    //右边增加加号
                    break;
                case R.id.navigation_data:
                    qmuiTopBar.setTitle(R.string.title_data);
                    break;
                case R.id.navigation_setting:
                    qmuiTopBar.setTitle(R.string.title_setting);
                    break;
            }
            viewPager.setCurrentItem(TabFragment.form(item.getItemId()).ordinal());
            return true;
        }
    };
    private enum TabFragment {

        home(R.id.navigation_home, HomeFragment.class),
        data(R.id.navigation_data, DataFragment.class),
        setting(R.id.navigation_setting, SettingFragment.class);

        private int menuId;
        private Class<? extends Fragment> mClass;
        private Fragment fragment;

        TabFragment(int menuId, Class<? extends Fragment> mClass) {
            this.menuId = menuId;
            this.mClass = mClass;
        }

        private Fragment fragment() {
            if (fragment == null) {
                try {
                    fragment = mClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    fragment = new Fragment();
                }
            }
            return fragment;
        }

        public static TabFragment form(int menuId) {
            for (TabFragment fragment : values()) {
                if (fragment.menuId == menuId) {
                    return fragment;
                }
            }
            return setting;
        }

        public static void onDestroy() {
            for (TabFragment fragment : values()) {
                fragment.fragment = null;
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
    }


    private void initView() {


        //此处初始化sdk
        ConcurrentHashMap<String, String> appInfo = new ConcurrentHashMap<>();
        appInfo.put("appId", Constant.APP_ID);
        appInfo.put("appSecret", Constant.APP_SECRET);

        List<ConcurrentHashMap<String, String>> productInfo = new ArrayList<>();

        ConcurrentHashMap<String, String> product = new ConcurrentHashMap<>();
        product.put("productKey", Constant.PET_PK);
        product.put("productSecret", Constant.PET_PS);
        productInfo.add(product);

        ConcurrentHashMap<String, String> product2 = new ConcurrentHashMap<>();
        product2.put("productKey", Constant.TIMER_PK);
        product2.put("productSecret", Constant.TIMER_PS);
        productInfo.add(product2);


        GizWifiSDK.sharedInstance().startWithAppInfo(this, appInfo, productInfo, null, false);
        String version = GizWifiSDK.sharedInstance().getVersion();
        L.e("version：" + version);


        //标题
        qmuiTopBar = (QMUITopBar)findViewById(R.id.topBar);
        qmuiTopBar.setTitle(getResources().getString(R.string.app_name));
        qmuiTopBar.addRightImageButton(R.mipmap.ic_add, R.id.topbar_right_change_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] items = new String[]{"一键配网", "扫描添加"};
                new QMUIDialog.MenuDialogBuilder(MainActivity.this)
                        .addItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        startActivityForResult(new Intent(MainActivity.this, NetConfigActivity.class), 105);
                                        break;
                                    case 1:
                                        Intent i = new Intent(MainActivity.this, CaptureActivity.class);
                                        startActivityForResult(i, REQUEST_QR_CODE);
                                        break;
                                }
                                dialog.dismiss();
                            }
                        })
                        .show();


            }
        });

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        viewPager = (ViewPager) findViewById(R.id.content);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return TabFragment.values()[position].fragment();
            }

            @Override
            public int getCount() {
                return TabFragment.values().length;
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewPagerSelected = position;
                supportInvalidateOptionsMenu();
                navigation.setSelectedItemId(TabFragment.values()[position].menuId);
            }
        });

        getLocalDevice();

    }

    private void getLocalDevice() {

        uid = SharePreUtils.getString(this, "_uid", null);
        token = SharePreUtils.getString(this, "_token", null);
        L.e("uid:" + uid + ",token:" + token);
        if (uid != null && token != null) {
            GizWifiSDK.sharedInstance().getBoundDevices(uid, token);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次返回都要注册一次sdk监听器，保证sdk状态能正确回调
        GizWifiSDK.sharedInstance().setListener(gizWifiSDKListener);
    }


    //设备在线刷新回调
    private GizWifiSDKListener gizWifiSDKListener = new GizWifiSDKListener() {


        @Override
        public void didBindDevice(int error, String errorMessage, String did) {
            super.didBindDevice(error, errorMessage, did);
            L.e("设备列表：didBindDevice error:" + error);
            L.e("设备列表：didBindDevice errorMessage:" + errorMessage);
        }


        @Override
        public void didBindDevice(GizWifiErrorCode result, String did) {
            super.didBindDevice(result, did);
            L.e("设备列表：didBindDevice result:" + result);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                Toast.makeText(mContext, "綁定成功！", Toast.LENGTH_SHORT).show();
                getLocalDevice();
            }
        }

        /** 用于设备列表 */
        @Override
        public void didDiscovered(GizWifiErrorCode result, List<GizWifiDevice> deviceList) {
            L.e("设备列表：onResume didDevicesCloudBack");
            L.e("设备列表：onResume deviceList:" + deviceList);
            HomeFragment.espDeviceList.clear();
            HomeFragment.espDeviceList.addAll(deviceList);
            for (int i = 0; i < HomeFragment.espDeviceList.size(); i++) {
                if (!deviceList.get(i).isBind()) {
                    startBindDevices(deviceList.get(i));
                }
            }
            mHandler.sendEmptyMessage(updataUICode);

        }

        /** 用于设备解绑 */
        public void didUnbindDevice(GizWifiErrorCode result, java.lang.String did) {
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                Toast.makeText(mContext, "删除成功！", Toast.LENGTH_SHORT).show();
                GizWifiSDK.sharedInstance().setListener(gizWifiSDKListener);
            } else {
                Toast.makeText(mContext, "删除失败！错误码：" + result, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void didNotifyEvent(GizEventType eventType, Object eventSource, GizWifiErrorCode eventID, String eventMessage) {
            super.didNotifyEvent(eventType, eventSource, eventID, eventMessage);
            L.e("机智云的SDK匿名登录前结果：" + eventType);
            if (eventType == GizEventType.GizEventSDK) {
                // 匿名登录
                GizWifiSDK.sharedInstance().userLoginAnonymous();
            }
        }

        @Override
        public void didUserLogin(GizWifiErrorCode result, String uid, String token) {
            super.didUserLogin(result, uid, token);
            L.e("机智云的SDK匿名登录结果：" + result);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                SharePreUtils.putString(getApplicationContext(), "_uid", uid);
                SharePreUtils.putString(getApplicationContext(), "_token", token);
            }
        }
    };



    //绑定设备方法
    private void startBindDevices(GizWifiDevice device) {
        String uid = SharePreUtils.getString(this, "_uid", null);
        String token = SharePreUtils.getString(this, "_token", null);
        L.e("uid:" + uid);
        L.e("token:" + token);
        if (uid != null && token != null) {
            GizWifiSDK.sharedInstance().bindRemoteDevice(uid, token, device.getMacAddress(), device.getProductKey(), Constant.PET_PS);
        }
    }

    //绑定设备方法二維碼
    private void startBindDevices(String did, String passcode) {
        String uid = SharePreUtils.getString(this, "_uid", null);
        String token = SharePreUtils.getString(this, "_token", null);
        L.e("uid:" + uid);
        L.e("token:" + token);
        if (uid != null && token != null) {
            L.e("开始绑定！");
            GizWifiSDK.sharedInstance().bindDevice(
                    uid
                    , token
                    , did
                    , passcode
                    , null);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK
                && requestCode == REQUEST_QR_CODE
                && data != null) {
            String text = data.getStringExtra("result");
            L.e("onActivityResult text:" + text);
            if (text.contains("product_key=") && text.contains("did=") && text.contains("passcode=")) {
                String did = getParamFomeUrl(text, "did");
                String passcode = getParamFomeUrl(text, "passcode");
                startBindDevices(did, passcode);
            } else {
                Toast.makeText(MainActivity.this, "请确认是我们的产品二维码哦亲~", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private String getParamFomeUrl(String url, String param) {
        String product_key = "";
        int startindex = url.indexOf(param + "=");
        startindex += (param.length() + 1);
        String subString = url.substring(startindex);
        int endindex = subString.indexOf("&");
        if (endindex == -1) {
            product_key = subString;
        } else {
            product_key = subString.substring(0, endindex);
        }
        return product_key;
    }


}
