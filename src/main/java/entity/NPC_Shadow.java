/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import main.GamePanel;

public class NPC_Shadow
extends Entity {
    public NPC_Shadow(GamePanel gp) {
        super(gp);
        this.direction = "left";
        this.speed = 0;
        this.dialogueSet = -1;
        this.getImage();
        this.setDialogue();
    }

    public void getImage() {
        this.left = this.setup("/npc/npcShadow", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/npc/npcShadow2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/npc/npcShadow", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/npc/npcShadow2", this.gp.tileSize, this.gp.tileSize);
        this.up = this.setup("/npc/npcShadow", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/npc/npcShadow2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/npc/npcShadow", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/npc/npcShadow2", this.gp.tileSize, this.gp.tileSize);
    }

    public void setDialogue() {
        dialogues[0][0] = "You move like a fish. For a moment, I\nthought you were someone else.";
        dialogues[0][1] = "Keep your guard up. You can strike or\nshield yourself.";
        dialogues[0][2] = "Look in the side rooms. What you find\nthere may help you further down.";
        dialogues[1][0] = "Rest at an emerald when you need to.";
        dialogues[1][1] = "This place keeps things better than\npeople do. Even things they'd rather\nforget.";
        dialogues[2][0] = "Strange. I knew your name before you\nhad a face, D.";
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

