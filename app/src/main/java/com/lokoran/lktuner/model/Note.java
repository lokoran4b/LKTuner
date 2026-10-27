package com.lokoran.lktuner.model;

public class Note {
    private static final String[] NOTE_NAMES = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    private final String name;
    private final int octave;
    private final int midiNumber;
    private final double frequency;

    public Note(String name, int octave, int midiNumber, double frequency) {
        this.name = name;
        this.octave = octave;
        this.midiNumber = midiNumber;
        this.frequency = frequency;
    }

    public String getName() {
        return name;
    }

    public int getOctave() {
        return octave;
    }

    public int getMidiNumber() {
        return midiNumber;
    }

    public double getFrequency() {
        return frequency;
    }

    public String getFullDisplayName() {
        return name + octave;
    }

    public static Note fromMidi(int midiNumber, double a4Reference) {
        int noteIndex = ((midiNumber % 12) + 12) % 12;
        int octave = (midiNumber / 12) - 1;
        String noteName = NOTE_NAMES[noteIndex];
        double freq = a4Reference * Math.pow(2.0, (midiNumber - 69) / 12.0);
        return new Note(noteName, octave, midiNumber, freq);
    }

    public static NoteInfo fromFrequency(double frequency, double a4Reference) {
        if (frequency <= 20.0 || frequency >= 4000.0) {
            return null;
        }

        double midiNumberDouble = 69.0 + 12.0 * (Math.log(frequency / a4Reference) / Math.log(2.0));
        int nearestMidi = (int) Math.round(midiNumberDouble);
        double targetFrequency = a4Reference * Math.pow(2.0, (nearestMidi - 69) / 12.0);
        double cents = 1200.0 * (Math.log(frequency / targetFrequency) / Math.log(2.0));

        Note note = fromMidi(nearestMidi, a4Reference);
        return new NoteInfo(note, cents, frequency);
    }
}
