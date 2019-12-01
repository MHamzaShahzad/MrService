package com.example.mrservice.admin;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.mrservice.R;
import com.example.mrservice.adapters.AdapterAllTasksCat;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.models.TaskCat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentTaskCategoriesAdmin extends Fragment {

    private static final String TAG = FragmentTaskCategoriesAdmin.class.getName();
    private Context context;
    private View view;

    private ImageButton btnClose;
    private FloatingActionButton fab_add_new_task_cat;
    private RecyclerView recycler_task_cat;
    private AdapterAllTasksCat adapterAllTasksCat;

    private List<TaskCat> taskCatList;

    private ValueEventListener tasksCatEventListener;

    private boolean isAdmin;

    public static FragmentTaskCategoriesAdmin getInstance(boolean isAdmin){
        return new FragmentTaskCategoriesAdmin(isAdmin);
    }

    private FragmentTaskCategoriesAdmin(boolean isAdmin) {
        // Required empty public constructor
        taskCatList = new ArrayList<>();
        this.isAdmin = isAdmin;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = container.getContext();
        adapterAllTasksCat = new AdapterAllTasksCat(context, taskCatList, isAdmin);
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_task_categories_admin, container, false);
            initLayoutWidgets();
            initRecyclerView();

            setFabAddNewTaskCat();
            setBtnClose();
            loadTasksCat();
        }
        return view;
    }

    private void initLayoutWidgets() {
        fab_add_new_task_cat = view.findViewById(R.id.fab_add_new_task_cat);
        recycler_task_cat = view.findViewById(R.id.recycler_task_cat);
        btnClose = view.findViewById(R.id.btnClose);

        if (!isAdmin) {
            fab_add_new_task_cat.hide();
        }
    }

    private void setFabAddNewTaskCat() {
        fab_add_new_task_cat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentCatCRUD.getInstance().show(((FragmentActivity) context).getSupportFragmentManager(), "");

            }
        });
    }

    private void initRecyclerView() {
        recycler_task_cat.setHasFixedSize(true);
        recycler_task_cat.setLayoutManager(new GridLayoutManager(context, 2));
        recycler_task_cat.setAdapter(adapterAllTasksCat);
    }

    private void loadTasksCat() {
        tasksCatEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "onDataChange: " + dataSnapshot);
                taskCatList.clear();
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    try {

                        Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                        for (DataSnapshot snapshot : snapshots) {

                            TaskCat cat = snapshot.getValue(TaskCat.class);
                            if (cat != null)
                                taskCatList.add(cat);

                        }

                        Log.e(TAG, "onDataChange: " + taskCatList.size());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                adapterAllTasksCat.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        MyFirebaseDatabase.TASKS_CAT.addValueEventListener(tasksCatEventListener);
    }

    private void setBtnClose() {
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FragmentActivity) context).getSupportFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tasksCatEventListener != null)
            MyFirebaseDatabase.TASKS_CAT.removeEventListener(tasksCatEventListener);
    }
}
