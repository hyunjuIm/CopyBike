package com.example.copybike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.copybike.common.NaverMapHelper;
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
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static MainActivity instance;
    final static String TAG = "HYUNJU";

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
    private boolean isZoomSetting = true;
    private boolean isLocationUsing = true;
    private boolean trackOnce; //내 위치 셋팅

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
    private String mRental;

    private Intent gattServiceIntent;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

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

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(MAC_ADDRESS);
            Log.e(TAG, "Connect request result = " + result);
        }
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
                scanLeDevice(true);
                //인텐트로 서비스 특성 불러오기, Service 실행
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

                messageThread thread = new messageThread();
                thread.start();
            }
        });

        //이용권 구매
        findViewById(R.id.ll_btn_payment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PayActivity.class);
                intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //로그인
        findViewById(R.id.btn_title_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (prefHelper.isLogin()) {
//                    Intent intent = new Intent(getBaseContext(), MyPageActivity.class);
//                    startActivity(intent);
//                } else {
//                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
//                    startActivity(intent);
//                }

                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(intent);
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

        naverMap.addOnLocationChangeListener(mLocationChangeListener);
        naverMap.setOnMapClickListener(mMapClickListener);
        naverMap.setLocationSource(new FusedLocationSource(this, 1));
        naverMap.getUiSettings().setZoomControlEnabled(false);
        naverMap.getUiSettings().setCompassEnabled(false);
        naverMap.getUiSettings().setScaleBarEnabled(false);
        naverMap.getUiSettings().setLogoMargin(10, 0, 0, 10);
        naverMap.getUiSettings().setRotateGesturesEnabled(false);
        naverMap.setExtent(new LatLngBounds(
                new LatLng(30.664915, 122.628502), new LatLng(39.788312, 132.893671)));
        naverMap.setLiteModeEnabled(true);
        naverMap.setLocationTrackingMode(LocationTrackingMode.None);
        naverMap.setCameraPosition(new CameraPosition(initialPosition, 13));

        //내 위치 찾기
        requestLocationUpdates();
        //마커 초기화 셋팅
        clearAllMarker();
        //데이터 받아오기
        makeRequest();
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
            mNaverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);
        }
    }

    // 내 위치찾기 종료
    private void myLocationStop() {

        // 내위치 버튼 변경 (Default)
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

    private final NaverMap.OnLocationChangeListener mLocationChangeListener = new NaverMap.OnLocationChangeListener() {
        @Override
        public void onLocationChange(@NonNull Location location) {
            if (!isZoomSetting) {
                isZoomSetting = true;
                // 지도 zoom레벨 설정
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mNaverMap.setCameraPosition(new CameraPosition(latLng, 13));
            }

            if (trackOnce) {
                trackOnce = false;
                mNaverMap.setCameraPosition(new CameraPosition(
                        new LatLng(location.getLatitude(), location.getLongitude()), 17));
            }
        }
    };

    private final NaverMap.OnMapClickListener mMapClickListener = new NaverMap.OnMapClickListener() {
        @Override
        public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
            int radius = 1;
            List<Pickable> pickableList = mNaverMap.pickAll(pointF, radius);
            ArrayList<Marker> markers = new ArrayList<>();

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

    //volley로 대여소 정보 받아오기
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
        request.setShouldCache(false);
        requestQueue.add(request);
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
                    //connectDevice(); //스레드 끝나고 연결 시도
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
                Log.e(TAG, "Unable to initialize Bluetooth");
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
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Log.e(TAG, " !!! BLE 연결 성공 !!!");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.e(TAG, " !!! BLE 연결 실패 !!!!");
                unbindService(mServiceConnection);
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
        String unknownServiceString ="Unknown service";
        String unknownCharaString = "Unknown characteristic";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();

        // 사용 가능한 GATT 서비스
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put("NAME", GattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put("UUID", uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
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
                    currentCharaData.put("NAME", GattAttributes.lookup(uuid, unknownCharaString));
                    currentCharaData.put("UUID", uuid);
                    gattCharacteristicGroupData.add(currentCharaData);

                    mGattCharacteristics.add(charas);
                    gattCharacteristicData.add(gattCharacteristicGroupData);
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
    class messageThread extends Thread {
        ArrayList<String> message = new ArrayList<String>();

        public void run(){
            message.add("@tpwhd");
            message.add("&lopen");

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
            mRental = data;
            Log.e(TAG, "BLE 통신 응답 : " + data);
            Log.e(TAG, "mRental : " + mRental);
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