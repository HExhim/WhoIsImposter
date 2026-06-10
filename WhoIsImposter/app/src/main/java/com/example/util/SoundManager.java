package com.example.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {
    private static volatile SoundManager instance;
    private static final String PREFS_NAME = "mehfil_prefs";
    private static final String KEY_SOUND_MUTED = "sound_muted";
    private static final String KEY_VIBRATE_MUTED = "vibrate_muted";

    private final SharedPreferences prefs;
    private final ExecutorService soundExecutor;
    private ToneGenerator toneGenerator;
    private Vibrator vibrator;
    private boolean isSoundMuted;
    private boolean isVibrateMuted;

    private SoundManager(Context context) {
        Context appContext = context.getApplicationContext();
        prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isSoundMuted = prefs.getBoolean(KEY_SOUND_MUTED, false);
        isVibrateMuted = prefs.getBoolean(KEY_VIBRATE_MUTED, false);
        soundExecutor = Executors.newSingleThreadExecutor();
        vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);

        soundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
                } catch (Exception e) {
                    toneGenerator = null;
                }
            }
        });
    }

    public static SoundManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SoundManager.class) {
                if (instance == null) {
                    instance = new SoundManager(context);
                }
            }
        }
        return instance;
    }

    public boolean isSoundMuted() {
        return isSoundMuted;
    }

    public void setSoundMuted(boolean muted) {
        this.isSoundMuted = muted;
        prefs.edit().putBoolean(KEY_SOUND_MUTED, muted).apply();
    }

    public boolean isVibrateMuted() {
        return isVibrateMuted;
    }

    public void setVibrateMuted(boolean muted) {
        this.isVibrateMuted = muted;
        prefs.edit().putBoolean(KEY_VIBRATE_MUTED, muted).apply();
    }

    public void playClick() {
        if (isSoundMuted) return;
        soundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (toneGenerator != null) {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 70); // Crisp touch tone
                }
            }
        });
    }

    public void playReveal() {
        if (isSoundMuted) return;
        soundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (toneGenerator != null) {
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 120);
                }
            }
        });
        vibrate(80);
    }

    public void playSuccess() {
        if (isSoundMuted) return;
        soundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (toneGenerator != null) {
                    // Success visual sound cascade code
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 100);
                    try { Thread.sleep(120); } catch (Exception ignored) {}
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 100);
                    try { Thread.sleep(120); } catch (Exception ignored) {}
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 200);
                }
            }
        });
        vibrate(200);
    }

    public void playFail() {
        if (isSoundMuted) return;
        soundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (toneGenerator != null) {
                    toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 350); // Low warning/error buzz-tone
                }
            }
        });
        vibrate(300);
    }

    public void vibrate(final long ms) {
        if (isVibrateMuted || vibrator == null) return;
        try {
            vibrator.vibrate(ms);
        } catch (Exception ignored) {}
    }
}
