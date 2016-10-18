package com.test.googledrive;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    Calendar currentCal = Calendar.getInstance();
    BroadcastBackup broadcastBackup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bundle = getIntent().getExtras();
        registerBroadcast();
        if (bundle != null) {
            if(bundle.containsKey("backup")) {
                setBackup(true);
            }
            else if(bundle.containsKey("backupSchedule")) {
                Log.d(TAG,"backupSchedule == true");
                setBackupSchedule(true);
                backup.setText("BACKUP (Last backup "+currentCal.get(Calendar.HOUR_OF_DAY)+":"+currentCal.get(Calendar.MINUTE)+":"+currentCal.get(Calendar.SECOND));
//                backup.setEnabled(false);
            }
        }
        timeDialog = new TimeDialog(this);
        setOnClick();
        settingManager = SettingManager.getInstance(this);
        if (BaseApp.getInstance().getLogin()) {
            login.setText("Change Account");
        }
    }

    private void registerBroadcast() {
        broadcastBackup = new BroadcastBackup();
        IntentFilter filter = new IntentFilter();
        filter.addAction("BACKUP");
        this.registerReceiver(broadcastBackup,filter);
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
                break;
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        Log.d(TAG, "bat dau " + hourOfDay + " gio " + minute + " phut " + second + " giay");
        backupSetting(hourOfDay,minute,second);
    }

    private void backupSetting(int hour, int minutes, int seconds) {
        Calendar firingCal= Calendar.getInstance();

        firingCal.set(Calendar.HOUR_OF_DAY, hour);
        firingCal.set(Calendar.MINUTE, minutes);
        firingCal.set(Calendar.SECOND, seconds);

        Log.d(TAG,"gio cua ngay "+currentCal.get(Calendar.HOUR_OF_DAY)+" gio "+currentCal.get(Calendar.HOUR)+" ngay trong thang "+currentCal.get(Calendar.DAY_OF_MONTH));

        long intendedTime = firingCal.getTimeInMillis();
        long currentTime = currentCal.getTimeInMillis();

        Intent intent = new Intent(getApplicationContext(), BackupService.class);
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

    @Override
    protected void onResume() {
        super.onResume();
        BaseApp.getInstance().activityResumed();
        Log.d(TAG,"isBackup "+isBackup());
        Log.d(TAG, "onResume: vao day");
//        if (isBackup()) {
//            backup.setEnabled(true);
//        } else {
//            backup.setEnabled(false);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: vao day");
        setChangeAccount(false);
        BaseApp.getInstance().activityDestroyed();
        this.unregisterReceiver(broadcastBackup);
    }

    private class BroadcastBackup extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null && intent.getAction().equalsIgnoreCase("BACKUP")) {
                backupData();
                backup.setText("BACKUP (Last backup "+currentCal.get(Calendar.HOUR_OF_DAY)+":"+currentCal.get(Calendar.MINUTE)+":"+currentCal.get(Calendar.SECOND));
//                backup.setEnabled(false);
            }
        }
    }
}
