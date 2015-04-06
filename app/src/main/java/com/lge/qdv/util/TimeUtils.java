package com.lge.qdv.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kyusoo.kim on 2015-04-01.
 */
public class TimeUtils {
    long timeStamp;

    public static String getTimeUnit(long timestamp) {
        StringBuilder sb = new StringBuilder();

        long millisec = timestamp % 1000;
        timestamp /= 1000;

        long sec = timestamp % 60;
        timestamp /= 60;

        long minute = timestamp % 60;
        timestamp /= 60;

        long hour = timestamp % 24;
        timestamp /= 24;

        long day = timestamp % 365;
        timestamp /= 365;

        if(day > 0){
            sb.append(day + "day ");
        }
        if(hour > 0){
            sb.append(hour + "h");
        }
        if(minute > 0){
            sb.append(minute + "m");
        }
        if(sec > 0) {
            sb.append(sec + "s");
        }

        return sb.toString();
    }

    public TimeUtils() {
        timeStamp = System.currentTimeMillis();
    }
    public long getTimeStamp() {
        return timeStamp;
    }

    /*
     *  Year : y
     *  Month : M
     *  Day : d
     *  Hour : H
     *  Minute : m
     *  Second : s
     */
    public static String getDateTime(long timestamp, String format) {
        Date d = new Date(timestamp);
        String dateTime = new SimpleDateFormat(format).format(d);
        return dateTime;
    }

    public static String getDateTime(long timestamp) {
        return getDateTime(timestamp, "yyyy-MM-dd HH:mm:ss");
    }
}
