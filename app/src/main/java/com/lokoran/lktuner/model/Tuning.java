package com.lokoran.lktuner.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tuning {
    private final String id;
    private final String name;
    private final String description;
    private final List<GuitarString> strings;

    public Tuning(String id, String name, String description, List<GuitarString> strings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.strings = strings != null ? strings : Collections.<GuitarString>emptyList();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<GuitarString> getStrings() {
        return strings;
    }

    public GuitarString findClosestString(double frequency, double a4Reference) {
        if (strings.isEmpty() || frequency <= 20.0) {
            return null;
        }
        GuitarString closest = null;
        double minLogDiff = Double.MAX_VALUE;
        for (GuitarString gs : strings) {
            Note currentNote = Note.fromMidi(gs.getNote().getMidiNumber(), a4Reference);
            double logDiff = Math.abs(Math.log(frequency / currentNote.getFrequency()));
            if (logDiff < minLogDiff) {
                minLogDiff = logDiff;
                closest = gs;
            }
        }
        return closest;
    }

    public static List<Tuning> getAllPresets(double a4Reference) {
        List<Tuning> list = new ArrayList<>();

        // 1. Estándar
        list.add(new Tuning(
            "standard",
            "Estándar (E A D G B E)",
            "Afinación clásica estándar",
            Arrays.asList(
                new GuitarString(6, Note.fromMidi(40, a4Reference)), // E2
                new GuitarString(5, Note.fromMidi(45, a4Reference)), // A2
                new GuitarString(4, Note.fromMidi(50, a4Reference)), // D3
                new GuitarString(3, Note.fromMidi(55, a4Reference)), // G3
                new GuitarString(2, Note.fromMidi(59, a4Reference)), // B3
                new GuitarString(1, Note.fromMidi(64, a4Reference))  // E4
            )
        ));

        // 2. Drop D
        list.add(new Tuning(
            "drop_d",
            "Drop D (D A D G B E)",
            "6ª cuerda bajada a Re",
            Arrays.asList(
                new GuitarString(6, Note.fromMidi(38, a4Reference)), // D2
                new GuitarString(5, Note.fromMidi(45, a4Reference)), // A2
                new GuitarString(4, Note.fromMidi(50, a4Reference)), // D3
                new GuitarString(3, Note.fromMidi(55, a4Reference)), // G3
                new GuitarString(2, Note.fromMidi(59, a4Reference)), // B3
                new GuitarString(1, Note.fromMidi(64, a4Reference))  // E4
            )
        ));

        // 3. Medio tono abajo (Eb)
        list.add(new Tuning(
            "half_step_down",
            "Medio tono abajo (Eb Ab Db Gb Bb Eb)",
            "Todas las cuerdas bajadas 1 semitono",
            Arrays.asList(
                new GuitarString(6, Note.fromMidi(39, a4Reference)), // Eb2
                new GuitarString(5, Note.fromMidi(44, a4Reference)), // Ab2
                new GuitarString(4, Note.fromMidi(49, a4Reference)), // Db3
                new GuitarString(3, Note.fromMidi(54, a4Reference)), // Gb3
                new GuitarString(2, Note.fromMidi(58, a4Reference)), // Bb3
                new GuitarString(1, Note.fromMidi(63, a4Reference))  // Eb4
            )
        ));

        // 4. Un tono abajo (D Standard)
        list.add(new Tuning(
            "full_step_down",
            "Un tono abajo (D G C F A D)",
            "Todas las cuerdas bajadas 2 semitonos",
            Arrays.asList(
                new GuitarString(6, Note.fromMidi(38, a4Reference)), // D2
                new GuitarString(5, Note.fromMidi(43, a4Reference)), // G2
                new GuitarString(4, Note.fromMidi(48, a4Reference)), // C3
                new GuitarString(3, Note.fromMidi(53, a4Reference)), // F3
                new GuitarString(2, Note.fromMidi(57, a4Reference)), // A3
                new GuitarString(1, Note.fromMidi(62, a4Reference))  // D4
            )
        ));

        // 5. Drop C
        list.add(new Tuning(
            "drop_c",
            "Drop C (C G C F A D)",
            "D Standard con 6ª cuerda en Do",
            Arrays.asList(
                new GuitarString(6, Note.fromMidi(36, a4Reference)), // C2
                new GuitarString(5, Note.fromMidi(43, a4Reference)), // G2
                new GuitarString(4, Note.fromMidi(48, a4Reference)), // C3
                new GuitarString(3, Note.fromMidi(53, a4Reference)), // F3
                new GuitarString(2, Note.fromMidi(57, a4Reference)), // A3
                new GuitarString(1, Note.fromMidi(62, a4Reference))  // D4
            )
        ));

        // 6. DADGAD
        list.add(new Tuning(
            "dadgad",
            "DADGAD (D A D G A D)",
            "Afinación modal celta y folk",
            Arrays.asList(
                new GuitarString(6, Note.fromMidi(38, a4Reference)), // D2
                new GuitarString(5, Note.fromMidi(45, a4Reference)), // A2
                new GuitarString(4, Note.fromMidi(50, a4Reference)), // D3
                new GuitarString(3, Note.fromMidi(55, a4Reference)), // G3
                new GuitarString(2, Note.fromMidi(57, a4Reference)), // A3
                new GuitarString(1, Note.fromMidi(62, a4Reference))  // D4
            )
        ));

        // 7. Open D
        list.add(new Tuning(
            "open_d",
            "Open D (D A D F# A D)",
            "Acorde de Re mayor al aire",
            Arrays.asList(
                new GuitarString(6, Note.fromMidi(38, a4Reference)), // D2
                new GuitarString(5, Note.fromMidi(45, a4Reference)), // A2
                new GuitarString(4, Note.fromMidi(50, a4Reference)), // D3
                new GuitarString(3, Note.fromMidi(54, a4Reference)), // F#3
                new GuitarString(2, Note.fromMidi(57, a4Reference)), // A3
                new GuitarString(1, Note.fromMidi(62, a4Reference))  // D4
            )
        ));

        // 8. Open G
        list.add(new Tuning(
            "open_g",
            "Open G (D G D G B D)",
            "Acorde de Sol mayor al aire",
            Arrays.asList(
                new GuitarString(6, Note.fromMidi(38, a4Reference)), // D2
                new GuitarString(5, Note.fromMidi(43, a4Reference)), // G2
                new GuitarString(4, Note.fromMidi(50, a4Reference)), // D3
                new GuitarString(3, Note.fromMidi(55, a4Reference)), // G3
                new GuitarString(2, Note.fromMidi(59, a4Reference)), // B3
                new GuitarString(1, Note.fromMidi(62, a4Reference))  // D4
            )
        ));

        // 9. Cromático
        list.add(new Tuning(
            "chromatic",
            "Cromático (Automático)",
            "Detección de cualquier nota libre",
            Collections.<GuitarString>emptyList()
        ));

        return list;
    }
}
