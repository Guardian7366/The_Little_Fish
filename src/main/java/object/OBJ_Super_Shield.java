/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Super_Shield
extends Entity {
    public static final String objName = "Super Bubble";

    public OBJ_Super_Shield(GamePanel gp) {
        super(gp);
        this.type = 5;
        this.name = objName;
        this.down = this.setup("/objects/shieldItem", gp.tileSize, gp.tileSize);
        this.defenseValue = 3;
        this.description = "[" + this.name + "]\nJust a super bubble.";
        this.price = 7;
    }
}

