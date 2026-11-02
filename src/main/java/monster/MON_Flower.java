/*
 * Decompiled with CFR 0.152.
 */
package monster;

import entity.Entity;
import java.util.Random;
import main.GamePanel;
import object.OBJ_Coin;
import object.OBJ_Heart;
import object.OBJ_WaterMana;

public class MON_Flower
extends Entity {
    GamePanel gp;

    public MON_Flower(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 2;
        this.name = "Flowe";
        this.speed = this.defaultSpeed = 3;
        this.life = this.maxLife = 30;
        this.attack = 20;
        this.defense = 10;
        this.exp = 30;
        this.knockBackPower = 5;
        this.solidArea.x = 4;
        this.solidArea.y = 4;
        this.solidArea.width = 40;
        this.solidArea.height = 44;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
        this.attackArea.width = 48;
        this.attackArea.height = 48;
        this.motion1_duration = 40;
        this.motion2_duration = 85;
        this.getImage();
        this.getAttackImage();
    }

    public void getImage() {
        this.up = this.setup("/monster/BadFlower", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/monster/BadFlower2", this.gp.tileSize, this.gp.tileSize);
        this.left = this.setup("/monster/BadFlower", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/monster/BadFlower2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/monster/BadFlower", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/monster/BadFlower2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/monster/BadFlower", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/monster/BadFlower2", this.gp.tileSize, this.gp.tileSize);
    }

    public void getAttackImage() {
        this.attackUp1 = this.setup("/monster/flowerUp1", this.gp.tileSize, this.gp.tileSize * 2);
        this.attackUp2 = this.setup("/monster/flowerUp2", this.gp.tileSize, this.gp.tileSize * 2);
        this.attackDown1 = this.setup("/monster/flowerDown1", this.gp.tileSize, this.gp.tileSize * 2);
        this.attackDown2 = this.setup("/monster/flowerDown2", this.gp.tileSize, this.gp.tileSize * 2);
        this.attackLeft1 = this.setup("/monster/flowerLeft2", this.gp.tileSize * 2, this.gp.tileSize);
        this.attackLeft2 = this.setup("/monster/flowerLeft1", this.gp.tileSize * 2, this.gp.tileSize);
        this.attackRight1 = this.setup("/monster/flowerRight1", this.gp.tileSize * 2, this.gp.tileSize);
        this.attackRight2 = this.setup("/monster/flowerRight2", this.gp.tileSize * 2, this.gp.tileSize);
    }

    @Override
    public void setAction() {
        if (this.onPath) {
            this.checkStopChasingOrNot(this.gp.player, 15, 100);
            this.searchPath(this.getGoalCol(this.gp.player), this.getGoalRow(this.gp.player));
        } else {
            this.checkStartChasingOrNot(this.gp.player, 10, 100);
            this.getRandomDirection(80);
        }
        if (!this.attacking) {
            this.checkAttackOrNot(50, this.gp.tileSize * 4, this.gp.tileSize);
        }
    }

    @Override
    public void damageReaction() {
        this.actionLockCounter = 0;
        this.onPath = true;
    }

    @Override
    public void checkDrop() {
        int i = new Random().nextInt(100) + 1;
        if (i < 50) {
            this.dropItem(new OBJ_Coin(this.gp));
        }
        if (i >= 50 && i < 75) {
            this.dropItem(new OBJ_Heart(this.gp));
        }
        if (i >= 75 && i < 100) {
            this.dropItem(new OBJ_WaterMana(this.gp));
        }
    }
}

