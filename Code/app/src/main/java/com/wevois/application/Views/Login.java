package com.wevois.application.Views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import com.wevois.application.R;
import com.wevois.application.databinding.ActivityLoginBinding;
import com.wevois.application.viewmodel.LoginViewModel;

public class Login extends AppCompatActivity {
    ActivityLoginBinding binding;
    LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        binding.setLoginviewmodel(viewModel);
        viewModel.init(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        viewModel.firebaseAuthWithGoogle(requestCode,resultCode,data);
    }
}