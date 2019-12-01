package com.example.mrservice.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.mrservice.CommonFunctionsClass;
import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.admin.FragmentTaskCategoriesAdmin;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.fragments.FragmentAllTasksHome;
import com.example.mrservice.fragments.FragmentCreateEditProfile;
import com.example.mrservice.fragments.FragmentMoreContainer;
import com.example.mrservice.fragments.FragmentMyAllTasks;
import com.example.mrservice.fragments.FragmentMyBids;
import com.example.mrservice.interfaces.FragmentInteractionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

public class MainActivity extends AppCompatActivity implements FragmentInteractionListener {

    private Context context;

    private SpaceNavigationView navigationView;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    private FragmentMoreContainer fragmentMoreContainer;
    private FragmentAllTasksHome fragmentAllTasksHome;
    private FragmentCreateEditProfile fragmentCreateEditProfile;
    private FragmentMyBids fragmentMyBids;
    private FragmentMyAllTasks fragmentMyAllTasks;
    private FragmentTaskCategoriesAdmin fragmentTaskCategories;

    private static final String TAG = MainActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        initHomeFragments();

        navigationView = findViewById(R.id.space);
        navigationView.initWithSaveInstanceState(savedInstanceState);

        navigationView.addSpaceItem(new SpaceItem("My tasks", R.drawable.ic_task_black_24dp));
        navigationView.addSpaceItem(new SpaceItem("My bids", R.drawable.ic_mytask_black_24dp));
        navigationView.addSpaceItem(new SpaceItem("Post task", R.drawable.ic_message_black_24dp));
        navigationView.addSpaceItem(new SpaceItem("More", R.drawable.ic_more_vert_black_24dp));

        navigationView.showTextOnly();

        navigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                Toast.makeText(MainActivity.this, "onCentreButtonClick", Toast.LENGTH_SHORT).show();

                navigationView.setCentreButtonSelectable(true);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_home, fragmentAllTasksHome).commit();
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                replaceViews(itemIndex);
                Toast.makeText(MainActivity.this, itemIndex + " " + itemName, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {
                replaceViews(itemIndex);
                Toast.makeText(MainActivity.this, itemIndex + " " + itemName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStateNotSaved() {
        super.onStateNotSaved();
        FirebaseMessaging.getInstance().subscribeToTopic(firebaseUser.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Log.e(TAG, "onComplete: SUBSCRIBED!");
                else
                    Log.e(TAG, "onComplete: CAN'T SUBSCRIBE!" );
            }
        });
    }

    private void replaceViews(int index) {
        switch (index) {
            case 0:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_home, fragmentMyAllTasks).commit();
                break;
            case 1:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_home, fragmentMyBids).commit();
                break;
            case 2:
                checkIfProfileCreatedAndMoveToPostTask();
                break;
            case 3:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_home, fragmentMoreContainer).commit();
                break;
        }
    }

    private void initHomeFragments() {
        fragmentMoreContainer = FragmentMoreContainer.getInstance();
        fragmentAllTasksHome = FragmentAllTasksHome.getInstance();
        fragmentTaskCategories = FragmentTaskCategoriesAdmin.getInstance(false);
        fragmentCreateEditProfile = FragmentCreateEditProfile.getInstance();
        fragmentMyAllTasks = FragmentMyAllTasks.getInstance();
        fragmentMyBids = FragmentMyBids.getInstance();
    }

    private void checkIfProfileCreatedAndMoveToPostTask() {
        MyFirebaseDatabase.USERS_REFERENCE.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragmentTaskCategories).addToBackStack(Constants.TITLE_SELECT_TASK_CATEGORY).commit();
                } else {
                    CommonFunctionsClass.showViewIf(context, "Alert", "Complete your profile to continue!", fragmentCreateEditProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void alertDialogForBackPress() {
        new AlertDialog.Builder(this,
                R.style.AlertDialog_Theme)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to quit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseMessaging.getInstance().subscribeToTopic(firebaseUser.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.e(TAG, "onComplete: SUCCESSFUL" );
                }else {
                    Log.e(TAG, "onComplete: UN_SUCCESSFUL" );
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            alertDialogForBackPress();
        } else
            super.onBackPressed();

    }

    @Override
    public void onFragmentInteractionListener(String title) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }
}
