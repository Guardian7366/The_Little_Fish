package main;

import entity.Entity;
import entity.NPC_BigRock;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import monster.MON_Flower;
import monster.MON_Secret;
import monster.MON_Tella;
import object.OBJ_DoorIron;
import tile_interactive.IT_MetalPlate;
import tile_interactive.InteractiveTile;

/** Coordinates and persistent identities for the temple on the original map 4. */
public final class Temple {
    public static final int MAP = 4, REVISION = 1;
    public static final int WALL = 93, FRIEZE = 94, PILLAR = 95, LAMP = 96, FLOOR = 97;
    public static final int RELIEF_WEST = 105, RELIEF_EAST = 106;
    public static final int[] SECRET_SLOTS = {81, 82};
    public static final int[][] RELIEFS = {{7, 21}, {42, 23}};
    public static final int[][] MURALS = {{22, 35}, {23, 8}, {26, 26}};
    public static final int[][] FLOWERS = {{24,39}, {17,26}, {29,32}, {31,17}, {17,13}, {27,19}};
    private static final int[][] TELLAS = {{29,31,27,29,33,34}, {30,16,28,13,34,19}, {20,22,18,19,22,25}};

    public record Lane(int rockSlot, int plateSlot, int x, int y, int endX, int endY, String direction) {
        int dx() { return Integer.signum(endX - x); }
        int dy() { return Integer.signum(endY - y); }
    }
    public static final Lane[] LANES = {
        new Lane(4,17,11,15,11,10,"up"),
        new Lane(5,16,35,12,40,12,"right"),
        new Lane(6,15,13,31,8,31,"left"),
        new Lane(7,14,36,30,36,35,"down")
    };

    private final GamePanel gp;
    public int reliefs, defeats;
    private MON_Flower tether;

    public Temple(GamePanel gp) { this.gp = gp; }

    public static boolean usesTemple(int map) { return map==MAP || map==TempleExtension.MAP; }

    public void prepareArt() {
        TempleExtension.prepareArt();
        for (String mural : new String[]{"procession","d-threshold","four-weights"})
            SpriteCache.load("/septima/murals/"+mural+".png",144,96);
        for (String detail : new String[]{"gate-open","plate-on","plate-off","rock","rune-1","rune-2","rune-3","rune-4"})
            SpriteCache.load("/septima/puzzle/"+detail+".png",48,48);
    }

    public static void monsters(GamePanel gp, Entity[][] actors, int defeats) {
        for (int slot = 49; slot <= 60; slot++) actors[MAP][slot] = null;
        for (int i = 0; i < FLOWERS.length; i++)
            actors[MAP][49+i] = place(new MON_Flower(gp), FLOWERS[i][0], FLOWERS[i][1]);
        for (int i = 0; i < TELLAS.length; i++)
            actors[MAP][56+i] = place(new MON_Tella(gp), TELLAS[i][0], TELLAS[i][1]);
        for (int i = 0; i < 2; i++) {
            actors[5][SECRET_SLOTS[i]] = null;
            actors[MAP][SECRET_SLOTS[i]] = (defeats & (1 << i)) == 0
                ? place(new MON_Secret(gp), i == 0 ? 4 : 45, i == 0 ? 21 : 23) : null;
        }
        configure(gp, actors);
    }

    public static void configure(GamePanel gp, Entity[][] actors) {
        for (Entity actor : actors[MAP])
            if (actor instanceof MON_Flower flower) flower.prepareTemple();
        for (int i = 0; i < TELLAS.length; i++) {
            int[] t = TELLAS[i];
            if (actors[MAP][56+i] instanceof MON_Tella tella)
                tella.configureAbyss(t[0]*48,new Rectangle(t[2]*48,t[3]*48,(t[4]-t[2]+1)*48,(t[5]-t[3]+1)*48));
        }
        for (int i = 0; i < 2; i++)
            if (actors[MAP][SECRET_SLOTS[i]] instanceof MON_Secret secret)
                secret.configureTemple(i, i == 0 ? new Rectangle(2*48,19*48,5*48,5*48)
                    : new Rectangle(43*48,21*48,5*48,5*48));
    }

    public static Entity place(Entity actor, int x, int y) {
        actor.worldX = x * 48;
        actor.worldY = y * 48;
        return actor;
    }

    public static void fixtures(GamePanel gp, Entity[][] npcs, InteractiveTile[][] tiles) {
        for (Lane lane : LANES) {
            npcs[MAP][lane.rockSlot] = place(new NPC_BigRock(gp),lane.x,lane.y);
            tiles[MAP][lane.plateSlot] = new IT_MetalPlate(gp,lane.endX,lane.endY);
        }
    }

    public static Entity gate(GamePanel gp) {
        return gate(gp,24,5);
    }

    public static Entity gate(GamePanel gp, int x, int y) {
        Entity gate = place(new OBJ_DoorIron(gp),x,y);
        gate.down = SpriteCache.load("/septima/puzzle/gate-closed.png",48,48);
        gate.solidArea.setBounds(0,0,48,48);
        gate.solidAreaDefaultX = gate.solidAreaDefaultY = 0;
        return gate;
    }

