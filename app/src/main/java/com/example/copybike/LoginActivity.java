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
    private static LoginActivity instance;
    private PreferencesHelper prefHelper;

    private String TAG = "LOGIN";

    private EditText inputId;
    private EditText inputPassword;

    public  AlertDialog.Builder alert;

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

        //회원 가입 버튼
        findViewById(R.id.btn_join).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), JoinGuideActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void requestLogin() {
        // 입력된 ID 검증
        if (!UserInfoValidation.checkId(inputId.getText().toString().trim())) {
            Toast.makeText(instance, "아이디와 비밀번호를 확인해주세요.", Toast.LENGTH_LONG);
        } else {
            if (!NetworkCheck.isNetworkAvailable(getBaseContext())) {
                alert.setTitle("알림");
                alert.setMessage("서버 연결에 실패했습니다. 잠시 후에 다시 시도해주세요.");
                alert.setPositiveButton("확인", null);
            } else {
                loginRequest();
            }
        }
    }

    private void loginRequest() {
        final String token = prefHelper.getAuthToken();

        String requestUrl = "http://1.245.175.54:8080/v1/user/login";

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
                            Toast.makeText(instance, "아이디나 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        return;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(instance, "아이디나 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, instance.getClass().getSimpleName() + " -> " + "error.toString() : " + error.toString());
                    }
                }){
            @Override
            public Map getHeaders() throws AuthFailureError {
                Map params = new HashMap();
                params.put("auth-token", token);
                return params;
            }
        };

        if (request != null) {
            request.setShouldCache(false);
            MainActivity.requestQueue.add(request);
        }
    }

}