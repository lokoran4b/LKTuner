package com.lokoran.lktuner.model;

public class GuitarString {
    private final int stringNumber; // 1 es E4 (aguda), 6 es E2 (grave)
    private final Note note;

    public GuitarString(int stringNumber, Note note) {
        this.stringNumber = stringNumber;
        this.note = note;
    }

    public int getStringNumber() {
        return stringNumber;
    }

    public Note getNote() {
        return note;
    }

    public String getDisplayName() {
        return stringNumber + "ª " + note.getFullDisplayName();
    }
}
