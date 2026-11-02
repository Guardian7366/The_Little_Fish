/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_WaterMana
extends Entity {
    GamePanel gp;
    public static final String objName = "Water Crystal";

    public OBJ_WaterMana(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 7;
        this.name = objName;
        this.value = 1;
        this.down = this.setup("/objects/newMana", gp.tileSize, gp.tileSize);
        this.image = this.setup("/objects/newMana", gp.tileSize, gp.tileSize);
        this.image2 = this.setup("/objects/newMana2", gp.tileSize, gp.tileSize);
    }

    @Override
    public boolean use(Entity entity) {
        this.gp.playSE(13);
        this.gp.ui.addMessage("Water +" + this.value);
        entity.water += this.value;
        return true;
    }
}

