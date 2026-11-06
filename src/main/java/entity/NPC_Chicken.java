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
        left = right = up = down = setup("/refugios/chicken/npc-0",gp.tileSize,gp.tileSize);
        left2 = right2 = up2 = down2 = setup("/refugios/chicken/npc-1",gp.tileSize,gp.tileSize);
    }

    public void setDialogue() {
        dialogues[0][0] = "Pio. D used to leave one light on\nafter dark.";
        dialogues[0][1] = "A small shadow would curl up in the\nwarm patch.";
        dialogues[0][2] = "Try both lanterns.";
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

