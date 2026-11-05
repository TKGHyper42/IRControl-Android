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
    private static final int BG = Color.rgb(9, 15, 28);
    private static final int PANEL = Color.rgb(20, 31, 51);
    private static final int BORDER = Color.rgb(48, 68, 93);
    private static final int WHITE = Color.rgb(240, 248, 255);
    private static final int MUTED = Color.rgb(155, 173, 196);
    private static final int CYAN = Color.rgb(33, 217, 229);
    private static final int AMBER = Color.rgb(255, 178, 85);
    private static final int RED = Color.rgb(170, 64, 75);
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
    private TextView status, detail, level, countdown;
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
            show("EMISOR IR DETECTADO", CYAN);
            detail.setText("Listo para transmitir por Android ConsumerIrManager.");
        } else {
            show("EMISOR IR NO EXPUESTO", AMBER);
            detail.setText("El sistema/ROM no publica la interfaz IR. En una ROM personalizada puede faltar el HAL infrarrojo.");
        }
        toggle.setEnabled(hasEmitter);
        test.setEnabled(hasEmitter);
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        scroll.setFillViewport(true);
        scroll.setOnApplyWindowInsetsListener((v, insets) -> {
            scroll.setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            return insets;
        });
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(22), dp(26), dp(22), dp(30));
        scroll.addView(column, new ScrollView.LayoutParams(-1, -2));
        setContentView(scroll);

        column.addView(text("TKG HYPERX  /  HARDWARE LAB", 12, CYAN, true));
        TextView heading = text("IR CONTROL", 35, WHITE, true);
        add(column, heading, 7);
        add(column, text("Control nativo del emisor IR de Android", 14, MUTED, false), 5);

        LinearLayout stateCard = card();
        add(column, stateCard, 24);
        status = text("COMPROBANDO HARDWARE", 16, MUTED, true);
        stateCard.addView(status);
        detail = text("", 13, MUTED, false);
        add(stateCard, detail, 9);

        LinearLayout powerCard = card();
        add(column, powerCard, 15);
        powerCard.addView(text("ACTIVIDAD DEL IR", 12, MUTED, true));
        level = text("10 %", 39, WHITE, true);
        add(powerCard, level, 7);
        SeekBar seek = new SeekBar(this);
        seek.setMax(15);
        seek.setProgress(5);
        seek.setProgressTintList(ColorStateList.valueOf(CYAN));
        seek.setThumbTintList(ColorStateList.valueOf(CYAN));
        add(powerCard, seek, 10);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int p, boolean user) {
                duty = p + 5;
                level.setText(duty + " %");
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
        });
        powerCard.addView(text("5 %                                                       20 %", 11, MUTED, false));
        add(powerCard, text("Porcentaje de tiempo con portadora IR durante cada ráfaga. No cambia la corriente ni la potencia óptica instantánea.", 12, MUTED, false), 11);

        LinearLayout config = card();
        add(column, config, 15);
        config.addView(text("FRECUENCIA DE PORTADORA", 12, MUTED, true));
        Spinner carrier = new Spinner(this);
        add(config, carrier, 8);
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
        add(config, text("Frecuencias anunciadas por el controlador; 38 kHz si no reporta rangos.", 12, MUTED, false), 3);
        add(config, text("TEMPORIZADOR", 12, MUTED, true), 18);
        Spinner timer = new Spinner(this);
        timer.setAdapter(adapter(new String[]{"5 segundos", "15 segundos", "30 segundos"}));
        timer.setSelection(1);
        timer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> a, View v, int p, long id) {
                durationSeconds = new int[]{5,15,30}[p];
            }
            @Override public void onNothingSelected(AdapterView<?> a) {}
        });
        add(config, timer, 7);
        add(config, text("Apagado automático y al minimizar. Pausa de 3 segundos entre sesiones.", 12, MUTED, false), 4);

        toggle = new Button(this);
        toggle.setText("INICIAR EMISIÓN");
        toggle.setTextColor(BG);
        toggle.setTextSize(15);
        toggle.setAllCaps(false);
        toggle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        toggle.setBackground(background(CYAN, CYAN, 16));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-1, dp(58));
        tlp.topMargin = dp(23);
        column.addView(toggle, tlp);
        toggle.setOnClickListener(v -> {
            if (running.get()) stop("DETENIDO MANUALMENTE");
            else start();
        });

        test = new Button(this);
        test.setText("PRUEBA CORTA (0.1 s)");
        test.setAllCaps(false);
        test.setTextSize(13);
        test.setTextColor(WHITE);
        test.setBackground(background(PANEL, BORDER, 16));
        LinearLayout.LayoutParams testlp = new LinearLayout.LayoutParams(-1, dp(52));
        testlp.topMargin = dp(12);
        column.addView(test, testlp);
        test.setOnClickListener(v -> quickTest());

        countdown = text("", 15, CYAN, true);
        countdown.setGravity(Gravity.CENTER);
        add(column, countdown, 17);
        add(column, text("La señal infrarroja es invisible. No apuntes el emisor a los ojos. Las transmisiones repetidas pueden calentar el equipo; el límite de tiempo es una precaución, no una garantía térmica.", 12, MUTED, false), 22);
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
        toggle.setText("INICIAR EMISIÓN");
        toggle.setTextColor(BG);
        toggle.setBackground(background(CYAN, CYAN, 16));
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

    private void show(String s, int color) { status.setText(s); status.setTextColor(color); }
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
