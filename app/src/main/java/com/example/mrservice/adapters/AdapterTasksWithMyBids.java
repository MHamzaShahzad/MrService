package com.example.mrservice.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.example.mrservice.CommonFunctionsClass;
import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.fragments.FragmentTaskDescription;
import com.example.mrservice.models.TaskBid;
import com.example.mrservice.models.TaskModel;
import com.example.mrservice.models.UserProfileModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterTasksWithMyBids extends RecyclerView.Adapter<AdapterTasksWithMyBids.Holder> {

    private Context context;
    private List<TaskModel> list;

    private Bundle bundle;

    public AdapterTasksWithMyBids(Context context, List<TaskModel> list) {
        this.context = context;
        this.list = list;

        bundle = new Bundle();
    }

    @NonNull
    @Override
    public AdapterTasksWithMyBids.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_all_tasks, null);
        return new AdapterTasksWithMyBids.Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterTasksWithMyBids.Holder holder, int position) {
        TaskModel taskModel = list.get(holder.getAdapterPosition());

        holder.taskLocation.setText(taskModel.getTaskLocation());
        holder.taskStatus.setText(CommonFunctionsClass.getStringTaskStatus(taskModel.getTaskStatus()));
        holder.taskTitle.setText(taskModel.getTaskTitle());

        getUserProfile(holder, taskModel);
        getTaskOffers(holder, taskModel.getTaskId());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void getTaskOffers(final AdapterTasksWithMyBids.Holder holder, String taskId) {

        MyFirebaseDatabase.TASKS_REFERENCE.child(taskId).child(Constants.STRING_OFFERS_REF).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<TaskBid> taskBidList = new ArrayList<>();
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
                holder.taskOffers.setText(taskBidList.size() + " Offers");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getUserProfile(final AdapterTasksWithMyBids.Holder holder, final TaskModel taskModel) {
        MyFirebaseDatabase.USERS_REFERENCE.child(taskModel.getTaskUploadedBy()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    try {

                        final UserProfileModel userProfileModel = dataSnapshot.getValue(UserProfileModel.class);
                        if (userProfileModel != null) {
                            if (userProfileModel.getUserImageUrl() != null)
                                Picasso.get()
                                        .load(userProfileModel.getUserImageUrl())
                                        .placeholder(R.drawable.image_avatar)
                                        .error(R.drawable.image_avatar)
                                        .centerInside().fit()
                                        .into(holder.userProfilePicture);

                            holder.userRating.setText(userProfileModel.getUserRating() + "(" + userProfileModel.getRatingCounts() + ")");


                            holder.cardTask.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    bundle.putString(UserProfileModel.STRING_USER_PROFILE_ID_REF, list.get(holder.getAdapterPosition()).getTaskUploadedBy());
                                    bundle.putString(TaskModel.STRING_TASK_ID_REF, list.get(holder.getAdapterPosition()).getTaskId());
                                    ((FragmentActivity) context).getSupportFragmentManager()
                                            .beginTransaction()
                                            .add(android.R.id.content, FragmentTaskDescription.getInstance(bundle), Constants.TITLE_TASK_DESCRIPTION)
                                            .addToBackStack(Constants.TITLE_TASK_DESCRIPTION)
                                            .commit();
                                }
                            });

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

    public class Holder extends RecyclerView.ViewHolder {

        private CardView cardTask;
        private CircleImageView userProfilePicture;
        private TextView taskTitle, taskLocation, taskOffers, taskStatus, userRating;

        public Holder(@NonNull View itemView) {
            super(itemView);

            cardTask = itemView.findViewById(R.id.cardTask);
            userProfilePicture = itemView.findViewById(R.id.userProfilePicture);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskLocation = itemView.findViewById(R.id.taskLocation);
            taskOffers = itemView.findViewById(R.id.taskOffers);
            taskStatus = itemView.findViewById(R.id.taskStatus);
            userRating = itemView.findViewById(R.id.userRating);

        }
    }
}
