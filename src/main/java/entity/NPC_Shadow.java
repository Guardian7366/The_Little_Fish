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
        this.dialogues[0][0] = "Hello, traveller";
        this.dialogues[0][1] = "So you\u00b4ve fall into this uh?";
        this.dialogues[0][2] = "Well i can help you to go into this thing";
        this.dialogues[0][3] = "You can attack and defend yourself \nfrom enemies.";
        this.dialogues[0][4] = "Also try kill them all hahah";
        this.dialogues[0][5] = "This way you can level up";
        this.dialogues[0][6] = "You will find many objects that \nwill help you, believe me";
        this.dialogues[1][0] = "If you start to forget,\nrest in the emerald squares";
        this.dialogues[1][1] = "If you want to get out of here,\ndefeat yourself";
        this.dialogues[1][2] = "?           ?                ?";
        this.dialogues[1][3] = "What?";
        this.dialogues[1][4] = "Why do you see me that way?";
        this.dialogues[2][0] = "Forget the past D...";
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

