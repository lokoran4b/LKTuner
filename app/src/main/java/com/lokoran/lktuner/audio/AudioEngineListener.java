package com.lokoran.lktuner.audio;

public interface AudioEngineListener {
    void onPitchResult(PitchResult result);
    void onAudioError(String message);
}
