package com.example.copybike;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.copybike.common.PreferencesHelper;
import com.example.copybike.util.NetworkCheck;
import com.example.copybike.util.TelecomDialog;
import com.example.copybike.util.UserInfoValidation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JoinActivity extends AppCompatActivity {

    private String TAG = "JoinActivity";

    private static JoinActivity instance;
    private PreferencesHelper prefHelper;
    private AlertDialog.Builder alert;

    private static EditText edt_postcode;
    private static EditText edt_address;
    public static TelecomDialog telecomDialog;
    private EditText edt_telecom;
    private EditText edt_rentalPass;

    private String resi; // 주민번호

    private boolean is_dupl_chk = false; //아이디 중복확인

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        instance = this;
        prefHelper = PreferencesHelper.getInstance(getBaseContext());

        alert = new AlertDialog.Builder(instance);

        initView();
    }

    @Override
    public void onBackPressed() {
        backActivity();
    }

    private void backActivity(){
        alert.setTitle("회원가입을 취소하시겠습니까?");
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(getBaseContext(), JoinTermsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            try {
                String postcode = data.getStringExtra("postcode");
                String address = data.getStringExtra("address");
                String building = data.getStringExtra("building");

                edt_postcode.setText(postcode);
                if (building.trim().equals("") || building.trim() == null) {
                    edt_address.setText(address);
                } else {
                    edt_address.setText(address + ", " + building);
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "우편번호 NULL 에러");
            }
        }
    }

    private void initView(){
        ((TextView)findViewById(R.id.tv_title)).setText("회원가입");

        // EditText
        edt_postcode = (EditText)findViewById(R.id.et_address_postal_code);
        edt_address = (EditText)findViewById(R.id.et_address1);
        edt_telecom = (EditText)findViewById(R.id.et_cell_phone_telecom);
        edt_rentalPass = (EditText)findViewById(R.id.et_rental_pass);

        // 본인인증 고정데이터
        ((EditText)findViewById(R.id.et_name)).setText(getIntent().getStringExtra("NAME"));
        String et_regi_front = getIntent().getStringExtra("BIRTH");
        String et_regi_gender = getIntent().getStringExtra("GENDER");
        et_regi_front = et_regi_front.substring(2, 8);
        ((EditText)findViewById(R.id.et_regi_front)).setText(et_regi_front);
        ((EditText)findViewById(R.id.et_regi_gender)).setText(et_regi_gender);

        // Button
        findViewById(R.id.btn_title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });
        findViewById(R.id.btn_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), PostActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        findViewById(R.id.btn_dupl_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserInfoValidation.checkJoinId(((EditText)findViewById(R.id.et_id)).getText().toString())) {
                    alert.setTitle("아이디를 확인해주세요");
                    alert.setPositiveButton("확인", null);
                    alert.show();
                } else {
                    idDuplCheck();
                }
            }
        });
        findViewById(R.id.btn_rental_pass_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_rentalPass.getText().toString().trim().length() < 4) {
                    edt_rentalPass.setText(edt_rentalPass.getText().toString().trim() + "1");
                } else {
                    edt_rentalPass.setText("1");
                }
            }
        });
        findViewById(R.id.btn_rental_pass_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_rentalPass.getText().toString().trim().length() < 4) {
                    edt_rentalPass.setText(edt_rentalPass.getText().toString().trim() + "2");
                } else {
                    edt_rentalPass.setText("2");
                }
            }
        });
        findViewById(R.id.btn_rental_pass_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_rentalPass.getText().toString().trim().length() < 4) {
                    edt_rentalPass.setText(edt_rentalPass.getText().toString().trim() + "3");
                } else {
                    edt_rentalPass.setText("3");
                }
            }
        });
        findViewById(R.id.btn_rental_pass_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_rentalPass.getText().toString().trim().length() < 4) {
                    edt_rentalPass.setText(edt_rentalPass.getText().toString().trim() + "4");
                } else {
                    edt_rentalPass.setText("4");
                }
            }
        });
        findViewById(R.id.et_cell_phone_telecom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                telecomDialog = new TelecomDialog(instance, sktBtnClickListener, ktBtnClickListener, lgtBtnClickListener,
                        cjhBtnClickListener, kctBtnClickListener);
                telecomDialog.show();
            }
        });
        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validationData()) {
                    joinMemberRequest();
                }
            }
        });
    }

    // 아이디 중복체크
    private void idDuplCheck() {
        if (UserInfoValidation.checkJoinId(((EditText)findViewById(R.id.et_id)).getText().toString().trim())) {
            if (!NetworkCheck.isNetworkAvailable(getBaseContext())) {
                Toast.makeText(instance, "네트워크 오류", Toast.LENGTH_LONG).show();
            } else {
                idCheckRequest();
            }
        } else {
            Toast.makeText(instance, "아이디 오류", Toast.LENGTH_LONG).show();
        }
    }

    private void idCheckRequest(){
        String requestUrl = "http://1.245.175.54:8080/v1/user/add/id";

        Map<String, String> params = new HashMap<String, String>();
        params.put("u_id", ((EditText)findViewById(R.id.et_id)).getText().toString().trim());

        Request<JSONObject> request = new JsonObjectRequest(Request.Method.POST, requestUrl,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("results").trim().equals("true")) {
                                Log.e(TAG, "!!!! 사용 가능 아이디 !!!!");
                                is_dupl_chk = true;
                                alert.setTitle("사용가능한 아이디입니다.");
                                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                                alert.show();
                            } else {
                                Log.e(TAG, "!!!! 사용 불가 아이디 !!!!");
                                is_dupl_chk = false;
                                alert.setTitle("해당 아이디로는 가입할 수 없습니다.");
                                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                                alert.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "아이디 중복 데이터 받아오기 오류");
                    }
                }
        );

        if (request != null) {
            request.setShouldCache(false);
            MainActivity.requestQueue.add(request);
        }
    }

    Button.OnClickListener sktBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            edt_telecom.setText("SKT");
            edt_telecom.setTag("01");
            telecomDialog.dismiss();
        }
    };
    Button.OnClickListener ktBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            edt_telecom.setText("KT");
            edt_telecom.setTag("02");
            telecomDialog.dismiss();
        }
    };
    Button.OnClickListener lgtBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            edt_telecom.setText("LGT");
            edt_telecom.setTag("03");
            telecomDialog.dismiss();
        }
    };
    Button.OnClickListener cjhBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            edt_telecom.setText("헬로모바일");
            edt_telecom.setTag("17");
            telecomDialog.dismiss();
        }
    };
    Button.OnClickListener kctBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            edt_telecom.setText("티플러스");
            edt_telecom.setTag("18");
            telecomDialog.dismiss();
        }
    };

    // 회원가입
    private void joinMemberRequest() {
        String requestUrl = "http://1.245.175.54:8080/v1/user/add";

        Map<String, String> params = new HashMap<String, String>();
        params.put("u_id", ((EditText)findViewById(R.id.et_id)).getText().toString().trim());
        params.put("u_name", ((EditText)findViewById(R.id.et_name)).getText().toString().trim());
        params.put("pw", ((EditText)findViewById(R.id.et_pass)).getText().toString().trim());
        params.put("cell_phone", ((EditText)findViewById(R.id.et_cell_phone)).getText().toString().trim());
        params.put("telecom", edt_telecom.getTag().toString());
        params.put("rent_pass", ((EditText)findViewById(R.id.et_rental_pass)).getText().toString().trim());
        params.put("zipcode", ((EditText)findViewById(R.id.et_address_postal_code)).getText().toString().trim());
        params.put("addr1", ((EditText)findViewById(R.id.et_address1)).getText().toString().trim());
        params.put("addr2", ((EditText)findViewById(R.id.et_address2)).getText().toString().trim());
        params.put("weight", ((EditText)findViewById(R.id.et_weight)).getText().toString().trim());
        params.put("email", ((EditText)findViewById(R.id.et_email)).getText().toString().trim());
        String birth = ((EditText)findViewById(R.id.et_regi_front)).getText().toString();
        String gender = ((EditText)findViewById(R.id.et_regi_gender)).getText().toString();
        String checkbit = ((EditText)findViewById(R.id.et_regi_end)).getText().toString();
        params.put("u_birth", birth);
        params.put("gender", gender);
        params.put("u_checkbit", checkbit);
        params.put("cert_safe_key", getIntent().getStringExtra("SAFE_KEY"));
        params.put("cert_jumin", getIntent().getStringExtra("JUMIN"));
        params.put("cert_ipin_di", getIntent().getStringExtra("IPIN_DI"));

        Request<JSONObject> request = new JsonObjectRequest(Request.Method.POST, requestUrl,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("results").trim().equals("true")) {
                                String auth_token = response.getString("auth_token");
                                prefHelper.setAuthToken(auth_token);

                                Intent intent = new Intent(getBaseContext(), JoinCompleteActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                alert.setTitle("이미 존재하는 회원입니다.");
                                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                                alert.show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        alert.setTitle("이미 존재하는 회원입니다.");
                        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                        alert.show();

                        Log.e(TAG, "회원가입 데이터 오류");
                        Log.e(TAG, instance.getClass().getSimpleName() + " -> " + "error.toString() : " + error.toString());
                    }
                }
        );

        if (request != null) {
            request.setShouldCache(false);
            MainActivity.requestQueue.add(request);
        }
    }

    // 입력데이터 검증
    private boolean validationData() {
        // 아이디 검증 : 6~20자리
        if (!is_dupl_chk) {
            Toast.makeText(instance, "사용할 수 없는 아이디입니다.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!UserInfoValidation.checkJoinId(((EditText)findViewById(R.id.et_id)).getText().toString().trim())) {
            Toast.makeText(instance, "아이디를 확인해주세요.", Toast.LENGTH_LONG).show();
            return false;
        }

        // 이름 검증
        if (!(((EditText)findViewById(R.id.et_name)).getText().toString().trim().length() >=2)) {
            Toast.makeText(instance, "이름을 확인해주세요.", Toast.LENGTH_LONG).show();
            return false;
        }

        // 주민번호 검증
        resi = ((EditText)findViewById(R.id.et_regi_end)).getText().toString().trim();

        if (resi.trim().length() != 3) {
            Toast.makeText(instance, "주민번호 뒤 3자리를 확인해주세요.", Toast.LENGTH_LONG).show();
            return false;
        }

        // 이메일 검증
        if (!(android.util.Patterns.EMAIL_ADDRESS.matcher(((EditText)findViewById(R.id.et_email)).getText().toString().trim()).matches())) {
            Toast.makeText(instance, "이메일을 확인해주세요.", Toast.LENGTH_LONG).show();
            return false;
        }

        // 휴대폰번호 검증
        if (!(((EditText)findViewById(R.id.et_cell_phone_telecom)).getText().toString().trim().length() != 0 &&
                ((EditText)findViewById(R.id.et_cell_phone)).getText().toString().trim().length() >= 10)) {
            Toast.makeText(instance, "휴대폰번호를 확인해주세요.", Toast.LENGTH_LONG).show();
            return false;
        }

        // 주소 검증
        if (!(((EditText)findViewById(R.id.et_address_postal_code)).getText().toString().trim().length() != 0 &&
                ((EditText)findViewById(R.id.et_address1)).getText().toString().trim().length() != 0 &&
                ((EditText)findViewById(R.id.et_address2)).getText().toString().trim().length() != 0)) {
            Toast.makeText(instance, "주소를 확인해주세요.", Toast.LENGTH_LONG).show();
            return false;
        }

        // 비밀번호 검증 : 비밀번호는 영문+숫자를 포함해야하고 8~20자리, 비밀번호와 비밀번호확인은 동일해야함.
        if (!UserInfoValidation.checkPw(((EditText)findViewById(R.id.et_pass)).getText().toString().trim(), ((EditText)findViewById(R.id.et_pass_check)).getText().toString().trim())) {
            Toast.makeText(instance, "비밀번호를 확인해주세요. (6~12자 영문/숫자 조합)", Toast.LENGTH_LONG).show();
            return false;
        }

        // 대여비밀번호 검증
        if (((EditText)findViewById(R.id.et_rental_pass)).getText().toString().trim().length() != 4) {
            Toast.makeText(instance, "대여비밀번호를 확인해주세요.", Toast.LENGTH_LONG).show();
            return false;
        }

        // 체중 검증
        if (!(((EditText)findViewById(R.id.et_weight)).getText().toString().trim().length() != 0)) {
            ((EditText)findViewById(R.id.et_weight)).setText("65");
        }
        return true;
    }
}