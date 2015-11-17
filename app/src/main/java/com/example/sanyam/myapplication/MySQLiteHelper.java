package com.example.sanyam.myapplication; /**
 * Created by DELL on 31-03-2015.
 */


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.IOException;
import java.sql.SQLException;

public class MySQLiteHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "operator_details";
    public Context mContext;
    SQLiteDatabase db;
    DataBaseHelper myDbHelper;
    Cursor cursor;

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        myDbHelper = new DataBaseHelper(mContext);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String eval(String user_num, String find) throws SQLException {
        String code1 = "", code2 = "", code3 = "", user_circle = "", user_operator = "", t = "";
        int count = 0, flag = 10;
        String[] COLUMNS = new String[]{"Circle", "Operator"};

        try {

            myDbHelper.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        try {
            myDbHelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }
        try {
            t = user_num.substring(0, 1);

            if (t.equalsIgnoreCase("0")) {
                code1 = user_num.substring(1, 4);
                flag = 1;
            } else if (t.equals("+")) {
                code1 = user_num.substring(3, 6);
                flag = 2;
            } else
                code1 = user_num.substring(0, 3);

            db = myDbHelper.getReadableDatabase();
            cursor =
                    db.query("operator", // a. table
                            COLUMNS, // b. column names
                            " _id = ?", // c. selections
                            new String[]{String.valueOf(code1)}, // d. selections args
                            null, // e. group by
                            null, // f. having
                            null, // g. order by
                            null);
            if (cursor.moveToFirst()) {
                if (find == "Circle") {
                    user_circle = cursor.getString(0);
                    return (user_circle);
                } else if (find == "Operator") {
                    user_operator = cursor.getString(1);
                    return (user_operator);
                }
            } else {
                if (flag == 1)
                    code2 = user_num.substring(1, 5);
                else if (flag == 2)
                    code2 = user_num.substring(3, 7);
                else {
                    //  Log.e("USER NUM", user_num);
                    code2 = user_num.substring(0, 4);

                }
                db = myDbHelper.getReadableDatabase();
                cursor = db.query("operator", // a. table
                        COLUMNS, // b. column names
                        " _id = ?", // c. selections
                        new String[]{String.valueOf(code2)}, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null);
                if (cursor.moveToFirst()) {
                    if (find == "Circle") {
                        user_circle = cursor.getString(0);
                        return (user_circle);
                    } else if (find == "Operator") {
                        user_operator = cursor.getString(1);
                        return (user_operator);
                    }
                } else {
                    if (flag == 1)
                        code3 = user_num.substring(1, 6);
                    else if (flag == 2)
                        code3 = user_num.substring(3, 8);
                    else
                        code3 = user_num.substring(0, 5);
                    db = myDbHelper.getReadableDatabase();
                    cursor = db.query("operator", // a. table
                            COLUMNS, // b. column names
                            " _id = ?", // c. selections
                            new String[]{String.valueOf(code3)}, // d. selections args
                            null, // e. group by
                            null, // f. having
                            null, // g. order by
                            null);
                    if (cursor.moveToFirst()) {
                        if (find == "Circle") {
                            user_circle = cursor.getString(0);
                            return (user_circle);
                        } else if (find == "Operator") {
                            user_operator = cursor.getString(1);
                            return (user_operator);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                myDbHelper.close();
                cursor.close();
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String[] getDropDowndata(String type) throws SQLException {
        int n = 0, m = 0;

        int i = 0;
        Log.e("1:", "hello");
        try {

            myDbHelper.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }
        Log.e("2:", "hello");
        try {
            myDbHelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }
        Log.e("3:", "hello");
        try {
            db = myDbHelper.getReadableDatabase();
            Log.e("4:", type);
            cursor = db.rawQuery("SELECT distinct count(*) from ( SELECT distinct " + type + " from operator)", null);
            Log.e("5:", "hello");
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        if (type.equalsIgnoreCase("Circle")) ;
                        n = Integer.parseInt(cursor.getString(0));
                        if (type.equalsIgnoreCase("Operator")) ;
                        m = Integer.parseInt(cursor.getString(0));
                    } while (cursor.moveToNext());

                }
            }

            Log.e("m:", String.valueOf(m));
            String[] circles = new String[n + 1];
            String[] operators = new String[m + 1];
            cursor = db.rawQuery("SELECT distinct " + type + " from operator", null);
            Log.e("7:", "hello");
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        if (type.equalsIgnoreCase("Circle")) {
                            circles[i] = cursor.getString(0);
                            i++;
                        }
                        if (type.equalsIgnoreCase("Operator")) {
                            operators[i] = cursor.getString(0);
                            i++;
                        }
                    } while (cursor.moveToNext());

                }
            }
            if (type.equalsIgnoreCase("Circle"))
                return circles;
            if (type.equalsIgnoreCase("Operator"))
                return operators;


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                myDbHelper.close();
                cursor.close();
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
