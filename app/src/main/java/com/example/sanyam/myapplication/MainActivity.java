package com.example.sanyam.myapplication;


import android.content.Context;
import android.content.Intent;
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

import com.example.sanyam.myapplication.Controller.DatabaseHandler;
import com.example.sanyam.myapplication.Model.Data;
import com.example.sanyam.myapplication.Services.TrafficService;
import com.example.sanyam.myapplication.Services.VMUploadService;
import com.firebase.client.Firebase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    String my_num, my_operator;
    @Bind(R.id.listView_usage)
    ListView mListView;
    private List<String> feedList = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        my_num = "8951262709";
        my_operator = "Airtel";

        feedList = fetchData();
        ArrayAdapter<String> usageAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.listitemusage,
                R.id.listView_textView,
                feedList
        );

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

    @Override
    protected void onStart() {
        super.onStart();
        this.startService(new Intent(this, TrafficService.class));
        this.startService(new Intent(this, VMUploadService.class));
        pushToFirebase(this);
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
    //puts value in listView for display
    private void updateList() {
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.listitemusage, feedList);
        mListView.setAdapter(mAdapter);

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    //method for fetching data from sql in list format
    private List<String> fetchData() {
        DatabaseHandler db = new DatabaseHandler(this);
        Log.e("Reading from DB :", "Reading all usage");
        List<Data> usages = db.getUsageList();

        //Put fetched data from sql in a list
        List<String> usageArray = new ArrayList<>();
        for (Data du : usages) {
            long newtxWifiBytes = du.getTxWifiBytes();
            long newrxWifiBytes = du.getRxWifiBytes();
            long newtxCellBytes = du.getTxCellBytes();
            long newrxCellBytes = du.getRxCellBytes();
            String date = du.getDate();
            usageArray.add(newtxWifiBytes + " " + newrxWifiBytes + " " + newtxCellBytes + " " + newrxCellBytes + " " + date);
        }
        Collections.reverse(usageArray);
        return usageArray;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);

    }

    //method for fetching data from server in json format starting from initial Id
    private String[] fetchJsonData(long initialId) {
        String json[] = new String[2];

        DatabaseHandler db = new DatabaseHandler(this);
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
            mainObj.put("number", my_num);
            mainObj.put("operator", my_operator);
            do {
                data = new JSONObject();
                Log.e("id in SQL", String.valueOf(cursor.getLong(0)));
                Log.e("txwifi in SQL", String.valueOf(cursor.getLong(1) / 1024));
                Log.e("rxwifi in SQL", String.valueOf(cursor.getLong(2) / 1024));
                Log.e("txCell in SQL", String.valueOf(cursor.getLong(3) / 1024));
                Log.e("rxCell in SQL", String.valueOf(cursor.getLong(4) / 1024));
                data.put("txwifi", cursor.getLong(1) / 1024);
                data.put("rxwifi", cursor.getLong(2) / 1024);
                data.put("txcell", cursor.getLong(3) / 1024);
                data.put("rxcell", cursor.getLong(4) / 1024);
                data.put("date", DatabaseHandler.toDate(cursor.getString(5)));
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
        DatabaseHandler db = new DatabaseHandler(this);
        db.deleteRecords();
    }

    public void onLogout(View view) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("LoggedIn?", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        Log.e("Logging Out", "Logged Out");
        // Log.d(TAG, "Now log out and start the activity login");
        Intent i = new Intent(MainActivity.this, LoginForm.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
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
}