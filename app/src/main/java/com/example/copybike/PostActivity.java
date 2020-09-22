package com.example.copybike;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.example.copybike.util.NetworkCheck;

public class PostActivity extends AppCompatActivity {

    private WebView webPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        initView();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void initView() {
        ((TextView)findViewById(R.id.tv_title)).setText("주소찾기");

        findViewById(R.id.btn_title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        webPost = (WebView) findViewById(R.id.webPost);

        webPost.getSettings().setJavaScriptEnabled(true);
        webPost.getSettings().setLoadsImagesAutomatically(true);
        webPost.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webPost.getSettings().setSupportMultipleWindows(false);
        webPost.getSettings().setDomStorageEnabled(true);
        webPost.getSettings().setLoadWithOverviewMode(true);

        webPost.addJavascriptInterface(new AndroidBridge(), "Post");
        webPost.setWebChromeClient(new WebChromeClient());
        webPost.setWebViewClient(new CustomWebViewClient());

        webPost.loadUrl("http://www.sejongbike.kr/appserver/post/daumPost.html");
    }

    private class AndroidBridge {
        // 다음웹으로부터 우편번호 및 주소 넘겨받기
        @JavascriptInterface
        public void setAddress(final String postcode, final String address, final String building, final String dong) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("postcode", postcode);
            returnIntent.putExtra("address", address);
            returnIntent.putExtra("building", building);
            returnIntent.putExtra("dong", dong);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    private static class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

}