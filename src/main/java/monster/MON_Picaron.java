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

public class MON_Picaron
extends Entity {
    GamePanel gp;
    private int homeX, homeY;
    private boolean territorySet, returning;

    public void setHome(int x, int y) {
        homeX=x; homeY=y; territorySet=true; onPath=false;
    }

    private boolean inside(int x, int y) {
        return Math.abs(x-homeX)<=4*gp.tileSize && Math.abs(y-homeY)<=4*gp.tileSize;
    }

    @Override
    protected void moveInDirection(String movement) {
        if ((gp.currentMap==2 || gp.currentMap==main.FourthZone.MAP) && territorySet) {
            int x=worldX+(movement.equals("right")?speed:movement.equals("left")?-speed:0);
            int y=worldY+(movement.equals("down")?speed:movement.equals("up")?-speed:0);
            if (!inside(x,y)) { returning=true; onPath=false; if(knockBack) finishKnockBack(); return; }
        }
        super.moveInDirection(movement);
    }

    private void territoryAction() {
        if(!territorySet) setHome(worldX,worldY);
        int distance=getTileDistance(gp.player);
        if(onPath && (distance>7 || !inside(gp.player.worldX,gp.player.worldY))) {
            onPath=false; returning=true;
        }
        if(returning) {
            if(Math.abs(worldX-homeX)+Math.abs(worldY-homeY)<gp.tileSize/2) {returning=false; speed=0;}
            else {
                speed=defaultSpeed;
                if(Math.abs(worldX-homeX)+Math.abs(worldY-homeY)<gp.tileSize) {
                    int dx=homeX-worldX,dy=homeY-worldY;
                    direction=Math.abs(dx)>Math.abs(dy)?(dx<0?"left":"right"):(dy<0?"up":"down");
                } else searchPath(homeX/gp.tileSize,homeY/gp.tileSize);
            }
            return;
        }
        if(!onPath && distance<=5 && inside(gp.player.worldX,gp.player.worldY) && visiblePlayer()) onPath=true;
        if(onPath) {
            speed=defaultSpeed;
            searchPath(getGoalCol(gp.player),getGoalRow(gp.player));
            if(!attacking) checkAttackOrNot(50,gp.tileSize*4,gp.tileSize);
        } else speed=0;
    }

    private boolean visiblePlayer() {
        int dx=gp.player.getCenterX()-getCenterX(),dy=gp.player.getCenterY()-getCenterY();
        int steps=Math.max(Math.abs(dx),Math.abs(dy))/12+1;
        for(int i=1;i<steps;i++) {
            int col=(getCenterX()+dx*i/steps)/gp.tileSize,row=(getCenterY()+dy*i/steps)/gp.tileSize;
            if(col<0||row<0||col>=gp.maxWorldCol||row>=gp.maxWorldRow||gp.tileM.tile[gp.tileM.mapTileNum[gp.currentMap][col][row]].collision) return false;
            for(var tile:gp.iTile[gp.currentMap]) if(tile!=null && tile.destructible && tile.worldX/gp.tileSize==col && tile.worldY/gp.tileSize==row) return false;
        }
        return true;
    }


    public MON_Picaron(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 2;
        this.name = "Purpli";
        this.speed = this.defaultSpeed = 2;
        this.life = this.maxLife = 20;
        this.attack = 5;
        this.defense = 2;
        this.exp = 15;
        this.knockBackPower = 2;
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
        this.up = this.setup("/monster/PicaronLeft", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/monster/PicaronLeft2", this.gp.tileSize, this.gp.tileSize);
        this.left = this.setup("/monster/PicaronLeft", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/monster/PicaronLeft2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/monster/PicaronDown", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/monster/PicaronDown2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/monster/PicaronRight", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/monster/PicaronRight2", this.gp.tileSize, this.gp.tileSize);
    }

    public void getAttackImage() {
        this.attackUp1 = this.setup("/monster/PicaronAUp1", this.gp.tileSize, this.gp.tileSize * 2);
        this.attackUp2 = this.setup("/monster/PicaronAUp2", this.gp.tileSize, this.gp.tileSize * 2);
        this.attackDown1 = this.setup("/monster/PicaronADown1", this.gp.tileSize, this.gp.tileSize * 2);
        this.attackDown2 = this.setup("/monster/PicaronADown2", this.gp.tileSize, this.gp.tileSize * 2);
        this.attackLeft1 = this.setup("/monster/PicaronALeft1", this.gp.tileSize * 2, this.gp.tileSize);
        this.attackLeft2 = this.setup("/monster/PicaronALeft2", this.gp.tileSize * 2, this.gp.tileSize);
        this.attackRight1 = this.setup("/monster/PicaronARight1", this.gp.tileSize * 2, this.gp.tileSize);
        this.attackRight2 = this.setup("/monster/PicaronARight2", this.gp.tileSize * 2, this.gp.tileSize);
    }

    @Override
    public void setAction() {
        if((gp.currentMap==2 || gp.currentMap==main.FourthZone.MAP)) { territoryAction(); return; }
        if (this.onPath) {
            this.checkStopChasingOrNot(this.gp.player, 18, 100);
            this.searchPath(this.getGoalCol(this.gp.player), this.getGoalRow(this.gp.player));
        } else {
            this.checkStartChasingOrNot(this.gp.player, 18, 100);
            this.getRandomDirection(100);
        }
        if (!this.attacking) {
            this.checkAttackOrNot(50, this.gp.tileSize * 4, this.gp.tileSize);
        }
    }

    @Override
    public void damageReaction() {
        this.actionLockCounter = 0;
        this.onPath = (gp.currentMap!=2 && gp.currentMap!=main.FourthZone.MAP) || (territorySet && inside(gp.player.worldX,gp.player.worldY));
    }

    @Override
    public void checkDrop() {
        int i = new Random().nextInt(100) + 1;
        if (i < 50) {
            this.dropItem(new OBJ_Coin(this.gp));
        }
        if (i >= 50 && i < 75) {
            this.dropItem(new OBJ_WaterMana(this.gp));
        }
        if (i >= 75 && i < 100) {
            this.dropItem(new OBJ_Heart(this.gp));
        }
    }
}

