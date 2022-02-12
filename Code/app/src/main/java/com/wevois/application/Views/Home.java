package com.wevois.application.Views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import com.wevois.application.Adapter.ParentRecyclerViewAdapter;
import com.wevois.application.R;
import com.wevois.application.databinding.ActivityHomeBinding;
import com.wevois.application.viewmodel.HomeViewModel;
import com.wevois.application.viewmodel.LoginViewModel;
import com.wevois.application.viewmodelfactory.HomeViewModelFactory;

public class Home extends AppCompatActivity {
    ActivityHomeBinding binding;
    HomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        viewModel = new ViewModelProvider(this, new HomeViewModelFactory(this,binding.parentListview)).get(HomeViewModel.class);
        binding.setHomeviewmodel(viewModel);

    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.resume();
    }
}