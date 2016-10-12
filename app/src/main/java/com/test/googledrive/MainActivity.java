package com.test.googledrive;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.test.googledrive.Config.BaseApp;
import com.test.googledrive.Setting.SettingActivity;
import com.test.googledrive.Setting.SettingManager;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    Button login;
    Button setting;
    SettingManager settingManager;
    Bundle bundle;
    SecretKey secretKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = (Button) findViewById(R.id.login);
        setting = (Button) findViewById(R.id.setting);
        bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey("backup")) {
            setBackup(true);
        }
        setting.setOnClickListener(this);
        login.setOnClickListener(this);
        settingManager = SettingManager.getInstance(this);
        if(BaseApp.getInstance().getLogin()) {
            login.setText("Change Account");
        }
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
        }
    }

}
