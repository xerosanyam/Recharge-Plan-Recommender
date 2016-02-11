package com.example.sanyam.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.sanyam.myapplication.Model.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Sanyam Jain & Anisha Lunawat on 5/11/15.
 */
public class DataUsageDatabaseHandler extends SQLiteOpenHelper {

    public static final int DB_VERSION = 13;                             //DB Version
    public static final String DB_NAME = "UsageDB";                     //DB Name
    public static final String TABLE_DATA = "data";                     //Table Name
    public static final String KEY_ID = "id";                           //Column Name
    public static final String KEY_TXWIFIBYTES = "txWifiBytes";         //Column Name
    public static final String KEY_RXWIFIBYTES = "rxWifiBytes";         //Column Name
    public static final String KEY_TXCELLBYTES = "txCellBytes";         //Column Name
    public static final String KEY_RXCELLBYTES = "rxCellBytes";         //Column Name
    public static final String KEY_DATE = "date";                       //Column Name

    public DataUsageDatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static String toDate(String str) {
        SimpleDateFormat s = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date d;
        String date = null;
        try {
            d = s.parse(str);
            date = formatter.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date strToDate(String str) {
        SimpleDateFormat s = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        Date d = null;
        try {
            d = s.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    //create DB
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createDataTableQuery = "CREATE TABLE " + TABLE_DATA + "(" +
                KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_TXWIFIBYTES + " INTEGER, " +
                KEY_RXWIFIBYTES + " INTEGER, " +
                KEY_TXCELLBYTES + " INTEGER, " +
                KEY_RXCELLBYTES + " INTEGER, " +
                KEY_DATE + " TEXT)";
        db.execSQL(createDataTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        onCreate(db);
    }

    public void addDataUsage(Data dataUsage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TXWIFIBYTES, dataUsage.getTxWifiBytes());
        values.put(KEY_RXWIFIBYTES, dataUsage.getRxWifiBytes());
        values.put(KEY_TXCELLBYTES, dataUsage.getTxCellBytes());
        values.put(KEY_RXCELLBYTES, dataUsage.getRxCellBytes());
        values.put(KEY_DATE, String.valueOf(dataUsage.getDate()));
        db.insert(TABLE_DATA, null, values);
        db.close();
    }

    public List<Data> getUsageList() {
        List<Data> usageList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_DATA;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Data usage = new Data();
                usage.setTxWifiBytes(cursor.getInt(1));
                usage.setRxWifiBytes(cursor.getInt(2));
                usage.setTxCellBytes(cursor.getInt(3));
                usage.setRxCellBytes(cursor.getInt(4));
                usage.setDate(strToDate(cursor.getString(5)));
                usageList.add(usage);
            } while (cursor.moveToNext());
        }
        return usageList;
    }

    public Cursor getDataCursor() {
        String selectQuery = "SELECT * FROM " + TABLE_DATA;
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery(selectQuery, null);
    }

    public int getRecordsCount() {
        String countQuery = "SELECT * FROM " + TABLE_DATA;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();
    }

    public void deleteRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DATA, null, null);
    }
}
