package com.example.sanyam.myapplication;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by Sanyam Jain & Anisha Lunawat on 11/2/16.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
    }
}
