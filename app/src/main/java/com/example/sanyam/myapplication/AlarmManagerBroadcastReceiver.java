package com.example.sanyam.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Created by Sanyam Jain & Anisha Lunawat on 5/11/15.
 */
//receiver of intent & requests to start alarm
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
    private static final int PERIOD = 30 * 1000;

    public static void setAlarm(Context context) {
        //when alarm goes off this intent is broadcasted by the system
        Intent i = new Intent(context, AlarmManagerHandler.class);

        //helps in creating intent that must be fired at some later time
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        //schedule a repeating alarm; they are inexact
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.setRepeating(
                AlarmManager.ELAPSED_REALTIME,              //type of time(UTC, elapsed) & wake mobile or not
                SystemClock.elapsedRealtime() + PERIOD,     //trigger at Milliseconds; time at which alarm should go off
                PERIOD,                                     //interval in Milliseconds between subsequent repeat of alarm
                pi                                          //action to perform when alarm goes off pending intent
        );
    }

    //this is called when broadcast receiver is receiving an intent
    //should not be called on main thread!
    @Override
    public void onReceive(Context context, Intent intent) {
        setAlarm(context);
    }
}
