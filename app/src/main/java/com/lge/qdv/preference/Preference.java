package com.lge.qdv.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by kyusoo.kim on 2015-04-01.
 */
public class Preference {

    private static Preference preference;

    public static final String PREF_SETTINGS = "com.lge.qdv.settings";
    public static final String PREF_DEVICE_INFO = "com.lge.qdv.deviceinfo";
    public static final String PREF_PASSWORD = "lge123";

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public static Preference getInstance(Context c, String domain){
        if(preference == null){
            preference = new Preference(c, domain);
        } else{
            preference.mSharedPreferences = preference.mContext.getSharedPreferences(domain, Activity.MODE_PRIVATE);
            preference.mEditor = preference.mSharedPreferences.edit();
        }

        return preference;
    }

    public Preference(Context c, String domain){
        mContext = c;
        mSharedPreferences = mContext.getSharedPreferences(domain, Activity.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public void put(String key, String value) {
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public void put(String key, boolean value){
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    public void put(String key, int value){
        mEditor.putInt(key, value);
        mEditor.commit();
    }

    public void put(String key, long value){
        mEditor.putLong(key, value);
        mEditor.commit();
    }

    public String get(String key, String defaultValue){
        try{
            return mSharedPreferences.getString(key, defaultValue);
        }catch(Exception e){
            return defaultValue;
        }
    }

    public boolean get(String key, boolean defaultValue) {
        try{
            return mSharedPreferences.getBoolean(key, defaultValue);
        }catch(Exception e){
            return defaultValue;
        }
    }

    public int get(String key, int defaultValue) {
        try {
            return mSharedPreferences.getInt(key, defaultValue);
        }catch (Exception e) {
            return defaultValue;
        }
    }

    public long get(String key, long defaultValue) {
        try {
            return mSharedPreferences.getLong(key, defaultValue);
        }catch (Exception e) {
            return defaultValue;
        }
    }

    public Map getAll() {
        return mSharedPreferences.getAll();
    }
}
