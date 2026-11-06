package main;

import data.Progress;
import entity.Entity;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import monster.MON_GodFish;
import object.OBJ_DoorIron;
import object.OBJ_Iron;
import object.OBJ_Key;
import object.OBJ_Rock;

/** Geometry, arrival, presentation and ownership shared by the final encounter and its save migration. */
public final class FinalArena {
    public static final int MAP=6, REVISION=2, FIRST_TILE=182, BOSS_SLOT=87, SUMMON_SLOT=86;
    public static final int ARRIVAL_X=24, ARRIVAL_Y=44, RETURN_X=23;
    public static final int THRESHOLD_Y=38, SEAL_Y=39, SAFE_Y=41;
    public static final int DOOR_SLOT=9, DOOR_X=26, DOOR_Y=12, EXIT_X=26, EXIT_Y=5;
    public static final int KEY_SLOT=28, SEAL_SLOT=29, KEY_X=24, KEY_Y=32;
    public static final int BOSS_X=25*48, BOSS_Y=28*48+24, INTRO_Y=35*48;
    private static final BufferedImage FAR = background("far-trench");
    private static final BufferedImage SIDES = background("basalt-sides");
    private static final BufferedImage SEDIMENT = background("sediment");
    private static final BufferedImage COLUMN = prop("broken-column"), RUBBLE = prop("basalt-rubble"), SILT = prop("silt");
    private static final BufferedImage NORTH_ARCH = SpriteCache.load("/septima/tiles/return-arch.png",48,48);
    private static final int[][] COLUMNS={{16,20},{33,20},{16,36},{33,36},{21,42},{28,45},{26,15}};
    private final GamePanel gp;
    private int tick, cameraY;
    private boolean warned;

    FinalArena(GamePanel gp) { this.gp = gp; }

    public MON_GodFish boss() {
        return gp.monster[MAP][BOSS_SLOT] instanceof MON_GodFish boss ? boss : null;
    }

    boolean interact() {
        if (gp.currentMap!=MAP || gp.gameState!=gp.playState || Progress.GodFishDefeated) return false;
        int row = (gp.player.worldY+24)/48;
        if (row!=THRESHOLD_Y) warned = false;
        if (gp.bossBattleOn || row!=THRESHOLD_Y || (gp.player.worldX+24)/48!=ARRIVAL_X || !"up".equals(gp.player.direction)
                || gp.player.worldY+gp.player.solidArea.y+gp.player.solidArea.height>SEAL_Y*48) return false;
        boolean rocky = gp.player.inventory.stream().anyMatch(OBJ_Rock.class::isInstance);
        boolean iron = gp.player.inventory.stream().anyMatch(OBJ_Iron.class::isInstance);
        if (!rocky || !iron || boss()==null) {
            gp.player.collisionOn = true;
            gp.player.attackCanceled = true;
            if (!warned) {
                gp.ui.addMessage(boss()==null ? "The trench is quiet. You can return to TV Supply."
                    : "Bring " + (!rocky && !iron ? "Rocky and Iron" : rocky ? "Iron" : "Rocky") + ". Return to TV Supply.");
                warned = true;
            }
            return true;
        }
        gp.player.collisionOn = gp.player.attackCanceled = true;
        gp.csManager.beginFinalIntro();
        return true;
    }

    void seal() {
        Entity door = Temple.place(new OBJ_DoorIron(gp), ARRIVAL_X, SEAL_Y);
        door.temp = true;
        door.solidArea.setSize(48,48);
        gp.obj[MAP][SEAL_SLOT] = door;
        gp.playSE(3);
    }

    public void releaseSeal() {
        Entity door = gp.obj[MAP][SEAL_SLOT];
        if (door instanceof OBJ_DoorIron && door.temp && door.worldX==ARRIVAL_X*48 && door.worldY==SEAL_Y*48)
            gp.obj[MAP][SEAL_SLOT] = null;
    }

    public void clearHazards() {
        gp.monster[MAP][SUMMON_SLOT] = null;
        for (Entity p : gp.projectile[MAP]) if (p!=null) p.alive = false;
        gp.particleList.clear();
    }

    public void abandon() {
        if (gp.bossBattleOn && !Progress.GodFishDefeated) {
            clearHazards();
            gp.monster[MAP][BOSS_SLOT] = new MON_GodFish(gp);
        }
        gp.bossBattleOn = false;
        releaseSeal();
        warned = false;
    }

    public void victory() {
        if (Progress.GodFishDefeated) return;
        Progress.GodFishDefeated = true;
        gp.bossBattleOn = false;
        clearHazards();
        releaseSeal();
        gp.obj[MAP][KEY_SLOT] = Temple.place(new OBJ_Key(gp), KEY_X, KEY_Y);
        gp.ui.addMessage("God Fish defeated. Take the key.");
        gp.ui.addMessage("Exp + 500!");
        gp.player.exp = (int)Math.min(Integer.MAX_VALUE, (long)gp.player.exp+500);
        gp.player.checkLevelUp();
        gp.stopMusic();
        gp.playMusic(22);
    }

