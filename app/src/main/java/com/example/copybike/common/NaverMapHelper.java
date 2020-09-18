package com.example.copybike.common;

import com.example.copybike.data.SbikeStation;
import com.example.copybike.data.Station;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NaverMapHelper {
    public static ArrayList<Station> getStationInfo(JSONArray results) {
        try {
            ArrayList<Station> stationList = new ArrayList<Station>();
            Station station;

            for (int i=0; i<results.length(); i++) {
                JSONObject station_data = results.getJSONObject(i);
                station = new Station();

                int locker_total = station_data.optInt("locker_total");
                int locker_on = station_data.optInt("locker_on");
                int locker_off = locker_total - locker_on;
                int lockerOffPercent = (locker_on * 100) / locker_total;
                int lockerOnPercent = (locker_off * 100) / locker_total;

                station.setMainAddress(station_data.optString("gu"));
                station.setSubAddress(station_data.optString("dong"));
                station.setCountLockerOn(locker_on);
                station.setCountLockerOff(locker_off);
                station.setLockerTotalCount(locker_total);
                station.setStationId(station_data.optString("station_no"));
                station.setStationName(station_data.optString("station_nm"));
                station.setStationNumber(station_data.optString("station_no"));
                station.setLockerOffPercent(lockerOffPercent);
                station.setLockerOnPercent(lockerOnPercent);
                station.setX(station_data.optDouble("x_pos"));
                station.setY(station_data.optDouble("y_pos"));
                station.setKioskState(station_data.optString("kiosk_state"));

                stationList.add(station);
            }

            return stationList;
        } catch (JSONException e) {
            return null;
        }
    }

    public static ArrayList<SbikeStation> getSbikeStationInfo(JSONArray results) {
        try {
            ArrayList<SbikeStation> stationList = new ArrayList<SbikeStation>();
            SbikeStation station;

            for (int i=0; i<results.length(); i++) {
                JSONObject station_data = results.getJSONObject(i);
                station = new SbikeStation();

                station.setBikeParking(station_data.optString("bike_parking"));
                station.setGeofenceDistance(station_data.optInt("geofence_distance"));
                station.setMainAddress(station_data.optString("gu"));
                station.setSubAddress(station_data.optString("dong"));
                station.setStationId(station_data.optString("station_id"));
                station.setStationName(station_data.optString("station_name"));
                station.setStationNumber(station_data.optString("station_no"));
                station.setX(station_data.optDouble("x_pos"));
                station.setY(station_data.optDouble("y_pos"));
                station.setArea(station_data.optString("area"));

                stationList.add(station);
            }

            return stationList;
        } catch (JSONException e) {
            return null;
        }
    }
}
