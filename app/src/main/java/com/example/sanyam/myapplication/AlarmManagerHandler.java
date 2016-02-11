package com.example.sanyam.myapplication;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.os.PowerManager;
import android.util.Log;

import com.example.sanyam.myapplication.Model.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sanyam Jain & Anisha Lunawat on 8/11/15.
 */
//Called when alarm goes off
// Handle async request on demand (basically does some work on new thread)

public class AlarmManagerHandler extends IntentService {
    long txWifiBytes, rxWifiBytes, txCellBytes, rxCellBytes;
    long newtxWifiBytes, newrxWifiBytes, newtxCellBytes, newrxCellBytes;
    String date;

    Context context;

    public AlarmManagerHandler() {
        super("ScheduledService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(getClass().getSimpleName(), "Traffic Stats Api Called!");
        context = getApplicationContext();

        //Retrieving Shared preferences
        SharedPreferences settings = context.getSharedPreferences("MYPREF", 0);
        txWifiBytes = settings.getLong("txWifiBytes", 0);
        rxWifiBytes = settings.getLong("rxWifiBytes", 0);
        txCellBytes = settings.getLong("txCellBytes", 0);
        rxCellBytes = settings.getLong("rxCellBytes", 0);

        if (txWifiBytes < 0) txWifiBytes = 0;
        if (rxWifiBytes < 0) rxWifiBytes = 0;
        if (txCellBytes < 0) txCellBytes = 0;
        if (rxCellBytes < 0) rxCellBytes = 0;
        //gives control of power state of device. affects battery.
        //Do not acquire WakeLock unless you really need them, use the minimum levels
        //possible, and be sure to release them as soon as possible.
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Getting Wake Lock");

        //Acquire the lock
        wl.acquire();

        newtxWifiBytes = (TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes()) - txWifiBytes;
        txWifiBytes = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();

        newrxWifiBytes = (TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes()) - rxWifiBytes;
        rxWifiBytes = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();

        newtxCellBytes = (TrafficStats.getMobileTxBytes()) - txCellBytes;
        txCellBytes = (TrafficStats.getMobileTxBytes());

        newrxCellBytes = (TrafficStats.getMobileRxBytes()) - rxCellBytes;
        rxCellBytes = (TrafficStats.getMobileRxBytes());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date DATE = new Date();
        date = formatter.format(DATE);
        Log.d("txWifiBytes", String.valueOf(newtxWifiBytes));
        Log.d("rxWifiBytes", String.valueOf(newrxWifiBytes));
        Log.d("txCellBytes", String.valueOf(newtxCellBytes));
        Log.d("rxCellBytes", String.valueOf(newrxCellBytes));
        Log.d("date", date);

        DataUsageDatabaseHandler db = new DataUsageDatabaseHandler(context);
        Log.d("Inserting: ", "Using Data Usage Database Handler");
        db.addDataUsage(new Data(newtxWifiBytes, newrxWifiBytes, newtxCellBytes, newrxCellBytes, DATE));
        db.close();
        Log.d("Inserted: ", "Inserting in SQL complete!");

        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("txWifiBytes", txWifiBytes);
        editor.putLong("rxWifiBytes", rxWifiBytes);
        editor.putLong("txCellytes", txCellBytes);
        editor.putLong("rxCellBytes", rxCellBytes);
        editor.commit();
        editor.clear();
        wl.release();
        Log.d(getClass().getSimpleName(), "I ran!");
    }
}
