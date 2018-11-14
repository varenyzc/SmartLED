package com.xuhong_gizwits.xhopensouresgizandroid.Utils;

import java.util.Calendar;

/*
 * 项目名：iot_android-TCL
 * 包名：com.DeviceSceneModule.Utils
 * 文件名：TimersUtils
 * 创建时间：2017/5/5  上午 11:03
 * 创建者 xuhong
 * 描述：情景模式的定时工具类
 */
public class TimersUtils {

    /**
     * 返回一个将要的时间,单位是s
     *
     * @param temptimers 开始工作的时间四位数 格式 1025 表示 10:25
     * @return 将在这个时间之后启动
     */
    public static int creatTimers(String temptimers) {

        int returnTimers = 0;
        int times = Integer.parseInt(temptimers);
        Calendar mCalendar = Calendar.getInstance();
        //把当前时间转为 1234格式
        int NowTimes = mCalendar.get(Calendar.HOUR_OF_DAY) * 100 + mCalendar.get(Calendar.MINUTE);
        //如果设置定时的时间大于现在的时间 比如 当前是 10:23，设置的时间是 12:35
        if (NowTimes >= times) {
            returnTimers = 24 * 3600 - (creatMinute(NowTimes) - creatMinute(times));
        } else {
            returnTimers = (creatMinute(times) - creatMinute(NowTimes));
        }
        return returnTimers;
    }


    private static int creatMinute(int timers) {
        int[] value = new int[4];

        value[0] = timers % 10000 % 1000 % 100 % 10; //个位
        value[1] = timers / 10 % 1000 % 100 % 10;//十位
        value[2] = timers / 100 % 100 % 10;//百位
        value[3] = timers / 1000 % 10;//千位

        return value[0] * 60 + value[1] * 600 + value[2] * 3600 + value[3] * 10 * 3600;
    }

}
