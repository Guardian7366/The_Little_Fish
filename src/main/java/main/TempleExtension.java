package main;

import entity.Entity;
import entity.NPC_BigRock;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import monster.MON_Flower;
import monster.MON_Tella;
import object.OBJ_DoorIron;
import tile_interactive.IT_MetalPlate;
import tile_interactive.InteractiveTile;

/** The chambers and persistent puzzle identities of the second temple map. */
public final class TempleExtension {
    public static final int MAP = 9, REVISION = 1, GATE_X = 24, GATE_Y = 6;

    public record Cell(int x, int y) {}
    public record Lane(int rockSlot, int plateSlot, Cell... path) {
        public Cell start() { return path[0]; }
        public Cell end() { return path[path.length-1]; }
        public int step(Entity rock) {
            for (int i=0; i<path.length; i++)
                if (rock.worldX==path[i].x*48 && rock.worldY==path[i].y*48) return i;
            return -1;
        }
    }

    public static final Lane[] LANES = {
        new Lane(0,0,new Cell(10,39),new Cell(11,39),new Cell(12,39),new Cell(13,39),new Cell(14,39)),
        new Lane(1,1,new Cell(8,14),new Cell(8,13),new Cell(8,12),new Cell(8,11),new Cell(8,10)),
        new Lane(2,2,new Cell(35,32),new Cell(36,32),new Cell(37,32),new Cell(38,32),
            new Cell(38,33),new Cell(38,34),new Cell(38,35),new Cell(38,36))
    };

    public record Inscription(String statue, int x, int y, int readX, int readY, String facing, String text) {}
    public static final Inscription[] INSCRIPTIONS = {
        new Inscription("velo",7,22,8,22,"up",
            "I left room on the bench after they\nstopped coming."),
        new Inscription("fauce",43,24,43,24,"up",
            "At the end she needed help getting to\nthe window. I carried her there."),
        new Inscription("centinela",28,9,28,9,"up",
            "My hands were empty when I left. I\ncould still turn the handle.")
    };

    static final int[][] FLOWERS = {{24,37},{15,33},{14,23},{23,27},{16,15},{34,16},{42,12}};
    static final int[][] TELLAS = {
        {36,25,33,22,39,28}, {27,23,21,22,28,29}, {17,11,13,10,20,17}, {33,8,30,7,37,10}
    };

    private TempleExtension() {}

    public static void populate(GamePanel gp, Entity[][] actors) {
        for (int i=0; i<FLOWERS.length; i++) {
            MON_Flower flower = new MON_Flower(gp);
            actors[MAP][i] = Temple.place(flower,FLOWERS[i][0],FLOWERS[i][1]);
            flower.prepareTemple();
        }
        for (int i=0; i<TELLAS.length; i++)
            actors[MAP][FLOWERS.length+i] = Temple.place(new MON_Tella(gp),TELLAS[i][0],TELLAS[i][1]);
        configure(actors);
    }

    public static void configure(Entity[][] actors) {
        for (Entity actor : actors[MAP])
            if (actor instanceof MON_Flower flower) flower.prepareTemple();
        for (int i=0; i<TELLAS.length; i++) {
            int[] t = TELLAS[i];
            if (actors[MAP][FLOWERS.length+i] instanceof MON_Tella tella)
                tella.configureAbyss(t[0]*48,new Rectangle(t[2]*48,t[3]*48,(t[4]-t[2]+1)*48,(t[5]-t[3]+1)*48));
        }
    }

    public static void rocks(GamePanel gp, Entity[][] npcs) {
        for (Lane lane : LANES)
            npcs[MAP][lane.rockSlot] = Temple.place(new NPC_BigRock(gp),lane.start().x,lane.start().y);
    }

    public static void plates(GamePanel gp, InteractiveTile[][] tiles) {
        for (Lane lane : LANES) tiles[MAP][lane.plateSlot] = new IT_MetalPlate(gp,lane.end().x,lane.end().y);
    }

    public static boolean valid(Entity rock, Entity plate, Lane lane) {
        if (!(rock instanceof NPC_BigRock) || !(plate instanceof IT_MetalPlate) ||
            plate.worldX!=lane.end().x*48 || plate.worldY!=lane.end().y*48) return false;
        int step = lane.step(rock);
        return step>=0 && (step==lane.path.length-1 ? rock.linkedEntity==plate : rock.linkedEntity==null);
    }

    private static boolean paired(Entity rock, Entity plate, Lane lane) {
        return valid(rock,plate,lane) && rock.linkedEntity==plate;
    }

    public static boolean solved(Entity[][] npcs, InteractiveTile[][] tiles) {
        for (Lane lane : LANES)
            if (!paired(npcs[MAP][lane.rockSlot],tiles[MAP][lane.plateSlot],lane)) return false;
        return true;
    }

    static void push(GamePanel gp, NPC_BigRock rock, String direction) {
        for (Lane lane : LANES) {
            if (gp.npc[MAP][lane.rockSlot]!=rock) continue;
            int step = lane.step(rock);
            if (step<0 || step==lane.path.length-1) return;
            Cell from = lane.path[step], to = lane.path[step+1];
            if (!Temple.pushStep(gp,rock,direction,to.x-from.x,to.y-from.y)) return;
            if (to.equals(lane.end())) {
                rock.linkedEntity = gp.iTile[MAP][lane.plateSlot];
                gp.playSE(14);
            }
            if (solved(gp.npc,gp.iTile) && gp.obj[MAP][0] instanceof OBJ_DoorIron) {
                gp.obj[MAP][0] = null;
                gp.playSE(3);
            }
            return;
        }
    }

    private static String statuePath(Inscription inscription) {
        return "/octava/estatua-"+inscription.statue+".png";
    }

    public static void prepareArt() {
        for (Inscription inscription : INSCRIPTIONS) SpriteCache.load(statuePath(inscription),96,144);
    }

    static void drawDetails(Graphics2D g, GamePanel gp) {
        for (Inscription inscription : INSCRIPTIONS)
            g.drawImage(SpriteCache.load(statuePath(inscription),96,144),
                inscription.x*48-gp.player.worldX+gp.player.screenX,
                inscription.y*48-144-gp.player.worldY+gp.player.screenY,null);
        if (solved(gp.npc,gp.iTile)) Temple.sprite(g,gp,"puzzle/gate-open",GATE_X*48,GATE_Y*48,48,48);
        for (int i=0; i<LANES.length; i++) {
            Lane lane = LANES[i];
            Entity plate = gp.iTile[MAP][lane.plateSlot];
            boolean active = paired(gp.npc[MAP][lane.rockSlot],plate,lane);
            if (plate!=null) {
                Temple.sprite(g,gp,active ? "puzzle/plate-on" : "puzzle/plate-off",plate.worldX,plate.worldY,48,48);
                gp.temple.rune(g,i,plate.worldX,plate.worldY);
            }
            Graphics2D rune = (Graphics2D)g.create();
            if (!active) rune.setComposite(AlphaComposite.SrcOver.derive(0.45f));
            gp.temple.rune(rune,i,(23+i)*48,8*48);
            rune.dispose();
        }
    }
}
