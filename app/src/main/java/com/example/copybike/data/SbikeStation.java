package com.example.copybike.data;

import com.naver.maps.map.overlay.Marker;

public class SbikeStation {

    private int img                     = -1;       // 마커 이미지
    private String mainAddress 			= null;		// 주소1
    private String subAddress 			= null;		// 주소2
    private String stationId 			= null;		// 스테이션 ID
    private String stationName 			= null;		// 스테이션 명
    private String stationNumber 		= null;		// 스테이션 번호
    private String bikeParking 			= null;		// 자전거 대여 가능
    private int geofenceDistance 		= 0;		// 지오펜스 거리
    private String area 				= null;		// 권역
    private Double x 					= null;		// 스테이션 위치 x
    private Double y 					= null;		// 스테이션 위치 y

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

    public String getSubAddress() { return subAddress; }
    public void setSubAddress(String subAddress) { this.subAddress = subAddress; }

    public String getBikeParking() { return bikeParking; }
    public void setBikeParking(String bike) { this.bikeParking = bike; }

    public int getGeofenceDistance() { return geofenceDistance; }
    public void setGeofenceDistance(int distance) { this.geofenceDistance = distance; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

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

    // Marker
    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
