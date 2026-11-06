/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import main.GamePanel;

public class Projectile
extends Entity {
    Entity user;

    public Projectile(GamePanel gp) {
        super(gp);
    }

    public void set(int worldX, int worldY, String direction, boolean alive, Entity user) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.direction = direction;
        this.alive = alive;
        this.user = user;
        this.life = this.maxLife;
    }

    @Override
    public void update() {
        if (gp.currentMap==main.FinalArena.MAP && user==gp.player) {
            updateTrenchShot();
            return;
        }
        int monsterIndex;
        if (this.user == this.gp.player && (monsterIndex = this.gp.cChecker.checkEntity(this, this.gp.monster)) != 999) {
            this.gp.player.damageMonster(monsterIndex, this, this.attack * Math.max(1, this.gp.player.level / 2), this.knockBackPower);
            this.generateParticle(this.user.projectile, this.gp.monster[this.gp.currentMap][monsterIndex]);
            this.alive = false;
        }
        if (this.user != this.gp.player) {
            boolean contactPlayer = this.gp.cChecker.checkPlayer(this);
            if (!this.gp.player.invincible && contactPlayer) {
                this.damagePlayer(this.attack);
                this.generateParticle(this.user.projectile, this.user.projectile);
                this.alive = false;
            }
        }
        switch (this.direction) {
            case "up": {
                this.worldY -= this.speed;
                break;
            }
            case "down": {
                this.worldY += this.speed;
                break;
            }
            case "left": {
                this.worldX -= this.speed;
                break;
            }
            case "right": {
                this.worldX += this.speed;
            }
        }
        --this.life;
        if (this.life <= 0) {
            this.alive = false;
        }
        ++this.spriteCounter;
        if (this.spriteCounter > 12) {
            if (this.spriteNum == 1) {
                this.spriteNum = 2;
            } else if (this.spriteNum == 2) {
                this.spriteNum = 1;
            }
            this.spriteCounter = 0;
        }
    }

    private void updateTrenchShot() {
        int dx = "left".equals(direction) ? -1 : "right".equals(direction) ? 1 : 0;
        int dy = "up".equals(direction) ? -1 : "down".equals(direction) ? 1 : 0;
        int damage = attack*Math.max(1,gp.player.level/2);
        for (int i=0; i<=speed; i++) {
            var body = new java.awt.Rectangle(worldX+16,worldY+16,16,16);
            if (!gp.finalArena.clear(body)) { alive=false; return; }
            var boss = gp.finalArena.boss();
            if (boss!=null && boss.hit(body,worldX+24,worldY+24,null,damage)) { alive=false; return; }
            for (int slot=0; slot<gp.monster[gp.currentMap].length; slot++) {
                Entity enemy = gp.monster[gp.currentMap][slot];
                if (slot!=main.FinalArena.BOSS_SLOT && enemy!=null && enemy.alive && !enemy.dying
                        && body.intersects(main.Temple.bounds(enemy,enemy.worldX,enemy.worldY))) {
                    gp.player.damageMonster(slot,this,damage,knockBackPower);
                    generateParticle(this,enemy);
                    alive=false;
                    return;
                }
            }
            if (i<speed) { worldX+=dx; worldY+=dy; }
        }
        if (--life<=0) alive=false;
        if (++spriteCounter>12) {
            spriteNum=3-spriteNum;
            spriteCounter=0;
        }
    }

    public boolean haveResourse(Entity user) {
        boolean haveResource = false;
        return haveResource;
    }

    public void substractResource(Entity user) {
    }
}

