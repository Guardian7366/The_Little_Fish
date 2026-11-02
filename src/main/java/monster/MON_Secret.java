/*
 * Decompiled with CFR 0.152.
 */
package monster;

import entity.Entity;
import java.util.Random;
import main.GamePanel;
import object.OBJ_Coin;

public class MON_Secret
extends Entity {
    GamePanel gp;

    public MON_Secret(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 2;
        this.name = "Fhisty";
        this.speed = this.defaultSpeed = 1;
        this.life = this.maxLife = 100;
        this.attack = 1;
        this.defense = 0;
        this.exp = 100;
        this.solidArea.x = 0;
        this.solidArea.y = 0;
        this.solidArea.width = 48;
        this.solidArea.height = 48;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
        this.getImage();
    }

    public void getImage() {
        this.up = this.setup("/monster/SecretUp", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/monster/SecretUp2", this.gp.tileSize, this.gp.tileSize);
        this.left = this.setup("/monster/SecretLeft", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/monster/SecretLeft2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/monster/SecretDown", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/monster/SecretDown2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/monster/SecretRight", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/monster/SecretRigh2", this.gp.tileSize, this.gp.tileSize);
    }

    @Override
    public void setAction() {
        if (!this.onPath) {
            this.getRandomDirection(80);
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
            this.dropItem(new OBJ_Coin(this.gp));
        }
    }
}