    public static boolean paired(Entity rock, Entity plate, Lane lane) {
        return rock instanceof NPC_BigRock && plate instanceof IT_MetalPlate &&
            rock.worldX == lane.endX*48 && rock.worldY == lane.endY*48 &&
            plate.worldX == rock.worldX && plate.worldY == rock.worldY && rock.linkedEntity == plate;
    }

    public static boolean solved(Entity[][] npcs, InteractiveTile[][] tiles) {
        for (Lane lane : LANES)
            if (!paired(npcs[MAP][lane.rockSlot], tiles[MAP][lane.plateSlot], lane)) return false;
        return true;
    }

    public boolean solved() { return solved(gp.npc,gp.iTile); }

    public void push(NPC_BigRock rock, String direction) {
        if (gp.currentMap==TempleExtension.MAP) { TempleExtension.push(gp,rock,direction); return; }
        for (Lane lane : LANES) {
            if (gp.npc[MAP][lane.rockSlot] != rock || !lane.direction.equals(direction)) continue;
            if (paired(rock,gp.iTile[MAP][lane.plateSlot],lane) || !onLane(rock,lane)) return;
            if (!pushStep(gp,rock,direction,lane.dx(),lane.dy())) return;
            if (rock.worldX == lane.endX*48 && rock.worldY == lane.endY*48) {
                rock.linkedEntity = gp.iTile[MAP][lane.plateSlot];
                gp.playSE(14);
            }
            if (solved() && gp.obj[MAP][0] instanceof OBJ_DoorIron) {
                gp.obj[MAP][0] = null;
                gp.playSE(3);
            }
            return;
        }
    }

    static boolean pushStep(GamePanel gp, NPC_BigRock rock, String direction, int dx, int dy) {
        String expected = dx>0 ? "right" : dx<0 ? "left" : dy>0 ? "down" : "up";
        if (!expected.equals(direction)) return false;
        Rectangle player = bounds(gp.player,gp.player.worldX,gp.player.worldY);
        Rectangle stone = bounds(rock,rock.worldX,rock.worldY);
        boolean behind = switch (direction) {
            case "up" -> player.y >= stone.y+stone.height-1 && player.y-stone.y-stone.height<=gp.player.speed &&
                player.getCenterX() >= stone.x && player.getCenterX() < stone.x+stone.width;
            case "down" -> player.y+player.height <= stone.y+1 && stone.y-player.y-player.height<=gp.player.speed &&
                player.getCenterX() >= stone.x && player.getCenterX() < stone.x+stone.width;
            case "left" -> player.x >= stone.x+stone.width-1 && player.x-stone.x-stone.width<=gp.player.speed &&
                player.getCenterY() >= stone.y && player.getCenterY() < stone.y+stone.height;
            default -> player.x+player.width <= stone.x+1 && stone.x-player.x-player.width<=gp.player.speed &&
                player.getCenterY() >= stone.y && player.getCenterY() < stone.y+stone.height;
        };
        if (!behind) return false;
        for (int step = 4; step <= 48; step += 4)
            if (!clear(gp,rock,rock.worldX+dx*step,rock.worldY+dy*step)) return false;
        // One atomic tile step. Save snapshots can only see aligned positions.
        rock.worldX += dx*48;
        rock.worldY += dy*48;
        return true;
    }

    public static boolean onLane(Entity rock, Lane lane) {
        if (rock == null || rock.worldX%48 != 0 || rock.worldY%48 != 0) return false;
        int x = rock.worldX/48, y = rock.worldY/48;
        return x >= Math.min(lane.x,lane.endX) && x <= Math.max(lane.x,lane.endX) &&
            y >= Math.min(lane.y,lane.endY) && y <= Math.max(lane.y,lane.endY);
    }

    public static Rectangle bounds(Entity actor, int x, int y) {
        return new Rectangle(x+actor.solidAreaDefaultX,y+actor.solidAreaDefaultY,
            actor.solidArea.width,actor.solidArea.height);
    }

    public static boolean terrainClear(GamePanel gp, Rectangle box) {
        return terrainClear(gp,MAP,box);
    }

    public static boolean terrainClear(GamePanel gp, int map, Rectangle box) {
        for (int x = Math.floorDiv(box.x,48); x <= Math.floorDiv(box.x+box.width-1,48); x++)
            for (int y = Math.floorDiv(box.y,48); y <= Math.floorDiv(box.y+box.height-1,48); y++)
                if (x<0 || y<0 || x>=gp.maxWorldCol || y>=gp.maxWorldRow ||
                    gp.tileM.tile[gp.tileM.mapTileNum[map][x][y]].collision) return false;
        return true;
    }

    public static boolean portalOverlap(GamePanel gp, Rectangle box) {
        return box.intersects(24*48,(gp.currentMap==TempleExtension.MAP ? 45 : 44)*48,48,48) || box.intersects(24*48,5*48,48,48);
    }

