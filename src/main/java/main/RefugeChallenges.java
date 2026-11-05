package main;

import entity.Entity;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import monster.MON_Picaron;
import object.OBJ_Chest;

import static main.ExtraRooms.MAP;
import static main.ExtraRooms.Room.*;

/** Independent progress for the garden, guard lesson and memory archive. */
public final class RefugeChallenges {
    public static final int PICARON = 77;
    public static final int[][] BINDINGS = {{14,27},{17,30}};
    public static final int[][] STONES = {{34,16},{37,16},{35,20}};
    public static final int[] SEQUENCE = {0,2,1};
    private static final int DEMO_TICKS=240;
    public record State(int bindings,int blocks,boolean defeated,int prefix) {
        public boolean valid() {
            return bindings>=0 && bindings<=3 && blocks>=0 && blocks<=2 && prefix>=0 && prefix<=3 &&
                (!defeated || blocks==2);
        }
    }
    public static final State EMPTY=new State(0,0,false,0);
    private final GamePanel gp;
    private State state=EMPTY;
    private int demoTicks;
    private int lastStone=-1;

    public RefugeChallenges(GamePanel gp) { this.gp=gp; }
    public State state() { return state; }

    public void restore(State saved) {
        state=saved;
        resetTransient();
        gp.extraRooms.syncTerrain();
    }

    public void resetTransient() {
        demoTicks=0;
        lastStone=gp.extraRooms.active()==GHOST ? stoneUnderPlayer() : -1;
    }

    public static boolean open(ExtraRooms.Room room,State state) {
        return switch(room) {
            case PUMP -> state.bindings==3;
            case HONGUI -> state.blocks==2;
            case GHOST -> state.prefix==3;
            default -> false;
        };
    }

    private void replace(State next) {
        state=next;
        gp.extraRooms.syncTerrain();
    }

    public void syncBindings() {
        for (int i=0;i<BINDINGS.length;i++)
            gp.tileM.setTerrainCell(MAP,BINDINGS[i][0],BINDINGS[i][1],(state.bindings&(1<<i))==0?144:145);
    }

    public void cutBindings(Rectangle hit) {
        if (gp.currentMap!=MAP || gp.extraRooms.active()!=PUMP) return;
        for (int i=0;i<BINDINGS.length;i++) {
            int bit=1<<i;
            if ((state.bindings&bit)==0 && hit.intersects(BINDINGS[i][0]*48,BINDINGS[i][1]*48,48,48)) {
                replace(new State(state.bindings|bit,state.blocks,state.defeated,state.prefix));
                gp.playSE(14);
            }
        }
    }

    public void blocked(Entity attacker) {
        if (gp.currentMap==MAP && gp.extraRooms.active()==HONGUI &&
            attacker==gp.monster[MAP][PICARON] && attacker instanceof MON_Picaron picaron &&
            picaron.registerRefugeGuard()) {
            replace(new State(state.bindings,Math.min(2,state.blocks+1),state.defeated,state.prefix));
            gp.playSE(3);
        }
    }

    public boolean claimDefeat(int slot) {
        if (gp.currentMap!=MAP || slot!=PICARON) return true;
        if (state.defeated) return false;
        replace(new State(state.bindings,2,true,state.prefix));
        return true;
    }

    public static void populate(GamePanel gp,Entity[][] actors,State state) {
        for (int slot=73;slot<=80;slot++) actors[MAP][slot]=null;
        if (!state.defeated) actors[MAP][PICARON]=Temple.place(new MON_Picaron(gp),16,17);
        configure(actors,state);
    }

    public static void configure(Entity[][] actors,State state) {
        if (state.defeated) actors[MAP][PICARON]=null;
        if (actors[MAP][PICARON] instanceof MON_Picaron picaron)
            picaron.setRefugeHome(16*48,17*48,new Rectangle(13*48,15*48,5*48+1,4*48+1));
    }

    public static void placeFixtures(Entity[][] npcs,Entity[][] objects) {
        for (var room : new ExtraRooms.Room[]{PUMP,HONGUI,GHOST}) {
            for (Entity npc : npcs[MAP])
                if (npc!=null && ExtraRooms.at(npc.worldX/48,npc.worldY/48)==room)
                    Temple.place(npc,room.npcX,room.npcY);
        }
        for (int[] pair : new int[][]{{4,20,30},{6,21,15},{5,29,21}})
            if (objects[MAP][pair[0]] instanceof OBJ_Chest) Temple.place(objects[MAP][pair[0]],pair[1],pair[2]);
    }

