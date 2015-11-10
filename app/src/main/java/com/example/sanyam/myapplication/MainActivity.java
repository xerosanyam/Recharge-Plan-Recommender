package com.example.sanyam.myapplication;


import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ED send;
    ListView mListView;
    private List<String> feedList = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AlarmManagerBroadcastReceiver.setAlarm(this);

        //populating listview on first run
        feedList = fetchData();
        ArrayAdapter<String> usageAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.listitemusage,
                R.id.listView_textView,
                feedList
        );
        mListView = (ListView) findViewById(R.id.listView_usage);
        mListView.setAdapter(usageAdapter);

        //setting up refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new DownloadFilesTask().execute();          //creates thread to get data from sql & populate listView
            }
        });
    }

    //puts value in listView for display
    private void updateList() {
        ArrayAdapter mAdapter = new ArrayAdapter(MainActivity.this, R.layout.listitemusage, feedList);
        mListView.setAdapter(mAdapter);

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    //method for fetching data from sql in list format
    private List<String> fetchData() {
        DataUsageDatabaseHandler db = new DataUsageDatabaseHandler(this);
        Log.e("Reading from DB :", "Reading all usage");
        List<DataUsage> usages = db.getUsageList();

        //Put fetched data from sql in a list
        List<String> usageArray = new ArrayList<>();
        for (DataUsage du : usages) {
            long newtxWifiBytes = du.getTxWifiBytes();
            long newrxWifiBytes = du.getRxWifiBytes();
            long newtxCellBytes = du.getTxCellBytes();
            long newrxCellBytes = du.getRxCellBytes();
            Date date = du.getDate();
            usageArray.add(newtxWifiBytes + " " + newrxWifiBytes + " " + newtxCellBytes + " " + newrxCellBytes + " " + date);
        }
        Collections.reverse(usageArray);
        return usageArray;
    }

    //method for fetching data from server in json format
    private String fetchJsonData() {
        String json = "";
        DataUsageDatabaseHandler db = new DataUsageDatabaseHandler(this);
        Cursor cursor = db.getDataCursor();
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
                    jobj.put("date", DataUsageDatabaseHandler.toDate(cursor.getString(5)));
                    arr.put(jobj);
                } while (cursor.moveToNext());
            }
            json = arr.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    //method for deleting records from sql 'if required'
    public void deleteRecords() {
        DataUsageDatabaseHandler db = new DataUsageDatabaseHandler(this);
        db.deleteRecords();
    }

    //this works on button press
    public void refreshList(View view) {
        new DownloadFilesTask().execute();
    }

    //this works on button press
    public void onSubmit(View view) {
        new sendUsageTask().execute();
//        printToLog();
    }

    //to print long json to log
    private void printToLog() {
        String json = fetchJsonData();
        int length = json.length();
        for (int i = 0; i < length; i += 1024) {
            if (i + 1024 < length)
                Log.d("JSON OUTPUT", json.substring(i, i + 1024));
            else
                Log.d("JSON OUTPUT", json.substring(i, length));
        }
    }

    //thread for getting data from sql & populating listview
    private class DownloadFilesTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(Void result) {
            updateList();
        }

        @Override
        protected Void doInBackground(String... params) {
            feedList = fetchData();
            return null;
        }
    }

    //thread for sending data to server
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
//                StringEntity se = new StringEntity(json);
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
                //ok
            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
                }
            return null;
        }
    }
}
