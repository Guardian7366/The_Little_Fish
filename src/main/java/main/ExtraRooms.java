package main;

import entity.Entity;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import monster.MON_Enfadillo;
import monster.MON_Normalillo;
import object.OBJ_Chest;

/** Stable map-5 room bounds and independent refuge challenges. */
public final class ExtraRooms {
    public static final int MAP = 5, REVISION = 3, ALL_FLAGS = 63;
    public static final int GREENY = 65, GUARD_ONE = 69, GUARD_TWO = 70;
    public static final int FLOWER_DONE = 4, SHORTCUT = 8, FIRST_CHEST = 1;
    private static final int[][] LANTERNS = {{12,40},{16,36}};

    public enum Room {
        CHICKEN(9,35,44,"chicken",10,40,11,39,20,41,18,41,111),
        FLOWER(27,35,44,"flower",39,40,39,42,28,37,31,37,117),
        SPIKY(27,24,33,"spiky",39,29,39,28,34,32,36,31,123),
        PUMP(9,24,33,"pump",10,29,11,28,20,30,19,31,130),
        HONGUI(9,14,22,"hongui",10,19,11,18,21,15,19,21,136),
        GHOST(27,14,22,"ghost",39,19,39,18,29,21,31,21,142), IC(9,5,12,"ic",10,9,11,10,21,10,19,10,162),
        HANDY(27,5,12,"handy",39,9,39,10,28,11,30,11,156);

        public final int x,y,bottom,arrivalX,arrivalY,npcX,npcY,chestX,chestY,gateX,gateY,closed;
        public final String art;
        Room(int x,int y,int bottom) { this(x,y,bottom,null,0,0,0,0,0,0,0,0,0); }
        Room(int x,int y,int bottom,String art,int arrivalX,int arrivalY,int npcX,int npcY,
             int chestX,int chestY,int gateX,int gateY,int closed) {
            this.x=x; this.y=y; this.bottom=bottom; this.art=art;
            this.arrivalX=arrivalX; this.arrivalY=arrivalY; this.npcX=npcX; this.npcY=npcY;
            this.chestX=chestX; this.chestY=chestY; this.gateX=gateX; this.gateY=gateY; this.closed=closed;
        }
        boolean contains(int col,int row) { return col>=x && col<x+14 && row>=y && row<=bottom; }
        public Rectangle view() {
            // Boundary rows are drawn only once. Lower rooms keep the top frieze.
            int last=bottom<44 ? bottom : bottom+1;
            return new Rectangle(x-1,y-1,16,last-y+2);
        }
    }

    private static final Room[] ROOMS=Room.values();
    private final GamePanel gp;
    private int flags;
    public final RefugeChallenges later;
    public final FinalRefuges ending;

    public ExtraRooms(GamePanel gp) { this.gp=gp; later=new RefugeChallenges(gp); ending=new FinalRefuges(gp); }

    public static Room at(int col,int row) {
        for (Room room : ROOMS) if (room.contains(col,row)) return room;
        return null;
    }
    public Room active() {
        return gp.currentMap==MAP ? at(gp.player.getCenterX()/48,gp.player.getCenterY()/48) : null;
    }
    public boolean visible(Entity actor) {
        return visible(actor,active());
    }

    public boolean visible(Entity actor,Room room) {
        return actor!=null && (gp.currentMap!=MAP ||
            (room!=null && room.contains(actor.worldX/48,actor.worldY/48)));
    }

    public int cameraX(Room room) { return (room.x-1)*48-(gp.screenWidht-16*48)/2; }
    public int cameraY(Room room) {
        Rectangle view=room.view();
        return view.y*48-(gp.screenHeight-view.height*48)/2;
    }
    public int flags() { return flags; }
    public void restore(int state) { flags=state; syncTerrain(); }

    public static boolean open(Room room,int state) {
        return switch (room) {
            case CHICKEN -> (state&3)==3;
            case FLOWER -> (state&FLOWER_DONE)!=0;
            case SPIKY -> (state&SHORTCUT)!=0;
            default -> false;
        };
    }

    public boolean isOpen(Room room) {
        return open(room,flags) || RefugeChallenges.open(room,later.state()) || FinalRefuges.open(room,ending.state());
    }

