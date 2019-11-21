package com.example.mrservice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    TextView forgetPassword,signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        findViewsById();

    }


    private void findViewsById() {
        forgetPassword= findViewById(R.id.forget_password);
        signUp= findViewById(R.id.signup);
        setClickListeners();
    }
    @Override
    public void onClick(View view) {
        if (view == forgetPassword) {
            Intent i = new Intent(LoginActivity.this, ForgotPaswwordActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

        }
        if (view == signUp) {
            Intent i = new Intent(LoginActivity.this,SignUpActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        }

    }
    private void setClickListeners() {
        forgetPassword.setOnClickListener(this);
        signUp.setOnClickListener(this);


    }
}
