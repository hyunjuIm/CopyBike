package com.example.copybike.util;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.example.copybike.R;

public class TelecomDialog extends Dialog {
    private View.OnClickListener sktBtnListener, ktBtnListener, lgtBtnListener, cjhBtnListener, kctBtnListener;

    public TelecomDialog(Activity activity, View.OnClickListener sktBtnListener, View.OnClickListener ktBtnListener, View.OnClickListener lgtBtnListener, View.OnClickListener cjhBtnListener, View.OnClickListener kctBtnListener) {
        super(activity);
        this.sktBtnListener = sktBtnListener;
        this.ktBtnListener = ktBtnListener;
        this.lgtBtnListener = lgtBtnListener;
        this.cjhBtnListener = cjhBtnListener;
        this.kctBtnListener = kctBtnListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_telecom);

        findViewById(R.id.ll_skt).setOnClickListener(sktBtnListener);
        findViewById(R.id.ll_kt).setOnClickListener(ktBtnListener);
        findViewById(R.id.ll_lgt).setOnClickListener(lgtBtnListener);
        findViewById(R.id.ll_cjh).setOnClickListener(cjhBtnListener);
        findViewById(R.id.ll_kct).setOnClickListener(kctBtnListener);

    }
}