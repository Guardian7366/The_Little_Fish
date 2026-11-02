/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Bust
extends Entity {
    public static final String objName = "Bust";

    public OBJ_Bust(GamePanel gp) {
        super(gp);
        this.name = objName;
        this.down = this.setup("/objects/speedItem", gp.tileSize, gp.tileSize);
        this.collision = true;
    }
}

