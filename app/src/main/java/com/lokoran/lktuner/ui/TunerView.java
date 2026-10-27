package com.lokoran.lktuner.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class TunerView extends View {
    private String displayedNote = "--";
    private String displayedOctave = "";
    private double targetFrequency = 0.0;
    private double currentFrequency = 0.0;
    private float targetCents = 0f;
    private float animatedCents = 0f;
    private boolean isNoteActive = false;
    private boolean isInTune = false;

    // Paleta de colores PRO
    private final int colorBgInTune = Color.parseColor("#1B5E20");
    private final int colorAccentGreen = Color.parseColor("#00E676");
    private final int colorAccentOrange = Color.parseColor("#FF9100");
    private final int colorAccentRed = Color.parseColor("#FF5252");
    private final int colorTextPrimary = Color.WHITE;
    private final int colorTextSecondary = Color.parseColor("#9E9E9E");
    private final int colorTicks = Color.parseColor("#616161");

    private final Paint notePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint octavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textInfoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint meterBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint inTuneGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF meterRect = new RectF();

    // Factor de amortiguación para movimiento fluido a 60/120Hz
    private static final float DAMPING_FACTOR = 0.22f;

    public TunerView(Context context) {
        super(context);
        init();
    }

    public TunerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TunerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        notePaint.setColor(colorTextPrimary);
        notePaint.setTextAlign(Paint.Align.CENTER);
        notePaint.setFakeBoldText(true);

        octavePaint.setColor(colorTextSecondary);
        octavePaint.setTextAlign(Paint.Align.LEFT);

        textInfoPaint.setColor(colorTextSecondary);
        textInfoPaint.setTextAlign(Paint.Align.CENTER);

        needlePaint.setStrokeWidth(6f);
        needlePaint.setStyle(Paint.Style.STROKE);

        needleFillPaint.setStyle(Paint.Style.FILL);

        tickPaint.setColor(colorTicks);
        tickPaint.setStrokeWidth(3f);

        meterBackgroundPaint.setStyle(Paint.Style.FILL);
        inTuneGlowPaint.setStyle(Paint.Style.FILL);
    }

    public void setTuningState(
        String note,
        String octave,
        double targetFreq,
        double detectedFreq,
        double cents,
        boolean active
    ) {
        this.displayedNote = active ? note : "--";
        this.displayedOctave = active ? octave : "";
        this.targetFrequency = targetFreq;
        this.currentFrequency = detectedFreq;
        this.targetCents = active ? (float) Math.max(-50.0, Math.min(50.0, cents)) : 0f;
        this.isNoteActive = active;

        // Histéresis de afinación: entra a ±3 cents, sale a ±4.5 cents para evitar parpadeos
        if (active) {
            double absCents = Math.abs(cents);
            if (!isInTune && absCents <= 3.0) {
                isInTune = true;
            } else if (isInTune && absCents > 4.5) {
                isInTune = false;
            }
        } else {
            isInTune = false;
        }

        // Solicitar repintado continuo con animación
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Suavizado exponencial continuo de la aguja
        float centsDiff = targetCents - animatedCents;
        if (Math.abs(centsDiff) > 0.05f) {
            animatedCents += centsDiff * DAMPING_FACTOR;
            postInvalidateOnAnimation();
        } else {
            animatedCents = targetCents;
        }

        // 1. Resplandor verde de afinación
        if (isInTune) {
            inTuneGlowPaint.setColor(colorBgInTune);
            canvas.drawRect(0, 0, w, h, inTuneGlowPaint);
        }

        // 2. Nota principal gigante
        float centerX = w / 2f;
        float centerY = h * 0.40f;

        notePaint.setTextSize(h * 0.22f);
        notePaint.setColor(isInTune ? colorAccentGreen : colorTextPrimary);
        canvas.drawText(displayedNote, centerX, centerY, notePaint);

        // Subíndice de octava al lado de la nota
        if (!displayedOctave.isEmpty()) {
            octavePaint.setTextSize(h * 0.09f);
            octavePaint.setColor(isInTune ? colorAccentGreen : colorTextSecondary);
            float noteWidth = notePaint.measureText(displayedNote);
            canvas.drawText(displayedOctave, centerX + (noteWidth / 2f) + 12f, centerY - (h * 0.08f), octavePaint);
        }

        // 3. Frecuencia y desviación
        textInfoPaint.setTextSize(h * 0.045f);
        if (isNoteActive) {
            String centsStr = (animatedCents >= 0 ? "+" : "") + Math.round(animatedCents) + " ¢";
            String freqStr = String.format("%.1f Hz / %.1f Hz", currentFrequency, targetFrequency);
            textInfoPaint.setColor(isInTune ? colorAccentGreen : colorTextSecondary);
            canvas.drawText(freqStr + "   (" + centsStr + ")", centerX, centerY + (h * 0.09f), textInfoPaint);
        } else {
            textInfoPaint.setColor(colorTextSecondary);
            canvas.drawText("Toca una cuerda para afinar...", centerX, centerY + (h * 0.09f), textInfoPaint);
        }

        // 4. Medidor de cents horizontal
        float meterTop = h * 0.65f;
        float meterBottom = meterTop + (h * 0.12f);
        float meterLeft = w * 0.10f;
        float meterRight = w * 0.90f;
        float meterWidth = meterRight - meterLeft;
        float meterCenter = meterLeft + (meterWidth / 2f);

        meterRect.set(meterLeft, meterTop, meterRight, meterBottom);
        meterBackgroundPaint.setColor(isInTune ? Color.parseColor("#2E7D32") : Color.parseColor("#263238"));
        canvas.drawRoundRect(meterRect, 16f, 16f, meterBackgroundPaint);

        for (int cent = -50; cent <= 50; cent += 10) {
            float tickX = meterCenter + (cent / 50f) * (meterWidth / 2f);
            boolean isCenter = cent == 0;
            float tickHeight = isCenter ? (meterBottom - meterTop) * 0.8f : (meterBottom - meterTop) * 0.4f;
            float tickY1 = meterTop + ((meterBottom - meterTop - tickHeight) / 2f);
            float tickY2 = tickY1 + tickHeight;

            tickPaint.setStrokeWidth(isCenter ? 5f : 2.5f);
            tickPaint.setColor(isCenter ? colorAccentGreen : colorTicks);
            canvas.drawLine(tickX, tickY1, tickX, tickY2, tickPaint);
        }

        // 5. Aguja indicadora
        if (isNoteActive) {
            float needleX = meterCenter + (animatedCents / 50f) * (meterWidth / 2f);
            int needleColor;
            if (isInTune) {
                needleColor = colorAccentGreen;
            } else if (Math.abs(animatedCents) <= 12) {
                needleColor = colorAccentOrange;
            } else {
                needleColor = colorAccentRed;
            }

            needlePaint.setColor(needleColor);
            needlePaint.setStrokeWidth(7f);
            canvas.drawLine(needleX, meterTop - 10f, needleX, meterBottom + 10f, needlePaint);

            Path trianglePath = new Path();
            trianglePath.moveTo(needleX, meterTop - 4f);
            trianglePath.lineTo(needleX - 14f, meterTop - 22f);
            trianglePath.lineTo(needleX + 14f, meterTop - 22f);
            trianglePath.close();

            needleFillPaint.setColor(needleColor);
            canvas.drawPath(trianglePath, needleFillPaint);
        }
    }
}
