package main;

import entity.Entity;
import monster.MON_Normalillo;

/** First-zone encounter data. Slots remain stable across saves and respawns. */
public final class FirstZone {
    public static final int REVISION = 1;
    private static final int[][] SPAWNS = {{22,31}, {22,22}, {33,21}, {33,30}, {37,29}, {13,22}};
    // Inclusive floor rectangles. Each enemy stays inside its own encounter.
    private static final int[][] TERRITORIES = {
        {19,28,25,34}, {19,20,25,24}, {30,19,36,24},
        {31,27,35,33}, {35,27,38,33}, {10,20,15,24}
    };
    private FirstZone() { }

    public static void populate(GamePanel gp) {
        for (int slot = 0; slot < SPAWNS.length; slot++) {
            MON_Normalillo enemy = new MON_Normalillo(gp);
            gp.monster[0][slot] = enemy;
            placeAtSpawn(enemy, slot, gp.tileSize);
            configure(enemy, slot, gp.tileSize);
        }
    }

    public static void placeAtSpawn(Entity enemy, int slot, int tileSize) {
        enemy.worldX = SPAWNS[slot][0] * tileSize;
        enemy.worldY = SPAWNS[slot][1] * tileSize;
    }

    public static void configure(Entity enemy, int slot, int tileSize) {
        if (slot >= 0 && slot < TERRITORIES.length && enemy instanceof MON_Normalillo greeny) {
            int[] room = TERRITORIES[slot];
            greeny.setTerritory(room[0] * tileSize, room[1] * tileSize,
                room[2] * tileSize, room[3] * tileSize);
        }
    }

    /** Apply only to prepared save objects, before publishing live state. */
    public static void adaptSavedActors(GamePanel gp, Entity player, int map, Entity[][] npcs,
                                       Entity[][] monsters, Entity[][] objects, boolean oldLayout) {
        if (oldLayout) {
            if (map == 0) { player.worldX = 13 * gp.tileSize; player.worldY = 31 * gp.tileSize; }
            if (npcs[0][0] != null) {
                npcs[0][0].worldX = 15 * gp.tileSize;
                npcs[0][0].worldY = 29 * gp.tileSize;
            }
            for (Entity object : objects[0]) if (object != null && !onOpenFloor(gp, object)) {
                object.worldX = 12 * gp.tileSize; object.worldY = 32 * gp.tileSize;
            }
        }
        for (int slot = 0; slot < SPAWNS.length; slot++) {
            Entity enemy = monsters[0][slot];
            if (enemy == null) continue; // Never resurrect defeated enemies during import.
            if (oldLayout) placeAtSpawn(enemy, slot, gp.tileSize);
            configure(enemy, slot, gp.tileSize);
        }
    }

    private static boolean onOpenFloor(GamePanel gp, Entity actor) {
        int left = actor.worldX / gp.tileSize, top = actor.worldY / gp.tileSize;
        int right = (actor.worldX + gp.tileSize - 1) / gp.tileSize;
        int bottom = (actor.worldY + gp.tileSize - 1) / gp.tileSize;
        for (int x = left; x <= right; x++) for (int y = top; y <= bottom; y++) {
            if (x < 0 || y < 0 || x >= gp.maxWorldCol || y >= gp.maxWorldRow ||
                gp.tileM.tile[gp.tileM.mapTileNum[0][x][y]].collision) return false;
        }
        return true;
    }
}
