package com.intkhabahmed.smartnotes.utils;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.intkhabahmed.smartnotes.R;
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

    public static void setSortCriteriaAndSortId(String criteria, int sortId) {
        sSharedPreferences.edit().putString(AppConstants.SORT_CRITERIA, criteria)
                .putInt(AppConstants.SORT_CRITERIA_ID, sortId)
                .apply();
    }

    public static String getSortCriteria() {
        return sSharedPreferences.getString(AppConstants.SORT_CRITERIA, AppConstants.COLUMN_DATE_CREATED_DESC);
    }

    public static int getSortId() {
        return sSharedPreferences.getInt(AppConstants.SORT_CRITERIA_ID, AppConstants.SORT_CRITERIA_ID_DEFAULT);
    }

    public static boolean getDarkThemeStatus() {
        return sSharedPreferences.getBoolean(sGlobalInstance.getString(R.string.dark_theme_key), false);
    }

    public static void setDataForWidgetId(String key, int value) {
        sSharedPreferences.edit().putInt(key, value).apply();
    }

    public static int getDataForWidgetId(String key) {
        return sSharedPreferences.getInt(key, 0);
    }

    public static void deleteDataForWidgetId(String key) {
        sSharedPreferences.edit().remove(key).apply();
    }
}
