package com.example.sanyam.myapplication;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AlarmManagerBroadcastReceiver.setAlarm(this);
//        DataUsageDatabaseHandler db=new DataUsageDatabaseHandler(this);
//        db.deleteRecords();
    }
    public void submit(View view){
        EditText editName=(EditText)findViewById(R.id.getName);
        name=editName.getText().toString();
        System.out.print(name);
        TextView setName=(TextView)findViewById(R.id.setName);
        setName.setText(name);
    }
}
