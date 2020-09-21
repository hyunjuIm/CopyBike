package com.example.copybike;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;

public class JoinTermsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_terms);

        initView();
    }


    @Override
    public void onBackPressed() {
        backActivity();
    }

    private void backActivity() {
        Intent intent = new Intent(getBaseContext(), JoinGuideActivity.class);
        startActivity(intent);
        finish();
    }

    private void AuthorizationActivity() {
        Intent intent = new Intent(getBaseContext(), JoinAuthorizationActivity.class);
        startActivity(intent);
        finish();
    }

    private void initView() {
        ((TextView)findViewById(R.id.tv_title)).setText("약관동의");

        findViewById(R.id.btn_title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });
        findViewById(R.id.btn_ll_check_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckBox)findViewById(R.id.check_all)).setChecked(true);

                checkTermsAgree("ALL");
                AuthorizationActivity();
            }
        });
        findViewById(R.id.check_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTermsAgree("ALL");
                AuthorizationActivity();
            }
        });
        findViewById(R.id.btn_ll_terms_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)findViewById(R.id.check_01)).isChecked()) {
                    ((CheckBox)findViewById(R.id.check_01)).setChecked(false);
                } else {
                    ((CheckBox)findViewById(R.id.check_01)).setChecked(true);
                }
                checkTermsAgree("CHECK");
            }
        });
        findViewById(R.id.check_01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTermsAgree("CHECK");
            }
        });
        findViewById(R.id.btn_ll_terms_privacy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)findViewById(R.id.check_02)).isChecked()) {
                    ((CheckBox)findViewById(R.id.check_02)).setChecked(false);
                } else {
                    ((CheckBox)findViewById(R.id.check_02)).setChecked(true);
                }
                checkTermsAgree("CHECK");
            }
        });
        findViewById(R.id.check_02).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTermsAgree("CHECK");
            }
        });

        findViewById(R.id.btn_ll_terms_privacy_number).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)findViewById(R.id.check_number)).isChecked()) {
                    ((CheckBox)findViewById(R.id.check_number)).setChecked(false);
                } else {
                    ((CheckBox)findViewById(R.id.check_number)).setChecked(true);
                }
                checkTermsAgree("CHECK");
            }
        });
        findViewById(R.id.check_number).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTermsAgree("CHECK");
            }
        });

        findViewById(R.id.btn_ll_third_party).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)findViewById(R.id.check_03)).isChecked()) {
                    ((CheckBox)findViewById(R.id.check_03)).setChecked(false);
                } else {
                    ((CheckBox)findViewById(R.id.check_03)).setChecked(true);
                }
                checkTermsAgree("CHECK");
            }
        });
        findViewById(R.id.check_03).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTermsAgree("CHECK");
            }
        });
        findViewById(R.id.btn_ll_terms_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)findViewById(R.id.check_04)).isChecked()) {
                    ((CheckBox)findViewById(R.id.check_04)).setChecked(false);
                } else {
                    ((CheckBox)findViewById(R.id.check_04)).setChecked(true);
                }
                checkTermsAgree("CHECK");
            }
        });
        findViewById(R.id.check_04).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTermsAgree("CHECK");
            }
        });
        findViewById(R.id.btn_terms_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), TermsDetailActivity.class);
                intent.putExtra("ACTIVITY", getClass().getName().trim());
                intent.putExtra("TITLE", "위치기반 서비스 이용약관");
                intent.putExtra("CONTENTS", R.string.location_service_terms_agree);
                startActivity(intent);
                finish();
            }
        });
        findViewById(R.id.btn_terms_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), TermsDetailActivity.class);
                intent.putExtra("ACTIVITY", getClass().getName().trim());
                intent.putExtra("TITLE", "서비스 이용 약관");
                intent.putExtra("CONTENTS", R.string.service_terms_agree);
                startActivity(intent);
                finish();
            }
        });
        findViewById(R.id.btn_terms_privacy_number).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), TermsDetailActivity.class);
                intent.putExtra("ACTIVITY", getClass().getName().trim());
                intent.putExtra("TITLE", "개인정보 고유식별번호 동의");
                intent.putExtra("CONTENTS", R.string.privacy_information_number_terms_agree);
                startActivity(intent);
                finish();
            }
        });
        findViewById(R.id.btn_terms_third_party).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), TermsDetailActivity.class);
                intent.putExtra("ACTIVITY", getClass().getName().trim());
                intent.putExtra("TITLE", "개인정보 제3자 정보제공");
                intent.putExtra("CONTENTS", R.string.third_party_information_terms_agree);
                startActivity(intent);
                finish();
            }
        });
        findViewById(R.id.btn_terms_privacy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), TermsDetailActivity.class);
                intent.putExtra("ACTIVITY", getClass().getName().trim());
                intent.putExtra("TITLE", "개인정보 수집 이용 동의");
                intent.putExtra("CONTENTS", R.string.privacy_information_terms_agree);
                startActivity(intent);
                finish();
            }
        });

        // 스크롤뷰 터치 제어
        final ScrollView scrollRoot = (ScrollView) findViewById(R.id.scrollView_root);
        findViewById(R.id.check_all).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!((CheckBox) findViewById(R.id.check_all)).isChecked()) {
                        scrollRoot.fullScroll(View.FOCUS_DOWN);
                    }
                }
                return false;
            }
        });
    }

    // 체크박스 설정
    private void checkTermsAgree(String type) {
        CheckBox termsService = (CheckBox) findViewById(R.id.check_01);
        CheckBox termsPersonal = (CheckBox) findViewById(R.id.check_02);
        CheckBox termsNumber   = (CheckBox) findViewById(R.id.check_number);
        CheckBox termsAnother  = (CheckBox) findViewById(R.id.check_03);
        CheckBox termsLocation = (CheckBox) findViewById(R.id.check_04);
        CheckBox termsCheckAll = (CheckBox) findViewById(R.id.check_all);

        switch (type) {
            case "CHECK":
                if (termsService.isChecked() && termsPersonal.isChecked() && termsNumber.isChecked() &&
                        termsAnother.isChecked() && termsLocation.isChecked()) {
                    termsCheckAll.setChecked(true);
                    AuthorizationActivity();
                } else {
                    termsCheckAll.setChecked(false);
                }
                break;
            case "ALL":
                if (termsCheckAll.isChecked()) {
                    termsService.setChecked(true);
                    termsPersonal.setChecked(true);
                    termsNumber.setChecked(true);
                    termsAnother.setChecked(true);
                    termsLocation.setChecked(true);
                    termsCheckAll.setChecked(true);
                } else {
                    termsService.setChecked(false);
                    termsPersonal.setChecked(false);
                    termsNumber.setChecked(false);
                    termsAnother.setChecked(false);
                    termsLocation.setChecked(false);
                    termsCheckAll.setChecked(false);
                }
                break;
            default:
                break;
        }
    }
}