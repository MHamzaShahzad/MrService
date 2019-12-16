package com.example.mrservice.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.mrservice.CommonFunctionsClass;
import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.fragments.FragmentTaskDescription;
import com.example.mrservice.models.BidModel;
import com.example.mrservice.models.TaskModel;
import com.example.mrservice.models.UserProfileModel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterTasksMapCustomInfoWindow implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    private Context context;

    private CircleImageView profileImage;
    private TextView textBudget, taskTitle, taskDueDate, btnViewTask, userRating, taskOffers, taskStatus;

    private TaskModel taskModel;
    private Bundle bundle;

    private GoogleMap mMap;

    public AdapterTasksMapCustomInfoWindow(Context context, GoogleMap mMap) {
        this.context = context;
        this.mMap = mMap;
        bundle = new Bundle();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity) context).getLayoutInflater()
                .inflate(R.layout.map_custom_infowindow, null);

        mMap.setOnInfoWindowClickListener(this);
        initLayoutWidgets(view);

        taskModel = (TaskModel) marker.getTag();
        if (taskModel != null) {

            taskStatus.setText(CommonFunctionsClass.getStringTaskStatus(taskModel.getTaskStatus()));
            taskTitle.setText(taskModel.getTaskTitle());
            taskDueDate.setText("Due Date : " + taskModel.getTaskDueDate());
            textBudget.setText(taskModel.getTaskBudget());

            getUserProfile(taskModel.getTaskUploadedBy());
            getTaskOffers(taskModel.getTaskId());


        }

        return view;
    }

    private void initLayoutWidgets(View view) {
        profileImage = view.findViewById(R.id.profileImage);
        textBudget = view.findViewById(R.id.textBudget);
        taskTitle = view.findViewById(R.id.taskTitle);
        taskDueDate = view.findViewById(R.id.taskDueDate);
        btnViewTask = view.findViewById(R.id.btnViewTask);

        userRating = view.findViewById(R.id.userRating);
        taskStatus = view.findViewById(R.id.taskStatus);
        taskOffers = view.findViewById(R.id.taskOffers);
    }

    private void setBtnViewTask(Marker marker) {
        bundle.clear();
        bundle.putString(UserProfileModel.STRING_USER_PROFILE_ID_REF, taskModel.getTaskUploadedBy());
        bundle.putString(TaskModel.STRING_TASK_ID_REF, taskModel.getTaskId());

        ((FragmentActivity) context)
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, FragmentTaskDescription.getInstance(bundle))
                .addToBackStack(null)
                .commit();

    }

    private void getUserProfile(final String userId) {
        MyFirebaseDatabase.USERS_REFERENCE.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    try {

                        final UserProfileModel userProfileModel = dataSnapshot.getValue(UserProfileModel.class);
                        if (userProfileModel != null) {
                            if (userProfileModel.getUserImageUrl() != null)
                                Picasso.get()
                                        .load(userProfileModel.getUserImageUrl())
                                        .placeholder(R.drawable.ic_launcher_background)
                                        .error(R.drawable.ic_launcher_background)
                                        .centerInside().fit()
                                        .into(profileImage);

                            userRating.setText(userProfileModel.getUserRating() + "(" + userProfileModel.getRatingCounts() + ")");
                            /*profileImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    bundle.putSerializable(Constants.STRING_USER_PROFILE_OBJECT, userProfileModel);
                                    ((FragmentActivity) context)
                                            .getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(android.R.id.content, FragmentViewProfile.getInstance(bundle), Constants.TITLE_PROFILE)
                                            .addToBackStack(Constants.TITLE_PROFILE)
                                            .commit();
                                }
                            });*/

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getTaskOffers(String taskId) {

        MyFirebaseDatabase.TASKS_REFERENCE.child(taskId).child(Constants.STRING_OFFERS_REF).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<BidModel> taskBidList = new ArrayList<>();
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                    for (DataSnapshot snapshot : snapshots) {
                        if (snapshot.exists() && snapshot.getValue() != null)
                            try {
                                BidModel taskBid = snapshot.getValue(BidModel.class);
                                taskBidList.add(taskBid);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
                taskOffers.setText(taskBidList.size() + " Offers");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        setBtnViewTask(marker);
    }
}
