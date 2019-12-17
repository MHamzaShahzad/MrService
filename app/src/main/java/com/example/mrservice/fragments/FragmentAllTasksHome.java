package com.example.mrservice.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mrservice.CommonFunctionsClass;
import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.adapters.AdapterAllTasks;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.controllers.SendPushNotificationFirebase;
import com.example.mrservice.interfaces.FragmentInteractionListener;
import com.example.mrservice.interfaces.OnTaskFilterI;
import com.example.mrservice.models.TaskModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class FragmentAllTasksHome extends Fragment implements SwipeRefreshLayout.OnRefreshListener, OnTaskFilterI, View.OnClickListener {

    private static final String TAG = FragmentAllTasksHome.class.getName();
    private Context context;
    private View view;

    private SearchView searchTaskByTitle;
    private ImageButton btnShowTaskOnMap;
    private ImageView btnShowFilters;
    private static RecyclerView recyclerAllTasks;
    private static AdapterAllTasks adapterAllTasks;
    private ArrayAdapter<String> adapterSearchAutoComplete;

    private List<TaskModel> taskModelList;
    private ValueEventListener allTasksValueEventListener;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FragmentInteractionListener mListener;

    private FragmentMapTasks fragmentMapTasks;

    private static HashMap<String, Object> mapFilter;
    private List<String> listTitlesForATS;

    public static FragmentAllTasksHome getInstance() {
        return new FragmentAllTasksHome();
    }

    private FragmentAllTasksHome() {
        // Required empty public constructor
        taskModelList = new ArrayList<>();
        mapFilter = new HashMap<>();
        listTitlesForATS = new ArrayList<>();
        fragmentMapTasks = FragmentMapTasks.getInstance();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_HOME);
        context = container.getContext();
        adapterSearchAutoComplete = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, listTitlesForATS);
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_all_tasks_home, container, false);


            recyclerAllTasks = view.findViewById(R.id.recyclerAllTasks);
            recyclerAllTasks.setHasFixedSize(true);
            recyclerAllTasks.setLayoutManager(new LinearLayoutManager(context));

            initSwipeRefreshLayout();
            initLayoutWidgets();
        }
        return view;
    }

    private void initLayoutWidgets() {
        btnShowFilters = view.findViewById(R.id.btnShowFilters);
        btnShowTaskOnMap = view.findViewById(R.id.btnShowTaskOnMap);

        searchTaskByTitle = view.findViewById(R.id.searchTaskByTitle);
        ImageView searchIcon = searchTaskByTitle.findViewById(R.id.search_button);
        searchIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_search_white));
        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchTaskByTitle.findViewById(R.id.search_src_text);
        searchAutoComplete.setAdapter(adapterSearchAutoComplete);

        btnShowFilters.setOnClickListener(this);
        btnShowTaskOnMap.setOnClickListener(this);

        setSearchListener(searchAutoComplete);
    }

    private void setSearchListener(final SearchView.SearchAutoComplete searchAutoComplete) {
        searchAutoComplete.setTextColor(getResources().getColor(R.color.white));
        searchAutoComplete.setHintTextColor(getResources().getColor(R.color.white));
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                searchAutoComplete.setText(adapterSearchAutoComplete.getItem(i));
                searchAutoComplete.setSelection(searchAutoComplete.getText().toString().length());
            }
        });

        searchTaskByTitle.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(context, "You clicked on search " + searchTaskByTitle.getQuery(), Toast.LENGTH_SHORT).show();
                List<TaskModel> taskModelListTemp = new ArrayList<>();
                for (TaskModel model : taskModelList)
                    if (Pattern.compile(Pattern.quote(searchTaskByTitle.getQuery().toString()), Pattern.CASE_INSENSITIVE).matcher(model.getTaskTitle()).find())
                        taskModelListTemp.add(model);

                taskModelList.clear();
                taskModelList.addAll(taskModelListTemp);
                adapterAllTasks.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void initSwipeRefreshLayout() {
        // SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        /*
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {
                startRefreshing();
                // Fetching data from server
                loadAllTasks(mapFilter);
            }
        });
    }

    private void loadAllTasks(final HashMap<String, Object> map) {
        removeAllTasksEventListener();
        allTasksValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                Log.e(TAG, "onDataChange: " + dataSnapshot);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        taskModelList.clear();
                        listTitlesForATS.clear();
                        if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                            try {
                                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                                for (DataSnapshot snapshot : snapshots) {
                                    TaskModel taskModel = snapshot.getValue(TaskModel.class);
                                    if (taskModel != null) {

                                        boolean matches = true;

                                        for (Map.Entry<String, Object> entry : map.entrySet()) {

                                            switch (entry.getKey()) {

                                                case Constants.STRING_TASK_CITY:
                                                    Log.e(TAG, "run: " + entry.getValue());
                                                    if (taskModel.getTaskLocation() != null && !Pattern.compile(Pattern.quote((String) entry.getValue()), Pattern.CASE_INSENSITIVE).matcher(taskModel.getTaskLocation()).find())
                                                        matches = false;
                                                    break;

                                                case Constants.STRING_TASKS_CAT_ID:
                                                    if (!Pattern.compile(Pattern.quote((String) entry.getValue()), Pattern.CASE_INSENSITIVE).matcher(taskModel.getTaskCategory()).find())
                                                        matches = false;
                                                    break;

                                                case Constants.STRING_TASKS_CAT:
                                                    if (!Pattern.compile(Pattern.quote((String) entry.getValue()), Pattern.CASE_INSENSITIVE).matcher(taskModel.getTaskCatName()).find())
                                                        matches = false;
                                                    break;
                                                case Constants.STRING_SHOW_ONLY_OPEN_TASK:
                                                    if ((Boolean) entry.getValue())
                                                        if (!taskModel.getTaskStatus().equals(Constants.TASKS_STATUS_OPEN))
                                                            matches = false;
                                                    break;

                                                case Constants.STRING_TASK_BUDGET:
                                                    Log.e(TAG, "onDataChange: " + taskModel.getTaskBudget() + " : " + entry.getValue());
                                                    if (Integer.parseInt(taskModel.getTaskBudget()) < (Integer) entry.getValue()) {
                                                        matches = false;
                                                    }
                                                    break;

                                                case Constants.STRING_TASK_TYPE:
                                                    if (!entry.getValue().equals("2") && !taskModel.getTaskType().equals((String) entry.getValue())) {
                                                        matches = false;
                                                    }
                                                    break;

                                            }

                                        }

                                        if (!taskModel.getTaskStatus().equals(Constants.TASKS_STATUS_CANCELLED))
                                            if (CommonFunctionsClass.isOutdated(taskModel.getTaskDueDate()) && taskModel.getTaskStatus().equals(Constants.TASKS_STATUS_OPEN))
                                                makeTaskCancelIfOutDate(taskModel);
                                            else {
                                                if (matches) {
                                                    taskModelList.add(taskModel);
                                                    listTitlesForATS.add(taskModel.getTaskTitle());
                                                }
                                            }
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Log.e(TAG, "onDataChange: " + taskModelList.size());
                        adapterAllTasks = new AdapterAllTasks(context, taskModelList);
                        recyclerAllTasks.setAdapter(adapterAllTasks);
                        adapterSearchAutoComplete.notifyDataSetChanged();
                        fragmentMapTasks.onTaskListUpdate(taskModelList);
                        stopRefreshing();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                stopRefreshing();
            }
        };
        MyFirebaseDatabase.TASKS_REFERENCE.addValueEventListener(allTasksValueEventListener);
    }

    private void makeTaskCancelIfOutDate(final TaskModel taskModel) {
        MyFirebaseDatabase.TASKS_REFERENCE.child(taskModel.getTaskId()).child(TaskModel.STRING_TASK_STATUS_REF).setValue(Constants.TASKS_STATUS_CANCELLED).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                SendPushNotificationFirebase.buildAndSendNotification(context,
                        taskModel.getTaskUploadedBy(),
                        "Task Cancelled!",
                        "Your task has been cancelled due to Outdated."
                );
            }
        });
    }

    private void removeAllTasksEventListener() {
        if (allTasksValueEventListener != null)
            MyFirebaseDatabase.TASKS_REFERENCE.removeEventListener(allTasksValueEventListener);
    }

    private void startRefreshing() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
    }

    private void stopRefreshing() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeAllTasksEventListener();
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
        loadAllTasks(mapFilter);
        super.onResume();
        Log.e(TAG, "onResume: ");
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_HOME);
    }

    @Override
    public void onRefresh() {
        loadAllTasks(mapFilter);
    }

    @Override
    public void onTaskFilter(HashMap<String, Object> mapFilter) {
        Log.e(TAG, "onTaskFilter: " + mapFilter);
        loadAllTasks(mapFilter);
        FragmentAllTasksHome.mapFilter = mapFilter;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnShowFilters:
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(android.R.id.content, FragmentTaskFilter.getInstance(mapFilter, FragmentAllTasksHome.this)).addToBackStack("Tasks Filter").commit();
                break;
            case R.id.btnShowTaskOnMap:
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragmentMapTasks).addToBackStack("Tasks On Map").commit();
                break;
        }
    }
}
