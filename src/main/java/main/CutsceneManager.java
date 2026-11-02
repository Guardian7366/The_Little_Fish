/*
 * Decompiled with CFR 0.152.
 */
package main;

import entity.PlayerDummy;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import main.GamePanel;
import object.OBJ_DoorIron;
import object.OBJ_Exit;

public class CutsceneManager {
    GamePanel gp;
    Graphics2D g2;
    public int sceneNum;
    public int scenePhase;
    int counter = 0;
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
        if (sceneNum == GodFish) scene_GodFish();
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
        if ((sceneNum == GodFish && scenePhase == 3) || (sceneNum == ending && scenePhase == 1)) {
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
    }

    public void scene_GodFish() {
        int i;
        if (this.scenePhase == 0) {
            this.gp.keyH.enterPressed = false;
            this.gp.flow.resetDialogue();
            this.gp.bossBattleOn = true;
            i = 0;
            while (i < this.gp.obj[1].length) {
                if (this.gp.obj[this.gp.currentMap][i] == null) {
                    this.gp.obj[this.gp.currentMap][i] = new OBJ_DoorIron(this.gp);
                    this.gp.obj[this.gp.currentMap][i].worldX = this.gp.tileSize * 25;
                    this.gp.obj[this.gp.currentMap][i].worldY = this.gp.tileSize * 26;
                    this.gp.obj[this.gp.currentMap][i].temp = true;
                    this.gp.playSE(3);
                    break;
                }
                ++i;
            }
            i = 0;
            while (i < this.gp.npc[1].length) {
                if (this.gp.npc[this.gp.currentMap][i] == null) {
                    this.gp.npc[this.gp.currentMap][i] = new PlayerDummy(this.gp);
                    this.gp.npc[this.gp.currentMap][i].worldX = this.gp.player.worldX;
                    this.gp.npc[this.gp.currentMap][i].worldY = this.gp.player.worldY;
                    this.gp.npc[this.gp.currentMap][i].direction = this.gp.player.direction;
                    break;
                }
                ++i;
            }
            this.gp.player.drawing = false;
            ++this.scenePhase;
        }
        if (this.scenePhase == 1) {
            this.gp.player.worldY -= 2;
            if (this.gp.player.worldY < this.gp.tileSize * 15) {
                ++this.scenePhase;
            }
        }
        if (this.scenePhase == 2) {
            i = 0;
            while (i < this.gp.monster[1].length) {
                if (this.gp.monster[this.gp.currentMap][i] != null && this.gp.monster[this.gp.currentMap][i].name.equals("God Fish")) {
                    this.gp.monster[this.gp.currentMap][i].sleep = false;
                    this.gp.ui.npc = this.gp.monster[this.gp.currentMap][i];
                    ++this.scenePhase;
                    break;
                }
                ++i;
            }
        }
        if (this.scenePhase == 3) {
            this.gp.flow.updateDialogue();
        }
        if (this.scenePhase == 4) {
            i = 0;
            while (i < this.gp.npc[1].length) {
                if (this.gp.npc[this.gp.currentMap][i] != null && this.gp.npc[this.gp.currentMap][i].name.equals("Dummy")) {
                    this.gp.player.worldX = this.gp.npc[this.gp.currentMap][i].worldX;
                    this.gp.player.worldY = this.gp.npc[this.gp.currentMap][i].worldY;
                    this.gp.npc[this.gp.currentMap][i] = null;
                    break;
                }
                ++i;
            }
            this.gp.player.drawing = true;
            this.sceneNum = 0;
            this.scenePhase = 0;
            this.gp.gameState = this.gp.playState;
            this.gp.stopMusic();
            this.gp.playMusic(23);
        }
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

