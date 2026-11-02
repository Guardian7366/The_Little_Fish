/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Lantern
extends Entity {
    public static final String objName = "Lantern";

    public OBJ_Lantern(GamePanel gp) {
        super(gp);
        this.type = 9;
        this.name = objName;
        this.down = this.setup("/objects/lintern", gp.tileSize, gp.tileSize);
        this.description = "[Lantern]\nIlluminates your \n destiny.";
        this.price = 3;
        this.lightRadius = 400;
    }
}