    public void syncTerrain() {
        for (Room room : ROOMS) if (room.art!=null)
            gp.tileM.setTerrainCell(MAP,room.gateX,room.gateY,room.closed+(isOpen(room)?1:0));
        later.syncBindings();
        ending.syncTerrain();
    }

    private void complete(int bit) {
        if ((flags&bit)!=0) return;
        flags|=bit;
        syncTerrain();
        gp.playSE(3);
    }

    public boolean interact() {
        if (later.interact()) return true;
        Room room=active();
        if (!gp.keyH.enterPressed || room==null || gp.gameState!=gp.playState) return false;
        int col=gp.player.getCenterX()/48,row=gp.player.getCenterY()/48;
        if (room==Room.CHICKEN) {
            for (int i=0;i<LANTERNS.length;i++) {
                int[] lamp=LANTERNS[i];
                if (facing(col,row,lamp[0],lamp[1])) {
                    complete(1<<i);
                    gp.player.attackCanceled=true;
                    return true;
                }
            }
        }
        if (room==Room.SPIKY && col==35 && row==31 && gp.player.direction.equals("right") &&
            !open(room,flags)) {
            complete(SHORTCUT);
            gp.player.attackCanceled=true;
            return true;
        }
        return false;
    }

    private boolean facing(int x,int y,int tx,int ty) {
        if (x==tx && y==ty) return true;
        return switch (gp.player.direction) {
            case "left" -> tx==x-1 && ty==y;
            case "right" -> tx==x+1 && ty==y;
            case "up" -> tx==x && ty==y-1;
            default -> tx==x && ty==y+1;
        };
    }

    public boolean claimDefeat(int slot) {
        if (gp.currentMap!=MAP) return true;
        if (!later.claimDefeat(slot) || !ending.claimDefeat(slot)) return false;
        int bit=switch(slot) {
            case GREENY -> FLOWER_DONE;
            case GUARD_ONE -> 16;
            case GUARD_TWO -> 32;
            default -> 0;
        };
        if (bit==0) return true;
        if ((flags&bit)!=0) return false;
        complete(bit);
        return true;
    }

    public static void populate(GamePanel gp,Entity[][] monsters,int state) {
        for (int slot=61;slot<=72;slot++) monsters[MAP][slot]=null;
        if ((state&FLOWER_DONE)==0) monsters[MAP][GREENY]=Temple.place(new MON_Normalillo(gp),35,39);
        if ((state&16)==0) monsters[MAP][GUARD_ONE]=Temple.place(new MON_Enfadillo(gp),32,26);
        if ((state&32)==0) monsters[MAP][GUARD_TWO]=Temple.place(new MON_Enfadillo(gp),34,30);
        configure(monsters,state);
    }

    public static void configure(Entity[][] monsters,int state) {
        if ((state&FLOWER_DONE)!=0) monsters[MAP][GREENY]=null;
        if ((state&16)!=0) monsters[MAP][GUARD_ONE]=null;
        if ((state&32)!=0) monsters[MAP][GUARD_TWO]=null;
        if (monsters[MAP][GREENY] instanceof MON_Normalillo greeny) greeny.setTerritory(32*48,36*48,37*48,40*48);
        for (int slot : new int[]{GUARD_ONE,GUARD_TWO})
            if (monsters[MAP][slot] instanceof MON_Enfadillo guard) guard.setRefugeSentry(slot==GUARD_ONE?"right":"left");
    }

    public static void placeFixtures(Entity[][] npcs,Entity[][] objects) {
        for (int i=0;i<3;i++) {
            Room room=ROOMS[i];
            for (Entity npc : npcs[MAP])
                if (npc!=null && at(npc.worldX/48,npc.worldY/48)==room) Temple.place(npc,room.npcX,room.npcY);
            Entity chest=objects[MAP][FIRST_CHEST+i];
            if (chest instanceof OBJ_Chest) Temple.place(chest,room.chestX,room.chestY);
        }
    }

    public static int openedRewards(Entity[][] objects) {
        int result=0;
        for (int i=0;i<3;i++) {
            Entity chest=objects[MAP][FIRST_CHEST+i];
            if (chest instanceof OBJ_Chest && chest.opened) result|=i==0?3:i==1?FLOWER_DONE:SHORTCUT;
        }
        return result;
    }

