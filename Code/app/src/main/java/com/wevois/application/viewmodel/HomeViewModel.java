package com.wevois.application.viewmodel;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wevois.application.Adapter.ParentRecyclerViewAdapter;
import com.wevois.application.Adapter.TypesLVAdapter;
import com.wevois.application.Interface.TypeLvInterface;
import com.wevois.application.Repository.HomeRepository;
import com.wevois.application.Views.Details;
import com.wevois.application.model.LandingListModel;
import com.wevois.application.R;
import com.wevois.application.Utilities.CommonMethods;
import com.wevois.application.Utilities.DateTimeUtilities;
import com.wevois.application.model.TypesListModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

public class HomeViewModel extends ViewModel implements TypeLvInterface {
    Activity activity;
    CommonMethods cmn;
    BottomSheetDialog bottomSheetDialog;
    DateTimeUtilities dateUt;
    ArrayList<LandingListModel> landingListAl = new ArrayList<>(), completeListAl = new ArrayList<>(), pendingListAl = new ArrayList<>();
    ArrayList<TypesListModel> typeListAl = new ArrayList<>();
    ArrayList<String> zonesAl = new ArrayList<>();
    ArrayAdapter<String> spinnerArrAdapterForZones, spinnerArrAdapterForWards;
    public ObservableField<ParentRecyclerViewAdapter> parentRecyclerViewAdapter = new ObservableField<>();
    TypesLVAdapter typesLVAdapter;
    HomeRepository repository;
    JSONObject zonesWardObject = new JSONObject();
    SharedPreferences preferences;
    public ObservableField<Boolean> pendingTVBackground = new ObservableField<>(true), completedTVBackground = new ObservableField<>(false);
    public ObservableField<Boolean> pendingTVTextColor = new ObservableField<>(true), completedTVTextColor = new ObservableField<>(false);
    public ObservableField<Spanned> pendingTVText = new ObservableField<>(Html.fromHtml("<b>Pending</b>")), completedTVText = new ObservableField<>(Html.fromHtml("Completed"));
    String zone = "", ward = "";
    boolean isPass = true;
    int position = 0, category = 2, tempCat = 2;
    boolean isPendingSelected = true, isTodayCbSelected = true, isYesterdayCbSelected = false;

    public HomeViewModel(Activity activity, RecyclerView recyclerView) {
        this.activity = activity;
        preferences = activity.getSharedPreferences("WeVOISIE", MODE_PRIVATE);
        preferences.edit().putString("intentData", "").apply();
        cmn = CommonMethods.getInstance();
        dateUt = new DateTimeUtilities();
        repository = new HomeRepository(cmn, activity);
        cmn.setProgressDialog("", "Please wait checking types", activity);
        setTypes(cmn.getFiltersPref(activity).getString("wastebin_types_en", ""));
        setZoneAndWard();
        fetchDb(0);
    }

    private void setUpPendingCompleteFilter() {
        penComSelection();
        landingListAl = isPendingSelected ? pendingListAl : completeListAl;
        parentRecyclerViewAdapter.set(new ParentRecyclerViewAdapter(landingListAl, activity, this));
        cmn.closeDialog(activity);
    }

    private void penComSelection() {
        if (isPendingSelected) {
            pendingTVBackground.set(true);
            completedTVBackground.set(false);
            pendingTVTextColor.set(true);
            completedTVTextColor.set(false);
            pendingTVText.set(Html.fromHtml("<b>Pending</b>"));
            completedTVText.set(Html.fromHtml("Completed"));
        } else {
            pendingTVBackground.set(false);
            completedTVBackground.set(true);
            pendingTVTextColor.set(false);
            completedTVTextColor.set(true);
            completedTVText.set(Html.fromHtml("<b>Complete</b>"));
            pendingTVText.set(Html.fromHtml("Pending"));
        }
    }

    private void setTypes(String types) {
        Log.e("HomeView ",types);
        String temp = types;
        temp = temp.substring(1, temp.length() - 1);
        String[] tmp = temp.split(", ");
        for (String str : tmp) {
            String[] s = str.split("~");
            typeListAl.add(new TypesListModel(Integer.parseInt(s[0].trim()), s[1].trim(), Integer.parseInt(s[0].trim()) == category));
        }
        typesLVAdapter = new TypesLVAdapter(activity, typeListAl, this);
        cmn.dialog.setMessage("Checking zones");
    }

