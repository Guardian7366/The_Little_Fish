/*
 * Decompiled with CFR 0.152.
 */
package main;

import entity.Entity;
import main.GamePanel;

public class CollisionChecker {
    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkTile(Entity entity) {
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;
        int entityLeftCol = entityLeftWorldX / this.gp.tileSize;
        int entityRightCol = entityRightWorldX / this.gp.tileSize;
        int entityTopRow = entityTopWorldY / this.gp.tileSize;
        int entityBottomRow = entityBottomWorldY / this.gp.tileSize;
        String direction = entity.direction;
        if (entity.knockBack) {
            direction = entity.knockBackDirection;
        }
        switch (direction) {
            case "up": {
                entityTopRow = (entityTopWorldY - entity.speed) / this.gp.tileSize;
                int tileNum1 = this.gp.tileM.mapTileNum[this.gp.currentMap][entityLeftCol][entityTopRow];
                int tileNum2 = this.gp.tileM.mapTileNum[this.gp.currentMap][entityRightCol][entityTopRow];
                if (!this.gp.tileM.tile[tileNum1].collision && !this.gp.tileM.tile[tileNum2].collision) break;
                entity.collisionOn = true;
                break;
            }
            case "down": {
                entityBottomRow = (entityBottomWorldY + entity.speed) / this.gp.tileSize;
                int tileNum1 = this.gp.tileM.mapTileNum[this.gp.currentMap][entityLeftCol][entityBottomRow];
                int tileNum2 = this.gp.tileM.mapTileNum[this.gp.currentMap][entityRightCol][entityBottomRow];
                if (!this.gp.tileM.tile[tileNum1].collision && !this.gp.tileM.tile[tileNum2].collision) break;
                entity.collisionOn = true;
                break;
            }
            case "left": {
                entityLeftCol = (entityLeftWorldX - entity.speed) / this.gp.tileSize;
                int tileNum1 = this.gp.tileM.mapTileNum[this.gp.currentMap][entityLeftCol][entityTopRow];
                int tileNum2 = this.gp.tileM.mapTileNum[this.gp.currentMap][entityLeftCol][entityBottomRow];
                if (!this.gp.tileM.tile[tileNum1].collision && !this.gp.tileM.tile[tileNum2].collision) break;
                entity.collisionOn = true;
                break;
            }
            case "right": {
                entityRightCol = (entityRightWorldX + entity.speed) / this.gp.tileSize;
                int tileNum1 = this.gp.tileM.mapTileNum[this.gp.currentMap][entityRightCol][entityTopRow];
                int tileNum2 = this.gp.tileM.mapTileNum[this.gp.currentMap][entityRightCol][entityBottomRow];
                if (!this.gp.tileM.tile[tileNum1].collision && !this.gp.tileM.tile[tileNum2].collision) break;
                entity.collisionOn = true;
            }
        }
    }

    public int checkObject(Entity entity, boolean player) {
        int index = 999;
        String direction = entity.direction;
        if (entity.knockBack) {
            direction = entity.knockBackDirection;
        }
        int i = 0;
        while (i < this.gp.obj[1].length) {
            block21: {
                if (!gp.extraRooms.visible(gp.obj[gp.currentMap][i])) break block21;
                entity.solidArea.x = entity.worldX + entity.solidArea.x;
                entity.solidArea.y = entity.worldY + entity.solidArea.y;
                this.gp.obj[this.gp.currentMap][i].solidArea.x = this.gp.obj[this.gp.currentMap][i].worldX + this.gp.obj[this.gp.currentMap][i].solidArea.x;
                this.gp.obj[this.gp.currentMap][i].solidArea.y = this.gp.obj[this.gp.currentMap][i].worldY + this.gp.obj[this.gp.currentMap][i].solidArea.y;
                switch (direction) {
                    case "up": {
                        entity.solidArea.y -= entity.speed;
                        break;
                    }
                    case "down": {
                        entity.solidArea.y += entity.speed;
                        break;
                    }
                    case "left": {
                        entity.solidArea.x -= entity.speed;
                        break;
                    }
                    case "right": {
                        entity.solidArea.x += entity.speed;
                    }
                }
                if (entity.solidArea.intersects(this.gp.obj[this.gp.currentMap][i].solidArea)) {
                    if (this.gp.obj[this.gp.currentMap][i].collision) {
                        entity.collisionOn = true;
                    }
                    if (player) {
                        index = i;
                    }
                }
                entity.solidArea.x = entity.solidAreaDefaultX;
                entity.solidArea.y = entity.solidAreaDefaultY;
                this.gp.obj[this.gp.currentMap][i].solidArea.x = this.gp.obj[this.gp.currentMap][i].solidAreaDefaultX;
                this.gp.obj[this.gp.currentMap][i].solidArea.y = this.gp.obj[this.gp.currentMap][i].solidAreaDefaultY;
            }
            ++i;
        }
        return index;
    }

