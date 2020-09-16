package com.example.copybike.util;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.example.copybike.R;

public class StationTypeDialog extends Dialog {
    private View.OnClickListener stationAllBtnListener, stationBikeBtnListener, stationSbikeBtnListener;
    private int selectedValue = 0;

    public StationTypeDialog(Activity activity, int selectedValue,
                             View.OnClickListener stationAllBtnListener,
                             View.OnClickListener stationBikeBtnListener,
                             View.OnClickListener stationSbikeBtnListener) {
        super(activity);
        this.selectedValue = selectedValue;
        this.stationAllBtnListener = stationAllBtnListener;
        this.stationBikeBtnListener = stationBikeBtnListener;
        this.stationSbikeBtnListener = stationSbikeBtnListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_station_type2);

        findViewById(R.id.ll_station_all).setOnClickListener(stationAllBtnListener);
        findViewById(R.id.ll_station_bike).setOnClickListener(stationBikeBtnListener);
        findViewById(R.id.ll_station_sbike).setOnClickListener(stationSbikeBtnListener);

        /*
        // 현재값 Bold 처리
        if (selectedValue == 1) {
            setBold((TextView) findViewById(R.id.tv_bike));
        } else if (selectedValue == 2) {
            setBold((TextView) findViewById(R.id.tv_sbike));
        } else {
            setBold((TextView) findViewById(R.id.tv_all));
        }
         */
    }
}
