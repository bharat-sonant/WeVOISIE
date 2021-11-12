package com.wevois.application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wevois.application.Interface.SwipeListenerInterface;
import com.wevois.application.Model.LandingListModel;
import com.wevois.application.Utilities.CommonMethods;
import com.wevois.application.Utilities.DateTimeUtilities;
import com.wevois.application.Utilities.OnSwipeTouchListener;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

public class ComplaintActivity extends AppCompatActivity implements SwipeListenerInterface {
    LandingListModel model;
    CommonMethods cmn;
    DateTimeUtilities dateTimeUtil;
    ImageView imageHolderIV;
    TextView imageHeadingTv, timeTv;
    AlertDialog captureDialog;
    public static final int FOCUS_AREA_SIZE = 300;
    Camera mCamera;
    Camera.PictureCallback pictureCallback;
    Bitmap photo;
    LocationCallback locationCallback;
    Location lastKnownLocation;
    LocationSettingsRequest locReqSet;
    float distance;
    StorageReference stoRef;
    DatabaseReference rdmsRef;
    ImageButton dirtyIb, cleanIb;
    boolean isPass = true;
    AlertDialog permissionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);
        cmn = new CommonMethods(ComplaintActivity.this);
        dateTimeUtil = new DateTimeUtilities();
        model = (LandingListModel) getIntent().getSerializableExtra("LandingListModel");
        if (model.isToday()) {
            stoRef = cmn.stoRef().child("WastebinMonitorImages/" + dateTimeUtil.getYear() + "/" + dateTimeUtil.getMonth() + "/" + dateTimeUtil.getTodayDate());
            rdmsRef = cmn.rdbmsRef().child("WastebinMonitor/ImagesData/" + dateTimeUtil.getYear() + "/" + dateTimeUtil.getMonth() + "/" + dateTimeUtil.getTodayDate());
        } else {
            stoRef = cmn.stoRef().child("WastebinMonitorImages/" + dateTimeUtil.getyYear() + "/" + dateTimeUtil.getyMonth() + "/" + dateTimeUtil.getYDate());
            rdmsRef = cmn.rdbmsRef().child("WastebinMonitor/ImagesData/" + dateTimeUtil.getyYear() + "/" + dateTimeUtil.getyMonth() + "/" + dateTimeUtil.getYDate());
        }
        new Thread(this::setPageTitle).start();
        inIt();
        initializePermissionDialog();
        runOnUiThread(() -> downloadAndShowImage(model.getImageNm(), dirtyIb));
        runOnUiThread(this::attachListener);
    }

    private void setPageTitle() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_arrow);
        toolbar.setNavigationOnClickListener(v -> {
            super.onBackPressed();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void inIt() {
        imageHolderIV = findViewById(R.id.complaint_image_view);
        dirtyIb = findViewById(R.id.dirty_image_btn);
        cleanIb = findViewById(R.id.clean_image_btn);
        imageHeadingTv = findViewById(R.id.image_heading_tv);
        timeTv = findViewById(R.id.time_tv);
        TextView addressTv = findViewById(R.id.address_tv);
        ImageButton captureImageBtn = findViewById(R.id.capture_image_btn);
        ImageButton locBtn = findViewById(R.id.loc_btn);
        addressTv.setText(model.getAdd());
        timeTv.setText(model.getTime());

        dirtyIb.setOnClickListener(view -> {
            view.setVisibility(View.GONE);
            imageHeadingTv.setText("Complaint -");
            timeTv.setText(model.getTime());
            downloadAndShowImage(model.getImageNm(), view);
        });

        cleanIb.setOnClickListener(view -> {
            view.setVisibility(View.GONE);
            imageHeadingTv.setText("Resolved -");
            timeTv.setText(model.getActionTime().equals("null") ? "--" : model.getActionTime());
            downloadAndShowImage(model.getActionImageRef(), view);
        });

        locBtn.setOnClickListener(view -> {
            if (model.getLatlng().length() > 1) {
                try {
                    Uri gmmIntentUri = Uri.parse("geo:" + model.getLatlng() + "?q=" + model.getLatlng() + "(Label+Name)");
                    Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(ComplaintActivity.this, "Location Not available", Toast.LENGTH_SHORT).show();
            }
        });

        initializeCam();

        captureImageBtn.setOnClickListener(view -> {
            if (isPass) {
                isPass = false;
                checkGps();
            }
        });

        imageHolderIV.setOnTouchListener(new OnSwipeTouchListener(ComplaintActivity.this, this));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeCam() {
        try {
            View dialogLayout = ComplaintActivity.this.getLayoutInflater().inflate(R.layout.custom_camera_alertbox, null);
            captureDialog = new AlertDialog.Builder(ComplaintActivity.this).setView(dialogLayout).setCancelable(false).create();
            SurfaceView surfaceView = (SurfaceView) dialogLayout.findViewById(R.id.surfaceViews);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_HARDWARE);
            SurfaceHolder.Callback surfaceViewCallBack = new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        mCamera = Camera.open();
                        Camera.Parameters parameters = mCamera.getParameters();
                        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
                        parameters.setPictureSize(sizes.get(0).width, sizes.get(0).height);
                        mCamera.setParameters(parameters);
                        cmn.setCameraDisplayOrientation(ComplaintActivity.this, 0, mCamera);
                        mCamera.setPreviewDisplay(surfaceHolder);
                        mCamera.startPreview();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                }
            };
            surfaceHolder.addCallback(surfaceViewCallBack);

            dialogLayout.findViewById(R.id.capture_image_btn)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isPass) {
                                isPass = false;
                                mCamera.takePicture(null, null, null, pictureCallback);
                            }

                        }
                    });

            dialogLayout.findViewById(R.id.close_image_btn).setOnClickListener(v -> {
                captureDialog.cancel();
            });
            captureDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            surfaceView.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        cmn.focusOnTouch(motionEvent, mCamera, surfaceView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void captureDialog() {
        try {
            isPass = true;
            if (ContextCompat.checkSelfPermission(ComplaintActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(ComplaintActivity.this, "Please allow permissions", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!(distance <= cmn.getFiltersPref().getFloat("LocMatDisValForBvgApp", 20000))) {
                Toast.makeText(ComplaintActivity.this, "You are not in range", Toast.LENGTH_SHORT).show();
                return;
            }
            cmn.closeAlertDialog(captureDialog);
            captureDialog.show();
            pictureCallback = (bytes, camera) -> {
                cmn.setProgressDialog("", "Please wait saving data", ComplaintActivity.this);
                isPass = true;
                photo = cmn.returnScaledBitmap(bytes);
                if (photo != null) {
                    saveData();
                } else {
                    Toast.makeText(this, "Please Retry", Toast.LENGTH_SHORT).show();
                }
                captureDialog.cancel();
                camera.stopPreview();
                if (camera != null) {
                    camera.release();
                    mCamera = null;
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadAndShowImage(String imageName, View view) {
        try {
            if (!(imageName.trim().length() > 1) || imageName.equals("null")) {
                if (view.getId() == dirtyIb.getId()) {
                    cleanIb.setVisibility(View.VISIBLE);
                } else {
                    dirtyIb.setVisibility(View.VISIBLE);
                }
                imageHolderIV.setScaleType(ImageView.ScaleType.CENTER);
                imageHolderIV.setImageResource(R.drawable.img_not_available);
                return;
            }
            cmn.setProgressDialog("", "Loading Image", ComplaintActivity.this);
            FirebaseStorage.getInstance()
                    .getReferenceFromUrl(stoRef.child(imageName).toString())
                    .getBytes(1024 * 1024)
                    .addOnSuccessListener(bytes -> {
                        imageHolderIV.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageHolderIV.setImageBitmap(cmn.bytesTOBitmap(bytes));
                        if (view.getId() == dirtyIb.getId()) {
                            cleanIb.setVisibility(View.VISIBLE);
                        } else {
                            dirtyIb.setVisibility(View.VISIBLE);
                        }
                        cmn.closeDialog(ComplaintActivity.this);
                    }).addOnFailureListener(e -> {
                cmn.closeDialog(ComplaintActivity.this);
                if (view.getId() == dirtyIb.getId()) {
                    cleanIb.setVisibility(View.VISIBLE);
                } else {
                    dirtyIb.setVisibility(View.VISIBLE);
                }
                imageHolderIV.setScaleType(ImageView.ScaleType.CENTER);
                imageHolderIV.setImageResource(R.drawable.img_not_available);
            });
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private void attachListener() {
        instantiateLocReq();
        if (ContextCompat.checkSelfPermission(ComplaintActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void instantiateLocReq() {
        LocationRequest locReq = new LocationRequest().setInterval(5000).setFastestInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locReqSet = new LocationSettingsRequest.Builder()
                .addLocationRequest(locReq)
                .setAlwaysShow(true).setNeedBle(true).build();
    }

    private void checkGps() {
        LocationServices.getSettingsClient(this).checkLocationSettings(locReqSet)
                .addOnCompleteListener(task1 -> {
                    try {
                        task1.getResult(ApiException.class);
                        if (task1.isSuccessful()) {
                            captureDialog();
                        } else {
                            Toast.makeText(ComplaintActivity.this, "Please Retry", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ApiException e) {
                        cmn.closeDialog(ComplaintActivity.this);
                        if (e instanceof ResolvableApiException) {
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(this, 0002);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void saveData() {
        ByteArrayOutputStream bosUpload = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bosUpload);
        stoRef.child(model.getImageNm() + "~RESOLVED").putBytes(bosUpload.toByteArray())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HashMap<String, Object> mapToUpload = new HashMap<>();
                        mapToUpload.put("date", dateTimeUtil.getTodayDate());
                        mapToUpload.put("time", dateTimeUtil.getCurrentTime());
                        mapToUpload.put("latlng", lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude());
                        mapToUpload.put("imageRef", model.getImageNm() + "~RESOLVED");
                        mapToUpload.put("user", cmn.getLoginPref().getString("uid", ""));
                        rdmsRef.child(model.getCategory() + "/" + model.getSerialNm() + "/BvgAction").setValue(mapToUpload).addOnCompleteListener(taskRDBMS -> {
                            if (taskRDBMS.isSuccessful()) {
                                cmn.closeDialog(ComplaintActivity.this);
                                Toast.makeText(ComplaintActivity.this, "Data saved Successfully", Toast.LENGTH_SHORT).show();
                                model.setActionDate(dateTimeUtil.getTodayDate());
                                model.setActionTime(dateTimeUtil.getCurrentTime());
                                model.setActionLatLng(lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude());
                                model.setActionImageRef(model.getImageNm() + "~RESOLVED");
                                model.setActionUser(cmn.getLoginPref().getString("uid", ""));
                            }
                        });
                    }
                }).addOnFailureListener(e -> {
            cmn.closeDialog(ComplaintActivity.this);
            Toast.makeText(ComplaintActivity.this, "Image saving failed", Toast.LENGTH_SHORT).show();
        });

    }

    private void initializePermissionDialog() {
        View diaLayout = this.getLayoutInflater().inflate(R.layout.info_dialog_layout, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).setView(diaLayout).setCancelable(false);
        permissionDialog = alertDialog.create();
        diaLayout.findViewById(R.id.accept_dialog_btn).setOnClickListener(v -> {
            permissionDialog.dismiss();
            cmn.intentToAppInfo();
        });
        permissionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }

    private void infoDialog() {
        try {
            permissionDialog.dismiss();
            permissionDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(ComplaintActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            infoDialog();
        }
    }

    @Override
    protected void onDestroy() {
        if (locationCallback != null) {
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0002 && resultCode == RESULT_OK) {
            captureDialog();
        } else {
            isPass = true;
        }
    }

    @Override
    public void swipeLTR() {
        if (cleanIb.getVisibility() == View.VISIBLE) {
            cleanIb.setVisibility(View.GONE);
            imageHeadingTv.setText("Resolved -");
            timeTv.setText(model.getActionTime().equals("null") ? "--" : model.getActionTime());
            downloadAndShowImage(model.getActionImageRef(), cleanIb);
        }
    }

    @Override
    public void swipeRTL() {
        if (dirtyIb.getVisibility() == View.VISIBLE) {
            dirtyIb.setVisibility(View.GONE);
            imageHeadingTv.setText("Complaint -");
            timeTv.setText(model.getTime());
            downloadAndShowImage(model.getImageNm(), dirtyIb);
        }
    }
}