/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Rock
extends Entity {
    public static final String objName = "Rocky";

    public OBJ_Rock(GamePanel gp) {
        super(gp);
        this.type = 4;
        this.name = objName;
        this.down = this.setup("/objects/rockItem", gp.tileSize, gp.tileSize);
        this.attackValue = 1;
        this.attackArea.width = 25;
        this.attackArea.height = 30;
        this.description = "[" + this.name + "]\nJust a rock for rocks.";
        this.price = 2;
        this.motion1_duration = 5;
        this.motion2_duration = 25;
    }
}

