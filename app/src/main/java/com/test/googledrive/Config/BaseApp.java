package com.test.googledrive.Config;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ANDT on 10/6/2016.
 */

public class BaseApp extends Application {

    private static BaseApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static BaseApp getInstance() {
        if(instance == null) {
            instance = new BaseApp();
        }
        return instance;
    }

    public void setLogin(boolean check) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("google_drive", check);
        editor.commit();
    }

    public boolean getLogin() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean check = sharedPreferences.getBoolean("google_drive", false);
        return check;
    }

    public void setDataSetting(String data) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("data_setting", data);
        editor.commit();
    }

    public String getDataSetting() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String data = sharedPreferences.getString("data_setting", "");
        return data;
    }

    public void setFirstUseApp(int data) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("first_use_app", data);
        editor.commit();
    }

    public int getFirstUseApp() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int data = sharedPreferences.getInt("first_use_app", 0);
        return data;
    }

}
