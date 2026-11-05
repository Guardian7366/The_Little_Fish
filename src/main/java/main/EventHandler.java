/*
 * Decompiled with CFR 0.152.
 */
package main;

import data.Progress;
import entity.Entity;
import main.EventRect;
import main.GamePanel;

public class EventHandler {
    GamePanel gp;
    EventRect[][][] eventRect;
    int previousEventX;
    int previousEventY;
    boolean canTouchEvent;
    int tempMap;
    int tempCol;
    int tempRow;
    Entity eventMaster;

    public EventHandler(GamePanel gp) {
        this.gp = gp;
        canTouchEvent = true;
        eventMaster = new Entity(gp);
        eventRect = new EventRect[gp.maxMap][gp.maxWorldCol][gp.maxWorldRow];
        for (int map = 0; map < gp.maxMap; map++) {
            for (int col = 0; col < gp.maxWorldCol; col++) {
                for (int row = 0; row < gp.maxWorldRow; row++) {
                    EventRect rect = new EventRect();
                    rect.setBounds(23, 23, 15, 15);
                    rect.eventRectDefaultX = rect.eventRectDefaultY = 23;
                    eventRect[map][col][row] = rect;
                }
            }
        }
        setDialogue();
    }

    public void setDialogue() {
        for (int i=0;i<TempleExtension.INSCRIPTIONS.length;i++)
            eventMaster.dialogues[15+i][0]=TempleExtension.INSCRIPTIONS[i].text();
        eventMaster.dialogues[12][0]="The fish went down in single file.\nOne carried a small light.";
        eventMaster.dialogues[13][0]="The D was carved over much older marks.";
        eventMaster.dialogues[14][0]="Four weights guarded the way.\nD. I counted four and left a light on.";
        eventMaster.dialogues[14][1]="The old lamp and fish marks hide a seam.";
        eventMaster.dialogues[11][0]="Someone tested a tool here.\nThe last mark points toward the refuge.";
        eventMaster.dialogues[8][0]="The marks continue on the other side\nof the stone.";
        eventMaster.dialogues[9][0]="Someone wrote: GO BACK.\nThe arrow points down.";
        eventMaster.dialogues[10][0]="This shape feels familiar.\nYou cannot remember seeing it before.";
        this.eventMaster.dialogues[7][0] = "Someone counted the shadows here.\nThen stopped counting.";
        this.eventMaster.dialogues[0][0] = "You take damage";
        this.eventMaster.dialogues[1][0] = "MAX HP and MANA\n You don\u00b4t forget your memories";
        this.eventMaster.dialogues[2][0] = "A distortion in your memory occurred";
        this.eventMaster.dialogues[3][0] = "Someone carved a smile into this rock.";
        this.eventMaster.dialogues[3][1] = "Even at the bottom of the sea,\nsomeone had time for this.";
        this.eventMaster.dialogues[4][0] = "The mark is worn. One letter remains:\nD...";
        this.eventMaster.dialogues[5][0] = "NO FISHING.\nSomeone remembered to put up a sign.";
        this.eventMaster.dialogues[6][0] = "A trail of marks ends here.\nOne has been carefully rubbed away.";
        this.eventMaster.dialogues[4][1] = "You almost remember who wrote it.";
    }

