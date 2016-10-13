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

public class BackupService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private static final String LOG_TAG = "BackupService";
    SettingManager settingManager;

    public BackupService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        settingManager = SettingManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mGoogleApiClient != null) {
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }
        Log.d("ThuNghiem", "bat dau vao service");
        Toast.makeText(this, "bat dau backup bang service nhe !", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        backupSetting();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(LOG_TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            return;
        }
    }

    public void backupSetting() {
        try {
            Query query = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TITLE, "SettingADA"))
                    .build();
            Drive.DriveApi.query(mGoogleApiClient, query)
                    .setResultCallback(metadataCallbackBackup);
        } catch (Exception e) {
            Log.e(LOG_TAG, "errorMsg " + e.getMessage());
        }
    }

    final private ResultCallback<DriveApi.MetadataBufferResult> metadataCallbackBackup = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                    if (result.getMetadataBuffer() != null) {
                        if (result.getMetadataBuffer().getCount() > 0) {
                            DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient, result.getMetadataBuffer().get(0).getDriveId());
                            backupData(file);
                        }
                    } else {
                        Log.d(LOG_TAG, result.getStatus() + "");
                    }
                }
            };

    private void backupData(DriveFile file) {
        if(file != null) {
            file.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                    DriveContents driveContents = result.getDriveContents();
                    ParcelFileDescriptor parcelFileDescriptor = driveContents.getParcelFileDescriptor();
                    FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor
                            .getFileDescriptor());
                    try {
                        JSONObject json = setPreferenceToJsonObject();
                        String jsonString = json.toString();
                        String data = GlobalUtils.getInstance(getApplicationContext()).encryptData(jsonString);
                        fileInputStream.read(new byte[fileInputStream.available()]);
                        FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor
                                .getFileDescriptor());
                        Writer writer = new OutputStreamWriter(fileOutputStream);
                        writer.write(data);
                        writer.close();
                        BaseApp.getInstance().setDataSetting(jsonString);
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(StringUtils.KEY_BACKUP)
                                .setMimeType("text/plain").build();
                        driveContents.commit(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                Log.d(LOG_TAG,"da backup bang service");
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else {
            Log.d("vaoday","file == null");
            return;
        }
    }

    public JSONObject setPreferenceToJsonObject() {
        Map<String, Object> allEntries = settingManager.getAllKeyAndValuePreference();
        JSONObject jsonObject = new JSONObject();
        if (checkRestoreData()) {
            for (Map.Entry<String, Object> entry : allEntries.entrySet()) {
                try {
                    if (!"tree_uri".equals(entry.getKey())) {
                        jsonObject.put(entry.getKey(), entry.getValue());
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return jsonObject;
    }

    private boolean checkRestoreData() {
        Map<String, Object> allEntries = settingManager.getAllKeyAndValuePreference();
        if (allEntries.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
}
