package com.tkghyperx.ircontrol;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(7, 7, 11);
    private static final int PANEL = Color.rgb(19, 19, 23);
    private static final int SURFACE2 = Color.rgb(29, 27, 33);
    private static final int BORDER = Color.rgb(54, 49, 60);
    private static final int WHITE = Color.rgb(250, 247, 250);
    private static final int MUTED = Color.rgb(169, 162, 173);
    private static final int PINK = Color.rgb(255, 44, 119);
    private static final int DEEP_PINK = Color.rgb(153, 3, 78);
    private static final int CYAN = PINK;
    private static final int GREEN = Color.rgb(54, 222, 141);
    private static final int AMBER = Color.rgb(255, 187, 103);
    private static final int RED = Color.rgb(158, 36, 67);
    private static final int FRAME_US = 100_000;
    private static final long COOLDOWN_MS = 3000L;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ConsumerIrManager ir;
    private boolean hasEmitter;
    private volatile boolean testing;
    private volatile int duty = 10;
    private volatile int frequency = 38000;
    private volatile int durationSeconds = 15;
    private int sequence = 0;
    private long lastStop = -COOLDOWN_MS;
    private TextView status, stateDot, detail, level, countdown;
    private Button toggle, test;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        ir = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
        try { hasEmitter = ir != null && ir.hasIrEmitter(); }
        catch (RuntimeException e) { hasEmitter = false; }
        buildUi();
        if (hasEmitter) {
            show("LISTO PARA TRANSMITIR", GREEN);
            detail.setText("Hardware detectado mediante ConsumerIrManager.");
        } else {
            show("EMISOR IR NO DISPONIBLE", AMBER);
            detail.setText("El sistema/ROM no publica la interfaz IR. En una ROM personalizada puede faltar el HAL infrarrojo.");
        }
        toggle.setEnabled(hasEmitter);
        test.setEnabled(hasEmitter);
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setOnApplyWindowInsetsListener((v, insets) -> {
            scroll.setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            return insets;
        });

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(20), dp(25), dp(20), dp(32));
        scroll.addView(column, new ScrollView.LayoutParams(-1, -2));
        setContentView(scroll);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        android.widget.ImageView logo = new android.widget.ImageView(this);
        logo.setImageResource(R.drawable.ic_ir_wave_foreground);
        logo.setContentDescription("Icono de onda infrarroja");
        logo.setPadding(dp(4), dp(4), dp(4), dp(4));
        logo.setBackground(background(SURFACE2, BORDER, 15));
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(54), dp(54));
        logoLp.rightMargin = dp(14);
        header.addView(logo, logoLp);

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        TextView heading = text("IR Control Testing", 23, WHITE, true);
        hero.addView(heading);
        add(hero, text("EMISOR INFRARROJO  •  V2.0", 10, MUTED, true), 3);
        header.addView(hero, new LinearLayout.LayoutParams(0, -2, 1));
        column.addView(header);

        LinearLayout stateCard = card();
        add(column, stateCard, 23);
        LinearLayout stateLine = new LinearLayout(this);
        stateLine.setGravity(Gravity.CENTER_VERTICAL);
        stateDot = text("●", 18, GREEN, true);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(-2, -2);
        dotLp.rightMargin = dp(11);
        stateLine.addView(stateDot, dotLp);
        status = text("VERIFICANDO EMISOR", 14, WHITE, true);
        stateLine.addView(status, new LinearLayout.LayoutParams(0, -2, 1));
        stateCard.addView(stateLine);
        detail = text("", 12, MUTED, false);
        add(stateCard, detail, 7);

        LinearLayout powerCard = card();
        add(column, powerCard, 14);
        LinearLayout powerRow = new LinearLayout(this);
        powerRow.setGravity(Gravity.CENTER_VERTICAL);
        powerRow.addView(text("ACTIVIDAD DEL EMISOR", 13, MUTED, true),
                new LinearLayout.LayoutParams(0, -2, 1));
        level = text("10 %", 28, WHITE, true);
        powerRow.addView(level);
        powerCard.addView(powerRow);
        add(powerCard, text("Ciclo de actividad infrarroja", 13, WHITE, false), 7);
        SeekBar seek = new SeekBar(this);
        seek.setMax(15);
        seek.setProgress(5);
        seek.setProgressTintList(ColorStateList.valueOf(PINK));
        seek.setThumbTintList(ColorStateList.valueOf(PINK));
        seek.setProgressBackgroundTintList(ColorStateList.valueOf(BORDER));
        add(powerCard, seek, 11);
        addRangeLabels(powerCard, "5 %", "20 %");
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int p, boolean user) {
                duty = p + 5;
                level.setText(duty + " %");
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
        });
        add(powerCard, text("Regula el tiempo activo de cada ráfaga; no modifica la corriente del LED.", 12, MUTED, false), 13);

        LinearLayout carrierCard = card();
        add(column, carrierCard, 14);
        carrierCard.addView(text("∿  FRECUENCIA", 14, WHITE, true));
        add(carrierCard, text("Portadora del emisor", 12, MUTED, false), 4);
        Spinner carrier = new Spinner(this);
        carrier.setBackground(background(SURFACE2, BORDER, 12));
        carrier.setPadding(dp(12), dp(3), dp(12), dp(3));
        add(carrierCard, carrier, 12);
        ArrayList<Integer> frequencies = new ArrayList<>();
        if (hasEmitter) {
            try {
                ConsumerIrManager.CarrierFrequencyRange[] ranges = ir.getCarrierFrequencies();
                int[] defaults = {30000, 33000, 36000, 38000, 40000, 56000};
                if (ranges != null) for (int hz : defaults) {
                    for (ConsumerIrManager.CarrierFrequencyRange r : ranges) {
                        if (hz >= r.getMinFrequency() && hz <= r.getMaxFrequency()) {
                            frequencies.add(hz);
                            break;
                        }
                    }
                }
            } catch (RuntimeException ignored) {}
        }
        if (frequencies.isEmpty()) frequencies.add(38000);
        String[] labels = new String[frequencies.size()];
        for (int i = 0; i < labels.length; i++)
            labels[i] = String.format(Locale.US, "%.1f kHz", frequencies.get(i) / 1000d);
        carrier.setAdapter(adapter(labels));
        int selected = Math.max(0, frequencies.indexOf(38000));
        carrier.setSelection(selected);
        frequency = frequencies.get(selected);
        carrier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> a, View v, int p, long id) {
                frequency = frequencies.get(p);
            }
            @Override public void onNothingSelected(AdapterView<?> a) {}
        });
        add(carrierCard, text("Opciones compatibles informadas por Android; 38 kHz como respaldo.", 12, MUTED, false), 10);

        LinearLayout timerCard = card();
        add(column, timerCard, 14);
        timerCard.addView(text("◷  TEMPORIZADOR", 14, WHITE, true));
        add(timerCard, text("Apagado automático", 12, MUTED, false), 4);
        Spinner timer = new Spinner(this);
        timer.setBackground(background(SURFACE2, BORDER, 12));
        timer.setPadding(dp(12), dp(3), dp(12), dp(3));
        timer.setAdapter(adapter(new String[]{"5 segundos", "15 segundos", "30 segundos"}));
        timer.setSelection(1);
        timer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> a, View v, int p, long id) {
                durationSeconds = new int[]{5, 15, 30}[p];
            }
            @Override public void onNothingSelected(AdapterView<?> a) {}
        });
        add(timerCard, timer, 12);
        add(timerCard, text("Límite de 30 s, pausa de 3 s entre sesiones y detención al salir.", 12, MUTED, false), 10);

        toggle = new Button(this);
        toggle.setText("TRANSMITIR IR");
        toggle.setTextColor(WHITE);
        toggle.setTextSize(16);
        toggle.setAllCaps(false);
        toggle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        toggle.setBackground(primaryBackground());
        toggle.setElevation(dp(3));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-1, dp(60));
        tlp.topMargin = dp(22);
        column.addView(toggle, tlp);
        toggle.setOnClickListener(v -> {
            if (running.get()) stop("DETENIDO MANUALMENTE");
            else start();
        });

        test = new Button(this);
        test.setText("PRUEBA RÁPIDA  ·  0.1 s");
        test.setAllCaps(false);
        test.setTextSize(14);
        test.setTextColor(WHITE);
        test.setBackground(background(SURFACE2, BORDER, 16));
        LinearLayout.LayoutParams testLp = new LinearLayout.LayoutParams(-1, dp(52));
        testLp.topMargin = dp(11);
        column.addView(test, testLp);
        test.setOnClickListener(v -> quickTest());

        countdown = text("LISTO", 13, MUTED, true);
        countdown.setGravity(Gravity.CENTER);
        add(column, countdown, 15);

        LinearLayout safetyCard = card();
        add(column, safetyCard, 11);
        safetyCard.addView(text("USO SEGURO", 12, PINK, true));
        add(safetyCard, text("No apuntes el emisor a los ojos. La repetición de pulsos puede calentar el teléfono: detén la prueba si notas temperatura inusual. La protección temporal no garantiza seguridad térmica.", 12, MUTED, false), 7);
    }

    private void addRangeLabels(LinearLayout parent, String min, String max) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(text(min, 11, MUTED, false),
                new LinearLayout.LayoutParams(0, -2, 1));
        TextView right = text(max, 11, MUTED, false);
        right.setGravity(Gravity.RIGHT);
        row.addView(right, new LinearLayout.LayoutParams(0, -2, 1));
        parent.addView(row);
    }

    private GradientDrawable primaryBackground() {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{PINK, DEEP_PINK});
        d.setCornerRadius(dp(16));
        return d;
    }

    private void start() {
        if (!hasEmitter || testing || running.get()) return;
        long wait = COOLDOWN_MS - (SystemClock.elapsedRealtime() - lastStop);
        if (wait > 0) {
            show("ESPERA " + ((wait + 999) / 1000) + " s PARA REINICIAR", AMBER);
            return;
        }
        running.set(true);
        final int id = ++sequence;
        final long end = SystemClock.elapsedRealtime() + durationSeconds * 1000L;
        toggle.setText("DETENER EMISIÓN");
        toggle.setTextColor(WHITE);
        toggle.setBackground(background(RED, RED, 16));
        test.setEnabled(false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        show("TRANSMITIENDO IR", CYAN);
        countdown.setText("Tiempo restante: " + durationSeconds + " s");
        Thread worker = new Thread(() -> {
            String reason = "TEMPORIZADOR FINALIZADO";
            int lastSecond = -1;
            try {
                while (running.get() && id == sequence && SystemClock.elapsedRealtime() < end) {
                    long frameStart = SystemClock.elapsedRealtime();
                    int on = FRAME_US * duty / 100;
                    int off = FRAME_US - on;
                    ir.transmit(frequency, new int[]{on, off});
                    long elapsed = SystemClock.elapsedRealtime() - frameStart;
                    if (elapsed < 100) SystemClock.sleep(100 - elapsed);
                    final int seconds = (int) Math.max(0, (end - SystemClock.elapsedRealtime() + 999) / 1000L);
                    if (seconds != lastSecond) {
                        lastSecond = seconds;
                        runOnUiThread(() -> {
                            if (running.get() && sequence == id)
                                countdown.setText("Tiempo restante: " + seconds + " s");
                        });
                    }
                }
            } catch (Exception e) {
                reason = "ERROR DEL CONTROLADOR: " + e.getClass().getSimpleName();
            } finally {
                final String finalReason = reason;
                runOnUiThread(() -> { if (id == sequence) stop(finalReason); });
            }
        }, "ir-transmitter");
        worker.start();
    }

    private void stop(String reason) {
        boolean wasRunning = running.getAndSet(false);
        ++sequence;
        if (wasRunning) lastStop = SystemClock.elapsedRealtime();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        toggle.setText("TRANSMITIR IR");
        toggle.setTextColor(WHITE);
        toggle.setBackground(primaryBackground());
        toggle.setEnabled(hasEmitter && !testing);
        test.setEnabled(hasEmitter && !testing);
        if (wasRunning) {
            show(reason, AMBER);
            countdown.setText("LED IR sin señal");
        }
    }

    private void quickTest() {
        if (!hasEmitter || running.get() || testing) return;
        testing = true;
        toggle.setEnabled(false);
        test.setEnabled(false);
        show("TRANSMITIENDO PRUEBA...", CYAN);
        final int hz = frequency;
        new Thread(() -> {
            String message = "PRUEBA ENVIADA";
            try { ir.transmit(hz, new int[]{10000, 90000}); }
            catch (Exception e) { message = "ERROR: " + e.getClass().getSimpleName(); }
            final String result = message;
            runOnUiThread(() -> {
                testing = false;
                if (!isFinishing() && !isDestroyed()) {
                    toggle.setEnabled(hasEmitter);
                    test.setEnabled(hasEmitter);
                    show(result, result.startsWith("ERROR") ? AMBER : CYAN);
                    countdown.setText("Verifica la emisión IR con una cámara compatible.");
                }
            });
        }, "ir-test").start();
    }

    @Override protected void onPause() {
        if (running.get()) stop("DETENIDO AL SALIR");
        super.onPause();
    }

    @Override protected void onDestroy() {
        if (running.get()) stop("APAGADO");
        super.onDestroy();
    }

    private void show(String s, int color) { status.setText(s); status.setTextColor(color); stateDot.setTextColor(color); }
    private int dp(int x) { return (int) (x * getResources().getDisplayMetrics().density + .5f); }
    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(sp); t.setTextColor(color); t.setLineSpacing(dp(2),1f);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private void add(LinearLayout l, View v, int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(top);
        l.addView(v, p);
    }
    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(18),dp(18),dp(18),dp(18));
        c.setBackground(background(PANEL,BORDER,20));
        return c;
    }
    private GradientDrawable background(int fill, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill); d.setStroke(dp(1),stroke); d.setCornerRadius(dp(radius));
        return d;
    }
    private ArrayAdapter<String> adapter(String[] labels) {
        ArrayAdapter<String> a = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels) {
            @Override public View getView(int p, View old, ViewGroup parent) {
                TextView t = (TextView) super.getView(p,old,parent);
                t.setTextColor(WHITE); t.setTextSize(16); t.setPadding(0,dp(9),0,dp(9));
                return t;
            }
            @Override public View getDropDownView(int p, View old, ViewGroup parent) {
                TextView t = (TextView) super.getDropDownView(p,old,parent);
                t.setTextColor(WHITE); t.setBackgroundColor(PANEL); t.setPadding(dp(12),dp(13),dp(12),dp(13));
                return t;
            }
        };
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }
}