    public static boolean safePosition(GamePanel gp,Room room,Entity player,Entity[][] npcs,Entity[][] objects,
                                       Entity[][] monsters,int state,RefugeChallenges.State laterState,
                                       FinalRefuges.State finalState) {
        boolean gateOpen=open(room,state) || RefugeChallenges.open(room,laterState) || FinalRefuges.open(room,finalState);
        int col=player.getCenterX()/48;
        if ((!gateOpen && room==Room.CHICKEN && col>room.gateX) ||
            (!gateOpen && room==Room.FLOWER && col<room.gateX)) return false;
        if ((!gateOpen && (room==Room.PUMP || room==Room.HONGUI || room==Room.IC) && col>room.gateX) ||
            (!gateOpen && (room==Room.GHOST || room==Room.HANDY) && col<room.gateX)) return false;
        Rectangle body=Temple.bounds(player,player.worldX,player.worldY);
        for (int y=body.y/48;y<=(body.y+body.height-1)/48;y++)
            for (int x=body.x/48;x<=(body.x+body.width-1)/48;x++) {
                if (!room.contains(x,y)) return false;
                if (x==room.gateX && y==room.gateY) {
                    if (!gateOpen) return false;
                } else {
                    int tile=gp.tileM.mapTileNum[MAP][x][y];
                    for (int i=0;i<RefugeChallenges.BINDINGS.length;i++)
                        if (x==RefugeChallenges.BINDINGS[i][0] && y==RefugeChallenges.BINDINGS[i][1])
                            tile=(laterState.bindings()&(1<<i))==0?144:145;
                    for (int i=0;i<FinalRefuges.LATCHES.length;i++)
                        if (x==FinalRefuges.LATCHES[i][0] && y==FinalRefuges.LATCHES[i][1])
                            tile=(finalState.latches()&(1<<i))==0?FinalRefuges.LATCH_INTACT:FinalRefuges.LATCH_BROKEN;
                    if (gp.tileM.tile[tile].collision) return false;
                }
            }
        for (Entity actor : objects[MAP])
            if (actor!=null && actor.collision && overlaps(actor,body)) return false;
        for (Entity[] actors : new Entity[][]{npcs[MAP],monsters[MAP]})
            for (Entity actor : actors)
                if (actor!=null && actor.alive && !actor.dying && overlaps(actor,body)) return false;
        return true;
    }

    private static boolean overlaps(Entity actor,Rectangle body) {
        return body.intersects(Temple.bounds(actor,actor.worldX,actor.worldY));
    }

    public void background(Graphics2D g,Room room) {
        if (room!=null && room.art!=null)
            g.drawImage(SpriteCache.load(path(room,"background"),960,576),0,0,null);
    }

    private static String path(Room room,String name) { return "/refugios/"+room.art+"/"+name+".png"; }

    void prop(Graphics2D g,Room room,String name,int x,int y,int width) {
        g.drawImage(SpriteCache.load(path(room,name),width,48),x*48-cameraX(room),y*48-cameraY(room),null);
    }

    public void details(Graphics2D g,Room room) {
        later.draw(g,room);
        ending.draw(g,room);
        if (room==Room.CHICKEN) {
            prop(g,room,"table-two",13,38,96);
            prop(g,room,"basket",21,36,48);
            prop(g,room,"jar",10,36,48);
            for (int i=0;i<LANTERNS.length;i++) {
                int x=LANTERNS[i][0],y=LANTERNS[i][1];
                boolean lit=(flags&(1<<i))!=0;
                if (lit) {
                    Graphics2D glow=(Graphics2D)g.create();
                    int cx=x*48+24-cameraX(room),cy=y*48+20-cameraY(room);
                    glow.setPaint(new RadialGradientPaint(cx,cy,82,new float[]{0,1},
                        new Color[]{new Color(255,174,71,98),new Color(255,174,71,0)}));
                    glow.fillOval(cx-82,cy-82,164,164);
                    glow.dispose();
                }
                prop(g,room,lit?"lantern-on":"lantern-off",x,y,48);
            }
        } else if (room==Room.FLOWER) {
            prop(g,room,"remedy-shelf",28,35,96);
            prop(g,room,"pot-0",32,35,48);
            prop(g,room,"pot-1",37,41,48);
            prop(g,room,"pot-0",39,35,48);
        } else if (room==Room.SPIKY) {
            prop(g,room,"banner",39,24,48);
            prop(g,room,"cot",32,33,96);
        }
    }
}
