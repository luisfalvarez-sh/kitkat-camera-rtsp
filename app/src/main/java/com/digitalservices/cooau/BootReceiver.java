package com.digitalservices.cooau;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences(CameraWatchdogService.PREFS_NAME, Context.MODE_PRIVATE);
            boolean bootStart = prefs.getBoolean(CameraWatchdogService.KEY_BOOT_START, true);

            if (bootStart) {
                // Iniciar servicio guardián
                Intent serviceIntent = new Intent(context, CameraWatchdogService.class);
                context.startService(serviceIntent);

                // Lanzar la transmisión de la cámara
                IntentHelper.launchCamera(context, true);
            }
        }
    }
}
