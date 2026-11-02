/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import main.GamePanel;

public class NPC_Candle
extends Entity {
    public NPC_Candle(GamePanel gp) {
        super(gp);
        this.direction = "down";
        this.speed = 0;
        this.getImage();
        this.setDialogue();
    }

    public void getImage() {
        this.left = this.setup("/npc/Candly", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/npc/Candly2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/npc/Candly", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/npc/Candly2", this.gp.tileSize, this.gp.tileSize);
        this.up = this.setup("/npc/Candly", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/npc/Candly2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/npc/Candly", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/npc/Candly2", this.gp.tileSize, this.gp.tileSize);
    }

    public void setDialogue() {
        this.dialogues[0][0] = "This place is so rare";
        this.dialogues[0][1] = "Its like a dream??";
        this.dialogues[0][2] = "Dont hear me";
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

