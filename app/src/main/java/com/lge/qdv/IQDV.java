package com.lge.qdv;

import android.os.Build;

/**
 * Created by kyusoo.kim on 2015-04-01.
 */
public interface IQDV {
    public static final String TAG = "QDV";
    public static final int ID_NOTIFICATION = 261959;

    public static final boolean USER_DEBUG = !Build.TYPE.equals("user");

    public static final String QDV_DATA_STORE_DIRECTORY = "/sdcard/QDV";
    public static final String QDV_DATA_FILE_PATH = QDV_DATA_STORE_DIRECTORY + "/qdv.log";

    public static final int MSG_ERROR = -1;
    public static final int MSG_CLOSE_DIALOG = 1;
    public static final int MSG_UPDATE_DEVICE_INFO = 2;
}
