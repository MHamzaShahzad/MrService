package com.example.mrservice.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mrservice.Constants;
import com.example.mrservice.R;


public class FragmentPostTask extends Fragment {

    public static final String TAG = FragmentPostTask.class.getName();
    private Context context;
    private View view;

    private Bundle bundleData;

    public static FragmentPostTask getInstance(Bundle bundle) {
        return new FragmentPostTask(bundle);
    }

    private FragmentPostTask(Bundle bundle) {
        // Required empty public constructor
        this.bundleData = bundle;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = container.getContext();
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_post_task, container, false);
        }
        return view;
    }

}
