package com.example.copybike;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.copybike.common.PreferencesHelper;
import com.example.copybike.util.NetworkCheck;
import com.example.copybike.util.UserInfoValidation;
import com.example.copybike.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private LoginActivity instance;
    private PreferencesHelper prefHelper;

    private String TAG = "LOGIN";

    private EditText inputId;
    private EditText inputPassword;

    public  AlertDialog.Builder alert;

    private String requestType = "REQ_TYPE_NONE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        instance = this;

        prefHelper = PreferencesHelper.getInstance(getBaseContext());

        ((TextView)findViewById(R.id.tv_title)).setText("로그인");
        alert = new AlertDialog.Builder(instance);

        inputId = (EditText) findViewById(R.id.et_id);
        inputPassword = (EditText) findViewById(R.id.et_pw);

        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void initView(){
        findViewById(R.id.btn_title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //로그인 버튼
        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLogin();
            }
        });
    }

    private void requestLogin() {
        // 입력된 ID 검증
        if (!UserInfoValidation.checkId(inputId.getText().toString().trim())) {
            alert.setTitle("알림");
            alert.setMessage("아이디와 비밀번호를 확인해주세요.");
            alert.setPositiveButton("확인", null);
        } else {
            if (!NetworkCheck.isNetworkAvailable(getBaseContext())) {
                alert.setTitle("알림");
                alert.setMessage("서버 연결에 실패했습니다. 잠시 후에 다시 시도해주세요.");
                alert.setPositiveButton("확인", null);
            } else {
                requestType = "REQ_TYPE_LOGIN";
                volleyRequest();
            }
        }
    }

    public void volleyRequest() {
        final String token = prefHelper.getAuthToken();

        String requestUrl = "http:app.sejongbike.kr/v1/user/login";

        Map<String, String> params = new HashMap<String, String>();
        params.put("u_id", inputId.getText().toString().trim());
        params.put("password", inputPassword.getText().toString().trim());

        Request<JSONObject> request = new JsonObjectRequest(Request.Method.POST, requestUrl,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject userInfo = response.getJSONObject("results");
                            prefHelper.setLogin(userInfo.getString("u_name"), userInfo.getString("u_id"),
                                    userInfo.getString("cell_phone"), userInfo.getString("telecom"),
                                    userInfo.getString("rent_pass"), userInfo.getString("u_birth"),
                                    userInfo.getString("email"),
                                    response.optString("auth_token"), userInfo.optString("mem_group"),
                                    response.optString("voucher_start_date"), response.optString("voucher_end_date"));

                            // 메인화면 새로고침
                            Intent intent = new Intent(getBaseContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            Toast.makeText(instance, "아이디나 비밀번호가 잘못되었습니다.", Toast.LENGTH_LONG);
                            e.printStackTrace();
                        }
                        return;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            Log.e(TAG, instance.getClass().getSimpleName() + " -> " + "error.toString() : " + error.toString());

                            if (error.toString().trim().equals("com.android.volley.TimeoutError")) {
                                Toast.makeText(instance, "POPUP_ERROR", Toast.LENGTH_LONG).show();
                            } else if (error.toString().trim().equals("java.lang.RuntimeException: Bad URL null")) {
                                Toast.makeText(instance, "POPUP_ERROR_BAD_URL", Toast.LENGTH_LONG).show();
                            } else {
                                NetworkResponse response = error.networkResponse;

                                String message = new String(response.data, "UTF-8");
                                message = trimMessage(message, "message");

                                if (message == null) {
                                    Toast.makeText(instance, "POPUP_NETWORK_SERVER", Toast.LENGTH_LONG).show();
                                } else {
                                    if (message.trim().equals("회원정보가 존재하지 않습니다.")) {
                                        Toast.makeText(instance, "POPUP_ERROR_LOGIN", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(instance, message, Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, instance.getClass().getSimpleName() + " -> " + "volleyErrorHandler() : Exception");
                            Toast.makeText(instance, "POPUP_ERROR", Toast.LENGTH_LONG).show();
                        }
                    }
                }){
            @Override
            public Map getHeaders() throws AuthFailureError {
                Map params = new HashMap();
                params.put("auth-token", token);
                return params;
            }
        };

        requestType = "REQ_TYPE_NONE";

        if (request != null) {
            request.setShouldCache(false);
            request.setRetryPolicy(new DefaultRetryPolicy(15000, 5, 1f));
            MainActivity.requestQueue.add(request);
        }
    }

    // volleyErrorHandler에서 에러문자를 뽑아내기 위해서 사용되는 함수
    public String trimMessage(String json, String key) {
        String trimmedString = null;

        try {
            JSONObject obj = new JSONObject(json);
            //name존재 하는 경우 매핑 된 값을 반환하고 , 필요한 경우 강제하거나, 그러한 매핑이없는 경우 throw합니다.
            trimmedString = obj.getString(key);
        } catch (JSONException e) {
            Log.e(TAG, "trimMessage()");
            return null;
        }

        return trimmedString;
    }
}