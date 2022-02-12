package com.wevois.application.Utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wevois.application.R;
import com.wevois.application.Views.Home;
import com.wevois.application.Views.Login;

import java.util.ArrayList;
import java.util.List;

public class CommonMethods {
    public ProgressDialog dialog;
    private static CommonMethods single_instance = null;
    private SurfaceView surfaceView;
    SurfaceHolder.Callback surfaceViewCallBack;
    Camera.PictureCallback pictureCallback;
    static boolean isCameraClosed = true;
    AlertDialog captureDialog;
    private Camera mCamera;
    private static final int FOCUS_AREA_SIZE = 300;

    private CommonMethods() {
    }

    public static CommonMethods getInstance() {
        if (single_instance == null) {
            single_instance = new CommonMethods();
        }
        return single_instance;
    }

    public SharedPreferences getFiltersPref(Context context) {
        return context.getSharedPreferences("filters_data", Context.MODE_PRIVATE);
    }

    public SharedPreferences getLoginPref(Context context) {
        return context.getSharedPreferences("login_details", Context.MODE_PRIVATE);
    }

    public DatabaseReference rdbmsRef(Context context) {
        return FirebaseDatabase.getInstance(context.getSharedPreferences("db_util", Context.MODE_PRIVATE).getString("dbRef", " ")).getReference();
    }

    public StorageReference stoRef(Activity activity) {
        return FirebaseStorage.getInstance().getReferenceFromUrl(activity.getSharedPreferences("db_util", Context.MODE_PRIVATE).getString("stoRef", ""));
    }

    @SuppressLint("ObsoleteSdkInt")
    public void setProgressDialog(String title, String message, Activity activity) {
        closeDialog(activity);
        dialog = new ProgressDialog(activity);
        dialog.setTitle(title);
        dialog.setMessage(message);
        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.create();
        }
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if (!dialog.isShowing() && !activity.isFinishing()) {
            dialog.show();
        }
    }

    public void closeDialog(Activity activity) {
        try {
            if (dialog != null) {
                if (dialog.isShowing() && !activity.isFinishing()) {
                    dialog.dismiss();
                }
            }
        }catch (Exception e){}
    }

    public void setUpCheckBox(CheckBox cb) {
        if (cb.isChecked()) {
            cb.setChecked(false);
        } else {
            cb.setChecked(true);
        }
    }

    public void intentToAppInfo(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    public void closeAlertDialog(AlertDialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void focusOnTouch(MotionEvent event) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0) {
                Rect rect = calculateFocusArea(event.getX(), event.getY());

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);

                mCamera.setParameters(parameters);
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            } else {
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }

    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = (success, camera) -> {
        if (success) {
        } else {
        }
    };

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / surfaceView.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / surfaceView.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public void finishCamera(){
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
            }
        }catch (Exception e){
        }
    }

    public Bitmap returnScaledBitmap(byte[] bytes) {
        Bitmap b = bytesTOBitmap(bytes);
        Matrix matrix = new Matrix();
        matrix.postRotate(90F);
        b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
        return Bitmap.createScaledBitmap(b, 512,
                (int) (b.getHeight() * (512.0 / b.getWidth())), false);
    }

    public Bitmap bytesTOBitmap(byte[] bytes){
        return  BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public float distance(float lat_a, float lng_a, float lat_b, float lng_b) {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }

    public void fetchWastebinMonitorSettings(Activity activity) {
        try {
            rdbmsRef(activity).child("Settings/WastebinMonitorApplicationSettings").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue()!=null){
                        if (snapshot.hasChild("notificationMessage")) {
                            activity.getSharedPreferences("login_details", Context.MODE_PRIVATE).edit().putString("notificationMessage", snapshot.child("notificationMessage").getValue().toString()).apply();
                        }
                        if (snapshot.hasChild("notificationTitle")) {
                            activity.getSharedPreferences("login_details", Context.MODE_PRIVATE).edit().putString("notificationTitle", snapshot.child("notificationTitle").getValue().toString()).apply();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LiveData<Boolean> permissionDialog(Activity activity) {
        MutableLiveData<Boolean> isAllow = new MutableLiveData<>();
        View dialog = activity.getLayoutInflater().inflate(R.layout.info_dialog_layout, null);
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(activity).setView(dialog).setCancelable(false);
        AlertDialog infoDialog = alertDialog.create();
        dialog.findViewById(R.id.accept_dialog_btn).setOnClickListener(v -> {
            infoDialog.dismiss();
            isAllow.setValue(true);
            intentToAppInfo(activity);
        });
        infoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        try {
            infoDialog.dismiss();
            infoDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isAllow;
    }

    public void proceed(Activity activity) {
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (getLoginPref(activity).getString("uid", "").trim().length() > 1) {
                    activity.startActivity(new Intent(activity, Home.class));
                } else {
                    activity.startActivity(new Intent(activity, Login.class));
                }
                activity.finish();
            }
        }.start();
    }

    @SuppressLint("StaticFieldLeak")
    public LiveData<Bitmap> captureDialog(Activity activity) {
        MutableLiveData<Bitmap> response = new MutableLiveData<>();
        try {
            closeDialog(activity);
            if (captureDialog != null) {
                captureDialog.dismiss();
            }
        } catch (Exception e) {
        }
        isCameraClosed = true;
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.custom_camera_alertbox, null);
        captureDialog = new AlertDialog.Builder(activity).setView(dialogLayout).setCancelable(false).create();
        surfaceView = (SurfaceView) dialogLayout.findViewById(R.id.surfaceViews);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_HARDWARE);
        surfaceViewCallBack = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    mCamera = Camera.open();
                } catch (RuntimeException e) {
                }
                Camera.Parameters parameters;
                parameters = mCamera.getParameters();
                List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
                parameters.setPictureSize(sizes.get(0).width, sizes.get(0).height);
                mCamera.setParameters(parameters);
                setCameraDisplayOrientation(activity, 0, mCamera);
                try {
                    mCamera.setPreviewDisplay(surfaceHolder);
                    mCamera.startPreview();
                } catch (Exception e) {
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
        Button btn = dialogLayout.findViewById(R.id.capture_image_btn);
        btn.setOnClickListener(v -> {
            try {
                if (isCameraClosed) {
                    isCameraClosed = false;
                    setProgressDialog("", "Uploading...", activity);
                    new AsyncTask<Void, Void, Boolean>() {

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }


                        @Override
                        protected Boolean doInBackground(Void... p) {
                            mCamera.takePicture(null, null, null, pictureCallback);
                            return null;
                        }
                    }.execute();
                }
            } catch (Exception e) {
            }
        });
        Button closeBtn = dialogLayout.findViewById(R.id.close_image_btn);
        captureDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        closeBtn.setOnClickListener(v -> {
            closeDialog(activity);
            response.setValue(null);
            try {
                if (captureDialog != null) {
                    captureDialog.dismiss();
                }
            } catch (Exception e) {
            }
        });

        if (!activity.isFinishing()) {
            captureDialog.show();
        }

        pictureCallback = (bytes, camera) -> {
            Bitmap thumbnail = returnScaledBitmap(bytes);
            camera.stopPreview();
            if (camera != null) {
                camera.release();
                mCamera = null;
            }
            try {
                if (captureDialog != null) {
                    captureDialog.dismiss();
                }
            } catch (Exception e) {
            }
            closeDialog(activity);
            response.setValue(thumbnail);
        };

        surfaceView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                focusOnTouch(motionEvent);
            }
            return false;
        });
        return response;
    }
}
