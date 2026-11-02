/*
 * Decompiled with CFR 0.152.
 */
package monster;

import entity.Entity;
import java.util.Random;
import main.GamePanel;
import object.OBJ_Heart;

public class MON_Bomb
extends Entity {
    GamePanel gp;

    public MON_Bomb(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 2;
        this.name = "Bomby";
        this.speed = this.defaultSpeed = 0;
        this.life = this.maxLife = 1000;
        this.attack = 999;
        this.defense = 200;
        this.exp = 0;
        this.solidArea.x = 0;
        this.solidArea.y = 0;
        this.solidArea.width = 44;
        this.solidArea.height = 44;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
        this.getImage();
    }

    public void getImage() {
        this.up = this.setup("/monster/Bomb", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/monster/Bomb2", this.gp.tileSize, this.gp.tileSize);
        this.left = this.setup("/monster/Bomb", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/monster/Bomb2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/monster/Bomb", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/monster/Bomb2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/monster/Bomb", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/monster/Bomb2", this.gp.tileSize, this.gp.tileSize);
    }

    @Override
    public void setAction() {
        if (!this.onPath) {
            this.getRandomDirection(120);
        }
    }

    @Override
    public void damageReaction() {
        this.actionLockCounter = 0;
    }

    @Override
    public void checkDrop() {
        int i = new Random().nextInt(50) + 1;
        if (i < 50) {
            this.dropItem(new OBJ_Heart(this.gp));
        }
    }
}

