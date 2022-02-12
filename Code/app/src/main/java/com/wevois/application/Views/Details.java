package com.wevois.application.Views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.wevois.application.R;
import com.wevois.application.Utilities.CommonMethods;
import com.wevois.application.databinding.ActivityDetailsBinding;
import com.wevois.application.viewmodel.DetailsViewModel;
import com.wevois.application.viewmodelfactory.DetailsViewModelFactory;

public class Details extends AppCompatActivity {
    ActivityDetailsBinding binding;
    DetailsViewModel viewModel;
    CommonMethods cmn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_details);
        viewModel = new ViewModelProvider(this, new DetailsViewModelFactory(this)).get(DetailsViewModel.class);
        binding.setDetailsviewmodel(viewModel);
        setPageTitle();
        cmn = CommonMethods.getInstance();
    }

    private void setPageTitle() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_arrow);
        toolbar.setNavigationOnClickListener(v -> {
            super.onBackPressed();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cmn.permissionDialog(this).observeForever(response -> { });
        }
    }
}