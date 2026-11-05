package main;

import entity.Entity;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.geom.Area;
import monster.MON_Enfadillo;
import object.OBJ_Chest;
import object.OBJ_Rock;

import static main.ExtraRooms.MAP;
import static main.ExtraRooms.Room.*;

/** Progress and movement local to the workshop and ice chamber. */
public final class FinalRefuges {
    public static final int SENTRY = 86;
    public static final int[][] LATCHES = {{32,6},{37,11}};
    public static final int[][] MARKS = {{13,6},{18,12}};
    public static final int LATCH_INTACT = 164, LATCH_BROKEN = 165;
    public static final int ICE_A = 166, ICE_B = 167;
    public record State(int latches,int damaged,int marks,boolean sentryDefeated) {
        public boolean valid() {
            return latches>=0 && latches<=3 && damaged>=0 && damaged<=3 && marks>=0 && marks<=3 &&
                (latches&damaged)==0;
        }
    }
    public static final State EMPTY = new State(0,0,0,false);
    private final GamePanel gp;
    private State state=EMPTY;
    private int swingHits;
    private String slideDirection;
    private int heldAtStop;

    public FinalRefuges(GamePanel gp) { this.gp=gp; }
    public State state() { return state; }
    public boolean sliding() { return slideDirection!=null; }

    public void restore(State saved) {
        state=saved;
        resetTransient();
        gp.extraRooms.syncTerrain();
    }

    public void resetTransient() {
        slideDirection=null;
        heldAtStop=0;
        swingHits=0;
    }

    public static boolean open(ExtraRooms.Room room,State state) {
        return switch(room) {
            case HANDY -> state.latches==3;
            case IC -> state.marks==3;
            default -> false;
        };
    }

    private void replace(State next) {
        state=next;
        gp.extraRooms.syncTerrain();
    }

    public void syncTerrain() {
        for (int i=0;i<LATCHES.length;i++)
            gp.tileM.setTerrainCell(MAP,LATCHES[i][0],LATCHES[i][1],
                (state.latches&(1<<i))==0?LATCH_INTACT:LATCH_BROKEN);
        for (int i=0;i<MARKS.length;i++)
            gp.tileM.setTerrainCell(MAP,MARKS[i][0],MARKS[i][1],169+2*i+((state.marks>>i)&1));
    }

    public void hitLatches(Rectangle hit,boolean firstFrame) {
        if (gp.extraRooms.active()!=HANDY || !(gp.player.currentWeapon instanceof OBJ_Rock)) return;
        if (firstFrame) swingHits=0;
        for (int i=0;i<LATCHES.length;i++) {
            int bit=1<<i;
            if (((state.latches|swingHits)&bit)!=0 ||
                !hit.intersects(LATCHES[i][0]*48,LATCHES[i][1]*48,48,48)) continue;
            swingHits|=bit;
            if ((state.damaged&bit)==0) replace(new State(state.latches,state.damaged|bit,state.marks,state.sentryDefeated));
            else replace(new State(state.latches|bit,state.damaged&~bit,state.marks,state.sentryDefeated));
            gp.playSE(14);
        }
    }

    public boolean claimDefeat(int slot) {
        if (gp.currentMap!=MAP || slot!=SENTRY) return true;
        if (state.sentryDefeated) return false;
        replace(new State(state.latches,state.damaged,state.marks,true));
        return true;
    }

    public static void populate(GamePanel gp,Entity[][] actors,State state) {
        // Ic no longer has an encounter. Preserve these original slot IDs for migration.
        for (int slot=83;slot<=85;slot++) actors[MAP][slot]=null;
        actors[MAP][SENTRY]=state.sentryDefeated?null:Temple.place(new MON_Enfadillo(gp),32,8);
        configure(actors,state);
    }

    public static void configure(Entity[][] actors,State state) {
        if (state.sentryDefeated) actors[MAP][SENTRY]=null;
        if (actors[MAP][SENTRY] instanceof MON_Enfadillo sentry) sentry.setRefugeSentry("right");
    }

    public static void placeFixtures(Entity[][] npcs,Entity[][] objects) {
        for (var room : new ExtraRooms.Room[]{IC,HANDY}) for (Entity npc : npcs[MAP])
            if (npc!=null && ExtraRooms.at(npc.worldX/48,npc.worldY/48)==room)
                Temple.place(npc,room.npcX,room.npcY);
        if (objects[MAP][7] instanceof OBJ_Chest) Temple.place(objects[MAP][7],IC.chestX,IC.chestY);
    }

    public static State migrate(Entity[][] objects,Entity player,int map) {
        var room=map==MAP ? ExtraRooms.at(player.getCenterX()/48,player.getCenterY()/48) : null;
        boolean pastRocky=map==3 || map==4 || map==6 || map==8 || map==9 || room==IC || room==HANDY;
        boolean missingRocky=player.inventory.stream().noneMatch(OBJ_Rock.class::isInstance);
        return new State(opened(objects,8) || (pastRocky && missingRocky)?3:0,0,opened(objects,7)?3:0,false);
    }

    private static boolean opened(Entity[][] objects,int slot) {
        return objects[MAP][slot] instanceof OBJ_Chest && objects[MAP][slot].opened;
    }

    public void released(int mask) { heldAtStop &= ~mask; }

    private int held() {
        return (gp.keyH.upPressed?1:0)|(gp.keyH.downPressed?2:0)|
            (gp.keyH.leftPressed?4:0)|(gp.keyH.rightPressed?8:0);
    }

    public boolean accepts(int direction) {
        return (heldAtStop&direction)==0 || gp.extraRooms.active()!=IC;
    }

