/*
 * Decompiled with CFR 0.152.
 */
package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import main.GamePanel;
import object.OBJ_Exit;

public class CutsceneManager {
    GamePanel gp;
    Graphics2D g2;
    public int sceneNum;
    public int scenePhase;
    int counter = 0;
    private int introTick, introStartY, introTargetY;
    private boolean seenIntro, shortIntro;
    float alpha = 0.0f;
    int y;
    String endCredit;
    public final int NA = 0;
    public final int GodFish = 1;
    public final int ending = 2;

    public CutsceneManager(GamePanel gp) {
        this.gp = gp;
        this.endCredit = "Encargado de hacer todo: Yo\nDavid Silva Padilla\n\n\n\n\n\n\n\n\n\n\n\n\n\nAgradecimientos especiales\nYo\nMi papa\nMi mama\nMi hermano (un poco)\nMolly\nSammy\nLuny\nKitty\nDina\nMis amigos (no los topo)\nEs broma XD\nLos limones son god\nque\nso\nNumero de cuenta bancaria: \nMe acorde de que no tengo\nAyer que hice?\nNose\nPero mientras les cuento que...\nBla bla bla\nBla bla bla\nBla bla bla\nNo eh dormido nada hoy\nUn saludo a la Grasa :v \nChao Chao \n";
    }

    public void update() {
        if (sceneNum == GodFish) updateFinalIntro();
        else if (sceneNum == ending) scene_Ending();
    }

    public boolean isDialogueActive() {
        return (sceneNum == GodFish && scenePhase == 3) || (sceneNum == ending && scenePhase == 1);
    }

    public void draw(Graphics2D g2) {
        this.g2 = g2;
        gp.ui.g2 = g2;
        g2.setFont(gp.ui.purisaB);
        g2.setColor(Color.WHITE);
        if (sceneNum == GodFish) drawFinalIntro(g2);
        if (isDialogueActive()) {
            gp.ui.drawDialogueScreen();
        }
        if (sceneNum != ending) return;
        if (scenePhase == 4) drawBlackBackground(alpha);
        if (scenePhase >= 5) drawBlackBackground(1f);
        if (scenePhase == 5) drawString(alpha, 22f, 125,
            "After everything you did to \ncreate this dungeon of lost thoughts...\nWhat do you earn?\nNothing...\nBut for some reason you feel that every day \nyou get closer to that dream you have.\n", 70);
        if (scenePhase == 6) drawString(1f, 80f, gp.screenHeight / 2, "The little fish", 40);
        if (scenePhase == 7) drawString(1f, 30f, y, endCredit, 40);
        if (scenePhase == 8) drawString(1f, 38f, y, endCredit, 40);
    }

    public void reset() {
        sceneNum = NA;
        scenePhase = counter = y = 0;
        alpha = 0f;
        introTick = 0;
    }

    public void clearIntroMemory(boolean newSession) {
        if (newSession) seenIntro = false;
    }

    void beginFinalIntro() {
        if (sceneNum!=NA || gp.finalArena.boss()==null) return;
        gp.flow.resetDialogue();
        gp.keyH.clearPlayKeys();
        gp.player.direction = "up";
        gp.player.attacking = gp.player.guarding = false;
        gp.player.projectile.alive = false;
        introStartY = gp.player.worldY-gp.player.screenY;
        introTargetY = FinalArena.BOSS_Y-174;
        shortIntro = seenIntro;
        introTick = 0;
        sceneNum = GodFish;
        scenePhase = 0;
        gp.gameState = gp.cutsceneState;
        gp.finalArena.seal();
    }

    int finalCameraY() {
        if (scenePhase==0) return introStartY;
        if (scenePhase==1) return smooth(introStartY,introTargetY,introTick/(shortIntro?42f:60f));
        if (scenePhase==4) return smooth(introTargetY,gp.finalArena.targetCameraY(),(introTick-18)/42f);
        return introTargetY;
    }

    private static int smooth(int from, int to, float t) {
        t = Math.max(0f,Math.min(1f,t));
        return Math.round(from+(to-from)*t*t*(3-2*t));
    }

