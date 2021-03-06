package com.wevois.application.viewmodel;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.wevois.application.R;
import com.wevois.application.Repository.DetailsRepository;
import com.wevois.application.Utilities.CommonMethods;
import com.wevois.application.Utilities.DateTimeUtilities;
import com.wevois.application.model.LandingListModel;

public class DetailsViewModel extends ViewModel {
    Activity activity;
    CommonMethods cmn;
    DetailsRepository repository;
    DateTimeUtilities dateTimeUtil;
    SharedPreferences preferences;
    LandingListModel model;
    StorageReference stoRef;
    DatabaseReference rdmsRef;
    float distance;
    Bitmap photo;
    LocationCallback locationCallback;
    Location lastKnownLocation;
    public ObservableField<Boolean> isDirtyBtnVisible = new ObservableField<>(false), isCleanBtnVisible = new ObservableField<>(true);
    public ObservableField<String> headingTv = new ObservableField<>("Complaint -"), timeTv = new ObservableField<>(""), addressTv = new ObservableField<>("");
    public ObservableField<Bitmap> imageViewUrl = new ObservableField<>(), complaintViewUrl = new ObservableField<>(null), resolvedViewUrl = new ObservableField<>(null);

    public DetailsViewModel(Activity activity) {
        this.activity = activity;
        cmn = CommonMethods.getInstance();
        repository = new DetailsRepository();
        preferences = activity.getSharedPreferences("WeVOISIE", MODE_PRIVATE);
        preferences.edit().putString("intentData", "").apply();
        dateTimeUtil = new DateTimeUtilities();
        model = (LandingListModel) activity.getIntent().getSerializableExtra("LandingListModel");
        if (model.isToday()) {
            stoRef = cmn.stoRef(activity).child("WastebinMonitorImages/" + dateTimeUtil.getYear() + "/" + dateTimeUtil.getMonth() + "/" + dateTimeUtil.getTodayDate());
            rdmsRef = cmn.rdbmsRef(activity).child("WastebinMonitor/ImagesData/" + dateTimeUtil.getYear() + "/" + dateTimeUtil.getMonth() + "/" + dateTimeUtil.getTodayDate());
        } else {
            stoRef = cmn.stoRef(activity).child("WastebinMonitorImages/" + dateTimeUtil.getyYear() + "/" + dateTimeUtil.getyMonth() + "/" + dateTimeUtil.getYDate());
            rdmsRef = cmn.rdbmsRef(activity).child("WastebinMonitor/ImagesData/" + dateTimeUtil.getyYear() + "/" + dateTimeUtil.getyMonth() + "/" + dateTimeUtil.getYDate());
        }
        inIt();
        new Thread(()->{
            attachListener();
        }).start();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void inIt() {
        addressTv.set(model.getAdd());
        timeTv.set(model.getTime());
        callImageMethod(stoRef.child(model.getImageNm()).toString(), true);
    }

    public void mapClick() {
        if (model.getLatlng().length() > 1) {
            try {
                Uri gmmIntentUri = Uri.parse("geo:" + model.getLatlng() + "?q=" + model.getLatlng() + "(Label+Name)");
                Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                activity.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(activity, "Location Not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void cleanClick() {
        isCleanBtnVisible.set(false);
        isDirtyBtnVisible.set(true);
        headingTv.set("Resolved -");
        timeTv.set(model.getActionTime().equals("null") ? "--" : model.getActionTime());
        if (resolvedViewUrl.get() == null) {
            callImageMethod(stoRef.child(model.getActionImageRef()).toString(), false);
        } else {
            imageViewUrl.set(resolvedViewUrl.get());
        }
    }

    public void dirtyClick() {
        isDirtyBtnVisible.set(false);
        isCleanBtnVisible.set(true);
        headingTv.set("Complaint -");
        timeTv.set(model.getTime());
        if (complaintViewUrl.get() == null) {
            callImageMethod(stoRef.child(model.getImageNm()).toString(), true);
        } else {
            imageViewUrl.set(complaintViewUrl.get());
        }
    }

    private void callImageMethod(String ref, boolean isComplaint) {
        cmn.setProgressDialog("Please wait...", "Image loading...", activity);
        repository.downloadAndShowImage(ref, cmn, activity).observe((LifecycleOwner) activity, response -> {
            if (isComplaint) {
                complaintViewUrl.set(response);
            } else {
                resolvedViewUrl.set(response);
            }
            imageViewUrl.set(response);
            cmn.closeDialog(activity);
        });
    }

    @BindingAdapter({"imageUrl"})
    public static void loadImage(ImageView view, Bitmap bitmap) {
        if (bitmap == null) {
            view.setImageResource(R.drawable.img_not_available);
        } else {
            view.setImageBitmap(bitmap);
        }
    }

    private void attachListener() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String[] latlng = model.getLatlng().split(",");
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult.getLocations().size() > 0) {
                    lastKnownLocation = locationResult.getLastLocation();
                    distance = cmn.distance(Float.parseFloat(latlng[0].trim()),
                            Float.parseFloat(latlng[1].trim()),
                            Float.parseFloat(String.valueOf(lastKnownLocation.getLatitude())),
                            Float.parseFloat(String.valueOf(lastKnownLocation.getLongitude())));
                }
            }
        };
        LocationRequest locationRequest = new LocationRequest().setInterval(1000).setFastestInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.getFusedLocationProviderClient(activity).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void checkGps() {
        LocationRequest locReq = new LocationRequest().setInterval(5000).setFastestInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest locReqSet = new LocationSettingsRequest.Builder().addLocationRequest(locReq).setAlwaysShow(true).setNeedBle(true).build();
        LocationServices.getSettingsClient(activity).checkLocationSettings(locReqSet).addOnCompleteListener(task1 -> {
            try {
                task1.getResult(ApiException.class);
                if (task1.isSuccessful()) {
                    cmn.captureDialog(activity).observe((LifecycleOwner) activity,response->{
                        photo=response;
                        if (photo != null) {
                            repository.saveData(activity,cmn,dateTimeUtil,model,lastKnownLocation,preferences,stoRef,rdmsRef,photo);
                        } else {
                            Toast.makeText(activity, "Please Retry", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    cmn.closeDialog(activity);
                    Toast.makeText(activity, "Please Retry", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                cmn.closeDialog(activity);
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(activity, 0002);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public void captureClick() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(activity, "Please allow permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(distance <= cmn.getFiltersPref(activity).getFloat("LocMatDisValForBvgApp", 20000))) {
            Toast.makeText(activity, "You are not in range", Toast.LENGTH_SHORT).show();
            return;
        }
        cmn.setProgressDialog("Please wait...","",activity);
        checkGps();
    }
}
