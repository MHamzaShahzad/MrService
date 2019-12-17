package com.example.mrservice.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mrservice.CommonFunctionsClass;
import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.adapters.AdapterForServicesList;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.interfaces.OnServiceSelectedI;
import com.example.mrservice.models.TaskCat;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentTaskFilter extends Fragment implements OnServiceSelectedI, View.OnClickListener {

    private static final String TAG = FragmentTaskFilter.class.getName();
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 89;
    private Context context;
    private View view;

    private SwitchCompat switchHideAssignedTask;
    private Spinner spinnerSelectTaskType;
    private EditText taskCategory, taskLocation, taskBudget;
    private Button btnSearch, btnReset;

    private LatLng taskLocationLatLng;
    private String selectedServiceId;
    private BottomSheetDialog mBottomSheetDialog;
    private BottomSheetBehavior sheetBehavior;
    private FragmentAllTasksHome fragmentAllTasksHome;

    private HashMap<String, Object> map;

    public static FragmentTaskFilter getInstance(HashMap<String, Object> mapFilter, Object object) {
        return new FragmentTaskFilter(mapFilter, object);
    }

    private FragmentTaskFilter(HashMap<String, Object> map, Object object) {
        // Required empty public constructor
        this.map = map;
        this.fragmentAllTasksHome = (FragmentAllTasksHome) object;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = container.getContext();
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_task_filter, container, false);

            findViewsById();
            initSheetBehaviour();
            bottomSheetDialog();
            loadPreviousFilters();
        }
        return view;
    }

    private void findViewsById() {

        switchHideAssignedTask = view.findViewById(R.id.switchHideAssignedTask);
        spinnerSelectTaskType = view.findViewById(R.id.spinnerSelectTaskType);

        taskCategory = view.findViewById(R.id.taskCategory);
        taskLocation = view.findViewById(R.id.taskLocation);
        taskBudget = view.findViewById(R.id.taskBudget);

        btnReset = view.findViewById(R.id.btnReset);
        btnSearch = view.findViewById(R.id.btnSearch);

        setClickListeners();
        initSpinnerData();

    }

    private void initSpinnerData() {

        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("All");
        spinnerItems.add("Online");
        spinnerItems.add("Physical");

        spinnerSelectTaskType.setAdapter(new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, spinnerItems));

    }

    private void setClickListeners() {
        taskCategory.setOnClickListener(this);
        taskLocation.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
    }

    private void loadPreviousFilters() {

        if (map != null && map.size() > 0) {

            for (Map.Entry<String, Object> entry : map.entrySet()) {

                switch (entry.getKey()) {
                    case Constants.STRING_SHOW_ONLY_OPEN_TASK:
                        switchHideAssignedTask.setChecked((Boolean) entry.getValue());
                        break;
                    case Constants.STRING_TASK_BUDGET:
                        taskBudget.setText("" + (Integer) entry.getValue());
                        break;
                    case Constants.STRING_LOCATION_ADDRESS:
                        taskLocation.setText((String) entry.getValue());
                        break;
                    case Constants.STRING_LOCATION_LATLNG:
                        taskLocationLatLng = (LatLng) entry.getValue();
                        break;
                    case Constants.STRING_TASKS_CAT:
                        taskCategory.setText((String) map.get(Constants.STRING_TASKS_CAT));
                        break;
                    case Constants.STRING_TASKS_CAT_ID:
                        selectedServiceId = (String) entry.getValue();
                        break;
                    case Constants.STRING_TASK_TYPE:
                        Log.e(TAG, "loadPreviousFilters: TASK_TYPE - " + entry.getValue());
                        spinnerSelectTaskType.setSelection(Integer.parseInt(entry.getValue().toString()));
                        break;

                }

            }

        }

    }

    private void initSheetBehaviour() {
        sheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottom_sheet));
        // callback for do something
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
    }

    private void loadServicesList(final RecyclerView recyclerView) {
        MyFirebaseDatabase.TASKS_CAT.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<TaskCat> list = new ArrayList<>();
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                    for (DataSnapshot snapshot : snapshots) {

                        try {

                            TaskCat model = snapshot.getValue(TaskCat.class);
                            if (model != null)
                                list.add(model);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
                recyclerView.setAdapter(new AdapterForServicesList(FragmentTaskFilter.this, context, list));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void bottomSheetDialog() {

        View dialogView = getLayoutInflater().inflate(R.layout.services_bottom_up_sheet, null);

        mBottomSheetDialog = new BottomSheetDialog(context,
                R.style.MaterialDialogSheet);

        RecyclerView recyclerView = dialogView.findViewById(R.id.listServices);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 1));
        recyclerView.setHasFixedSize(true);

        loadServicesList(recyclerView);

        mBottomSheetDialog.setContentView(dialogView);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void setBtnSearch() {

        map.clear();

        if (!TextUtils.isEmpty(taskBudget.getText())) {
            map.put(Constants.STRING_TASK_BUDGET, Integer.valueOf(taskBudget.getText().toString().trim()));
        }
        if (!TextUtils.isEmpty(taskLocation.getText())) {
            map.put(Constants.STRING_LOCATION_ADDRESS, taskLocation.getText().toString().trim());
        }
        if (taskLocationLatLng != null) {
            map.put(Constants.STRING_LOCATION_LATLNG, taskLocationLatLng);
            map.put(Constants.STRING_TASK_CITY, CommonFunctionsClass.getCityFromLatLng(context, taskLocationLatLng.latitude, taskLocationLatLng.longitude));
        }
        if (!TextUtils.isEmpty(taskCategory.getText())) {
            map.put(Constants.STRING_TASKS_CAT, taskCategory.getText().toString().trim());
        }
        if (selectedServiceId != null) {
            map.put(Constants.STRING_TASKS_CAT_ID, selectedServiceId);
        }
        if (switchHideAssignedTask.isChecked()) {
            map.put(Constants.STRING_SHOW_ONLY_OPEN_TASK, true);
        }

        if (getTaskTpeFromSpinner() != null)
            map.put(Constants.STRING_TASK_TYPE, getTaskTpeFromSpinner());

        fragmentAllTasksHome.onTaskFilter(map);
        ((FragmentActivity) context).getSupportFragmentManager().popBackStack();

    }

    private String getTaskTpeFromSpinner() {
        if (spinnerSelectTaskType.getSelectedItemPosition() == 0)
            return "2";
        if (spinnerSelectTaskType.getSelectedItemPosition() == 1)
            return "1";
        if (spinnerSelectTaskType.getSelectedItemPosition() == 2)
            return "0";
        return null;
    }

    private void setBtnReset() {
        selectedServiceId = null;
        taskLocationLatLng = null;

        taskBudget.setText("");
        taskLocation.setText("");
        taskCategory.setText("");
        switchHideAssignedTask.setChecked(false);

        spinnerSelectTaskType.setSelection(0);

        map.clear();
        fragmentAllTasksHome.onTaskFilter(map);
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
    public void onServiceSelected(String serviceId, String serviceName) {
        taskCategory.setText(serviceName);
        selectedServiceId = serviceId;
        if (mBottomSheetDialog != null)
            mBottomSheetDialog.dismiss();
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

        switch (view.getId()) {
            case R.id.taskLocation:
                selectLocationDialog();
                break;
            case R.id.taskCategory:
                if (mBottomSheetDialog != null)
                    mBottomSheetDialog.show();
                break;
            case R.id.btnReset:
                setBtnReset();
                break;
            case R.id.btnSearch:
                setBtnSearch();
                break;
        }

    }
}
