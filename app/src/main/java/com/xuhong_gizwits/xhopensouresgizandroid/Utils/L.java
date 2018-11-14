package com.xuhong_gizwits.xhopensouresgizandroid.Utils;

import android.util.Log;

/*
 * 项目名：XHOpenSouresGizAndroid
 * 包名：com.xuhong_gizwits.xhopensouresgizandroid.Utils
 * 文件名：L
 * 创建时间：2018/3/13
 * 创建者： 徐宏  
 * CSDN:http://blog.csdn.net/xh870189248
 * GitHub:https://github.com/xuhongv
 * 描述：TODO
 */
public class L {

    private static boolean isLog = true;

    private static final String TAG = "smarthomeLog";

    public static void i(String tag, String msg) {
        if (isLog) {
            Log.i(tag, msg);
        }
    }

    public static void i(String msg) {
        if (isLog) {
            Log.i(TAG, msg);
        }
    }



    public static void d(String tag, String msg) {
        if (isLog) {
            Log.d(tag, msg);
        }
    }

    public static void d(String msg) {
        if (isLog) {
            Log.d(TAG, msg);
        }
    }







    public static void e(String tag, String msg) {
        if (isLog) {
            Log.e(tag, msg);
        }
    }

    public static void e(String msg) {
        if (isLog) {
            Log.e(TAG, msg);
        }
    }

}
