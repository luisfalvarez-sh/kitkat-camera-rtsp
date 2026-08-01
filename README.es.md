# KitKat Camera RTSP (Android 4.4 KitKat)

[🇺🇸 English Version](README.md) | **🇪🇸 Versión en Español**

**KitKat Camera RTSP** es una aplicación e infraestructura guardián (*Watchdog*) diseñada para reutilizar tablets y dispositivos antiguos con **Android 4.4 KitKat (API 19)** (con o sin privilegios **Root**) como monitores dedicados de vídeo de seguridad RTSP usando VLC (`org.videolan.vlc`).

> 💡 **Compatibilidad de VLC & Reinicio Limpio:** Probado y optimizado específicamente para **VLC versión 2.0.6** en Android 4.4 KitKat. Cada vez que el Guardián detecta una caída de red, congelamiento o cierre del vídeo, **elimina los procesos colgados previos y relanza VLC en una sesión 100% nueva**, asegurando que la reproducción en vivo continúe de manera automática y fluida sin necesidad de presionar el botón de reproducción (*Play*).

---

## 🚀 Características Principales

1. **Panel de Control con Parámetros Dinámicos:**
   - Configuración de la **URL RTSP** objetivo.
   - Ajuste de **Caching de red (ms)**.
   - Conmutador para **Forzar RTSP sobre TCP**.
   - Ajuste de paquete y actividad del reproductor objetivo (`org.videolan.vlc`).
   - Almacenamiento persistente en base de datos local (`SharedPreferences`).

2. **Servicio Guardián (Watchdog Service):**
   - Bucle de monitoreo continuo en segundo plano asíncrono en hilo secundario (evita bloqueos de UI / ANR).
   - Comprueba en tiempo real si la pantalla activa en primer plano es el reproductor de vídeo (`VideoPlayerActivity`).
   - Si detecta caída de red o cierre del reproductor, **destruye el proceso retenido y reactiva el vídeo en vivo automáticamente**.

3. **Intervalo de Revisión Configurable:**
   - Ajuste personalizable del tiempo de escaneo del Guardián en segundos (predeterminado 7s).

4. **Notificación Persistente e Interactiva:**
   - Mantiene un servicio en primer plano (`Foreground Service`) en Android 4.4.
   - Incluye **botones de acción directa mediante `BroadcastReceiver`**:
     - ⏯ **Pausar/Activar Guardián**: Alterna el monitoreo sin apagar el servicio ni alterar tu configuración de preferencia.
     - ✖ **Apagar Servicio**: Detiene el servicio y libera memoria del sistema al instante.

5. **Auto-Inicio al Encender (Boot Receiver):**
   - Escucha `android.intent.action.BOOT_COMPLETED`.
   - Al encender la tablet, la app se inicia automáticamente en segundo plano y abre la cámara si el switch de *Autostart* está activo.

6. **Soporte de Privilegios Root (`su 0`):**
   - Consulta la tabla de procesos del kernel Linux (`su 0 ps`) y fuerza cierre (`am force-stop`) como mecanismo de rescate de hardware.

7. **Widget de Escritorio 1x1 (AppWidget):**
   - Icono de acceso rápido de 1 toque para el lanzador de Android 4.4.

---

## 🛠️ Arquitectura de Código

Construido en Java puro nativo para API 19 (Android 4.4):

* **[`MainActivity.java`](app/src/main/java/com/digitalservices/cooau/MainActivity.java):** Panel interactivo con localización multi-idioma (Español / Inglés) y actualización de estado en tiempo real.
* **[`CameraWatchdogService.java`](app/src/main/java/com/digitalservices/cooau/CameraWatchdogService.java):** Servicio principal en segundo plano asíncrono (`ScheduledExecutorService`).
* **[`NotificationActionReceiver.java`](app/src/main/java/com/digitalservices/cooau/NotificationActionReceiver.java):** Receptor de eventos para botones de la persiana de notificaciones.
* **[`IntentHelper.java`](app/src/main/java/com/digitalservices/cooau/IntentHelper.java):** Constructor de Intents RTSP dinámicos con banderas de limpieza de tareas (`CLEAR_TASK`), parámetros en vivo (`from_start`) y matado de procesos previos (`killBackgroundProcesses`).
* **[`RootShell.java`](app/src/main/java/com/digitalservices/cooau/RootShell.java):** Módulo de comandos de consola root (`su 0`).
* **[`BootReceiver.java`](app/src/main/java/com/digitalservices/cooau/BootReceiver.java):** Receptor de eventos de arranque de sistema.
* **[`CameraWidgetProvider.java`](app/src/main/java/com/digitalservices/cooau/CameraWidgetProvider.java):** Proveedor del widget 1x1 de escritorio.

---

## 📦 Compilación

```bash
./gradlew assembleDebug --no-daemon
```
El instalador APK resultante se encuentra en: `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 Licencia
Este proyecto se distribuye bajo la licencia **MIT License**.  
Creador & Autor: **Luis Alvarez** ([@luisfalvarez-sh](https://github.com/luisfalvarez-sh)).
