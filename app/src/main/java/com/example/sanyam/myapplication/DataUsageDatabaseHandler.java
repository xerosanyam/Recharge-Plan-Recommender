package com.example.sanyam.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sanyam on 5/11/15.
 */
public class DataUsageDatabaseHandler extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;                             //DB Version
    public static final String DB_NAME = "UsageDB";                     //DB Name
    public static final String TABLE_DATA = "data";                     //Table Name
    public static final String KEY_ID = "id";                           //Column Name
    public static final String KEY_TXWIFIBYTES = "txWifiBytes";         //Column Name
    public static final String KEY_RXWIFIBYTES = "rxWifiBytes";         //Column Name
    public static final String KEY_TXCELLBYTES = "txCellBytes";         //Column Name
    public static final String KEY_RXCELLBYTES = "rxCellBytes";         //Column Name
    public static final String KEY_DATE = "date";                       //Column Name
    public static final String KEY_TIME = "time";                       //Column Name


    public DataUsageDatabaseHandler(Context context) {
            super(context,DB_NAME,null,DB_VERSION);;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createDataTableQuery = "CREATE TABLE " + TABLE_DATA + "(" +
                KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_TXWIFIBYTES + " INTEGER, " +
                KEY_RXWIFIBYTES + " INTEGER, " +
                KEY_TXCELLBYTES + " INTEGER, " +
                KEY_RXCELLBYTES + " INTEGER, " +
                KEY_DATE + " TEXT, " +
                KEY_TIME + " TEXT " + ")";
        db.execSQL(createDataTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_DATA);
        onCreate(db);
    }

    public void addDataUsage(DataUsage dataUsage){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(KEY_TXWIFIBYTES,dataUsage.getTxWifiBytes());
        values.put(KEY_RXWIFIBYTES,dataUsage.getRxWifiBytes());
        values.put(KEY_TXCELLBYTES,dataUsage.getTxCellBytes());
        values.put(KEY_RXCELLBYTES,dataUsage.getRxCellBytes());
        values.put(KEY_DATE,dataUsage.getDate());
        values.put(KEY_TIME,dataUsage.getTime());
        db.insert(TABLE_DATA, null, values);
        db.close();
    }
    public List<DataUsage> getAllUsage(){
        List<DataUsage> usageList=new ArrayList<DataUsage>();
        String selectQuery="SELECT * FROM " + TABLE_DATA;
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery(selectQuery,null);
        if(cursor.moveToFirst()){
            do{
                DataUsage usage=new DataUsage();
                usage.setTxWifiBytes(cursor.getInt(1));
                usage.setRxWifiBytes(cursor.getInt(2));
                usage.setTxCellBytes(cursor.getInt(3));
                usage.setRxCellBytes(cursor.getInt(4));
                usage.setDate(cursor.getString(5));
                usage.setTime(cursor.getString(6));
                usageList.add(usage);
            }while (cursor.moveToNext());
        }
        return usageList;
    }
    public int getRecordsCount(){
        String countQuery="SELECT * FROM " +TABLE_DATA;
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();
    }
    public void deleteRecords(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.delete(TABLE_DATA,null,null);
    }
}
