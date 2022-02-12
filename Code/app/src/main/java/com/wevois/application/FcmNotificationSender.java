package com.wevois.application;

import android.app.Activity;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FcmNotificationSender {
    String userToken;
    String title;
    String body;
    String data;
    String icon;
    String color;
    String click_action;
    Context context;
    Activity activity;

    RequestQueue requestQueue;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey = "AAAAWLxrVRI:APA91bGFMeeQoUzVDlX2a7IBYOK76e9D2D3uB6kgUrfMcuZDhw0g9ciEUSSljMH3em_Yfh2Wie4Dp54z1T0zABESSMbFs52enD2ZM1q1ZOLQotBcdnK7-X9j6WUYzfwC8mlCvYsyZkt7";

    public FcmNotificationSender(String userToken, String title, String body, String data, String icon, String color, String click_action, Context context, Activity activity) {
        this.userToken = userToken;
        this.title = title;
        this.body = body;
        this.data = data;
        this.icon = icon;
        this.color = color;
        this.click_action = click_action;
        this.context = context;
        this.activity = activity;
    }

    public void SendNotification(){
        requestQueue = Volley.newRequestQueue(activity);
        try {
            JSONObject mainObj = new JSONObject();
            mainObj.put("to",userToken);
            JSONObject notiObj = new JSONObject();
            notiObj.put("title",title);
            notiObj.put("body",body);
            notiObj.put("icon",icon);
            notiObj.put("color",color);
            notiObj.put("click_action",click_action);
            JSONObject dataObj = new JSONObject();
            dataObj.put("reference",data);
            mainObj.put("notification",notiObj);
            mainObj.put("data",dataObj);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, response -> {
            }, error -> {
            }) {
                @Override
                public Map<String, String> getHeaders() {

                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;
                }
            };
            requestQueue.add(request);

        } catch (Exception ignored) {
        }
    }
}
