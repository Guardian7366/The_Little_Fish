/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import main.GamePanel;

public class NPC_Flower
extends Entity {
    public NPC_Flower(GamePanel gp) {
        super(gp);
        this.direction = "down";
        this.speed = 0;
        this.getImage();
        this.setDialogue();
    }

    public void getImage() {
        this.left = this.setup("/npc/npcFlower", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/npc/npcFlower2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/npc/npcFlower", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/npc/npcFlower2", this.gp.tileSize, this.gp.tileSize);
        this.up = this.setup("/npc/npcFlower", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/npc/npcFlower2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/npc/npcFlower", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/npc/npcFlower2", this.gp.tileSize, this.gp.tileSize);
    }

    public void setDialogue() {
        this.dialogues[0][0] = "I think you were a monster";
        this.dialogues[0][1] = "But you are better than them...";
        this.dialogues[0][2] = "You can do it D";
        this.dialogues[0][3] = "Take this chest and GO!";
    }

    @Override
    public void setAction() {
        this.getRandomDirection(120);
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
}

