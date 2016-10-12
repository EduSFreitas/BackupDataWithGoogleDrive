package com.test.googledrive.Setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

/**
 * Created by ANDT on 10/6/2016.
 */

public class SettingManager {
    private SharedPreferences pre;
    private static SettingManager instance;

    private SettingManager(Context context) {
        pre = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
    }

    public static SettingManager getInstance(Context mContext) {
        if (instance == null) {
            instance = new SettingManager(mContext);
        }
        return instance;
    }

    public void setShowFile(boolean value) {
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean("show_file", value);
        editor.commit();
    }

    public boolean getShowFile() {
        return pre.getBoolean("show_file", false);
    }

    public void setChangeDisplay(boolean value) {
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean("change_display", value);
        editor.commit();
    }

    public boolean getChangeDisplay() {
        return pre.getBoolean("change_display", false);
    }

    public void setAutoHide(boolean value) {
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean("action_hie", value);
        editor.commit();
    }

    public boolean getAutoHide() {
        return pre.getBoolean("action_hie", false);
    }

    public Map<String, Object> getAllKeyAndValuePreference() {
        return (Map<String, Object>) pre.getAll();
    }

    public void restoreSetting(String key, Object object) {
        SharedPreferences.Editor editor = pre.edit();
        if (object.getClass().equals(Boolean.class)) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object.getClass().equals(Float.class)) {
            editor.putFloat(key, (Float) object);
        } else if (object.getClass().equals(String.class)) {
            editor.putString(key, (String) object);
        }
        editor.commit();
    }

}
