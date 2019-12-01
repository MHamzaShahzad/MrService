package com.example.mrservice.fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentLargePicture extends Fragment {

    private String imageUrl;

    public static FragmentLargePicture getInstance(String imageUrl){
        return new FragmentLargePicture(imageUrl);
    }

    private FragmentLargePicture(String imageUrl) {
        // Required empty public constructor
        this.imageUrl = imageUrl;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_large_picture, container, false);
        try {
            if (imageUrl != null)
                Picasso.get()
                        .load(imageUrl)
                        .error(R.drawable.ic_launcher_background)
                        .placeholder(R.drawable.ic_launcher_background)
                        .centerInside().fit()
                        .into((ImageView) view.findViewById(R.id.largeImage));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

}
