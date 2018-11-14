package com.xuhong_gizwits.xhopensouresgizandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.xuhong_gizwits.xhopensouresgizandroid.R;

import java.util.List;

/*
 * 项目名：XHOpenSouresGizAndroid
 * 包名：com.xuhong_gizwits.xhopensouresgizandroid.adapter
 * 文件名：${DOCUMENT_NAME}
 * 创建时间：2018/3/13
 * 创建者： 徐宏  
 * CSDN:http://blog.csdn.net/xh870189248
 * GitHub:https://github.com/xuhongv
 * 描述：TODO
 */
public class LVDevicesListAdapter  extends BaseAdapter {


    private LayoutInflater inflater;
    private List<GizWifiDevice> mList;
    private Context mContext;

    public LVDevicesListAdapter(Context mContext, List<GizWifiDevice> mList) {
        this.mContext = mContext;
        this.mList = mList;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        GizWifiDevice device = mList.get(i);
        ViewHolerListView holder = null;
        View view;

        //缓存页面
        if (convertView == null) {
            view = inflater.inflate(R.layout.item_list_deveces, null);
            holder = new ViewHolerListView();

            holder.all = (RelativeLayout) view.findViewById(R.id.all);
            holder.tvDevicesStutas = (TextView) view.findViewById(R.id.tvDevicesStutas);
            holder.tvDevicesName = (TextView) view.findViewById(R.id.tvDevicesName);
            holder.ivDevicesIcon = (ImageView) view.findViewById(R.id.ivDevicesIcon);
            holder.ivNext = (ImageView) view.findViewById(R.id.ivNext);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolerListView) view.getTag();
        }


        //设置名字，先加载别名是否为空
        if (!device.getAlias().isEmpty()) {
            holder.tvDevicesName.setText(device.getAlias());
        } else {
            holder.tvDevicesName.setText(device.getProductName());
        }


        if (device.isLAN()) {
            holder.tvDevicesStutas.setText("局域网在线");
        } else {
            holder.tvDevicesStutas.setText("远程在线");
        }


        //设置状态
        if (device.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceOffline) {
            holder.ivNext.setVisibility(View.INVISIBLE);
            holder.tvDevicesStutas.setText("离线");
            holder.tvDevicesStutas.setTextColor(mContext.getResources().getColor(R.color.side_bar));
            holder.tvDevicesName.setTextColor(mContext.getResources().getColor(R.color.side_bar));
        } else {
            holder.ivNext.setVisibility(View.VISIBLE);
            holder.tvDevicesStutas.setTextColor(mContext.getResources().getColor(R.color.black0));
            holder.tvDevicesName.setTextColor(mContext.getResources().getColor(R.color.black0));
        }

        return view;

    }


    private class ViewHolerListView {
        TextView tvDevicesName, tvDevicesStutas;
        ImageView ivDevicesIcon, ivNext;
        RelativeLayout all;
    }
}
