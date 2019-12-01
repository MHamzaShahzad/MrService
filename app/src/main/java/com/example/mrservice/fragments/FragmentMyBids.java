package com.example.mrservice.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.adapters.AdapterTasksWithMyBids;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.interfaces.FragmentInteractionListener;
import com.example.mrservice.models.TaskModel;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FragmentMyBids extends Fragment {

    private Context context;
    private View view;

    private AdapterTasksWithMyBids adapterTasksWithMyBids;

    private TabLayout myOffersTabs;
    private RecyclerView recyclerMyOffers;
    private ValueEventListener tasksValueEventListener;
    private List<TaskModel> taskModelList, taskModelListTemp;
    private FirebaseUser firebaseUser;
    private FragmentInteractionListener mListener;


    public static FragmentMyBids getInstance(){
        return new FragmentMyBids();
    }

    private FragmentMyBids() {
        // Required empty public constructor
        taskModelList = new ArrayList<>();
        taskModelListTemp = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_MY_BIDS);
        context = container.getContext();
        adapterTasksWithMyBids = new AdapterTasksWithMyBids(context, taskModelListTemp);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_my_bids, container, false);
            initLayoutWidgets(view);
            initTasksValueEventListener();
        }
        return view;
    }

    private void initLayoutWidgets(View view) {
        myOffersTabs = view.findViewById(R.id.myOffersTabs);
        recyclerMyOffers = view.findViewById(R.id.recyclerMyOffers);

        setTabMyTasks();
        setRecyclerMyTasks();
    }

    private void setRecyclerMyTasks() {
        recyclerMyOffers.setHasFixedSize(true);
        recyclerMyOffers.setLayoutManager(new GridLayoutManager(context, 1));
        recyclerMyOffers.setAdapter(adapterTasksWithMyBids);
    }

    private void setTabMyTasks() {
        myOffersTabs.addTab(myOffersTabs.newTab().setText("Sent"), true);
        myOffersTabs.addTab(myOffersTabs.newTab().setText("Assigned"));
        myOffersTabs.addTab(myOffersTabs.newTab().setText("Completed"));
        myOffersTabs.addTab(myOffersTabs.newTab().setText("Incomplete"));
        myOffersTabs.addTab(myOffersTabs.newTab().setText("Reviewed"));

        setTabSelectedListener();
    }

    private void setTabSelectedListener() {
        myOffersTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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

    private void loadTasksBasedOnTabSelected(int position) {
        switch (position) {
            case 0:
                getTasksWithMyBid(Constants.TASKS_STATUS_OPEN);
                break;
            case 1:
                getTasksWithMyBid(Constants.TASKS_STATUS_ASSIGNED);
                break;
            case 2:
                getTasksWithMyBid(Constants.TASKS_STATUS_COMPLETED);
                break;
            case 3:
                getTasksWithMyBid(Constants.TASKS_STATUS_UNCOMPLETED);
                break;
            case 4:
                getTasksWithMyBid(Constants.TASKS_STATUS_REVIEWED);
                break;
            default:
        }
    }

    private void getTasksWithMyBid(String status) {

        taskModelListTemp.clear();

        for (final TaskModel taskModel : taskModelList)
            if (taskModel != null && taskModel.getTaskStatus() != null && taskModel.getTaskStatus().equals(status))
                taskModelListTemp.add(taskModel);

        adapterTasksWithMyBids.notifyDataSetChanged();

    }

    private void initTasksValueEventListener() {
        tasksValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {

                    taskModelList.clear();
                    Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                    for (DataSnapshot snapshot : snapshots) {
                        try {
                            final TaskModel taskModel = snapshot.getValue(TaskModel.class);
                            if (taskModel != null)
                                if (taskModel.getTaskStatus().equals(Constants.TASKS_STATUS_OPEN)) {

                                    MyFirebaseDatabase.TASKS_REFERENCE.child(taskModel.getTaskId()).child(Constants.STRING_OFFERS_REF).child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                                                taskModelList.add(taskModel);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                } else {
                                    if (taskModel.getTaskAssignedTo() != null && taskModel.getTaskAssignedTo().equals(firebaseUser.getUid()))
                                        taskModelList.add(taskModel);
                                }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadTasksBasedOnTabSelected(myOffersTabs.getSelectedTabPosition());
                        }
                    }, 2000);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        MyFirebaseDatabase.TASKS_REFERENCE.addValueEventListener(tasksValueEventListener);
    }

    private void removeTasksValueEventListener() {
        if (tasksValueEventListener != null)
            MyFirebaseDatabase.TASKS_REFERENCE.removeEventListener(tasksValueEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        setRecyclerMyTasks();
        setTabSelectedListener();
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_MY_BIDS);
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
