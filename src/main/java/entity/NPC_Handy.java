/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import main.GamePanel;

public class NPC_Handy
extends Entity {
    public NPC_Handy(GamePanel gp) {
        super(gp);
        this.direction = "down";
        this.speed = 0;
        this.getImage();
        this.setDialogue();
    }

    public void getImage() {
        left = right = up = down = setup("/refugios/handy/npc-0",gp.tileSize,gp.tileSize);
        left2 = right2 = up2 = down2 = setup("/refugios/handy/npc-1",gp.tileSize,gp.tileSize);
    }

    public void setDialogue() {
        dialogues[0][0] = "D kept knocking long after the bolt\nwent quiet.";
        dialogues[0][1] = "I could make a stronger tool. The\ndoor still wasn't mine to open.";
        dialogues[0][2] = "Equip Rocky. Both latches need two\nhits.";
        dialogues[1][0] = "Keep your hands steady, D. You can\nstill open what's ahead.";
    }

    @Override
    public void setAction() {
        this.getRandomDirection(120);
    }

    @Override
    public void speak() {
        facePlayer();
        startDialogue(this,gp.extraRooms.isOpen(main.ExtraRooms.Room.HANDY)?1:0);
    }
}

