/*
 * Decompiled with CFR 0.152.
 */
package monster;

import entity.Entity;
import java.util.Random;
import main.GamePanel;
import object.OBJ_Bad_Bubble;
import object.OBJ_Heart;

public class MON_Enfadillo
extends Entity {
    GamePanel gp;
    private int shotCooldown;

    public MON_Enfadillo(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 2;
        this.name = "Angry";
        this.speed = this.defaultSpeed = 1;
        this.life = this.maxLife = 20;
        this.attack = 4;
        this.defense = 1;
        this.exp = 7;
        this.projectile = new OBJ_Bad_Bubble(gp);
        this.solidArea.x = 0;
        this.solidArea.y = 0;
        this.solidArea.width = 45;
        this.solidArea.height = 45;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
        this.getImage();
    }

    public void getImage() {
        this.up = this.setup("/monster/Endadillo", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/monster/EnfadilloMove", this.gp.tileSize, this.gp.tileSize);
        this.left = this.setup("/monster/Endadillo", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/monster/EnfadilloMove", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/monster/Endadillo", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/monster/EnfadilloMove", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/monster/Endadillo", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/monster/EnfadilloMove", this.gp.tileSize, this.gp.tileSize);
    }

    @Override
    public void setAction() {
        if ((gp.currentMap == 1 || gp.currentMap == 2)) {
            onPath = false;
            int dx = gp.player.getCenterX() - getCenterX();
            int dy = gp.player.getCenterY() - getCenterY();
            direction = Math.abs(dx) >= Math.abs(dy) ? (dx < 0 ? "left" : "right") : (dy < 0 ? "up" : "down");
            shotCooldown = Math.min(120, shotCooldown + 1);
            int distance = Math.max(Math.abs(dx), Math.abs(dy));
            if (gp.gameState == gp.playState && shotCooldown >= 120 && !projectile.alive &&
                distance <= 4 * gp.tileSize && Math.min(Math.abs(dx), Math.abs(dy)) <= 16 &&
                ((OBJ_Bad_Bubble)projectile).clearShot(this, distance)) {
                for (int slot=0; slot<gp.projectile[gp.currentMap].length; slot++) {
                    if (gp.projectile[gp.currentMap][slot] != null) continue;
                    projectile.set(worldX, worldY, direction, true, this);
                    gp.projectile[gp.currentMap][slot] = projectile;
                    shotCooldown = 0;
                    gp.playSE(26);
                    break;
                }
            }
            return;
        }
        if (this.onPath) {
            this.checkStopChasingOrNot(this.gp.player, 10, 20);
            this.searchPath(this.getGoalCol(this.gp.player), this.getGoalRow(this.gp.player));
        } else {
            this.checkStartChasingOrNot(this.gp.player, 10, 20);
            this.getRandomDirection(120);
        }
    }

    @Override
    protected void moveInDirection(String direction) {
        if ((gp.currentMap != 1 && gp.currentMap != 2)) super.moveInDirection(direction);
    }

    @Override
    public void damageReaction() {
        this.actionLockCounter = 0;
        this.onPath = (gp.currentMap != 1 && gp.currentMap != 2);
    }

    @Override
    public void checkDrop() {
        int i = new Random().nextInt(100) + 1;
        if (i < 50) {
            this.dropItem(new OBJ_Heart(this.gp));
        }
    }
}

