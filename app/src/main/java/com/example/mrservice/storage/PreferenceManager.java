package com.example.mrservice.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mrservice.R;

public class PreferenceManager {
    private Context context;
    private SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        this.context = context;
        getSharedPreference();
    }


    private void getSharedPreference() {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.my_preference), Context.MODE_PRIVATE);
    }

    public void writeprefernce() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.my_preference_key), "INIT_OK");
        editor.apply();

    }

    public boolean checkPreference() {
        boolean status = false;

        status = !sharedPreferences.getString(context.getString(R.string.my_preference_key), "null").equals("null");
        return status;
    }

    public void clearPreference() {
        sharedPreferences.edit().clear().apply();
    }

}
