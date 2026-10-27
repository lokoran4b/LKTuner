package com.lokoran.lktuner.audio;

public class ResultPoster implements Runnable {
    private final AudioEngineListener listener;
    private final PitchResult result;

    public ResultPoster(AudioEngineListener listener, PitchResult result) {
        this.listener = listener;
        this.result = result;
    }

    @Override
    public void run() {
        if (listener != null) {
            listener.onPitchResult(result);
        }
    }
}
