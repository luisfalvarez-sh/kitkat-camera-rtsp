package com.digitalservices.cooau;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

    public static final String ACTION_QUICKBOOT = "android.intent.action.QUICKBOOT_POWERON";
    public static final String ACTION_HTC_QUICKBOOT = "com.htc.intent.action.QUICKBOOT_POWERON";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                ACTION_QUICKBOOT.equals(action) ||
                ACTION_HTC_QUICKBOOT.equals(action) ||
                Intent.ACTION_REBOOT.equals(action)) {

            // Ejecutar en hilo secundario con un delay de 3 segundos para evitar ANR de BroadcastQueue durante el arranque
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Esperar 3 segundos a que SuperSU y la interfaz del sistema se estabilicen
                        Thread.sleep(3000);

                        SharedPreferences prefs = context.getSharedPreferences(CameraWatchdogService.PREFS_NAME, Context.MODE_PRIVATE);
                        boolean bootStart = prefs.getBoolean(CameraWatchdogService.KEY_BOOT_START, true);

                        if (bootStart) {
                            // 1. Iniciar servicio guardián
                            Intent serviceIntent = new Intent(context, CameraWatchdogService.class);
                            context.startService(serviceIntent);

                            // 2. Lanzar transmisión de la cámara
                            IntentHelper.launchCamera(context, false);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }).start();
        }
    }
}