    public void checkEvent() {
        if (gp.extraRooms.interact()) return;
        if (gp.market.interact()) return;
        if (gp.currentMap==TempleExtension.MAP && gp.gameState==gp.playState && gp.keyH.enterPressed) {
            for (int i=0;i<TempleExtension.INSCRIPTIONS.length;i++) {
                var inscription = TempleExtension.INSCRIPTIONS[i];
                if (hit(TempleExtension.MAP,inscription.readX(),inscription.readY(),inscription.facing())) {
                    inspectSecret(15+i);
                    return;
                }
            }
        }
        if (gp.currentMap==Temple.MAP && gp.gameState==gp.playState && gp.keyH.enterPressed) {
            int x=(gp.player.worldX+24)/48, y=(gp.player.worldY+24)/48;
            for (int i=0;i<Temple.MURALS.length;i++) {
                int[] mural=Temple.MURALS[i];
                if (y==mural[1]+2 && x>=mural[0] && x<mural[0]+3 && gp.player.direction.equals("up")) {
                    inspectSecret(12+i);
                    return;
                }
            }
            if ((x==8 && y==21 && gp.player.direction.equals("left") && gp.temple.openRelief(0)) ||
                (x==41 && y==23 && gp.player.direction.equals("right") && gp.temple.openRelief(1))) {
                gp.player.attackCanceled=true;
                return;
            }
        }
        if(gp.currentMap==3 && gp.gameState==gp.playState && gp.keyH.enterPressed &&
            (gp.player.worldX+24)/48==14 && (gp.player.worldY+24)/48==5 && gp.player.direction.equals("up")) {
            inspectSecret(11);return;
        }
        if(gp.currentMap==FourthZone.MAP && gp.gameState==gp.playState && gp.keyH.enterPressed && gp.player.direction.equals("up")) {
            int col=(gp.player.worldX+24)/48,row=(gp.player.worldY+24)/48;
            for(int i=0;i<FourthZone.SECRETS.length;i++) if(col==FourthZone.SECRETS[i][0] && row==FourthZone.SECRETS[i][1]) {inspectSecret(8+i);return;}
        }
        if(gp.currentMap==2 && gp.gameState==gp.playState && gp.keyH.enterPressed &&
           (gp.player.worldX+24)/48==27 && (gp.player.worldY+24)/48==33 && gp.player.direction.equals("left")) {
            inspectSecret(7);return;
        }
        if (gp.currentMap == 1 && gp.gameState == gp.playState && gp.keyH.enterPressed && gp.player.direction.equals("up")) {
            int col=(gp.player.worldX+24)/48,row=(gp.player.worldY+24)/48;
            if(col==8 && row==26) { inspectSecret(5); return; }
            if(col==26 && row==7) { inspectSecret(6); return; }
        }
        if (gp.currentMap == 0 && gp.keyH.enterPressed && gp.gameState == gp.playState) {
            int col = (gp.player.worldX + gp.tileSize / 2) / gp.tileSize;
            int row = (gp.player.worldY + gp.tileSize / 2) / gp.tileSize;
            if (col == 23 && row == 39 && gp.player.direction.equals("down")) {
                inspectSecret(3); return;
            }
            if (col == 12 && row == 19 && gp.player.direction.equals("up")) {
                inspectSecret(4); return;
            }
        }
        int yDistance;
        int xDistance = Math.abs(this.gp.player.worldX - this.previousEventX);
        int distance = Math.max(xDistance, yDistance = Math.abs(this.gp.player.worldY - this.previousEventY));
        if (distance > this.gp.tileSize) {
            this.canTouchEvent = true;
        }
        if (this.canTouchEvent) {
            if (this.hit(0, 5, 6, "any")) {
                this.gp.getClass();
                this.damagePit(5, 6, 3);
            } else if (this.hit(0, 13, 31, "any")) {
                this.gp.getClass();
                this.healingPool(13, 31, 3);
            } else if (this.hit(1, 14, 41, "any")) {
                this.gp.getClass();
                this.healingPool(14, 31, 3);
            } else if (this.hit(2, 17, 6, "any")) {
                this.gp.getClass();
                this.healingPool(17, 6, 3);
            } else if (this.hit(2, 28, 6, "any")) {
                this.gp.getClass();
                this.healingPool(28, 6, 3);
            } else if (this.hit(3, 25, 20, "any")) {
                this.gp.getClass();
                this.healingPool(25, 20, 3);
            } else if (this.hit(4, 21, 41, "any") || this.hit(4, 24, 30, "any")) {
                this.gp.getClass();
                this.healingPool(25, 20, 3);
            } else if (this.hit(4, 25, 30, "any")) {
                this.gp.getClass();
                this.healingPool(25, 20, 3);
            } else if (this.hit(6, 24, 41, "any")) {
                this.gp.getClass();
                this.healingPool(24, 41, 3);
            } else if (this.hit(6, 25, 41, "any")) {
                this.gp.getClass();
                this.healingPool(25, 41, 3);
            } else if (this.hit(0, 38, 25, "any")) {
                this.teleportMap(1, 9, 41, this.gp.outside);
            } else if (this.hit(1, 39, 7, "any")) {
                this.teleportMap(2, 22, 7, this.gp.outside);
            } else if (this.hit(2, 20, 44, "any")) {
                this.teleportMap(FourthZone.MAP, 25, 7, this.gp.outside);
            } else if (this.hit(FourthZone.MAP, 25, 45, "any")) {
                this.teleportMap(3, 25, 23, this.gp.dungeons);
            } else if (this.hit(FourthZone.MAP, 25, 8, "any")) {
                this.healingPool(25, 8, gp.dialogueState);
            } else if (this.hit(3, 9, 44, "any")) {
                this.teleportMap(8, 25, 43, this.gp.dungeons);
            } else if (this.hit(8, 23, 45, "any")) {
                this.teleportMap(3, 17, 44, this.gp.dungeons);
            } else if (this.hit(8, 25, 3, "any")) {
                this.teleportMap(4, 24, 42, this.gp.dungeons);
            } else if (this.hit(4, 24, 44, "any")) {
                this.teleportMap(8, 25, 6, this.gp.dungeons);
            } else if (this.hit(8, 25, 41, "any")) {
                this.healingPool(25, 41, this.gp.dialogueState);
            } else if (this.hit(4, 24, 5, "any")) {
                if (gp.temple.solved()) this.teleportMap(TempleExtension.MAP, 24, 43, this.gp.dungeons);
            } else if (this.hit(TempleExtension.MAP, 24, 45, "any")) {
                this.teleportMap(4, 24, 6, this.gp.dungeons);
            } else if (this.hit(TempleExtension.MAP, 22, 41, "any")) {
                this.healingPool(22, 41, gp.dialogueState);
            } else if (this.hit(TempleExtension.MAP, 24, 5, "any")) {
                if (TempleExtension.solved(gp.npc,gp.iTile)) this.teleportMap(Market.MAP, 24, 27, gp.indoor);
            } else if (this.hit(Market.MAP, 24, 19, "any")) {
                this.teleportMap(6, 24, 44, gp.dungeons);
            } else if (this.hit(Market.MAP, 28, 27, "any")) {
                this.healingPool(28, 27, gp.dialogueState);
            } else if (this.hit(Market.MAP, 24, 29, "any")) {
                int returnRow = TempleExtension.solved(gp.npc, gp.iTile) ? TempleExtension.GATE_Y : TempleExtension.GATE_Y + 1;
                this.teleportMap(TempleExtension.MAP, 24, returnRow, gp.dungeons);
            } else if (this.hit(0, 28, 18, "any")) {
                this.teleportMap(5, 10, 40, this.gp.indoor);
            } else if (this.hit(5, 9, 40, "any")) {
                this.teleportMap(0, 28, 18, this.gp.outside);
            } else if (this.hit(1, 9, 34, "any")) {
                this.teleportMap(5, 39, 40, this.gp.indoor);
            } else if (this.hit(5, 40, 40, "any")) {
                this.teleportMap(1, 9, 34, this.gp.outside);
            } else if (this.hit(1, 39, 23, "any")) {
                this.teleportMap(5, 39, 29, this.gp.indoor);
            } else if (this.hit(5, 40, 29, "any")) {
                this.teleportMap(1, 39, 23, this.gp.outside);
            } else if (this.hit(2, 39, 13, "any")) {
                this.teleportMap(5, 10, 29, this.gp.indoor);
            } else if (this.hit(5, 9, 29, "any")) {
                this.teleportMap(2, 39, 13, this.gp.outside);
            } else if (this.hit(2, 9, 17, "any")) {
                this.teleportMap(5, 10, 19, this.gp.indoor);
            } else if (this.hit(5, 9, 19, "any")) {
                this.teleportMap(2, 9, 17, this.gp.outside);
            } else if (this.hit(2, 19, 16, "any")) {
                this.teleportMap(5, 39, 19, this.gp.indoor);
            } else if (this.hit(5, 40, 19, "any")) {
                this.teleportMap(2, 19, 16, this.gp.outside);
            } else if (this.hit(3, 9, 5, "any")) {
                this.teleportMap(5, 39, 9, this.gp.indoor);
            } else if (this.hit(5, 40, 9, "any")) {
                this.teleportMap(3, 9, 5, this.gp.dungeons);
            } else if (this.hit(3, 14, 40, "any")) {
                this.teleportMap(5, 10, 9, this.gp.indoor);
            } else if (this.hit(5, 9, 9, "any")) {
                this.teleportMap(3, 14, 40, this.gp.dungeons);
            } else if (this.hit(6, 25, 25, "any")) {
                this.GodFish();
            }
        }
    }

