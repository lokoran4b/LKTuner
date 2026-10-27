package com.lokoran.lktuner.audio;

import java.util.Arrays;

/**
 * Filtro de mediana y estabilizador temporal para eliminar oscilaciones,
 * saltos de octava y parpadeos en la afinación de instrumentos.
 */
public class PitchStabilizer {
    private static final int WINDOW_SIZE = 5;
    private final double[] pitchHistory = new double[WINDOW_SIZE];
    private int historyCount = 0;
    private int historyIndex = 0;

    private double lastStableFrequency = -1.0;
    private long lastDetectionTime = 0;
    private static final long HOLD_TIME_MS = 380; // Tiempo de persistencia para evitar que parpadee en silencios breves

    public PitchStabilizer() {
        reset();
    }

    public synchronized void reset() {
        historyCount = 0;
        historyIndex = 0;
        lastStableFrequency = -1.0;
        lastDetectionTime = 0;
        Arrays.fill(pitchHistory, 0.0);
    }

    public synchronized PitchResult process(PitchResult raw) {
        long now = System.currentTimeMillis();

        if (raw == null || raw.getFrequency() <= 0) {
            // Si hubo silencio reciente, mantener la última frecuencia estable durante el HOLD_TIME
            if (lastStableFrequency > 0 && (now - lastDetectionTime) < HOLD_TIME_MS) {
                return new PitchResult(lastStableFrequency, 0.5, raw != null ? raw.getRmsDb() : -60.0);
            }
            // Superado el tiempo de retención, confirmar silencio
            lastStableFrequency = -1.0;
            return raw;
        }

        double freq = raw.getFrequency();

        // 1. Corrección de salto de octava común en guitarras (82 Hz <-> 164 Hz)
        if (lastStableFrequency > 0) {
            double ratio = freq / lastStableFrequency;
            // Si detecta exactamente el doble de frecuencia (2º armónico)
            if (ratio >= 1.90 && ratio <= 2.10) {
                freq = freq / 2.0;
            }
            // Si detecta exactamente la mitad de frecuencia por subarmónico
            else if (ratio >= 0.47 && ratio <= 0.53) {
                freq = freq * 2.0;
            }
        }

        // 2. Insertar en ventana deslizante para filtro de mediana
        pitchHistory[historyIndex] = freq;
        historyIndex = (historyIndex + 1) % WINDOW_SIZE;
        if (historyCount < WINDOW_SIZE) {
            historyCount++;
        }

        // 3. Calcular la mediana de las lecturas válidas recientes
        double[] sorted = new double[historyCount];
        System.arraycopy(pitchHistory, 0, sorted, 0, historyCount);
        Arrays.sort(sorted);

        double medianFreq = sorted[historyCount / 2];

        // 4. Si la mediana es consistente, actualizar estado estable
        lastStableFrequency = medianFreq;
        lastDetectionTime = now;

        return new PitchResult(medianFreq, raw.getProbability(), raw.getRmsDb());
    }
}
