package com.wevois.application.viewmodelfactory;

import android.app.Activity;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.wevois.application.viewmodel.DetailsViewModel;
import com.wevois.application.viewmodel.HomeViewModel;

public class DetailsViewModelFactory implements ViewModelProvider.Factory {
    Activity activity;

    public DetailsViewModelFactory(Activity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DetailsViewModel(activity);
    }
}

