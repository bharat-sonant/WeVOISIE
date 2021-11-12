package com.wevois.application.Utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wevois.application.ComplaintActivity;

import java.util.ArrayList;
import java.util.List;

public class CommonMethods {
    private final SharedPreferences dbPref, loginPref, filtersPref;
    public ProgressDialog dialog;
    Context context;
    Matrix matrix;

    public CommonMethods(Context context) {
        this.context = context;
        dbPref = context.getSharedPreferences("db_util", Context.MODE_PRIVATE);
        loginPref = context.getSharedPreferences("login_details", Context.MODE_PRIVATE);
        filtersPref = context.getSharedPreferences("filters_data", Context.MODE_PRIVATE);
        matrix = new Matrix();
        matrix.postRotate(90F);
    }

    public SharedPreferences getFiltersPref() {
        return filtersPref;
    }

    public SharedPreferences getLoginPref() {
        return loginPref;
    }

    public DatabaseReference rdbmsRef() {
        return FirebaseDatabase.getInstance(dbPref.getString("dbRef", " ")).getReference();
    }

    public StorageReference stoRef() {
        return FirebaseStorage.getInstance().getReferenceFromUrl(dbPref.getString("stoRef", ""));
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
        if (dialog != null) {
            if (dialog.isShowing() && !activity.isFinishing()) {
                dialog.dismiss();
            }
        }
    }

    public void setUpCheckBox(CheckBox cb) {
        if (cb.isChecked()) {
            cb.setChecked(false);
        } else {
            cb.setChecked(true);
        }
    }

    public void intentToAppInfo() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public void closeAlertDialog(AlertDialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private Rect calculateFocusArea(float x, float y, SurfaceView surfaceView) {
        int left = clamp(Float.valueOf((x / surfaceView.getWidth()) * 2000 - 1000).intValue());
        int top = clamp(Float.valueOf((y / surfaceView.getHeight()) * 2000 - 1000).intValue());

        return new Rect(left, top, left + ComplaintActivity.FOCUS_AREA_SIZE, top + ComplaintActivity.FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCam) {
        if (Math.abs(touchCoordinateInCam) + ComplaintActivity.FOCUS_AREA_SIZE / 2 > 1000) {
            if (touchCoordinateInCam > 0) {
                return 1000 - ComplaintActivity.FOCUS_AREA_SIZE / 2;
            } else {
                return -1000 + ComplaintActivity.FOCUS_AREA_SIZE / 2;
            }
        } else {
            return touchCoordinateInCam - ComplaintActivity.FOCUS_AREA_SIZE / 2;
        }
    }

    public void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
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
            result = (360 - (info.orientation + degrees) % 360) % 360;
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public void focusOnTouch(MotionEvent event, Camera mCamera, SurfaceView surfaceView) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                Rect rect = calculateFocusArea(event.getX(), event.getY(), surfaceView);
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);
                mCamera.setParameters(parameters);
            }
            mCamera.autoFocus((success, camera) -> {

            });

        }
    }

    public Bitmap returnScaledBitmap(byte[] bytes) {
        Bitmap b = bytesTOBitmap(bytes);
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

}
