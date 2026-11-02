/*
 * Decompiled with CFR 0.152.
 */
package monster;

import entity.Entity;
import java.util.Random;
import main.GamePanel;
import object.OBJ_Heart;

public class MON_Tella
extends Entity {
    GamePanel gp;

    public MON_Tella(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 2;
        this.name = "Fhisty";
        this.speed = this.defaultSpeed = 10;
        this.life = this.maxLife = 100;
        this.attack = 10;
        this.defense = 10;
        this.exp = 30;
        this.solidArea.x = 0;
        this.solidArea.y = 0;
        this.solidArea.width = 44;
        this.solidArea.height = 44;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
        this.getImage();
    }

    public void getImage() {
        this.up = this.setup("/monster/Fishtella", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/monster/Fishtella2", this.gp.tileSize, this.gp.tileSize);
        this.left = this.setup("/monster/Fishtella", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/monster/Fishtella2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/monster/Fishtella", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/monster/Fishtella2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/monster/Fishtella", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/monster/Fishtella2", this.gp.tileSize, this.gp.tileSize);
    }

    @Override
    public void setAction() {
        if (!this.onPath) {
            this.getRandomDirection(60);
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

