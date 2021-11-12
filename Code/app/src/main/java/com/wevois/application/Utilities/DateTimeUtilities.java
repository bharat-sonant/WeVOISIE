package com.wevois.application.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtilities {
    private final String todayDate, todayMonthName, todayYear, yDate, yYear, yMonth,currentTime;
    Date date;
    SimpleDateFormat dateFormat, monthFormat, yearFormat;

    @SuppressLint("SimpleDateFormat")
    public DateTimeUtilities() {
        date = new Date();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        monthFormat = new SimpleDateFormat("MMMM");
        yearFormat = new SimpleDateFormat("yyyy");
        todayDate = dateFormat.format(date);
        todayMonthName = monthFormat.format(date);
        todayYear = yearFormat.format(date);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        yDate = dateFormat.format(cal.getTime());
        yYear = yearFormat.format(cal.getTime());
        yMonth = monthFormat.format(cal.getTime());
        this.currentTime = "";
    }

    public String getTodayDate() {
        return todayDate;
    }

    public String getMonth() {
        return todayMonthName;
    }

    public String getYear() {
        return todayYear;
    }

    public String getYDate() {
        return yDate;
    }

    public String getyMonth() {
        return yMonth;
    }

    public String getyYear() {
        return yYear;
    }

    public String getCurrentTime(){return new SimpleDateFormat("HH:mm").format(date);}

}