    private void setZoneAndWard() {
        try {
//            zonesAl.add("All Zone");
            String temp = cmn.getFiltersPref(activity).getString("zonesAl", "");
            temp = temp.substring(1, temp.length() - 1);
            zonesAl = new ArrayList<>(Arrays.asList(temp.split(",")));
            zonesWardObject = new JSONObject(cmn.getFiltersPref(activity).getString("zonesJSON", ""));
            setUpZoneSpinner();
            cmn.closeDialog(activity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUpZoneSpinner() {
        spinnerArrAdapterForZones = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, zonesAl);
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
            spinnerArrAdapterForWards = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, tempAl);
            spinnerArrAdapterForWards.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            wardSpinner.setAdapter(spinnerArrAdapterForWards);
            wardSpinner.setSelection(tempAl.indexOf(ward));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchDb(int category) {
        cmn.setProgressDialog("", "Please Wait fetching data", activity);
        landingListAl.clear();
        pendingListAl.clear();
        completeListAl.clear();
        if (isTodayCbSelected) {
            fetchData(dateUt.getYear(), dateUt.getMonth(), dateUt.getTodayDate(),category, true);
        }
        if (isYesterdayCbSelected) {
            fetchData(dateUt.getyYear(), dateUt.getyMonth(), dateUt.getYDate(),category, false);
        }
    }

    private void fetchData(String year, String month, String todayDate, int category,boolean b) {
        activity.runOnUiThread(() -> {
            Boolean isToday = b;
            repository.fetchData(activity, year, month, todayDate,category).observe((LifecycleOwner) activity, snapshot -> {
                if (snapshot.getValue() != null) {
                    Log.e("Data URL", snapshot.toString());
                    if (category > 0){
                        filterDataForModel(snapshot, isToday);
                    }else {
                        allDataForModel(snapshot, isToday);
                    }

                } else {
                    setUpPendingCompleteFilter();
                }
            });
        });
    }

    private void allDataForModel(DataSnapshot snap, boolean isToday) {
        activity.runOnUiThread(() -> {
            Log.e("Data URL", snap.toString());
            for (DataSnapshot dataSnapshot : snap.getChildren()) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Log.e("Data URL", "dataSnapshot1 " + dataSnapshot1.child("ward").toString());
                    if (zone.length() > 1) {
//                    if (zone.trim().equalsIgnoreCase(String.valueOf(dataSnapshot.child("zone").getValue()))) {
                        Log.e("Data URL", ward + " " + dataSnapshot1.child("ward").getValue());
                        if (ward.length() > 1) {
                            if (ward.trim().equalsIgnoreCase(String.valueOf(dataSnapshot1.child("ward").getValue()))) {
                                addDataToModel(dataSnapshot1, dataSnapshot, isToday);
                            }
                        } else {
                            addDataToModel(dataSnapshot1, dataSnapshot, isToday);
                        }
//                    }
                    } else {
                        addDataToModel(dataSnapshot1, dataSnapshot, isToday);
                    }
                }
            }
            setUpPendingCompleteFilter();
        });
    }

    private void filterDataForModel(DataSnapshot snap, boolean isToday) {
        activity.runOnUiThread(() -> {
            Log.e("Data URL", snap.toString());
            for (DataSnapshot dataSnapshot : snap.getChildren()) {
//                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
//                    Log.e("Data URL", "dataSnapshot1 " + dataSnapshot1.child("ward").toString());
                    if (zone.length() > 1) {
//                    if (zone.trim().equalsIgnoreCase(String.valueOf(dataSnapshot.child("zone").getValue()))) {
                        Log.e("Data URL", ward + " " + dataSnapshot.child("ward").getValue());
                        if (ward.length() > 1) {
                            if (ward.trim().equalsIgnoreCase(String.valueOf(dataSnapshot.child("ward").getValue()))) {
                                addDataToModel(dataSnapshot, snap, isToday);
                            }
                        } else {
                            addDataToModel(dataSnapshot, snap, isToday);
                        }
//                    }
                    } else {
                        addDataToModel(dataSnapshot, dataSnapshot, isToday);
                    }
//                }
            }
            setUpPendingCompleteFilter();
        });
    }

