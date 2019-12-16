package com.example.mrservice.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.mrservice.CommonFunctionsClass;
import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.adapters.AdapterMyTasks;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.controllers.SendPushNotificationFirebase;
import com.example.mrservice.interfaces.FragmentInteractionListener;
import com.example.mrservice.models.TaskModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FragmentMyAllTasks extends Fragment {

    private static final String TAG = FragmentMyAllTasks.class.getName();
    private Context context;
    private View view;

    private TabLayout tabMyTasks;
    private RecyclerView recyclerMyTasks;
    private AdapterMyTasks adapterMyTasks;
    private List<TaskModel> taskModelList, taskModelListTemp;

    private ValueEventListener tasksValueEventListener;

    private FirebaseUser firebaseUser;
    private FragmentInteractionListener mListener;


    public static FragmentMyAllTasks getInstance() {
        return new FragmentMyAllTasks();
    }

    private FragmentMyAllTasks() {
        // Required empty public constructor
        taskModelList = new ArrayList<>();
        taskModelListTemp = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_MY_TASKS);
        context = container.getContext();
        adapterMyTasks = new AdapterMyTasks(context, taskModelListTemp);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_my_tasks, container, false);

            initLayoutWidgets();
        }
        return view;
    }

    private void initLayoutWidgets() {
        tabMyTasks = view.findViewById(R.id.tabMyTasks);
        recyclerMyTasks = view.findViewById(R.id.recyclerMyTasks);

        setTabMyTasks();
        setRecyclerMyTasks();
    }

    private void setRecyclerMyTasks() {
        recyclerMyTasks.setHasFixedSize(true);
        recyclerMyTasks.setLayoutManager(new GridLayoutManager(context, 1));
        recyclerMyTasks.setAdapter(adapterMyTasks);
    }

    private void setTabMyTasks() {
        tabMyTasks.addTab(tabMyTasks.newTab().setText("Open"), true);
        tabMyTasks.addTab(tabMyTasks.newTab().setText("Assigned"));
        tabMyTasks.addTab(tabMyTasks.newTab().setText("Completed"));
        tabMyTasks.addTab(tabMyTasks.newTab().setText("Incomplete"));
        tabMyTasks.addTab(tabMyTasks.newTab().setText("Reviewed"));
        tabMyTasks.addTab(tabMyTasks.newTab().setText("Cancelled"));

        setTabSelectedListener();
    }

    private void loadTasksBasedOnTabSelected(int position) {
        switch (position) {
            case 0:
                getMyTasks(Constants.TASKS_STATUS_OPEN);
                break;
            case 1:
                getMyTasks(Constants.TASKS_STATUS_ASSIGNED);
                break;
            case 2:
                getMyTasks(Constants.TASKS_STATUS_COMPLETED);
                break;
            case 3:
                getMyTasks(Constants.TASKS_STATUS_UNCOMPLETED);
                break;
            case 4:
                getMyTasks(Constants.TASKS_STATUS_REVIEWED);
                break;
            case 5:
                getMyTasks(Constants.TASKS_STATUS_CANCELLED);
                break;
            default:
        }
    }

    private void setTabSelectedListener() {
        tabMyTasks.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadTasksBasedOnTabSelected(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                loadTasksBasedOnTabSelected(tab.getPosition());
            }
        });
    }

    private void getMyTasks(String status) {
        Log.e(TAG, "getMyTasks: " + status);
        taskModelListTemp.clear();
        for (TaskModel taskModel : taskModelList)
            if (taskModel != null && taskModel.getTaskStatus() != null && taskModel.getTaskStatus().equals(status))
                taskModelListTemp.add(taskModel);
        adapterMyTasks.notifyDataSetChanged();
    }

    private void initTasksValueEventListener() {
        removeTasksValueEventListener();
        tasksValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {


                    taskModelList.clear();
                    Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                    for (DataSnapshot snapshot : snapshots)
                        if (snapshot.exists() && snapshot.getValue() != null)
                            loadTaskDetails((String) snapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        MyFirebaseDatabase.MY_TASKS_REFERENCE.child(firebaseUser.getUid()).addValueEventListener(tasksValueEventListener);
    }

    private void loadTaskDetails(String taskId) {
        MyFirebaseDatabase.TASKS_REFERENCE.child(taskId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {

                    try {
                        TaskModel taskModel = dataSnapshot.getValue(TaskModel.class);

                        if (taskModel != null && taskModel.getTaskUploadedBy().equals(firebaseUser.getUid()))
                            if (CommonFunctionsClass.isOutdated(taskModel.getTaskDueDate()) && taskModel.getTaskStatus().equals(Constants.TASKS_STATUS_OPEN))
                                makeTaskCancelIfOutDate(taskModel);
                            else {
                                taskModelList.remove(taskModel);
                                taskModelList.add(taskModel);
                            }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                loadTasksBasedOnTabSelected(tabMyTasks.getSelectedTabPosition());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void makeTaskCancelIfOutDate(final TaskModel taskModel) {
        MyFirebaseDatabase.TASKS_REFERENCE.child(taskModel.getTaskId()).child(TaskModel.STRING_TASK_STATUS_REF).setValue(Constants.TASKS_STATUS_CANCELLED).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                SendPushNotificationFirebase.buildAndSendNotification(context,
                        taskModel.getTaskUploadedBy(),
                        "Task Cancelled!",
                        "Your task has been cancelled due to Outdated."
                );
            }
        });
    }

    private void removeTasksValueEventListener() {
        if (tasksValueEventListener != null)
            MyFirebaseDatabase.MY_TASKS_REFERENCE.child(firebaseUser.getUid()).removeEventListener(tasksValueEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        setRecyclerMyTasks();
        setTabSelectedListener();
        initTasksValueEventListener();

        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_MY_TASKS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeTasksValueEventListener();
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

}
