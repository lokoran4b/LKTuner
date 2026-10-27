package com.lokoran.lktuner.audio;

public class PitchDetector {
    private final int sampleRate;
    private final int bufferSize;
    private final double threshold;

    private final double minFreq = 50.0;
    private final double maxFreq = 1200.0;
    private final int minPeriod;
    private final int maxPeriod;
    private final double[] yinBuffer;

    public PitchDetector(int sampleRate, int bufferSize, double threshold) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.threshold = threshold;

        this.minPeriod = Math.max(2, (int) (sampleRate / maxFreq));
        this.maxPeriod = Math.min(bufferSize / 2, (int) (sampleRate / minFreq));
        this.yinBuffer = new double[bufferSize / 2];
    }

    public PitchResult process(short[] audioBuffer) {
        if (audioBuffer == null || audioBuffer.length < bufferSize) {
            return null;
        }

        // 1. RMS y puerta de ruido
        double sumSquares = 0.0;
        for (int i = 0; i < bufferSize; i++) {
            double sample = audioBuffer[i] / 32768.0;
            sumSquares += sample * sample;
        }
        double rms = Math.sqrt(sumSquares / bufferSize);
        double rmsDb = rms > 0.00001 ? 20.0 * Math.log10(rms) : -100.0;

        // Si el volumen está por debajo de -50 dBFS, consideramos silencio
        if (rmsDb < -50.0) {
            return new PitchResult(-1.0, 0.0, rmsDb);
        }

        // 2. Función de diferencia YIN
        int halfBufferSize = bufferSize / 2;
        for (int tau = minPeriod; tau <= maxPeriod; tau++) {
            double diff = 0.0;
            for (int j = 0; j < halfBufferSize; j++) {
                double delta = (audioBuffer[j] - audioBuffer[j + tau]) / 32768.0;
                diff += delta * delta;
            }
            yinBuffer[tau] = diff;
        }

        // 3. Diferencia acumulativa media normalizada
        yinBuffer[0] = 1.0;
        double runningSum = 0.0;
        for (int tau = 1; tau <= maxPeriod; tau++) {
            runningSum += yinBuffer[tau];
            yinBuffer[tau] = runningSum > 0 ? (yinBuffer[tau] * tau) / runningSum : 1.0;
        }

        // 4. Búsqueda del primer mínimo absoluto bajo el umbral
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

        // Mínimo global si no superó el umbral
        if (bestTau == -1) {
            double minVal = Double.MAX_VALUE;
            for (int tau = minPeriod; tau <= maxPeriod; tau++) {
                if (yinBuffer[tau] < minVal) {
                    minVal = yinBuffer[tau];
                    bestTau = tau;
                }
            }
            if (minVal > 0.40) {
                return new PitchResult(-1.0, 1.0 - minVal, rmsDb);
            }
        }

        // 5. Interpolación parabólica
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
