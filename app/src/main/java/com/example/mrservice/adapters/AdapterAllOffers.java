package com.example.mrservice.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.controllers.SendPushNotificationFirebase;
import com.example.mrservice.fragments.FragmentViewProfile;
import com.example.mrservice.interfaces.OnTaskModelUpdateI;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterAllOffers extends RecyclerView.Adapter<AdapterAllOffers.Holder> implements OnTaskModelUpdateI {
    private static final String TAG = AdapterAllOffers.class.getName();
    private Context context;
    private TaskModel taskModel;
    private List<TaskBid> list;
    private FirebaseUser firebaseUser;

    public AdapterAllOffers(Context context, TaskModel taskModel, List<TaskBid> list) {
        this.context = context;
        this.taskModel = taskModel;
        this.list = list;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public AdapterAllOffers.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_offer, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterAllOffers.Holder holder, int position) {

        holder.btnAcceptOffer.setVisibility(View.INVISIBLE);
        holder.textOfferMessage.setVisibility(View.INVISIBLE);

        TaskBid taskBid = list.get(holder.getAdapterPosition());

        if (taskModel.getTaskUploadedBy().equals(firebaseUser.getUid())) {

            if (taskModel.getTaskStatus().equals(Constants.TASKS_STATUS_OPEN)) {

                holder.btnAcceptOffer.setVisibility(View.VISIBLE);
                holder.textOfferMessage.setVisibility(View.VISIBLE);
                holder.textOfferMessage.setText(taskBid.getBidMessage());
                setBtnAcceptOffer(holder, taskBid);

            } else {

                if (taskModel.getTaskAssignedTo().equals(taskBid.getBidBuyerId())) {
                    holder.btnAcceptOffer.setVisibility(View.VISIBLE);
                    holder.textOfferMessage.setVisibility(View.VISIBLE);
                    holder.textOfferMessage.setText(taskBid.getBidMessage());
                    holder.btnAcceptOffer.setEnabled(false);
                    holder.textOfferMessage.setEnabled(false);
                    switch (taskModel.getTaskStatus()) {
                        case Constants.TASKS_STATUS_ASSIGNED:
                            holder.btnAcceptOffer.setText(Constants.STRING_TASKS_STATUS_ASSIGNED);
                            break;
                        case Constants.TASKS_STATUS_COMPLETED:
                            holder.btnAcceptOffer.setText(Constants.STRING_TASKS_STATUS_COMPLETED);
                            break;
                        case Constants.TASKS_STATUS_CANCELLED:
                            holder.btnAcceptOffer.setText(Constants.STRING_TASKS_STATUS_CANCELLED);
                            break;
                        case Constants.TASKS_STATUS_REVIEWED:
                            holder.btnAcceptOffer.setText(Constants.STRING_TASKS_STATUS_REVIEWED);
                            break;
                    }
                }

            }

        }

        getBuyerDetails(holder, list.get(holder.getAdapterPosition()).getBidBuyerId());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onTaskModelUpdate(TaskModel taskModel) {
        this.taskModel = taskModel;
    }

    public class Holder extends RecyclerView.ViewHolder {

        private CircleImageView profileImage;
        private TextView userName, userRating, textOfferMessage;
        private RatingBar ratingBar;
        private Button btnAcceptOffer;

        public Holder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            userRating = itemView.findViewById(R.id.userRating);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            textOfferMessage = itemView.findViewById(R.id.textOfferMessage);
            btnAcceptOffer = itemView.findViewById(R.id.btnAcceptOffer);

        }
    }

    private void getBuyerDetails(final Holder holder, String buyerId) {
        MyFirebaseDatabase.USERS_REFERENCE.child(buyerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {

                    try {

                        UserProfileModel userProfileModel = dataSnapshot.getValue(UserProfileModel.class);

                        if (userProfileModel != null) {

                            if (userProfileModel.getUserImageUrl() != null && !userProfileModel.getUserImageUrl().equals("null") && !userProfileModel.getUserImageUrl().equals(""))
                                Picasso.get()
                                        .load(userProfileModel.getUserImageUrl())
                                        .placeholder(R.drawable.image_avatar)
                                        .error(R.drawable.image_avatar)
                                        .centerInside().fit()
                                        .into(holder.profileImage);

                            holder.userName.setText(userProfileModel.getUserName());
                            holder.userRating.setText(userProfileModel.getUserRating() + "(" + userProfileModel.getRatingCounts() + ")");
                            holder.ratingBar.setRating(userProfileModel.getUserRating());

                            seeUserProfile(holder, userProfileModel);
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

    private void setBtnAcceptOffer(final Holder holder, final TaskBid taskBid) {

        holder.btnAcceptOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> mapForUpdate = new HashMap<>();
                mapForUpdate.put(TaskModel.STRING_TASK_ASSIGNED_TO_REF, taskBid.getBidBuyerId());
                mapForUpdate.put(TaskModel.STRING_TASK_STATUS_REF, Constants.TASKS_STATUS_ASSIGNED);
                MyFirebaseDatabase.TASKS_REFERENCE.child(taskModel.getTaskId()).updateChildren(mapForUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Offer accepted successfully!", Toast.LENGTH_LONG).show();
                            SendPushNotificationFirebase.buildAndSendNotification(context,
                                    taskBid.getBidBuyerId(),
                                    "Offer Accepted",
                                    "Your, bid request has been accepted!");
                        } else
                            Toast.makeText(context, "Offer can't be accepted!", Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
    }

    private void seeUserProfile(Holder holder, final UserProfileModel profileModel) {
        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.STRING_USER_PROFILE_OBJECT, profileModel);
                ((FragmentActivity) context)
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content
                                , FragmentViewProfile.getInstance(bundle))
                        .addToBackStack(null)
                        .commit();

            }
        });
    }

}

