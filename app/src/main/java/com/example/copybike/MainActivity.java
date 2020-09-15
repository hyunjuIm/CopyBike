package com.example.copybike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.copybike.common.NaverMapHelper;
import com.example.copybike.data.Station;
import com.google.gson.Gson;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.Pickable;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.pedro.library.AutoPermissions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    Button btn_current_location;
    TextView tv_last_notice;

    final static String TAG = "HYUNJU";

    private MapFragment mMapFragment;

    static RequestQueue requestQueue;

    // 최근 공지사항 번호
    private String lastNoticeSeq = null;

    private ArrayList<Station> stationList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AutoPermissions.Companion.loadAllPermissions(this, 101);

        btn_current_location = findViewById(R.id.btn_current_location);
        tv_last_notice = findViewById(R.id.tv_last_notice);

        if(requestQueue ==null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        initMap();
        makeRequest();

        btn_current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationService();
            }
        });
    }

    //맵 초기화
    private void initMap() {
        FragmentManager fm = getSupportFragmentManager();
        mMapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment);
        if (mMapFragment == null) {
            mMapFragment = MapFragment.newInstance();
            //beginTransaction() -> Fragment에서의 작업
            fm.beginTransaction().add(R.id.map_fragment, mMapFragment).commit();
        }
        //맵 사용 준비 완료
        mMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        // 카메라 초기 위치 설정
        LatLng initialPosition = new LatLng(36.502812, 127.256329);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
        naverMap.moveCamera(cameraUpdate);
    }

    @SuppressLint("MissingPermission")
    public void startLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            //이전에 확인했던 위치정보 가져오기
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null){
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
            }

            GPSListener gpsListener = new GPSListener();
            long minTime = 10000;
            float minDistance = 0;

            //위치 요청
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //위치 관리자에서 전달하는 위치정보를 받기 위해 정의된 인터페이스
    class GPSListener implements LocationListener{
        //위치가 확인 되었을 때 자동으로 호출
        public void onLocationChanged(Location location){
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            Log.e(TAG, "내 위치 : "+ latitude + ", " + longitude);
        }
    }

    public void makeRequest(){
        // URL 설정
        Request<JSONObject> request = null;
        String requestUrl = "http://1.245.175.54:8080/v1/station/list/extra";

        request = new JsonObjectRequest(Request.Method.GET, requestUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // 최근 공지사항 가져오기
                            JSONObject notice = response.getJSONObject("notice");
                            if (notice != null) {
                                tv_last_notice.setText(notice.optString("title"));
                                lastNoticeSeq = notice.optString("notice_seq");
                            } else {
                                tv_last_notice.setText("공지사항이 없습니다.");
                                lastNoticeSeq = null;
                            }

                            //대여소정보 가져오기
                            stationList = NaverMapHelper.getStationInfo(response.getJSONArray("bike_station"));
                            Log.e(TAG, "대여소 주소 : " + stationList.get(0).getMainAddress());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "대여소 데이터 가져오기 실패");
            }
        });
        request.setShouldCache(false);
        requestQueue.add(request);
    }
}