package com.example.mrservice.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.adapters.AdapterAllOffers;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.controllers.SendPushNotificationFirebase;
import com.example.mrservice.interfaces.FragmentInteractionListener;
import com.example.mrservice.models.TaskBid;
import com.example.mrservice.models.TaskModel;
import com.example.mrservice.models.UserProfileModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class FragmentTaskDescription extends Fragment {


    private static final String TAG = FragmentTaskDescription.class.getName();
    private Context context;
    private View view;

    private CircleImageView profileImage;
    private Button btnMakeOffer, btnEditOffer, btnRemoveOffer, btnCompleteTask, btnIncompleteTask, btnReview;
    private TextView taskTitle, placePostedBy, placeUploadedDuration, placeTaskDescription, placeTaskLocation,
            viewOnMap, placeDueDate, placeBudget, taskReview;
    private RecyclerView recyclerTaskOffers;
    private LinearLayout layout_edit_remove, layout_complete_incomplete_task;

    private AdapterAllOffers adapterAllOffers;
    private List<TaskBid> taskBidList;

    private ValueEventListener taskValueEventListener, userProfileValueEventListener, bidsValueEventListener;
    private String taskId, userProfileId;

    private FirebaseUser firebaseUser;
    private FragmentInteractionListener mListener;

    private Bundle arguments;

    private FragmentTaskDescription(Bundle bundle) {
        // Required empty public constructor
        this.arguments = bundle;
        taskBidList = new ArrayList<>();
    }

    public static FragmentTaskDescription getInstance(Bundle bundle) {
        return new FragmentTaskDescription(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_TASK_DESCRIPTION);
        context = container.getContext();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Inflate the layout for this fragment
        if (view == null) {

            view = inflater.inflate(R.layout.fragment_task_description, container, false);

            initLayoutWidgets();
            getDetails();

        }

        return view;
    }

    private void initLayoutWidgets() {

        layout_edit_remove = view.findViewById(R.id.layout_edit_remove);
        layout_complete_incomplete_task = view.findViewById(R.id.layout_complete_incomplete_task);
        taskTitle = view.findViewById(R.id.taskTitle);
        profileImage = view.findViewById(R.id.profileImage);
        placePostedBy = view.findViewById(R.id.placePostedBy);
        placeUploadedDuration = view.findViewById(R.id.placeUploadedDuration);
        placeTaskDescription = view.findViewById(R.id.placeTaskDescription);
        placeTaskLocation = view.findViewById(R.id.placeTaskLocation);
        placeDueDate = view.findViewById(R.id.placeDueDate);
        placeBudget = view.findViewById(R.id.placeBudget);
        taskReview = view.findViewById(R.id.taskReview);

        viewOnMap = view.findViewById(R.id.viewOnMap);
        btnMakeOffer = view.findViewById(R.id.btnMakeOffer);
        btnEditOffer = view.findViewById(R.id.btnEditOffer);
        btnRemoveOffer = view.findViewById(R.id.btnRemoveOffer);
        btnCompleteTask = view.findViewById(R.id.btnCompleteTask);
        btnIncompleteTask = view.findViewById(R.id.btnIncompleteTask);
        btnReview = view.findViewById(R.id.btnReview);

        recyclerTaskOffers = view.findViewById(R.id.recyclerTaskOffers);
    }

    private void setRecyclerView(TaskModel taskModel) {

        recyclerTaskOffers.setLayoutManager(new LinearLayoutManager(context));
        recyclerTaskOffers.setHasFixedSize(true);
        adapterAllOffers = new AdapterAllOffers(context, taskModel, taskBidList);
        recyclerTaskOffers.setAdapter(adapterAllOffers);

        getTaskOffers(taskModel.getTaskId());
    }

    private void getDetails() {

        if (arguments != null) {

            taskId = arguments.getString(TaskModel.STRING_TASK_ID_REF);
            userProfileId = arguments.getString(UserProfileModel.STRING_USER_PROFILE_ID_REF);

            if (taskId != null) {
                loadTaskModelData(taskId);
            }

            if (userProfileId != null) {
                loadTaskUploadedByProfileData(userProfileId);
            }

        }

    }

    private void loadTaskModelData(final String taskId) {
        taskValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                setDefaultButtonsVisibility();
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    try {

                        TaskModel taskModel = dataSnapshot.getValue(TaskModel.class);

                        if (taskModel != null) {

                            setRecyclerView(taskModel);

                            taskTitle.setText(taskModel.getTaskTitle());
                            placeTaskLocation.setText(taskModel.getTaskLocation());
                            placeBudget.setText(taskModel.getTaskBudget());
                            placeDueDate.setText(taskModel.getTaskDueDate());
                            placeUploadedDuration.setText(taskModel.getTaskUploadedOn());
                            placeTaskDescription.setText(taskModel.getTaskDescription());

                            setViewOnMap(taskModel.getTaskLocation(), getTaskLatitude(taskModel.getTaskLatLng()), getTaskLongitude(taskModel.getTaskLatLng()));

                            switch (taskModel.getTaskStatus()) {
                                case Constants.TASKS_STATUS_OPEN:
                                    if (!taskModel.getTaskUploadedBy().equals(firebaseUser.getUid()))
                                        setBtnMakeOffer(taskModel);
                                case Constants.TASKS_STATUS_CANCELLED:
                                    if (taskModel.getTaskUploadedBy().equals(firebaseUser.getUid()))
                                        editDeleteTask(taskModel);
                                    break;
                                case Constants.TASKS_STATUS_ASSIGNED:
                                    if (taskModel.getTaskUploadedBy().equals(firebaseUser.getUid()))
                                        completeInCompleteTask(taskModel);
                                    break;
                                case Constants.TASKS_STATUS_COMPLETED:
                                case Constants.TASKS_STATUS_UNCOMPLETED:
                                    if (taskModel.getTaskUploadedBy().equals(firebaseUser.getUid()))
                                        reviewTaskBySeller(taskModel);
                                    break;
                                case Constants.TASKS_STATUS_REVIEWED:
                                    taskReview.setVisibility(View.VISIBLE);
                                    taskReview.setText(taskModel.getTaskReviewBySeller());
                                    break;
                            }

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

    private void reviewTaskBySeller(final TaskModel taskModel) {

        btnReview.setVisibility(View.VISIBLE);
        btnReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.STRING_TASK_OBJECT, taskModel);
                FragmentReviewOnTask.getInstance(bundle)
                        .show(((FragmentActivity) context)
                                .getSupportFragmentManager(), Constants.TITLE_REVIEW_TASK);
            }
        });

    }

    private void setDefaultButtonsVisibility() {
        taskReview.setVisibility(View.INVISIBLE);
        btnMakeOffer.setVisibility(View.INVISIBLE);
        layout_complete_incomplete_task.setVisibility(View.INVISIBLE);
        layout_edit_remove.setVisibility(View.INVISIBLE);
        btnReview.setVisibility(View.INVISIBLE);
    }

    private void completeInCompleteTask(final TaskModel taskModel) {

        layout_complete_incomplete_task.setVisibility(View.VISIBLE);

        btnCompleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyFirebaseDatabase.TASKS_REFERENCE.child(taskModel.getTaskId()).child(TaskModel.STRING_TASK_STATUS_REF).setValue(Constants.TASKS_STATUS_COMPLETED).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Successfully!", Toast.LENGTH_LONG).show();
                            SendPushNotificationFirebase.buildAndSendNotification(context,
                                    taskModel.getTaskAssignedTo(), "Task Completed!",
                                    "Your task has been declared completed."
                            );
                        } else
                            Toast.makeText(context, "Un-Successful!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        btnIncompleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyFirebaseDatabase.TASKS_REFERENCE.child(taskModel.getTaskId()).child(TaskModel.STRING_TASK_STATUS_REF).setValue(Constants.TASKS_STATUS_UNCOMPLETED).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Successful!", Toast.LENGTH_LONG).show();
                            SendPushNotificationFirebase.buildAndSendNotification(context, taskModel.getTaskUploadedBy(), "Task incomplete!", "Your task has been declared incomplete by provider.");
                        } else
                            Toast.makeText(context, "Un-Successful!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }


    private void loadTaskUploadedByProfileData(String userProfileId) {
        userProfileValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    try {
                        UserProfileModel userProfileModel = dataSnapshot.getValue(UserProfileModel.class);
                        if (userProfileModel != null) {
                            if (userProfileModel.getUserImageUrl() != null && !userProfileModel.getUserImageUrl().equals("") && !userProfileModel.getUserImageUrl().equals("null"))
                                Picasso.get()
                                        .load(userProfileModel.getUserImageUrl())
                                        .placeholder(R.drawable.image_avatar)
                                        .error(R.drawable.image_avatar)
                                        .centerInside().fit()
                                        .into(profileImage);

                            placePostedBy.setText(userProfileModel.getUserName());
                            seeUserProfile(userProfileModel);
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
        MyFirebaseDatabase.USERS_REFERENCE.child(userProfileId).addListenerForSingleValueEvent(userProfileValueEventListener);
    }

    private double getTaskLatitude(String latLng) {
        if (latLng != null) {
            if (latLng.contains("-")) {
                return Double.parseDouble(latLng.split("-")[0]);
            }
        }
        return 0.0;
    }

    private double getTaskLongitude(String latLng) {
        if (latLng != null) {
            if (latLng.contains("-")) {
                return Double.parseDouble(latLng.split("-")[1]);
            }
        }
        return 0.0;
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
        MyFirebaseDatabase.TASKS_REFERENCE.child(taskId).child(Constants.STRING_OFFERS_REF).addValueEventListener(bidsValueEventListener);
    }

    private void setViewOnMap(final String locationAddress, final double latitude, final double longitude) {
        viewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.STRING_LOCATION_ADDRESS, locationAddress);
                bundle.putDouble(Constants.STRING_LOCATION_LATITUDE, latitude);
                bundle.putDouble(Constants.STRING_LOCATION_LONGITUDE, longitude);

                ((FragmentActivity) context)
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, FragmentMapLocationForTask.getInstance(bundle), Constants.TITLE_LOCATION)
                        .addToBackStack(Constants.TITLE_LOCATION)
                        .commit();
            }
        });
    }

    private void seeUserProfile(final UserProfileModel profileModel) {
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.STRING_USER_PROFILE_OBJECT, profileModel);

                ((FragmentActivity) context)
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, FragmentViewProfile.getInstance(bundle), Constants.TITLE_PROFILE)
                        .addToBackStack(Constants.TITLE_PROFILE)
                        .commit();

            }
        });
    }

    private void setBtnMakeOffer(final TaskModel taskModel) {

        btnMakeOffer.setVisibility(View.VISIBLE);
        if (taskModel.getTaskStatus().equals(Constants.TASKS_STATUS_OPEN)) {
            MyFirebaseDatabase.TASKS_REFERENCE.child(taskModel.getTaskId()).child(Constants.STRING_OFFERS_REF).child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists() && dataSnapshot.getValue() != null)
                        btnMakeOffer.setEnabled(false);
                    else {
                        btnMakeOffer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                MyFirebaseDatabase.USERS_REFERENCE.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable(Constants.STRING_TASK_OBJECT, taskModel);
                                            FragmentMakeOffer.getInstance(bundle).show(((FragmentActivity) context).getSupportFragmentManager(), Constants.TITLE_MAKE_OFFER);

                                        } else {
                                            ((FragmentActivity) context)
                                                    .getSupportFragmentManager()
                                                    .beginTransaction()
                                                    .replace(android.R.id.content, FragmentCreateEditProfile.getInstance(), Constants.TITLE_PROFILE)
                                                    .addToBackStack(Constants.TITLE_PROFILE)
                                                    .commit();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else
            btnMakeOffer.setEnabled(false);
    }

    private void editDeleteTask(final TaskModel taskModel) {

        layout_edit_remove.setVisibility(View.VISIBLE);

        btnEditOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.STRING_TASK_OBJECT, taskModel);
                ((FragmentActivity) context)
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, FragmentEditTaskDetails.getInstance(bundle), Constants.TITLE_EDIT_TASK)
                        .addToBackStack(Constants.TITLE_EDIT_TASK)
                        .commit();
            }
        });

        btnRemoveOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(context)
                        .setMessage("Are you sure , you want to remove this task ?")
                        .setPositiveButton("Yes, Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MyFirebaseDatabase.TASKS_REFERENCE.child(taskModel.getTaskId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "Task removed successfully!", Toast.LENGTH_LONG).show();
                                            ((FragmentActivity) context).getSupportFragmentManager().popBackStack();
                                        } else
                                            Toast.makeText(context, "Task could't be removed!", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create().show();
            }
        });

    }

    private void removeTaskEventListener() {
        if (taskValueEventListener != null && taskId != null)
            MyFirebaseDatabase.TASKS_REFERENCE.child(taskId).removeEventListener(taskValueEventListener);

    }

    private void removeBidsEventListener() {
        if (bidsValueEventListener != null && taskId != null)
            MyFirebaseDatabase.TASKS_REFERENCE.child(taskId).child(Constants.STRING_OFFERS_REF).removeEventListener(bidsValueEventListener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeTaskEventListener();
        removeBidsEventListener();
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
            mListener.onFragmentInteractionListener(Constants.TITLE_TASK_DESCRIPTION);
    }

}