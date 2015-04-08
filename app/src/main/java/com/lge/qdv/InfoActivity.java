package com.lge.qdv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.lge.qdv.preference.Preference;
import com.lge.qdv.util.DeviceInfo;
import com.lge.qdv.util.DeviceInfoDiff;
import com.lge.qdv.util.TimeUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class InfoActivity extends Activity {

    public static Context mContext;

    TextView mTvMemFreeCached;
    TextView mTvFreeMem;
    TextView mTvXoTherm;
    TextView mTvLMK;

    TextView mTvTimeInfo;
    TextView mTvErrorInfo;

    ProgressDialog mProgressDialog;

    DeviceInfo mDeviceInfo;
    DeviceInfo mPrevDeviceInfo;

    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case IQDV.MSG_ERROR:
                    mTvErrorInfo.setText(mDeviceInfo.getErrorMessage());
                    break;
                case IQDV.MSG_CLOSE_DIALOG:
                    mProgressDialog.dismiss();
                    break;
                case IQDV.MSG_UPDATE_DEVICE_INFO:
                    updateDeviceInfo();




                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        mContext = getApplicationContext();
        getView();
        getPrevDeviceInfo();
        getDeviceInfo();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getView() {
        mTvTimeInfo = (TextView)findViewById(R.id.tvTimeInfo);

        mTvMemFreeCached = (TextView)findViewById(R.id.tvMemFreeCached);
        mTvFreeMem = (TextView)findViewById(R.id.tvFreeMem);
        mTvXoTherm = (TextView)findViewById(R.id.tvXoTherm);
        mTvLMK = (TextView)findViewById(R.id.tvLMK);

        mTvErrorInfo = (TextView)findViewById(R.id.tvErrorInfo);
    }

    private void getPrevDeviceInfo(){
        Preference pref = Preference.getInstance(InfoActivity.this, Preference.PREF_DEVICE_INFO);

        long TimeStamp = pref.get("TimeStamp", 0L);
        int MemFree = pref.get("MemFree", 0);
        int Cached = pref.get("Cached", 0);
        int FreeMem = pref.get("FreeMem", 0);
        int XoTherm = pref.get("XO_THERM", 0);
        int LMK = pref.get("LMK", 0);

        mPrevDeviceInfo = new DeviceInfo(TimeStamp, MemFree, Cached, XoTherm, LMK);

    }

    private void getDeviceInfo(){
        mProgressDialog = mProgressDialog.show(InfoActivity.this, "Working", "Wait...");
        launchThreadDeviceInfo();
    }

    public void launchThreadDeviceInfo(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                mDeviceInfo = new DeviceInfo(mHandler);
                mHandler.sendEmptyMessage(IQDV.MSG_UPDATE_DEVICE_INFO);
                mHandler.sendEmptyMessage(IQDV.MSG_CLOSE_DIALOG);

                DeviceInfoDiff diff = mDeviceInfo.getDiff(mPrevDeviceInfo);
                Log.d(IQDV.TAG, mDeviceInfo.toString(mPrevDeviceInfo));
                saveInfo(diff);
            }
        });

        t.start();
    }

    private void updateDeviceInfo(){
        DeviceInfoDiff diff = mDeviceInfo.getDiff(mPrevDeviceInfo);
        String strTimeInfo = null, strMemFreeCached = null, strFreeMem = null, strXoTherm = null, strLMK = null;
        if(mPrevDeviceInfo.getTimeStamp() == 0){
            strTimeInfo = TimeUtils.getDateTime(mDeviceInfo.getTimeStamp());
            strMemFreeCached = "MemFree:" + mDeviceInfo.getMemFree()/1024 + ", Cached:" + mDeviceInfo.getCached()/1024;
            strFreeMem =  "Free Memory:" + (mDeviceInfo.getMemFree()/1024 + mDeviceInfo.getCached()/1024);
            strXoTherm = "XO_THERM:" + mDeviceInfo.getXoTherm();
            if(mDeviceInfo.getLMK() >= 0)
                strLMK = "LMK:" + mDeviceInfo.getLMK();

        } else {
            strTimeInfo = TimeUtils.getDateTime(mDeviceInfo.getTimeStamp()) + "(" + TimeUtils.getTimeUnit(diff.dTimeStamp) + ")";
            strMemFreeCached = "MemFree:" + mDeviceInfo.getMemFree()/1024 + "(" + diff.dMemFree/1024 + "), Cached:" + mDeviceInfo.getCached()/1024 + "(" + diff.dCached/1024 + ")";
            strFreeMem =  "Free Memory:" + (mDeviceInfo.getMemFree()/1024 + mDeviceInfo.getCached()/1024) +  "(" + diff.dFreeMem/1024 + ")";
            strXoTherm = "XO_THERM:" + mDeviceInfo.getXoTherm() + "(" + diff.dXoTherm + ")";
            if(mDeviceInfo.getLMK() >= 0)
                strLMK = "LMK:" + mDeviceInfo.getLMK() + "(" + diff.dLMK + ")";
        }

        mTvTimeInfo.setText(strTimeInfo);
        mTvMemFreeCached.setText(strMemFreeCached);
        mTvFreeMem.setText(strFreeMem);
        mTvXoTherm.setText(strXoTherm);

        if(strLMK != null)
            mTvLMK.setText(strLMK);
    }

    public void saveInfo(DeviceInfoDiff diff){
        File directory = new File(IQDV.QDV_DATA_STORE_DIRECTORY);

        if( !directory.exists() && !directory.isDirectory() ) {
            if(directory.mkdirs()){
                Log.d(IQDV.TAG, "Make directory : " + IQDV.QDV_DATA_STORE_DIRECTORY);
            }else{
                Log.e(IQDV.TAG, "Unable to create app dir!");
            }
        } else {
            //Log.d(IQDV.TAG, "Already exists");
        }

        PrintWriter out = null;
        try {
            FileWriter fw = new FileWriter(IQDV.QDV_DATA_FILE_PATH, true);
            out = new PrintWriter(new BufferedWriter(fw));

            String strTimeInfo = null, strMemFreeCached = null, strFreeMem = null, strXoTherm = null, strLMK = null;
            if(mPrevDeviceInfo.getTimeStamp() == 0){
                strTimeInfo = TimeUtils.getDateTime(mDeviceInfo.getTimeStamp());
                strMemFreeCached = "MemFree:" + mDeviceInfo.getMemFree()/1024 + ", Cached:" + mDeviceInfo.getCached()/1024;
                strFreeMem =  "Free Memory:" + (mDeviceInfo.getMemFree()/1024 + mDeviceInfo.getCached()/1024);
                strXoTherm = "XO_THERM:" + mDeviceInfo.getXoTherm();
                if(mDeviceInfo.getLMK() >= 0)
                    strLMK = "LMK:" + mDeviceInfo.getLMK();

            } else {
                strTimeInfo = TimeUtils.getDateTime(mDeviceInfo.getTimeStamp()) + "(" + TimeUtils.getTimeUnit(diff.dTimeStamp) + ")";
                strMemFreeCached = "MemFree:" + mDeviceInfo.getMemFree()/1024 + "(" + diff.dMemFree/1024 + "), Cached:" + mDeviceInfo.getCached()/1024 + "(" + diff.dCached/1024 + ")";
                strFreeMem =  "Free Memory:" + (mDeviceInfo.getMemFree()/1024 + mDeviceInfo.getCached()/1024) +  "(" + diff.dFreeMem/1024 + ")";
                strXoTherm = "XO_THERM:" + mDeviceInfo.getXoTherm() + "(" + diff.dXoTherm + ")";
                if(mDeviceInfo.getLMK() >= 0)
                    strLMK = "LMK:" + mDeviceInfo.getLMK() + "(" + diff.dLMK + ")";
            }

            out.println("#");
            out.println(strTimeInfo);
            out.println(strMemFreeCached);
            out.println(strFreeMem);
            out.println(strXoTherm);
            if(strLMK != null)
                out.println(strLMK);
            out.println("$");

        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if(out != null)
                out.close();
        }

        //Log.d(IQDV.TAG, Environment.getExternalStorageDirectory().toString());
    }


    protected void onPause() {
        super.onPause();

        Preference pref = Preference.getInstance(InfoActivity.this, Preference.PREF_DEVICE_INFO);

        pref.put("TimeStamp", mDeviceInfo.getTimeStamp());
        pref.put("MemFree", mDeviceInfo.getMemFree());
        pref.put("Cached", mDeviceInfo.getCached());
        pref.put("FreeMem", (mDeviceInfo.getMemFree() + mDeviceInfo.getCached()));
        pref.put("XO_THERM", mDeviceInfo.getXoTherm());
        pref.put("LMK", mDeviceInfo.getLMK());
    }
}