    public boolean hit(int map, int col, int row, String reqDirection) {
        boolean hit = false;
        if (map == this.gp.currentMap && (map!=ExtraRooms.MAP ||
            ExtraRooms.at(col,row)==gp.extraRooms.active())) {
            this.gp.player.solidArea.x = this.gp.player.worldX + this.gp.player.solidArea.x;
            this.gp.player.solidArea.y = this.gp.player.worldY + this.gp.player.solidArea.y;
            this.eventRect[map][col][row].x = col * this.gp.tileSize + this.eventRect[map][col][row].x;
            this.eventRect[map][col][row].y = row * this.gp.tileSize + this.eventRect[map][col][row].y;
            if (this.gp.player.solidArea.intersects(this.eventRect[map][col][row]) && !this.eventRect[map][col][row].eventDone && (this.gp.player.direction.equals(reqDirection) || reqDirection.contentEquals("any"))) {
                hit = true;
                this.previousEventX = this.gp.player.worldX;
                this.previousEventY = this.gp.player.worldY;
            }
            this.gp.player.solidArea.x = this.gp.player.solidAreaDefaultX;
            this.gp.player.solidArea.y = this.gp.player.solidAreaDefaultY;
            this.eventRect[map][col][row].x = this.eventRect[map][col][row].eventRectDefaultX;
            this.eventRect[map][col][row].y = this.eventRect[map][col][row].eventRectDefaultY;
        }
        return hit;
    }

