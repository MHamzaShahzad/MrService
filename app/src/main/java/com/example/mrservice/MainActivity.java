package com.example.mrservice;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

public class MainActivity extends AppCompatActivity {

    SpaceNavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationView= findViewById(R.id.space);

        navigationView.initWithSaveInstanceState(savedInstanceState);


        navigationView.addSpaceItem(new SpaceItem("Message", R.drawable.ic_message_black_24dp));
        navigationView.addSpaceItem(new SpaceItem("Post task", R.drawable.ic_task_black_24dp));
        navigationView.addSpaceItem(new SpaceItem("My task", R.drawable.ic_mytask_black_24dp));
        navigationView.addSpaceItem(new SpaceItem("More", R.drawable.ic_more_vert_black_24dp));


        navigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                Toast.makeText(MainActivity.this, "onCentreButtonClick", Toast.LENGTH_SHORT).show();
                navigationView.setCentreButtonSelectable(true);
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {

                Toast.makeText(MainActivity.this, itemIndex + " " + itemName, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {
                Toast.makeText(MainActivity.this, itemIndex + " " + itemName, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
