package com.wevois.application.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.annotations.Nullable;
import com.wevois.application.R;
import com.wevois.application.databinding.ActivityLoginBinding;
import com.wevois.application.viewmodel.LoginViewModel;

public class Login extends AppCompatActivity {
    ActivityLoginBinding binding;
    LoginViewModel viewModel;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        firebaseAuth = FirebaseAuth.getInstance();
        binding.setLoginviewmodel(viewModel);
        viewModel.init(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("WeVois IE"," Task "+resultCode);
        viewModel.firebaseAuthWithGoogle(requestCode,resultCode,data);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        // Check condition
//        if (requestCode == 9001) {
//            // When request code is equal to 100 initialize
//            Log.e("WeVois IE"," Task "+data.getData()+" "+resultCode);
//            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
//            // check condition
//            if (signInAccountTask.isSuccessful()) {
//                // When google sign in successful initialize string
//                String s = "Google sign in successful";
//                // Display Toast
//                displayToast(s);
//                // Initialize sign in account
//                try {
//                    // Initialize sign in account
//                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
//                    // Check condition
//                    if (googleSignInAccount != null) {
//                        // When sign in account is not equal to null initialize auth credential
//                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
//                        // Check credential
//                        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                // Check condition
//                                if (task.isSuccessful()) {
//                                    // When task is successful redirect to profile activity display Toast
//                                    startActivity(new Intent(Login.this, Home.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                                    displayToast("Firebase authentication successful");
//                                } else {
//                                    // When task is unsuccessful display Toast
//                                    displayToast("Authentication Failed :" + task.getException().getMessage());
//                                }
//                            }
//                        });
//                    }
//                } catch (ApiException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    private void displayToast(String s) {
//        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
//    }
}