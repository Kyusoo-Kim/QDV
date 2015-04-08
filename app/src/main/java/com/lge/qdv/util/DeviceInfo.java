package com.lge.qdv.util;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.lge.qdv.IQDV;
import com.lge.qdv.InfoActivity;
import com.lge.qdv.preference.Preference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by kyusoo.kim on 2015-04-01.
 */
public class DeviceInfo {

    private long mTimeStamp;

    private int mMemFree;
    private int mCached;

    private int mPageFault;
    private int mPageMajorFault;

    private int mXoTherm;

    private int mLMK;

    private int mFreeMem;

    DeviceInfoReader mInfoReader;

    String mErrorMessage = new String();
    Handler mHandler;

    public DeviceInfo(long timeStamp, int memFree, int cached, int xoTherm, int lmk){
        this.mTimeStamp = timeStamp;
        this.mMemFree = memFree;
        this.mCached = cached;
        this.mFreeMem = memFree + cached;
        this.mXoTherm = xoTherm;
        this.mLMK = lmk;
    }

    public DeviceInfo(Handler handler){

        mHandler = handler;

        TimeUtils timer = new TimeUtils();
        mTimeStamp = timer.getTimeStamp();

        mInfoReader = DeviceInfoReader.getDeviceInfoReader();

        readFreeMemInfo();
        readPageInfo();
        readThermalInfo();
        readLMKInfo();

        if(mLMK < 0){
            Message msg = new Message();
            msg.obj = mErrorMessage;
            msg.what = IQDV.MSG_ERROR;
            mHandler.sendMessage(msg);
        }
    }

