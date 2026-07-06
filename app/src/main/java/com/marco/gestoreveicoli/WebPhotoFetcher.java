package com.marco.gestoreveicoli;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

/** Cerca su Wikipedia la foto principale del modello (es. "Fiat Panda") e la scarica. */
public class WebPhotoFetcher {

    private static final String UA = "LanBrumBrum/1.0 (app gestione veicoli)";

    public interface Callback {
        /** file != null se trovata; file == null ed error == null se nessun risultato. */
        void onResult(File file, String error);
    }

    public static void fetch(Context context, String query, Callback callback) {
        Handler main = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                // en.wikipedia ha quasi sempre l'immagine principale dei modelli, it spesso no
                String imageUrl = findImageUrl(query, "en");
                if (imageUrl == null) {
                    imageUrl = findImageUrl(query, "it");
                }
                if (imageUrl == null) {
                    main.post(() -> callback.onResult(null, null));
                    return;
                }
                File tmp = new File(context.getCacheDir(), "web_photo_tmp.jpg");
                download(imageUrl, tmp);
                main.post(() -> callback.onResult(tmp, null));
            } catch (Exception e) {
                main.post(() -> callback.onResult(null, e.getMessage()));
            }
        }).start();
    }

    private static String findImageUrl(String query, String lang) {
        try {
            String api = "https://" + lang + ".wikipedia.org/w/api.php" +
                    "?action=query&generator=search&gsrsearch=" + URLEncoder.encode(query, "UTF-8") +
                    "&gsrlimit=1&prop=pageimages&piprop=thumbnail&pithumbsize=1280&format=json";
            HttpURLConnection conn = (HttpURLConnection) new URL(api).openConnection();
            conn.setRequestProperty("User-Agent", UA);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            if (conn.getResponseCode() != 200) {
                conn.disconnect();
                return null;
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line);
                }
            }
            conn.disconnect();
            JSONObject queryObj = new JSONObject(sb.toString()).optJSONObject("query");
            JSONObject pages = queryObj == null ? null : queryObj.optJSONObject("pages");
            if (pages == null) {
                return null;
            }
            Iterator<String> keys = pages.keys();
            while (keys.hasNext()) {
                JSONObject page = pages.getJSONObject(keys.next());
                JSONObject thumb = page.optJSONObject("thumbnail");
                if (thumb != null) {
                    String src = thumb.optString("source", "");
                    if (!src.isEmpty()) {
                        return src;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static void download(String url, File dest) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", UA);
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
        } finally {
            conn.disconnect();
        }
    }
}
