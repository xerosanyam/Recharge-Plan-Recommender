package com.example.sanyam.myapplication.Receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.sanyam.myapplication.Controller.DatabaseHandler;
import com.example.sanyam.myapplication.R;
import com.example.sanyam.myapplication.Services.TrafficService;
import com.example.sanyam.myapplication.Services.VMUploadService;
import com.firebase.client.Firebase;

public class NetworkUpdate extends BroadcastReceiver {
    private static Intent i;
    private static PendingIntent pi;
    private static AlarmManager mgr;
    private int seconds = 900;                        //Change time here
    private int PERIOD = seconds * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        if (cm == null) {
            return;
        }
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            //Stop Alarm
            Log.e("Internt DOWN", "NOT Connected to the internet");
            Log.e("Internt DOWN", "Stopping Alarm");
            stopMyRepeatingAlarm(context);
            Log.e("Internt DOWN", "Stopped Alarm");
            return;
        }
        if (ni.isConnected() && ni.isAvailable()) {
            //Start RepeatingAlarm      //Push To Server Last Records
            Log.e("Internt Up", "Connected to the internet");

            Log.e("Internt UP", "Starting Alarm");
            startMyRepeatingAlarm(context);
            Log.e("Internt UP", "Started Alarm");

            Log.e("Internt UP", "Pushing Data to Server");
            pushToFirebase(context);
//            pushToOwnServer(context);
            Log.e("Internet UP", "Pushed Data to Server");
        }
    }

    public void pushToFirebase(Context context) {
        Firebase myFirebaseRef = new Firebase(context.getString(R.string.FirebaseURL));
        DatabaseHandler db = null;
        try {
            db = new DatabaseHandler(context);
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();
            myFirebaseRef.child(imei).child("Usage").setValue(db.getUsageList());
        } finally {
            if (db != null)
                db.close();
        }
    }

    private void pushToOwnServer(Context context) {
        Intent q = new Intent(context, VMUploadService.class);
        context.startService(q);
    }

    public void startMyRepeatingAlarm(Context context) {
        //when alarm goes off this intent is broadcasted by the system
        i = new Intent(context, TrafficService.class);

        //helps in creating intent that must be fired at some later time
        pi = PendingIntent.getService(context, 0, i, 0);

        //schedule a repeating alarm; they are inexact
        mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.setRepeating(
                AlarmManager.ELAPSED_REALTIME,              //type of time(UTC, elapsed) & wake mobile or not
                SystemClock.elapsedRealtime() + PERIOD,     //trigger at Milliseconds; time at which alarm should go off
                PERIOD,                                     //interval in Milliseconds between subsequent repeat of alarm
                pi                                          //action to perform when alarm goes off pending intent
        );
    }

    public void stopMyRepeatingAlarm(Context context) {
        i = new Intent(context, TrafficService.class);
        if (mgr != null) {
            pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            mgr.cancel(pi);
        }
        context.startService(i);
    }
}
