package com.example.fypapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button loginButtonMain,SignupButtonMain;
    public SharedPreferences.Editor edt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButtonMain = findViewById(R.id.loginBtnMain);
        SignupButtonMain = findViewById(R.id.SignUpBtnMain);

        loginButtonMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginHome.class);
                startActivity(intent);
            }
        });

        SignupButtonMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GuardianSignUp.class);
                startActivity(intent);
            }
        });
    }
}