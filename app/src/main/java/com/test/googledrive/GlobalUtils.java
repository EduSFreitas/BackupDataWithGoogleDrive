package com.test.googledrive;

import android.content.Context;
import android.util.Log;

import com.test.googledrive.Config.AESCrypt;
import com.test.googledrive.Setting.SettingManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ANDT on 10/6/2016.
 */

public class GlobalUtils {
    private static GlobalUtils instance;
    private SettingManager settingManager;
    public static final String password = "rubycellentertaiment";
    private static final String TAG = "GlobalUtils";

    private GlobalUtils(Context context) {
        settingManager = SettingManager.getInstance(context);
    }

    public static GlobalUtils getInstance(Context context) {
        if (instance == null) {
            instance = new GlobalUtils(context);
        }
        return instance;
    }

    public String getStringFromInputStream(InputStream stream) throws IOException {
        int n;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(stream, "UTF8");
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
        return writer.toString();
    }

    public void restore(String response) {
        try {
            String contentSettingSave = AESCrypt.decrypt(password, response);
            Log.d("contentSettingSave",contentSettingSave);
            if (contentSettingSave != null) {
                try {
                    JSONObject jsonObject = new JSONObject(contentSettingSave);
                    Iterator keysToCopyIterator = jsonObject.keys();
                    List<String> keysList = new ArrayList<>();
                    while (keysToCopyIterator.hasNext()) {
                        String key = (String) keysToCopyIterator.next();
                        keysList.add(key);
                    }
                    for (int i = 0; i < keysList.size(); i++) {
                        settingManager.restoreSetting(keysList.get(i), jsonObject.get(keysList.get(i)));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (GeneralSecurityException e) {
            showLog(e);
        }
    }

    public JSONObject setPreferenceToJsonObject() {
        Map<String, Object> allEntries = settingManager.getAllKeyAndValuePreference();
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Object> entry : allEntries.entrySet()) {
            try {
                if (!"tree_uri".equals(entry.getKey())) {
                    jsonObject.put(entry.getKey(), entry.getValue());
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return jsonObject;
    }

    public String encryptData(String data) {
        String encryptedMsg = "";
        try {
            encryptedMsg = AESCrypt.encrypt(password, data);
        }catch (GeneralSecurityException e){
            showLog(e);
        }
        return encryptedMsg;
    }

    private void showLog(Exception e) {
        Log.e(TAG, "errorMsg " + e.getMessage());
    }

}
