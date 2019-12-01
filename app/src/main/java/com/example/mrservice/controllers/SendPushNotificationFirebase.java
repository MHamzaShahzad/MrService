package com.example.mrservice.controllers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.mrservice.models.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SendPushNotificationFirebase {

    private static final String TAG = SendPushNotificationFirebase.class.getName();

    private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private static final String serverKey = "key=" + "AAAAHwp-ooo:APA91bEQ3aGGe1wEg5mb5unp0ttaYieDAoYgWeqblyEITB98huTV3BtGpkmHd58_MmOqYUFD4IudS-P2j-kVkZXMFJv80CJOwp83xNWEQMFRMA-M3i_0lf0PCB2ld8QF-AD1X8lKuMte";
    private static final String contentType = "application/json";

    public static void buildAndSendNotification(Context context, String TOPIC_SEND_TO, String notificationTitle, String notificationMsg){
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            notificationBody.put("title", notificationTitle);
            notificationBody.put("message", notificationMsg);

            notification.put("to", "/topics/" + TOPIC_SEND_TO);
            notification.put("data", notificationBody);
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e.getMessage() );
        }
        sendNotification(context, notification);
    }

    private static void sendNotification(final Context context, JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

}
