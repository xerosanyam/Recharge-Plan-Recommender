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
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
    public static final String ONE_TIME = "onetime";
    private static final int PERIOD = 30000;

    public static void setAlarm(Context context) {
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmManagerHandler.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + PERIOD, PERIOD, pi);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        setAlarm(context);
    }
}
