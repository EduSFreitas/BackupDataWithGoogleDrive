package com.test.googledrive.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.test.googledrive.BaseActivity;
import com.test.googledrive.Config.BaseApp;
import com.test.googledrive.Config.StringUtils;
import com.test.googledrive.GlobalUtils;
import com.test.googledrive.Setting.SettingManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

public class BackupService extends Service {

    SettingManager settingManager;

    public BackupService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        settingManager = SettingManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ThuNghiem", "bat dau vao service");
        showActivity();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showActivity() {
        if(BaseApp.getInstance().isActivityVisible()) {
            Intent intent = new Intent("BACKUP");
            this.sendBroadcast(intent);
        }
        else {
            Intent trIntent = new Intent("android.intent.action.MAIN");
            trIntent.setClass(this, com.test.googledrive.MainActivity.class);
            trIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            trIntent.putExtra("backupSchedule",true);
            this.startActivity(trIntent);
        }
        this.stopSelf();
    }
}
