# Cámara Exterior - Guardián RTSP (Android 4.4 KitKat)

**Cámara Exterior** es una aplicación y servicio guardián (*Watchdog*) diseñado para dispositivos y tablets dedicadas con **Android 4.4 KitKat (API 19)** con o sin acceso **Root**. Su propósito principal es mantener activa una transmisión de vídeo de cámara de seguridad vía **RTSP** en el reproductor VLC (`org.videolan.vlc`), relanzando automáticamente la transmisión si la aplicación cae, es cerrada o si el dispositivo se reinicia.

---

## 🚀 Características Principales

1. **Panel de Control con Parámetros Dinámicos:**
   - Permite configurar la **URL RTSP** (ej: `rtsp://192.168.1.100:8554/live`).
   - Permite ajustar el **Caching de red (ms)** (ej: `300 ms`).
   - Conmutador para **Forzar RTSP sobre TCP**.
   - Configuración de paquete y actividad del reproductor objetivo (`org.videolan.vlc`).
   - Almacenamiento persistente en base de datos local (`SharedPreferences`).

2. **Servicio Guardián (Watchdog Service):**
   - Servicio en segundo plano con bucle de verificación periódica (cada 7 segundos).
   - Comprueba si el proceso de VLC está en ejecución. Si detecta que VLC fue cerrado o se cayó por desconexión de red, **lo vuelve a levantar automáticamente con el Intent exacto**.

3. **Notificación Persistente e Interactiva:**
   - Mantiene el servicio en primer plano (`Foreground Service`) en la barra de estado de Android 4.4.
   - Incluye **botones de acción integrados en la notificación**:
     - ⏯ **Pausar/Activar Guardián**: Alterna el monitoreo sin cerrar el servicio.
     - ✖ **Apagar Servicio**: Detiene el servicio y quita la notificación de la barra.

4. **Auto-Inicio al Encender (Boot Receiver):**
   - Escucha la señal del sistema `android.intent.action.BOOT_COMPLETED`.
   - Al encender la tablet, la app se inicia automáticamente en segundo plano y abre la cámara si el switch de *Autostart* está activado.

5. **Integración con Privilegios Root (`su 0`):**
   - Opción conmutable *"Utilizar privilegios ROOT"*.
   - Ejecuta consultas directas al listado de procesos del núcleo de Linux mediante `su 0 ps` para evadir restricciones de aislamiento de procesos de Android.
   - Permite relanzar el Intent a nivel Root en caso de fallos de permisos.

6. **Widget de Escritorio 1x1 (AppWidget):**
   - Icono directo para la pantalla de inicio del lanzador de Android 4.4.
   - Permite abrir la transmisión RTSP de 1 solo toque.

---

## 🛠️ Arquitectura de Código

El proyecto está construido en Java puro con compatibilidad nativa para API 19 (Android 4.4):

* **[`MainActivity.java`](app/src/main/java/com/digitalservices/cooau/MainActivity.java):** Interfaz gráfica de usuario y panel de configuración. Verifica dinámicamente el estado real del servicio guardián en el sistema operativo.
* **[`CameraWatchdogService.java`](app/src/main/java/com/digitalservices/cooau/CameraWatchdogService.java):** Servicio principal en segundo plano con notificación persistente y acciones interactivas.
* **[`IntentHelper.java`](app/src/main/java/com/digitalservices/cooau/IntentHelper.java):** Clase de utilidad que construye los Intents dinámicos para VLC combinando la URL RTSP y parámetros guardados.
* **[`RootShell.java`](app/src/main/java/com/digitalservices/cooau/RootShell.java):** Ejecutor de shell interactivo con superusuario (`su 0`).
* **[`BootReceiver.java`](app/src/main/java/com/digitalservices/cooau/BootReceiver.java):** Receptor Broadcast para el inicio automático del sistema.
* **[`CameraWidgetProvider.java`](app/src/main/java/com/digitalservices/cooau/CameraWidgetProvider.java):** Proveedor del widget para el escritorio de Android.

---

## ⚙️ ¿Cómo funciona la opción "Utilizar privilegios Root (su 0)"?

En Android 4.4 (KitKat), las aplicaciones estándar solo tienen visibilidad limitada sobre los procesos que pertenecen a su propio UID por motivos de seguridad. 

Cuando la opción **"Utilizar privilegios ROOT"** está activada:
1. El servicio invoca el ejecutable `su` (SuperSU / Magisk / BusyBox) en la consola root (`UID 0`).
2. Ejecuta el comando `ps` para inspeccionar la tabla de procesos del núcleo del sistema operativo.
3. Si el nombre del proceso de VLC (`org.videolan.vlc`) no aparece en la tabla general de Linux, el Guardián ejecuta un comando `am start` como usuario Root (`su 0`):
   ```bash
   su 0 am start -a android.intent.action.VIEW \
                -n org.videolan.vlc/org.videolan.vlc.gui.video.VideoPlayerActivity \
                -d "rtsp://192.168.1.100:8554/live" \
                --ez "rtsp_tcp" true \
                --ei "network_caching" 300 \
                -f 0x10000000
   ```
Esto garantiza que la cámara se vuelva a abrir incluso si VLC quedó bloqueado o en un estado de error.

---

## 📦 Compilación

### Compilar el APK con Gradle / Android Studio:
```bash
./gradlew assembleDebug --no-daemon
```
El instalador APK se generará en: `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 Licencia y Autores
Desarrollado para **Digital Services** (`@ds.net.gt`).
Desarrollador: **Luis Alvarez**.
