package com.digitalservices.cooau;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

public class IntentHelper {

    public static final String KEY_RTSP_URL = "rtsp_url";
    public static final String KEY_NETWORK_CACHING = "network_caching";
    public static final String KEY_RTSP_TCP = "rtsp_tcp";
    public static final String KEY_VLC_PACKAGE = "vlc_package";
    public static final String KEY_VLC_ACTIVITY = "vlc_activity";

    public static final String DEFAULT_URL = "rtsp://192.168.1.100:8554/live";
    public static final String DEFAULT_PKG = "org.videolan.vlc";
    public static final String DEFAULT_ACT = "org.videolan.vlc.gui.video.VideoPlayerActivity";
    public static final int DEFAULT_CACHING = 300;

    public static Intent buildVlcIntent(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(CameraWatchdogService.PREFS_NAME, Context.MODE_PRIVATE);

        String rtspUrl = prefs.getString(KEY_RTSP_URL, DEFAULT_URL);
        String vlcPkg = prefs.getString(KEY_VLC_PACKAGE, DEFAULT_PKG);
        String vlcAct = prefs.getString(KEY_VLC_ACTIVITY, DEFAULT_ACT);
        int caching = prefs.getInt(KEY_NETWORK_CACHING, DEFAULT_CACHING);
        boolean rtspTcp = prefs.getBoolean(KEY_RTSP_TCP, true);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setComponent(new ComponentName(vlcPkg, vlcAct));
        intent.setDataAndType(Uri.parse(rtspUrl), "video/*");

        // Parámetros de reproducción de VLC
        intent.putExtra("rtsp_tcp", rtspTcp);
        intent.putExtra("rtsp-tcp", rtspTcp);
        intent.putExtra("network_caching", caching);
        intent.putExtra("from_start", true);
        intent.putExtra("position", 0L);

        // Banderas para forzar reinicio limpio del reproductor sin pantalla en negro ni pausa
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        return intent;
    }

    public static void launchCamera(Context context, boolean showToast) {
        if (showToast) {
            Toast.makeText(context.getApplicationContext(), context.getString(R.string.toast_opening), Toast.LENGTH_SHORT).show();
        }

        try {
            Intent intent = buildVlcIntent(context);
            context.startActivity(intent);
        } catch (Exception e) {
            if (showToast) {
                Toast.makeText(context.getApplicationContext(), "Error al abrir VLC: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            SharedPreferences prefs = context.getSharedPreferences(CameraWatchdogService.PREFS_NAME, Context.MODE_PRIVATE);
            String rtspUrl = prefs.getString(KEY_RTSP_URL, DEFAULT_URL);
            String vlcPkg = prefs.getString(KEY_VLC_PACKAGE, DEFAULT_PKG);
            String vlcAct = prefs.getString(KEY_VLC_ACTIVITY, DEFAULT_ACT);
            int caching = prefs.getInt(KEY_NETWORK_CACHING, DEFAULT_CACHING);
            boolean rtspTcp = prefs.getBoolean(KEY_RTSP_TCP, true);

            String cmd = String.format("am start -a android.intent.action.VIEW -n %s/%s -d \"%s\" -t \"video/*\" --ez \"rtsp_tcp\" %b --ei \"network_caching\" %d --ez \"from_start\" true -f 0x14000000",
                    vlcPkg, vlcAct, rtspUrl, rtspTcp, caching);
            RootShell.execRoot(cmd);
        }
    }
}
