# LKTuner 🎸

[![Download APK](https://img.shields.io/badge/Descargar-LKTuner.apk-00E676?style=for-the-badge&logo=android&logoColor=white)](https://github.com/lokoran4b/LKTuner/releases/latest/download/LKTuner.apk)
[![Build and Release](https://github.com/lokoran4b/LKTuner/actions/workflows/build-release.yml/badge.svg)](https://github.com/lokoran4b/LKTuner/actions)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android_8.0+-green.svg)](https://developer.android.com)

Un afinador de guitarra de alta precisión, ultra ligero y reactivo para Android, diseñado con un motor acústico YIN y **100% libre de anuncios, sin rastreadores y con todas las afinaciones desbloqueadas permanentemente**.

---

## 📥 Enlace Directo de Descarga

Puedes descargar el archivo APK compilado automáticamente desde la última versión de GitHub:

👉 **[Descargar LKTuner.apk (Última Versión)](https://github.com/lokoran4b/LKTuner/releases/latest/download/LKTuner.apk)**

---

## ⚡ Características Principales

1. **Motor de Audio DSP (YIN + Filtro Pasa-Bajos):**
   - **Filtro Pasa-Bajos de 2 etapas (12 dB/octava a ~900 Hz):** Elimina ruidos de púas, armónicos estridentes y chirridos de traste para aislar la frecuencia fundamental.
   - **Algoritmo YIN con interpolación parabólica:** Precisión sub-centésima en la lectura de tono.
   - **Estabilizador y filtro de mediana (5 muestras):** Elimina oscilaciones erráticas y saltos de octava.
   - **Puerta de ruido integrada (-48 dBFS):** Evita falsas lecturas con el ruido de la sala.
   - **Tiempo de retención (*Hold Time*):** Mantiene la lectura estable mientras decae la vibración de la cuerda.

2. **Afinaciones de Guitarra Incluidas:**
   - **Estándar:** E2, A2, D3, G3, B3, E4
   - **Drop D:** D2, A2, D3, G3, B3, E4
   - **Medio tono abajo (Eb):** Eb2, Ab2, Db3, Gb3, Bb3, Eb4
   - **Un tono abajo (D Standard):** D2, G2, C3, F3, A3, D4
   - **Drop C:** C2, G2, C3, F3, A3, D4
   - **DADGAD:** Afinación modal celta/folk
   - **Open D / Open G:** Afinaciones abiertas para slide/blues
   - **Cromático Libre (Auto):** Detección continua de cualquier nota musical en la escala temperada.

3. **Interfaz Visual Optimizada:**
   - Aguja con física de amortiguación continua a **60/120 Hz**.
   - Resplandor verde de afinación con **histéresis** (±3 cents de entrada, ±4.5 cents de salida) para evitar parpadeos.
   - Selector manual de cuerda o detección inteligente automática.
   - Calibrador de tono A4 (440 Hz por defecto, configurable de 420 a 460 Hz).

4. **100% Privado y Autónomo:**
   - Cero permisos de red (`INTERNET` no requerido).
   - Sin servicios en segundo plano ni telemetría.
   - Peso final de solo ~29 KB.

---

## 🛠️ Compilación Local

Si deseas compilar el APK manualmente en tu entorno local:

```bash
chmod +x ./build.sh
./build.sh
```

El script detecta automáticamente el Android SDK (`$ANDROID_HOME` o `$ANDROID_SDK_ROOT`), compila recursos con `aapt2`, genera bytecode con `javac`, convierte a DEX con `d8`, alinea con `zipalign` y firma con `apksigner`.
