package com.marco.gestoreveicoli;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Vehicle {
    public String id;
    public String targa;
    public String marca;
    public String modello;
    public String proprietario;
    public String immatricolazione; // gg/mm/aaaa, opzionale
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
        o.put("immatricolazione", immatricolazione == null ? "" : immatricolazione);
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
        v.immatricolazione = o.optString("immatricolazione", "");
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

    /**
     * Prossima revisione (regole italiane): 4 anni dopo l'immatricolazione,
     * poi ogni 2, entro la fine del mese. Null se manca l'immatricolazione.
     */
    public String prossimaRevisione() {
        Calendar imm = parseData(immatricolazione);
        if (imm == null) {
            return null;
        }
        Calendar due = (Calendar) imm.clone();
        due.add(Calendar.YEAR, 4);
        fineMese(due);
        Calendar oggi = oggi();
        while (due.before(oggi)) {
            due.add(Calendar.YEAR, 2);
            fineMese(due);
        }
        return formatta(due);
    }

    /**
     * Prossima scadenza del bollo: ogni anno, entro la fine del mese
     * di immatricolazione. Null se manca l'immatricolazione.
     */
    public String prossimoBollo() {
        Calendar imm = parseData(immatricolazione);
        if (imm == null) {
            return null;
        }
        Calendar oggi = oggi();
        Calendar due = (Calendar) oggi.clone();
        due.set(Calendar.MONTH, imm.get(Calendar.MONTH));
        fineMese(due);
        if (due.before(oggi)) {
            due.add(Calendar.YEAR, 1);
            fineMese(due);
        }
        return formatta(due);
    }

    static Calendar parseData(String d) {
        try {
            String[] p = d.trim().split("/");
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(Integer.parseInt(p[2]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[0]));
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    private static Calendar oggi() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    private static void fineMese(Calendar c) {
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
    }

    private static String formatta(Calendar c) {
        return String.format(Locale.ITALY, "%02d/%02d/%04d",
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
    }
}
