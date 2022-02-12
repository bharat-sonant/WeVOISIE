package com.wevois.application.viewmodel;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

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
import com.wevois.application.Views.Home;
import com.wevois.application.Views.Login;

import java.util.HashMap;
import java.util.Objects;

public class LoginViewModel extends ViewModel {
    Activity activity;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    boolean isPass=true;
    CommonMethods cmn;
    DateTimeUtilities dtUtil;
    private FirebaseAuth mAuth;

    public void init(Login login) {
        activity = login;
        try {
            cmn = CommonMethods.getInstance();
            dtUtil = new DateTimeUtilities();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("381118272786-govj6slvjf5uathafc3lm8fq9r79qtiq.apps.googleusercontent.com")
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void firebaseAuthWithGoogle(int requestCode,int resultCode,Intent data) {
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            cmn.setProgressDialog("Login", "Creating Account", activity);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(activity, taska -> {
                            isPass = true;
                            if (taska.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                cmn.rdbmsRef(activity)
                                        .child("WastebinMonitor/BVGUsers/" + user.getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.getValue() == null) {
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("email", user.getEmail());
                                                    hashMap.put("name", user.getDisplayName());
                                                    hashMap.put("date", dtUtil.getTodayDate() + " " + dtUtil.getCurrentTime());
                                                    cmn.rdbmsRef(activity).child("WastebinMonitor/BVGUsers/" + user.getUid() + "/").setValue(hashMap);
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
            } catch (ApiException e) {
                e.printStackTrace();
            }
        } else {
            isPass = false;
        }
    }

    public void loginBtn() {
        if (isPass) isPass = false;
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        cmn.setProgressDialog("Login", "Creating Account", activity);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            SharedPreferences pref = activity.getSharedPreferences("login_details", MODE_PRIVATE);
            pref.edit().putString("uid", user.getUid()).apply();
            pref.edit().putString("name", Objects.requireNonNull(user).getDisplayName()).apply();
            pref.edit().putString("email", user.getEmail()).apply();
            cmn.closeDialog(activity);
            activity.startActivity(new Intent(activity, Home.class));
            activity.finish();
        }
    }

}
