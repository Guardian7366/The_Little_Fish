/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Fishbowl
extends Entity {
    GamePanel gp;
    public static final String objName = "bed";

    public OBJ_Fishbowl(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 6;
        this.name = objName;
        this.down = this.setup("/objects/bed", gp.tileSize, gp.tileSize);
        this.description = "[Bed]\n You can sleep \nbut cant forget.";
        this.price = 5;
        this.stackable = true;
    }

    @Override
    public boolean use(Entity entity) {
        this.gp.gameState = this.gp.sleepState;
        this.gp.playSE(17);
        this.gp.player.life = this.gp.player.maxLife;
        this.gp.player.water = this.gp.player.maxWater;
        this.gp.player.getSleepingImage(this.down);
        return true;
    }
}

