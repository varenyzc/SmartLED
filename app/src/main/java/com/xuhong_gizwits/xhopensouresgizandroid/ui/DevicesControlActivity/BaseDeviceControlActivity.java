package com.xuhong_gizwits.xhopensouresgizandroid.ui.DevicesControlActivity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.xuhong_gizwits.xhopensouresgizandroid.BaseAIActivity;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.Constant;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.L;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public abstract class BaseDeviceControlActivity extends BaseAIActivity {


    protected GizWifiDevice gizWifiDevice;

    protected QMUITopBar qmuiTopBar;

    private QMUITipDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDevice();
    }


    private void initDevice() {
        //拿到上个界面传过来的设备
        gizWifiDevice = this.getIntent().getParcelableExtra("_wifiDevice");
        gizWifiDevice.setListener(gizWifiDeviceListener);

        dialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("同步状态...")
                .create();
        dialog.show();

        getStatusOfDevice();
    }

    //取消订阅
    @Override
    protected void onDestroy() {
        super.onDestroy();
        gizWifiDevice.setListener(null);
        switch (gizWifiDevice.getProductKey()) {
            case Constant.PET_PK:
                gizWifiDevice.setSubscribe(Constant.PET_PS, false);
                break;
            case Constant.TIMER_PK:
                gizWifiDevice.setSubscribe(Constant.TIMER_PS, false);
                break;
        }
    }


    /**
     * 设备订阅回调
     *
     * @param result       错误码
     * @param device       被订阅设备
     * @param isSubscribed 订阅状态
     */
    protected void didSetSubscribe(GizWifiErrorCode result, GizWifiDevice device, boolean isSubscribed) {
        L.e("设备订阅回调 didSetSubscribe result:" + result);
    }

    /**
     * 设备状态回调
     *
     * @param result  错误码
     * @param device  当前设备
     * @param dataMap 当前设备状态
     * @param sn      命令序号
     */
    protected void didReceiveData(GizWifiErrorCode result, GizWifiDevice device,
                                  ConcurrentHashMap<String, Object> dataMap, int sn) {
        L.e("设备状态回调 didReceiveData result: " + result);
        L.e("设备状态回调 didReceiveData dataMap: " + dataMap);

        if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
            dialog.dismiss();
        } else {
            if (!dialog.isShowing()) {
                dialog.show();
            }
        }
    }

    /**
     * 设备硬件信息回调
     *
     * @param result       错误码
     * @param device       当前设备
     * @param hardwareInfo 当前设备硬件信息
     */
    protected void didGetHardwareInfo(GizWifiErrorCode result, GizWifiDevice device,
                                      ConcurrentHashMap<String, String> hardwareInfo) {
        L.e(" didGetHardwareInfo 设备硬件信息回调 result :" + result);
        L.e(" didGetHardwareInfo 设备硬件信息回调 hardwareInfo :" + hardwareInfo);
    }

    /**
     * 修改设备信息回调
     *
     * @param result 错误码
     * @param device 当前设备
     */
    protected void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device) {
        L.e(" 修改设备信息回调:" + result);
        if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {

            qmuiTopBar.setTitle(Objects.equals(gizWifiDevice.getAlias(), "") || gizWifiDevice.getAlias() == null ? gizWifiDevice.getProductName() : gizWifiDevice.getAlias());
        }
    }

    /**
     * 设备状态变化回调
     */
    protected void didUpdateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus) {
        L.e(" didUpdateNetStatus 设备状态变化回调:" + netStatus);
        if (netStatus == GizWifiDeviceNetStatus.GizDeviceOffline) {
            Toast.makeText(this, "设备已断开！", Toast.LENGTH_SHORT).show();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            finish();
        }
    }


    /**
     * Description:页面加载后弹出等待框，等待设备可被控制状态回调，如果一直不可被控，等待一段时间后自动退出界面
     */
    public void getStatusOfDevice() {
        // 设备是否可控
        if (isDeviceCanBeControlled()) {
            // 可控则查询当前设备状态
            gizWifiDevice.getNetStatus();
            dialog.dismiss();
        }
    }

    private boolean isDeviceCanBeControlled() {
        return gizWifiDevice.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceControlled;
    }


    /**
     * 发送指令,下发单个数据点的命令可以用这个方法
     *
     * @param key   数据点对应的标识名
     * @param value 需要改变的值
     */
    protected void sendCommand(String key, Object value) {
        if (value == null) {
            return;
        }
        L.d("sendData:" + value);
        int sn = 5;
        ConcurrentHashMap<String, Object> hashMap = new ConcurrentHashMap<>();
        hashMap.put(key, value);
        gizWifiDevice.write(hashMap, sn);
    }


    private GizWifiDeviceListener gizWifiDeviceListener = new GizWifiDeviceListener() {

        /** 用于设备订阅 */
        public void didSetSubscribe(GizWifiErrorCode result, GizWifiDevice device, boolean isSubscribed) {
            BaseDeviceControlActivity.this.didSetSubscribe(result, device, isSubscribed);
        }

        /** 用于获取设备状态 */
        public void didReceiveData(GizWifiErrorCode result, GizWifiDevice device,
                                   ConcurrentHashMap<String, Object> dataMap, int sn) {
            BaseDeviceControlActivity.this.didReceiveData(result, device, dataMap, sn);
        }

        /** 用于设备硬件信息 */
        public void didGetHardwareInfo(GizWifiErrorCode result, GizWifiDevice device,
                                       ConcurrentHashMap<String, String> hardwareInfo) {
            BaseDeviceControlActivity.this.didGetHardwareInfo(result, device, hardwareInfo);
        }

        /** 用于修改设备信息 */
        public void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device) {
            BaseDeviceControlActivity.this.didSetCustomInfo(result, device);
        }

        /** 用于设备状态变化 */
        public void didUpdateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus) {

            if (netStatus == GizWifiDeviceNetStatus.GizDeviceControlled) {
                dialog.dismiss();
            }
            BaseDeviceControlActivity.this.didUpdateNetStatus(device, netStatus);
        }


    };


}
