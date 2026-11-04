package main;

import entity.Entity;
import java.awt.Rectangle;
import monster.MON_Sharky;
import monster.MON_Tella;
import object.OBJ_Coin;
import tile_interactive.IT_DestructibleWall;
import tile_interactive.InteractiveTile;

public final class SixthZone {
    public static final int MAP = MON_Tella.ABYSS_MAP;
    public static final int REVISION = 1;
    public static final int[] GATE_SLOTS = {0, 1};
    static final int[] SHORTCUT_SLOTS = {2, 3};

    private record Encounter(int x, int y, int left, int top, int right, int bottom) {
        Rectangle bounds(int tile) {
            return new Rectangle(left * tile, top * tile, (right - left + 1) * tile, (bottom - top + 1) * tile);
        }
    }

    private static final Encounter[] TELLAS = {
        new Encounter(25, 36, 19, 33, 31, 38),
        new Encounter(39, 27, 36, 24, 43, 30),
        new Encounter(27, 14, 20, 12, 32, 17),
        new Encounter(30, 8, 28, 6, 34, 11)
    };
    private static final Encounter[] SHARKS = {
        new Encounter(17, 28, 15, 26, 21, 32),
        new Encounter(32, 28, 29, 26, 34, 32),
        new Encounter(18, 14, 15, 12, 23, 16),
        new Encounter(18, 8, 15, 6, 22, 11),
        new Encounter(39, 10, 36, 8, 40, 12)
    };

    private SixthZone() {}

    public static boolean usesAbyss(int map) { return map == FifthZone.MAP || map == MAP; }

    public static void populate(GamePanel gp, Entity[][] monsters) {
        for (int i = 0; i < TELLAS.length; i++) monsters[MAP][i] = new MON_Tella(gp);
        for (int i = 0; i < SHARKS.length; i++) monsters[MAP][TELLAS.length + i] = new MON_Sharky(gp);
        configure(gp, monsters, true);
    }

    public static void configure(GamePanel gp, Entity[][] monsters, boolean place) {
        int tile = gp.tileSize;
        for (int i = 0; i < TELLAS.length; i++) {
            if (!(monsters[MAP][i] instanceof MON_Tella tella)) continue;
            Encounter encounter = TELLAS[i];
            if (place) {
                tella.worldX = encounter.x * tile;
                tella.worldY = encounter.y * tile;
            }
            tella.configureAbyss(encounter.x * tile, encounter.bounds(tile));
        }
        for (int i = 0; i < SHARKS.length; i++) {
            if (!(monsters[MAP][TELLAS.length + i] instanceof MON_Sharky shark)) continue;
            Encounter encounter = SHARKS[i];
            if (place) {
                shark.worldX = encounter.x * tile;
                shark.worldY = encounter.y * tile;
            }
            shark.setHome(encounter.x * tile, encounter.y * tile, encounter.bounds(tile), -1);
        }
    }

    public static void obstacles(GamePanel gp, InteractiveTile[][] tiles) {
        tiles[MAP][GATE_SLOTS[0]] = new IT_DestructibleWall(gp, 24, 20);
        tiles[MAP][GATE_SLOTS[1]] = new IT_DestructibleWall(gp, 25, 20);
        tiles[MAP][SHORTCUT_SLOTS[0]] = new IT_DestructibleWall(gp, 25, 31);
        tiles[MAP][SHORTCUT_SLOTS[1]] = new IT_DestructibleWall(gp, 26, 31);
    }

    static boolean gateIntact(InteractiveTile[][] tiles) {
        for (int slot : GATE_SLOTS) {
            var tile = tiles[MAP][slot];
            if (tile == null || !tile.destructible) return false;
        }
        return true;
    }

    public static void reward(GamePanel gp, Entity[][] objects) {
        Entity reward = new OBJ_Coin(gp);
        reward.worldX = 42 * gp.tileSize;
        reward.worldY = 26 * gp.tileSize;
        objects[MAP][0] = reward;
    }
}
