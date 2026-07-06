package com.marco.gestoreveicoli;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Storage {
    private static final String PREFS = "gestore_veicoli";
    private static final String KEY = "veicoli_json";

    private static Storage instance;
    private final SharedPreferences prefs;
    private final List<Vehicle> vehicles = new ArrayList<>();

    private Storage(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        load();
    }

    public static synchronized Storage get(Context context) {
        if (instance == null) {
            instance = new Storage(context);
        }
        return instance;
    }

    private void load() {
        vehicles.clear();
        String json = prefs.getString(KEY, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null) {
                    vehicles.add(Vehicle.fromJson(o));
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void save() {
        try {
            JSONArray arr = new JSONArray();
            for (Vehicle v : vehicles) {
                arr.put(v.toJson());
            }
            prefs.edit().putString(KEY, arr.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    public List<Vehicle> vehicles() {
        return vehicles;
    }

    public Vehicle byId(String id) {
        for (Vehicle v : vehicles) {
            if (v.id.equals(id)) {
                return v;
            }
        }
        return null;
    }

    public void delete(Vehicle v) {
        vehicles.remove(v);
        save();
    }

    /** JSON completo dei dati, per l'esportazione. */
    public String exportJson() {
        try {
            JSONArray arr = new JSONArray();
            for (Vehicle v : vehicles) {
                arr.put(v.toJson());
            }
            return arr.toString(2);
        } catch (Exception e) {
            return "[]";
        }
    }

    /** Sostituisce tutti i dati con quelli del JSON importato. Ritorna i veicoli caricati, -1 se non valido. */
    public int importJson(String json) {
        try {
            JSONArray arr = new JSONArray(json);
            List<Vehicle> imported = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null) {
                    imported.add(Vehicle.fromJson(o));
                }
            }
            vehicles.clear();
            vehicles.addAll(imported);
            save();
            return imported.size();
        } catch (Exception e) {
            return -1;
        }
    }
}
