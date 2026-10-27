package com.lokoran.lktuner.audio;

public class PitchResult {
    private final double frequency;
    private final double probability;
    private final double rmsDb;

    public PitchResult(double frequency, double probability, double rmsDb) {
        this.frequency = frequency;
        this.probability = probability;
        this.rmsDb = rmsDb;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getProbability() {
        return probability;
    }

    public double getRmsDb() {
        return rmsDb;
    }
}
