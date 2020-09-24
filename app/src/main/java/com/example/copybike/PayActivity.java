package com.example.copybike;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Browser;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class PayActivity extends AppCompatActivity {
    private static PayActivity instance;
    private String TAG = "PAY";

    private WebView webPay = null;

    private String authno = ""; //카드사 승인번호
    private String trno = ""; // 거래번호
    private String orderno = ""; //주문번호
    private String validationPrice = "";
    private String validationTid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        instance = this;

        ((TextView) findViewById(R.id.tv_title)).setText("이용권 구매");

        webPay = (WebView) findViewById(R.id.webPay);

        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //자바 스크립트 안드로이드 호출
    @SuppressLint("JavascriptInterface")
    private void initView() {
        findViewById(R.id.btn_title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        webPay.getSettings().setJavaScriptEnabled(true);

        // android 5.0부터 앱에서 API수준21이상을 타겟킹하는 경우 아래추가
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webPay.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webPay, true);
        }

        //제공된 Java 객체를 WebView에 삽입
        //파라미터 :  JavascriptInterface에 대한 상세 로직을 구현, Web에서 앱을 호출할 인터페이스 네이밍을 지정
        webPay.addJavascriptInterface(new PayActivity.PayBridge(), "PayApp");
        //다양한 알림과 요청을 받을 WebViewClient를 설정 (=핸들러)
        webPay.setWebViewClient(new LguplusWebClient());
        webPay.setWebChromeClient(new LguplusWebChromeClient());

        webPay.loadUrl("http://www.sejongbike.kr/appserver/pay_lguplus/Loading.html");
    }

    private class PayBridge {
        @JavascriptInterface
        public void Result(final String result, final String authno, final String trno, final String ordno) {
            // 구매완료
            try {
                Log.d(TAG, "PayBridge, Result() : result : " + result + ", authno : " + authno + ", trno : " + trno + ", ordno : " + ordno);

                if (result != null && authno != null && trno != null && ordno != null) {
                    if (result.trim().equals("0000")) {
                        instance.authno = authno.trim();
                        instance.trno = trno.trim();
                        instance.orderno = ordno.trim();
                    } else {
                        Toast.makeText(PayActivity.this, "취소", Toast.LENGTH_LONG);
                    }
                } else {
                    Toast.makeText(PayActivity.this, "취소", Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                Log.d(TAG, instance.getClass().getSimpleName() + " -> " + "PayBridge : Result()");
                Toast.makeText(PayActivity.this, "취소", Toast.LENGTH_LONG);
            }
        }

        @JavascriptInterface
        public void Result(final String result, final String authno, final String trno, final String ordno, final String tid, final String amount) {
            // 구매완료
            try {
                Log.d(TAG, "PayBridge, Result() : result : " + result + ", authno : " + authno + ", trno : " + trno + ", ordno : " + ordno);

                if (result != null && authno != null && trno != null && ordno != null && tid != null && amount != null) {
                    validationTid = tid;
                    if (result.trim().equals("0000") && validationPrice.equals(amount)) {
                        instance.authno = authno.trim();
                        instance.trno = trno.trim();
                        instance.orderno = ordno.trim();
                    } else if (result.trim().equals("0000")) {
                        //Cancel Call
                        Toast.makeText(PayActivity.this, "취소", Toast.LENGTH_LONG);
                    } else {
                        Toast.makeText(PayActivity.this, "취소", Toast.LENGTH_LONG);
                    }
                } else {
                    Toast.makeText(PayActivity.this, "취소", Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                Log.d(TAG, instance.getClass().getSimpleName() + " -> " + "PayBridge : Result()");
                Toast.makeText(PayActivity.this, "취소", Toast.LENGTH_LONG);
            }
        }

        @JavascriptInterface
        public void Cancel() {
            Toast.makeText(PayActivity.this, "취소", Toast.LENGTH_LONG);
        }

        @JavascriptInterface
        public void GetUserData() {
            // 사용자 데이터 보내기
            // 클래스가 객체화 된 이후 값이 바뀌는 것을 방지하기 위해 final 변수로 정의
            final String payment_kind = "SC0010";//결제타입 - SC0060(폰결제), SC0010(카드결제)
            final String userName = "임현주"; // 성명
            final String userId = "abc"; // ID
            final String userEmail = "dear_jjwim@naver.com"; // email
            final String orderNumber = "0000"; // 주문번호
            final String goodsName = "정기권"; // 상품명
            final String price = "10"; // 가격

            //자바스크립트로 메세지 전달
            webPay.post(new Runnable() {
                @Override
                public void run() {
                    String url = "javascript:userData('" + payment_kind + "', '" + price + "', '" + userName + "', '"
                            + orderNumber + "', '" + goodsName + "', '" + userId + "', '" + userEmail + "')";
                    Log.d(TAG, "url : " + url);
                    webPay.loadUrl(url);
                }
            });
        }
    }

    // WebViewClient : 웹뷰에서 일어나는 요청, 상태, 에러 등 다양한 상황에서의 콜백을 조작
    private class LguplusWebClient extends WebViewClient {
        //현재 페이지의 url을 읽어 올 수 있는 메서드
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            if ((url.startsWith("http://") || url.startsWith("https://")) && url.endsWith(".apk")) {
                downloadFile(url);
                return super.shouldOverrideUrlLoading(view, url);
            } else if ((url.startsWith("http://") || url.startsWith("https://")) &&
                    (url.contains("market.android.com") || url.contains("m.ahnlab.com/kr/site/download"))) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(intent);
                    return true;
                } catch (ActivityNotFoundException e) {
                    return false;
                }
            } else if (url.startsWith("http://") || url.startsWith("https://")) {
                view.loadUrl(url);
                return true;
            } else if (url != null &&
                    (url.contains("vguard") || url.contains("droidxantivirus") || url.contains("smhyundaiansimclick://")
                            || url.contains("smshinhanansimclick://") || url.contains("smshinhancardusim://")
                            || url.contains("smartwall://") || url.contains("appfree://")  || url.contains("v3mobile")
                            || url.endsWith(".apk") || url.contains("market://") || url.contains("ansimclick")
                            || url.contains("market://details?id=com.shcard.smartpay") || url.contains("shinhan-sr-ansimclick://"))) {
                return callApp(url);
            } else if (url.startsWith("smartxpay-transfer://")) {
                boolean isatallFlag = isPackageInstalled(getApplicationContext(), "kr.co.uplus.ecredit");
                if (isatallFlag) {
                    boolean override = false;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());

                    try {
                        startActivity(intent);
                        override = true;
                    } catch (ActivityNotFoundException ex) {
                    }
                    return override;
                } else {
                    showAlert("확인버튼을 누르시면 구글플레이로 이동합니다.", "확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(("market://details?id=kr.co.uplus.ecredit")));
                            intent.addCategory(Intent.CATEGORY_BROWSABLE);
                            intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
                            startActivity(intent);
                        }
                    }, "취소", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    return true;
                }
            } else if (url.startsWith("ispmobile://")) {
                boolean isatallFlag = isPackageInstalled(getApplicationContext(), "kvp.jjy.MispAndroid320");
                if (isatallFlag) {
                    boolean override = false;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());

                    try {
                        startActivity(intent);
                        override = true;
                    } catch (ActivityNotFoundException ex) {
                    }
                    return override;
                } else {
                    showAlert("확인버튼을 누르시면 구글플레이로 이동합니다.", "확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            view.loadUrl("http://mobile.vpay.co.kr/jsp/MISP/andown.jsp");
                        }
                    }, "취소", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    return true;
                }
            } else if (url.startsWith("paypin://")) {
                boolean isatallFlag = isPackageInstalled(getApplicationContext(), "com.skp.android.paypin");
                if (isatallFlag) {
                    boolean override = false;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());

                    try {
                        startActivity(intent);
                        override = true;
                    } catch (ActivityNotFoundException ex) {
                    }
                    return override;
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(("market://details?id=com.skp.android.paypin&feature=search_result#?t=W251bGwsMSwxLDEsImNvbS5za3AuYW5kcm9pZC5wYXlwaW4iXQ..")));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
                    startActivity(intent);
                    return true;
                }
            } else if (url.startsWith("lguthepay://")) {
                boolean isatallFlag = isPackageInstalled(getApplicationContext(), "com.lguplus.paynow");
                if (isatallFlag) {
                    boolean override = false;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());

                    try {
                        startActivity(intent);
                        override = true;
                    } catch (ActivityNotFoundException ex) {
                    }
                    return override;
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(("market://details?id=com.lguplus.paynow")));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
                    startActivity(intent);
                    return true;
                }
            } else {
                return callApp(url);
            }
        }


        private void downloadFile(String mUrl) {
            new DownloadFileTask().execute(mUrl);
        }

        //AsyncTask : 하나의 클래스에서 UI 작업을 쉽게 할 수 있게 해줌
        private class DownloadFileTask extends AsyncTask<String, Void, String> {
            //중간 중간 진행 상태를 UI에 업데이트
            @Override
            protected String doInBackground(String... urls) {
                URL myFileUrl = null;
                try {
                    myFileUrl = new URL(urls[0]);
                } catch (MalformedURLException e) {
                    System.out.println("예외발생");
                }
                try {
                    HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();

                    // 다운 받는 파일의 경로는 sdcard/ 에 저장되며 sdcard에 접근하려면 uses-permission에
                    // android.permission.WRITE_EXTERNAL_STORAGE을 추가해야만 가능.
                    String mPath = "sdcard/v3mobile.apk";
                    FileOutputStream fos;
                    File f = new File(mPath);
                    if (f.createNewFile()) {
                        fos = new FileOutputStream(mPath);
                        int read;
                        while ((read = is.read()) != -1) {
                            fos.write(read);
                        }
                        fos.close();
                    }

                    return "v3mobile.apk";
                } catch (IOException e) {
                    System.out.println("예외발생");
                    return "";
                }
            }

            //doInBackground( ) 작업이 끝나면 onPostExcuted( ) 로 결과 파라미터를 return
            //return값을 통해 스레드 작업이 끝났을 때의 동작을 구현
            @Override
            protected void onPostExecute(String filename) {
                if (!"".equals(filename)) {
                    Toast.makeText(getApplicationContext(), "download complete", Toast.LENGTH_SHORT).show();

                    // 안드로이드 패키지 매니저를 사용한 어플리케이션 설치.
                    File apkFile = new File(Environment.getExternalStorageDirectory() + "/" + filename);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                    startActivity(intent);
                }
            }
        }


        // 외부 앱 호출
        public boolean callApp(String url) {
            Intent intent = null;
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                setIntentSecurity(intent);
            } catch (URISyntaxException ex) {
                return false;
            }
            try {
                boolean retval = true;
                if (url.startsWith("intent")) {
                    //암시적 인텐트를 받을 수 있는 앱이 기기에 없을 경우
                    //인텐트를 수신할 앱이 있는지 먼저 확인하려면, Intent 객체에서 resolveActivity()를 호출
                    if (getPackageManager().resolveActivity(intent, 0) == null) {
                        String packagename = intent.getPackage();
                        if (packagename != null) {
                            Uri uri = Uri.parse("market://search?q=pname:" + packagename);
                            intent = new Intent(Intent.ACTION_VIEW, uri);
                            setIntentSecurity(intent);
                            startActivity(intent);
                            retval = true;
                        }
                    } else { //처리할 수 있는 앱이 최소한 하나는 있다는 뜻
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setComponent(null);
                        try {
                            if (startActivityIfNeeded(intent, -1)) {
                                retval = true;
                            }
                        } catch (ActivityNotFoundException ex) {
                            retval = false;
                        }
                    }
                } else { //구 방식
                    Uri uri = Uri.parse(url);
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    setIntentSecurity(intent);
                    startActivity(intent);
                    retval = true;
                }
                return retval;
            } catch (ActivityNotFoundException e) {
                System.out.println("예외발생");
                return false;
            }
        }

        private void setIntentSecurity(Intent intent) {
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            //기본값이 null인 경우 시스템은 인텐트의 다른 필드 (작업, 데이터, 유형, 범주)를 기반으로 사용할 적절한 클래스를 결정
            intent.setComponent(null);
            //인텐트 선택기 설정
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                intent.setSelector(null);
            }
        }
    }

    // App 체크 메소드
    public static boolean isPackageInstalled(Context ctx, String pkgName) {
        try {
            //시스템에 설치된 애플리케이션 패키지에 대한 전체 정보를 검색
            ctx.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            System.out.println("예외발생");
            return false;
        }
        return true;
    }

    public void showAlert(String message, String positiveButton, DialogInterface.OnClickListener positiveListener, String negativeButton, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(message);
        alert.setPositiveButton(positiveButton, positiveListener);
        alert.setNegativeButton(negativeButton, negativeListener);
        alert.show();
    }

    //현제 페이지에서 일어나는 알람등을 알려 주기 위한 콜백 인터페이스
    //Javascript 대화 상자, 즐겨 찾기 아이콘, 제목 및 진행률을 처리
    private class LguplusWebChromeClient extends WebChromeClient {

        public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
            new AlertDialog.Builder(PayActivity.this).setTitle("").setMessage(message).setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                }
            }).setCancelable(false).create().show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(PayActivity.this).setTitle("").setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                }
            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    result.cancel();
                }
            }).create().show();
            return true;
        }
    }
}