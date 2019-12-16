package com.example.mrservice.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;


import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.controllers.SendPushNotificationFirebase;
import com.example.mrservice.interfaces.FragmentInteractionListener;
import com.example.mrservice.models.TaskBid;
import com.example.mrservice.models.TaskModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMakeOffer extends DialogFragment {

    private static final String TAG = FragmentMakeOffer.class.getName();
    private Context context;

    private EditText inputOfferMessage;
    private Button btnMakeOffer;

    private FirebaseUser firebaseUser;

    private TaskModel taskModel;
    private FragmentInteractionListener mListener;

    private Bundle arguments;

    public static FragmentMakeOffer getInstance(Bundle arguments) {
        return new FragmentMakeOffer(arguments);
    }

    private FragmentMakeOffer(Bundle arguments) {
        // Required empty public constructor
        this.arguments = arguments;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_MAKE_OFFER);
        context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.fragment_make_offer, null);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        inputOfferMessage = view.findViewById(R.id.inputOfferMessage);
        btnMakeOffer = view.findViewById(R.id.btnMakeOffer);


        if (arguments != null) {
            taskModel = (TaskModel) arguments.getSerializable(Constants.STRING_TASK_OBJECT);
        }

        btnMakeOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (inputOfferMessage.length() > 0)
                    MyFirebaseDatabase.TASK_OFFERS_REFERENCE.child(taskModel.getTaskId()).child(firebaseUser.getUid()).setValue(buildBidInstance()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Offer Sent Successfully!", Toast.LENGTH_LONG).show();
                                SendPushNotificationFirebase.buildAndSendNotification(
                                        context,
                                        taskModel.getTaskUploadedBy(),
                                        "New Offer!",
                                        "Your task has new Offer");
                                FragmentMakeOffer.this.dismiss();
                            } else
                                Toast.makeText(context, "Offer Could't be sent!", Toast.LENGTH_LONG).show();
                        }
                    });
                else
                    inputOfferMessage.setError("Field is required!");

            }
        });

        builder.setView(view);
        return builder.create();
    }

    private TaskBid buildBidInstance() {
        return new TaskBid(
                firebaseUser.getUid(),
                inputOfferMessage.getText().toString().trim()
        );
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
            mListener.onFragmentInteractionListener(Constants.TITLE_MAKE_OFFER);
    }

}
