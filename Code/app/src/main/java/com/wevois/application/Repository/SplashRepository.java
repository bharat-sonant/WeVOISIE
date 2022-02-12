package com.wevois.application.Repository;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import com.wevois.application.Utilities.CommonMethods;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

public class SplashRepository {
    CommonMethods cmn;
    Activity activity;

    public SplashRepository(CommonMethods cmn, Activity activity) {
        this.cmn = cmn;
        this.activity = activity;
    }

    @SuppressLint("StaticFieldLeak")
    public void fetchWastebinTypes() {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... p) {
                try {
                    cmn.stoRef(activity).child("/Defaults/ImageOptionTypes.json").getMetadata().addOnSuccessListener(storageMetadata -> {
                        long fileCreationTime = storageMetadata.getCreationTimeMillis();
                        long fileDownloadTime = cmn.getFiltersPref(activity).getLong("ImageOptionTypesDownloadTime", 0);
                        if (fileDownloadTime != fileCreationTime) {
                            cmn.stoRef(activity).child("/Defaults/ImageOptionTypes.json")
                                    .getBytes(10000000)
                                    .addOnSuccessListener(taskSnapshot -> {

                                        try {
                                            String str = new String(taskSnapshot, StandardCharsets.UTF_8);
                                            JSONArray obj = new JSONArray(str);
                                            ArrayList<String> al_en = new ArrayList<>();
                                            for (int i = 0; i < obj.length(); i++) {
                                                if (!obj.get(i).toString().equals("null")) {
                                                    JSONObject o = new JSONObject(obj.get(i).toString());
                                                    al_en.add(i + "~" + o.get("en").toString());
                                                }
                                            }
                                            cmn.getFiltersPref(activity).edit().putString("wastebin_types_en", al_en.toString()).apply();
                                            cmn.getFiltersPref(activity).edit().putLong("ImageOptionTypesDownloadTime", fileCreationTime).apply();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void fetchZonesAndWards() {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... p) {
                try {
                    cmn.stoRef(activity).child("/Defaults/zones.json").getMetadata().addOnSuccessListener(storageMetadata -> {
                        long fileCreationTime = storageMetadata.getCreationTimeMillis();
                        long fileDownloadTime = cmn.getFiltersPref(activity).getLong("zonesDownloadTime", 0);
                        if (fileDownloadTime != fileCreationTime) {
                            cmn.stoRef(activity).child("/Defaults/zones.json")
                                    .getBytes(10000000)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        try {
                                            String str = new String(taskSnapshot, StandardCharsets.UTF_8);
                                            JSONObject zonesWardObject = new JSONObject(str);
                                            ArrayList<String> zonesAl = new ArrayList<>();
                                            zonesAl.add("All Zone");
                                            Iterator<String> iterator = zonesWardObject.keys();
                                            while (iterator.hasNext()) {
                                                String key = iterator.next();
                                                zonesAl.add(key);
                                            }
                                            cmn.getFiltersPref(activity).edit().putString("zonesJSON", zonesWardObject.toString()).apply();
                                            cmn.getFiltersPref(activity).edit().putString("zonesAl", zonesAl.toString()).apply();
                                            cmn.getFiltersPref(activity).edit().putLong("zonesDownloadTime", fileCreationTime).apply();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void fetchLocMatchVal() {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... p) {
                try {
                    cmn.stoRef(activity).child("Defaults/LocMatDisValForBvgApp.json").getMetadata().addOnSuccessListener(storageMetadata -> {
                        long fileCreationTime = storageMetadata.getCreationTimeMillis();
                        long fileDownloadTime = cmn.getFiltersPref(activity).getLong("LocMatDisValForBvgAppDownloadTime", 0);
                        if (fileDownloadTime != fileCreationTime) {
                            cmn.stoRef(activity).child("Defaults/LocMatDisValForBvgApp.json").getBytes(10000000).addOnSuccessListener(taskSnapshot -> {
                                try {
                                    String str = new String(taskSnapshot, StandardCharsets.UTF_8);
                                    cmn.getFiltersPref(activity).edit().putFloat("LocMatDisValForBvgApp", Float.parseFloat(str)).apply();
                                    cmn.getFiltersPref(activity).edit().putLong("LocMatDisValForBvgAppDownloadTime", fileCreationTime).apply();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }
}
