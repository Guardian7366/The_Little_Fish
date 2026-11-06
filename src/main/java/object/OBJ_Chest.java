/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Chest
extends Entity {
    GamePanel gp;
    public static final String objName = "Chest";

    public OBJ_Chest(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 8;
        this.name = objName;
        this.image = this.setup("/objects/chest", gp.tileSize, gp.tileSize);
        this.image2 = this.setup("/objects/openChest", gp.tileSize, gp.tileSize);
        this.down = this.image;
        this.collision = true;
        this.solidArea.x = 4;
        this.solidArea.y = 16;
        this.solidArea.width = 40;
        this.solidArea.height = 32;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
    }

    @Override
    public void setLoot(Entity loot) {
        this.loot = loot;
        this.setDialogue();
    }

    public void setDialogue() {
        dialogues[0][0] = "You cannot carry anymore.";
        dialogues[1][0] = "You found " + loot.name + "!";
        dialogues[2][0] = "It's empty.";
    }

    @Override
    public void interact() {
        if (!this.opened) {
            this.gp.playSE(3);
            if (!this.gp.player.canObtainItem(this.loot)) {
                this.startDialogue(this, 0);
            } else {
                this.startDialogue(this, 1);
                this.down = this.image2;
                this.opened = true;
            }
        } else {
            this.startDialogue(this, 2);
        }
    }
}

