package com.example.sanyam.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.sql.SQLException;

/**
 * Created by anisha on 16/11/15.
 */
public class Login extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, TextWatcher {
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    public String my_num;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    Button login;
    EditText e_my_num;
    String[] operators;
    MySQLiteHelper db;
    SharedPreferences sharedPref;
    Intent in;
    int number, period;
    private String my_operator = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        db = new MySQLiteHelper(this);
        e_my_num = (EditText) findViewById(R.id.my_num);
        e_my_num.addTextChangedListener(this);


        buildGoogleApiClient();
        Log.e("success", "oncreate");
        sharedPref = getSharedPreferences("data", MODE_PRIVATE);
        number = sharedPref.getInt("isLogged", 0);
        Log.e("logoutinlogin", String.valueOf(MainActivity.logout));
        if (MainActivity.logout == 1) {
            MainActivity.logout = 0;
            SharedPreferences.Editor prefEditor = sharedPref.edit();
            prefEditor.putInt("isLogged", 0);
            prefEditor.commit();
            Log.e("logoutinloop", String.valueOf(number));
        }
        Log.e("logoutoutloop", String.valueOf(number));

        try {
            Log.e("in", "Operator list");
            Spinner dropdown = (Spinner) findViewById(R.id.operator_list);
            operators = db.getDropDowndata("Operator");
            Log.e("after dropdown", "hello");
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, operators);
            dropdown.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (number == 1) {
            in = new Intent(Login.this, MainActivity.class);
            String my_number = null;
            Log.e("success", "second login");
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            my_number = prefs.getString("name", "No name defined");//"No name defined" is the default value.

            try {
                period = 0;
                my_operator = db.eval(my_number, "Operator");
                Log.e("Operator", my_operator);
                Log.e("period", String.valueOf(period));
                in.putExtra("my_num", my_number);
                in.putExtra("my_operator", my_operator);
            } catch (Exception e) {
                e.printStackTrace();
            }
            startActivity(in);
            Login.this.finish();
            System.exit(0);
        }
        Log.e("success", "first login");

        //paste here
        login = (Button) findViewById(R.id.login);


        login.setOnClickListener(this);


    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.e("Latitude", String.valueOf(mLastLocation.getLatitude()));
            Log.e("Latitude", String.valueOf(mLastLocation.getLongitude()));
        } else {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        Log.i("", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Intent i;
        my_num = e_my_num.getText().toString();

        i = new Intent(Login.this, MainActivity.class);


        if (my_num.equalsIgnoreCase(null) || my_num.length() < 10) {
            Toast.makeText(getBaseContext(), "Please enter a valid length number!!", Toast.LENGTH_LONG).show();
            SharedPreferences.Editor prefEditor = sharedPref.edit();
            prefEditor.putInt("isLogged", 0);
            prefEditor.commit();
        } else {
            try {
                my_operator = db.eval(my_num, "Operator");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (my_operator == null) {
                Toast.makeText(getBaseContext(), "Please enter a valid number!!", Toast.LENGTH_LONG).show();
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                prefEditor.putInt("isLogged", 0);
                prefEditor.commit();
            } else {
                if (number == 0) {
                    Log.e("success", "set shared preference");
                    //Open the login activity and set this so that next it value is 1 then this conditin will be false.
                    SharedPreferences.Editor prefEditor = sharedPref.edit();
                    prefEditor.putInt("isLogged", 1);
                    Log.e("Login", String.valueOf(number));
                    prefEditor.commit();
                }
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("name", my_num);
                editor.commit();

                Log.e("period", String.valueOf(period));
                i.putExtra("my_num", my_num);
                i.putExtra("my_operator", my_operator);

                startActivity(i);
                System.exit(0);
            }
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        int l = s.length();
        if (l != 0) {
            char c = s.toString().charAt(0);
            if (c == '0' && l == 11) {
                my_num = e_my_num.getText().toString();
                try {
                    //my_circle = db.eval(my_num, "Circle");
                    my_operator = db.eval(my_num, "Operator");
                    if (my_operator == " ") {
                        Toast.makeText(getBaseContext(), "Enter a valid number", Toast.LENGTH_LONG).show();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    Spinner dropdown = (Spinner) findViewById(R.id.operator_list);
                    operators = db.getDropDowndata("Operator");
                    operators[operators.length - 1] = my_operator;
                    String temp1 = operators[operators.length - 1];
                    operators[operators.length - 1] = operators[0];
                    operators[0] = temp1;
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, operators);
                    dropdown.setAdapter(adapter);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (c != '0' && l == 10) {
                my_num = e_my_num.getText().toString();
                try {
                    //my_circle = db.eval(my_num, "Circle");
                    my_operator = db.eval(my_num, "Operator");
                    if (my_operator == " ") {
                        Toast.makeText(getBaseContext(), "Enter a valid number", Toast.LENGTH_LONG).show();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
//                    dropdown = (Spinner)findViewById(R.id.circle_list);
//                    circles = db.getDropDowndata("Circle");
//                    circles[circles.length - 1] = my_circle;
//                    String temp = circles[circles.length - 1];
//                    circles[circles.length - 1] = circles[0];
//                    circles[0] = temp;
//                    adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, circles);
//                    dropdown.setAdapter(adapter1);

                    Spinner dropdown = (Spinner) findViewById(R.id.operator_list);
                    operators = db.getDropDowndata("Operator");
                    operators[operators.length - 1] = my_operator;
                    String temp1 = operators[operators.length - 1];
                    operators[operators.length - 1] = operators[0];
                    operators[0] = temp1;
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, operators);
                    dropdown.setAdapter(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

}


