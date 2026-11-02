package main;
import entity.Entity;
import monster.MON_Normalillo;
import monster.MON_Enfadillo;

/** Stable slots preserve defeated enemies while adapting old layouts. */
public final class SecondZone {
 public static final int REVISION=1;
 private static final int[][] SPAWNS={{22,41},{11,30},{15,32},{21,26},{29,25},{25,29},{35,24},{22,12},{31,12},{30,40},{15,28},{25,24},{29,29},{36,20},{34,10}};
 private static final int[][] ROOMS={{20,39,24,43},{9,29,12,32},{14,31,16,33},{20,24,22,28},{28,24,30,26},{24,28,26,30},{34,23,36,25},{20,10,23,14},{29,10,32,14}};
 private SecondZone(){}
 public static void populate(GamePanel gp){
  for(int i=0;i<SPAWNS.length;i++){
   Entity e=i<9?new MON_Normalillo(gp):new MON_Enfadillo(gp);gp.monster[1][6+i]=e;
   place(e,i,gp.tileSize);configure(e,i,gp.tileSize);
  }
 }
 private static void place(Entity e,int i,int size){e.worldX=SPAWNS[i][0]*size;e.worldY=SPAWNS[i][1]*size;}
 private static void configure(Entity e,int i,int size){if(i<9&&e instanceof MON_Normalillo green){int[] r=ROOMS[i];green.setTerritory(r[0]*size,r[1]*size,r[2]*size,r[3]*size);}}
 public static void adapt(GamePanel gp,Entity player,int map,Entity[][] monsters,Entity[][] objects,boolean old){
  if(old&&map==1){player.worldX=14*gp.tileSize;player.worldY=41*gp.tileSize;}
  if(old)for(Entity e:objects[1])if(e!=null){e.worldX=13*gp.tileSize;e.worldY=42*gp.tileSize;}
  for(int i=0;i<SPAWNS.length;i++){Entity e=monsters[1][6+i];if(e==null)continue;if(old)place(e,i,gp.tileSize);configure(e,i,gp.tileSize);}
 }
}