    private void updateFinalIntro() {
        var boss = gp.finalArena.boss();
        if (boss==null || gp.currentMap!=FinalArena.MAP) {
            gp.finalArena.releaseSeal();
            gp.flow.resetDialogue();
            gp.ui.npc = null;
            gp.keyH.clearPlayKeys();
            gp.gameState = gp.playState;
            reset();
            return;
        }
        switch (scenePhase) {
            case 0 -> {
                if (++introTick >= (shortIntro ? 8 : 18)) advanceIntro();
            }
            case 1 -> {
                gp.finalArena.advanceIntroPlayer();
                if (++introTick >= (shortIntro ? 42 : 60)) advanceIntro();
            }
            case 2 -> {
                if (++introTick >= (shortIntro ? 14 : 28)) {
                    gp.ui.npc = boss;
                    boss.dialogueSet = 0;
                    boss.dialogueIndex = shortIntro ? 3 : 0;
                    advanceIntro();
                }
            }
            case 3 -> gp.flow.updateDialogue();
            case 4 -> {
                if (++introTick >= 60) {
                    seenIntro = true;
                    gp.ui.npc = null;
                    gp.keyH.clearPlayKeys();
                    reset();
                    gp.gameState = gp.playState;
                    gp.bossBattleOn = true;
                    gp.finalArena.beginCombatCamera();
                    boss.beginFight();
                    gp.stopMusic();
                    gp.playMusic(23);
                }
            }
            default -> throw new IllegalStateException("Unexpected final introduction beat");
        }
    }

    private void advanceIntro() { scenePhase++; introTick = 0; }

    private void drawFinalIntro(Graphics2D g) {
        if (scenePhase>=3) return;
        float darkness = switch (scenePhase) {
            case 0, 1 -> .86f;
            default -> Math.max(0f, .86f-introTick/(shortIntro?14f:28f));
        };
        if (darkness>0) {
            g.setColor(new Color(0,9,18,Math.round(darkness*255)));
            g.fillRect(0,0,gp.screenWidht,gp.screenHeight);
        }
        var boss = gp.finalArena.boss();
        if (boss!=null && scenePhase>=1 && (scenePhase==2 || introTick>12))
            GodFishArt.eye(g,boss.getScreenX(),boss.getScreenY());
        // The real player stays legible during the pan.
        gp.player.draw(g);
    }

    public void scene_Ending() {
        if (this.scenePhase == 0) {
            this.gp.stopMusic();
            this.gp.ui.npc = new OBJ_Exit(this.gp);
            ++this.scenePhase;
        }
        if (this.scenePhase == 1) {
            this.gp.flow.updateDialogue();
        }
        if (this.scenePhase == 2) {
            this.gp.playMusic(24);
            ++this.scenePhase;
        }
        if (this.scenePhase == 3 && this.counterReached(300)) {
            ++this.scenePhase;
        }
        if (this.scenePhase == 4) {
            this.alpha += 0.005f;
            if (this.alpha > 1.0f) {
                this.alpha = 1.0f;
            }
            if (this.alpha == 1.0f) {
                this.alpha = 0.0f;
                ++this.scenePhase;
            }
        }
        if (this.scenePhase == 5) {
            this.alpha += 0.005f;
            if (this.alpha > 1.0f) {
                this.alpha = 1.0f;
            }
            if (this.counterReached(720)) {
                ++this.scenePhase;
            }
        }
        if (this.scenePhase == 6) {
            if (this.counterReached(240)) {
                ++this.scenePhase;
            }
        }
        if (this.scenePhase == 7) {
            this.y = this.gp.screenHeight / 2;
            if (this.counterReached(240)) {
                ++this.scenePhase;
            }
        }
        if (this.scenePhase == 8) {
            --this.y;
        }
    }

    public boolean counterReached(int target) {
        boolean counterReached = false;
        ++this.counter;
        if (this.counter > target) {
            counterReached = true;
            this.counter = 0;
        }
        return counterReached;
    }

    public void drawBlackBackground(float alpha) {
        this.g2.setComposite(AlphaComposite.getInstance(3, alpha));
        this.g2.setColor(Color.black);
        this.g2.fillRect(0, 0, this.gp.screenWidht, this.gp.screenHeight);
        this.g2.setComposite(AlphaComposite.getInstance(3, 1.0f));
    }

    public void drawString(float alpha, float fontSize, int y, String text, int lineHeight) {
        this.g2.setComposite(AlphaComposite.getInstance(3, alpha));
        this.g2.setColor(Color.white);
        this.g2.setFont(this.g2.getFont().deriveFont(fontSize));
        String[] stringArray = text.split("\n");
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String line = stringArray[n2];
            int x = this.gp.ui.getXforCenteredText(line);
            this.g2.drawString(line, x, y);
            y += lineHeight;
            ++n2;
        }
        this.g2.setComposite(AlphaComposite.getInstance(3, 1.0f));
    }
}

