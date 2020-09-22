package com.example.copybike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.copybike.common.NaverMapHelper;
import com.example.copybike.common.PreferencesHelper;
import com.example.copybike.data.SbikeStation;
import com.example.copybike.data.Station;
import com.example.copybike.util.StationTypeDialog;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.Pickable;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.pedro.library.AutoPermissions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static MainActivity instance;
    final static String TAG = "HYUNJU";

    private PreferencesHelper prefHelper;

    //위젯
    Button btn_current_location; //내 위치 버튼
    TextView tv_last_notice; //공지 텍스트뷰

    //Volley
    public static RequestQueue requestQueue;
    private String lastNoticeSeq = null; //최근 공지사항 번호
    private ArrayList<Station> stationList = null; //공공대여소 리스트
    private ArrayList<SbikeStation> sbikeStationList = null; //공유 대여소 리스트

    //지도
    private NaverMap mNaverMap;
    private MapFragment mMapFragment;
    private InfoWindow mCurrentWindow; //지도 위에 올리는 정보창

    //마커 출력 분류
    public StationTypeDialog stationTypeDialog;
    private static final int STATION_ALL = 0;
    private static final int STATION_ONLY_BIKE = 1;
    private static final int STATION_ONLY_SBIKE = 2;
    private int markerStationType = STATION_ALL;

    //내 위치
    private LocationManager locationManager;
    private boolean isOnlyMyLocation = false;
    private boolean isLocationUsing = true;
    private boolean trackOnce;

    //BLE 통신
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler = new Handler();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private BluetoothLeService mBluetoothLeService;

    private static final long SCAN_PERIOD = 5000;

    private final static UUID[] uuid = new UUID[1];
    public final static String MAC_ADDRESS = "D4:7C:44:40:09:5F";
    private boolean mRental = false;

    private Intent gattServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        prefHelper = PreferencesHelper.getInstance(getBaseContext());

        //위험 권한 자동 부여 라이브러리
        AutoPermissions.Companion.loadAllPermissions(this, 101);

        btn_current_location = findViewById(R.id.btn_current_location);
        tv_last_notice = findViewById(R.id.tv_last_notice);

        if(requestQueue ==null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        //전달하는 파라미터에 따라서 원하는 클래스형으로 형변환을 해야 한다는 것을 의미
        //파라미터로 전달되는 name값에 따라서 시스템 레벨의 서비스를 제어할 수 있는 핸들을 리턴
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        uuid[0] = UUID.fromString("F000C0E0-0451-4000-B000-000000000000");

        gattServiceIntent = new Intent(this, BluetoothLeService.class);

        initMap();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefHelper.isLogin()) {
            findViewById(R.id.btn_title_right).setBackgroundResource(R.drawable.btn_mypage_selector);
        } else {
            findViewById(R.id.btn_title_right).setBackgroundResource(R.drawable.btn_login_selector);
        }

        scanLeDevice(true);
        //인텐트로 서비스 특성 불러오기, Service 실행
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //브로드캐스트 등록
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(MAC_ADDRESS); //연결
            Log.e(TAG, "Connect request result = " + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private void initView(){
        //스테이션 분류 팝업
        findViewById(R.id.btn_station_type).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stationTypeDialog = new StationTypeDialog(instance, markerStationType,
                        stationAllBtnListener, stationBikeBtnListener, stationSbikeBtnListener);
                stationTypeDialog.show();
            }
        });

        //내 위치 정보
        btn_current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 내 위치 기능 사용중인지 체크
                if (isOnlyMyLocation) {
                    isLocationUsing = false;
                    myLocationStop();
                } else {
                    myLocationStart();
                }
            }
        });

        //새로고침 버튼
        findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllMarker();
                getMarkerItems();
            }
        });

        //대여 버튼
        findViewById(R.id.ll_btn_rental).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "대여 스캔 시작");
                MessageThread thread = new MessageThread();
                thread.start();
            }
        });

        //이용권 구매
        findViewById(R.id.ll_btn_payment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), PayActivity.class);
                intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //로그인
        findViewById(R.id.btn_title_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prefHelper.isLogin()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(instance);
                    builder.setTitle("이미 로그인이 된 상태입니다.\n로그아웃 하시겠습니까?");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            PreferencesHelper prefHelper = PreferencesHelper.getInstance(getBaseContext());
                            prefHelper.setInitUserInfo();

                            dialog.cancel();
                            Toast.makeText(instance, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

                            // 메인화면 새로고침
                            Intent intent = new Intent(getBaseContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("취소",null);
                    builder.show();
                } else {
                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                    intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            }
        });
    }

    //전체
    Button.OnClickListener stationAllBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            markerStationType = STATION_ALL;
            setStationTypeMarkers();
        }
    };
    //어울링 선택
    Button.OnClickListener stationBikeBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            markerStationType = STATION_ONLY_BIKE;
            setStationTypeMarkers();
        }
    };
    //뉴어울링 선택
    Button.OnClickListener stationSbikeBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            markerStationType = STATION_ONLY_SBIKE;
            setStationTypeMarkers();
        }
    };

    private void setStationTypeMarkers() {
        if (stationTypeDialog != null) {
            stationTypeDialog.dismiss();
        }

        clearAllMarker();
        getMarkerItems();
    }

    private void clearAllMarker() {
        if (stationList != null) {
            for (Station station : stationList) {
                if (station != null) {
                    if (station.getMarker() != null) {
                        station.getMarker().setMap(null);
                    }
                }
            }
        }

        if (sbikeStationList != null) {
            for (SbikeStation station : sbikeStationList) {
                if (station != null) {
                    if (station.getMarker() != null) {
                        station.getMarker().setMap(null);
                    }
                }
            }
        }

        if (mCurrentWindow != null) {
            mCurrentWindow.close();
        }

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
        mNaverMap = naverMap;

        // 카메라 초기 위치 설정
        LatLng initialPosition = new LatLng(36.502812, 127.256329);

        naverMap.addOnLocationChangeListener(mLocationChangeListener);//사용자 위치 변경에 대한 이벤트 리스너
        naverMap.setOnMapClickListener(mMapClickListener);
        naverMap.setLocationSource(new FusedLocationSource(this, 1)); //위치 소스 지정
        naverMap.getUiSettings().setZoomControlEnabled(false); //줌 컨트롤 활성화 여부
        naverMap.getUiSettings().setCompassEnabled(false);// 나침반 활성화 여부
        naverMap.getUiSettings().setScaleBarEnabled(false); // //축척바 활성화 여부
        naverMap.getUiSettings().setLogoMargin(10, 0, 0, 10); //네이버 로고 마진
        naverMap.getUiSettings().setRotateGesturesEnabled(false); //회전 제스처 활성화 여부
        naverMap.setExtent(new LatLngBounds(
                new LatLng(30.664915, 122.628502), new LatLng(39.788312, 132.893671))); //제한 영역
        naverMap.setLiteModeEnabled(true); //라이트 모드 활성화
        naverMap.setLocationTrackingMode(LocationTrackingMode.None); //위치 추적 모드
        naverMap.setCameraPosition(new CameraPosition(initialPosition, 13));

        //내 위치 찾기
        requestLocationUpdates();
        //마커 초기화 셋팅
        clearAllMarker();
        //대여소 데이터 받아오기
        stationRequest();
        //유저 데이터 받아오기
        userRequest();
    }

    //내 위치 찾기
    private void myLocationStart(){
        // LocationManager 생성
        if (null == locationManager) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        // 내위치 아이콘 변경 (Selected)
        btn_current_location.setBackgroundResource(R.drawable.gps_icon_selected);
        isOnlyMyLocation = true;
        isLocationUsing = true;

        // 위치요청
        requestLocationUpdates();
    }

    //내 위치 탐색
    private void requestLocationUpdates() {
        if (mNaverMap != null) {
            trackOnce = true;
            //위치 추적 활성화, 현위치 오버레이가 사용자의 위치를 따라 움직이지만 지도는 움직이지 않음
            mNaverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);
        }
    }

    // 내 위치찾기 종료
    private void myLocationStop() {
        btn_current_location.setBackgroundResource(R.drawable.btn_myposition_selector);
        isOnlyMyLocation = false;

        // 내위치 탐색 기능 종료
        requestLocationRemoveUpdates();
    }

    // 내위치 탐색 기능 종료
    private void requestLocationRemoveUpdates() {
        if (mNaverMap != null) {
            if (!isLocationUsing) {
                mNaverMap.setCameraPosition(new CameraPosition(
                        new LatLng(36.502812, 127.256329), 13));
            }
            mNaverMap.setLocationTrackingMode(LocationTrackingMode.None);
        }
    }

    //사용자 위치 변경에 대한 이벤트 리스너
    private final NaverMap.OnLocationChangeListener mLocationChangeListener = new NaverMap.OnLocationChangeListener() {
        @Override
        public void onLocationChange(@NonNull Location location) {
            if (trackOnce) {
                trackOnce = false;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mNaverMap.setCameraPosition(new CameraPosition(latLng, 17));
            }
        }
    };

    private final NaverMap.OnMapClickListener mMapClickListener = new NaverMap.OnMapClickListener() {
        @Override
        public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
            int radius = 1;
            //특정 화면 좌표 주변 radius 픽셀 내에 나타난 모든 오버레이 및 심벌을 가져옴
            List<Pickable> pickableList = mNaverMap.pickAll(pointF, radius);
            ArrayList<Marker> markers = new ArrayList<>();

            //선택된 마커 정보
            for (Pickable pickable : pickableList) {
                if (pickable instanceof Marker) {
                    markers.add(((Marker) pickable));
                }
            }

            if (markers.size() <= 0) {
                if (mCurrentWindow != null) {
                    mCurrentWindow.close();
                }
            }
        }
    };

    public void userRequest(){
        final String token = prefHelper.getAuthToken();

        String requestUrl = "http://app.sejongbike.kr/v1/user/edit";
        Map<String, String> params = new HashMap<String, String>();

        //지정된 URL에서 JSONObject의 응답 본문을 가져오기 위한 요청, 요청 본문의 일부로 선택적 JSONObject를 전달할 수 있음.
        Request<JSONObject> request = new JsonObjectRequest(Request.Method.GET, requestUrl, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject userInfo = response.getJSONObject("results");
                            JSONObject voucherInfo = response.getJSONObject("voucher");
                            JSONObject sbikeInfo = response.getJSONObject("sbike");

                            prefHelper.setUserInfo(
                                    userInfo.getString("u_name"), userInfo.getString("u_id"), userInfo.getString("cell_phone"),
                                    userInfo.getString("telecom"), userInfo.getString("rent_pass"), userInfo.getString("u_birth"),
                                    userInfo.getString("email"), userInfo.optString("mem_group"),
                                    voucherInfo.getString("voucher_start_date"), voucherInfo.getString("voucher_end_date"),
                                    sbikeInfo.getString("sbike_id"), sbikeInfo.getString("sbike_ble_address"));

                            if (prefHelper.isLogin()) {
                                findViewById(R.id.btn_title_right).setBackgroundResource(R.drawable.btn_mypage_selector);
                            } else {
                                findViewById(R.id.btn_title_right).setBackgroundResource(R.drawable.btn_login_selector);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "유저 데이터 가져오기 실패");
            }
        }){
            @Override
            public Map getHeaders() throws AuthFailureError {
                Map params = new HashMap();
                params.put("auth-token", token);
                return params;
            }
        };
        request.setShouldCache(false); //이전 응답 결과 사용하지 않겠다 ->cache 사용 안함
        requestQueue.add(request); //요청 큐 넣어주기
    }

    //volley로 대여소 정보 받아오기
    public void stationRequest(){
        String requestUrl = "http://1.245.175.54:8080/v1/station/list/extra";

        //지정된 URL에서 JSONObject의 응답 본문을 가져오기 위한 요청, 요청 본문의 일부로 선택적 JSONObject를 전달할 수 있음.
        Request<JSONObject> request = new JsonObjectRequest(Request.Method.GET, requestUrl, null,
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

                            //공유 대여소정보 가져오기
                            sbikeStationList = NaverMapHelper.getSbikeStationInfo(response.getJSONArray("sbike_station"));

                            //마커출력
                            getMarkerItems();
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
        request.setShouldCache(false); //이전 응답 결과 사용하지 않겠다 ->cache 사용 안함
        requestQueue.add(request); //요청 큐 넣어주기
    }

    //마커 셋팅
    private void getMarkerItems() {
        // 마커 내부 Context 정의
        // LayoutInflater : 레이아웃 XML 파일을 해당 View 객체 로 인스턴스화
        View rootMarker = LayoutInflater.from(this).inflate(R.layout.item_marker, null);
        LinearLayout iv = (LinearLayout) rootMarker.findViewById(R.id.iv_marker);
        TextView textMarker = (TextView) rootMarker.findViewById(R.id.tv_marker);

        if (stationList != null && sbikeStationList != null) {
            if (stationList.size() > 0 || sbikeStationList.size() > 0) {
                // 공공 대여소
                if (markerStationType == STATION_ALL || markerStationType == STATION_ONLY_BIKE) {
                    for (int i=0; i<stationList.size(); i++) {
                        addMarker(instance, stationList.get(i), rootMarker, iv, textMarker);
                    }
                }

                // 공유 대여소
                if (markerStationType == STATION_ALL || markerStationType == STATION_ONLY_SBIKE) {
                    for (int i=0; i<sbikeStationList.size(); i++) {
                        addSbikeMarker(instance, sbikeStationList.get(i), rootMarker, iv, textMarker);
                    }
                }
            }
        }
    }

    //공공 대여소 마커 추가
    public Marker addMarker(Context context, Station markerItem, View rootMarker, LinearLayout iv, TextView textMarker) {

        // 마커 위치
        double x = markerItem.getX();
        double y = markerItem.getY();

        // 대여소 번호
        textMarker.setText(String.valueOf(markerItem.getLockerOnCount()));

        // 마커 이미지
        double per = markerItem.getLockerOffPercent();
        String kioskState = markerItem.getKioskState();

        //개발 서버는 키오스크 에러로 출력돼서 전부 장애 아이콘으로 뜸
        if (per <= 0.0)         iv.setBackgroundResource(R.drawable.station_00);
        else if (per < 20)      iv.setBackgroundResource(R.drawable.station_01);
        else if (per < 30)      iv.setBackgroundResource(R.drawable.station_01);
        else if (per < 40)      iv.setBackgroundResource(R.drawable.station_02);
        else if (per < 50)      iv.setBackgroundResource(R.drawable.station_02);
        else if (per < 60)      iv.setBackgroundResource(R.drawable.station_03);
        else if (per < 70)      iv.setBackgroundResource(R.drawable.station_03);
        else if (per < 80)      iv.setBackgroundResource(R.drawable.station_04);
        else if (per < 90)      iv.setBackgroundResource(R.drawable.station_04);
        else if (per < 100)     iv.setBackgroundResource(R.drawable.station_05);
        else if (per == 100)    iv.setBackgroundResource(R.drawable.station_05);
        else                    iv.setBackgroundResource(R.drawable.station_05);

        // 장애일 시 아이콘 변경
        if (kioskState != null) {
            if (!kioskState.equals("00")) {
                iv.setBackgroundResource(R.drawable.station_error);
            }
        }

        // 정보창 텍스트
        final String infoText = markerItem.getStationNumber() + ". " +
                markerItem.getStationName() + "\r\n 대여가능 자전거 : " + " " +
                markerItem.getLockerOnCount()  + " / " + markerItem.getLockerTotalCount();

        final InfoWindow infoWindow = new InfoWindow();
        infoWindow.setAdapter(new InfoWindow.ViewAdapter() {
            @NonNull
            @Override
            public View getView(@NonNull InfoWindow infoWindow) {
                return getInfoWindow(infoText);
            }
        });


        final LatLng position = new LatLng(y, x);
        Marker marker = new Marker();
        marker.setPosition(position);
        marker.setMap(mNaverMap);
        marker.setIcon(OverlayImage.fromView(rootMarker));
        marker.setOnClickListener(new Overlay.OnClickListener() {
            @Override
            public boolean onClick(@NonNull Overlay overlay) {
                if (mCurrentWindow != null) {
                    mCurrentWindow.close();
                }
                mCurrentWindow = infoWindow;
                infoWindow.open((Marker)overlay);
                mNaverMap.moveCamera(CameraUpdate.scrollTo(position).animate(CameraAnimation.Easing, 200));
                return false;
            }
        });

        markerItem.setMarker(marker);
        return null;
    }

    //공유 대여소 마커 추가
    public Marker addSbikeMarker(Context context, SbikeStation markerItem, View rootMarker, LinearLayout iv, TextView textMarker) {
        // 마커 위치
        double x = markerItem.getX();
        double y = markerItem.getY();

        // 대여소 번호
        textMarker.setText(markerItem.getBikeParking());

        // 마커 이미지
        iv.setBackgroundResource(R.drawable.station_share);

        // 정보창 텍스트
        final String infoText = markerItem.getStationNumber() + ". " +
                markerItem.getStationName() + "\r\n 대여가능 자전거 : " +
                markerItem.getBikeParking();

        final InfoWindow infoWindow = new InfoWindow();
        infoWindow.setAdapter(new InfoWindow.ViewAdapter() {
            @NonNull
            @Override
            public View getView(@NonNull InfoWindow infoWindow) {
                return getInfoWindow(infoText);
            }
        });

        final LatLng position = new LatLng(y, x);
        Marker marker = new Marker();
        marker.setPosition(position);
        marker.setMap(mNaverMap);
        marker.setIcon(OverlayImage.fromView(rootMarker));
        marker.setOnClickListener(new Overlay.OnClickListener() {
            @Override
            public boolean onClick(@NonNull Overlay overlay) {
                if (mCurrentWindow != null) {
                    mCurrentWindow.close();
                }
                mCurrentWindow = infoWindow;
                infoWindow.open((Marker)overlay);
                mNaverMap.moveCamera(CameraUpdate.scrollTo(position).animate(CameraAnimation.Easing, 200));
                return false;
            }
        });

        markerItem.setMarker(marker);


        return null;
    }

    //마커 정보창 setText
    private View getInfoWindow(String text) {
        View calloutParent = LayoutInflater.from(this).inflate(R.layout.callout_overlay_view, null);
        LinearLayout calloutView = calloutParent.findViewById(R.id.callout_overlay);
        TextView calloutText = calloutParent.findViewById(R.id.callout_text);
        calloutText.setText(text);

        return calloutParent;
    }

    //BLE 스캔 시작
    private void scanLeDevice(final boolean enable){
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    connectDevice(); //스레드 끝나고 연결 시도
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(uuid,mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addDevice(device);
                }
            });
        }
    };


    public void addDevice(BluetoothDevice device) {
        final String deviceName = device.getName();
        final String deviceAddress = device.getAddress();

        if (!mLeDevices.contains(device) && deviceName != null && deviceName.length() > 0){
            Log.e(TAG, "deviceName : "+ deviceName);
            Log.e(TAG, "device : "+ deviceAddress);

            Log.e(TAG, "BLE 목록 : " + deviceName + "\n" + deviceAddress);
            mLeDevices.add(device);
        }
    }


    //BLE 디바이스 연결
    private void connectDevice() {
        //mLeDevices에서 하나씩 연결 시도
        for(BluetoothDevice bluetoothDevice : mLeDevices){
            Log.e(TAG, "찾은 디바이스 : " + bluetoothDevice.getAddress());
            if(MAC_ADDRESS.equals(bluetoothDevice.getAddress())){
                Log.e(TAG, "디바이스에 연결 중 : " + bluetoothDevice.getAddress());
                Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE); //인텐트로 서비스 특성 불러오기, Service 실행
            }
        }
    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "블루투스를 초기화할 수 없습니다.");
                finish();
            }
            mBluetoothLeService.connect(MAC_ADDRESS);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // 서비스에서 발생한 다양한 이벤트를 처리
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        //원하는 브로드캐스트 메세지가 도착하면 자동 호출
        @Override
        public void onReceive(Context context, final Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Log.e(TAG, " !!! BLE 연결 성공 !!!");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                if(mRental){
                    AlertDialog.Builder builder = new AlertDialog.Builder(instance);
                    builder.setTitle("대여에 성공하였습니다.");
                    builder.setPositiveButton("확인",null);
                    builder.show();
                }
                mConnected = false;
                mRental = false;
                //unbindService(mServiceConnection);
                mBluetoothLeService = null;
                Log.e(TAG, " !!! BLE 연결 실패 !!!!");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // GATT 서비스를 발견 - 사용자 인터페이스에 지원되는 모든 서비스 및 특성을 표시
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                // ACTION_DATA_AVAILABLE: 기기에서 수신 된 데이터 또는 알림 작업
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    // 지원 되는 GATT Services/Characteristics
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null){
            return;
        }

        String uuid = null;
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();

        // 사용 가능한 GATT 서비스
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put("NAME", GattAttributes.lookup(uuid, "Unknown service"));
            currentServiceData.put("UUID", uuid);
            gattServiceData.add(currentServiceData);

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // 사용 가능한 Characteristics
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                Log.e(TAG, "gattCharacteristic.getUuid() : " + gattCharacteristic.getUuid()+"");
                Log.e(TAG, "UUID() : " + UUID.fromString(GattAttributes.CHARACTERISTIC_STRING)+"");

                if(gattCharacteristic.getUuid().equals(UUID.fromString(GattAttributes.CHARACTERISTIC_STRING))) {
                    charas.add(gattCharacteristic);
                    HashMap<String, String> currentCharaData = new HashMap<String, String>();
                    uuid = gattCharacteristic.getUuid().toString();
                    currentCharaData.put("NAME", GattAttributes.lookup(uuid, "Unknown characteristic"));
                    currentCharaData.put("UUID", uuid);

                    mGattCharacteristics.add(charas);
                }
            }
        }

        matchCharacteristics();
    }

    private void matchCharacteristics(){
        if (mGattCharacteristics != null) {
            final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(0).get(0);
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                if (mNotifyCharacteristic != null) {
                    mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                    mNotifyCharacteristic = null;
                }
                mBluetoothLeService.readCharacteristic(characteristic);
            }
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mNotifyCharacteristic = characteristic;
                mBluetoothLeService.setCharacteristicNotification(characteristic, true);
            }
        }
    }

    //명령어 제어 스레드
    class MessageThread extends Thread {
        ArrayList<String> message = new ArrayList<String>();
        Handler handler = new Handler();

        public MessageThread(){
            scanLeDevice(true);
            //인텐트로 서비스 특성 불러오기, Service 실행
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

            message.add("@tpwhd");
            message.add("&lopen");
        }

        public void run(){
            for(int i=0; i<message.size(); i++){
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(!mConnected){
                    Log.d(TAG, "Failed to sendData due to no connection");
                    return;
                }
                startCommunication(mGattCharacteristics.get(0).get(0), message.get(i));
            }
        }
    }

    // 주어진 GATT 특성이 선택되면 지원되는 기능을 확인합니다. 이 샘플은 '읽기'및 '알림'기능을 보여줍니다.
    private void startCommunication(BluetoothGattCharacteristic bluetoothGattCharacteristic, String input){
        bluetoothGattCharacteristic.setValue(input);
        mBluetoothLeService.writeCharacteristic(bluetoothGattCharacteristic);
    }

    private void displayData(String data) {
        if (data != null) {
            if("L_OP_OK\r\n".equals(data)){
                mRental = true;
            }
            Log.e(TAG, "BLE 통신 응답 : " + data);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}