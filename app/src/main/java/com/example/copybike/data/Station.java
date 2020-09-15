package com.example.copybike.data;

import com.naver.maps.map.overlay.Marker;

public class Station {

    private int img                     = -1;       // 마커 이미지
    private String mainAddress 			= null;		// 주소1
    private String subAddress 			= null;		// 주소2
    private int lockerOnCount 			= -1;		// 자전거 대여 가능 수
    private int lockerOffCount 			= -1;		// 자전거 대여 불가능 수
    private int lockerTotalCount 		= -1;		// 총 자전거 수
    private String stationId 			= null;		// 스테이션 ID
    private String stationName 			= null;		// 스테이션 명
    private String stationNumber 		= null;		// 스테이션 번호
    private double lockerOffPercent 	= -1.0;		// 자전거 대여 가능 점유율
    private double lockerOnPercent 		= -1.0;		// 자전거 대여 불가능 점유율
    private Double x 					= null;		// 스테이션 위치 x
    private Double y 					= null;		// 스테이션 위치 y
    private String kioskState 			= null;		// 키오스크 상태

    private Marker marker				= null;		// 네이버지도 v3 마커

    // 마커 이미지
    public int getImg() {
        return img;
    }
    public void setImg(int img) {
        this.img = img;
    }

    // mainAddress
    public String getMainAddress() { return mainAddress; }
    public void setMainAddress(String mainAddress) { this.mainAddress = mainAddress; }

    // subAddress
    public String getSubAddress() { return subAddress; }
    public void setSubAddress(String subAddress) { this.subAddress = subAddress; }

    // countLockerOn
    public int getLockerOnCount() { return lockerOnCount; }
    public void setCountLockerOn(int count) { this.lockerOnCount = count; }

    // countLockerOff
    public int getLockerOffCount() { return lockerOffCount; }
    public void setCountLockerOff(int count) { this.lockerOffCount = count; }

    // countLockerTotal
    public int getLockerTotalCount() { return lockerTotalCount; }
    public void setLockerTotalCount(int count) {
        this.lockerTotalCount = count;
    }

    // IP getter, setter
    // public String getIP() { return IP; }
    // public void setIP(String IP) { this.IP = IP; }

    // stationId getter, setter
    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }

    // stationName
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    // stationNumber
    public String getStationNumber() { return stationNumber; }
    public void setStationNumber(String stationNumber) {
        this.stationNumber = stationNumber;
    }

    // percentLockerOff
    public double getLockerOffPercent() { return lockerOffPercent; }
    public void setLockerOffPercent(double percent) {
        this.lockerOffPercent = percent;
    }

    // percentLockerOn
    public double getLockerOnPercent() { return lockerOnPercent; }
    public void setLockerOnPercent(double percent) {
        this.lockerOnPercent = percent;
    }

    // x getter, setter
    public Double getX() { return x; }
    public void setX(Double x) {
        this.x = x;
    }

    // y getter, setter
    public Double getY() { return y; }
    public void setY(Double y) {
        this.y = y;
    }

    // kioskState
    public String getKioskState() { return kioskState; }
    public void setKioskState(String kioskState) {
        this.kioskState = kioskState;
    }

    // Marker
    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

}
