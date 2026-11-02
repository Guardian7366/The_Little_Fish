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

public class MON_Normalillo
extends Entity {
    GamePanel gp;
    private int minX, minY, maxX, maxY;
    private boolean territorial;

    /** Optional encounter bounds; other maps retain the original movement. */
    public void setTerritory(int minX, int minY, int maxX, int maxY) {
        this.minX = minX; this.minY = minY; this.maxX = maxX; this.maxY = maxY;
        territorial = true;
    }

    @Override
    protected void moveInDirection(String movement) {
        if (!territorial) { super.moveInDirection(movement); return; }
        int nextX = worldX, nextY = worldY;
        switch (movement) {
            case "up" -> nextY -= speed;
            case "down" -> nextY += speed;
            case "left" -> nextX -= speed;
            case "right" -> nextX += speed;
        }
        if (nextX < minX || nextX > maxX || nextY < minY || nextY > maxY) {
            direction = switch (movement) {
                case "up" -> "down"; case "down" -> "up";
                case "left" -> "right"; default -> "left";
            };
            if (knockBack) finishKnockBack();
            return;
        }
        super.moveInDirection(movement);
    }

    public MON_Normalillo(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 2;
        this.name = "Greeny";
        this.speed = this.defaultSpeed = 1;
        this.life = this.maxLife = 4;
        this.attack = 2;
        this.defense = 0;
        this.exp = 1;
        this.solidArea.x = 0;
        this.solidArea.y = 0;
        this.solidArea.width = 48;
        this.solidArea.height = 48;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
        this.getImage();
    }

    public void getImage() {
        this.up = this.setup("/monster/NormalilloUp", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/monster/NormalilloUp2", this.gp.tileSize, this.gp.tileSize);
        this.left = this.setup("/monster/Normalillobase", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/monster/Normalillobase2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/monster/NormalilloDown", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/monster/NormalilloDown2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/monster/NormalilloRight", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/monster/NormalilloRight2", this.gp.tileSize, this.gp.tileSize);
    }

    @Override
    public void setAction() {
        if (!this.onPath) {
            this.getRandomDirection(100);
        }
    }

    @Override
    public void damageReaction() {
        this.actionLockCounter = 0;
        this.direction = this.gp.player.direction;
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