    public static State migrate(Entity[][] objects) {
        boolean pump=opened(objects,4),hongui=opened(objects,6),ghost=opened(objects,5);
        return new State(pump?3:0,hongui?2:0,hongui,ghost?3:0);
    }

    private static boolean opened(Entity[][] objects,int slot) {
        return objects[MAP][slot] instanceof OBJ_Chest && objects[MAP][slot].opened;
    }

    public boolean interact() {
        if (gp.extraRooms.active()!=GHOST || !gp.keyH.enterPressed ||
            gp.gameState!=gp.playState || !atPedestal()) return false;
        demoTicks=DEMO_TICKS;
        lastStone=stoneUnderPlayer();
        gp.player.attackCanceled=true;
        return true;
    }

    private boolean atPedestal() {
        int x=gp.player.getCenterX()/48,y=gp.player.getCenterY()/48;
        return x==37 && y==19 && gp.player.direction.equals("up");
    }

    public boolean observing() { return demoTicks>0; }

    public int displayedStone() {
        if (demoTicks<=0) return -1;
        int elapsed=DEMO_TICKS-demoTicks;
        return elapsed%80<60 ? SEQUENCE[elapsed/80] : -1;
    }

    private int stoneUnderPlayer() {
        Rectangle body=Temple.bounds(gp.player,gp.player.worldX,gp.player.worldY);
        for (int i=0;i<STONES.length;i++)
            if (new Rectangle(STONES[i][0]*48,STONES[i][1]*48,48,48).contains(body)) return i;
        return -1;
    }

    public void update() {
        if (gp.extraRooms.active()!=GHOST || gp.gameState!=gp.playState) return;
        int stone=stoneUnderPlayer();
        if (demoTicks>0) {
            demoTicks--;
            lastStone=stone;
            return;
        }
        if (stone!=lastStone && stone>=0 && state.prefix<3) {
            int next=stone==SEQUENCE[state.prefix] ? state.prefix+1 : 0;
            replace(new State(state.bindings,state.blocks,state.defeated,next));
            gp.playSE(next==0?8:3);
        }
        lastStone=stone;
    }

    public void draw(Graphics2D g,ExtraRooms.Room room) {
        if (room==PUMP) {
            gp.extraRooms.prop(g,room,"planter",15,28,48);
            gp.extraRooms.prop(g,room,"planter",16,29,48);
            gp.extraRooms.prop(g,room,"seed-jar",10,24,48);
        } else if (room==HONGUI) {
            gp.extraRooms.prop(g,room,"shelter",10,16,96);
            gp.extraRooms.prop(g,room,"glow-mushroom",14,14,48);
            gp.extraRooms.prop(g,room,"glow-mushroom",21,21,48);
            for (int i=0;i<2;i++) gp.extraRooms.prop(g,room,i<state.blocks?"guard-mark-1":"guard-mark-0",20+i,14,48);
        } else if (room==GHOST) {
            gp.extraRooms.prop(g,room,observing()?"echo-pedestal-1":"echo-pedestal-0",37,18,48);
            for (int i=0;i<STONES.length;i++) {
                boolean lit=observing()?displayedStone()==i:solvedPrefixContains(i);
                gp.extraRooms.prop(g,room,"memory-"+i+"-"+(lit?1:0),STONES[i][0],STONES[i][1],48);
            }
        }
    }

    private boolean solvedPrefixContains(int symbol) {
        for (int i=0;i<state.prefix;i++) if (SEQUENCE[i]==symbol) return true;
        return false;
    }

    public void drawNotice(Graphics2D g) {
        if (gp.extraRooms.active()!=GHOST) return;
        Graphics2D text=(Graphics2D)g.create();
        text.setFont(gp.ui.purisaB.deriveFont(16f));
        text.setColor(java.awt.Color.WHITE);
        text.drawString(observing()?"Watch the marks...":"Watch the marks. Then follow them.",230,558);
        text.dispose();
    }
}
