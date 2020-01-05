package com.example.mrservice.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.adapters.AdapterAllOffers;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.models.TaskBid;
import com.example.mrservice.models.TaskModel;
import com.example.mrservice.models.UserProfileModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentOffersList extends Fragment {

    private static final String TAG = FragmentOffersList.class.getName();
    private Context context;
    private View view;

    private AdapterAllOffers adapterAllOffers;
    private List<TaskBid> taskBidList;
    private ValueEventListener bidsValueEventListener;
    private RecyclerView recyclerTaskOffers;

    private Bundle arguments;
    private TaskModel taskModel;
    private ValueEventListener taskValueEventListener;
    private UserProfileModel userProfileModel;


    public static FragmentOffersList getInstance(Bundle arguments) {
        return new FragmentOffersList(arguments);
    }

    private FragmentOffersList(Bundle arguments) {
        // Required empty public constructor
        this.arguments = arguments;
        taskBidList = new ArrayList<>();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = container.getContext();
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_offers_list, container, false);

            getDetails();
        }
        return view;
    }

    private void getDetails() {

        if (arguments != null) {

            taskModel = (TaskModel) arguments.getSerializable(Constants.STRING_TASK_OBJECT);
            userProfileModel = (UserProfileModel) arguments.getSerializable(Constants.STRING_USER_PROFILE_OBJECT);

            setRecyclerView(taskModel, userProfileModel);
            loadTaskModelData(taskModel.getTaskId());
        }

    }

    private void loadTaskModelData(final String taskId) {
        taskValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    try {

                        TaskModel taskModel = dataSnapshot.getValue(TaskModel.class);

                        if (taskModel != null) {
                            if (adapterAllOffers != null)
                                adapterAllOffers.onTaskModelUpdate(taskModel);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        MyFirebaseDatabase.TASKS_REFERENCE.child(taskId).addValueEventListener(taskValueEventListener);
    }

    private void setRecyclerView(TaskModel taskModel, UserProfileModel userProfileModel) {
        recyclerTaskOffers = view.findViewById(R.id.recyclerTaskOffers);
        recyclerTaskOffers.setLayoutManager(new LinearLayoutManager(context));
        recyclerTaskOffers.setHasFixedSize(true);
        adapterAllOffers = new AdapterAllOffers(context, taskModel, userProfileModel, taskBidList);
        recyclerTaskOffers.setAdapter(adapterAllOffers);

        getTaskOffers(taskModel.getTaskId());
    }

    private void getTaskOffers(String taskId) {
        bidsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                taskBidList.clear();
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                    for (DataSnapshot snapshot : snapshots) {
                        if (snapshot.exists() && snapshot.getValue() != null)
                            try {
                                TaskBid taskBid = snapshot.getValue(TaskBid.class);
                                if (taskBid != null)
                                    taskBidList.add(taskBid);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
                adapterAllOffers.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        MyFirebaseDatabase.TASK_OFFERS_REFERENCE.child(taskId).addValueEventListener(bidsValueEventListener);
    }

    private void removeBidsEventListener() {
        if (bidsValueEventListener != null && taskModel != null)
            MyFirebaseDatabase.TASK_OFFERS_REFERENCE.child(taskModel.getTaskId()).removeEventListener(bidsValueEventListener);

    }

    private void removeTaskModelEventListener() {
        if (taskValueEventListener != null && taskModel != null)
            MyFirebaseDatabase.TASKS_REFERENCE.child(taskModel.getTaskId()).removeEventListener(taskValueEventListener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeBidsEventListener();
        removeTaskModelEventListener();
    }

}
