package com.wevois.application.viewmodelfactory;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.wevois.application.viewmodel.SplashViewModel;

public class SplashViewModelFactory implements ViewModelProvider.Factory {
    Activity activity;

    public SplashViewModelFactory(Activity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SplashViewModel(activity);
    }
}

