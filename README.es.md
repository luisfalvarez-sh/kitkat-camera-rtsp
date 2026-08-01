# KitKat Camera RTSP (Android 4.4 KitKat)

[🇺🇸 English Version](README.md) | **🇪🇸 Versión en Español**

**KitKat Camera RTSP** es una aplicación e infraestructura guardián (*Watchdog*) diseñada para reutilizar tablets y dispositivos antiguos con **Android 4.4 KitKat (API 19)** (con o sin privilegios **Root**) como monitores dedicados de vídeo de seguridad RTSP usando VLC (`org.videolan.vlc`).

> 💡 **Compatibilidad de VLC:** Probado y optimizado específicamente para **VLC versión 2.0.6** en Android 4.4 KitKat, aunque el flujo de Intents es compatible con versiones posteriores de VLC que soporten KitKat.

---

## 🚀 Características Principales

1. **Panel de Control con Parámetros Dinámicos:**
   - Configuración de la **URL RTSP** objetivo.
   - Ajuste de **Caching de red (ms)**.
   - Conmutador para **Forzar RTSP sobre TCP**.
   - Ajuste de paquete y actividad del reproductor objetivo (`org.videolan.vlc`).
   - Almacenamiento persistente en base de datos local (`SharedPreferences`).

2. **Servicio Guardián (Watchdog Service):**
   - Bucle de monitoreo continuo en segundo plano (cada 7 segundos).
   - Comprueba la salud del proceso de VLC. Si detecta caída de red o cierre del reproductor, **lo reactiva automáticamente**.

3. **Notificación Persistente e Interactiva:**
   - Mantiene un servicio en primer plano (`Foreground Service`) en Android 4.4.
   - Incluye **botones de acción directa en la notificación**:
     - ⏯ **Pausar/Activar Guardián**: Alterna el monitoreo sin apagar el servicio.
     - ✖ **Apagar Servicio**: Detiene el servicio y libera memoria del sistema.

4. **Auto-Inicio al Encender (Boot Receiver):**
   - Escucha `android.intent.action.BOOT_COMPLETED`.
   - Al encender la tablet, la app se inicia automáticamente en segundo plano y abre la cámara si el switch de *Autostart* está activo.

5. **Soporte de Privilegios Root (`su 0`):**
   - Consulta la tabla de procesos del kernel Linux (`su 0 ps`) para evadir aislamiento de procesos.
   - Relanza el Intent con privilegios elevados de Superusuario en caso de fallos.

6. **Widget de Escritorio 1x1 (AppWidget):**
   - Icono de acceso rápido de 1 toque para el lanzador de Android 4.4.

---

## 🛠️ Arquitectura de Código

Construido en Java puro nativo para API 19 (Android 4.4):

* **[`MainActivity.java`](app/src/main/java/com/digitalservices/cooau/MainActivity.java):** Panel interactivo con localización multi-idioma (Español / Inglés).
* **[`CameraWatchdogService.java`](app/src/main/java/com/digitalservices/cooau/CameraWatchdogService.java):** Servicio principal en segundo plano con notificación interactiva.
* **[`IntentHelper.java`](app/src/main/java/com/digitalservices/cooau/IntentHelper.java):** Constructor de Intents RTSP dinámicos.
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
