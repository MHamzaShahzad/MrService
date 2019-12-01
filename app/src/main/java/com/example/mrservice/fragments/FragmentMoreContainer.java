package com.example.mrservice.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mrservice.CommonFunctionsClass;
import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.interfaces.FragmentInteractionListener;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMoreContainer extends Fragment implements View.OnClickListener {

    private static final String TAG = FragmentMoreContainer.class.getName();
    private Context context;
    private View view;

    private TextView btnLogout, btnProfile, btnAdminLogin, btnContactUs, btnAboutUs;

    private FirebaseAuth mAuth;

    private FragmentCreateEditProfile fragmentCreateEditProfile;
    private FragmentContactUs fragmentContactUs;
    private FragmentAboutUs fragmentAboutUs;

    private FragmentInteractionListener mListener;

    public static FragmentMoreContainer getInstance() {
        return new FragmentMoreContainer();
    }

    private FragmentMoreContainer() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_MORE_OPTIONS);
        context = container.getContext();
        mAuth = FirebaseAuth.getInstance();
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_more_container, container, false);

            findViewById();
            initFragments();
        }
        return view;
    }

    private void findViewById() {
        btnLogout = view.findViewById(R.id.btnLogout);
        btnProfile = view.findViewById(R.id.btnProfile);
        btnAdminLogin = view.findViewById(R.id.btnAdminLogin);
        btnContactUs = view.findViewById(R.id.btnContactUs);
        btnAboutUs = view.findViewById(R.id.btnAboutUs);
        setClickListeners();
    }

    private void setClickListeners() {
        btnLogout.setOnClickListener(this);
        btnProfile.setOnClickListener(this);
        btnAdminLogin.setOnClickListener(this);
        btnContactUs.setOnClickListener(this);
        btnAboutUs.setOnClickListener(this);
    }

    private void initFragments() {
        fragmentCreateEditProfile = FragmentCreateEditProfile.getInstance();
        fragmentContactUs = FragmentContactUs.getInstance();
        fragmentAboutUs = FragmentAboutUs.getInstance();

    }

    @Override
    public void onClick(View view) {

        if (view == btnProfile) {
            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragmentCreateEditProfile).addToBackStack(Constants.TITLE_PROFILE).commit();
        }
        if (view == btnLogout) {
            CommonFunctionsClass.logoutUser(mAuth, context);
        }
        if (view == btnAdminLogin) {
            CommonFunctionsClass.getAdminLoginDialog(context);
        }
        if (view == btnContactUs) {
            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragmentContactUs).addToBackStack(Constants.TITLE_PROFILE).commit();
        }
        if (view == btnAboutUs) {
            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragmentAboutUs).addToBackStack(Constants.TITLE_PROFILE).commit();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_MORE_OPTIONS);
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
