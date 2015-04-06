package com.lge.qdv;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lge.qdv.preference.Preference;


public class MainActivity extends Activity {

    private static String mPassword = "";

    private static boolean mValid;

    private RelativeLayout mRlMain;
    private Button btnStart;
    private Button btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRlMain = (RelativeLayout)findViewById(R.id.rlMain);
        btnStart = (Button)findViewById(R.id.btnStart);
        btnStop = (Button)findViewById(R.id.btnCancel);

        Preference pref = Preference.getInstance(this, Preference.PREF_SETTINGS);
        boolean auth = pref.get("Auth", false);

        if(!auth) {
            mRlMain.setVisibility(View.GONE);
            checkPassword();
        }
    }

    public void checkPassword() {
        Preference pref = Preference.getInstance(MainActivity.this, Preference.PREF_PASSWORD);
        mPassword = pref.get("Password", "");

        final LinearLayout linear = (LinearLayout)View.inflate(this, R.layout.password, null);


        if (Preference.PREF_PASSWORD.equals(mPassword)) {
            mValid = true;
        } else {
            AlertDialog.Builder builder =
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Authentication")
                    .setMessage("Please input password")
                    .setView(linear)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            EditText etPassword = (EditText) linear.findViewById(R.id.etPassword);
                            String pw = etPassword.getText().toString();

                            if (Preference.PREF_PASSWORD.equals(pw)) {
                                Preference pref = Preference.getInstance(MainActivity.this, Preference.PREF_PASSWORD);
                                pref.put("Password", pw);
                                mValid = true;
                                mRlMain.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                checkPassword();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    });

            builder.setCancelable(false);
            builder.show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void mOnClickStart(View v){

        Preference pref = Preference.getInstance(this, Preference.PREF_SETTINGS);
        pref.put("Auth", true);

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, InfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent content = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(MainActivity.this)
                .setTicker("Device State")
                .setContentTitle("Device State")
                .setContentText("P1 3Team 1Part")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_piechart))
                .setContentIntent(content)
                .setOngoing(true)
                .build();

        notificationManager.notify(IQDV.TAG, IQDV.ID_NOTIFICATION, notification);

        startActivity(intent);

        finish();
    }

    public void mOnClickCancel(View v){
        Preference pref = Preference.getInstance(this, Preference.PREF_SETTINGS);
        pref.put("Auth", false);

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(IQDV.TAG, IQDV.ID_NOTIFICATION);
        finish();
    }

    protected void onPause() {
        super.onPause();

        Preference pref = Preference.getInstance(MainActivity.this, Preference.PREF_PASSWORD);
        pref.put("Password", "");
    }
}
