package com.marco.gestoreveicoli;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.Locale;

public class NotificationHelper {

    public static final String CHANNEL_SCADENZE = "scadenze";
    public static final String CHANNEL_UPDATES = "aggiornamenti";

    public static void ensureChannels(Context c) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        NotificationChannel scadenze = new NotificationChannel(CHANNEL_SCADENZE,
                c.getString(R.string.canale_scadenze), NotificationManager.IMPORTANCE_DEFAULT);
        scadenze.setDescription(c.getString(R.string.canale_scadenze_desc));
        nm.createNotificationChannel(scadenze);
        NotificationChannel updates = new NotificationChannel(CHANNEL_UPDATES,
                c.getString(R.string.canale_aggiornamenti), NotificationManager.IMPORTANCE_DEFAULT);
        nm.createNotificationChannel(updates);
    }

    /** Notifica le manutenzioni con scadenza entro i giorni di preavviso (o già scadute). */
    public static void checkScadenze(Context c) {
        if (!NotificationManagerCompat.from(c).areNotificationsEnabled()) {
            return;
        }
        int preavviso = Prefs.giorniPreavviso(c);
        long oggi = today();
        int notifId = 1000;
        for (Vehicle v : Storage.get(c).vehicles()) {
            for (Maintenance m : v.manutenzioni) {
                if (m.scadenza != null && !m.scadenza.isEmpty()) {
                    notifId = notifyIfDue(c, notifId, v, m.tipo, m.scadenza, oggi, preavviso);
                }
            }
            // scadenze automatiche da data di immatricolazione
            String rev = v.prossimaRevisione();
            if (rev != null) {
                notifId = notifyIfDue(c, notifId, v, c.getString(R.string.tipo_revisione), rev, oggi, preavviso);
            }
            String bollo = v.prossimoBollo();
            if (bollo != null) {
                notifId = notifyIfDue(c, notifId, v, c.getString(R.string.tipo_bollo), bollo, oggi, preavviso);
            }
            if (v.assicurazioneScadenza != null && !v.assicurazioneScadenza.isEmpty()) {
                String tipo = c.getString(R.string.tipo_assicurazione);
                if (v.assicurazioneCompagnia != null && !v.assicurazioneCompagnia.trim().isEmpty()) {
                    tipo += " (" + v.assicurazioneCompagnia.trim() + ")";
                }
                notifId = notifyIfDue(c, notifId, v, tipo, v.assicurazioneScadenza, oggi, preavviso);
            }
        }
    }

    private static int notifyIfDue(Context c, int notifId, Vehicle v,
                                   String tipo, String scadenza, long oggi, int preavviso) {
        long due = parseDateMillis(scadenza);
        if (due <= 0) {
            return notifId;
        }
        long giorni = (due - oggi) / (24L * 3600 * 1000);
        if (giorni > preavviso) {
            return notifId;
        }
        String testo;
        if (giorni < 0) {
            testo = c.getString(R.string.notifica_scaduta, tipo, scadenza);
        } else if (giorni == 0) {
            testo = c.getString(R.string.notifica_scade_oggi, tipo);
        } else {
            testo = c.getString(R.string.notifica_in_scadenza, tipo, (int) giorni, scadenza);
        }
        notify(c, notifId, v.targa + " · " + v.marcaModello(), testo);
        return notifId + 1;
    }

    private static void notify(Context c, int id, String title, String text) {
        Intent open = new Intent(c, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(c, id, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b = new NotificationCompat.Builder(c, CHANNEL_SCADENZE)
                .setSmallIcon(R.drawable.ic_launcher_fg)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pi)
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(c).notify(id, b.build());
        } catch (SecurityException ignored) {
        }
    }

    public static void notifyUpdate(Context c, String version) {
        if (!NotificationManagerCompat.from(c).areNotificationsEnabled()) {
            return;
        }
        Intent open = new Intent(c, SettingsActivity.class);
        PendingIntent pi = PendingIntent.getActivity(c, 999, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b = new NotificationCompat.Builder(c, CHANNEL_UPDATES)
                .setSmallIcon(R.drawable.ic_launcher_fg)
                .setContentTitle(c.getString(R.string.app_name))
                .setContentText(c.getString(R.string.notifica_aggiornamento, version))
                .setContentIntent(pi)
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(c).notify(999, b.build());
        } catch (SecurityException ignored) {
        }
    }

    static long today() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    static long parseDateMillis(String d) {
        try {
            String[] p = d.trim().split("/");
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(p[2]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[0]), 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }
}
