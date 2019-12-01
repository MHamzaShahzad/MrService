package com.example.mrservice.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;


import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.interfaces.FragmentInteractionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FragmentMapLocationForTask extends Fragment implements OnMapReadyCallback {

    private static final String TAG = FragmentMapLocationForTask.class.getName();
    Context context;
    View view;

    private GoogleMap mMap;

    private FragmentInteractionListener mListener;
    private Bundle arguments;

    public static FragmentMapLocationForTask getInstance(Bundle arguments) {
        return new FragmentMapLocationForTask(arguments);
    }

    private FragmentMapLocationForTask(Bundle arguments) {
        // Required empty public constructor
        this.arguments = arguments;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_LOCATION);
        context = container.getContext();
        /*if (mListener != null)
            mListener.onFragmentInteraction(Constant.TITLE_SELECT_LOCATION);*/
        // Inflate the layout for this fragment
        if (view == null) {

            view = inflater.inflate(R.layout.fragment_map_location_for_task, container, false);


            initMap();

        }
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e(TAG, "onMapReady: ");
        mMap = googleMap;
        getLatLngLocationFromArguments();
    }


    private void initMap() {
        try {

            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.fragment_map);
            mapFragment.getMapAsync(this);

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private void getLatLngLocationFromArguments() {
        if (arguments != null) {
            String address = arguments.getString(Constants.STRING_LOCATION_ADDRESS);
            double addressLat = arguments.getDouble(Constants.STRING_LOCATION_LATITUDE);
            double addressLng = arguments.getDouble(Constants.STRING_LOCATION_LONGITUDE);
            showMakerOnMap(address, addressLat, addressLng);
        }
    }

    private void showMakerOnMap(String address, double latitude, double longitude) {
        if (mMap != null) {
            mMap.clear();
            LatLng latLng = new LatLng(latitude, longitude);
            mMap.addMarker(
                    new MarkerOptions()
                            .title("Address")
                            .snippet(address)
                            .position(latLng)
            ).showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        }
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
            mListener.onFragmentInteractionListener(Constants.TITLE_LOCATION);
    }

}
