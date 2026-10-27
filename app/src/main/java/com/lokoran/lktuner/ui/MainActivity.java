package com.lokoran.lktuner.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.lokoran.lktuner.R;
import com.lokoran.lktuner.audio.AudioEngine;
import com.lokoran.lktuner.audio.AudioEngineListener;
import com.lokoran.lktuner.audio.PitchResult;
import com.lokoran.lktuner.model.GuitarString;
import com.lokoran.lktuner.model.Note;
import com.lokoran.lktuner.model.NoteInfo;
import com.lokoran.lktuner.model.Tuning;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AudioEngineListener, AdapterView.OnItemSelectedListener, View.OnClickListener {
    private TunerView tunerView;
    private Spinner spinnerTunings;
    private TextView tvCalibrateFreq;
    private Button btnCalibrateDown;
    private Button btnCalibrateUp;
    private TextView tvModeStatus;
    private Button btnAutoMode;
    private LinearLayout layoutStrings;

    private AudioEngine audioEngine;
    private double a4Reference = 440.0;
    private List<Tuning> allTunings = new ArrayList<>();
    private Tuning currentTuning;
    private GuitarString lockedString = null;
    private final List<Button> stringButtons = new ArrayList<>();

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        tunerView = findViewById(R.id.tunerView);
        spinnerTunings = findViewById(R.id.spinnerTunings);
        tvCalibrateFreq = findViewById(R.id.tvCalibrateFreq);
        btnCalibrateDown = findViewById(R.id.btnCalibrateDown);
        btnCalibrateUp = findViewById(R.id.btnCalibrateUp);
        tvModeStatus = findViewById(R.id.tvModeStatus);
        btnAutoMode = findViewById(R.id.btnAutoMode);
        layoutStrings = findViewById(R.id.layoutStrings);

        setupTunings();
        setupCalibration();
        setupModeControls();

        audioEngine = new AudioEngine(this);
        checkPermissionAndStart();
    }

    private void setupTunings() {
        allTunings = Tuning.getAllPresets(a4Reference);
        currentTuning = allTunings.get(0);

        List<String> names = new ArrayList<>();
        for (Tuning t : allTunings) {
            names.add(t.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinnerTunings.setAdapter(adapter);
        spinnerTunings.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currentTuning = allTunings.get(position);
        lockedString = null;
        updateModeStatus();
        buildStringButtons();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    private void setupCalibration() {
        updateCalibrationText();
        btnCalibrateDown.setOnClickListener(this);
        btnCalibrateUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnCalibrateDown) {
            if (a4Reference > 420.0) {
                a4Reference -= 1.0;
                onCalibrationChanged();
            }
        } else if (id == R.id.btnCalibrateUp) {
            if (a4Reference < 460.0) {
                a4Reference += 1.0;
                onCalibrationChanged();
            }
        } else if (id == R.id.btnAutoMode) {
            lockedString = null;
            updateModeStatus();
            highlightActiveString(null);
        } else if (v.getTag() instanceof GuitarString) {
            GuitarString gs = (GuitarString) v.getTag();
            if (lockedString == gs) {
                lockedString = null;
            } else {
                lockedString = gs;
            }
            updateModeStatus();
            highlightActiveString(lockedString);
        }
    }

    private void onCalibrationChanged() {
        updateCalibrationText();
        int currentPos = spinnerTunings.getSelectedItemPosition();
        allTunings = Tuning.getAllPresets(a4Reference);
        currentTuning = allTunings.get(Math.max(0, currentPos));
        buildStringButtons();
    }

    private void updateCalibrationText() {
        tvCalibrateFreq.setText("A4: " + Math.round(a4Reference) + " Hz");
    }

    private void setupModeControls() {
        btnAutoMode.setOnClickListener(this);
    }

    private void updateModeStatus() {
        if (lockedString != null) {
            tvModeStatus.setText("Modo: Bloqueado en " + lockedString.getDisplayName());
            tvModeStatus.setTextColor(Color.parseColor("#FFD54F"));
            btnAutoMode.setVisibility(View.VISIBLE);
        } else {
            tvModeStatus.setText("Modo: Detección Automática");
            tvModeStatus.setTextColor(Color.parseColor("#B0BEC5"));
            btnAutoMode.setVisibility(View.GONE);
        }
    }

    private void buildStringButtons() {
        layoutStrings.removeAllViews();
        stringButtons.clear();

        List<GuitarString> strings = currentTuning.getStrings();
        if (strings.isEmpty()) {
            TextView tvChromatic = new TextView(this);
            tvChromatic.setText("Modo Cromático Libre Activo");
            tvChromatic.setTextColor(Color.parseColor("#9E9E9E"));
            tvChromatic.setTextSize(13f);
            layoutStrings.addView(tvChromatic);
            return;
        }

        for (GuitarString gs : strings) {
            Button btn = new Button(this);
            btn.setText(gs.getStringNumber() + "\n" + gs.getNote().getName());
            btn.setTextSize(11f);
            btn.setTextColor(Color.WHITE);
            btn.setBackgroundColor(Color.parseColor("#37474F"));
            btn.setTag(gs);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            params.setMargins(4, 0, 4, 0);
            btn.setLayoutParams(params);

            btn.setOnClickListener(this);

            stringButtons.add(btn);
            layoutStrings.addView(btn);
        }
    }

    private void highlightActiveString(GuitarString activeString) {
        List<GuitarString> strings = currentTuning.getStrings();
        for (int i = 0; i < strings.size(); i++) {
            GuitarString gs = strings.get(i);
            if (i < stringButtons.size()) {
                Button btn = stringButtons.get(i);
                if (gs == activeString) {
                    btn.setBackgroundColor(Color.parseColor("#00E676"));
                    btn.setTextColor(Color.BLACK);
                } else {
                    btn.setBackgroundColor(Color.parseColor("#37474F"));
                    btn.setTextColor(Color.WHITE);
                }
            }
        }
    }

    @Override
    public void onPitchResult(PitchResult result) {
        if (result.getFrequency() <= 0) {
            tunerView.setTuningState("--", "", 0.0, 0.0, 0.0, false);
            if (lockedString == null) {
                highlightActiveString(null);
            }
            return;
        }

        GuitarString targetString = lockedString != null ? lockedString : currentTuning.findClosestString(result.getFrequency(), a4Reference);
        boolean useGuitarString = false;

        if (targetString != null && !currentTuning.getStrings().isEmpty()) {
            Note targetNote = Note.fromMidi(targetString.getNote().getMidiNumber(), a4Reference);
            double cents = 1200.0 * (Math.log(result.getFrequency() / targetNote.getFrequency()) / Math.log(2.0));

            // Si está bloqueada manualmente o está dentro del rango de captura de la cuerda (±3 semitonos)
            if (lockedString != null || Math.abs(cents) <= 300.0) {
                useGuitarString = true;
                tunerView.setTuningState(
                    targetNote.getName(),
                    String.valueOf(targetNote.getOctave()),
                    targetNote.getFrequency(),
                    result.getFrequency(),
                    cents,
                    true
                );
                highlightActiveString(targetString);
            }
        }

        if (!useGuitarString) {
            NoteInfo noteInfo = Note.fromFrequency(result.getFrequency(), a4Reference);
            if (noteInfo != null) {
                tunerView.setTuningState(
                    noteInfo.getNote().getName(),
                    String.valueOf(noteInfo.getNote().getOctave()),
                    noteInfo.getNote().getFrequency(),
                    result.getFrequency(),
                    noteInfo.getCentsDeviation(),
                    true
                );
                if (lockedString == null) {
                    highlightActiveString(null);
                }
            }
        }
    }

    @Override
    public void onAudioError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void checkPermissionAndStart() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            audioEngine.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            audioEngine.start();
        } else {
            Toast.makeText(this, "Permiso de micrófono requerido para afinar", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            audioEngine.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        audioEngine.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioEngine.stop();
    }
}
