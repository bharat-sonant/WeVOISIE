package com.wevois.application.Model;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.util.Map;

public class LandingListModel implements Serializable {
    String date, imageNm, latlng, locality, time, ward, zone, address, serialNm, category;
    String actionDate, actionImageRef, actionLatLng,actionTime,actionUser;

    public String getActionDate() {
        return actionDate;
    }

    public void setActionDate(String actionDate) {
        this.actionDate = actionDate;
    }

    public String getActionImageRef() {
        return actionImageRef;
    }

    public void setActionImageRef(String actionImageRef) {
        this.actionImageRef = actionImageRef;
    }

    public String getActionLatLng() {
        return actionLatLng;
    }

    public void setActionLatLng(String actionLatLng) {
        this.actionLatLng = actionLatLng;
    }

    public String getActionTime() {
        return actionTime;
    }

    public void setActionTime(String actionTime) {
        this.actionTime = actionTime;
    }

    public String getActionUser() {
        return actionUser;
    }

    public void setActionUser(String actionUser) {
        this.actionUser = actionUser;
    }

    boolean isToday;

    public boolean isToday() {
        return isToday;
    }

    public String getDate() {
        return date;
    }

    public String getImageNm() {
        return imageNm;
    }

    public String getLatlng() {
        return latlng;
    }

    public String getLocality() {
        return locality;
    }

    public String getTime() {
        return time;
    }

    public String getWard() {
        return ward;
    }

    public String getZone() {
        return zone;
    }

    public String getAdd() {
        return address;
    }

    public String getSerialNm() {
        return serialNm;
    }

    public String getCategory() {
        return category;
    }


    public LandingListModel(String key,
                            String imageNm,
                            String latlng,
                            String locality,
                            String time,
                            String ward,
                            String zone,
                            String address,
                            String serialNm,
                            String category,
                            boolean isToday,
                            String actionDate,
                            String actionImageRef,
                            String actionTime,
                            String actionLatLng,
                            String actionUser) {

        this.date = key;
        this.imageNm = imageNm;
        this.latlng = latlng;
        this.locality = locality;
        this.time = time;
        this.ward = ward;
        this.zone = zone;
        this.address = address;
        this.serialNm = serialNm;
        this.category = category;
        this.isToday = isToday;
        this.actionDate = actionDate;
        this.actionImageRef = actionImageRef;
        this.actionTime = actionTime;
        this.actionLatLng = actionLatLng;
        this.actionUser = actionUser;
    }

    public LandingListModel(String key,
                            String imageNm,
                            String latlng,
                            String locality,
                            String time,
                            String ward,
                            String zone,
                            String address,
                            String serialNm,
                            String category,
                            boolean isToday) {

        this.date = key;
        this.imageNm = imageNm;
        this.latlng = latlng;
        this.locality = locality;
        this.time = time;
        this.ward = ward;
        this.zone = zone;
        this.address = address;
        this.serialNm = serialNm;
        this.category = category;
        this.isToday = isToday;
        this.actionDate = "null";
        this.actionImageRef = "null";
        this.actionTime = "null";
        this.actionLatLng = "null";
        this.actionUser = "null";
    }
}
