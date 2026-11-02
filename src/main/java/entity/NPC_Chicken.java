/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import main.GamePanel;

public class NPC_Chicken
extends Entity {
    public NPC_Chicken(GamePanel gp) {
        super(gp);
        this.direction = "down";
        this.speed = 0;
        this.getImage();
        this.setDialogue();
    }

    public void getImage() {
        this.left = this.setup("/npc/Chicken", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/npc/Chicken2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/npc/Chicken", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/npc/Chicken2", this.gp.tileSize, this.gp.tileSize);
        this.up = this.setup("/npc/Chicken", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/npc/Chicken2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/npc/Chicken", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/npc/Chicken2", this.gp.tileSize, this.gp.tileSize);
    }

    public void setDialogue() {
        this.dialogues[0][0] = "Pio Pio";
        this.dialogues[0][1] = "Take this chest Pio";
        this.dialogues[0][2] = "You can Pio be better..";
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

