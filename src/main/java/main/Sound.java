package main;

import java.util.LinkedHashMap;
import javax.sound.sampled.*;

/** Device operations belong exclusively to the audio worker, never the game or Swing thread. */
public final class Sound implements AutoCloseable {
    private static final String[] FILES = {
        "theme.wav", "powerUP.wav", "coinEffect.wav", "doorEffect.wav", "keyEffect.wav",
        "finishEffect.wav", "titleThem.wav", "select.wav", "newDamage.wav", "playerBubble.wav",
        "enemyHitted.wav", "levelUp.wav", "healEffect.wav", "waterEffect.wav", "rockEffect.wav",
        "gameOverTheme.wav", "changeEffect.wav", "bedTime.wav", "shieldEffect.wav", "parryEffect.wav",
        "healpool.wav", "talk2.wav", "dungeonTheme.wav", "FINALBOSS.wav", "Credits.wav",
        "playerHitted.wav", "proEffect.wav", "talk.wav"
    };

    private static final float[] VOLUMES = {-80f, -25f, -20f, -15f, -10f, 2f};
    interface ClipFactory { Clip open(int index) throws Exception; }
    private record Request(int index, boolean loop, long created) {}
    private final Object lock = new Object();
    private final LinkedHashMap<Integer, Request> pending = new LinkedHashMap<>();
    private final Clip[] clips = new Clip[FILES.length];
    private final boolean[] failed = new boolean[FILES.length];
    private final long[] used = new long[FILES.length];
    private final int maxVoices;
    private final ClipFactory factory;
    private final Thread worker;
    private final boolean disabled = Boolean.getBoolean("littlefish.audio.disabled");
    private boolean closing, stopRequested, volumeRequested, busy;
    private long sequence;
    private volatile int selected = -1;
    volatile int volumeScale = 1;
    private volatile int cachedCount;
    private volatile float attenuation;

    public Sound() { this(1); }
    public Sound(int maxVoices) { this(maxVoices, Sound::openClip); }
    Sound(int maxVoices, ClipFactory factory) {
        if (maxVoices < 1 || maxVoices > 32) throw new IllegalArgumentException("Invalid voice count");
        this.maxVoices = maxVoices;
        this.factory = factory;
        worker = new Thread(this::run, "little-fish-audio-" + maxVoices);
        worker.setDaemon(true);
        worker.start();
    }

    private static Clip openClip(int index) throws Exception {
        return openClip("/sound/" + FILES[index]);
    }

    static Clip openClip(String path) throws Exception {
        var resource = Sound.class.getResource(path);
        if (resource == null) throw new IllegalArgumentException("Missing audio resource: " + path);
        Clip clip = AudioSystem.getClip();
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(resource)) {
            clip.open(stream);
            return clip;
        } catch (Exception error) {
            clip.close();
            throw error;
        }
    }

    public void setFile(int index) { selected = index; }
    public void play() { requestPlay(selected, false); }
    public void loop() { requestPlay(selected, true); }

    public void requestPlay(int index, boolean loop) {
        if (disabled || index < 0 || index >= FILES.length) return;
        synchronized (lock) {
            if (closing) return;
            // Music keeps only the latest choice. Repeated letters/cursor ticks cannot build a backlog.
            if (maxVoices == 1) pending.clear();
            pending.put(index, new Request(index, loop, System.nanoTime()));
            lock.notifyAll();
        }
    }

    void setAttenuation(float decibels) {
        attenuation=decibels;
        checkVolume();
    }

    public void checkVolume() {
        volumeScale = Math.max(0, Math.min(5, volumeScale));
        synchronized (lock) { if (!closing) { volumeRequested = true; lock.notifyAll(); } }
    }

    public void stop() {
        synchronized (lock) {
            pending.clear();
            if (!closing) { stopRequested = true; lock.notifyAll(); }
        }
    }

    @Override public void close() {
        synchronized (lock) { closing = true; pending.clear(); lock.notifyAll(); }
    }

    private void run() {
        try {
            while (true) {
                Request request = null;
                boolean stop, volume;
                synchronized (lock) {
                    while (!closing && pending.isEmpty() && !stopRequested && !volumeRequested) lock.wait();
                    if (closing) return;
                    busy = true;
                    stop = stopRequested; stopRequested = false;
                    volume = volumeRequested; volumeRequested = false;
                    if (!pending.isEmpty()) {
                        var iterator = pending.values().iterator();
                        request = iterator.next(); iterator.remove();
                    }
                }
                try {
                    if (stop) for (Clip clip : clips) if (clip != null) clip.stop();
                    if (volume) for (Clip clip : clips) if (clip != null) applyVolume(clip);
                    if (request != null && (maxVoices == 1 || System.nanoTime() - request.created < 250_000_000L)) {
                        playRequest(request);
                    }
                } catch (Exception error) {
                    System.err.println("Audio operation failed: " + error.getMessage());
                } finally {
                    synchronized (lock) { busy = false; lock.notifyAll(); }
                }
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } finally {
            for (Clip clip : clips) if (clip != null) { clip.stop(); clip.close(); }
            cachedCount = 0;
            synchronized (lock) { busy = false; lock.notifyAll(); }
        }
    }

    private void playRequest(Request request) throws Exception {
        int index = request.index;
        if (failed[index]) return;
        Clip clip = clips[index];
        if (clip == null) {
            try { clip = factory.open(index); }
            catch (Exception error) {
                failed[index] = true;
                System.err.println("Audio resource unavailable: " + index + " (" + error.getMessage() + ")");
                return;
            }
            clips[index] = clip;
            cachedCount++;
        }
        // Device initialization may be slow: do not play an obsolete request after stop/close/new music.
        synchronized (lock) {
            if (closing || stopRequested || (maxVoices == 1 && !pending.isEmpty())) return;
            if (maxVoices > 1 && System.nanoTime() - request.created > 250_000_000L) return;
        }
        int running = 0, oldest = -1;
        for (int i = 0; i < clips.length; i++) {
            if (i != index && clips[i] != null && clips[i].isRunning()) {
                running++;
                if (oldest < 0 || used[i] < used[oldest]) oldest = i;
            }
        }
        if (running >= maxVoices && oldest >= 0) clips[oldest].stop();
        used[index] = ++sequence;
        clip.stop();
        clip.setFramePosition(0);
        applyVolume(clip);
        if (request.loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
        else clip.start();
        // Keep one clip per resource (at most 28); close only on shutdown, never per letter.
    }

    private void applyVolume(Clip clip) {
        int scale = Math.max(0, Math.min(5, volumeScale));
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            control.setValue(Math.max(control.getMinimum(), Math.min(control.getMaximum(), VOLUMES[scale]-attenuation)));
        }
    }

    public int openVoiceCount() { return cachedCount; }
    /** Diagnostics/tests only. Gameplay must never await the audio device. */
    boolean awaitIdle(long timeoutMillis) throws InterruptedException {
        long deadline = System.nanoTime() + timeoutMillis * 1_000_000L;
        synchronized (lock) {
            while (busy || !pending.isEmpty() || stopRequested || volumeRequested) {
                long left = deadline - System.nanoTime();
                if (left <= 0) return false;
                lock.wait(Math.max(1, left / 1_000_000L));
            }
            return true;
        }
    }
    boolean awaitClosed(long timeoutMillis) throws InterruptedException {
        worker.join(timeoutMillis);
        return !worker.isAlive();
    }
}
