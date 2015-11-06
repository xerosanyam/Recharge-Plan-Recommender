package com.example.sanyam.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ArrayAdapter<String> usageAdapter;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AlarmManagerBroadcastReceiver.setAlarm(this);

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

        fetchData();
//        fetchJsonData();
    }

    private String fetchJsonData() {
        String json = "";
        DataUsageDatabaseHandler db = new DataUsageDatabaseHandler(this);
        Cursor cursor = db.getAllData();
        JSONObject jobj;
        JSONArray arr = new JSONArray();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        try {
            if (cursor.moveToFirst()) {
                do {
                    jobj = new JSONObject();
                    jobj.put("IMEI", imei);
                    jobj.put("txwifi", cursor.getInt(1));
                    jobj.put("rxwifi", cursor.getInt(2));
                    jobj.put("txcell", cursor.getInt(3));
                    jobj.put("rxcell", cursor.getInt(4));
                    String a = cursor.getString(5) + "T" + cursor.getString(6) + "+05:30";
                    jobj.put("date", a);
                    arr.put(jobj);
                    //jobj.put("IMEI","1234");
                } while (cursor.moveToNext());
            }
            json = arr.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
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
        Collections.reverse(usageArray);
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

    public void refreshList(View view) {
        fetchData();
    }

    public void onSubmit(View view) {
        Log.e("beforeSubmit", "let's see what happens");
        sendUsageTask ut = new sendUsageTask();
        ut.execute();
        Log.e("afterSubmit", "let's see what happens");
    }

    public class sendUsageTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            Log.e("in back", "bg");
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {

                URL url = new URL("http://192.168.43.46:8888/sync_data");
                Log.e("connecting..", "building connection");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(15000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);


                OutputStream outputStream = urlConnection.getOutputStream();
                String json = fetchJsonData();
                Log.e("json", json);
                outputStream.write(json.getBytes("UTF8"));
                outputStream.flush();
                outputStream.close();
                Log.e("connection done", "built connection");
                int responseCode = urlConnection.getResponseCode();
                Log.e("response code", String.valueOf(responseCode));
                Log.e("connection done", "no response ?");
            } catch (Exception e) {
                Log.e("LOG_TAG", "Error in getting data ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }
}
