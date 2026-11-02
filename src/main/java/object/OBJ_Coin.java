/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Coin
extends Entity {
    GamePanel gp;
    public static final String objName = "Coin";

    public OBJ_Coin(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.name = objName;
        this.type = 7;
        this.down = this.setup("/objects/coinItem", gp.tileSize, gp.tileSize);
        this.value = 1;
        this.price = 1;
    }

    @Override
    public boolean use(Entity entity) {
        this.gp.playSE(2);
        this.gp.ui.addMessage("Coin +" + this.value);
        this.gp.player.coin += this.value;
        return true;
    }
}

