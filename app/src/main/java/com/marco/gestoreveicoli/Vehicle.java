package com.marco.gestoreveicoli;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Vehicle {
    public String id;
    public String targa;
    public String marca;
    public String modello;
    public String proprietario;
    public long km;
    public List<Maintenance> manutenzioni = new ArrayList<>();

    public Vehicle() {
        this.id = UUID.randomUUID().toString();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("targa", targa);
        o.put("marca", marca == null ? "" : marca);
        o.put("modello", modello);
        o.put("proprietario", proprietario);
        o.put("km", km);
        JSONArray arr = new JSONArray();
        for (Maintenance m : manutenzioni) {
            arr.put(m.toJson());
        }
        o.put("manutenzioni", arr);
        return o;
    }

    public static Vehicle fromJson(JSONObject o) {
        Vehicle v = new Vehicle();
        v.id = o.optString("id", v.id);
        v.targa = o.optString("targa", "");
        v.marca = o.optString("marca", "");
        v.modello = o.optString("modello", "");
        v.proprietario = o.optString("proprietario", "");
        v.km = o.optLong("km", 0);
        JSONArray arr = o.optJSONArray("manutenzioni");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject mo = arr.optJSONObject(i);
                if (mo != null) {
                    v.manutenzioni.add(Maintenance.fromJson(mo));
                }
            }
        }
        return v;
    }

    public String marcaModello() {
        String m = (marca == null ? "" : marca.trim());
        return m.isEmpty() ? modello : m + " " + modello;
    }

    public double totaleSpese() {
        double tot = 0;
        for (Maintenance m : manutenzioni) {
            tot += m.costo;
        }
        return tot;
    }
}
