package com.wevois.application.Repository;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.wevois.application.FcmNotificationSender;
import com.wevois.application.Utilities.CommonMethods;
import com.wevois.application.Utilities.DateTimeUtilities;
import com.wevois.application.model.LandingListModel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class DetailsRepository {

    @SuppressLint("StaticFieldLeak")
    public LiveData<Bitmap> downloadAndShowImage(String imageName, CommonMethods cmn, Activity activity) {
        MutableLiveData<Bitmap> response = new MutableLiveData<>();
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... p) {
                try {
                    FirebaseStorage.getInstance().getReferenceFromUrl(imageName).getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
                        response.setValue(cmn.bytesTOBitmap(bytes));
                    }).addOnFailureListener(e -> {
                        response.setValue(null);
                    });
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
                return null;
            }
        }.execute();
        return response;
    }

    public void saveData(Activity activity, CommonMethods cmn, DateTimeUtilities dateTimeUtil, LandingListModel model, Location lastKnownLocation, SharedPreferences preferences, StorageReference stoRef, DatabaseReference rdmsRef, Bitmap photo) {
        cmn.setProgressDialog("Please wait...","Data saving...",activity);
        ByteArrayOutputStream bosUpload = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bosUpload);
        stoRef.child(model.getImageNm() + "~RESOLVED").putBytes(bosUpload.toByteArray()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                HashMap<String, Object> mapToUpload = new HashMap<>();
                mapToUpload.put("date", dateTimeUtil.getTodayDate());
                mapToUpload.put("time", dateTimeUtil.getCurrentTime());
                mapToUpload.put("latlng", lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude());
                mapToUpload.put("imageRef", model.getImageNm() + "~RESOLVED");
                mapToUpload.put("user", cmn.getLoginPref(activity).getString("uid", ""));
                mapToUpload.put("address", model.getAdd());
                rdmsRef.child(model.getDate() + "/" + model.getSerialNm() + "/BvgAction").setValue(mapToUpload).addOnCompleteListener(taskRDBMS -> {
                    if (taskRDBMS.isSuccessful()) {
                        new Thread(() -> cmn.rdbmsRef(activity).child("WastebinMonitor/Summary/DateWise/" + dateTimeUtil.getTodayDate() + "/totalResolvedCount")
                                .runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData currentData1) {
                                        if (currentData1.getValue() == null) {
                                            currentData1.setValue(1);
                                        } else {
                                            currentData1.setValue(String.valueOf((Integer.parseInt(currentData1.getValue().toString()) + 1)));
                                        }
                                        return Transaction.success(currentData1);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError error1, boolean committed1, @Nullable DataSnapshot currentData1) {
                                        if (error1 == null) {
                                        }
                                    }
                                })).start();

                        new Thread(() -> cmn.rdbmsRef(activity).child("WastebinMonitor/Summary/CategoryWise/totalResolvedCount")
                                .runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData currentData12) {
                                        if (currentData12.getValue() == null) {
                                            currentData12.setValue(1);
                                        } else {
                                            currentData12.setValue(String.valueOf((Integer.parseInt(currentData12.getValue().toString()) + 1)));
                                        }
                                        return Transaction.success(currentData12);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError error12, boolean committed12, @Nullable DataSnapshot currentData12) {
                                        if (error12 == null) {
                                        }
                                    }
                                })).start();

                        new Thread(() -> cmn.rdbmsRef(activity).child("WastebinMonitor/Summary/DateWise/" + dateTimeUtil.getTodayDate() + "/" + model.getCategory() + "/totalResolvedCount")
                                .runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData currentData13) {
                                        if (currentData13.getValue() == null) {
                                            currentData13.setValue(1);
                                        } else {
                                            currentData13.setValue(String.valueOf((Integer.parseInt(currentData13.getValue().toString()) + 1)));
                                        }
                                        return Transaction.success(currentData13);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError error13, boolean committed13, @Nullable DataSnapshot currentData13) {
                                        if (error13 == null) {
                                        }
                                    }
                                })).start();

                        new Thread(() -> cmn.rdbmsRef(activity).child("WastebinMonitor/Summary/CategoryWise/" + model.getCategory() + "/totalResolvedCount")
                                .runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData currentData14) {
                                        if (currentData14.getValue() == null) {
                                            currentData14.setValue(1);
                                        } else {
                                            currentData14.setValue(String.valueOf((Integer.parseInt(currentData14.getValue().toString()) + 1)));
                                        }
                                        return Transaction.success(currentData14);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError error14, boolean committed14, @Nullable DataSnapshot currentData14) {
                                        if (error14 == null) {
                                        }
                                    }
                                })).start();


                        model.setActionDate(dateTimeUtil.getTodayDate());
                        model.setActionTime(dateTimeUtil.getCurrentTime());
                        model.setActionLatLng(lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude());
                        model.setActionImageRef(model.getImageNm() + "~RESOLVED");
                        model.setActionUser(cmn.getLoginPref(activity).getString("uid", ""));
                        ArrayList<LandingListModel> pendingListAls = new ArrayList<>();
                        pendingListAls.add(model);
                        Gson gson = new Gson();
                        String json = gson.toJson(pendingListAls);
                        preferences.edit().putString("intentData", json).apply();
                        cmn.closeDialog(activity);
                        Toast.makeText(activity, "Data saved Successfully", Toast.LENGTH_SHORT).show();
                    }
                });

                cmn.rdbmsRef(activity).child("WastebinMonitor/Users/" + model.getUser() + "/token").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            if (snapshot.getValue().toString().trim().length() > 0) {
                                SharedPreferences sPre = activity.getSharedPreferences("login_details", Context.MODE_PRIVATE);
                                FcmNotificationSender fcmNotificationSender = new FcmNotificationSender(snapshot.getValue().toString(), sPre.getString("notificationTitle", "WeVOIS"),
                                        sPre.getString("notificationMessage", "WeVOIS"), model.getImageNm(), "login_app_icon", "push_notification_icon_color", ".views.ResolvedActivity", activity, activity);
                                fcmNotificationSender.SendNotification();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(e -> {
            cmn.closeDialog(activity);
            Toast.makeText(activity, "Image saving failed", Toast.LENGTH_SHORT).show();
        });
    }
}
