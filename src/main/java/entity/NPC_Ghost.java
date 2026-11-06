/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import main.GamePanel;

public class NPC_Ghost
extends Entity {
    public NPC_Ghost(GamePanel gp) {
        super(gp);
        this.direction = "down";
        this.speed = 0;
        this.getImage();
        this.setDialogue();
    }

    public void getImage() {
        left = right = up = down = setup("/refugios/ghost/npc-0",gp.tileSize,gp.tileSize);
        left2 = right2 = up2 = down2 = setup("/refugios/ghost/npc-1",gp.tileSize,gp.tileSize);
    }

    public void setDialogue() {
        dialogues[0][0] = "Two sets of footsteps used to stop\nat this door.";
        dialogues[0][1] = "One day, I heard the other set carry\non past it.";
        dialogues[0][2] = "I heard the promise they made to D.\nThey were making it to someone else.";
        dialogues[0][3] = "Use the pedestal. Watch the marks,\nthen follow them.";
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

