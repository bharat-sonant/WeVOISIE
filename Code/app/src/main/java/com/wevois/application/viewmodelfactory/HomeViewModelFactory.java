package com.wevois.application.viewmodelfactory;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.wevois.application.viewmodel.HomeViewModel;

public class HomeViewModelFactory implements ViewModelProvider.Factory {
    Activity activity;
    RecyclerView recyclerView;

    public HomeViewModelFactory(Activity activity,RecyclerView recyclerView) {
        this.activity = activity;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new HomeViewModel(activity,recyclerView);
    }
}