    public boolean clear(Rectangle body) {
        if (!Temple.terrainClear(gp,MAP,body)) return false;
        for (Entity object : gp.obj[MAP])
            if (object!=null && object.collision && body.intersects(Temple.bounds(object,object.worldX,object.worldY)))
                return false;
        return true;
    }

    public boolean visibleFrom(int x, int y, int targetX, int targetY) {
        int steps = Math.max(1, Math.max(Math.abs(targetX-x), Math.abs(targetY-y))/4);
        for (int i=0; i<=steps; i++)
            if (!clear(new Rectangle(x+(targetX-x)*i/steps,y+(targetY-y)*i/steps,1,1))) return false;
        return true;
    }

    int combatCameraY() {
        return cameraY;
    }

    int targetCameraY() {
        var boss = boss();
        int focus = boss==null ? gp.player.worldY : boss.cameraFocusY();
        int playerY=gp.player.worldY+24;
        int framing=playerY>focus+220?285:245;
        int y=(playerY+focus)/2-framing;
        return Math.max(gp.player.worldY-448,Math.min(gp.player.worldY-36,y));
    }

    void beginCombatCamera() { cameraY = targetCameraY(); }

    void advanceIntroPlayer() {
        var player=gp.player;
        int next=Math.max(INTRO_Y,player.worldY-4);
        if (next>=player.worldY || !clear(Temple.bounds(player,player.worldX,next))) return;
        player.worldY=next;
        player.spriteNum=(next/24)%2+1;
    }

    public boolean pushPlayer(Rectangle body,int dx,int dy) {
        var player=gp.player;
        if (!body.intersects(Temple.bounds(player,player.worldX,player.worldY))) return true;
        int sx=Integer.compare(dx,0), sy=Integer.compare(dy,0);
        if (sx==0 && sy==0) sy=1;
        for (int[] direction : new int[][]{{sx,sy},{-sy,sx},{sy,-sx}}) {
            for (int n=1;n<=12;n++) {
                int x=player.worldX+direction[0]*n,y=player.worldY+direction[1]*n;
                var next=Temple.bounds(player,x,y);
                if (!clear(next)) break;
                Entity summon=gp.monster[MAP][SUMMON_SLOT];
                if (summon!=null && next.intersects(Temple.bounds(summon,summon.worldX,summon.worldY))) break;
                if (!next.intersects(body)) {
                    player.worldX=x;
                    player.worldY=y;
                    return true;
                }
            }
        }
        return false;
    }

    void update() {
        tick = (tick+1)%3840;
        if (gp.bossBattleOn && boss()!=null && !boss().holdsCamera()) {
            int delta=targetCameraY()-cameraY;
            cameraY+=Math.max(-5,Math.min(5,delta));
        }
    }

    void background(Graphics2D g) {
        g.drawImage(FAR,0,0,null);
        g.drawImage(SIDES,0,0,null);
    }

    void details(Graphics2D g) {
        for (int[] p : COLUMNS)
            g.drawImage(COLUMN,p[0]*48-gp.cameraX(),p[1]*48-gp.cameraY(),null);
        g.drawImage(RUBBLE,16*48-gp.cameraX(),36*48-gp.cameraY(),null);
        g.drawImage(RUBBLE,33*48-gp.cameraX(),36*48-gp.cameraY(),null);
        g.drawImage(SILT,26*48-gp.cameraX(),14*48-gp.cameraY(),null);
        g.drawImage(NORTH_ARCH,26*48-gp.cameraX(),11*48-gp.cameraY(),null);
        g.drawImage(NORTH_ARCH,24*48-gp.cameraX(),37*48-gp.cameraY(),null);
        if (boss()!=null) boss().drawFloor(g);
    }

    void foreground(Graphics2D g) {
        int offset = tick/8;
        g.drawImage(SEDIMENT,0,-offset,null);
        g.drawImage(SEDIMENT,0,576-offset,null);
        if (boss()!=null) boss().drawEffects(g);
    }

    void prompt(Graphics2D g) {
        if (gp.gameState!=gp.playState || gp.bossBattleOn || Progress.GodFishDefeated) return;
        if (Math.abs(gp.player.worldY-THRESHOLD_Y*48)>110) return;
        var copy = (Graphics2D)g.create();
        copy.setColor(new Color(9,19,31,230));
        copy.fillRoundRect(229,476,502,38,10,10);
        copy.setColor(new Color(178,230,220));
        copy.setFont(gp.ui.purisaB.deriveFont(18f));
        copy.drawString("The trench ahead   |   Bring Rocky and Iron",251,501);
        copy.dispose();
    }

    private static BufferedImage background(String name) { return GodFishArt.scaled(GodFishArt.load("fosa/background/"+name)); }
    private static BufferedImage prop(String name) { return GodFishArt.scaled(GodFishArt.load("fosa/props/"+name)); }
}
