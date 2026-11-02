/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Weapon_Normal
extends Entity {
    public static final String objName = "Normal buuble";

    public OBJ_Weapon_Normal(GamePanel gp) {
        super(gp);
        this.type = 3;
        this.name = objName;
        this.down = this.setup("/objects/weaponNormal", gp.tileSize, gp.tileSize);
        this.attackValue = 2;
        this.attackArea.width = 40;
        this.attackArea.height = 36;
        this.description = "[" + this.name + "]\nJust a normal bubble.";
        this.price = 1;
        this.knockBackPower = 2;
        this.motion1_duration = 5;
        this.motion2_duration = 25;
    }
}

