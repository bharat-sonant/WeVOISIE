package com.wevois.application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.wevois.application.Utilities.CommonMethods;
import com.wevois.application.Utilities.DateTimeUtilities;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;
    CommonMethods cmn;
    DateTimeUtilities dtUtil;
    boolean isPass = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_actitivty);
        try {
            cmn = new CommonMethods(LoginActivity.this);
            dtUtil = new DateTimeUtilities();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("381118272786-govj6slvjf5uathafc3lm8fq9r79qtiq.apps.googleusercontent.com")
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void login(View view) {
        if (isPass) isPass = false;
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    isPass = true;
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        cmn.rdbmsRef()
                                .child("WastebinMonitor/BVGUsers/" + user.getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.getValue() == null) {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("email", user.getEmail());
                                            hashMap.put("name", user.getDisplayName());
                                            hashMap.put("date", dtUtil.getTodayDate() + " " + dtUtil.getCurrentTime());
                                            cmn.rdbmsRef()
                                                    .child("WastebinMonitor/BVGUsers/" + user.getUid() + "/").setValue(hashMap);
                                        }
                                        updateUI(user);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    } else {
                        updateUI(null);
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            cmn.setProgressDialog("Login", "Creating Account", LoginActivity.this);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                e.printStackTrace();
            }
        } else {
            isPass = false;
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            SharedPreferences pref = getSharedPreferences("login_details", MODE_PRIVATE);
            pref.edit().putString("uid", user.getUid()).apply();
            pref.edit().putString("name", Objects.requireNonNull(user).getDisplayName()).apply();
            pref.edit().putString("email", user.getEmail()).apply();
            cmn.closeDialog(LoginActivity.this);
            startActivity(new Intent(LoginActivity.this, LandingActivity.class));
            finish();
        }
    }

}