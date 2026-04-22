package com.fbsharebx.app;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREF = "fbsharebx_prefs";
    private static final String KEY_DARK = "dark_mode";

    public static void applySavedTheme(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        boolean dark = sp.getBoolean(KEY_DARK, false);
        AppCompatDelegate.setDefaultNightMode(
            dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static boolean toggle(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        boolean dark = !sp.getBoolean(KEY_DARK, false);
        sp.edit().putBoolean(KEY_DARK, dark).apply();
        AppCompatDelegate.setDefaultNightMode(
            dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        return dark;
    }
}
