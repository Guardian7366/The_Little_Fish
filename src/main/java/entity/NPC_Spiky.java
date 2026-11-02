/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import main.GamePanel;

public class NPC_Spiky
extends Entity {
    public NPC_Spiky(GamePanel gp) {
        super(gp);
        this.direction = "down";
        this.speed = 0;
        this.getImage();
        this.setDialogue();
    }

    public void getImage() {
        this.left = this.setup("/npc/Spiky", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/npc/Spiky2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/npc/Spiky", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/npc/Spiky2", this.gp.tileSize, this.gp.tileSize);
        this.up = this.setup("/npc/Spiky", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/npc/Spiky2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/npc/Spiky", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/npc/Spiky2", this.gp.tileSize, this.gp.tileSize);
    }

    public void setDialogue() {
        this.dialogues[0][0] = "Sp- Sp- Sp-";
        this.dialogues[0][1] = "Th- Th- Th-";
        this.dialogues[0][2] = "Ch- Ch- Ch-";
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

