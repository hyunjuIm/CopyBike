package com.example.copybike.util;

import android.content.SharedPreferences;
import android.util.Log;

public class AbstractPreferencesWrpper {
    private String TAG = "AbstractPreferencesWrpper";

    private SharedPreferences mSharedPreferences;

    public void setPreference(SharedPreferences shared_pref) {
        mSharedPreferences = shared_pref;
    }

    protected int getInt(String key, int def_value) {
        int value = 0;

        try {
            value = mSharedPreferences.getInt(key, def_value);
        } catch (ClassCastException e) {
            Log.e(TAG, "getInt() : ClassCastException");
        }

        return value;
    }

    protected long getLong(String key, long def_value) {
        long value = 0;

        try {
            value = mSharedPreferences.getLong(key, def_value);
        } catch (ClassCastException e) {
            Log.e(TAG, "getLong() : ClassCastException");
        }

        return value;
    }

    protected double getDouble(String key, double def_value) {
        double value = 0;

        try {
            value = Double.longBitsToDouble(mSharedPreferences.getLong(key, Double.doubleToRawLongBits(def_value)));
        } catch (ClassCastException e) {
            Log.e(TAG, "getDouble() : ClassCastException");
        }

        return value;
    }

    protected String getString(String key, String def_value) {
        String value = "";

        try {
            value = mSharedPreferences.getString(key, def_value);
        } catch (ClassCastException e) {
            Log.e(TAG, "getString() : ClassCastException");
        }

        return value;
    }

    protected boolean getBoolean(String key, boolean def_value) {
        boolean value = false;

        try {
            value = mSharedPreferences.getBoolean(key, def_value);
        } catch (ClassCastException e) {
            Log.e(TAG, "getBoolean() : ClassCastException");
        }

        return value;
    }

    protected boolean setInt(String key, int value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt(key, value);

        return edit.commit();
    }

    protected boolean setLong(String key, long value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putLong(key, value);

        return edit.commit();
    }

    protected boolean setDouble(String key, double value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putLong(key, Double.doubleToRawLongBits(value));

        return edit.commit();
    }

    protected boolean setString(String key, String value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(key, value);

        return edit.commit();
    }

    protected boolean setBoolean(String key, boolean value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putBoolean(key, value);

        return edit.commit();
    }
}
