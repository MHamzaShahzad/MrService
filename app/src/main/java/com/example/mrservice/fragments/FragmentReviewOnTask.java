package com.example.mrservice.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.controllers.SendPushNotificationFirebase;
import com.example.mrservice.interfaces.FragmentInteractionListener;
import com.example.mrservice.models.TaskModel;
import com.example.mrservice.models.UserProfileModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class FragmentReviewOnTask extends DialogFragment {

    private Context context;

    private TextView closeReviewDialog;
    private EditText inputReviewMessage;
    private RatingBar ratingBarReviewTask;
    private Button btnSubmitReview;

    private FragmentInteractionListener mListener;

    private Bundle arguments;

    public static FragmentReviewOnTask getInstance(Bundle arguments){
        return new FragmentReviewOnTask(arguments);
    }

    private FragmentReviewOnTask(Bundle arguments) {
        // Required empty public constructor
        this.arguments = arguments;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_REVIEW_TASK);
        context = getContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.fragment_review_on_task, null);
        initDialogWidgets(view);
        builder.setView(view);
        getArgumentAsBundle();
        return builder.create();
    }

    private void initDialogWidgets(View view) {

        btnSubmitReview = view.findViewById(R.id.btnSubmitReview);
        ratingBarReviewTask = view.findViewById(R.id.ratingBarReviewTask);
        inputReviewMessage = view.findViewById(R.id.inputReviewMessage);
        closeReviewDialog = view.findViewById(R.id.closeReviewDialog);

        setCloseReviewDialog();

    }

    private void setCloseReviewDialog() {
        closeReviewDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentReviewOnTask.this.dismiss();
            }
        });
    }

    private void getArgumentAsBundle() {
        if (arguments != null) {
            try {

                TaskModel taskModel = (TaskModel) arguments.getSerializable(Constants.STRING_TASK_OBJECT);
                if (taskModel != null) {
                    review(taskModel);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void review(final TaskModel taskModel) {
        btnSubmitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map<String, Object> mapForReview = new HashMap<>();
                mapForReview.put(TaskModel.STRING_TASK_STATUS_REF, Constants.TASKS_STATUS_REVIEWED);
                mapForReview.put(TaskModel.STRING_TASK_REVIEW_BY_SELLER_MESSAGE_REF, inputReviewMessage.getText().toString().trim());
                MyFirebaseDatabase.TASKS_REFERENCE.child(taskModel.getTaskId()).updateChildren(mapForReview).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Successfully!", Toast.LENGTH_LONG).show();

                            MyFirebaseDatabase.USERS_REFERENCE.child(taskModel.getTaskAssignedTo()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                                        try {

                                            UserProfileModel userProfileModel = dataSnapshot.getValue(UserProfileModel.class);
                                            if (userProfileModel != null) {

                                                int ratingCounts = userProfileModel.getRatingCounts();
                                                int newRatingCounts = userProfileModel.getRatingCounts() + 1;
                                                float rating = userProfileModel.getUserRating();

                                                float newRating = ((rating * ratingCounts) + ratingBarReviewTask.getRating()) / newRatingCounts;

                                                Map<String, Object> mapForReview = new HashMap<>();
                                                mapForReview.put(UserProfileModel.STRING_RATING_REF, newRating);
                                                mapForReview.put(UserProfileModel.STRING_RATING_COUNTS_REF, newRatingCounts);
                                                dataSnapshot.getRef().updateChildren(mapForReview).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(context, "Successfully!", Toast.LENGTH_LONG).show();
                                                            SendPushNotificationFirebase.buildAndSendNotification(context, taskModel.getTaskUploadedBy(), "Task Reviewed!", "Your task has been reviewed by provider.");
                                                            FragmentReviewOnTask.this.dismiss();
                                                        } else
                                                            Toast.makeText(context, "Un-Successful!", Toast.LENGTH_LONG).show();
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

                        } else
                            Toast.makeText(context, "Un-Successful!", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
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
            mListener.onFragmentInteractionListener(Constants.TITLE_REVIEW_TASK);
    }

}
