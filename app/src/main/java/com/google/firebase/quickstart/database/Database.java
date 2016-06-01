package com.google.firebase.quickstart.database;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class Database extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
