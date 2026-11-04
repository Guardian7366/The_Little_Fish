/*
 * Decompiled with CFR 0.152.
 */
package monster;

import entity.Entity;
import java.util.Random;
import main.GamePanel;
import object.OBJ_Coin;
import object.OBJ_WaterMana;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class MON_Sharky
extends Entity {
    GamePanel gp;
    public enum State { LURK, PREPARE, CHARGE, RECOVER, STUNNED, RETURN }
    public static final int PREPARE_TICKS=30, RECOVER_TICKS=45, STUN_TICKS=75, COOLDOWN_TICKS=90;
    private State state=State.LURK;
    private int ticks, cooldown, travelled, homeX, homeY, encounter=-1, stuckTicks;
    private boolean homeSet, impacted;
    private Rectangle territory;
    private final BufferedImage[][] prepare=new BufferedImage[4][2], charge=new BufferedImage[4][2];
    private final BufferedImage[] stunned=new BufferedImage[2];

    public State state() { return state; }
    public int stateTicks() { return ticks; }
    public int travelled() { return travelled; }

    public void setHome(int x,int y,Rectangle bounds,int group) {
        homeX=x; homeY=y; territory=new Rectangle(bounds); encounter=group; homeSet=true;
        resetAmbush();
    }
    public void resetAmbush() {
        state=State.LURK; ticks=travelled=stuckTicks=0; cooldown=COOLDOWN_TICKS;
        impacted=false; onPath=attacking=knockBack=offBalance=false; speed=defaultSpeed;
    }
    private void enter(State next) { state=next; ticks=0; }
    private boolean inside(int x,int y) { return territory.contains(x,y); }
    private boolean visible() {
        int x=getScreenX(),y=getScreenY();
        return x>=0 && y>=0 && x+gp.tileSize<=gp.screenWidht && y+gp.tileSize<=gp.screenHeight;
    }
    private Rectangle body(Entity e,int x,int y) {
        return new Rectangle(x+e.solidAreaDefaultX,y+e.solidAreaDefaultY,e.solidArea.width,e.solidArea.height);
    }
    /** Immutable hitbox queries: no mutation of shared rectangles and no tunnelling. */
    private boolean blocked(int x,int y,boolean allies) {
        Rectangle r=body(this,x,y);
        if(r.x<0||r.y<0||r.x+r.width>=gp.maxWorldCol*gp.tileSize||r.y+r.height>=gp.maxWorldRow*gp.tileSize) return true;
        for(int col=r.x/gp.tileSize;col<=(r.x+r.width-1)/gp.tileSize;col++)
            for(int row=r.y/gp.tileSize;row<=(r.y+r.height-1)/gp.tileSize;row++)
                if(gp.tileM.tile[gp.tileM.mapTileNum[gp.currentMap][col][row]].collision) return true;
        for(Entity e:gp.obj[gp.currentMap]) if(e!=null&&e.collision&&r.intersects(body(e,e.worldX,e.worldY))) return true;
        for(var e:gp.iTile[gp.currentMap]) if(e!=null&&e.destructible&&r.intersects(body(e,e.worldX,e.worldY))) return true;
        for(Entity e:gp.npc[gp.currentMap]) if(e!=null&&r.intersects(body(e,e.worldX,e.worldY))) return true;
        if(allies) for(Entity e:gp.monster[gp.currentMap])
            if(e!=null&&e!=this&&e.alive&&!e.dying&&r.intersects(body(e,e.worldX,e.worldY))) return true;
        return false;
    }
    private boolean lineClear() {
        int dx=gp.player.getCenterX()-getCenterX(),dy=gp.player.getCenterY()-getCenterY();
        int n=Math.max(Math.abs(dx),Math.abs(dy));
        for(int i=1;i<=n;i++) if(blocked(worldX+dx*i/Math.max(1,n),worldY+dy*i/Math.max(1,n),false)) return false;
        return true;
    }
    private boolean targetReady() {
        int dx=gp.player.getCenterX()-getCenterX(),dy=gp.player.getCenterY()-getCenterY();
        return visible() && inside(gp.player.worldX,gp.player.worldY) &&
            Math.max(Math.abs(dx),Math.abs(dy))<=5*gp.tileSize &&
            Math.min(Math.abs(dx),Math.abs(dy))<gp.tileSize/2 && lineClear();
    }
    @Override public void facePlayer() {
        int dx=gp.player.getCenterX()-getCenterX(),dy=gp.player.getCenterY()-getCenterY();
        direction=Math.abs(dx)>=Math.abs(dy)?(dx<0?"left":"right"):(dy<0?"up":"down");
    }
    private boolean encounterFree() {
        if(encounter<0) return true;
        for(Entity e:gp.monster[gp.currentMap]) if(e instanceof MON_Sharky s && s!=this && s.alive&&!s.dying&&
            s.encounter==encounter && s.state!=State.LURK && s.state!=State.RETURN) return false;
        return true;
    }
    private boolean step(String dir,boolean charging) {
        int x=worldX+(dir.equals("right")?1:dir.equals("left")?-1:0);
        int y=worldY+(dir.equals("down")?1:dir.equals("up")?-1:0);
        if(!inside(x,y)||blocked(x,y,true)) return false;
        if(body(this,x,y).intersects(body(gp.player,gp.player.worldX,gp.player.worldY))) {
            if(charging) damagePlayer(attack);
            return false;
        }
        worldX=x;worldY=y; return true;
    }
    @Override public void update() {
        if(gp.gameState!=gp.playState||sleep) return;
        if(!alive||dying||life<=0) { resetAmbush(); return; }
        if(!homeSet) setHome(worldX,worldY,new Rectangle(worldX-4*gp.tileSize,worldY-4*gp.tileSize,8*gp.tileSize+1,8*gp.tileSize+1),-1);
        updateCombatTimers();
        if(cooldown>0) cooldown--;
        if(knockBack) {
            String dir=knockBackDirection;
            for(int i=0;i<Math.min(speed,8);i++) if(!step(dir,false)) break;
            finishKnockBack(); enter(State.STUNNED);
        }
        ticks++;
        switch(state) {
            case LURK -> {
                if(cooldown==0 && targetReady() && encounterFree()) {facePlayer();enter(State.PREPARE);}
                else if(Math.abs(worldX-homeX)+Math.abs(worldY-homeY)>gp.tileSize) enter(State.RETURN);
                else {
                    // A short, slow visible patrol, never a map-wide pursuit.
                    int patrolX=homeX+((ticks/90)%2==0?12:0);
                    if(worldX!=patrolX) {direction=worldX<patrolX?"right":"left";step(direction,false);advanceWalkAnimation(24);}
                }
            }
            case PREPARE -> {
                if(!visible()||!lineClear()||!inside(gp.player.worldX,gp.player.worldY)) {enter(State.RECOVER);break;}
                if(ticks>=PREPARE_TICKS) {facePlayer();travelled=0;impacted=false;enter(State.CHARGE);}
            }
            case CHARGE -> {
                for(int i=0;i<8 && state==State.CHARGE;i++) {
                    if(!step(direction,true)) {if(state==State.CHARGE) enter(State.STUNNED);break;}
                    if(++travelled>=5*gp.tileSize) enter(State.RECOVER);
                }
            }
            case RECOVER, STUNNED -> {
                if(ticks>=(state==State.STUNNED?STUN_TICKS:RECOVER_TICKS)) {
                    cooldown=COOLDOWN_TICKS;offBalance=false;enter(State.RETURN);
                }
            }
            case RETURN -> {
                if(Math.abs(worldX-homeX)+Math.abs(worldY-homeY)<=2) {enter(State.LURK);break;}
                int oldX=worldX,oldY=worldY;
                direction=Math.abs(worldX-homeX)>=Math.abs(worldY-homeY)?(worldX>homeX?"left":"right"):(worldY>homeY?"up":"down");
                if(!step(direction,false)) {
                    searchPath(homeX/gp.tileSize,homeY/gp.tileSize);
                    step(direction,false);
                }
                if(oldX==worldX&&oldY==worldY) {
                    // A player/ally can occupy home. Retry later without teleporting or healing.
                    if(++stuckTicks>=120) {stuckTicks=0;cooldown=COOLDOWN_TICKS;enter(State.LURK);}
                } else stuckTicks=0;
            }
        }
        speed=defaultSpeed;
        if(state==State.CHARGE || state==State.RETURN) advanceWalkAnimation(state==State.CHARGE?6:24);
        else spriteNum=1;
    }
    @Override public void damagePlayer(int value) {
        if(state!=State.CHARGE||impacted||dying||!alive) return;
        impacted=true;
        boolean parry=!gp.player.invincible && gp.player.guarding && gp.player.guardCounter<10 &&
            gp.player.direction.equals(getOppositeDirection(direction));
        super.damagePlayer(value);
        finishKnockBack(); enter(parry?State.STUNNED:State.RECOVER);
    }
    @Override public void checkCollision() { /* Movement and contact are swept in update(). */ }
    @Override public void updateVisualState() {
        if(gp.gameState!=gp.playState) return;
        if(dying||!alive||life<=0) resetAmbush();
        super.updateVisualState();
    }
    @Override public void draw(Graphics2D g) {
        super.draw(g);
        if(!alive||dying||!inCamera()) return;
        int d=switch(direction){case "right"->0;case "down"->1;case "left"->2;default->3;};
        BufferedImage overlay=switch(state){
            case PREPARE->prepare[d][ticks/8%2];case CHARGE->charge[d][ticks/6%2];
            case STUNNED->stunned[ticks/12%2];default->null;
        };
        if(overlay!=null) g.drawImage(overlay,getScreenX()-24,getScreenY()-24,null);
    }

    public MON_Sharky(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 2;
        this.name = "Sharky";
        this.speed = this.defaultSpeed = 1;
        this.life = this.maxLife = 20;
        this.attack = 9;
        this.defense = 0;
        this.exp = 28;
        this.solidArea.x = 0;
        this.solidArea.y = 0;
        this.solidArea.width = 42;
        this.solidArea.height = 42;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
        this.getImage();
        String[] dirs={"right","down","left","up"};
        for(int d=0;d<4;d++) for(int f=0;f<2;f++) {
            prepare[d][f]=setup("/quinta/sharky/prepare-"+dirs[d]+"-"+f,96,96);
            charge[d][f]=setup("/quinta/sharky/charge-"+dirs[d]+"-"+f,96,96);
        }
        for(int f=0;f<2;f++) stunned[f]=setup("/quinta/sharky/stunned-"+f,96,96);
    }

    public void getImage() {
        this.up = this.setup("/monster/sharkyUp", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/monster/sharkyUp2", this.gp.tileSize, this.gp.tileSize);
        this.left = this.setup("/monster/sharkyLeft", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/monster/sharkyLeft2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/monster/sharkyDown", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/monster/sharkyDown2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/monster/sharkyRight", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/monster/sharkyRight2", this.gp.tileSize, this.gp.tileSize);
    }

    @Override
    public void setAction() {
        // State machine is advanced only by update(), once per active tick.
    }

    @Override
    public void damageReaction() {
        this.actionLockCounter = 0;
        enter(State.RECOVER);
    }

    @Override
    public void checkDrop() {
        int i = new Random().nextInt(75) + 1;
        if (i < 50) {
            this.dropItem(new OBJ_Coin(this.gp));
        }
        if (i >= 50 && i < 75) {
            this.dropItem(new OBJ_WaterMana(this.gp));
        }
    }
}

