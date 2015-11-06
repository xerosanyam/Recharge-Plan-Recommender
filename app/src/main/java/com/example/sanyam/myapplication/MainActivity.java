package com.example.sanyam.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AlarmManagerBroadcastReceiver.setAlarm(this);

        //Shared preferences code
        SharedPreferences settings = getSharedPreferences("MYPREF", 0);
        int val = settings.getInt("my", 0);

        //Shared preferences code

//        DataUsageDatabaseHandler db=new DataUsageDatabaseHandler(this);
//        db.deleteRecords();

        //Code for listView
//        String[] fakeUsageArray={"Sanyam","Anisha","Abhinay","Ankit"};      //Fake data
//        List<String> dayUsage=new ArrayList<String>(
//                Arrays.asList(fakeUsageArray)
//        );

//        ArrayAdapter<String> usageAdapter=new ArrayAdapter<String>(
//                getApplicationContext(),
//                R.layout.listitemusage,
//                R.id.listView_textView,
//                dayUsage
//        );

//        listView.setAdapter(usageAdapter);
        //code for listView Ends here

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.e("IMEI", telephonyManager.getDeviceId());
        fetchData();
    }

    private void fetchData() {
        //code for listView fetching data from Sql
        DataUsageDatabaseHandler db = new DataUsageDatabaseHandler(this);
        Log.e("Reading from DB :", "Reading all usage");
        List<DataUsage> usages;
        usages = db.getAllUsage();
        List<String> usageArray = new ArrayList<>();
        for (DataUsage du : usages) {
            long newtxWifiBytes = du.getTxWifiBytes();
            long newrxWifiBytes = du.getRxWifiBytes();
            long newtxCellBytes = du.getTxCellBytes();
            long newrxCellBytes = du.getRxCellBytes();
            String date = du.getDate();
            String time = du.getTime();
            usageArray.add(newtxWifiBytes + " " + newrxWifiBytes + " " + newtxCellBytes + " " + newrxCellBytes + " " + date + " " + time);
        }
        ArrayAdapter<String> usageAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.listitemusage,
                R.id.listView_textView,
                usageArray
        );
        ListView listView = (ListView) findViewById(R.id.listView_usage);
        listView.setAdapter(usageAdapter);
        Log.d("Read :", "Reading complete");
        //code for listView fetching data from Sql ends here
    }
}
