package com.khushitshah.blindspartner;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class StaticSettings {
    public static float speechRate = 1.5f;
    public static boolean onlineTranslation = false;
    public static String lang = "english";
    static String emergencyNumber = "";

    public static void setSettingValues(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        emergencyNumber = pref.getString("emergency", "");
        lang = pref.getString("lang", "").toLowerCase();
        speechRate = pref.getFloat("speechRateCust", 1.5f);
        onlineTranslation = pref.getBoolean("trans", false);

    }

    public static void setSettings(String key, String value, Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        pref.edit().putString(key, value).apply();
    }

    public static void setSettings(String key, float value, Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        pref.edit().putFloat(key, value).apply();
    }

    public static void setSettings(String key, boolean value, Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        pref.edit().putBoolean(key, value).apply();
    }

    public static void setSettings(String key, int value, Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        pref.edit().putInt(key, value).apply();
    }
}
