package com.marco.gestoreveicoli;

import android.content.Context;
import android.content.SharedPreferences;

/** Preferenze dell'app (sezione impostazioni). */
public class Prefs {
    private static final String PREFS = "impostazioni";

    private static SharedPreferences p(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean notificheAttive(Context c) {
        return p(c).getBoolean("notifiche_attive", false);
    }

    public static void setNotificheAttive(Context c, boolean v) {
        p(c).edit().putBoolean("notifiche_attive", v).apply();
    }

    public static int giorniPreavviso(Context c) {
        return p(c).getInt("giorni_preavviso", 30);
    }

    public static void setGiorniPreavviso(Context c, int giorni) {
        p(c).edit().putInt("giorni_preavviso", giorni).apply();
    }
}
