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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ArrayAdapter<String> usageAdapter;
    ED send;
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


            InputStream inputStream = null;
            String result = "";
            try {

                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost("http://192.168.43.46:8888/sync_data");

                String json = fetchJsonData();
                String encrypt = ED.encrypt(json, "qazxswedc");

                StringEntity se = new StringEntity(encrypt);
                //StringEntity se = new StringEntity(json);
                // 6. set httpPost Entity
                httpPost.setEntity(se);
                Log.e("success", "sent");
                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                // 8. Execute POST request to the given URL
                HttpResponse httpResponse = httpclient.execute(httpPost);

                // 9. receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();


            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
                }
            return null;
        }
    }
}
