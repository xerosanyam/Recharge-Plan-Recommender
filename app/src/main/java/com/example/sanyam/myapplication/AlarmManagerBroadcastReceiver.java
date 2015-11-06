package com.example.sanyam.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.os.PowerManager;
import android.util.Log;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by sanyam & anisha on 5/11/15.
 */
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    public static final String ONE_TIME = "onetime";
    static long txWifiBytes, rxWifiBytes, txCellBytes, rxCellBytes;
    static Context context1;
    long newtxWifiBytes, newrxWifiBytes, newtxCellBytes, newrxCellBytes;
    String date, time;

    public static void setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra(ONE_TIME, Boolean.FALSE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        //After 10 seconds
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60, pi);
        context1 = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Your_Tag");
        //Acquire the lock
        wl.acquire();
        //Shared PRefrences code; RETREIVING
        SharedPreferences settings = context.getSharedPreferences("MYPREF", 0);
        txWifiBytes = settings.getLong("txWifiBytes", 0);
        rxWifiBytes = settings.getLong("rxWifiBytes", 0);
        txCellBytes = settings.getLong("txCellBytes", 0);
        rxCellBytes = settings.getLong("rxCellBytes", 0);
        //Shared PRefrences code
//        Log.e("ny tx wifi:", String.valueOf(txWifiBytes));
//        Log.e("ny tx wifi:", String.valueOf(rxWifiBytes));
//        Log.e("ny tx wifi:", String.valueOf(txCellBytes));
//        Log.e("ny tx wifi:", String.valueOf(rxCellBytes));


        newtxWifiBytes = (TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes()) - txWifiBytes;
        txWifiBytes = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
//        Log.e("my tx wifi:", String.valueOf(txWifiBytes));

        newrxWifiBytes = (TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes()) - rxWifiBytes;
        rxWifiBytes = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
//        Log.e("my rx wifi:", String.valueOf(rxWifiBytes));

        newtxCellBytes = (TrafficStats.getMobileTxBytes()) - txCellBytes;
        txCellBytes = (TrafficStats.getMobileTxBytes());
//        Log.e("my tx cell:", String.valueOf(txCellBytes));

        newrxCellBytes = (TrafficStats.getMobileRxBytes()) - rxCellBytes;
        rxCellBytes = (TrafficStats.getMobileRxBytes());
//        Log.e("my rx cell:", String.valueOf(rxCellBytes));

        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date DATE = new Date();
        date = formatter.format(DATE);
        formatter = new SimpleDateFormat("HH:mm:ss");
        time = formatter.format(DATE);
        Log.d("txWifiBytes", String.valueOf(newtxWifiBytes));
        Log.d("rxWifiBytes", String.valueOf(newrxWifiBytes));
        Log.d("txCellBytes", String.valueOf(newtxCellBytes));
        Log.d("rxCellBytes", String.valueOf(newrxCellBytes));
        Log.d("date", date);
        Log.d("time", time);
        DataUsageDatabaseHandler db = new DataUsageDatabaseHandler(context1);
        Log.d("Insert: ", "Inserting ..");
        db.addDataUsage(new DataUsage(newtxWifiBytes, newrxWifiBytes, newtxCellBytes, newrxCellBytes, date, time));
        Log.d("Inserted: ", "Inserting complete..");

        Log.d("Reading :", "Reading all usage");
        List<DataUsage> usages;
        usages = db.getAllUsage();
        for (DataUsage du : usages) {
            long newtxWifiBytes = du.getTxWifiBytes();
            long newrxWifiBytes = du.getRxWifiBytes();
            long newtxCellBytes = du.getTxCellBytes();
            long newrxCellBytes = du.getRxCellBytes();
            String date = du.getDate();
            String time = du.getTime();
            Log.d("txWifiBytes ", String.valueOf(newtxWifiBytes));
            Log.d("rxWifiBytes ", String.valueOf(newrxWifiBytes));
            Log.d("txCellBytes ", String.valueOf(newtxCellBytes));
            Log.d("rxCellBytes ", String.valueOf(newrxCellBytes));
            Log.d("Date ", date);
            Log.d("Time ", time);
        }
        Log.d("Read :", "Reading complete");
        db.close();

        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("txWifiBytes", txWifiBytes);
        editor.putLong("rxWifiBytes", rxWifiBytes);
        editor.putLong("txCellytes", txCellBytes);
        editor.putLong("rxCellBytes", rxCellBytes);
        editor.commit();
        wl.release();
    }
}
