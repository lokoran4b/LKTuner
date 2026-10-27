package com.lokoran.lktuner.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioEngine implements Runnable {
    private final AudioEngineListener listener;
    private final int sampleRate = 44100;
    private final int bufferSize = 4096;
    private final int hopSize = 2048; // 50% de solapamiento para mayor fluidez de análisis

    private final PitchDetector pitchDetector;
    private final PitchStabilizer pitchStabilizer;
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private Thread recordThread;
    private AudioRecord audioRecord;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AudioEngine(AudioEngineListener listener) {
        this.listener = listener;
        this.pitchDetector = new PitchDetector(sampleRate, bufferSize, 0.15);
        this.pitchStabilizer = new PitchStabilizer();
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
        short[] audioBuffer = new short[bufferSize];
        short[] hopBuffer = new short[hopSize];

        // Llenar el buffer inicial
        int initialRead = 0;
        while (initialRead < bufferSize && isRecording.get()) {
            AudioRecord ar = audioRecord;
            if (ar == null) break;
            int r = ar.read(audioBuffer, initialRead, bufferSize - initialRead);
            if (r <= 0) break;
            initialRead += r;
        }

        while (isRecording.get()) {
            // Leer bloque de avance (hopSize)
            int readTotal = 0;
            while (readTotal < hopSize && isRecording.get()) {
                AudioRecord ar = audioRecord;
                if (ar == null) break;
                int read = ar.read(hopBuffer, readTotal, hopSize - readTotal);
                if (read <= 0) break;
                readTotal += read;
            }

            if (readTotal == hopSize && isRecording.get()) {
                // Desplazar muestras anteriores hacia la izquierda
                System.arraycopy(audioBuffer, hopSize, audioBuffer, 0, bufferSize - hopSize);
                // Copiar las nuevas muestras al final
                System.arraycopy(hopBuffer, 0, audioBuffer, bufferSize - hopSize, hopSize);

                // Procesar detección y estabilización
                PitchResult rawResult = pitchDetector.process(audioBuffer);
                PitchResult stableResult = pitchStabilizer.process(rawResult);

                if (stableResult != null) {
                    mainHandler.post(new ResultPoster(listener, stableResult));
                }
            }
        }
    }

    public void start() {
        if (isRecording.get()) {
            return;
        }

        pitchStabilizer.reset();

        int minInternalBufSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        );

        if (minInternalBufSize <= 0) {
            listener.onAudioError("Configuración de audio no soportada");
            return;
        }

        try {
            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                Math.max(minInternalBufSize, bufferSize * 2)
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                listener.onAudioError("No se pudo inicializar la grabación");
                audioRecord.release();
                audioRecord = null;
                return;
            }

            audioRecord.startRecording();
            isRecording.set(true);

            recordThread = new Thread(this, "LKTuner-AudioThread");
            recordThread.start();
        } catch (SecurityException se) {
            listener.onAudioError("Permiso de micrófono no otorgado");
            stop();
        } catch (Exception e) {
            listener.onAudioError("Error al iniciar audio: " + e.getMessage());
            stop();
        }
    }

    public void stop() {
        isRecording.set(false);
        if (recordThread != null) {
            try {
                recordThread.join(400);
            } catch (InterruptedException ignored) {}
            recordThread = null;
        }

        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception ignored) {}
            audioRecord = null;
        }

        pitchStabilizer.reset();
    }
}
