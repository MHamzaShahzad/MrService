package com.example.mrservice.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.mrservice.R;

public class SignUpActivity extends AppCompatActivity {

    private TextView termConditionSignup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        termConditionSignup = findViewById(R.id.termConditionSignup);
        termConditionSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(SignUpActivity.this, Pop.class));

            }
        });
    }
}
