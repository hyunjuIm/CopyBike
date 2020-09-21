package com.example.copybike;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class TermsDetailActivity extends AppCompatActivity {

    private TermsDetailActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_detail);
        instance = this;

        initView();
    }


    @Override
    public void onBackPressed() {
        backActivity();
    }

    private void backActivity() {
        try {
            // 돌아가야할 액티비티의 Class를 얻음
            String getActivity = getIntent().getStringExtra("ACTIVITY").trim().split("\\$")[0];
            Class c = Class.forName(getActivity);

            // 액티비티 실행 및 약관화면 종료
            Intent intent = new Intent(getBaseContext(), c);
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException e) {
            finish();
        }
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