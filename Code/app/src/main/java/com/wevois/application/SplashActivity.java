package com.wevois.application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

import com.wevois.application.Interface.OnUpdateNeededListener;
import com.wevois.application.Utilities.CommonMethods;
import com.wevois.application.Utilities.ForceUpdateChecker;

import java.nio.charset.StandardCharsets;

public class SplashActivity extends AppCompatActivity implements OnUpdateNeededListener {
    SharedPreferences pref;
    CommonMethods cmn;
    boolean checkPermission = false;
    ForceUpdateChecker forceUpdateChecker;
    AlertDialog infoDialog;
    String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    private void init() {
        cmn = new CommonMethods(SplashActivity.this);
        pref = getSharedPreferences("db_util", MODE_PRIVATE);
        forceUpdateChecker = new ForceUpdateChecker(SplashActivity.this, this);
//        pref.edit().putString("dbRef", "https://iejaipurgreater.firebaseio.com/").apply();
//        pref.edit().putString("stoRef", "gs://dtdnavigator.appspot.com/Jaipur-Greater").apply();
        pref.edit().putString("dbRef", "https://dtdnavigatortesting.firebaseio.com/").apply();
        pref.edit().putString("stoRef", "gs://dtdnavigator.appspot.com/Test").apply();
        initializePermissionDialog();
        new Thread(this::fetchLocMatchVal).start();
        forceUpdateChecker.check();
    }

    private void initializePermissionDialog() {
        View dialog = this.getLayoutInflater().inflate(R.layout.info_dialog_layout, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).setView(dialog).setCancelable(false);
        infoDialog = alertDialog.create();
        dialog.findViewById(R.id.accept_dialog_btn).setOnClickListener(v -> {
            infoDialog.dismiss();
            checkPermission = true;
            cmn.intentToAppInfo();
        });
        infoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void checkPermission() {
        SplashActivity.this.runOnUiThread(() -> {
            if (ActivityCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SplashActivity.this, PERMISSIONS, 0000);
                return;
            }
            proceed();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0000) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    String per = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, per)) {
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                            alertBuilder.setCancelable(false);
                            alertBuilder.setTitle("जरूरी सूचना");
                            alertBuilder.setMessage("सभी permissions देना अनिवार्य है बिना permissions के आप आगे नहीं बढ़ सकते है |");
                            alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> checkPermission());

                            AlertDialog alert = alertBuilder.create();
                            alert.show();
                        } else {
                            infoDialog();
                        }
                        return;
                    }
                }
                proceed();
            } else {
                checkPermission();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0000) {
            if (resultCode == RESULT_OK) {
                proceed();
            } else {
                checkPermission();
            }
        }
    }

    private void proceed() {
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (cmn.getLoginPref().getString("uid", "").trim().length() > 1) {
                    startActivity(new Intent(SplashActivity.this, LandingActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            }
        }.start();
    }

    private void infoDialog() {
        try {
            infoDialog.dismiss();
            infoDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission) {
            checkPermission = false;
            checkPermission();
        }
    }

    private void fetchLocMatchVal() {
        try {
            cmn.stoRef().child("Defaults/LocMatDisValForBvgApp.json").getMetadata().addOnSuccessListener(storageMetadata -> {
                long fileCreationTime = storageMetadata.getCreationTimeMillis();
                long fileDownloadTime = cmn.getFiltersPref().getLong("LocMatDisValForBvgAppDownloadTime", 0);
                if (fileDownloadTime != fileCreationTime) {
                    cmn.stoRef().child("Defaults/LocMatDisValForBvgApp.json")
                            .getBytes(10000000)
                            .addOnSuccessListener(taskSnapshot -> {
                                try {
                                    String str = new String(taskSnapshot, StandardCharsets.UTF_8);
                                    cmn.getFiltersPref().edit().putFloat("LocMatDisValForBvgApp", Float.parseFloat(str)).apply();
                                    cmn.getFiltersPref().edit().putLong("LocMatDisValForBvgAppDownloadTime", fileCreationTime).apply();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateNeeded(String updateUrl) {
        try {
            if (updateUrl == null) {
                checkPermission();
            } else {
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("New version available")
                        .setCancelable(false)
                        .setMessage("Please, update app to new version to continue service.")
                        .setPositiveButton("Update",
                                (dialog1, which) -> redirectStore(updateUrl)).setNegativeButton("No, thanks",
                                (dialog12, which) -> {
                                    if (forceUpdateChecker.mustUpdate()) {
                                        finish();
                                    } else {
                                        dialog12.dismiss();
                                    }
                                }).create();
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void redirectStore(String updateUrl) {
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}