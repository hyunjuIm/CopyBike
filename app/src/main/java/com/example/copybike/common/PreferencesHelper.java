package com.example.copybike.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.copybike.util.AbstractPreferencesWrpper;

public class PreferencesHelper extends AbstractPreferencesWrpper {

    private static PreferencesHelper instance = null;

    private PreferencesHelper(Context context) {
        //데이터를 파일로 저장
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        super.setPreference(sharedPref);
    }

    public static PreferencesHelper getInstance(Context context) {
        if (null == instance) {
            synchronized(PreferencesHelper.class) {
                if (null == instance) {
                    instance = new PreferencesHelper(context);
                }
            }
        }
        return instance;
    }

    // 로그인
    public void setLogin(String name, String id, String cell_phone, String telecom, String rent_pass,
                         String u_birth, String email, String auth_token, String mem_group,
                         String voucher_start_date, String voucher_end_date) {
        resetUserInfo();
        setUserName(name);
        setUserID(id);
        setUserCellPhone(cell_phone);
        setUserTelecom(telecom);
        setUserRentalPassword(rent_pass);
        setUserBirth(u_birth);
        setUserEmail(email);
        setAuthToken(auth_token);
        setUserMemberGroup(mem_group);
        setUserVoucherStartDate(voucher_start_date);
        setUserVoucherEndDate(voucher_end_date);
    }

    // 유저정보초기화
    public void setInitUserInfo() {
        setAuthToken(null);
        resetUserInfo();
    }

    // 로그인 유무
    public boolean isLogin() {
        return getAuthToken()!=null;
    }

    // 유저정보입력
    public void setUserInfo(String name, String id, String cell_phone, String telecom, String rent_pass,
                            String u_birth, String email, String mem_grpup,
                            String voucher_start_date, String voucher_end_date,
                            String sbike_id, String sbike_ble_address) {
        resetUserInfo();
        setUserName(name);
        setUserID(id);
        setUserCellPhone(cell_phone);
        setUserTelecom(telecom);
        setUserRentalPassword(rent_pass);
        setUserBirth(u_birth);
        setUserEmail(email);
        setUserVoucherStartDate(voucher_start_date);
        setUserVoucherEndDate(voucher_end_date);
        setUserMemberGroup(mem_grpup);
        setSbikeId(sbike_id);
        setSbikeBleAddress(sbike_ble_address);
    }

    // 유저정보삭제
    public void resetUserInfo() {
        setUserName(null);
        setUserID(null);
        setUserCellPhone(null);
        setUserTelecom(null);
        setUserRentalPassword(null);
        setUserBirth(null);
        setUserEmail(null);
        setUserMemberGroup(null);
        setUserVoucherStartDate(null);
        setUserVoucherEndDate(null);
        setSbikeId(null);
        setSbikeBleAddress(null);
    }

    // 자동 로그인 유무
    public boolean isAutoLogin() {
        return getAutoLogin();
    }

    // 정회원 유무
    public boolean isRegularMember() {
        if (getUserVoucherStartDate() != null) {
            if (!getUserVoucherStartDate().trim().equals("null")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // Setters

    // -- 유저정보

    public boolean setUserName(String value) {
        return setString("PREF_USER_NAME", value);
    }
    public boolean setUserID(String value) {
        return setString("PREF_USER_ID", value);
    }
    public boolean setUserCellPhone(String value) {
        return setString("PREF_USER_CELL_PHONE", value);
    }
    public boolean setUserTelecom(String value) {
        return setString("PREF_USER_TELECOM", value);
    }
    public boolean setUserRentalPassword(String value) {
        return setString("PREF_USER_RENTPASS", value);
    }
    public boolean setUserBirth(String value) {
        return setString("PREF_USER_BIRTH", value);
    }
    public boolean setUserEmail(String value) {
        return setString("PREF_USER_EMAIL", value);
    }
    public boolean setAuthToken(String value) {
        return setString("PREF_USER_AUTH_TOKEN", value);
    }
    public boolean setUserMemberGroup(String value) {
        return setString("PREF_USER_MEMBER_GROUP", value);
    }
    public boolean setUserVoucherStartDate(String value) {
        return setString("PREF_USER_VOUCHER_START_DATE", value);
    }
    public boolean setUserVoucherEndDate(String value) {
        return setString("PREF_USER_VOUCHER_END_DATE", value);
    }

    // -- 로그인 관련 유틸

    public boolean setAutoLogin(boolean value) {
        return setBoolean("PREF_AUTO_LOGIN_YN", value);
    }

    // -- 푸시정보

    public boolean setPush(boolean value) {
        return setBoolean("PREF_PUSH", value);
    }

    // -- 디바이스정보

    public boolean setScreenWidth(int value) {
        return setInt("PREF_SCREEN_WIDTH", value);
    }
    public boolean setTheFirst(boolean value) {
        return setBoolean("PREF_THE_FIRST", value);
    }

    // -- 공유자전거 대여정보

    public boolean setSbikeBleAddress(String value) {
        return setString("PREF_SBIKE_BLE_ADDRESS", value);
    }
    public boolean setSbikeId(String value) {
        return setString("PREF_SBIKE_ID", value);
    }

    // Getters

    // -- 유저정보

    public String getUserName() {
        return getString("PREF_USER_NAME", null);
    }
    public String getUserID() {
        return getString("PREF_USER_ID", null);
    }
    public String getUserCellPhone() {
        return getString("PREF_USER_CELL_PHONE", null);
    }
    public String getUserTelecom() {
        return getString("PREF_USER_TELECOM", null);
    }
    public String getUserRentalPassword() {
        return getString("PREF_USER_RENTPASS", null);
    }
    public String getUserBirth() {
        return getString("PREF_USER_BIRTH", null);
    }
    public String getUserEmail() {
        return getString("PREF_USER_EMAIL", null);
    }
    public String getUserVoucherStartDate() {
        return getString("PREF_USER_VOUCHER_START_DATE", null);
    }
    public String getUserVoucherEndDate() {
        return getString("PREF_USER_VOUCHER_END_DATE", null);
    }
    public String getAuthToken() {
        return getString("PREF_USER_AUTH_TOKEN", null);
    }
    public String getUserMemberGroup() {
        return getString("PREF_USER_MEMBER_GROUP", null);
    }

    // -- 로그인 관련 유틸

    public boolean getAutoLogin() {
        return getBoolean("PREF_AUTO_LOGIN_YN", true);
    }

    // -- 푸시정보

    public boolean getPush() {
        return getBoolean("PREF_PUSH", true);
    }

    // -- 디바이스정보

    public int getScreenWidth() {
        return getInt("PREF_SCREEN_WIDTH", 0);
    }
    public boolean getTheFirst() {
        return getBoolean("PREF_THE_FIRST", true);
    }

    // -- 공유자전거 대여정보

    public String getSbikeId() {
        return getString("PREF_SBIKE_ID", null);
    }
    public String getSbikeBleAddress() {
        return getString("PREF_SBIKE_BLE_ADDRESS", null);
    }

    // -- 관리자 유무
    public boolean getAdminLogin() {
        String memberGroup = getString("PREF_USER_MEMBER_GROUP", null);
        if (memberGroup != null) {
            if (memberGroup.equals("99") || memberGroup.equals("98") || memberGroup.equals("97") || memberGroup.equals("96")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
