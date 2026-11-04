package main;

import entity.Entity;
import java.awt.Rectangle;
import monster.MON_Picaron;
import monster.MON_Sharky;
import tile_interactive.*;

/** Fifth in traversal, still internal map 3. Stable slots preserve defeated enemies. */
public final class FifthZone {
    public static final int MAP=3, REVISION=2, WALL=88, GLOW_TOP=89;
    public static final int[] PICARON_SLOTS={33,36,39};
    public static final int[][] PICARONS={{6,32},{17,34},{28,36}};
    public static final int[][] SHARKS={{38,22},{30,10},{9,29},{35,33},{22,41},{29,41}};
    // Body origins must stay a tile inside the chamber boundary.
    private static final int[][] TERRITORIES={{33,18,42,26},{25,7,34,13},{6,26,13,33},{31,32,38,35},{19,39,31,43},{19,39,31,43}};
    // Original slots 4..10 are stone, 11..13 metal. Retain two stones, migrate three metals;
    // four genuinely new metal blocks get fresh slots, never resurrecting an old destroyed block.
    public static final int[] GATE_SLOTS={21,22};
    public static final int[][] GATE={{16,43},{16,44}};
    public static final int[] TILE_SLOTS={4,5,11,12,13,17,18,19,20,21,22};
    public static final int[][] OBSTACLES={{39,36},{40,36},{14,7},{15,7},{20,26},{20,27},{28,30},{29,30},{30,30},{16,43},{16,44}};
    public static final int[][] LIGHTS={{25,19},{34,18},{41,26},{30,7},{11,7},{16,17},{7,26},{15,33},{29,32},{21,39},{30,43},{15,44}};
    private FifthZone() {}

    public static void populate(GamePanel gp,Entity[][] monsters) {
        for(int slot=33;slot<=48;slot++) monsters[MAP][slot]=null;
        for(int i=0;i<PICARONS.length;i++) monsters[MAP][PICARON_SLOTS[i]]=new MON_Picaron(gp);
        for(int i=0;i<SHARKS.length;i++) monsters[MAP][40+i]=new MON_Sharky(gp);
        configure(gp,monsters,true);
    }
    public static void configure(GamePanel gp,Entity[][] monsters,boolean move) {
        for(int i=0;i<SHARKS.length;i++) if(monsters[MAP][40+i] instanceof MON_Sharky s) {
            int x=SHARKS[i][0]*48,y=SHARKS[i][1]*48;
            if(move){s.worldX=x;s.worldY=y;}
            int[] t=TERRITORIES[i];
            s.setHome(x,y,new Rectangle(t[0]*48,t[1]*48,(t[2]-t[0])*48+1,(t[3]-t[1])*48+1),i>=4?4:i);
        }
        for(int i=0;i<PICARONS.length;i++) if(monsters[MAP][PICARON_SLOTS[i]] instanceof MON_Picaron p) {
            int x=PICARONS[i][0]*48,y=PICARONS[i][1]*48;
            if(move){p.worldX=x;p.worldY=y;}p.setHome(x,y);
        }
    }
    public static void obstacles(GamePanel gp,InteractiveTile[][] tiles) {
        for(int i=0;i<TILE_SLOTS.length;i++) {
            int[] pos=OBSTACLES[i];tiles[MAP][TILE_SLOTS[i]]=i<2?new IT_Rock(gp,pos[0],pos[1]):new IT_DestructibleWall(gp,pos[0],pos[1]);
        }
    }
    public static void adapt(GamePanel gp,Entity player,int map,Entity[][] monsters,Entity[][] objects,InteractiveTile[][] tiles,int revision) {
        boolean old=revision==0;
        if(old) {
            for(int slot=33;slot<=39;slot++) if(slot!=33&&slot!=36&&slot!=39) monsters[MAP][slot]=null;
            for(int slot=46;slot<=48;slot++) monsters[MAP][slot]=null;
            for(int slot=6;slot<=10;slot++) tiles[MAP][slot]=null;
            for(int i=0;i<TILE_SLOTS.length-GATE_SLOTS.length;i++) {
                int slot=TILE_SLOTS[i];int[] p=OBSTACLES[i];
                if(i>=5) tiles[MAP][slot]=new IT_DestructibleWall(gp,p[0],p[1]);
                else if(tiles[MAP][slot]!=null) {tiles[MAP][slot].worldX=p[0]*48;tiles[MAP][slot].worldY=p[1]*48;}
            }
            // Loose drops move off obsolete walls; original extra-room chests stay untouched.
            for(Entity e:objects[MAP]) if(e!=null) {e.worldX=24*48;e.worldY=22*48;}
            if(map==MAP) {player.worldX=25*48;player.worldY=23*48;}
        }
        if(revision<2) {
            // Players already in later areas have completed this passage in the previous layout.
            // Preserve that progress as real rubble, never an exit/inventory condition.
            boolean completed=map==4||map==6;
            for(int i=0;i<GATE_SLOTS.length;i++) {
                int[] p=GATE[i];tiles[MAP][GATE_SLOTS[i]]=completed?
                    new IT_Rocky(gp,p[0],p[1]):new IT_DestructibleWall(gp,p[0],p[1]);
            }
            if(map==MAP && (intersectsNewGate(player)||blockedTerrain(gp,player)||
                (player.worldX<17*48 && player.worldY>=42*48))) {
                player.worldX=17*48;player.worldY=44*48;
            }
            for(Entity e:objects[MAP]) if(e!=null && (intersectsNewGate(e)||blockedTerrain(gp,e))) {
                e.worldX=17*48;e.worldY=44*48;
            }
            // Old saves could have sold/lost Iron while the exit still had a bypass.
            // Offer the original reward again, without granting/equipping a duplicate item.
            boolean hasIron=player.inventory.stream().anyMatch(e->e instanceof object.OBJ_Iron);
            if(!hasIron) for(Entity e:objects[5]) if(e instanceof object.OBJ_Chest &&
                e.loot instanceof object.OBJ_Iron && e.opened) {e.opened=false;e.down=e.image;}
        }
        configure(gp,monsters,old);
    }

    private static Rectangle body(Entity e) {
        return new Rectangle(e.worldX+e.solidAreaDefaultX,e.worldY+e.solidAreaDefaultY,e.solidArea.width,e.solidArea.height);
    }
    private static boolean intersectsNewGate(Entity e) {
        for(int[] p:GATE) if(body(e).intersects(new Rectangle(p[0]*48,p[1]*48,44,44)))return true;
        return false;
    }
    private static boolean blockedTerrain(GamePanel gp,Entity e) {
        Rectangle r=body(e);
        for(int x=Math.floorDiv(r.x,48);x<=Math.floorDiv(r.x+r.width-1,48);x++)
            for(int y=Math.floorDiv(r.y,48);y<=Math.floorDiv(r.y+r.height-1,48);y++)
                if(x<0||y<0||x>=gp.maxWorldCol||y>=gp.maxWorldRow||gp.tileM.tile[gp.tileM.mapTileNum[MAP][x][y]].collision)return true;
        return false;
    }
}
