package com.wevois.application.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.wevois.application.R;
import com.wevois.application.Utilities.CommonMethods;
import com.wevois.application.viewmodel.DetailsViewModel;
import com.wevois.application.viewmodel.SplashViewModel;
import com.wevois.application.databinding.ActivitySplashBinding;
import com.wevois.application.viewmodelfactory.DetailsViewModelFactory;
import com.wevois.application.viewmodelfactory.SplashViewModelFactory;

public class Splash extends AppCompatActivity {
    ActivitySplashBinding binding;
    SplashViewModel viewModel;
    boolean checkPermission = false;
    CommonMethods cmn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        viewModel = new ViewModelProvider(this, new SplashViewModelFactory(this)).get(SplashViewModel.class);
        binding.setSplashviewmodel(viewModel);
        Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.mysplashanimation);
        binding.layout.setAnimation(myAnim);
        cmn = CommonMethods.getInstance();
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
                            alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> viewModel.checkPermission());

                            AlertDialog alert = alertBuilder.create();
                            alert.show();
                        } else {
                            cmn.permissionDialog(this).observeForever(response -> {
                                checkPermission = true;
                            });
                        }
                        return;
                    }
                }
                cmn.proceed(this);
            } else {
                viewModel.checkPermission();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0000) {
            if (resultCode == RESULT_OK) {
                cmn.proceed(this);
            } else {
                viewModel.checkPermission();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission) {
            checkPermission = false;
            viewModel.checkPermission();
        }
    }
}