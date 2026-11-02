/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import java.awt.Rectangle;
import java.util.ArrayList;
import main.GamePanel;
import tile_interactive.InteractiveTile;

public class NPC_BigRock
extends Entity {
    public static final String npcName = "Big Rock";

    public NPC_BigRock(GamePanel gp) {
        super(gp);
        this.name = npcName;
        this.direction = "down";
        this.speed = 4;
        this.solidArea = new Rectangle();
        this.solidArea.x = 2;
        this.solidArea.y = 6;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
        this.solidArea.width = 44;
        this.solidArea.height = 40;
        this.dialogueSet = -1;
        this.getImage();
        this.setDialogue();
    }

    public void getImage() {
        this.left = this.setup("/npc/bigRock", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/npc/bigRock", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/npc/bigRock", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/npc/bigRock", this.gp.tileSize, this.gp.tileSize);
        this.up = this.setup("/npc/bigRock", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/npc/bigRock", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/npc/bigRock", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/npc/bigRock", this.gp.tileSize, this.gp.tileSize);
    }

    public void setDialogue() {
        this.dialogues[0][0] = "Its you?...";
    }

    @Override
    public void setAction() {
    }

    @Override
    public void update() {
    }

    @Override
    public void speak() {
        this.facePlayer();
        this.startDialogue(this, this.dialogueSet);
        ++this.dialogueSet;
        if (this.dialogues[this.dialogueSet][0] == null) {
            --this.dialogueSet;
        }
    }

    @Override
    public void move(String d) {
        block16: {
            this.direction = d;
            this.checkCollision();
            if (this.collisionOn) break block16;
            switch (this.direction) {
                case "up": {
                    this.worldY -= this.speed;
                    break;
                }
                case "down": {
                    this.worldY += this.speed;
                    break;
                }
                case "left": {
                    this.worldX -= this.speed;
                    break;
                }
                case "right": {
                    this.worldX += this.speed;
                }
            }
        }
        this.detectedPlate();
    }

    public void detectedPlate() {
        ArrayList<InteractiveTile> plateList = new ArrayList<InteractiveTile>();
        ArrayList<Entity> rockList = new ArrayList<Entity>();
        int i = 0;
        while (i < this.gp.iTile[1].length) {
            if (this.gp.iTile[this.gp.currentMap][i] != null && this.gp.iTile[this.gp.currentMap][i].name != null && this.gp.iTile[this.gp.currentMap][i].name.equals("Metal Plate")) {
                plateList.add(this.gp.iTile[this.gp.currentMap][i]);
            }
            ++i;
        }
        i = 0;
        while (i < this.gp.npc[1].length) {
            if (this.gp.npc[this.gp.currentMap][i] != null && this.gp.npc[this.gp.currentMap][i].name != null && this.gp.npc[this.gp.currentMap][i].name.equals(npcName)) {
                rockList.add(this.gp.npc[this.gp.currentMap][i]);
            }
            ++i;
        }
        int count = 0;
        int i2 = 0;
        while (i2 < plateList.size()) {
            int yDistance;
            int xDistance = Math.abs(this.worldX - ((InteractiveTile)plateList.get((int)i2)).worldX);
            int distance = Math.max(xDistance, yDistance = Math.abs(this.worldY - ((InteractiveTile)plateList.get((int)i2)).worldY));
            if (distance < 8) {
                if (this.linkedEntity == null) {
                    this.linkedEntity = (Entity)plateList.get(i2);
                    this.gp.playSE(14);
                }
            } else if (this.linkedEntity == plateList.get(i2)) {
                this.linkedEntity = null;
            }
            ++i2;
        }
        i2 = 0;
        while (i2 < rockList.size()) {
            if (((Entity)rockList.get((int)i2)).linkedEntity != null) {
                ++count;
            }
            ++i2;
        }
        if (count == rockList.size()) {
            i2 = 0;
            while (i2 < this.gp.obj[1].length) {
                if (this.gp.obj[this.gp.currentMap][i2] != null && this.gp.obj[this.gp.currentMap][i2].name.equals("Iron Door")) {
                    this.gp.obj[this.gp.currentMap][i2] = null;
                    this.gp.playSE(3);
                }
                ++i2;
            }
        }
    }
}

