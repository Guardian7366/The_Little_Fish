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
        left = right = up = down = setup("/refugios/flower/npc-0",gp.tileSize,gp.tileSize);
        left2 = right2 = up2 = down2 = setup("/refugios/flower/npc-1",gp.tileSize,gp.tileSize);
    }

    public void setDialogue() {
        dialogues[0][0] = "D asked me to put aside a second\ndose.";
        dialogues[0][1] = "When D returned, the bottle was still\nsealed.";
        dialogues[0][2] = "The Greeny guards the remedy.";
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

