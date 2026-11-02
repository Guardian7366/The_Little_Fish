/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Projectile;
import entity.Entity;
import java.awt.Rectangle;
import java.awt.Color;
import main.GamePanel;

public class OBJ_Bad_Bubble
extends Projectile {
    GamePanel gp;
    private int originMap;
    public static final String objName = "Bad Bubble";

    public OBJ_Bad_Bubble(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.name = objName;
        this.speed = 8;
        this.life = this.maxLife = 30;
        this.attack = 3;
        this.useCost = 1;
        this.alive = false;
        solidArea.setBounds(16,16,16,16);
        solidAreaDefaultX = solidAreaDefaultY = 16;
        this.getImage();
    }

    @Override
    public void set(int x, int y, String direction, boolean alive, Entity owner) {
        super.set(x,y,direction,alive,owner);
        shooter = owner; originMap = gp.currentMap;
        spriteCounter = 0; spriteNum = 1; knockBack = false; invincible = false;
    }
    private Entity shooter;
    private int dx(String dir) { return dir.equals("left") ? -1 : dir.equals("right") ? 1 : 0; }
    private int dy(String dir) { return dir.equals("up") ? -1 : dir.equals("down") ? 1 : 0; }

    private boolean blocked(int x, int y, Entity owner) {
        Rectangle box = new Rectangle(x+16,y+16,16,16);
        int size=gp.tileSize;
        if(box.x<0 || box.y<0 || box.x+box.width>gp.maxWorldCol*size || box.y+box.height>gp.maxWorldRow*size) return true;
        for(int col=box.x/size;col<=(box.x+box.width-1)/size;col++)
            for(int row=box.y/size;row<=(box.y+box.height-1)/size;row++)
                if(gp.tileM.tile[gp.tileM.mapTileNum[gp.currentMap][col][row]].collision) return true;
        for(Entity e:gp.obj[gp.currentMap]) if(e!=null && e.collision && intersects(box,e)) return true;
        for(tile_interactive.InteractiveTile e:gp.iTile[gp.currentMap]) if(e!=null && (e.collision || e.destructible) && intersects(box,e)) return true;
        for(Entity e:gp.npc[gp.currentMap]) if(e!=null && intersects(box,e)) return true;
        for(Entity e:gp.monster[gp.currentMap]) if(e!=null && e!=owner && e.alive && !e.dying && intersects(box,e)) return true;
        return false;
    }
    private boolean intersects(Rectangle box, Entity e) {
        return box.intersects(new Rectangle(e.worldX+e.solidArea.x,e.worldY+e.solidArea.y,e.solidArea.width,e.solidArea.height));
    }
    public boolean clearShot(Entity owner, int distance) {
        for(int step=0;step<=distance;step++)
            if(blocked(owner.worldX+dx(owner.direction)*step,owner.worldY+dy(owner.direction)*step,owner)) return false;
        return true;
    }
    @Override
    public void update() {
        if(!alive) return;
        if(originMap!=gp.currentMap) { alive=false; return; }
        if(gp.gameState!=gp.playState) return;
        // Swept collision, one pixel at a time: walls win over a player behind them.
        for(int step=0;step<=speed;step++) {
            int x=worldX+dx(direction)*step,y=worldY+dy(direction)*step;
            if(blocked(x,y,shooter)) { alive=false; return; }
            if(intersects(new Rectangle(x+16,y+16,16,16),gp.player)) {
                worldX=x;worldY=y;
                if(!gp.player.invincible) damagePlayer(attack);
                generateParticle(this,this);
                alive=false; return;
            }
        }
        worldX+=dx(direction)*speed;worldY+=dy(direction)*speed;
        if(--life<=0) alive=false;
    }

    public void getImage() {
        this.up = this.setup("/proyectile/enemyPro1", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/proyectile/enemyPro1", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/proyectile/enemyPro1", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/proyectile/enemyPro1", this.gp.tileSize, this.gp.tileSize);
        this.left = this.setup("/proyectile/enemyPro1", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/proyectile/enemyPro1", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/proyectile/enemyPro1", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/proyectile/enemyPro1", this.gp.tileSize, this.gp.tileSize);
    }

    @Override
    public Color getparticleColor() {
        Color color = new Color(255, 69, 0);
        return color;
    }

    @Override
    public int getParticleSize() {
        int size = 10;
        return size;
    }

    @Override
    public int getParticleSpeed() {
        int speed = 1;
        return speed;
    }

    @Override
    public int getParticleMaxLife() {
        int maxLife = 10;
        return maxLife;
    }
}

