package com.example.copybike;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class TermsDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_detail);

        initView();
    }


    @Override
    public void onBackPressed() {
        backActivity();
    }

    private void backActivity() {
        Intent intent = new Intent(getBaseContext(), JoinTermsActivity.class);
        startActivity(intent);
        finish();
    }

    private void initView() {
        findViewById(R.id.btn_title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });

        Intent intent = getIntent();

        ((TextView) findViewById(R.id.tv_title)).setText(intent.getStringExtra("TITLE"));
        ((TextView)findViewById(R.id.tv_terms)).setText(intent.getIntExtra("CONTENTS", 0));
    }
}