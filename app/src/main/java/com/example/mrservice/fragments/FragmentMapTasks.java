package com.example.mrservice.fragments;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.mrservice.CommonFunctionsClass;
import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.adapters.AdapterTasksMapCustomInfoWindow;
import com.example.mrservice.interfaces.OnTasksListUpdateI;
import com.example.mrservice.models.TaskModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMapTasks extends Fragment implements OnMapReadyCallback, OnTasksListUpdateI, GoogleMap.OnMarkerClickListener {

    private static final String TAG = FragmentMapTasks.class.getName();
    private Context context;
    private View view;

    private GoogleMap mMap;
    private List<TaskModel> taskModelList;
    private List<Marker> markerList;
    private AdapterTasksMapCustomInfoWindow adapterTasksMapCustomInfoWindow;

    public static FragmentMapTasks getInstance() {
        return new FragmentMapTasks();
    }

    private FragmentMapTasks() {
        // Required empty public constructor
        taskModelList = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = container.getContext();
        markerList = new ArrayList<>();
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_map_location_for_task, container, false);


        initMap();

        return view;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                mMap.setMyLocationEnabled(true);

        adapterTasksMapCustomInfoWindow = new AdapterTasksMapCustomInfoWindow(context, mMap);
        mMap.setInfoWindowAdapter(adapterTasksMapCustomInfoWindow);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.3753, 69.3451), 6));

        loadMarkersFromTasksList();
    }

    @Override
    public void onTaskListUpdate(List<TaskModel> taskModelList) {
        this.taskModelList = taskModelList;
        loadMarkersFromTasksList();
    }

    private void loadMarkersFromTasksList() {
        if (mMap != null) {
            markerList.clear();
            for (TaskModel taskModel : taskModelList) {
                if (taskModel.getTaskType().equals(Constants.TASK_TYPE_PHYSICAL) || (taskModel.getTaskLatLng() != null
                        && !taskModel.getTaskLatLng().equals("null") && !taskModel.getTaskLatLng().equals(""))) {
                    Marker marker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(new LatLng(CommonFunctionsClass.getTaskLatitude(taskModel.getTaskLatLng()), CommonFunctionsClass.getTaskLongitude(taskModel.getTaskLatLng())))
                    );
                    marker.setTag(taskModel);
                    markerList.add(marker);
                }
            }
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e(TAG, "onMarkerClick: " + marker.getTag());
        marker.showInfoWindow();

        if (getIndex(marker) != -1) {
            marker.showInfoWindow();
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));

        return true;
    }

    private int getIndex(Marker selectedMarker) {
        for (int i = 0; i < markerList.size(); i++) {
            if (markerList.get(i).equals(selectedMarker))
                return i;
        }
        return -1;
    }

}