    public void teleport(int gameState, int x, int y) {
        this.gp.gameState = gameState;
        this.gp.playSE(17);
        this.eventMaster.startDialogue(this.eventMaster, 2);
        this.gp.player.worldX = this.gp.tileSize * x;
        this.gp.player.worldY = this.gp.tileSize * y;
    }

    private void inspectSecret(int dialogue) {
        gp.player.attackCanceled = true;
        eventMaster.startDialogue(eventMaster, dialogue);
    }

    public void damagePit(int col, int row, int gameState) {
        this.gp.gameState = gameState;
        this.gp.playSE(8);
        this.eventMaster.startDialogue(this.eventMaster, 0);
        --this.gp.player.life;
        this.canTouchEvent = false;
    }

    public void healingPool(int col, int row, int gameState) {
        if (this.gp.keyH.enterPressed) {
            this.gp.gameState = gameState;
            this.gp.player.attackCanceled = true;
            this.gp.playSE(20);
            this.eventMaster.startDialogue(this.eventMaster, 1);
            this.gp.player.life = this.gp.player.maxLife;
            this.gp.player.water = this.gp.player.maxWater;
            if (!this.gp.saveLoad.trySave()) {
                this.eventMaster.dialogues[1][0] = "No se pudo guardar.\n" + this.gp.saveLoad.getLastError();
            } else {
                this.setDialogue();
            }
        }
    }

    public void teleportMap(int map, int col, int row, int area) {
        this.gp.gameState = this.gp.transitionState;
        this.gp.nextArea = area;
        this.tempMap = map;
        this.tempCol = col;
        this.tempRow = row;
        this.canTouchEvent = false;
        this.gp.playSE(16);
    }

    public void speak(Entity entity) {
        if (this.gp.keyH.enterPressed) {
            this.gp.getClass();
            this.gp.gameState = 3;
            this.gp.player.attackCanceled = true;
            entity.speak();
        }
    }

    public void GodFish() {
        if (!this.gp.bossBattleOn && !Progress.GodFishDefeated) {
            this.gp.gameState = this.gp.cutsceneState;
            this.gp.csManager.sceneNum = this.gp.csManager.GodFish;
        }
    }
}