    public int getXoTherm() {
        return mXoTherm;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public int getMemFree() {
        return mMemFree;
    }

    public int getCached() {
        return mCached;
    }

    public int getFreeMem() { return mFreeMem; }

    public int getPageFault() {
        return mPageFault;
    }

    public int getPageMajorFault() {
        return mPageMajorFault;
    }

    public int getLMK() { return mLMK; }

    public String getErrorMessage() { return mErrorMessage; }

    public void readFreeMemInfo(){
        byte[] buffer = new byte[1024];

        try {
            mMemFree = 0;
            mCached = 0;

            FileInputStream is = new FileInputStream("/proc/meminfo");
            int len = is.read(buffer);
            is.close();
            final int BUFLEN = buffer.length;
            int count = 0;
            for (int i=0; i<len && count < 2; i++) {
                if (mInfoReader.matchText(buffer, i, "MemFree")) {
                    // private final String STR_MEMFREE = "MemFree";
                    // i += STR_MEMFREE.length();
                    i += 7;
                    mMemFree = mInfoReader.extractMemValue(buffer, i);
                    count++;
                } else if (mInfoReader.matchText(buffer, i, "Cached")) {
                    i += 6;
                    mCached = mInfoReader.extractMemValue(buffer, i);
                    count++;
                }

                while (i < BUFLEN && buffer[i] != '\n') {
                    i++;
                }
            }

            mFreeMem = mMemFree + mCached;
        } catch (java.io.FileNotFoundException e) {
        } catch (java.io.IOException e) {
        } finally {
        }
    }

    public void readPageInfo() {
        byte[] buffer = new byte[1024];
        int count = 0;
        mPageFault = 0;
        mPageMajorFault = 0;
        int start = 0, end = 0;
        try {
            FileInputStream is = new FileInputStream("/proc/vmstat");
            int len = is.read( buffer);
            is.close();
            final int BUFLEN =  buffer.length;

            for(int i=0; i<BUFLEN && count<2; i++){
                if ( mInfoReader.matchText(buffer, i, "pgfault")) {
                    // private final String STR_MEMFREE = "MemFree";
                    // i += STR_MEMFREE.length();
                    i += 7;
                    mPageFault =  mInfoReader.extractMemValue(buffer, i);
                    count++;
                } else if ( mInfoReader.matchText(buffer, i, "pgmajfault")) {
                    i += 10;
                    mPageMajorFault =  mInfoReader.extractMemValue(buffer, i);
                    count++;
                }

                while (i < BUFLEN &&  buffer[i] != '\n') {
                    i++;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String getXoThermPath() {
        Preference pref = Preference.getInstance(InfoActivity.mContext, Preference.PREF_SETTINGS);

        String pathXoTherm = "";
        if(Build.PRODUCT.contains("g3")){
            pathXoTherm = pref.get("XoThermPath", IQDV.XO_THERM_PATH_G3);
        } else if(Build.PRODUCT.contains("tiger6")){
            pathXoTherm = pref.get("XoThermPath", IQDV.XO_THERM_PATH_TIGER6);
        } else if(Build.PRODUCT.contains("z2")){
            pathXoTherm = pref.get("XoThermPath", IQDV.XO_THERM_PATH_Z2);
        } else if (Build.PRODUCT.contains("p1")){
            pathXoTherm = pref.get("XoThermPath", IQDV.XO_THERM_PATH_P1);
        }
        else {
            pathXoTherm = pref.get("XoThermPath","");
        }
        return pathXoTherm;
    }

    public void readThermalInfo() {
        byte[] buffer = new byte[1024];
        int count = 0;
        mXoTherm = 0;
        int start = 0, end = 0;

        Preference pref = Preference.getInstance(InfoActivity.mContext, Preference.PREF_SETTINGS);


        String pathXoTherm = getXoThermPath();

        try {
            FileInputStream is = new FileInputStream(pathXoTherm);
            int len = is.read(buffer);
            is.close();
            final int BUFLEN = buffer.length;

            for(int i=0; i<BUFLEN && count<1; i++){
                if(buffer[i] == ':')
                    start = i+1;
                else if('0' <= buffer[i] && buffer[i] <= '9')
                    end = i+1;
                else if(buffer[i] == ' ')
                    count++;
            }

            String str = new String(buffer, start, end-start);

            mXoTherm = Integer.parseInt(str);

        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public ArrayList<String> getFileNames(File folder, String keyword){
        ArrayList<String> fileList = new ArrayList<String>();
        File[] files = folder.listFiles();
        if(files != null) {
            for (File fileEntry : files) {
                if (!fileEntry.isDirectory()) {
                    if (fileEntry.getName().contains(keyword)) {
                        fileList.add(fileEntry.getAbsolutePath());
                    }
                }
            }
            return fileList;
        }
        return null;
    }

    public void readLMKInfo(){
        ArrayList<String> kernelLogFileNameList = null;
        String rLine = null;
        mLMK = 0;

        File loggerPath = new File("/data/logger");
        kernelLogFileNameList = getFileNames(loggerPath, "kernel");
        if(kernelLogFileNameList == null) {
            mLMK = -1;
            mErrorMessage += "LMK : Please check out whether device is permissive mode.";
            return;
        }

        for(String logFileName : kernelLogFileNameList){
            File logFile = new File(logFileName);

            try {
                BufferedReader reader = new BufferedReader(new FileReader(logFile));

                while( (rLine = reader.readLine()) != null ) {
                    if(rLine.contains("lowmemorykiller")) {
                        if(rLine.contains("Killing")){
                            mLMK++;
                        }
                    }
                }

                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();

                if( e.getMessage().contains("EACCES")){
                    mLMK = -1;
                    mErrorMessage += "LMK : Can't count LMK because of Permission!";
                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TimeUtils.getDateTime(mTimeStamp) + "\n");
        sb.append("MemFree:" + mMemFree + ", Cached: " + mCached + "\n");
        sb.append("FreeMem:" + mFreeMem + "\n");
        sb.append("XO_Therm:" + mXoTherm + "\n");
        if(mLMK >= 0)
            sb.append("LMK:" + mLMK);
        else
            sb.append(mErrorMessage);

        return sb.toString();
    }

    public String toString(DeviceInfo prev){

        if(prev.getTimeStamp() == 0){
            return toString();
        }

        String dTime = TimeUtils.getTimeUnit(this.mTimeStamp - prev.mTimeStamp);
        int dMemFree = this.mMemFree - prev.mMemFree;
        int dCached = this.mCached - prev.mCached;
        int dFreeMem = this.mFreeMem - prev.mFreeMem;
        int dXoTherm = this.mXoTherm - prev.mXoTherm;
        int dLMK = this.mLMK - prev.mLMK;

        StringBuilder sb = new StringBuilder();
        sb.append(TimeUtils.getDateTime(mTimeStamp) + "(" + dTime + ")\n");
        sb.append("MemFree:" + mMemFree + "(" + dMemFree + "), Cached:" + mCached + "(" + dCached + ")\n");
        sb.append("FreeMem:" + mFreeMem + "(" + dFreeMem + ")\n");
        sb.append("XO_THERM:" + mXoTherm + "(" + dXoTherm + ")\n");
        if(mLMK >= 0)
            sb.append("LMK:" + mLMK + "(" + dLMK + ")");
        else
            sb.append(mErrorMessage);

        return sb.toString();
    }

    public DeviceInfoDiff getDiff(DeviceInfo prev) {

        DeviceInfoDiff diff = new DeviceInfoDiff();

        diff.dTimeStamp = this.mTimeStamp - prev.mTimeStamp;
        diff.dMemFree = this.mMemFree - prev.mMemFree;
        diff.dCached = this.mCached - prev.mCached;
        diff.dFreeMem = this.mFreeMem - prev.mFreeMem;
        diff.dXoTherm = this.mXoTherm - prev.mXoTherm;
        diff.dLMK = this.mLMK - prev.mLMK;

        return diff;
    }

}
