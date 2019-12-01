package com.example.mrservice.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mrservice.CommonFunctionsClass;
import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.models.TaskCat;
import com.example.mrservice.models.TaskModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentGetTaskBudget extends Fragment {

    public static final String TAG = FragmentGetTaskBudget.class.getName();
    private Context context;
    private View view;

    private Bundle bundleData;

    private EditText taskBudget;
    private Button btnCreateTask;

    private Snackbar customSnackBar;
    private FirebaseUser firebaseUser;

    private ProgressDialog progressDialog;

    public static FragmentGetTaskBudget getInstance(Bundle bundle) {
        return new FragmentGetTaskBudget(bundle);
    }

    private FragmentGetTaskBudget(Bundle bundle) {
        // Required empty public constructor
        this.bundleData = bundle;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        context = container.getContext();
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_get_task_budget, container, false);

            findViewsById();
            initSnackBar();
            initProgressDialog();
        }
        return view;
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Uploading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
    }

    private void findViewsById() {
        taskBudget = view.findViewById(R.id.taskBudget);
        btnCreateTask = view.findViewById(R.id.btnCreateTask);

        setBtnCreateTask();
    }

    private void initSnackBar() {
        customSnackBar = Snackbar.make(view, "", Snackbar.LENGTH_LONG);
    }


    private void setBtnCreateTask() {
        btnCreateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(taskBudget.getText())) {
                    customSnackBar.setText("Please define busget for your poster / task!").show();
                } else {
                    progressDialog.show();
                    submitTak();
                }
            }
        });
    }

    private void submitTak() {
        String id = UUID.randomUUID().toString();
        TaskModel taskModel = buildTaskObject(id);
        if (taskModel != null)
            MyFirebaseDatabase.TASKS_REFERENCE.child(id).setValue(taskModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "TaskModel Uploaded Successfully", Toast.LENGTH_LONG).show();
                        CommonFunctionsClass.clearFragmentBackStack(((FragmentActivity) context).getSupportFragmentManager());
                    } else
                        customSnackBar.setText("Can't upload task due to : " + task.getException().getMessage()).show();
                }
            });
        else
            customSnackBar.setText("Something went wrong while creating your task, please try again!").show();

    }

    private TaskModel buildTaskObject(String id) {

        try {
            String currentDate = FragmentGetTaskDate.formatter.format(Calendar.getInstance().getTime());
            TaskCat taskCat = (TaskCat) bundleData.getSerializable(Constants.STRING_TASKS_CAT_OBJ);
            String taskTitle = bundleData.getString(TaskModel.STRING_TASK_TITLE_REF);
            String taskDescription = bundleData.getString(TaskModel.STRING_TASK_DESCRIPTION_REF);
            String taskLocation = bundleData.getString(TaskModel.STRING_TASK_LOCATION_REF);
            String selectedDueDate = bundleData.getString(TaskModel.STRING_TASK_DUE_DATE_REF);
            String taskType = bundleData.getString(TaskModel.STRING_TASK_TYPE_REF);
            String taskLocationLatLng = bundleData.getString(TaskModel.STRING_TASK_LAT_LNG_REF);

            return new TaskModel(
                    id,
                    Constants.TASKS_STATUS_OPEN,
                    firebaseUser.getUid(),
                    "",
                    "",
                    taskCat.getCategoryId(),
                    taskCat.getCategoryName(),
                    currentDate,
                    taskType,
                    taskTitle,
                    taskDescription,
                    taskLocation,
                    taskLocationLatLng,
                    selectedDueDate,
                    taskBudget.getText().toString().trim()

            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
