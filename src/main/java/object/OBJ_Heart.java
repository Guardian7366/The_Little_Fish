/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Heart
extends Entity {
    GamePanel gp;
    public static final String objName = "Heart";

    public OBJ_Heart(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 7;
        this.name = objName;
        this.value = 2;
        this.down = this.setup("/objects/newHeartItem", gp.tileSize, gp.tileSize);
        this.image = this.setup("/objects/newHeartItem", gp.tileSize, gp.tileSize);
        this.image2 = this.setup("/objects/halfHeart", gp.tileSize, gp.tileSize);
        this.image3 = this.setup("/objects/deathHeart", gp.tileSize, gp.tileSize);
    }

    @Override
    public boolean use(Entity entity) {
        this.gp.playSE(12);
        this.gp.ui.addMessage("HP +" + this.value);
        entity.life += this.value;
        return true;
    }
}

