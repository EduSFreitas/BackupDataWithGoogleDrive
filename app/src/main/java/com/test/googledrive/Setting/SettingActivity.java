package com.test.googledrive.Setting;

import android.content.Context;
import android.content.Intent;
import android.preference.*;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.test.googledrive.Config.BaseApp;
import com.test.googledrive.GlobalUtils;
import com.test.googledrive.MainActivity;
import com.test.googledrive.R;

public class SettingActivity extends AppCompatPreferenceActivity {

    private static final String TAG = SettingActivity.class.getSimpleName();
    SettingManager settingManager;
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new CustomPreferenceClickListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingManager = SettingManager.getInstance(this);
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Setting");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        setupTabInterfaceSetting();
    }

    private void setupTabInterfaceSetting() {
        addPreferencesFromResource(R.xml.pre_setting_interface);
        CheckBoxPreference checkShowFile = (CheckBoxPreference) findPreference("show_file");
        if (settingManager.getShowFile()) {
            checkShowFile.setChecked(true);
            Log.d(TAG,"checkShowFile "+settingManager.getShowFile());
        }
        else {
            checkShowFile.setChecked(false);
        }
        CheckBoxPreference checkChangeDisplay = (CheckBoxPreference) findPreference("change_display");
        if (settingManager.getChangeDisplay()) {
            checkChangeDisplay.setChecked(true);
            Log.d(TAG,"checkChangeDisplay "+settingManager.getChangeDisplay());
        }
        else {
            checkChangeDisplay.setChecked(false);
        }
        CheckBoxPreference checkAutoHide = (CheckBoxPreference) findPreference("auto_hide");
        if (settingManager.getAutoHide()) {
            checkAutoHide.setChecked(true);
            Log.d(TAG,"checkAutoHide "+settingManager.getAutoHide());
        }
        else {
            checkAutoHide.setChecked(false);
        }

        checkShowFile.setOnPreferenceChangeListener(new Preference
                .OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, newValue);
                return true;
            }
        });

        checkChangeDisplay.setOnPreferenceChangeListener(new Preference
                .OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, newValue);
                return true;
            }
        });

        checkAutoHide.setOnPreferenceChangeListener(new Preference
                .OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, newValue);
                return true;
            }
        });
    }


    private static class CustomPreferenceClickListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Context mContext = preference.getContext();
            String key = preference.getKey();
            SettingManager settingManager = SettingManager.getInstance(mContext);
            if("show_file".equalsIgnoreCase(key)) {
                settingManager.setShowFile((boolean)newValue);
            }
            else if("change_display".equalsIgnoreCase(key)) {
                settingManager.setChangeDisplay((boolean)newValue);
            }
            else if("auto_hide".equalsIgnoreCase(key)) {
                settingManager.setAutoHide((boolean)newValue);
            }
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_restore :
                restoreSetting();
                break;
            case android.R.id.home :
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent setting = new Intent(SettingActivity.this, MainActivity.class);
        setting.setAction("setting_change");
        setting.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setting.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        String data = GlobalUtils.getInstance(this).setPreferenceToJsonObject().toString();
        if(!data.equalsIgnoreCase(BaseApp.getInstance().getDataSetting())) {
            Log.d("vaoday","co vao cho khac nhau nay");
            setting.putExtra("backup",true);
        }
        else {
            Log.d("vaoday","khong vao cho khac nhau nay");
        }
        startActivity(setting);
    }

}
