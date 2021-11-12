package com.wevois.application.Utilities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.wevois.application.Interface.OnUpdateNeededListener;

public class ForceUpdateChecker {
    public static final String
            KEY_UPDATE_REQUIRED = "wevois_ie_update_required",
            KEY_CURRENT_VERSION = "wevois_ie_current_version",
            KEY_UPDATE_URL = "wevois_ie_playstore_url",
            KEY_MUST_UPDATE = "wevois_ie_must_update";

    private final OnUpdateNeededListener onUpdateNeededListener;
    private final Context context;

    public ForceUpdateChecker(@NonNull Context context, OnUpdateNeededListener onUpdateNeededListener) {
        this.context = context;
        this.onUpdateNeededListener = onUpdateNeededListener;
    }

    public void check() {
        try {
            FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
            remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    remoteConfig.getString(KEY_CURRENT_VERSION);
                }
                if (remoteConfig.getBoolean(KEY_UPDATE_REQUIRED)) {
                    String currentVersion = remoteConfig.getString(KEY_CURRENT_VERSION);
                    String appVersion = getAppVersion(context);
                    String updateUrl = remoteConfig.getString(KEY_UPDATE_URL);
                    if (!TextUtils.equals(currentVersion, appVersion) && onUpdateNeededListener != null) {
                        onUpdateNeededListener.onUpdateNeeded(updateUrl);
                    } else if (TextUtils.equals(currentVersion, appVersion) && onUpdateNeededListener != null) {
                        onUpdateNeededListener.onUpdateNeeded(null);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean mustUpdate() {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        return remoteConfig.getBoolean(KEY_MUST_UPDATE);
    }

    private String getAppVersion(Context context) {
        String result = "";
        try {
            result = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
}
