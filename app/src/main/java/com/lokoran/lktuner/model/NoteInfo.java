package com.lokoran.lktuner.model;

public class NoteInfo {
    private final Note note;
    private final double centsDeviation;
    private final double detectedFrequency;

    public NoteInfo(Note note, double centsDeviation, double detectedFrequency) {
        this.note = note;
        this.centsDeviation = centsDeviation;
        this.detectedFrequency = detectedFrequency;
    }

    public Note getNote() {
        return note;
    }

    public double getCentsDeviation() {
        return centsDeviation;
    }

    public double getDetectedFrequency() {
        return detectedFrequency;
    }

    public boolean isInTune() {
        return Math.abs(centsDeviation) <= 3.0;
    }
}
