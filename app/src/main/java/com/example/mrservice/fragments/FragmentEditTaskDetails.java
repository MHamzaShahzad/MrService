package com.example.mrservice.fragments;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mrservice.CommonFunctionsClass;
import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.adapters.AdapterForServicesList;
import com.example.mrservice.controllers.MyFirebaseDatabase;

import com.example.mrservice.interfaces.FragmentInteractionListener;
import com.example.mrservice.interfaces.OnServiceSelectedI;
import com.example.mrservice.models.TaskCat;
import com.example.mrservice.models.TaskModel;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentEditTaskDetails extends Fragment implements View.OnClickListener, OnServiceSelectedI {

    private static final String TAG = FragmentEditTaskDetails.class.getName();
    private Context context;
    private View view;

    private TextView selectDueDate;
    private EditText taskTitle, taskDescription, taskLocation, taskBudget, selectTaskCategory;
    private Button btnSubmitTask;
    private BottomSheetBehavior sheetBehavior;

    private DatePickerDialog dialog;

    private DateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 108;

    private LatLng taskLocationLatLng;
    private FirebaseUser user;
    private String uploadedDate;

    private String selectedServiceId;
    private BottomSheetDialog mBottomSheetDialog;

    private TaskModel taskModel;
    private FragmentInteractionListener mListener;

    private Snackbar customSnackBar;
    private RadioButton btnPhysicalTask, btnOnlineTask;

    private Bundle arguments;

    public static FragmentEditTaskDetails getInstance(Bundle arguments) {
        return new FragmentEditTaskDetails(arguments);
    }

    private FragmentEditTaskDetails(Bundle arguments) {
        // Required empty public constructor
        this.arguments = arguments;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_EDIT_TASK);
        context = container.getContext();
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_edit_task, container, false);

            user = FirebaseAuth.getInstance().getCurrentUser();


            initSheetBehaviour();
            initLayoutWidgets();
            initDatePickerDialog();
            bottomSheetDialog();
            initSnackBar();
            getTaskDetailsToBeEdited();
        }
        return view;
    }

    private void getTaskDetailsToBeEdited() {

        if (arguments != null) {

            taskModel = (TaskModel) arguments.getSerializable(Constants.STRING_TASK_OBJECT);
            if (taskModel != null) {

                selectDueDate.setText(taskModel.getTaskDueDate());
                taskTitle.setText(taskModel.getTaskTitle());
                taskDescription.setText(taskModel.getTaskDescription());
                taskBudget.setText(taskModel.getTaskBudget());
                selectTaskCategory.setText(taskModel.getTaskCatName());
                taskLocation.setText(taskModel.getTaskLocation());

                selectedServiceId = taskModel.getTaskCategory();
                uploadedDate = taskModel.getTaskUploadedOn();
                taskLocationLatLng = new LatLng(CommonFunctionsClass.getTaskLatitude(taskModel.getTaskLatLng()), CommonFunctionsClass.getTaskLongitude(taskModel.getTaskLatLng()));
                setTaskType(taskModel.getTaskType());
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

    private void initLayoutWidgets() {

        selectDueDate = view.findViewById(R.id.selectDueDate);

        taskTitle = view.findViewById(R.id.taskTitle);
        taskDescription = view.findViewById(R.id.taskDescription);
        taskLocation = view.findViewById(R.id.taskLocation);
        taskBudget = view.findViewById(R.id.taskBudget);
        selectTaskCategory = view.findViewById(R.id.selectTaskCategory);

        btnPhysicalTask = view.findViewById(R.id.btnPhysicalTask);
        btnOnlineTask = view.findViewById(R.id.btnOnlineTask);

        btnSubmitTask = view.findViewById(R.id.btnSubmitTask);

        initClickListeners();

    }

    private void initClickListeners() {
        selectDueDate.setOnClickListener(this);
        taskLocation.setOnClickListener(this);
        btnSubmitTask.setOnClickListener(this);
        selectTaskCategory.setOnClickListener(this);
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            datePicker.animate();
            Log.e(TAG, "onDateSet: " + getStringDay(day) + " " + getMonthString(month) + " " + year);
            selectDueDate.setText(getStringDay(day) + " " + getMonthString(month) + " " + year);
        }
    };

    public void bottomSheetDialog() {

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
                recyclerView.setAdapter(new AdapterForServicesList(FragmentEditTaskDetails.this, context, list));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getStringDay(int day) {
        if (day < 9)
            return "0" + day;
        else
            return String.valueOf(day);
    }

    private String getMonthString(int month) {
        String monthName = null;
        switch (month) {
            case 0:
                monthName = "Jan";
                break;
            case 1:
                monthName = "Feb";
                break;
            case 2:
                monthName = "Mar";
                break;
            case 3:
                monthName = "Apr";
                break;
            case 4:
                monthName = "May";
                break;
            case 5:
                monthName = "Jun";
                break;
            case 6:
                monthName = "Jul";
                break;
            case 7:
                monthName = "Aug";
                break;
            case 8:
                monthName = "Sep";
                break;
            case 9:
                monthName = "Oct";
                break;
            case 10:
                monthName = "Nov";
                break;
            case 11:
                monthName = "Dec";
                break;
        }
        return monthName;
    }

    private void showDatePickerDialog() {
        dialog.show();
    }

    private void initDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        dialog = new DatePickerDialog(
                context,
                mDateSetListener,
                year, month, day
        );
    }

    private TaskModel buildTaskObject(String id) {
        try {
            return new TaskModel(
                    id,
                    Constants.TASKS_STATUS_OPEN,
                    user.getUid(),
                    "",
                    "",
                    selectedServiceId,
                    selectTaskCategory.getText().toString(),
                    uploadedDate,
                    getTaskTypeSelected(),
                    taskTitle.getText().toString().trim(),
                    taskDescription.getText().toString().trim(),
                    taskLocation.getText().toString().trim(),
                    taskLocationLatLng.latitude + "-" + taskLocationLatLng.longitude,
                    selectDueDate.getText().toString().trim(),
                    taskBudget.getText().toString().trim()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void submitTak() {
        String id = taskModel.getTaskId();
        TaskModel taskModel = buildTaskObject(id);
        if (taskModel != null)
            MyFirebaseDatabase.TASKS_REFERENCE.child(id).setValue(taskModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "TaskModel Uploaded Successfully", Toast.LENGTH_LONG).show();
                        CommonFunctionsClass.clearFragmentBackStack(((FragmentActivity) context).getSupportFragmentManager());
                        //((FragmentActivity) context).getSupportFragmentManager().popBackStack();
                    } else
                        customSnackBar.setText("Can't upload task due to : " + task.getException().getMessage()).show();
                }
            });
        else
            customSnackBar.setText("Something went wrong while creating your task, please try again!").show();
    }

    private void initSnackBar() {
        customSnackBar = Snackbar.make(view, "", Snackbar.LENGTH_LONG);
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

        if (TextUtils.isEmpty(selectDueDate.getText())) {
            customSnackBar.setText("Please select due date for your poster / task.").show();
            return false;
        }

        if (TextUtils.isEmpty(taskBudget.getText())) {
            customSnackBar.setText("Please define budget for your poster / task.").show();
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

    private void setTaskType(String taskType) {
        switch (taskType) {
            case Constants.TASK_TYPE_ONLINE:
                btnOnlineTask.setChecked(true);

            case Constants.TASK_TYPE_PHYSICAL:
                btnPhysicalTask.setChecked(true);
        }
    }

    private String getTaskTypeSelected() {
        if (btnOnlineTask.isChecked())
            return Constants.TASK_TYPE_ONLINE;

        if (btnPhysicalTask.isChecked())
            return Constants.TASK_TYPE_PHYSICAL;

        return null;
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
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.selectDueDate:
                showDatePickerDialog();
                break;
            case R.id.btnSubmitTask:
                if (isFormValid())
                    submitTak();
                break;
            case R.id.taskLocation:
                selectLocationDialog();
                break;
            case R.id.selectTaskCategory:
                if (mBottomSheetDialog != null)
                    mBottomSheetDialog.show();
                break;
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
    public void onServiceSelected(String serviceId, String serviceName) {
        selectTaskCategory.setText(serviceName);
        selectedServiceId = serviceId;
        if (mBottomSheetDialog != null)
            mBottomSheetDialog.dismiss();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement FragmentInteractionListenerInterface.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_EDIT_TASK);
    }

}
