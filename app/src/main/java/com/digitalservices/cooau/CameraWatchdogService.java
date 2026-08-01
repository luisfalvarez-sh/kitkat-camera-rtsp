package com.digitalservices.cooau;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.util.List;

public class CameraWatchdogService extends Service {

    public static final String PREFS_NAME = "CameraPrefs";
    public static final String KEY_BOOT_START = "boot_start";
    public static final String KEY_WATCHDOG = "watchdog_enabled";
    public static final String KEY_PERSISTENT_NOTIF = "persistent_notif";
    public static final String KEY_USE_ROOT = "use_root";
    public static final String KEY_SERVICE_STATE = "service_state";

    public static final String ACTION_TOGGLE = "com.digitalservices.cooau.ACTION_TOGGLE";
    public static final String ACTION_STOP = "com.digitalservices.cooau.ACTION_STOP";

    private static final int NOTIFICATION_ID = 1001;
    private static final long CHECK_INTERVAL_MS = 7000; // 7 segundos

    private Handler handler;
    private Runnable watchdogRunnable;
    private boolean isRunning = false;

    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
            if (services != null) {
                for (ActivityManager.RunningServiceInfo service : services) {
                    if (CameraWatchdogService.class.getName().equals(service.service.getClassName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        setupWatchdogRunnable();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (ACTION_STOP.equals(action)) {
                stopSelf();
                return START_NOT_STICKY;
            } else if (ACTION_TOGGLE.equals(action)) {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                boolean currentWatchdog = prefs.getBoolean(KEY_WATCHDOG, true);
                prefs.edit().putBoolean(KEY_WATCHDOG, !currentWatchdog).apply();
            }
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SERVICE_STATE, true).apply();

        boolean persistentNotif = prefs.getBoolean(KEY_PERSISTENT_NOTIF, true);
        if (persistentNotif) {
            startForegroundServiceNotification();
        }

        if (!isRunning) {
            isRunning = true;
            handler.post(watchdogRunnable);
        }

        return START_STICKY;
    }

    private void startForegroundServiceNotification() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean watchdogActive = prefs.getBoolean(KEY_WATCHDOG, true);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingMainIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent toggleIntent = new Intent(this, CameraWatchdogService.class);
        toggleIntent.setAction(ACTION_TOGGLE);
        PendingIntent pendingToggleIntent = PendingIntent.getService(this, 1, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(this, CameraWatchdogService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String toggleText = watchdogActive ? "Pausar Guardián" : "Activar Guardián";
        String statusText = watchdogActive ? "Monitoreando transmisión RTSP activa" : "Guardián pausado";

        Notification notification = new Notification.Builder(this)
                .setContentTitle("KitKat Camera RTSP Guardián")
                .setContentText(statusText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingMainIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_pause, toggleText, pendingToggleIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Apagar Servicio", pendingStopIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void setupWatchdogRunnable() {
        watchdogRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    boolean watchdogEnabled = prefs.getBoolean(KEY_WATCHDOG, true);
                    boolean useRoot = prefs.getBoolean(KEY_USE_ROOT, false);

                    if (watchdogEnabled) {
                        boolean vlcRunning = isVlcRunning(useRoot);
                        if (!vlcRunning) {
                            IntentHelper.launchCamera(getApplicationContext(), false);
                        }
                    }
                } catch (Exception ignored) {
                }

                if (isRunning) {
                    handler.postDelayed(this, CHECK_INTERVAL_MS);
                }
            }
        };
    }

    private boolean isVlcRunning(boolean useRoot) {
        if (useRoot && RootShell.isVlcRunningRoot()) {
            return true;
        }

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
            if (processes != null) {
                for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
                    if (processInfo.processName.contains("org.videolan.vlc")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SERVICE_STATE, false).apply();

        if (handler != null && watchdogRunnable != null) {
            handler.removeCallbacks(watchdogRunnable);
        }
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
