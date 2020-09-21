package com.example.copybike;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class JoinGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_guide);

        initView();
    }

    @Override
    public void onBackPressed() {
        backActivity();
    }

    private void backActivity() {
        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // 약관동의화면
    private void JoinTermsActivity() {
        Intent intent = new Intent(getBaseContext(), JoinTermsActivity.class);
        startActivity(intent);
        finish();
    }

    // 어울링홈페이지
    private void LinkHomepage() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.sejongbike.kr/"));
        startActivity(intent);
    }

    private void initView() {
        ((TextView)findViewById(R.id.tv_title)).setText("회원가입");

        findViewById(R.id.btn_title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });
        findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JoinTermsActivity();
            }
        });
        findViewById(R.id.btn_homepage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkHomepage();
            }
        });
    }
}