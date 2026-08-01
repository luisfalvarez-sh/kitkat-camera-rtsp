package com.digitalservices.cooau;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        SharedPreferences prefs = context.getSharedPreferences(CameraWatchdogService.PREFS_NAME, Context.MODE_PRIVATE);

        if (CameraWatchdogService.ACTION_TOGGLE.equals(action)) {
            boolean currentWatchdog = prefs.getBoolean(CameraWatchdogService.KEY_WATCHDOG, true);
            prefs.edit().putBoolean(CameraWatchdogService.KEY_WATCHDOG, !currentWatchdog).apply();

            Intent serviceIntent = new Intent(context, CameraWatchdogService.class);
            serviceIntent.setAction(CameraWatchdogService.ACTION_TOGGLE);
            context.startService(serviceIntent);
        } else if (CameraWatchdogService.ACTION_STOP.equals(action)) {
            Intent serviceIntent = new Intent(context, CameraWatchdogService.class);
            serviceIntent.setAction(CameraWatchdogService.ACTION_STOP);
            context.stopService(serviceIntent);
        }
    }
}
