package com.wevois.application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.wevois.application.Adapter.ParentLVAdapter;
import com.wevois.application.Adapter.TypesLVAdapter;
import com.wevois.application.Interface.TypeLvInterface;
import com.wevois.application.Model.LandingListModel;
import com.wevois.application.Model.TypesListModel;
import com.wevois.application.Utilities.CommonMethods;
import com.wevois.application.Utilities.DateTimeUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class LandingActivity extends AppCompatActivity implements TypeLvInterface{

    CommonMethods cmn;
    ListView parentLv;
    DateTimeUtilities dateUt;
    String zone = "", ward = "";
    int category = 2, tempCat = 2;
    TypesLVAdapter typesLVAdapter;
    ParentLVAdapter parentAdapter;
    TextView pendingTv, completedTv;
    BottomSheetDialog bottomSheetDialog;
    ArrayList<String> al_en = new ArrayList<>();
    ArrayList<String> zonesAl = new ArrayList<>();
    ArrayList<TypesListModel> typeListAl = new ArrayList<>();
    ArrayList<LandingListModel> landingListAl = new ArrayList<>();
    ArrayList<LandingListModel> completeListAl = new ArrayList<>();
    ArrayList<LandingListModel> pendingListAl = new ArrayList<>();
    ArrayAdapter<String> spinnerArrAdapterForZones, spinnerArrAdapterForWards;
    JSONObject zonesWardObject = new JSONObject();
    boolean isPendingSelected = true, isTodayCbSelected = true, isYesterdayCbSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        setPageTitle();
        inIt();
    }

    private void setPageTitle() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void inIt() {
        cmn = new CommonMethods(LandingActivity.this);
        dateUt = new DateTimeUtilities();
        cmn.setProgressDialog("", "Please wait checking types", LandingActivity.this);
        pendingTv = findViewById(R.id.pending_tv);
        completedTv = findViewById(R.id.complete_tv);
        parentLv = findViewById(R.id.parent_listview);
        fetchWastebinTypes();
        pendingTv.setOnClickListener(view -> {
            isPendingSelected = true;
            setUpPendingCompleteFilter();
        });
        completedTv.setOnClickListener(view -> {
            isPendingSelected = false;
            setUpPendingCompleteFilter();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        penComSelection();
        fetchDb();
    }

    private void fetchDb() {
        cmn.setProgressDialog("", "Please Wait fetching data", LandingActivity.this);
        landingListAl.clear();
        pendingListAl.clear();
        completeListAl.clear();
        if (isYesterdayCbSelected && isTodayCbSelected) {
            downloadHelper(dateUt.getyYear(), dateUt.getyMonth(), dateUt.getYDate(), dateUt.getYear(), dateUt.getMonth(), dateUt.getTodayDate());
        } else {
            if (isTodayCbSelected) {
                downloadHelper(dateUt.getYear(), dateUt.getMonth(), dateUt.getTodayDate());
            } else {
                downloadHelper(dateUt.getyYear(), dateUt.getyMonth(), dateUt.getYDate());
            }
        }
    }

    private void downloadHelper(String year, String month, String date) {
        cmn.rdbmsRef().child("WastebinMonitor/ImagesData/" + year + "/" + month + "/" + date)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            filterDataForModel(snapshot, true);
                        } else {
                            setUpPendingCompleteFilter();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void downloadHelper(String yYear, String yMonth, String yDate, String year, String month, String date) {
        cmn.rdbmsRef().child("WastebinMonitor/ImagesData/" + yYear + "/" + yMonth + "/" + yDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot yesterdaySnapshot) {
                        cmn.rdbmsRef().child("WastebinMonitor/ImagesData/" + year + "/" + month + "/" + date)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (yesterdaySnapshot.getValue() == null && snapshot.getValue() == null) {
                                            setUpPendingCompleteFilter();
                                            return;
                                        }
                                        if (yesterdaySnapshot.getValue() != null) {
                                            filterDataForModel(yesterdaySnapshot, false);
                                        }
                                        if (snapshot.getValue() != null) {
                                            filterDataForModel(snapshot, true);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void filterDataForModel(DataSnapshot snap, boolean isToday) {
        runOnUiThread(() -> {
            for (DataSnapshot dataSnapshot : snap.child(category + "").getChildren()) {
                if (zone.length() > 1) {
                    if (zone.trim().equalsIgnoreCase(String.valueOf(dataSnapshot.child("zone").getValue()))) {
                        if (ward.length() > 1) {
                            if (ward.trim().equalsIgnoreCase(String.valueOf(dataSnapshot.child("ward").getValue()))) {
                                addDataToModel(dataSnapshot, snap, isToday);
                            }
                        } else {
                            addDataToModel(dataSnapshot, snap, isToday);
                        }
                    }
                } else {
                    addDataToModel(dataSnapshot, snap, isToday);
                }
            }
            setUpPendingCompleteFilter();
        });
    }

    private void addDataToModel(DataSnapshot dataSnapshot, DataSnapshot snap, boolean isToday) {
        if (dataSnapshot.hasChild("imageRef") &&
                dataSnapshot.hasChild("latLng") &&
                dataSnapshot.hasChild("locality") &&
                dataSnapshot.hasChild("time") &&
                dataSnapshot.hasChild("ward") &&
                dataSnapshot.hasChild("zone") &&
                dataSnapshot.hasChild("address")) {

            if (dataSnapshot.hasChild("BvgAction")) {
                completeListAl.add(new LandingListModel(snap.getKey(),
                        String.valueOf(dataSnapshot.child("imageRef").getValue()),
                        String.valueOf(dataSnapshot.child("latLng").getValue()),
                        String.valueOf(dataSnapshot.child("locality").getValue()),
                        String.valueOf(dataSnapshot.child("time").getValue()),
                        String.valueOf(dataSnapshot.child("ward").getValue()),
                        String.valueOf(dataSnapshot.child("zone").getValue()),
                        String.valueOf(dataSnapshot.child("address").getValue()),
                        String.valueOf(dataSnapshot.getKey()),
                        category + "",
                        isToday,
                        String.valueOf(dataSnapshot.child("BvgAction/date").getValue()),
                        String.valueOf(dataSnapshot.child("BvgAction/imageRef").getValue()),
                        String.valueOf(dataSnapshot.child("BvgAction/time").getValue()),
                        String.valueOf(dataSnapshot.child("BvgAction/latlng").getValue()),
                        String.valueOf(dataSnapshot.child("BvgAction/user").getValue())));
            } else {
                pendingListAl.add(new LandingListModel(snap.getKey(),
                        String.valueOf(dataSnapshot.child("imageRef").getValue()),
                        String.valueOf(dataSnapshot.child("latLng").getValue()),
                        String.valueOf(dataSnapshot.child("locality").getValue()),
                        String.valueOf(dataSnapshot.child("time").getValue()),
                        String.valueOf(dataSnapshot.child("ward").getValue()),
                        String.valueOf(dataSnapshot.child("zone").getValue()),
                        String.valueOf(dataSnapshot.child("address").getValue()),
                        String.valueOf(dataSnapshot.getKey()),
                        category + "",
                        isToday));
            }
        }
    }

    public void openFilters(View views) {
        if (bottomSheetDialog == null) bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.dismiss();
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog);
        Spinner zoneSpinner = bottomSheetDialog.findViewById(R.id.zone_spinner);
        Spinner wardSpinner = bottomSheetDialog.findViewById(R.id.ward_spinner);
        LinearLayout tPLayoutCb = bottomSheetDialog.findViewById(R.id.p_t_cb_layout);
        LinearLayout yPLayoutCb = bottomSheetDialog.findViewById(R.id.p_y_cb_layout);
        CheckBox tCb = bottomSheetDialog.findViewById(R.id.t_cb);
        CheckBox yCb = bottomSheetDialog.findViewById(R.id.y_cb);
        ListView typesLv = bottomSheetDialog.findViewById(R.id.types_lv);
        ImageView closeDialog = bottomSheetDialog.findViewById(R.id.close_dialog_iv);
        Button applyFilterBtn = bottomSheetDialog.findViewById(R.id.apply_filter_btn);

        typesLv.setAdapter(typesLVAdapter);
        zoneSpinner.setAdapter(spinnerArrAdapterForZones);
        zoneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (zoneSpinner.getSelectedItemId() != 0) {
                    setUpWardSpinner(zoneSpinner.getSelectedItem().toString(), wardSpinner);
                } else {
                    setUpWardSpinner("null", wardSpinner);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        zoneSpinner.setSelection(zonesAl.indexOf(zone));
        tCb.setChecked(isTodayCbSelected);
        yCb.setChecked(isYesterdayCbSelected);
        tPLayoutCb.setOnClickListener(view -> cmn.setUpCheckBox(tCb));
        yPLayoutCb.setOnClickListener(view -> cmn.setUpCheckBox(yCb));
        closeDialog.setOnClickListener(view -> bottomSheetDialog.dismiss());
        applyFilterBtn.setOnClickListener(view -> {
            if (tCb.isChecked() || yCb.isChecked()) {
                zone = zoneSpinner.getSelectedItemId() == 0 ? zone = "" : (zone = zoneSpinner.getSelectedItem().toString());
                ward = wardSpinner.getSelectedItemId() == 0 ? ward = "" : (ward = wardSpinner.getSelectedItem().toString());
                isTodayCbSelected = tCb.isChecked();
                isYesterdayCbSelected = yCb.isChecked();
                category = tempCat;
                fetchDb();
                bottomSheetDialog.dismiss();
            } else {
                Toast.makeText(LandingActivity.this, "Please Select atleast one day", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();

    }

    public void fetchWastebinTypes() {
        try {
            cmn.stoRef().child("/Defaults/ImageOptionTypes.json").getMetadata().addOnSuccessListener(storageMetadata -> {
                long fileCreationTime = storageMetadata.getCreationTimeMillis();
                long fileDownloadTime = cmn.getFiltersPref().getLong("ImageOptionTypesDownloadTime", 0);
                if (fileDownloadTime != fileCreationTime) {
                    cmn.dialog.setMessage("Downloading Types");
                    cmn.stoRef().child("/Defaults/ImageOptionTypes.json")
                            .getBytes(10000000)
                            .addOnSuccessListener(taskSnapshot -> {
                                try {
                                    String str = new String(taskSnapshot, StandardCharsets.UTF_8);
                                    JSONArray obj = new JSONArray(str);
                                    al_en = new ArrayList<>();
                                    typeListAl = new ArrayList<>();
                                    for (int i = 0; i < obj.length(); i++) {
                                        if (!obj.get(i).toString().equals("null")) {
                                            JSONObject o = new JSONObject(obj.get(i).toString());
                                            al_en.add(i + "~" + o.get("en").toString());
                                            typeListAl.add(new TypesListModel(i, o.get("en").toString(), i == category));

                                        }
                                    }
                                    typesLVAdapter = new TypesLVAdapter(LandingActivity.this, typeListAl, this);
                                    cmn.getFiltersPref().edit().putString("wastebin_types_en", al_en.toString()).apply();
                                    cmn.getFiltersPref().edit().putLong("ImageOptionTypesDownloadTime", fileCreationTime).apply();
                                    fetchZonesAndWards();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                } else {
                    String temp = cmn.getFiltersPref().getString("wastebin_types_en", "");
                    temp = temp.substring(1, temp.length() - 1);
                    String[] tmp = temp.split(", ");
                    for (String str : tmp) {
                        String[] s = str.split("~");
                        typeListAl.add(new TypesListModel(Integer.parseInt(s[0].trim()), s[1].trim(), Integer.parseInt(s[0].trim()) == category));
                    }
                    typesLVAdapter = new TypesLVAdapter(LandingActivity.this, typeListAl, this);
                    cmn.dialog.setMessage("Checking zones");
                    fetchZonesAndWards();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchZonesAndWards() {
        try {
            cmn.stoRef().child("/Defaults/zones.json").getMetadata().addOnSuccessListener(storageMetadata -> {
                long fileCreationTime = storageMetadata.getCreationTimeMillis();
                long fileDownloadTime = cmn.getFiltersPref().getLong("zonesDownloadTime", 0);
                if (fileDownloadTime != fileCreationTime) {
                    cmn.dialog.setMessage("Downloading zones");
                    cmn.stoRef().child("/Defaults/zones.json")
                            .getBytes(10000000)
                            .addOnSuccessListener(taskSnapshot -> {
                                try {
                                    String str = new String(taskSnapshot, StandardCharsets.UTF_8);
                                    zonesWardObject = new JSONObject(str);
                                    zonesAl = new ArrayList<>();
                                    zonesAl.add("All Zone");
                                    Iterator<String> iterator = zonesWardObject.keys();
                                    while (iterator.hasNext()) {
                                        String key = iterator.next();
                                        zonesAl.add(key);
                                    }
                                    cmn.getFiltersPref().edit().putString("zonesJSON", zonesWardObject.toString()).apply();
                                    cmn.getFiltersPref().edit().putString("zonesAl", zonesAl.toString()).apply();
                                    cmn.getFiltersPref().edit().putLong("zonesDownloadTime", fileCreationTime).apply();
                                    setUpZoneSpinner();
                                    cmn.closeDialog(LandingActivity.this);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                } else {
                    try {
                        zonesAl.add("All Zone");
                        String temp = cmn.getFiltersPref().getString("zonesAl", "");
                        temp = temp.substring(1, temp.length() - 1);
                        zonesAl = new ArrayList<>(Arrays.asList(temp.split(",")));
                        zonesWardObject = new JSONObject(cmn.getFiltersPref().getString("zonesJSON", ""));
                        setUpZoneSpinner();
                        cmn.closeDialog(LandingActivity.this);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpZoneSpinner() {
        spinnerArrAdapterForZones = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, zonesAl);
        spinnerArrAdapterForZones.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    private void setUpWardSpinner(String zone, Spinner wardSpinner) {
        try {
            ArrayList<String> tempAl = new ArrayList<>();
            if (!zone.equals("null")) {
                tempAl.add("All Ward");
                JSONArray arr = new JSONArray(zonesWardObject.get(zone.trim()).toString());
                for (int i = 0; i < arr.length(); i++) {
                    if (!arr.get(i).toString().equals("null")) {
                        tempAl.add(arr.get(i).toString());
                    }
                }
            } else {
                tempAl.add("All Ward");
            }
            spinnerArrAdapterForWards = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tempAl);
            spinnerArrAdapterForWards.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            wardSpinner.setAdapter(spinnerArrAdapterForWards);
            wardSpinner.setSelection(tempAl.indexOf(ward));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(int position, TypesListModel model) {
        tempCat = model.getId();
    }

    private void setUpPendingCompleteFilter() {
        penComSelection();
        landingListAl = isPendingSelected ? pendingListAl : completeListAl;
        parentAdapter = new ParentLVAdapter(LandingActivity.this, landingListAl);
        parentLv.setAdapter(parentAdapter);
        cmn.closeDialog(LandingActivity.this);
    }

    private void penComSelection() {
        if (isPendingSelected) {
            pendingTv.setBackground(getResources().getDrawable(R.drawable.selected_background));
            completedTv.setBackground(getResources().getDrawable(R.drawable.unselected_background));
            pendingTv.setTextColor(getResources().getColor(R.color.white));
            completedTv.setTextColor(getResources().getColor(R.color.black));
            pendingTv.setText(Html.fromHtml("<b>Pending</b>"));
            completedTv.setText("Completed");
        } else {
            completedTv.setBackground(getResources().getDrawable(R.drawable.selected_background));
            pendingTv.setBackground(getResources().getDrawable(R.drawable.unselected_background));
            completedTv.setTextColor(getResources().getColor(R.color.white));
            pendingTv.setTextColor(getResources().getColor(R.color.black));
            completedTv.setText(Html.fromHtml("<b>Complete</b>"));
            pendingTv.setText("Pending");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logOut) {
            FirebaseAuth.getInstance().signOut();
            cmn.getLoginPref().edit().putString("uid", "").apply();
            finish();
        }
        return super.onOptionsItemSelected(item);

    }
}