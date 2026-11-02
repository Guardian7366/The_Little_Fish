/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import main.GamePanel;

public class NPC_Pump
extends Entity {
    public NPC_Pump(GamePanel gp) {
        super(gp);
        this.direction = "down";
        this.speed = 0;
        this.getImage();
        this.setDialogue();
    }

    public void getImage() {
        this.left = this.setup("/npc/npcPump", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/npc/npcPump2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/npc/npcPump", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/npc/npcPump2", this.gp.tileSize, this.gp.tileSize);
        this.up = this.setup("/npc/npcPump", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/npc/npcPump2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/npc/npcPump", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/npc/npcPump2", this.gp.tileSize, this.gp.tileSize);
    }

    public void setDialogue() {
        this.dialogues[0][0] = "Im pumpkim you know?";
        this.dialogues[0][1] = "But well, just take this chest";
        this.dialogues[0][2] = "! - !";
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

