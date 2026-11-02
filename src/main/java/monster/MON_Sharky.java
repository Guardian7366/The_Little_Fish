/*
 * Decompiled with CFR 0.152.
 */
package monster;

import entity.Entity;
import java.util.Random;
import main.GamePanel;
import object.OBJ_Coin;
import object.OBJ_WaterMana;

public class MON_Sharky
extends Entity {
    GamePanel gp;

    public MON_Sharky(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 2;
        this.name = "Sharky";
        this.speed = this.defaultSpeed = 6;
        this.life = this.maxLife = 20;
        this.attack = 9;
        this.defense = 0;
        this.exp = 28;
        this.solidArea.x = 0;
        this.solidArea.y = 0;
        this.solidArea.width = 42;
        this.solidArea.height = 42;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
        this.getImage();
    }

    public void getImage() {
        this.up = this.setup("/monster/sharkyUp", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/monster/sharkyUp2", this.gp.tileSize, this.gp.tileSize);
        this.left = this.setup("/monster/sharkyLeft", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/monster/sharkyLeft2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/monster/sharkyDown", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/monster/sharkyDown2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/monster/sharkyRight", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/monster/sharkyRight2", this.gp.tileSize, this.gp.tileSize);
    }

    @Override
    public void setAction() {
        if (this.onPath) {
            this.checkStopChasingOrNot(this.gp.player, 15, 20);
            this.searchPath(this.getGoalCol(this.gp.player), this.getGoalRow(this.gp.player));
        } else {
            this.checkStartChasingOrNot(this.gp.player, 5, 20);
            this.getRandomDirection(80);
        }
    }

    @Override
    public void damageReaction() {
        this.actionLockCounter = 0;
        this.onPath = true;
    }

    @Override
    public void checkDrop() {
        int i = new Random().nextInt(75) + 1;
        if (i < 50) {
            this.dropItem(new OBJ_Coin(this.gp));
        }
        if (i >= 50 && i < 75) {
            this.dropItem(new OBJ_WaterMana(this.gp));
        }
    }
}

