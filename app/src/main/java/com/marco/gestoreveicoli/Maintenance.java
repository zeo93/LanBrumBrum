package com.marco.gestoreveicoli;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Maintenance {
    public String id;
    public String tipo;
    public String data;      // gg/mm/aaaa
    public long km;
    public double costo;
    public String scadenza;  // prossima scadenza, opzionale (gg/mm/aaaa)
    public String note;

    public Maintenance() {
        this.id = UUID.randomUUID().toString();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("tipo", tipo);
        o.put("data", data);
        o.put("km", km);
        o.put("costo", costo);
        o.put("scadenza", scadenza == null ? "" : scadenza);
        o.put("note", note == null ? "" : note);
        return o;
    }

    public static Maintenance fromJson(JSONObject o) {
        Maintenance m = new Maintenance();
        m.id = o.optString("id", m.id);
        m.tipo = o.optString("tipo", "");
        m.data = o.optString("data", "");
        m.km = o.optLong("km", 0);
        m.costo = o.optDouble("costo", 0);
        m.scadenza = o.optString("scadenza", "");
        m.note = o.optString("note", "");
        return m;
    }
}
