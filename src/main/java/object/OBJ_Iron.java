/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Iron
extends Entity {
    public static final String objName = "Iron";

    public OBJ_Iron(GamePanel gp) {
        super(gp);
        this.type = 10;
        this.name = objName;
        this.down = this.setup("/objects/ironItem", gp.tileSize, gp.tileSize);
        this.attackValue = 2;
        this.attackArea.width = 25;
        this.attackArea.height = 30;
        this.description = "[Iron]\nJust a iron for iron.";
        this.price = 2;
        this.motion1_duration = 5;
        this.motion2_duration = 25;
    }
}

