/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Second_Weapon
extends Entity {
    public static final String objName = "Toxic Bubble";

    public OBJ_Second_Weapon(GamePanel gp) {
        super(gp);
        this.type = 3;
        this.name = objName;
        this.down = this.setup("/objects/secondWeapon", gp.tileSize, gp.tileSize);
        this.attackValue = 3;
        this.attackArea.width = 49;
        this.attackArea.height = 36;
        this.description = "[" + this.name + "]\nJust a toxic bubble.";
        this.price = 7;
        this.knockBackPower = 10;
        this.motion1_duration = 3;
        this.motion2_duration = 15;
    }
}

