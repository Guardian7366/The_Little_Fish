/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import main.GamePanel;

public class NPC_Ic
extends Entity {
    public NPC_Ic(GamePanel gp) {
        super(gp);
        this.direction = "down";
        this.speed = 0;
        this.getImage();
        this.setDialogue();
        this.dialogueSet = -1;
    }

    public void getImage() {
        left = right = up = down = setup("/refugios/ic/npc-0",gp.tileSize,gp.tileSize);
        left2 = right2 = up2 = down2 = setup("/refugios/ic/npc-1",gp.tileSize,gp.tileSize);
    }

    public void setDialogue() {
        dialogues[0][0] = "The small prints came more slowly\nevery winter.";
        dialogues[0][1] = "D waited by the cold step until they\ncaught up.";
        dialogues[0][2] = "Slide to the two marks. Rough stone\nstops the slide.";
        dialogues[1][0] = "The ice has thawed. I can still tell\nyou where the prints were.";
    }

    @Override
    public void setAction() {
        if (this.onPath) {
            int goalCol = (this.gp.player.worldX + this.gp.player.solidArea.x) / this.gp.tileSize;
            int goalRow = (this.gp.player.worldY + this.gp.player.solidArea.y) / this.gp.tileSize;
            this.searchPath(goalCol, goalRow);
        } else {
            this.getRandomDirection(120);
        }
    }

    @Override
    public void speak() {
        facePlayer();
        startDialogue(this,gp.extraRooms.isOpen(main.ExtraRooms.Room.IC)?1:0);
    }
}

