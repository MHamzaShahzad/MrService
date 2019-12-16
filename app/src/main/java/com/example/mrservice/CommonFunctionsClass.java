package com.example.mrservice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.mrservice.activities.LoginActivity;
import com.example.mrservice.admin.FragmentTaskCategoriesAdmin;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommonFunctionsClass {

    public static void logoutUser(final FirebaseAuth mAuth, final Context context) {
        new AlertDialog.Builder(context,
                R.style.AlertDialog_Theme)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Logout confirmation!")
                .setMessage("Do you really want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes, Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CommonFunctionsClass.sigOut(mAuth, context);
                        Toast.makeText(context, "Logout Successful !", Toast.LENGTH_LONG).show();
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static void sigOut(final FirebaseAuth mAuth, final Context context) {
        if (mAuth != null)
            mAuth.signOut();
        LoginManager.getInstance().logOut();
        context.startActivity(new Intent(context, LoginActivity.class));
        ((Activity) context).finish();
    }

    public static String getCurrentDateTime(){
        return new SimpleDateFormat("dd MM yyyy hh:mm a").format(Calendar.getInstance().getTime());
    }


    public static void getAdminLoginDialog(final Context context) {

        final AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View view = ((FragmentActivity) context).getLayoutInflater().inflate(R.layout.authenticate_admin_dialog, null);
        final EditText editText_enter_username = (EditText) view.findViewById(R.id.editText_enter_username);
        final EditText editText_enter_password = (EditText) view.findViewById(R.id.editText_enter_password);
        final Button btn_submit_pin = (Button) view.findViewById(R.id.btn_submit);

        final TextView showPassword = (TextView) view.findViewById(R.id.show_password_eye_text);
        final String[] pType = {"editText_enter_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)"};

        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pType[0].equals("editText_enter_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)")) {
                    pType[0] = "editText_enter_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)";
                    editText_enter_password.setTransformationMethod(null);

                    if (editText_enter_password.getText().length() > 0)
                        editText_enter_password.setSelection(editText_enter_password.getText().length());
                    showPassword.setBackgroundResource(R.drawable.eye);
                } else {
                    pType[0] = "editText_enter_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)";
                    editText_enter_password.setTransformationMethod(new PasswordTransformationMethod());
                    if (editText_enter_password.getText().length() > 0)
                        editText_enter_password.setSelection(editText_enter_password.getText().length());
                    showPassword.setBackgroundResource(R.drawable.eye1);
                }
            }
        });


        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();

        btn_submit_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = "admin";
                String password = "admin123xyz";
                if (editText_enter_username.length() == 0) {
                    editText_enter_username.setError("Field is required!");
                    editText_enter_username.setFocusable(true);
                } else if (editText_enter_password.length() == 0) {
                    editText_enter_password.setError("Field is required!");
                    editText_enter_password.setFocusable(true);
                } else {

                    if (editText_enter_username.getText().toString().trim().equals(username) && editText_enter_password.getText().toString().trim().equals(password)) {
                        dialog.dismiss();
                        ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                                .replace(android.R.id.content, FragmentTaskCategoriesAdmin.getInstance(true))
                                .addToBackStack(null).commit();
                    }
                    if (!editText_enter_username.getText().toString().trim().equals(username)) {
                        editText_enter_username.setError("Invalid username!");
                        editText_enter_username.setFocusable(true);
                    }
                    if (!editText_enter_password.getText().toString().trim().equals(password)) {
                        editText_enter_password.setError("Invalid password!");
                        editText_enter_password.setFocusable(true);
                    }
                }
            }
        });
    }

    public static void showViewIf(final Context context, String title, String msg, final Object fragment) {
        new AlertDialog.Builder(context,
                R.style.AlertDialog_Theme)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Ok, Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (fragment != null)
                            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(android.R.id.content, (Fragment) fragment).addToBackStack(Constants.TITLE_PROFILE).commit();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create().show();
    }

    public static String getStringTaskStatus(String status) {
        String taskStatus = "";
        if (status != null)
            switch (status) {
                case Constants.TASKS_STATUS_OPEN:
                    taskStatus = Constants.STRING_TASKS_STATUS_OPEN;
                    break;
                case Constants.TASKS_STATUS_ASSIGNED:
                    taskStatus = Constants.STRING_TASKS_STATUS_ASSIGNED;
                    break;
                case Constants.TASKS_STATUS_COMPLETED:
                    taskStatus = Constants.STRING_TASKS_STATUS_COMPLETED;
                    break;
                case Constants.TASKS_STATUS_REVIEWED:
                    taskStatus = Constants.STRING_TASKS_STATUS_REVIEWED;
                    break;
                case Constants.TASKS_STATUS_CANCELLED:
                    taskStatus = Constants.STRING_TASKS_STATUS_CANCELLED;
                    break;
                default:
                    taskStatus = "";
            }
        return taskStatus;
    }

    public static double getTaskLatitude(String latLng) {
        if (latLng != null) {
            if (latLng.contains("-")) {
                return Double.parseDouble(latLng.split("-")[0]);
            }
        }
        return 0.0;
    }

    public static double getTaskLongitude(String latLng) {
        if (latLng != null) {
            if (latLng.contains("-")) {
                return Double.parseDouble(latLng.split("-")[1]);
            }
        }
        return 0.0;
    }

    public static String getCityFromLatLng(Context context, double LATITUDE, double LONGITUDE) {
        String cityName = null;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                cityName = returnedAddress.getLocality();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("@ErrorInAAddress", "My Current location address Cannot get Address!");
        }
        return cityName;
    }

    public static String getUserType(String val) {
        String userType = "";
        if (val != null)
            switch (val) {
                case Constants.USER_PROFILE_BUYER:
                    userType = Constants.STRING_USER_PROFILE_BUYER;
                    break;
                case Constants.USER_PROFILE_SELLER:
                    userType = Constants.STRING_USER_PROFILE_SELLER;
                    break;
                case Constants.USER_PROFILE_BUYER_SELLER:
                    userType = Constants.STRING_USER_PROFILE_BUYER_SELLER;
                    break;
                default:
                    userType = "";
            }
        return userType;
    }

    public static boolean isOutdated(String dueDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        Date strDate = null;
        try {
            strDate = sdf.parse(dueDate);
            if (strDate != null)
                if (System.currentTimeMillis() > strDate.getTime()) {
                    return true;
                }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void clearFragmentBackStack(FragmentManager fragmentManager) {
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++)
            fragmentManager.popBackStack();
    }

}