    public int checkEntity(Entity entity, Entity[][] target) {
        return checkEntity(entity,target,gp.extraRooms.active());
    }

    public int checkEntity(Entity entity,Entity[][] target,ExtraRooms.Room room) {
        int index = 999;
        String direction = entity.direction;
        if (entity.knockBack) {
            direction = entity.knockBackDirection;
        }
        int i = 0;
        while (i < target[1].length) {
            block19: {
                if (!gp.extraRooms.visible(target[gp.currentMap][i],room)) break block19;
                if (target[gp.currentMap][i] instanceof monster.MON_GodFish boss
                        && (entity!=gp.player || entity.attacking || !boss.protectedBody())) break block19;
                entity.solidArea.x = entity.worldX + entity.solidArea.x;
                entity.solidArea.y = entity.worldY + entity.solidArea.y;
                target[this.gp.currentMap][i].solidArea.x = target[this.gp.currentMap][i].worldX + target[this.gp.currentMap][i].solidArea.x;
                target[this.gp.currentMap][i].solidArea.y = target[this.gp.currentMap][i].worldY + target[this.gp.currentMap][i].solidArea.y;
                switch (direction) {
                    case "up": {
                        entity.solidArea.y -= entity.speed;
                        break;
                    }
                    case "down": {
                        entity.solidArea.y += entity.speed;
                        break;
                    }
                    case "left": {
                        entity.solidArea.x -= entity.speed;
                        break;
                    }
                    case "right": {
                        entity.solidArea.x += entity.speed;
                    }
                }
                if (entity.solidArea.intersects(target[this.gp.currentMap][i].solidArea) && target[this.gp.currentMap][i] != entity) {
                    entity.collisionOn = true;
                    index = i;
                }
                entity.solidArea.x = entity.solidAreaDefaultX;
                entity.solidArea.y = entity.solidAreaDefaultY;
                target[this.gp.currentMap][i].solidArea.x = target[this.gp.currentMap][i].solidAreaDefaultX;
                target[this.gp.currentMap][i].solidArea.y = target[this.gp.currentMap][i].solidAreaDefaultY;
            }
            ++i;
        }
        return index;
    }

    public boolean checkPlayer(Entity entity) {
        boolean contactPlayer = false;
        entity.solidArea.x = entity.worldX + entity.solidArea.x;
        entity.solidArea.y = entity.worldY + entity.solidArea.y;
        this.gp.player.solidArea.x = this.gp.player.worldX + this.gp.player.solidArea.x;
        this.gp.player.solidArea.y = this.gp.player.worldY + this.gp.player.solidArea.y;
        switch (entity.direction) {
            case "up": {
                entity.solidArea.y -= entity.speed;
                break;
            }
            case "down": {
                entity.solidArea.y += entity.speed;
                break;
            }
            case "left": {
                entity.solidArea.x -= entity.speed;
                break;
            }
            case "right": {
                entity.solidArea.x += entity.speed;
            }
        }
        if (entity.solidArea.intersects(this.gp.player.solidArea)) {
            entity.collisionOn = true;
            contactPlayer = true;
        }
        entity.solidArea.x = entity.solidAreaDefaultX;
        entity.solidArea.y = entity.solidAreaDefaultY;
        this.gp.player.solidArea.x = this.gp.player.solidAreaDefaultX;
        this.gp.player.solidArea.y = this.gp.player.solidAreaDefaultY;
        return contactPlayer;
    }
}

