/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_HealHP
extends Entity {
    GamePanel gp;
    public static final String objName = "Red Poti";
    int value = 5;

    public OBJ_HealHP(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 6;
        this.name = objName;
        this.down = this.setup("/objects/potionHeal", gp.tileSize, gp.tileSize);
        this.defenseValue = 3;
        this.description = "[" + this.name + "]\nHeals by " + this.value + ".";
        this.price = 4;
        this.stackable = true;
        this.setDialogue();
    }

    public void setDialogue() {
        this.dialogues[0][0] = "You use the " + this.name + "!\nYour life has been recovered by " + this.value + ".";
    }

    @Override
    public boolean use(Entity entity) {
        this.startDialogue(this, 0);
        entity.life += this.value;
        this.gp.playSE(12);
        return true;
    }
}

