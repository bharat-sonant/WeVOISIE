package com.wevois.application.viewmodel;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModel;

import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.wevois.application.Repository.SplashRepository;
import com.wevois.application.Utilities.CommonMethods;

public class SplashViewModel extends ViewModel {

    Activity activity;
    CommonMethods cmn;
    SharedPreferences pref;
    String[] PERMISSIONS = {Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_FINE_LOCATION};
    private AppUpdateManager mAppUpdateManager;
    SplashRepository repository;

    public SplashViewModel(Activity activity) {
        this.activity = activity;
        cmn = CommonMethods.getInstance();
        new Thread(()->{
            cmn.fetchWastebinMonitorSettings(activity);
        }).start();
        repository = new SplashRepository(cmn, activity);
        pref = activity.getSharedPreferences("db_util", MODE_PRIVATE);
//        pref.edit().putString("dbRef", "https://iejaipurgreater.firebaseio.com/").apply();
//        pref.edit().putString("stoRef", "gs://dtdnavigator.appspot.com/Jaipur-Greater").apply();
        pref.edit().putString("dbRef", "https://dtdnavigatortesting.firebaseio.com/").apply();
        pref.edit().putString("stoRef", "gs://dtdnavigator.appspot.com/Test").apply();
        new Thread(()-> repository.fetchWastebinTypes()).start();
        new Thread(()-> repository.fetchZonesAndWards()).start();
        new Thread(()-> repository.fetchLocMatchVal()).start();
        updateApp();
    }

    public void updateApp() {
        Log.d("TAG", "updateApp: check  "+mAppUpdateManager);
        mAppUpdateManager = AppUpdateManagerFactory.create(activity);
        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE /*AppUpdateType.IMMEDIATE*/)) {
                Log.d("TAG", "updateApp: check A "+mAppUpdateManager);
                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.IMMEDIATE /*AppUpdateType.IMMEDIATE*/, activity, 11);

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("TAG", "updateApp: check B "+mAppUpdateManager);
                checkPermission();
            }
        });
    }

    public void checkPermission() {
        activity.runOnUiThread(() -> {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS, 0000);
                return;
            }
            cmn.proceed(activity);
        });
    }
}
