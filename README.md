# LKTuner 🎸

Un afinador de guitarra limpio, rápido y de alta precisión para Android, inspirado en lo mejor del motor DSP de DaTuner pero **100% libre de publicidad, sin rastreadores y sin compras integradas (experiencia PRO nativa)**.

---

## Características Principales

1. **Motor de Detección de Tono (DSP YIN):**
   - Implementación pura y optimizada del algoritmo **YIN** con interpolación parabólica para precisión a nivel de sub-centésimas de semitono.
   - Puerta de ruido (*Noise Gate*) integrada a -50 dBFS para ignorar el ruido ambiental.
   - Rango espectral ajustado para frecuencias fundamentales de guitarra (50 Hz – 1200 Hz).

2. **Afinaciones de Guitarra Incluidas:**
   - **Estándar:** E2, A2, D3, G3, B3, E4
   - **Drop D:** D2, A2, D3, G3, B3, E4
   - **Medio tono abajo (Eb / Half-Step Down):** Eb2, Ab2, Db3, Gb3, Bb3, Eb4
   - **Un tono abajo (D Standard):** D2, G2, C3, F3, A3, D4
   - **Drop C:** C2, G2, C3, F3, A3, D4
   - **DADGAD:** Afinación modal celta/folk
   - **Open D:** Acorde de Re mayor al aire
   - **Open G:** Acorde de Sol mayor al aire
   - **Open E:** Acorde de Mi mayor al aire
   - **Open A:** Acorde de La mayor al aire
   - **Cromático Libre (Auto):** Detecta automáticamente cualquier nota musical.

3. **Interfaz Visual Dinámica:**
   - Aguja de desviación (-50 a +50 cents) con amortiguación suave.
   - Indicador de resplandor verde (*In-Tune Lock*) cuando la nota está afinada dentro de ±3 cents.
   - Selector manual de cuerda (*String Lock*) o modo automático.
   - Calibrador de tono de referencia A4 (por defecto 440 Hz, ajustable de 420 Hz a 460 Hz con pasos de 1 Hz).

4. **Privacidad y Rendimiento:**
   - Cero dependencias de publicidad (sin AdMob, sin AppLovin, sin Vungle).
   - Cero compras dentro de la app (todas las afinaciones desbloqueadas permanentemente).
   - Sin permisos de red (`INTERNET` no requerido).

---

## Estructura del Proyecto

* [`app/src/main/java/com/lokoran/lktuner/audio/`](app/src/main/java/com/lokoran/lktuner/audio/)
  * `PitchDetector.kt`: Algoritmo YIN con interpolación parabólica y cálculo RMS.
  * `AudioEngine.kt`: Gestión de `AudioRecord` en hilo prioritario de audio.
* [`app/src/main/java/com/lokoran/lktuner/model/`](app/src/main/java/com/lokoran/lktuner/model/)
  * `Note.kt`: Fórmulas acústicas de frecuencia, octavas, números MIDI y cents.
  * `Tuning.kt`: Catálogo de afinaciones y selector de cuerda más cercana.
* [`app/src/main/java/com/lokoran/lktuner/ui/`](app/src/main/java/com/lokoran/lktuner/ui/)
  * `TunerView.kt`: Canvas gráfico con aguja, graduación y efecto de afinación en verde.
  * `MainActivity.kt`: Control de ciclo de vida, selector de cuerdas y calibración.
