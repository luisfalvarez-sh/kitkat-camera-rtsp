package com.digitalservices.cooau;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CameraWatchdogService extends Service {

    public static final String PREFS_NAME = "CameraPrefs";
    public static final String KEY_BOOT_START = "boot_start";
    public static final String KEY_WATCHDOG = "watchdog_enabled";
    public static final String KEY_PERSISTENT_NOTIF = "persistent_notif";
    public static final String KEY_USE_ROOT = "use_root";
    public static final String KEY_SERVICE_STATE = "service_state";
    public static final String KEY_SERVICE_PAUSED = "service_paused";
    public static final String KEY_CHECK_INTERVAL = "check_interval";

    public static final String ACTION_TOGGLE = "com.digitalservices.cooau.ACTION_TOGGLE";
    public static final String ACTION_STOP = "com.digitalservices.cooau.ACTION_STOP";

    private static final int NOTIFICATION_ID = 1001;
    public static final int DEFAULT_CHECK_INTERVAL = 7; // 7 segundos predeterminado

    private ScheduledExecutorService executorService;
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
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (ACTION_STOP.equals(action)) {
                stopServiceInternal();
                return START_NOT_STICKY;
            } else if (ACTION_TOGGLE.equals(action)) {
                startForegroundServiceNotification();
            }
        }

        prefs.edit().putBoolean(KEY_SERVICE_STATE, true).apply();

        boolean persistentNotif = prefs.getBoolean(KEY_PERSISTENT_NOTIF, true);
        if (persistentNotif) {
            startForegroundServiceNotification();
        }

        if (!isRunning) {
            isRunning = true;
            scheduleWatchdogLoop(0);
        }

        return START_STICKY;
    }

    private void scheduleWatchdogLoop(long delaySeconds) {
        if (executorService != null && !executorService.isShutdown() && isRunning) {
            executorService.schedule(new Runnable() {
                @Override
                public void run() {
                    long nextDelaySeconds = DEFAULT_CHECK_INTERVAL;
                    try {
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        boolean watchdogEnabled = prefs.getBoolean(KEY_WATCHDOG, true);
                        boolean isPaused = prefs.getBoolean(KEY_SERVICE_PAUSED, false);
                        boolean useRoot = prefs.getBoolean(KEY_USE_ROOT, false);
                        int intervalSeconds = prefs.getInt(KEY_CHECK_INTERVAL, DEFAULT_CHECK_INTERVAL);
                        if (intervalSeconds < 2) intervalSeconds = 2;
                        nextDelaySeconds = intervalSeconds;

                        if (watchdogEnabled && !isPaused) {
                            String targetPkg = prefs.getString(IntentHelper.KEY_VLC_PACKAGE, IntentHelper.DEFAULT_PKG);
                            String targetAct = prefs.getString(IntentHelper.KEY_VLC_ACTIVITY, IntentHelper.DEFAULT_ACT);

                            boolean isVideoPlayingTop = isVlcVideoPlayerTop(targetPkg, targetAct, useRoot);
                            if (!isVideoPlayingTop) {
                                IntentHelper.launchCamera(getApplicationContext(), false);
                            }
                        }
                    } catch (Exception ignored) {
                    }

                    if (isRunning) {
                        scheduleWatchdogLoop(nextDelaySeconds);
                    }
                }
            }, delaySeconds, TimeUnit.SECONDS);
        }
    }

    private boolean isVlcVideoPlayerTop(String targetPkg, String targetAct, boolean useRoot) {
        if (useRoot && RootShell.isVlcVideoPlayerTopRoot(targetPkg, targetAct)) {
            return true;
        }

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (tasks != null && !tasks.isEmpty()) {
                ComponentName topActivity = tasks.get(0).topActivity;
                if (topActivity != null) {
                    return targetPkg.equals(topActivity.getPackageName()) &&
                            targetAct.equals(topActivity.getClassName());
                }
            }
        }
        return false;
    }

    private void stopServiceInternal() {
        isRunning = false;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SERVICE_STATE, false).apply();
        prefs.edit().putBoolean(KEY_SERVICE_PAUSED, false).apply();

        if (executorService != null) {
            executorService.shutdownNow();
        }

        stopForeground(true);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(NOTIFICATION_ID);
        }
        stopSelf();
    }

    private void startForegroundServiceNotification() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isPaused = prefs.getBoolean(KEY_SERVICE_PAUSED, false);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingMainIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent toggleIntent = new Intent(this, NotificationActionReceiver.class);
        toggleIntent.setAction(ACTION_TOGGLE);
        PendingIntent pendingToggleIntent = PendingIntent.getBroadcast(this, 101, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(this, NotificationActionReceiver.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingStopIntent = PendingIntent.getBroadcast(this, 102, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String toggleText = isPaused ? getString(R.string.notif_resume) : getString(R.string.notif_pause);
        String statusText = isPaused ? getString(R.string.notif_paused) : getString(R.string.notif_active);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(statusText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingMainIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_pause, toggleText, pendingToggleIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.notif_stop), pendingStopIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopServiceInternal();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
