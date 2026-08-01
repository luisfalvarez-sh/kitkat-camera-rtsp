package com.digitalservices.cooau;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private EditText etRtspUrl;
    private EditText etNetworkCaching;
    private CheckBox cbRtspTcp;
    private EditText etVlcPackage;
    private EditText etVlcActivity;
    private EditText etCheckInterval;

    private CheckBox cbBootStart;
    private CheckBox cbWatchdog;
    private CheckBox cbPersistentNotif;
    private CheckBox cbUseRoot;

    private TextView tvStatus;
    private Button btnSaveConfig;
    private Button btnToggleService;
    private Button btnLaunchNow;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(CameraWatchdogService.PREFS_NAME, MODE_PRIVATE);

        initViews();
        loadPreferences();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(this);
        loadPreferences();
        updateServiceUIStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (CameraWatchdogService.KEY_WATCHDOG.equals(key) ||
                CameraWatchdogService.KEY_SERVICE_STATE.equals(key) ||
                CameraWatchdogService.KEY_SERVICE_PAUSED.equals(key)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadPreferences();
                    updateServiceUIStatus();
                }
            });
        }
    }

    private void initViews() {
        etRtspUrl = (EditText) findViewById(R.id.et_rtsp_url);
        etNetworkCaching = (EditText) findViewById(R.id.et_network_caching);
        cbRtspTcp = (CheckBox) findViewById(R.id.cb_rtsp_tcp);
        etVlcPackage = (EditText) findViewById(R.id.et_vlc_package);
        etVlcActivity = (EditText) findViewById(R.id.et_vlc_activity);
        etCheckInterval = (EditText) findViewById(R.id.et_check_interval);

        cbBootStart = (CheckBox) findViewById(R.id.cb_boot_start);
        cbWatchdog = (CheckBox) findViewById(R.id.cb_watchdog);
        cbPersistentNotif = (CheckBox) findViewById(R.id.cb_persistent_notif);
        cbUseRoot = (CheckBox) findViewById(R.id.cb_use_root);

        tvStatus = (TextView) findViewById(R.id.tv_status);
        btnSaveConfig = (Button) findViewById(R.id.btn_save_config);
        btnToggleService = (Button) findViewById(R.id.btn_toggle_service);
        btnLaunchNow = (Button) findViewById(R.id.btn_launch_now);
    }

    private void loadPreferences() {
        etRtspUrl.setText(prefs.getString(IntentHelper.KEY_RTSP_URL, IntentHelper.DEFAULT_URL));
        etNetworkCaching.setText(String.valueOf(prefs.getInt(IntentHelper.KEY_NETWORK_CACHING, IntentHelper.DEFAULT_CACHING)));
        cbRtspTcp.setChecked(prefs.getBoolean(IntentHelper.KEY_RTSP_TCP, true));
        etVlcPackage.setText(prefs.getString(IntentHelper.KEY_VLC_PACKAGE, IntentHelper.DEFAULT_PKG));
        etVlcActivity.setText(prefs.getString(IntentHelper.KEY_VLC_ACTIVITY, IntentHelper.DEFAULT_ACT));
        etCheckInterval.setText(String.valueOf(prefs.getInt(CameraWatchdogService.KEY_CHECK_INTERVAL, CameraWatchdogService.DEFAULT_CHECK_INTERVAL)));

        cbBootStart.setChecked(prefs.getBoolean(CameraWatchdogService.KEY_BOOT_START, true));
        cbWatchdog.setChecked(prefs.getBoolean(CameraWatchdogService.KEY_WATCHDOG, true));
        cbPersistentNotif.setChecked(prefs.getBoolean(CameraWatchdogService.KEY_PERSISTENT_NOTIF, true));
        cbUseRoot.setChecked(prefs.getBoolean(CameraWatchdogService.KEY_USE_ROOT, false));
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = prefs.edit();

        String url = etRtspUrl.getText().toString().trim();
        if (url.isEmpty()) url = IntentHelper.DEFAULT_URL;
        editor.putString(IntentHelper.KEY_RTSP_URL, url);

        int caching = IntentHelper.DEFAULT_CACHING;
        try {
            caching = Integer.parseInt(etNetworkCaching.getText().toString().trim());
        } catch (Exception ignored) {
        }
        editor.putInt(IntentHelper.KEY_NETWORK_CACHING, caching);
        editor.putBoolean(IntentHelper.KEY_RTSP_TCP, cbRtspTcp.isChecked());

        String pkg = etVlcPackage.getText().toString().trim();
        if (pkg.isEmpty()) pkg = IntentHelper.DEFAULT_PKG;
        editor.putString(IntentHelper.KEY_VLC_PACKAGE, pkg);

        String act = etVlcActivity.getText().toString().trim();
        if (act.isEmpty()) act = IntentHelper.DEFAULT_ACT;
        editor.putString(IntentHelper.KEY_VLC_ACTIVITY, act);

        int interval = CameraWatchdogService.DEFAULT_CHECK_INTERVAL;
        try {
            interval = Integer.parseInt(etCheckInterval.getText().toString().trim());
        } catch (Exception ignored) {
        }
        if (interval < 2) interval = 2;
        editor.putInt(CameraWatchdogService.KEY_CHECK_INTERVAL, interval);

        editor.putBoolean(CameraWatchdogService.KEY_BOOT_START, cbBootStart.isChecked());
        editor.putBoolean(CameraWatchdogService.KEY_WATCHDOG, cbWatchdog.isChecked());
        editor.putBoolean(CameraWatchdogService.KEY_PERSISTENT_NOTIF, cbPersistentNotif.isChecked());
        editor.putBoolean(CameraWatchdogService.KEY_USE_ROOT, cbUseRoot.isChecked());

        editor.apply();
    }

    private void setupListeners() {
        btnSaveConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                if (CameraWatchdogService.isServiceRunning(MainActivity.this)) {
                    startService(new Intent(MainActivity.this, CameraWatchdogService.class));
                }
                Toast.makeText(MainActivity.this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
            }
        });

        View.OnClickListener preferenceChangeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                if (CameraWatchdogService.isServiceRunning(MainActivity.this)) {
                    startService(new Intent(MainActivity.this, CameraWatchdogService.class));
                }
            }
        };

        cbBootStart.setOnClickListener(preferenceChangeListener);
        cbWatchdog.setOnClickListener(preferenceChangeListener);
        cbPersistentNotif.setOnClickListener(preferenceChangeListener);
        cbUseRoot.setOnClickListener(preferenceChangeListener);
        cbRtspTcp.setOnClickListener(preferenceChangeListener);

        btnLaunchNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                IntentHelper.launchCamera(MainActivity.this, true);
            }
        });

        btnToggleService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                Intent intent = new Intent(MainActivity.this, CameraWatchdogService.class);
                if (CameraWatchdogService.isServiceRunning(MainActivity.this)) {
                    stopService(intent);
                    Toast.makeText(MainActivity.this, getString(R.string.toast_stopped), Toast.LENGTH_SHORT).show();
                } else {
                    startService(intent);
                    Toast.makeText(MainActivity.this, getString(R.string.toast_started), Toast.LENGTH_SHORT).show();
                }
                updateServiceUIStatus();
            }
        });
    }

    private void updateServiceUIStatus() {
        boolean isRunning = CameraWatchdogService.isServiceRunning(this);
        boolean isPaused = prefs.getBoolean(CameraWatchdogService.KEY_SERVICE_PAUSED, false);

        if (isRunning) {
            if (isPaused) {
                tvStatus.setText(getString(R.string.status_paused));
                tvStatus.setTextColor(Color.parseColor("#FFB300"));
            } else {
                tvStatus.setText(getString(R.string.status_active));
                tvStatus.setTextColor(Color.parseColor("#00E676"));
            }
            btnToggleService.setText(getString(R.string.btn_stop_service));
            btnToggleService.setBackgroundColor(Color.parseColor("#FF5252"));
        } else {
            tvStatus.setText(getString(R.string.status_stopped));
            tvStatus.setTextColor(Color.parseColor("#FF5252"));
            btnToggleService.setText(getString(R.string.btn_start_service));
            btnToggleService.setBackgroundColor(Color.parseColor("#29B6F6"));
        }
    }
}
