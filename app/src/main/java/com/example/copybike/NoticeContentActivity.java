package com.example.copybike;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class NoticeContentActivity extends AppCompatActivity {
    private static NoticeContentActivity instance;
    private String TAG = "NoticeContentActivity";

    private TextView title;
    private TextView date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_content);
        instance = this;

        initView();
    }

    private void initView(){
        ((TextView)findViewById(R.id.tv_title)).setText("공지사항");

        title = findViewById(R.id.tv_notice_title);
        date = findViewById(R.id.tv_notice_date);

        findViewById(R.id.btn_title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        String pk = intent.getStringExtra("seq");

        noticeRequest("http://1.245.175.54:8080/v1/customer/notice/"+pk);
    }

    private void noticeRequest(String requestUrl){
        Request<JSONObject> request = new JsonObjectRequest(Request.Method.GET, requestUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject results = response.getJSONObject("results");
                            title.setText(results.optString("title"));

                            String dateNotice = "";
                            for(int i=0; i<8; i++){
                                if(i==4 || i==6){
                                    dateNotice += "-";
                                }
                                dateNotice += results.optString("write_dt").charAt(i);
                            }
                            date.setText(dateNotice);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error.toString() : " + error.toString());
                Log.e(TAG, "공지사항 데이터 가져오기 실패");
            }
        });

        if (request != null) {
            request.setShouldCache(false);
            MainActivity.requestQueue.add(request);
        }
    }
}