package com.example.copybike;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class NoticeContentActivity extends AppCompatActivity {
    private static NoticeContentActivity instance;
    private String TAG = "NoticeContentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_content);
    }

    private void initView(){
        ((TextView)findViewById(R.id.tv_title)).setText("공지사항");

        findViewById(R.id.btn_title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}