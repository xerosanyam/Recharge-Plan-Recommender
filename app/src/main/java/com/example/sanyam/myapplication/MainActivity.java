package com.example.sanyam.myapplication;


import android.content.Context;
import android.content.SharedPreferences;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ED send = new ED();
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

    //method for fetching data from server in json format starting from initial Id
    private String[] fetchJsonData(long initialId) {
        String json[] = new String[2];

        DataUsageDatabaseHandler db = new DataUsageDatabaseHandler(this);
        Cursor cursor = db.getDataCursor();
        Log.e("Pos of cursor:", String.valueOf((int) initialId));
        if (cursor.getCount() <= (int) initialId) {
            Log.e("Pos of cursor <= initId", String.valueOf(cursor.getCount()));
            return null;
        }

        JSONObject mainObj = new JSONObject();

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();

        try {
            JSONObject data;
            JSONArray dataArray = new JSONArray();

            cursor.moveToPosition((int) initialId);

            mainObj.put("id", cursor.getLong(0));
            mainObj.put("IMEI", imei);
            do {
                data = new JSONObject();
                Log.e("id in SQL", String.valueOf(cursor.getLong(0)));
                Log.e("txwifi in SQL", String.valueOf(cursor.getLong(1)));
                Log.e("rxwifi in SQL", String.valueOf(cursor.getLong(2)));
                Log.e("txCell in SQL", String.valueOf(cursor.getLong(3)));
                Log.e("rxCell in SQL", String.valueOf(cursor.getLong(4)));
                data.put("txwifi", cursor.getLong(1));
                data.put("rxwifi", cursor.getLong(2));
                data.put("txcell", cursor.getLong(3));
                data.put("rxcell", cursor.getLong(4));
                data.put("date", DataUsageDatabaseHandler.toDate(cursor.getString(5)));
                dataArray.put(data);
//                    data.remove("txwifi");data.remove("rxwifi");data.remove("txcell");data.remove("rxcell");data.remove("date");
            } while (cursor.moveToNext());
            mainObj.put("data", dataArray);
            json[0] = mainObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("json data", json[0]);
        cursor.moveToLast();
        json[1] = String.valueOf(cursor.getInt(0));
        return json;
    }

    //method for deleting records from sql 'if required'
    public void deleteRecords() {
        DataUsageDatabaseHandler db = new DataUsageDatabaseHandler(this);
        db.deleteRecords();
    }

    //this works on button press
    public void refreshList(View view) {
//        new DownloadFilesTask().execute();
        getApplicationContext().getSharedPreferences("MYPREF", 0).edit().clear().commit();
    }

    //this works on button press
    public void onSubmit(View view) {
        new sendUsageTask().execute();
//        printToLog();
    }

    //to print long json to log
    private void printToLog() {
        String[] json;
        json = fetchJsonData(1);
        if (json == null) {
            Log.e("No entry made", "sqlite empty");
        } else {
            int length = json[0].length();
            for (int i = 0; i < length; i += 1024) {
                if (i + 1024 < length)
                    Log.d("JSON OUTPUT", json[0].substring(i, i + 1024));
                else
                    Log.d("JSON OUTPUT", json[0].substring(i, length));
            }
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
            Log.e("Sending data to server", "sending");
            SharedPreferences settings = getSharedPreferences("MYPREF", 0);
            SharedPreferences.Editor editor = settings.edit();

            //clear shared preferences
            long initial = settings.getLong("lastId", 0);
            Log.e("last id is: ", String.valueOf(initial));
            String[] json = fetchJsonData(initial);
            if (json == null) {
                Log.e("sendUsagetask:", "sqlite empty");
            } else {
                Log.e("Date got from sql is: ", json[0]);
                URL url;
                String response;
                try {
                    url = new URL("http://192.168.14.125:8888/sync_data");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    OutputStream outputStream = conn.getOutputStream();
                    String encrypt = ED.encrypt(json[0], "qazxswedc");
                    outputStream.write(encrypt.getBytes("UTF-8"));
                    outputStream.write(json[0].getBytes("UTF-8"));
                    Log.e("Sent data to server", "sent");
                    outputStream.flush();
                    outputStream.close();
                    Log.e("connection closed", "closed by client");

                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    Log.e("Response from server", result.toString());
                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        editor.putLong("lastId", Long.parseLong(result.toString()));
                        Log.e("Cursor pos by server:", result.toString());
                        editor.commit();
                    }
                    Log.e("response code", String.valueOf(responseCode));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}