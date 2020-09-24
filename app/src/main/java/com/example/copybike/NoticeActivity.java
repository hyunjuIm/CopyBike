package com.example.copybike;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.copybike.common.NaverMapHelper;
import com.example.copybike.ui.NoticeListViewItem;
import com.example.copybike.ui.NoticeViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NoticeActivity extends Activity implements AbsListView.OnScrollListener {
    private static NoticeActivity instance;
    private String TAG = "NoticeActivity";

    private ProgressDialog progressDialog;

    private ListView noticeListView;
    private NoticeViewAdapter adapter;

    private boolean mLockListView;
    private String next = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        instance = this;

        initView();
    }

    private void initView(){
        ((TextView)findViewById(R.id.tv_title)).setText("공지사항");

        findViewById(R.id.btn_title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        noticeListView = (ListView) findViewById(R.id.notice_list);
        adapter = new NoticeViewAdapter();
        noticeListView.setOnScrollListener(instance);
        noticeListView.setAdapter(adapter);

        noticeListRequest("http://1.245.175.54:8080/v1/customer/notice");
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //현재 처음보이는 셀번호와 보여지는 셀번호 값 = 전체 숫자 -> 가장 아래 스크롤
        int count = totalItemCount - visibleItemCount;

        if(firstVisibleItem >= count && totalItemCount != 0 && mLockListView == false){
            noticeListRequest(next);
        }
    }

    private void noticeListRequest(String requestUrl){
        mLockListView = true;

        Request<JSONObject> request = new JsonObjectRequest(Request.Method.GET, requestUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            next = response.getString("next");

                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject notice = results.getJSONObject(i);
                                NoticeListViewItem item = new NoticeListViewItem();
                                item.setNumber(notice.optString("board_seq"));
                                item.setWriter(notice.optString("writer"));
                                item.setInputDate(notice.optString("write_dt"));
                                item.setTitle(notice.optString("title"));
                                adapter.addItem(item);
                            }
                            adapter.notifyDataSetChanged();

                            mLockListView = false;

                            if(progressDialog != null){
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error.toString() : " + error.toString());
                Log.e(TAG, "공지사항 리스트 데이터 가져오기 실패");
            }
        });

        if(progressDialog == null) {
            progressDialog = new ProgressDialog(instance);
            progressDialog.setMessage("공지사항 불러오는중...");
        }
        progressDialog.show();

        request.setRetryPolicy(new DefaultRetryPolicy(
                72000000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        if (request != null) {
            request.setShouldCache(false);
            MainActivity.requestQueue.add(request);
        }
    }

}