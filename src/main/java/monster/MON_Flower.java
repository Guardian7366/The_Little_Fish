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

    public enum Vine { IDLE, AIM, EXTEND, PULL, RETRACT }
    private Vine vine = Vine.IDLE;
    private int ticks, length, limit, cutFlash;
    private final String[] directions = {"up","down","left","right"};
    private java.awt.image.BufferedImage[] idle, aim, mid, tip;
    private java.awt.image.BufferedImage retract, cutSprite;

    public boolean templePlant() { return gp.currentMap == main.Temple.MAP; }
    public Vine vine() { return vine; }
    public int vineLength() { return length; }

    public void prepareTemple() {
        if (idle != null) return;
        idle = new java.awt.image.BufferedImage[]{art("idle-0"),art("idle-1")};
        aim = new java.awt.image.BufferedImage[4];
        mid = new java.awt.image.BufferedImage[4];
        tip = new java.awt.image.BufferedImage[4];
        for (int i=0; i<4; i++) {
            aim[i]=art("aim-"+directions[i]);
            mid[i]=art("vine-mid-"+directions[i]);
            tip[i]=art("vine-tip-"+directions[i]);
        }
        retract=art("retract"); cutSprite=art("vine-cut");
    }

    private java.awt.image.BufferedImage art(String name) {
        return setup("/septima/flower/"+name,48,48);
    }
    private int dx() { return direction.equals("right")?1:direction.equals("left")?-1:0; }
    private int dy() { return direction.equals("down")?1:direction.equals("up")?-1:0; }

    private java.awt.Rectangle segment(int span) {
        int x=worldX+24, y=worldY+24;
        if (dx()!=0) return new java.awt.Rectangle(x+(dx()<0?-span:0),y-8,span,16);
        return new java.awt.Rectangle(x-8,y+(dy()<0?-span:0),16,span);
    }

    private int reach() {
        for (int span=4; span<=120; span+=4)
            if (main.Temple.vineBlocked(gp,segment(span))) return span-4;
        return 120;
    }

    public void resetVine() {
        gp.temple.release(this);
        vine=Vine.IDLE; ticks=length=cutFlash=0;
        finishKnockBack();
    }

    private void release(boolean cut) {
        gp.temple.release(this);
        vine=Vine.RETRACT; ticks=0;
        if (cut) cutFlash=12;
    }

    public boolean cut(java.awt.Rectangle hit) {
        if (!alive || dying || (vine!=Vine.EXTEND && vine!=Vine.PULL) || !segment(length).intersects(hit)) return false;
        release(true);
        return true;
    }

    @Override public void update() {
        if (!templePlant()) { super.update(); return; }
        if (gp.gameState!=gp.playState || !alive || dying || life<=0) return;
        prepareTemple();
        updateCombatTimers();
        if (knockBack) finishKnockBack();
        if (cutFlash>0) cutFlash--;
        ticks++;
        switch (vine) {
            case IDLE -> {
                if (ticks<45 || !inCamera()) return;
                var player=main.Temple.bounds(gp.player,gp.player.worldX,gp.player.worldY);
                int x=(int)player.getCenterX()-worldX-24,y=(int)player.getCenterY()-worldY-24;
                if (Math.max(Math.abs(x),Math.abs(y))>110 || Math.min(Math.abs(x),Math.abs(y))>15) return;
                direction=Math.abs(x)>=Math.abs(y)?(x<0?"left":"right"):(y<0?"up":"down");
                limit=reach();
                if (!segment(limit).intersects(player)) return;
                vine=Vine.AIM; ticks=0;
            }
            case AIM -> {
                if (ticks>=36) { vine=Vine.EXTEND; ticks=0; }
            }
            case EXTEND -> {
                limit=Math.min(limit,reach());
                length=Math.min(limit,length+12);
                var player=main.Temple.bounds(gp.player,gp.player.worldX,gp.player.worldY);
                if (length>0 && segment(length).intersects(player)) {
                    boolean parry=gp.player.guarding && gp.player.guardCounter<10 &&
                        gp.player.direction.equals(getOppositeDirection(direction));
                    if (parry) { gp.playSE(19); release(false); }
                    else if (gp.temple.attach(this)) { vine=Vine.PULL; ticks=0; }
                    else release(false);
                } else if (length>=limit) release(false);
            }
            case PULL -> {
                if (!gp.temple.attached(this) || ticks>=24 || gp.player.life<=0) { release(false); return; }
                var player=main.Temple.bounds(gp.player,gp.player.worldX,gp.player.worldY);
                int distance=dx()!=0?Math.abs((int)player.getCenterX()-worldX-24)
                    :Math.abs((int)player.getCenterY()-worldY-24);
                if (distance<=36) {
                    damagePlayer(attack,direction,false);
                    release(false);
                    return;
                }
                int nextX=gp.player.worldX-dx()*4,nextY=gp.player.worldY-dy()*4;
                if (!main.Temple.clear(gp,gp.player,nextX,nextY) ||
                    main.Temple.portalOverlap(main.Temple.bounds(gp.player,nextX,nextY)) ||
                    !segment(Math.min(limit,reach())).intersects(player)) { release(false); return; }
                gp.player.worldX=nextX; gp.player.worldY=nextY;
                length=Math.min(limit,distance);
            }
            case RETRACT -> {
                length=Math.max(0,length-10);
                if (ticks>=20) { vine=Vine.IDLE; ticks=0; }
            }
        }
    }

    @Override public void draw(java.awt.Graphics2D g) {
        if (!templePlant() || idle==null) { super.draw(g); return; }
        if (!inCamera()) return;
        int facing=java.util.List.of(directions).indexOf(direction);
        int x=getScreenX(),y=getScreenY();
        if (!dying && length>0) {
            java.awt.Graphics2D line=(java.awt.Graphics2D)g.create();
            var clip=segment(length);
            clip.translate(x-worldX,y-worldY);
            line.clip(clip);
            for (int offset=0; offset<length; offset+=48) {
                boolean last=offset+48>=length;
                int position=last?length-24:offset+24;
                line.drawImage(last?tip[facing]:mid[facing],x+dx()*position,y+dy()*position,null);
            }
            line.dispose();
        }
        var image=switch(vine) {
            case IDLE -> idle[ticks/15%2];
            case RETRACT -> retract;
            default -> aim[facing];
        };
        java.awt.Graphics2D copy=(java.awt.Graphics2D)g.create();
        if (invincible) changeAlpha(copy,0.4f);
        if (dying) dyingAnimation(copy);
        copy.drawImage(image,x,y,null);
        if (cutFlash>0 && !dying) copy.drawImage(cutSprite,x+dx()*24,y+dy()*24,null);
        copy.dispose();
    }

    @Override public void updateVisualState() {
        if (templePlant()) {
            if (dying || !alive) resetVine();
            if (gp.gameState!=gp.playState) return;
        }
        super.updateVisualState();
    }


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
        if (templePlant()) {
            if (life<=0) resetVine();
            return;
        }
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

