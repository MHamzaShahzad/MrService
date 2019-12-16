package com.example.mrservice.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.models.TaskModel;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentGetTaskDetail extends Fragment implements View.OnClickListener {

    public static final String TAG = FragmentGetTaskDate.class.getName();
    private Context context;
    private View view;

    private EditText taskTitle, taskDescription, taskLocation;
    private RadioButton btnPhysicalTask, btnOnlineTask;
    private Button btnNextFromDetails;


    private Bundle bundleData;
    private LatLng taskLocationLatLng;

    private Snackbar customSnackBar;

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 107;


    public static FragmentGetTaskDetail getInstance(Bundle bundle) {
        return new FragmentGetTaskDetail(bundle);
    }

    public FragmentGetTaskDetail(Bundle bundle) {
        // Required empty public constructor
        this.bundleData = bundle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = container.getContext();
        // Inflate the layout for this fragment
        if (view == null) {
            Log.e(TAG, "onCreateView: " );
            view = inflater.inflate(R.layout.fragment_get_task_detail, container, false);


            findViewsById();
            initSnackBar();
        }
        return view;
    }

    private void findViewsById() {

        taskTitle = view.findViewById(R.id.taskTitle);
        taskDescription = view.findViewById(R.id.taskDescription);
        taskLocation = view.findViewById(R.id.taskLocation);

        btnPhysicalTask = view.findViewById(R.id.btnPhysicalTask);
        btnOnlineTask = view.findViewById(R.id.btnOnlineTask);

        btnNextFromDetails = view.findViewById(R.id.btnNextFromDetails);

        initClickListeners();
    }

    private void initClickListeners() {
        taskLocation.setOnClickListener(this);
        btnNextFromDetails.setOnClickListener(this);
    }

    private boolean isFormValid() {

        if (TextUtils.isEmpty(taskTitle.getText())) {
            customSnackBar.setText("Please write some title about this poster / task.").show();
            return false;
        }
        if (TextUtils.isEmpty(taskDescription.getText())) {
            customSnackBar.setText("Please enter description about your poster / task.").show();
            return false;
        }

        if ((TextUtils.isEmpty(taskLocation.getText()) || taskLocationLatLng == null) && btnPhysicalTask.isChecked()) {
            customSnackBar.setText("Please set location for your poster / task.").show();
            return false;
        }

        if (!TextUtils.isEmpty(taskDescription.getText()) && TextUtils.getTrimmedLength(taskDescription.getText()) < 50) {
            customSnackBar.setText("Please write at least 50 characters for description about your poster / task.").show();
            return false;
        }

        if (getTaskTypeSelected() == null) {
            customSnackBar.setText("Please select type for your poster / task.").show();
            return false;
        }

        return true;
    }

    private String getTaskTypeSelected() {
        if (btnOnlineTask.isChecked())
            return Constants.TASK_TYPE_ONLINE;

        if (btnPhysicalTask.isChecked())
            return Constants.TASK_TYPE_PHYSICAL;

        return null;
    }

    private void initSnackBar() {
        customSnackBar = Snackbar.make(view, "", Snackbar.LENGTH_LONG);
    }

    private void selectLocationDialog() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build((Activity) context), PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                Place place = PlacePicker.getPlace(data, context);
                if (place != null) {

                    if (place.getLatLng() != null) {

                        Log.e(TAG, "onActivityResult: " + place.getLatLng());

                        taskLocationLatLng = place.getLatLng();
                    }
                    taskLocation.setText(place.getName() + "-" + place.getAddress());
                }

            }
        }
    } // onActivityResult Ended...

    @Override
    public void onClick(View view) {
        if (view == btnNextFromDetails) {
            if (isFormValid()) {
                bundleData.putString(TaskModel.STRING_TASK_TITLE_REF, taskTitle.getText().toString().trim());
                bundleData.putString(TaskModel.STRING_TASK_DESCRIPTION_REF, taskDescription.getText().toString().trim());
                bundleData.putString(TaskModel.STRING_TASK_TYPE_REF, getTaskTypeSelected());
                if (btnPhysicalTask.isChecked()) {
                    bundleData.putString(TaskModel.STRING_TASK_LOCATION_REF, taskLocation.getText().toString().trim());
                    bundleData.putString(TaskModel.STRING_TASK_LAT_LNG_REF, taskLocationLatLng.latitude + "-" + taskLocationLatLng.longitude);
                }
                ((FragmentActivity) context)
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, FragmentGetTaskDate.getInstance(bundleData), Constants.TITLE_TASK_DUE_DATE)
                        .addToBackStack(Constants.TITLE_TASK_DETAIL)
                        .commit();
            }
        }
        if (view == taskLocation) {
            selectLocationDialog();
        }
    }

}
