package com.intkhabahmed.smartnotes.utils;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.intkhabahmed.smartnotes.database.NotesDatabase;

public class Global extends Application {
    private static Global sGlobalInstance;
    private static SharedPreferences sSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sGlobalInstance = (Global) getApplicationContext();
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public static Global getInstance() {
        return sGlobalInstance;
    }

    public static NotesDatabase getDbInstance() {
        return NotesDatabase.getInstance(sGlobalInstance);
    }
}
