package com.test.googledrive;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.test.googledrive.Config.BaseApp;
import com.test.googledrive.Config.TimeDialog;
import com.test.googledrive.Services.BackupService;
import com.test.googledrive.Setting.SettingActivity;
import com.test.googledrive.Setting.SettingManager;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

    @BindView(R.id.login)
    Button login;
    @BindView(R.id.setting)
    Button setting;
    @BindView(R.id.scheduleBackup)
    Button scheduleBackup;
    @BindView(R.id.backup)
    Button backup;
    SettingManager settingManager;
    Bundle bundle;
    TimeDialog timeDialog;
    String LOG_TAG = "ThuNghiem";
    Calendar currentCal = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bundle = getIntent().getExtras();
        if (bundle != null) {
            if(bundle.containsKey("backup")) {
                setBackup(true);
            }
            else if(bundle.containsKey("backupSchedule")) {
                setBackupSchedule(true);
                backup.setText("BACKUP (Last backup "+currentCal.get(Calendar.HOUR_OF_DAY)+":"+currentCal.get(Calendar.MINUTE)+":"+currentCal.get(Calendar.SECOND));
                backup.setEnabled(false);
            }
        }
        timeDialog = new TimeDialog(this);
        setOnClick();
        settingManager = SettingManager.getInstance(this);
        if (BaseApp.getInstance().getLogin()) {
            login.setText("Change Account");
        }

        if (isBackup()) {
            backup.setEnabled(true);
        } else {
            backup.setEnabled(false);
        }
    }

    private void setOnClick() {
        setting.setOnClickListener(this);
        login.setOnClickListener(this);
        backup.setOnClickListener(this);
        scheduleBackup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.login:
                logInGoogle(login);
                break;
            case R.id.scheduleBackup:
                timeDialog.timeDialog();
                break;
            case R.id.backup:
                backupData();
                backup.setText("BACKUP (Last backup "+currentCal.get(Calendar.HOUR_OF_DAY)+":"+currentCal.get(Calendar.MINUTE)+":"+currentCal.get(Calendar.SECOND));
                backup.setEnabled(false);
                break;
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        Log.d(LOG_TAG, "bat dau " + hourOfDay + " gio " + minute + " phut " + second + " giay");
        backupSetting(hourOfDay,minute,second);
    }

    private void backupSetting(int hour, int minutes, int seconds) {
        Calendar firingCal= Calendar.getInstance();

        firingCal.set(Calendar.HOUR_OF_DAY, hour);
        firingCal.set(Calendar.MINUTE, minutes);
        firingCal.set(Calendar.SECOND, seconds);

        Log.d(LOG_TAG,"gio cua ngay "+currentCal.get(Calendar.HOUR_OF_DAY)+" gio "+currentCal.get(Calendar.HOUR)+" ngay trong thang "+currentCal.get(Calendar.DAY_OF_MONTH));

        long intendedTime = firingCal.getTimeInMillis();
        long currentTime = currentCal.getTimeInMillis();

        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.putExtra("backupSchedule",true);
        PendingIntent pintent = PendingIntent.getService(MainActivity.this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if(intendedTime >= currentTime){
            alarm.setRepeating(AlarmManager.RTC, intendedTime, AlarmManager.INTERVAL_DAY, pintent);
        } else{
            firingCal.add(Calendar.DAY_OF_MONTH, 1);
            intendedTime = firingCal.getTimeInMillis();
            alarm.setRepeating(AlarmManager.RTC, intendedTime, AlarmManager.INTERVAL_DAY, pintent);
        }
    }

}