    private Rectangle fullCell(int x,int y) {
        Rectangle body=Temple.bounds(gp.player,x,y);
        int col=body.x/48,row=body.y/48;
        Rectangle cell=new Rectangle(col*48,row*48,48,48);
        return cell.contains(body)?cell:null;
    }

    private boolean ice(Rectangle cell) {
        int tile=gp.tileM.mapTileNum[MAP][cell.x/48][cell.y/48];
        return tile==ICE_A || tile==ICE_B;
    }

    public void walked(int oldX,int oldY,String direction) {
        if (gp.extraRooms.active()!=IC || (oldX==gp.player.worldX && oldY==gp.player.worldY)) return;
        Rectangle cell=fullCell(gp.player.worldX,gp.player.worldY);
        if (cell==null) return;
        if (ice(cell)) slideDirection=direction;
        else if (!cell.equals(fullCell(oldX,oldY)) && activateMark(cell)) stop(cell);
    }

    public boolean slide() {
        if ((slideDirection==null && heldAtStop==0) || gp.extraRooms.active()!=IC) return false;
        heldAtStop&=held();
        if (slideDirection==null) return false;
        gp.player.direction=slideDirection;
        int speed=gp.player.defaultSpeed;
        int x=gp.player.worldX+(slideDirection.equals("left")?-speed:slideDirection.equals("right")?speed:0);
        int y=gp.player.worldY+(slideDirection.equals("up")?-speed:slideDirection.equals("down")?speed:0);
        if (!Temple.clear(gp,gp.player,x,y)) {
            stop(null);
        } else {
            gp.player.worldX=x;
            gp.player.worldY=y;
            Rectangle cell=fullCell(x,y);
            if (cell!=null && !ice(cell)) {
                activateMark(cell);
                stop(cell);
            }
        }
        return true;
    }

    private boolean activateMark(Rectangle cell) {
        for (int i=0;i<MARKS.length;i++) {
            if (cell.x!=MARKS[i][0]*48 || cell.y!=MARKS[i][1]*48) continue;
            int bit=1<<i;
            if ((state.marks&bit)==0) {
                replace(new State(state.latches,state.damaged,state.marks|bit,state.sentryDefeated));
                gp.playSE(3);
            }
            return true;
        }
        return false;
    }

    private void stop(Rectangle cell) {
        if (cell!=null && Temple.clear(gp,gp.player,cell.x,cell.y)) {
            gp.player.worldX=cell.x;
            gp.player.worldY=cell.y;
        }
        slideDirection=null;
        heldAtStop=held();
        gp.keyH.enterPressed=false;
    }

    public void draw(Graphics2D g,ExtraRooms.Room room) {
        if (room==HANDY) {
            light(g,room,37,10);
            light(g,room,31,6);
            gp.extraRooms.prop(g,room,"sconce",36,10,48);
            gp.extraRooms.prop(g,room,"sconce",30,6,48);
            gp.extraRooms.prop(g,room,"tool-rack",39,5,48);
            gp.extraRooms.prop(g,room,"anvil",39,12,48);
            gp.extraRooms.prop(g,room,"chain",30,10,48);
            String mechanism=switch(state.latches) {
                case 1 -> "mechanism-left";
                case 2 -> "mechanism-right";
                case 3 -> "mechanism-1";
                default -> "mechanism-0";
            };
            gp.extraRooms.prop(g,room,mechanism,28,7,96);
        } else if (room==IC) {
            for (int x=12;x<=18;x+=3) gp.extraRooms.prop(g,room,"icicles",x,5,48);
        }
    }

    private void light(Graphics2D g,ExtraRooms.Room room,int x,int y) {
        int cameraX=gp.extraRooms.cameraX(room),cameraY=gp.extraRooms.cameraY(room);
        int cx=x*48+24-cameraX,cy=y*48+24-cameraY;
        Area lit=new Area();
        for (int col=x-2;col<=x+2;col++) for (int row=y-2;row<=y+2;row++) {
            if (!room.contains(col,row)) continue;
            int steps=8;
            boolean clear=true;
            for (int i=1;i<steps;i++) {
                int tx=Math.floorDiv(x*48+24+(col-x)*48*i/steps,48);
                int ty=Math.floorDiv(y*48+24+(row-y)*48*i/steps,48);
                if ((tx!=col || ty!=row) && gp.tileM.tile[gp.tileM.mapTileNum[MAP][tx][ty]].collision) {
                    clear=false;
                    break;
                }
            }
            if (clear) lit.add(new Area(new Rectangle(col*48-cameraX,row*48-cameraY,48,48)));
        }
        Graphics2D glow=(Graphics2D)g.create();
        glow.clip(lit);
        glow.setPaint(new RadialGradientPaint(cx,cy,114,new float[]{0,1},
            new Color[]{new Color(255,174,71,114),new Color(255,174,71,0)}));
        glow.fillOval(cx-114,cy-114,228,228);
        glow.dispose();
    }

    public void drawNotice(Graphics2D g) {
        if (gp.extraRooms.active()!=HANDY || state.latches==3 || gp.player.currentWeapon instanceof OBJ_Rock) return;
        int x=gp.player.getCenterX()/48,y=gp.player.getCenterY()/48;
        for (int i=0;i<LATCHES.length;i++) if ((state.latches&(1<<i))==0 &&
            Math.abs(x-LATCHES[i][0])+Math.abs(y-LATCHES[i][1])<=2) {
            Graphics2D text=(Graphics2D)g.create();
            text.setFont(gp.ui.purisaB.deriveFont(16f));
            text.setColor(Color.WHITE);
            text.drawString("Equip Rocky to break the stone latch.",220,558);
            text.dispose();
            return;
        }
    }
}
