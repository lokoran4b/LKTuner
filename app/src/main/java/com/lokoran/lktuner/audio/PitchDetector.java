package com.lokoran.lktuner.audio;

public class PitchDetector {
    private final int sampleRate;
    private final int bufferSize;
    private final double threshold;

    private final double minFreq = 50.0;
    private final double maxFreq = 1100.0;
    private final int minPeriod;
    private final int maxPeriod;
    private final double[] yinBuffer;
    private final double[] filteredBuffer;

    // Coeficiente de filtro pasa-bajos a ~900 Hz para guitarra
    private final double lpfAlpha = 0.12;

    public PitchDetector(int sampleRate, int bufferSize, double threshold) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.threshold = threshold;

        this.minPeriod = Math.max(2, (int) (sampleRate / maxFreq));
        this.maxPeriod = Math.min(bufferSize / 2, (int) (sampleRate / minFreq));
        this.yinBuffer = new double[bufferSize / 2];
        this.filteredBuffer = new double[bufferSize];
    }

    public PitchResult process(short[] audioBuffer) {
        if (audioBuffer == null || audioBuffer.length < bufferSize) {
            return null;
        }

        // 1. RMS y puerta de ruido (Noise Gate a -48 dBFS)
        double sumSquares = 0.0;
        for (int i = 0; i < bufferSize; i++) {
            double sample = audioBuffer[i] / 32768.0;
            sumSquares += sample * sample;
        }
        double rms = Math.sqrt(sumSquares / bufferSize);
        double rmsDb = rms > 0.00001 ? 20.0 * Math.log10(rms) : -100.0;

        if (rmsDb < -48.0) {
            return new PitchResult(-1.0, 0.0, rmsDb);
        }

        // 2. Filtro pasa-bajos de 2 etapas (12 dB/octava) para atenuar armónicos altos y ruido de trastes
        double stage1 = 0.0;
        double stage2 = 0.0;
        for (int i = 0; i < bufferSize; i++) {
            double rawSample = audioBuffer[i] / 32768.0;
            stage1 += lpfAlpha * (rawSample - stage1);
            stage2 += lpfAlpha * (stage1 - stage2);
            filteredBuffer[i] = stage2;
        }

        // 3. Función de diferencia YIN sobre señal filtrada
        int halfBufferSize = bufferSize / 2;
        for (int tau = minPeriod; tau <= maxPeriod; tau++) {
            double diff = 0.0;
            for (int j = 0; j < halfBufferSize; j++) {
                double delta = filteredBuffer[j] - filteredBuffer[j + tau];
                diff += delta * delta;
            }
            yinBuffer[tau] = diff;
        }

        // 4. Diferencia acumulativa media normalizada
        yinBuffer[0] = 1.0;
        double runningSum = 0.0;
        for (int tau = 1; tau <= maxPeriod; tau++) {
            runningSum += yinBuffer[tau];
            yinBuffer[tau] = runningSum > 0 ? (yinBuffer[tau] * tau) / runningSum : 1.0;
        }

        // 5. Búsqueda del primer valle bajo el umbral
        int bestTau = -1;
        for (int tau = minPeriod; tau <= maxPeriod; tau++) {
            if (yinBuffer[tau] < threshold) {
                while (tau + 1 <= maxPeriod && yinBuffer[tau + 1] < yinBuffer[tau]) {
                    tau++;
                }
                bestTau = tau;
                break;
            }
        }

        // Si no supera el umbral, buscar el mínimo global
        if (bestTau == -1) {
            double minVal = Double.MAX_VALUE;
            for (int tau = minPeriod; tau <= maxPeriod; tau++) {
                if (yinBuffer[tau] < minVal) {
                    minVal = yinBuffer[tau];
                    bestTau = tau;
                }
            }
            if (minVal > 0.35) {
                return new PitchResult(-1.0, 1.0 - minVal, rmsDb);
            }
        }

        // 6. Interpolación parabólica de sub-muestra para afinación precisa
        double interpolatedTau = bestTau;
        if (bestTau > minPeriod && bestTau < maxPeriod) {
            double s0 = yinBuffer[bestTau - 1];
            double s1 = yinBuffer[bestTau];
            double s2 = yinBuffer[bestTau + 1];
            double delta = (s2 - s0) / (2.0 * (2.0 * s1 - s2 - s0));
            interpolatedTau = bestTau + delta;
        }

        double detectedFrequency = sampleRate / interpolatedTau;
        double confidence = Math.max(0.0, Math.min(1.0, 1.0 - yinBuffer[bestTau]));

        return new PitchResult(detectedFrequency, confidence, rmsDb);
    }
}
