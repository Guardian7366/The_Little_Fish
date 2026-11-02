/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Normal_Shield
extends Entity {
    public static final String objName = "Bubble shield";

    public OBJ_Normal_Shield(GamePanel gp) {
        super(gp);
        this.type = 5;
        this.name = objName;
        this.down = this.setup("/objects/normalShield", gp.tileSize, gp.tileSize);
        this.defenseValue = 1;
        this.description = "[" + this.name + "]\nJust a normal bubble.";
        this.price = 2;
    }
}

