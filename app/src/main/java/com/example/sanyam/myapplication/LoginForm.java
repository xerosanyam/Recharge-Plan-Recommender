package com.example.sanyam.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.example.sanyam.myapplication.Model.User;
import com.firebase.client.Firebase;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginForm extends AppCompatActivity {
    @Bind(R.id.ageText)
    EditText age;
    @Bind(R.id.radioGroup)
    RadioGroup gender;
    @Bind(R.id.cityText)
    EditText city;
    @Bind(R.id.monthlyText)
    EditText income;
    @Bind(R.id.landlineText)
    EditText landline;
    @Bind(R.id.mobileText)
    EditText mobile;

    Firebase myFirebaseRef;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout);
        ButterKnife.bind(this);
        myFirebaseRef = new Firebase
                ("https://dau-data.firebaseio.com/");
        myFirebaseRef.keepSynced(true);                                             //Send data to user after app restarts
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferences = getApplicationContext().getSharedPreferences("LoggedIn?", 0);
        if (preferences.getInt("LoggedIn?", 0) == 1) {
            Intent openWelcome = new Intent(getApplicationContext(), MainActivity.class);
            super.onResume();
            startActivity(openWelcome);
        }
    }

    public void sendToServer(View view) {
        if (age.getText().toString().length() == 0 || city.getText().toString().length() == 0 || income.getText().toString().length() == 0 || landline.getText().toString().length() == 0 || mobile.getText().toString().length() == 0) {
            if (age.getText().toString().length() == 0) {
                age.setError("Please enter your Age!");
            }
            if (city.getText().toString().length() == 0) {
                city.setError("Please enter your City!");
            }
            if (income.getText().toString().length() == 0) {
                income.setError("Please enter your Income");
            }
            if (landline.getText().toString().length() == 0) {
                landline.setError("Please Enter your Landline Data Plan Limit");
            }
            if (mobile.getText().toString().length() == 0) {
                mobile.setError("Please Enter your Mobile Data Plan Limit");
            }
        } else {
            User u = new User();
            u.setAge(Integer.parseInt(age.getText().toString()));
            if (gender.getCheckedRadioButtonId() == R.id.male) {
                u.setGender('M');
            } else {
                u.setGender('F');
            }

            u.setCity(city.getText().toString());
            u.setIncome(Integer.parseInt(income.getText().toString()));
            u.setLandline(Integer.parseInt(landline.getText().toString()));
            u.setMobile(Integer.parseInt(mobile.getText().toString()));

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();

//            AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
//            Account[] list = manager.getAccounts();
//            String gmail = "TEST";
//
//            for(Account account: list)
//            {
//                if(account.type.equalsIgnoreCase("com.google"))
//                {
//                    gmail = account.name;
//                    break;
//                }
//            }
//            gmail=gmail.replace("@gmail.com","").replace(".","").replace("#","").replace("$","").replace("[","").replace("]","");
            myFirebaseRef.child(imei).child("Info").setValue(u);

            SharedPreferences.Editor editor = preferences.edit();             //Log in User
            editor.putInt("LoggedIn?", 1);
            editor.commit();

            Intent openWelcome = new Intent(getApplicationContext(), MainActivity.class);   //Open Welcome Activity
            startActivity(openWelcome);
        }
    }
}
