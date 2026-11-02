/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import main.GamePanel;
import object.OBJ_HealHP;

public class NPC_TV
extends Entity {
    public NPC_TV(GamePanel gp) {
        super(gp);
        this.direction = "down";
        this.speed = 0;
        this.getImage();
        this.setDialogue();
        this.setItems();
    }

    public void getImage() {
        this.left = this.setup("/npc/TV", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/npc/TV2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/npc/TV", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/npc/TV2", this.gp.tileSize, this.gp.tileSize);
        this.up = this.setup("/npc/TV", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/npc/TV2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/npc/TV", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/npc/TV2", this.gp.tileSize, this.gp.tileSize);
    }

    public void setDialogue() {
        this.dialogues[0][0] = "Hello, im a TV!! \nNeed something D?";
        this.dialogues[1][0] = "See you, D";
        this.dialogues[2][0] = "You need more coins";
        this.dialogues[3][0] = "You need more space";
        this.dialogues[4][0] = "You cannot sell an equipped item :c";
    }

    public void setItems() {
        this.inventory.add(new OBJ_HealHP(this.gp));
    }

    @Override
    public void speak() {
        this.facePlayer();
        this.gp.gameState = this.gp.tradeState;
        this.gp.ui.npc = this;
    }
}