    private void addDataToModel(DataSnapshot dataSnapshot, DataSnapshot snap, boolean isToday) {
        if (dataSnapshot.hasChild("imageRef") &&
                dataSnapshot.hasChild("latLng") &&
//                dataSnapshot.hasChild("locality") &&
                dataSnapshot.hasChild("dateTime") &&
                dataSnapshot.hasChild("ward") &&
//                dataSnapshot.hasChild("zone") &&
//                dataSnapshot.hasChild("address") &&
                dataSnapshot.hasChild("userId")) {

            if (dataSnapshot.hasChild("BvgAFction")) {
                completeListAl.add(new LandingListModel(snap.getKey(),
                        String.valueOf(dataSnapshot.child("imageRef").getValue()),
                        String.valueOf(dataSnapshot.child("latLng").getValue()),
                        String.valueOf(dataSnapshot.child("locality").getValue()),
                        String.valueOf(dataSnapshot.child("dateTime").getValue()),
                        String.valueOf(dataSnapshot.child("ward").getValue()),
                        "",
                        String.valueOf(dataSnapshot.child("address").getValue()),
                        String.valueOf(dataSnapshot.getKey()),
                        category + "",
                        isToday,
                        String.valueOf(dataSnapshot.child("user").getValue()),
                        String.valueOf(dataSnapshot.child("BvgAction/date").getValue()),
                        String.valueOf(dataSnapshot.child("BvgAction/imageRef").getValue()),
                        String.valueOf(dataSnapshot.child("BvgAction/time").getValue()),
                        String.valueOf(dataSnapshot.child("BvgAction/latlng").getValue()),
                        String.valueOf(dataSnapshot.child("BvgAction/user").getValue())));
            } else {
                pendingListAl.add(new LandingListModel(snap.getKey(),
                        String.valueOf(dataSnapshot.child("imageRef").getValue()),
                        String.valueOf(dataSnapshot.child("latLng").getValue()),
                        "",
                        String.valueOf(dataSnapshot.child("dateTime").getValue()),
                        String.valueOf(dataSnapshot.child("ward").getValue()),
                        "Test",
                        String.valueOf(dataSnapshot.child("address").getValue()),
                        String.valueOf(dataSnapshot.getKey()),
                        category + "",
                        isToday,
                        String.valueOf(dataSnapshot.child("userId").getValue())));
            }
        }else {
            Log.e("Data URL", ward + " " );
        }
    }

    public void filterBtn() {
        if (bottomSheetDialog == null) bottomSheetDialog = new BottomSheetDialog(activity);
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
        setUpWardSpinner("Dehradun", wardSpinner);
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
//                zone = zoneSpinner.getSelectedItemId() == 0 ? zone = "" : (zone = zoneSpinner.getSelectedItem().toString());
                zone = "Test";
                ward = wardSpinner.getSelectedItemId() == 0 ? ward = "" : (ward = wardSpinner.getSelectedItem().toString());
                isTodayCbSelected = tCb.isChecked();
                isYesterdayCbSelected = yCb.isChecked();
                Log.e("HomeView ",""+tempCat);
                category = tempCat;
                fetchDb(category);
                bottomSheetDialog.dismiss();
            } else {
                Toast.makeText(activity, "Please Select atleast one day", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }

    public void pendingBtn() {
        isPendingSelected = true;
        setUpPendingCompleteFilter();
    }

    public void completedBtn() {
        isPendingSelected = false;
        setUpPendingCompleteFilter();
    }

    @Override
    public void onItemClick(int position, TypesListModel model) {
        tempCat = model.getId();
    }

    @Override
    public void onItemClick(int positions, LandingListModel model) {
        if (isPass) {
            isPass = false;
            position = positions;
            Intent intent = new Intent(activity, Details.class);
            intent.putExtra("LandingListModel", model);
            activity.startActivity(intent);
        }
    }

    public void resume() {
        isPass = true;
        if (!preferences.getString("intentData", "").equalsIgnoreCase("")) {
            Gson gson = new Gson();
            String json = preferences.getString("intentData", "");
            ArrayList<LandingListModel> pendingListAls = new ArrayList<>();
            Type type = new TypeToken<ArrayList<LandingListModel>>() {
            }.getType();
            pendingListAls = gson.fromJson(json, type);
            completeListAl.add(pendingListAls.get(0));
            pendingListAl.remove(position);
            setUpPendingCompleteFilter();
            preferences.edit().putString("intentData", "").apply();
        }
    }
}
