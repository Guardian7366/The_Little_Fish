package main;

import entity.Entity;
import monster.MON_Picaron;
import tile_interactive.InteractiveTile;
import tile_interactive.IT_Rock;

/** New fourth area; old map IDs remain stable for saved games and story events. */
public final class FourthZone {
    public static final int MAP = 7, REVISION = 1;
    public static final int[][] SPAWNS = {{25,17},{12,25},{37,25},{8,34},{17,33},{34,33},{20,41},{32,41}};
    public static final int[][] ROCKS = {{25,11},{26,11},{8,30},{9,30},{25,27},{26,27},{25,35},{26,35}};
    public static final int[][] SECRETS = {{7,37},{29,29},{40,33}};

    private FourthZone() {}

    public static void populate(GamePanel gp, Entity[][] monsters) {
        for (int i=0;i<SPAWNS.length;i++) {
            MON_Picaron enemy=new MON_Picaron(gp);
            enemy.worldX=SPAWNS[i][0]*gp.tileSize; enemy.worldY=SPAWNS[i][1]*gp.tileSize;
            monsters[MAP][i]=enemy;
        }
        configure(gp,monsters);
    }

    public static void configure(GamePanel gp, Entity[][] monsters) {
        for(int i=0;i<SPAWNS.length;i++)
            if(monsters[MAP][i] instanceof MON_Picaron enemy)
                enemy.setHome(SPAWNS[i][0]*gp.tileSize,SPAWNS[i][1]*gp.tileSize);
    }

    public static void rocks(GamePanel gp, InteractiveTile[][] tiles) {
        for(int i=0;i<ROCKS.length;i++) tiles[MAP][i]=new IT_Rock(gp,ROCKS[i][0],ROCKS[i][1]);
    }

    public static void adapt(GamePanel gp, Entity[][] monsters, InteractiveTile[][] tiles, boolean old) {
        if(old) { populate(gp,monsters); rocks(gp,tiles); }
        else configure(gp,monsters);
    }
}
