package com.test.googledrive;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
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
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.test.googledrive.Config.AESCrypt;
import com.test.googledrive.Config.BaseApp;
import com.test.googledrive.Config.StringUtils;
import com.test.googledrive.Setting.SettingManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * Created by ANDT on 10/6/2016.
 */

public abstract class BaseActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "BaseActivity";
    private static final int REQUEST_CODE_RESOLUTION = 1;
    private GoogleApiClient mGoogleApiClient;
    private boolean showDialogLogin = false;
    private boolean changeAccount = false;
    private SettingManager settingManager;
    private boolean backup;
    private boolean backupSetting = false;
    private static final String MINE_TYPE = "text/plain";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeAccount = false;
        backup = false;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        settingManager = SettingManager.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (BaseApp.getInstance().getLogin()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: vao day");
        changeAccount = false;
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        BaseApp.getInstance().setLogin(true);
        Log.d(TAG,"changeAccount == "+changeAccount);
        if (showDialogLogin) {
            changeAccount = true;
            showDialogLogin = false;
            mGoogleApiClient.clearDefaultAccountAndReconnect();
        } else {
            Toast.makeText(getApplicationContext(), "Đã kết nối !", Toast.LENGTH_LONG).show();
            if(isRestore()) {
                searchFile();
            }
            else if(backupSetting) {
                Log.d(TAG,"vao cho backupsetting nay roi");
                backupData();
            }
        }
    }

    private void searchFile() {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, StringUtils.KEY_BACKUP))
                .build();
        Drive.DriveApi.query(mGoogleApiClient, query)
                .setResultCallback(metadataCallback);
    }

    final private ResultCallback<DriveApi.MetadataBufferResult> metadataCallback = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                    if (result.getMetadataBuffer() != null) {
                        if (result.getMetadataBuffer().getCount() > 0) {
                            Log.d(TAG, "co du lieu nhe");
                            DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient, result.getMetadataBuffer().get(0).getDriveId());
                            if (isRestore()) {
                                changeAccount = false;
                                file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(contentsOpenedCallback);
                            }
                            else {
                                return;
                            }
                        } else {
                            Log.d(TAG, "chua co du lieu nhe");
                            BaseApp.getInstance().setFirstUseApp(1);
                            if(isBackup()) {
                                createFile();
                            }
                        }
                    } else {
                        Log.d(TAG,result.getStatus()+"");
                    }
                }
            };

    public void backupData() {
        try {
            if(isBackup()) {
                Log.d(TAG,"backup bang true");
                backup = false;
                Query query = new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TITLE, StringUtils.KEY_BACKUP))
                        .build();
                Drive.DriveApi.query(mGoogleApiClient, query)
                        .setResultCallback(metadataCallbackBackup);
            }
            else {
                Log.d(TAG,"backup bang false");
            }
        }
        catch (Exception e) {
            Log.e(TAG,"errorMsg "+e.getMessage());
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
                        } else {
                            if(isBackup()) {
                                createFile();
                            }
                        }
                    } else {
                        Log.d(TAG,result.getStatus()+"");
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
                        String data = GlobalUtils.getInstance(BaseActivity.this).encryptData(jsonString);
                        fileInputStream.read(new byte[fileInputStream.available()]);
                        FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor
                                .getFileDescriptor());
                        Writer writer = new OutputStreamWriter(fileOutputStream);
                        writer.write(data);
                        writer.close();
                        BaseApp.getInstance().setDataSetting(jsonString);
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(StringUtils.KEY_BACKUP)
                                .setMimeType(MINE_TYPE).build();
                        driveContents.commit(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                Toast.makeText(getApplicationContext(),"da backup !",Toast.LENGTH_SHORT).show();
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

    ResultCallback<DriveApi.DriveContentsResult> contentsOpenedCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                    DriveContents contents = result.getDriveContents();
                    InputStream inputStream = contents.getInputStream();
                    try {
                        BaseApp.getInstance().setFirstUseApp(1);
                        String dataRestore = GlobalUtils.getInstance(BaseActivity.this).getStringFromInputStream(inputStream);
                        Log.d("dayroi", dataRestore);
                        DialogRestoreData(dataRestore);
                        contents.discard(mGoogleApiClient);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

    public boolean isBackup() {
//        if (checkRestoreData() && backup) {
//            return true;
//        } else {
//            return false;
//        }
        String data = GlobalUtils.getInstance(this).setPreferenceToJsonObject().toString();
        if(!data.equalsIgnoreCase(BaseApp.getInstance().getDataSetting())) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isRestore() {
        Log.d(TAG, "isRestore: changeAccount "+changeAccount+ " "+BaseApp.getInstance().getFirstUseApp());
        if (changeAccount || BaseApp.getInstance().getFirstUseApp() == 0) {
            return true;
        } else {
            return false;
        }
    }

    private void DialogRestoreData(final String dataRestore) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông báo !");
        builder.setMessage("Bạn có muốn khôi phục Setting không ?");
        builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String dataDecrypt = AESCrypt.decrypt(GlobalUtils.password,dataRestore);
                    BaseApp.getInstance().setDataSetting(dataDecrypt);
                    GlobalUtils.getInstance(BaseActivity.this).restore(dataRestore);
                    Toast.makeText(getApplicationContext(), "Đã Restore Setting rồi nhé !", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Không nhé", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void xoaFileVaCreateFileKhiBackUp(DriveApi.MetadataBufferResult result) {
        Drive.DriveApi.getFile(mGoogleApiClient, result.getMetadataBuffer().get(0).getDriveId()).delete(mGoogleApiClient);
        createFile();
    }

    public void createFile() {
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(driveContentsCallback);
    }

    final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (result.getStatus().isSuccess()) {
                        CreateFileOnGoogleDrive(result);
                    }
                }
            };

    public void CreateFileOnGoogleDrive(DriveApi.DriveContentsResult result) {
        final DriveContents driveContents = result.getDriveContents();
        // Perform I/O off the UI thread.
        new Thread() {
            @Override
            public void run() {
                // write content to DriveContents
                JSONObject json = setPreferenceToJsonObject();
                OutputStream outputStream = driveContents.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);
                try {
                    String data = GlobalUtils.getInstance(BaseActivity.this).encryptData(json.toString());
                    writer.write(data);
                    writer.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                BaseApp.getInstance().setDataSetting(json.toString());
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(StringUtils.KEY_BACKUP)
                        .setMimeType(MINE_TYPE)
                        .setStarred(true).build();
                // getAppFolder để cho vào thư mục ẩn, để app mình dùng được, người dùng ko thấy và app khác ko truy cập được
                // khác với getRootFolder
                Drive.DriveApi.getAppFolder(mGoogleApiClient)
                        .createFile(mGoogleApiClient, changeSet, driveContents)
                        .setResultCallback(fileCallback);
            }
        }.start();
    }

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (result.getStatus().isSuccess()) {
                        Toast.makeText(getApplicationContext(), "Đã Backup rồi nhé !", Toast.LENGTH_LONG).show();

                    }
                    return;
                }
            };

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
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

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public void setBackup(boolean isBackup) {
        backup = isBackup;
    }

    public void setBackupSchedule(boolean isBackup) {
        backupSetting = isBackup;
    }

    public void logInGoogle(Button login) {
        if (BaseApp.getInstance().getFirstUseApp() == 0) {
            changeAccount = true;
            login.setText("Change Account");
            mGoogleApiClient.connect();
        } else {
            showDialogLogin = true;
            if(mGoogleApiClient.isConnected()) {
                changeAccount = true;
                showDialogLogin = false;
                mGoogleApiClient.clearDefaultAccountAndReconnect();
            }
        }
    }

    public void setChangeAccount(boolean changeAccount) {
        this.changeAccount = changeAccount;
    }
}
