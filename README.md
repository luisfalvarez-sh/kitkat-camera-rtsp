# KitKat Camera RTSP (Android 4.4 KitKat)

**English Version** | [🇪🇸 Versión en Español](README.es.md)

**KitKat Camera RTSP** is an application and Watchdog service designed to repurpose legacy tablets and devices running **Android 4.4 KitKat (API 19)** (with or without **Root** privileges) as dedicated RTSP security video monitors using VLC (`org.videolan.vlc`).

> 💡 **VLC Compatibility & Clean Relaunch:** Tested and optimized specifically for **VLC version 2.0.6** on Android 4.4 KitKat. Whenever the Watchdog detects a network drop, video freeze, or player closure, **it terminates stale background processes and launches VLC in a 100% fresh session**, ensuring live video playback resumes automatically and seamlessly without requiring manual play interaction.

---

## 🚀 Key Features

1. **Control Panel with Dynamic Parameters:**
   - Target **RTSP URL** configuration.
   - **Network caching (ms)** adjustment.
   - Toggle to **Force RTSP over TCP**.
   - Target player package and activity settings (`org.videolan.vlc`).
   - Persistent storage in local database (`SharedPreferences`).

2. **Watchdog Service:**
   - Asynchronous background monitoring loop on a worker thread (`ScheduledExecutorService`) to prevent UI freezes / ANRs.
   - Checks real-time foreground screen activity (`VideoPlayerActivity`).
   - If a stream stall or exit is detected, **it terminates stale processes and automatically resumes live playback**.

3. **Configurable Check Interval:**
   - Customizable watchdog scan frequency setting in seconds (default 7s).

4. **Persistent & Interactive Notification:**
   - Runs a `Foreground Service` on Android 4.4.
   - Features **direct action buttons backed by a `BroadcastReceiver`**:
     - ⏯ **Pause/Resume Watchdog**: Toggles monitoring without stopping the service or mutating your preference checkboxes.
     - ✖ **Stop Service**: Immediately stops the service and frees system memory.

5. **Autostart on Boot (Boot Receiver):**
   - Listens to `android.intent.action.BOOT_COMPLETED`.
   - On device boot, the app starts automatically in the background and opens the camera stream if *Autostart* is enabled.

6. **Root Privileges Support (`su 0`):**
   - Queries Linux kernel process table (`su 0 ps`) and forces process termination (`am force-stop`) for hardware fallback.

7. **1x1 Desktop Widget (AppWidget):**
   - 1-tap quick launcher icon for the Android 4.4 home screen.

---

## 🛠️ Code Architecture

Built in pure native Java for API 19 (Android 4.4):

* **[`MainActivity.java`](app/src/main/java/com/digitalservices/cooau/MainActivity.java):** Interactive panel with multi-language localization (Spanish / English) and real-time status updates.
* **[`CameraWatchdogService.java`](app/src/main/java/com/digitalservices/cooau/CameraWatchdogService.java):** Main asynchronous background service (`ScheduledExecutorService`).
* **[`NotificationActionReceiver.java`](app/src/main/java/com/digitalservices/cooau/NotificationActionReceiver.java):** BroadcastReceiver for status bar notification action buttons.
* **[`IntentHelper.java`](app/src/main/java/com/digitalservices/cooau/IntentHelper.java):** Dynamic RTSP Intent builder featuring task clearing (`CLEAR_TASK`), live playback parameters (`from_start`), and process cleanup (`killBackgroundProcesses`).
* **[`RootShell.java`](app/src/main/java/com/digitalservices/cooau/RootShell.java):** Root console execution module (`su 0`).
* **[`BootReceiver.java`](app/src/main/java/com/digitalservices/cooau/BootReceiver.java):** System boot event receiver.
* **[`CameraWidgetProvider.java`](app/src/main/java/com/digitalservices/cooau/CameraWidgetProvider.java):** 1x1 Home Screen widget provider.

---

## 📦 Build

```bash
./gradlew assembleDebug --no-daemon
```
The resulting APK installer will be located at: `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 License
This project is licensed under the **MIT License**.  
Creator & Author: **Luis Alvarez** ([@luisfalvarez-sh](https://github.com/luisfalvarez-sh)).
