package com.test.googledrive.Setting;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.test.googledrive.BaseActivity;
import com.test.googledrive.Config.AESCrypt;
import com.test.googledrive.Config.BaseApp;
import com.test.googledrive.GlobalUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

/**
 * Created by ANDT on 10/6/2016.
 */
public class AppCompatPreferenceActivity extends PreferenceActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private AppCompatDelegate mDelegate;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "PreferenceActivity";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang restore data !");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

	@NonNull
	@Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

	@Override
	protected void onPause() {
		super.onPause();
	}

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(mGoogleApiClient != null) {
            if(mGoogleApiClient.isConnected()) {
                return;
            }
            else {
                mGoogleApiClient.connect();
            }
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG,"Setting connected");
    }

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
    }

    public void restoreSetting() {
        progressDialog.show();
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "SettingADA"))
                .build();
        Drive.DriveApi.query(mGoogleApiClient, query)
                .setResultCallback(metadataCallback);
    }

    final private ResultCallback<DriveApi.MetadataBufferResult> metadataCallback = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    progressDialog.dismiss();
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                    if (result.getMetadataBuffer() != null) {
                        if (result.getMetadataBuffer().getCount() > 0) {
                            DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient, result.getMetadataBuffer().get(0).getDriveId());
                            file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(contentsOpenedCallback);
                        } else {
                            Toast.makeText(AppCompatPreferenceActivity.this,"khong co file restore",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d(TAG,result.getStatus()+"");
                    }
                }
            };

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
                        progressDialog.dismiss();
                        BaseApp.getInstance().setFirstUseApp(1);
                        String dataRestore = GlobalUtils.getInstance(AppCompatPreferenceActivity.this).getStringFromInputStream(inputStream);
                        String dataDecrypt = AESCrypt.decrypt(GlobalUtils.password,dataRestore);
                        BaseApp.getInstance().setDataSetting(dataDecrypt);
                        GlobalUtils.getInstance(AppCompatPreferenceActivity.this).restore(dataRestore);
                        Toast.makeText(AppCompatPreferenceActivity.this,"da restore, data sau khi giai ma la : "+ dataDecrypt,Toast.LENGTH_LONG).show();
                        contents.discard(mGoogleApiClient);
                        reload(AppCompatPreferenceActivity.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                }
            };

    private static void reload(Activity activity) {
        Intent intent = activity.getIntent();
        activity.overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.finish();
        activity.overridePendingTransition(0, 0);
        activity.startActivity(intent);
    }

}
