package main;
import entity.Entity;
import monster.MON_Picaron;
import tile_interactive.InteractiveTile;
public final class ThirdZone {
 public static final int REVISION=1;
 public static final int[][] SPAWNS={{34,10},{13,23},{35,25},{30,36},{12,40},{35,41},{29,12},{12,28},{36,31},{23,27},{24,39},{36,22}};
 public static final int[][] ROCKS={{22,20},{23,20},{27,30},{27,31}};
 public static void configure(GamePanel gp,Entity[][] monsters,boolean move){
  for(int i=0;i<SPAWNS.length;i++) {Entity e=monsters[2][21+i];if(e==null)continue;
   int x=SPAWNS[i][0]*gp.tileSize,y=SPAWNS[i][1]*gp.tileSize;
   if(move){e.worldX=x;e.worldY=y;}
   if(e instanceof MON_Picaron p)p.setHome(x,y);
  }
 }
 public static void adapt(GamePanel gp,Entity player,int map,Entity[][] monsters,Entity[][] objects,InteractiveTile[][] tiles,boolean old){
  configure(gp,monsters,old);
  if(old){
   if(map==2){player.worldX=22*gp.tileSize;player.worldY=7*gp.tileSize;}
   for(Entity e:objects[2])if(e!=null){e.worldX=21*gp.tileSize;e.worldY=8*gp.tileSize;}
   for(int i=0;i<4;i++)if(tiles[2][i]!=null){tiles[2][i].worldX=ROCKS[i][0]*gp.tileSize;tiles[2][i].worldY=ROCKS[i][1]*gp.tileSize;}
  }
 }
}
