package com.marco.gestoreveicoli;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Prefs.notificheAttive(context)) {
            NotificationHelper.ensureChannels(context);
            NotificationHelper.checkScadenze(context);
        }
    }
}
