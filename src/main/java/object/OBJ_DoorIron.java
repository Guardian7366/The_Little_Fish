/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_DoorIron
extends Entity {
    GamePanel gp;
    public static final String objName = "Iron Door";

    public OBJ_DoorIron(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 8;
        this.name = objName;
        this.down = this.setup("/objects/ironDoor", gp.tileSize, gp.tileSize);
        this.collision = true;
        this.setDialogue();
    }

    public void setDialogue() {
        this.dialogues[0][0] = "Nothing happens";
    }

    @Override
    public void interact() {
        this.startDialogue(this, 0);
    }
}

