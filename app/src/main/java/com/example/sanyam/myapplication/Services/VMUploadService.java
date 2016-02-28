package com.example.sanyam.myapplication.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.sanyam.myapplication.Controller.DatabaseHandler;
import com.example.sanyam.myapplication.Controller.ED;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sanyam Jain & Anisha Lunawat on 14/2/16.
 */
public class VMUploadService extends IntentService {

    public VMUploadService() {
        super("ScheduledService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Getting Wake Lock");

        //Acquire the lock
        wl.acquire();
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

                url = new URL("http://52.32.44.194:8888/sync_data");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                OutputStream outputStream = conn.getOutputStream();
                String encrypt = ED.encrypt(json[0], "qazxswedc");
                outputStream.write(encrypt.getBytes("UTF-8"));
                Log.e("json", json[0]);
                //outputStream.write(json[0].getBytes("UTF-8"));
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
            wl.release();
        }
    }

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
            mainObj.put("number", "8951262709");
            mainObj.put("operator", "airtel");
            do {
                data = new JSONObject();
                Log.e("id in SQL", String.valueOf(cursor.getLong(0)));
                Log.e("txwifi in SQL", String.valueOf(cursor.getLong(1) / 1024));
                Log.e("rxwifi in SQL", String.valueOf(cursor.getLong(2) / 1024));
                Log.e("txCell in SQL", String.valueOf(cursor.getLong(3) / 1024));
                Log.e("rxCell in SQL", String.valueOf(cursor.getLong(4) / 1024));
                data.put("txwifi", cursor.getLong(1));
                data.put("rxwifi", cursor.getLong(2));
                data.put("txcell", cursor.getLong(3));
                data.put("rxcell", cursor.getLong(4));
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
}
