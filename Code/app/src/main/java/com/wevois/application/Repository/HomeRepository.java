package com.wevois.application.Repository;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.wevois.application.Utilities.CommonMethods;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

public class HomeRepository {
    CommonMethods cmn;
    Activity activity;

    public HomeRepository(CommonMethods cmn, Activity activity) {
        this.cmn = cmn;
        this.activity = activity;
    }

    public LiveData<DataSnapshot> fetchData(Activity activity,String year, String month, String date,int category) {
        MutableLiveData<DataSnapshot> response = new MutableLiveData<>();
        try {
            Log.e("Data URL",cmn.rdbmsRef(activity).toString());
            if (category > 0){
                cmn.rdbmsRef(activity).child("WastebinMonitor/ImagesData/" + year + "/" + month + "/" + date + "/" + category).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        response.setValue(snapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }else {
                cmn.rdbmsRef(activity).child("WastebinMonitor/ImagesData/" + year + "/" + month + "/" + date).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        response.setValue(snapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
