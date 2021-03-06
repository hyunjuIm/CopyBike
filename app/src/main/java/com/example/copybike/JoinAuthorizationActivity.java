package com.example.copybike;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.example.copybike.util.AgeValidation;

import java.net.URISyntaxException;

public class JoinAuthorizationActivity extends AppCompatActivity {
    private static JoinAuthorizationActivity instance;
    private String TAG = "JoinAuthorizationActivity";

    private WebView webAuth = null;

    private boolean authConnectFailed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_authorization);
        instance = this;

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

    //자바스크립트에서 안드로이드 호출
    @SuppressLint("JavascriptInterface")
    private void initView() {
        ((TextView)findViewById(R.id.tv_title)).setText("본인인증");

        findViewById(R.id.btn_title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });

        webAuth = (WebView) findViewById(R.id.webAuth);

        // android 5.0부터 앱에서 API수준21이상을 타겟킹하는 경우 아래추가
        //시스템은 기본적으로 혼합 콘텐츠와 타사 쿠키를 차단
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //혼합 콘텐츠와 타사 쿠키를 허용
            webAuth.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webAuth, true);
        }

        webAuth.getSettings().setJavaScriptEnabled(true); //자바 스크립트로 이루어진 기능 사용
        webAuth.addJavascriptInterface(new AuthBridge(), "AuthApp");
        webAuth.setWebChromeClient(new WebChromeClient());
        webAuth.setWebViewClient(new WebViewClient(){
            //로딩이 시작될 때
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (url.toLowerCase().trim().contains("play.google.com")) {
                    view.stopLoading();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } else if (url.toLowerCase().trim().contains("intent://")) {
                    String uri = url.toLowerCase().trim();
                    int packageIndex = uri.indexOf("package=");
                    String appPackage = uri.substring(packageIndex + 8, uri.indexOf(";", packageIndex));

                    PackageManager pm = getApplicationContext().getPackageManager();

                    authConnectFailed = false;

                    if (isPackageInstalled(appPackage, pm)) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            authConnectFailed = true;
                            view.stopLoading();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
                            startActivity(intent);
                        }
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
                        startActivity(intent);
                    }
                }
            }

            // 새로운 URL이 webview에 로드되려 할 경우 컨트롤을 대신할 기회를 줌
            // return이 true면 새로운 url을 호출하기 위해 외부 브라우저를 더이상 찾지 않게 됨
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if ((url.startsWith("http://") || url.startsWith("https://")) && (url.contains("market.android.com")
                        || url.contains("m.ahnlab.com/kr/site/download"))) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    setIntentSecurity(intent);

                    try {
                        view.getContext().startActivity(intent, Bundle.EMPTY);
                        return true;
                    } catch (ActivityNotFoundException e) {
                        return false;
                    }
                } else if (url != null && url != "ansimclick.hyundiacard.com"
                        && (url.contains("com.sktelecom.tauth") || url.contains("com.kt.ktauth")
                        || url.contains("com.lguplus.smartotp") || url.contains("samsungpass")
                        || url.contains("market://") || url.contains("tel:"))) {
                    try {

                        Intent intent = null;
                        try {
                            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            setIntentSecurity(intent);
                            Log.e("intent getScheme+++===>", intent.getScheme());
                            Log.e("intent getData+++===>", intent.getDataString());
                        } catch (URISyntaxException ex) {
                            Log.e("Browser", "Bad URI " + url + ":" + ex.getMessage());
                            return false;
                        }
                        //chrome 버젼 방식 : 2014.01 추가
                        if (url.startsWith("intent")) { // chrome 버젼 방식
                            // 앱설치 체크를 합니다.
                            //설정에 맞는 액티비티를 찾고, 이 중에 어떤 액티비티를 실행할지 결정해줍니다.
                            if (getPackageManager().resolveActivity(intent, 0) == null) {
                                String packagename = intent.getPackage();
                                if (packagename != null) {
                                    Uri uri = Uri.parse("market://search?q=pname:"
                                            + packagename);
                                    intent = new Intent(Intent.ACTION_VIEW, uri);
                                    setIntentSecurity(intent);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        view.getContext().startActivity(intent, Bundle.EMPTY);
                                    } else {
                                        startActivity(intent);
                                    }
                                    return true;
                                }
                            }

                            //구동방식은 PG:쇼핑몰에서 결정하세요.
                            int runType=1;

                            if (runType == 1) {
                                Uri uri = Uri.parse(intent.getDataString());
                                intent = new Intent(Intent.ACTION_VIEW, uri);
                                setIntentSecurity(intent);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    view.getContext().startActivity(intent, Bundle.EMPTY);
                                } else {
                                    startActivity(intent);
                                }
                            } else {
                                setIntentSecurity(intent);
                                try {
                                    if (startActivityIfNeeded(intent, -1)) {
                                        return true;
                                    }
                                } catch (ActivityNotFoundException ex) {
                                    return false;
                                }
                            }
                        } else { // 구 방식
                            Uri uri = Uri.parse(url);
                            intent = new Intent(Intent.ACTION_VIEW, uri);
                            setIntentSecurity(intent);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                view.getContext().startActivity(intent, Bundle.EMPTY);
                            } else {
                                startActivity(intent);
                            }
                        }
                    } catch (ActivityNotFoundException e) {
                        Log.e("error ===>", e.getMessage());
                        System.out.println("예외발생");
                        return false;
                    }
                } else if (url.startsWith("https://")) {
                    view.loadUrl(url);
                    return false;
                } else {
                    return false;
                }
                return true;
            }
        });

        webAuth.loadUrl("http://www.sejongbike.kr/appserver/auth/NiceLoading.jsp?");
    }

    //스키마를 통해 외부 앱을 킬때는 Browasable로 등록된 앱들만 킬 수 있도록하여, 사용자의 보안을 향상시킨다
    private void setIntentSecurity(Intent intent) {
        //BROWSABLE 카테고리에 없는 액티비티 실행 X
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        //null : 명시적으로 불러오지 않는다 -> 인텐트를 처리할 어플리케이션 구성요소의 이름 또는 시스템이 자동으로 찾는다
        intent.setComponent(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            intent.setSelector(null);
        }
    }

    private void joinActivity(String name, String type, String safe_key, String jumin, String ipin_di, String birth, String gender) {
        Intent intent = new Intent(getBaseContext(), JoinActivity.class);
        intent.putExtra("NAME", name);
        intent.putExtra("TYPE", type);
        intent.putExtra("SAFE_KEY", safe_key);
        intent.putExtra("JUMIN", jumin);
        intent.putExtra("IPIN_DI", ipin_di);
        intent.putExtra("BIRTH", birth);
        intent.putExtra("GENDER", gender);
        Log.e("본인인증에서 받아온 정보 : ", name + type + safe_key + jumin + ipin_di + birth + gender);
        startActivity(intent);
        finish();
    }

    //PackageManager : 현재 디바이스에 설치되어 있는 애플리케이션 패키지와 정보를 검색하는 클래스
    private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            //시스템에 설치된 애플리케이션 패키지에 대한 전체 정보 검색
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private class AuthBridge{
        @JavascriptInterface
        public void Result(final String name, String type, String safe_key, String jumin, String ipin_di, String birth, String gender) {
            // 본인 인증 완료
            if (birth.trim().length() == 8) {
                // 만 19세 이상이면 통과, 아니면 팝업
                if (AgeValidation.isAge19(birth.trim())) {
                    joinActivity(name, type, safe_key, jumin, ipin_di, birth, gender);
                } else {
                    Toast.makeText(instance, "만 19세 미만입니다.", Toast.LENGTH_LONG).show();
                    backActivity();
                }
            } else {
                Toast.makeText(instance, "인증에 실패하였습니다.", Toast.LENGTH_LONG).show();
                backActivity();
            }
        }

        @JavascriptInterface
        public void BestClose() {
            // 에러 발생 및 취소
            Toast.makeText(instance, "인증에 실패하였습니다.", Toast.LENGTH_LONG).show();
            backActivity();
        }

        @JavascriptInterface
        public void hideDialog() {
            webAuth.post(new Runnable() {
                @Override
                public void run() {
                    webAuth.loadUrl("javascript:danal_start()");
                }
            });
        }
    }
}