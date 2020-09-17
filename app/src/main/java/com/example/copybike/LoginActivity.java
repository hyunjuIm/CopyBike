package com.example.copybike;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}