    public static boolean vineBlocked(GamePanel gp, Rectangle box) {
        if (!terrainClear(gp,gp.currentMap,box)) return true;
        for (Entity npc : gp.npc[gp.currentMap])
            if (npc != null && box.intersects(bounds(npc,npc.worldX,npc.worldY))) return true;
        for (Entity object : gp.obj[gp.currentMap])
            if (object != null && object.collision && box.intersects(bounds(object,object.worldX,object.worldY))) return true;
        for (InteractiveTile tile : gp.iTile[gp.currentMap])
            if (tile != null && (tile.collision || tile.destructible) && box.intersects(bounds(tile,tile.worldX,tile.worldY))) return true;
        return false;
    }

    public static boolean clear(GamePanel gp, Entity actor, int x, int y) {
        Rectangle box = bounds(actor,x,y);
        if (!terrainClear(gp,gp.currentMap,box)) return false;
        for (Entity npc : gp.npc[gp.currentMap])
            if (npc != null && npc != actor && box.intersects(bounds(npc,npc.worldX,npc.worldY))) return false;
        for (Entity enemy : gp.monster[gp.currentMap])
            if (enemy != null && enemy != actor && enemy.alive && !enemy.dying &&
                box.intersects(bounds(enemy,enemy.worldX,enemy.worldY))) return false;
        for (Entity object : gp.obj[gp.currentMap])
            if (object != null && object.collision && box.intersects(bounds(object,object.worldX,object.worldY))) return false;
        for (InteractiveTile tile : gp.iTile[gp.currentMap])
            if (tile != null && (tile.collision || tile.destructible) && box.intersects(bounds(tile,tile.worldX,tile.worldY))) return false;
        return actor == gp.player || !box.intersects(bounds(gp.player,gp.player.worldX,gp.player.worldY));
    }

    public boolean openRelief(int index) {
        int mask = 1 << index;
        if ((reliefs & mask) != 0) return false;
        reliefs |= mask;
        syncReliefs();
        gp.playSE(3);
        return true;
    }

    public void syncReliefs() {
        for (int i = 0; i < 2; i++)
            gp.tileM.setTerrainCell(MAP, RELIEFS[i][0], RELIEFS[i][1], (reliefs & (1<<i)) == 0 ? RELIEF_WEST+i : FLOOR);
    }

    public boolean grantSecret(int index) {
        int bit = 1 << index;
        if ((defeats & bit) != 0) return false;
        defeats |= bit;
        return true;
    }

    public boolean attach(MON_Flower flower) {
        if (tether != null) return false;
        tether = flower;
        return true;
    }
    public boolean attached(MON_Flower flower) { return tether == flower; }
    public boolean pulled() { return tether != null; }
    public void release(MON_Flower flower) { if (tether == flower) tether = null; }
    public void resetCombat() {
        tether = null;
        for (int map : new int[]{MAP,TempleExtension.MAP}) for (Entity e : gp.monster[map]) {
            if (e instanceof MON_Flower flower) flower.resetVine();
            if (e instanceof MON_Secret secret) secret.resetEscape();
            if (e instanceof MON_Tella tella) tella.resetPulse();
        }
    }

    public void cutVines(Rectangle attack) {
        for (Entity e : gp.monster[gp.currentMap])
            if (e instanceof MON_Flower flower) flower.cut(attack);
    }

    public static void sprite(Graphics2D g, GamePanel gp, String name, int x, int y, int w, int h) {
        g.drawImage(SpriteCache.load("/septima/"+name+".png",w,h),x-gp.player.worldX+gp.player.screenX,
            y-gp.player.worldY+gp.player.screenY,null);
    }

    public void drawDetails(Graphics2D g) {
        String[] names = {"procession","d-threshold","four-weights"};
        for (int i = 0; i < names.length; i++)
            sprite(g,gp,"murals/"+names[i],MURALS[i][0]*48,MURALS[i][1]*48,144,96);
        if (solved()) sprite(g,gp,"puzzle/gate-open",24*48,5*48,48,48);
        for (int i = 0; i < LANES.length; i++) {
            Lane lane = LANES[i];
            Entity rock = gp.npc[MAP][lane.rockSlot], plate = gp.iTile[MAP][lane.plateSlot];
            boolean active = paired(rock,plate,lane);
            if (plate != null) {
                sprite(g,gp,active ? "puzzle/plate-on" : "puzzle/plate-off",plate.worldX,plate.worldY,48,48);
                rune(g,i,plate.worldX,plate.worldY);
            }
            Graphics2D gateRunes = (Graphics2D) g.create();
            if (!active) gateRunes.setComposite(AlphaComposite.SrcOver.derive(0.45f));
            rune(gateRunes,i,(23+i)*48,7*48);
            gateRunes.dispose();
            rune(g,i,(25+i)*48,41*48);
        }
    }

    public void rune(Graphics2D g, int i, int x, int y) {
        sprite(g,gp,"puzzle/rune-"+(i+1),x,y,48,48);
    }
}
