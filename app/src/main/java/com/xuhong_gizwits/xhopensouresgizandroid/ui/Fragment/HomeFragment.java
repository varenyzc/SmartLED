package com.xuhong_gizwits.xhopensouresgizandroid.ui.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.xuhong_gizwits.xhopensouresgizandroid.R;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.Constant;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.L;
import com.xuhong_gizwits.xhopensouresgizandroid.Utils.SharePreUtils;
import com.xuhong_gizwits.xhopensouresgizandroid.adapter.LVDevicesListAdapter;
import com.xuhong_gizwits.xhopensouresgizandroid.ui.DevicesControlActivity.DeviceLightActivity;
import com.xuhong_gizwits.xhopensouresgizandroid.ui.DevicesControlActivity.DevicesTimerLight;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    //设备列表控件
    private ListView listView;
    //下拉刷新控件
    private SwipeRefreshLayout mSwipeRefreshLayout;

    //设备集合
    public static List<GizWifiDevice> espDeviceList;
    //弹窗
    private QMUITipDialog tipDialog;
    private QMUITipDialog refleshDialog;
    //适配器
    public static LVDevicesListAdapter adapter;


    @Override
    public void onResume() {
        super.onResume();
        getLocalDevice();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragement_home, container, false);
        initView(view);

        return view;
    }

    private void initView(View view) {
        // 设置下拉进度的主题颜色
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.mSwipeRefreshLayout);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                android.R.color.holo_blue_bright, R.color.colorPrimaryDark,
                android.R.color.holo_orange_dark, android.R.color.holo_red_dark, android.R.color.holo_purple);
        // 手动调用,通知系统去测量
        mSwipeRefreshLayout.measure(0, 0);

        //手动下拉刷新回调
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                refleshDialog = new QMUITipDialog.Builder(getContext())
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord("努力刷新...")
                        .create();
                refleshDialog.show();

                //3s之后重新获取
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        L.e("设备列表：onResume GizWifiSDK.sharedInstance().getDeviceList():" + GizWifiSDK.sharedInstance().getDeviceList());
                        if (GizWifiSDK.sharedInstance().getDeviceList().size() != 0) {
                            espDeviceList.clear();
                            espDeviceList.addAll(GizWifiSDK.sharedInstance().getDeviceList());
                            adapter.notifyDataSetChanged();
                        }


                        refleshDialog.dismiss();
                        mSwipeRefreshLayout.setRefreshing(false);

                        if (espDeviceList.size() == 0) {

                            tipDialog = new QMUITipDialog.Builder(getContext())
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                    .setTipWord("暂无设备")
                                    .create();
                            tipDialog.show();

                            mSwipeRefreshLayout.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    tipDialog.dismiss();
                                }
                            }, 1500);


                        } else {

                            tipDialog = new QMUITipDialog.Builder(getContext())
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                    .setTipWord("刷新成功")
                                    .create();
                            tipDialog.show();

                            mSwipeRefreshLayout.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    tipDialog.dismiss();
                                }
                            }, 1000);

                        }


                    }
                }, 3000);


            }


        });
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        espDeviceList = new ArrayList<>();

        //设备列表控件初始化并绑定上数据
        adapter = new LVDevicesListAdapter(getContext(), espDeviceList);
        listView = view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 3000);
        //设备点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startControl(espDeviceList.get(i));
            }
        });
        //长按
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showLongOnClickDialog(espDeviceList.get(i));
                return true;
            }
        });
        getLocalDevice();
    }
    //跳转控制
    private void startControl(GizWifiDevice device) {

        //离线设备不可能会跳转到控制页面
        if (device.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceOffline)
            return;

        device.setListener(gizWifiDeviceListener);
        L.e("intent KEY:" + device.getProductKey());

        //此处订阅，需要识别pk和ps
        switch (device.getProductKey()) {
            case Constant.PET_PK:
                device.setSubscribe(Constant.PET_PS, true);
                break;
            case Constant.TIMER_PK:
                device.setSubscribe(Constant.TIMER_PS, true);
                break;
        }

    }
    //listDialog
    private void showLongOnClickDialog(final GizWifiDevice mIEspDevice) {

        final String[] items = new String[]{"重命名设备", "删除设备"};
        new QMUIDialog.MenuDialogBuilder(getContext())
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case 0:
                                showReNameDialog(mIEspDevice);
                                break;
                            case 1:
                                showDeleteDialog(mIEspDevice);
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .show();
    }
    //重命名弹窗
    private void showReNameDialog(final GizWifiDevice mIEspDevice) {

        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getContext());
        builder.setTitle("重命名操作")
                .setPlaceholder("在此输入新名字")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确认", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        if (builder.getEditText().getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(), "输入为空，修改失败！", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                        mIEspDevice.setListener(gizWifiDeviceListener);
                        mIEspDevice.setCustomInfo(null, builder.getEditText().getText().toString());
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //删除弹窗
    private void showDeleteDialog(final GizWifiDevice mIEspDevice) {
        new QMUIDialog.MessageDialogBuilder(getContext())
                .setTitle("标题")
                .setMessage("确定要删除吗？")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {

                        dialog.dismiss();
                    }
                })
                .addAction(0, "删除", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        String uid = SharePreUtils.getString(getContext(), "_uid", null);
                        String token = SharePreUtils.getString(getContext(), "_token", null);
                        GizWifiSDK.sharedInstance().unbindDevice(uid, token, mIEspDevice.getDid());
                        dialog.dismiss();
                    }
                })
                .show();
    }
    //设备订阅回调
    private GizWifiDeviceListener gizWifiDeviceListener = new GizWifiDeviceListener() {


        //设备信息修改回调
        @Override
        public void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device) {
            super.didSetCustomInfo(result, device);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                Toast.makeText(getContext(), "修改成功！", Toast.LENGTH_SHORT).show();
                if (GizWifiSDK.sharedInstance().getDeviceList().size() != 0) {
                    HomeFragment.espDeviceList.clear();
                    HomeFragment.espDeviceList.addAll(GizWifiSDK.sharedInstance().getDeviceList());
                    HomeFragment.adapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(getContext(), "修改失败：" + result, Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void didSetSubscribe(GizWifiErrorCode result, GizWifiDevice device, boolean isSubscribed) {
            super.didSetSubscribe(result, device, isSubscribed);
            L.e("跳转结果：" + result);
            L.e("跳转结果device：" + device);

            //跳转判断
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                Intent intent = new Intent();
                intent.putExtra("_wifiDevice", device);
                switch (device.getProductKey()) {
                    //微信宠物屋
                    case Constant.PET_PK:
                        intent.setClass(getContext(), DeviceLightActivity.class);
                        startActivity(intent);
                        break;
                    //定时开关灯
                    case Constant.TIMER_PK:
                        intent.setClass(getContext(), DevicesTimerLight.class);
                        startActivity(intent);
                        break;
                }
            }


        }
    };
    private void getLocalDevice() {

        String uid = SharePreUtils.getString(getContext(), "_uid", null);
        String token = SharePreUtils.getString(getContext(), "_token", null);
        L.e("uid:" + uid + ",token:" + token);
        if (uid != null && token != null) {
            GizWifiSDK.sharedInstance().getBoundDevices(uid, token);
        }
    }
}
