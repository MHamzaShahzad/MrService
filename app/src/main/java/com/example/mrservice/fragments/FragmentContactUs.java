package com.example.mrservice.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.interfaces.FragmentInteractionListener;
import com.example.mrservice.models.ContactUsModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

public class FragmentContactUs extends Fragment {

    private Context context;


    EditText input_name, input_email, input_subject, input_message;
    Button btn_send;
    FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;
    private FragmentInteractionListener mListener;

    public static FragmentContactUs getInstance(){
        return new FragmentContactUs();
    }

    private FragmentContactUs() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_CONTACT_US);

        View rootView = inflater.inflate(R.layout.fragment_contact_us, container, false);
        context = container.getContext();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        btn_send = (Button) rootView.findViewById(R.id.btn_send);
        input_name = (EditText) rootView.findViewById(R.id.input_name);
        input_email = (EditText) rootView.findViewById(R.id.input_email);
        input_subject = (EditText) rootView.findViewById(R.id.input_subject);
        input_message = (EditText) rootView.findViewById(R.id.input_message);

        initProgressDialog();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = input_name.getText().toString();
                String email = input_email.getText().toString();
                String subject = input_subject.getText().toString();
                String message = input_message.getText().toString();


                if (name.isEmpty()) {
                    input_name.setError("Enter Your Name");
                } else if (email.isEmpty()) {
                    input_email.setError("Enter Your Email");
                }  else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    input_email.setError("Please enter valid email address.");
                }else if (message.isEmpty()) {
                    input_message.setError("Enter Your Email");
                } else {
                    progressDialog.show();
                    ContactUsModel model = new ContactUsModel(
                            name,
                            email,
                            subject,
                            message
                    );


                    MyFirebaseDatabase.FEEDBACK_REFERENCE.child(firebaseUser.getUid()).child(new Date().toString()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                new AlertDialog.Builder(context, R.style.AlertDialog_Theme)
                                        .setIcon(R.drawable.ic_feedback_black_24dp)
                                        .setTitle("Thank You")
                                        .setMessage("Our Team Will Contact You Soon!")
                                        .setCancelable(false)
                                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ((FragmentActivity) context).getSupportFragmentManager().popBackStack();

                                            }
                                        })
                                        .show();
                            } else {
                                Toast.makeText(context, "Un Sucessfully", Toast.LENGTH_LONG).show();

                            }
                        }
                    });

                }

            }
        });

        return rootView;
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Sending Your Request");
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void setBtn_send() {
        btn_send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String b_name = input_name.getText().toString();
                String b_email = input_email.getText().toString();
                String b_subject = input_subject.getText().toString();
                String b_message = input_message.getText().toString();

                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_TEXT, b_name);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{b_email});
                email.putExtra(Intent.EXTRA_SUBJECT, b_subject);
                email.putExtra(Intent.EXTRA_TEXT, b_message);

                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Choose an Email client :"));

            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    "must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mListener != null) {
            mListener.onFragmentInteractionListener(Constants.TITLE_HOME);
        }
        mListener = null;
    }
}