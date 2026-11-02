/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Key
extends Entity {
    GamePanel gp;
    public static final String objName = "Key";

    public OBJ_Key(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 6;
        this.name = objName;
        this.down = this.setup("/objects/keyItem", gp.tileSize, gp.tileSize);
        this.description = "[" + this.name + "]\nIt opens a door.";
        this.price = 3;
        this.stackable = true;
        this.setDialogue();
    }

    public void setDialogue() {
        this.dialogues[0][0] = "You use the " + this.name + " and open the door";
        this.dialogues[1][0] = "Why D?";
    }

    @Override
    public boolean use(Entity entity) {
        int objIndex = this.getDetected(entity, this.gp.obj, "Door");
        if (objIndex != 999) {
            this.startDialogue(this, 0);
            this.gp.playSE(3);
            this.gp.obj[this.gp.currentMap][objIndex] = null;
            return true;
        }
        this.startDialogue(this, 1);
        return false;
    }
}

