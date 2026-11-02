package main;

import data.Progress;
import data.SaveLoad;
import entity.Entity;
import java.awt.event.KeyEvent;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import object.OBJ_Coin;

/** Integration contracts for scheduling, input, restart and world persistence. */
public final class RuntimeRegression {
    private static int passed;
    static class QuietPanel extends GamePanel {
        @Override public void playSE(int index) {}
        @Override public void playMusic(int index) {}
        @Override public void stopMusic() {}
    }
    static final class LoopProbe extends QuietPanel {
        int frames;
        @Override public void update() {}
        @Override public void drawToTempScreen() {}
        @Override public void drawToScreen() { if (++frames == 120) gameThread = null; }
    }
    private static void check(boolean result, String contract) {
        if (!result) throw new AssertionError(contract);
        passed++;
        System.out.println("PASS " + contract);
    }
    private static KeyEvent key(GamePanel gp, int type, int code) {
        return new KeyEvent(gp, type, System.currentTimeMillis(), 0, code, KeyEvent.CHAR_UNDEFINED);
    }
    public static void main(String[] args) throws Exception {
        QuietPanel gp = new QuietPanel();
        gp.setupGame();
        gp.gameState = gp.optionsState;
        gp.ui.commandNum = 3;
        gp.gameThread = Thread.currentThread();
        gp.keyH.keyPressed(key(gp, KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER));
        gp.keyH.keyReleased(key(gp, KeyEvent.KEY_RELEASED, KeyEvent.VK_ENTER));
        check(!gp.keyH.enterPressed, "AWT input is queued without mutating simulation state");
        gp.update();
        check(gp.ui.subState == 2, "A quick Enter press/release is consumed once on the next tick");
        gp.gameThread = null;
        gp.gameState = gp.playState;
        gp.keyH.playState(KeyEvent.VK_ENTER);
        gp.keyH.playState(KeyEvent.VK_ESCAPE);
        gp.update();
        check(!gp.fullScreenOn && gp.ui.subState == 0,
            "An attack followed by Escape does not confirm an option");
        gp.gameState = gp.cutsceneState;
        gp.csManager.sceneNum = gp.csManager.GodFish;
        gp.csManager.scenePhase = 1;
        gp.keyH.dialogueState(KeyEvent.VK_ENTER);
        check(!gp.keyH.enterPressed, "Enter during cinematic camera movement cannot skip later dialogue");
        gp.csManager.reset();
        gp.keyH.upPressed = gp.keyH.spacePressed = gp.keyH.shotKeyPressed = true;
        gp.keyH.releaseAll();
        check(!gp.keyH.upPressed && !gp.keyH.spacePressed && !gp.keyH.shotKeyPressed,
            "Losing focus releases movement, guard and fire");

        Entity rock = null;
        for (Entity npc : gp.npc[4]) if (npc instanceof entity.NPC_BigRock) { rock = npc; break; }
        check(rock != null, "Original puzzle rocks are present");
        rock.worldX += gp.tileSize;
        int movedX = rock.worldX;
        gp.currentMap = 4;
        gp.currentArea = gp.dungeons;
        gp.nextArea = gp.indoor;
        gp.player.projectile.alive = true;
        gp.projectile[0][0] = gp.player.projectile;
        gp.changeArea();
        check(!gp.player.projectile.alive && gp.projectile[0][0] == null,
            "Changing maps releases shots left in the previous area");
        check(rock.worldX == movedX && java.util.Arrays.asList(gp.npc[4]).contains(rock),
            "Area changes preserve pushed rocks and their plate links");
        Progress.GodFishDefeated = true;
        gp.obj[6][10].opened = true;
        gp.resetGame(false);
        check(Progress.GodFishDefeated && gp.obj[6][10].opened && rock.worldX == movedX,
            "Retry preserves completed boss and world progress");
        check(gp.currentArea == gp.outside && gp.player.life == gp.player.maxLife,
            "Retry restores health at a valid starting area");
        gp.obj[0][29] = new OBJ_Coin(gp);
        gp.projectile[0][0] = gp.player.projectile;
        gp.csManager.sceneNum = gp.csManager.ending;
        gp.resetGame(true);
        check(gp.obj[0][29] == null && gp.projectile[0][0] == null && !Progress.GodFishDefeated,
            "New game clears previous drops, projectiles and boss flags");
        check(gp.csManager.sceneNum == gp.csManager.NA && gp.player.drawing,
            "New game clears cinematic state and restores the player");
        gp.gameState = gp.pauseState;
        Entity enemy = java.util.Arrays.stream(gp.monster[0]).filter(e -> e != null).findFirst().orElseThrow();
        enemy.dying = true;
        for (int i = 0; i < 60; i++) gp.update();
        check(enemy.alive, "Pause freezes death and gameplay timers");

        Path missing = Files.createTempDirectory(Path.of("build"), "missing-save-");
        gp.saveLoad = new SaveLoad(gp, missing.resolve("save-v1.dat"));
        gp.gameState = gp.titleState;
        gp.ui.commandNum = 1;
        gp.keyH.titleState(KeyEvent.VK_ENTER);
        check(gp.gameState == gp.titleState && !gp.ui.notice.isEmpty(),
            "Failed load stays on the title screen and shows an error");

        LoopProbe probe = new LoopProbe();
        probe.gameThread = Thread.currentThread();
        var bean = ManagementFactory.getThreadMXBean();
        long start = System.nanoTime();
        long cpuStart = bean.getCurrentThreadCpuTime();
        probe.run();
        long wall = System.nanoTime() - start;
        long cpu = bean.getCurrentThreadCpuTime() - cpuStart;
        double ratio = (double)cpu / wall;
        check(probe.frames == 120 && wall > 1_800_000_000L && wall < 5_000_000_000L,
            "The loop schedules 120 frames at approximately 60 ticks per second");
        check(ratio < .35, "The idle loop waits instead of occupying a CPU core");
        System.out.printf("LOOP_METRIC frames=%d wall_ms=%.2f thread_cpu_ms=%.2f cpu_wall_ratio=%.4f%n",
            probe.frames, wall / 1e6, cpu / 1e6, ratio);
        gp.stopGame();
        System.out.println("RUNTIME_REGRESSION PASS " + passed);
    }
}
