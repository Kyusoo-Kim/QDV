package com.lge.qdv;

import android.app.Activity;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.lge.qdv.preference.Preference;
import com.lge.qdv.util.DeviceInfo;


public class SettingsActivity extends Activity {

    EditText mEtXoThermPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getView();
        loadSettings();
    }

    public void getView(){
        mEtXoThermPath = (EditText) findViewById(R.id.etXO_THERMPath);
    }

    public void loadSettings(){
        mEtXoThermPath.setText(DeviceInfo.getXoThermPath());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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

    public void mOnClickSaveSettings(View v) {
        String node = mEtXoThermPath.getText().toString();

        Preference pref = Preference.getInstance(this, Preference.PREF_SETTINGS);
        pref.put("XoThermPath", node);
        finish();
    }

    public void mOnClickCancelSettings(View v) {
        finish();
    }
}
