package main;
import java.util.*;import java.nio.file.*;import javax.imageio.ImageIO;import entity.Entity;import monster.*;
public class ThirdZoneRegression {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 static class Quiet extends GamePanel{public void playSE(int i){}public void playMusic(int i){}public void stopMusic(){}}
 static boolean reaches(Quiet gp,int x,int y,boolean rocks){
 boolean[][] seen=new boolean[50][50];ArrayDeque<int[]> q=new ArrayDeque<>();q.add(new int[]{22,7});seen[22][7]=true;
 while(!q.isEmpty()){int[] p=q.remove();if(p[0]==x&&p[1]==y)return true;
 for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int a=p[0]+d[0],b=p[1]+d[1];if(a<0||b<0||a>=50||b>=50||seen[a][b]||gp.tileM.tile[gp.tileM.mapTileNum[2][a][b]].collision)continue;
 boolean blocked=false;if(rocks)for(var t:gp.iTile[2])if(t!=null&&t.destructible&&t.worldX/48==a&&t.worldY/48==b)blocked=true;
 if(!blocked){seen[a][b]=true;q.add(new int[]{a,b});}}
 }return false;}
 public static void main(String[] args)throws Exception{
 Quiet gp=new Quiet();gp.setupGame();gp.currentMap=2;gp.gameState=gp.playState;
 check(reaches(gp,19,16,true),"Rocky entrance inaccessible before Rocky");check(!reaches(gp,20,44,true),"Rocky gate can be bypassed");
 for(int[] p:new int[][]{{20,44},{9,17},{39,13},{17,34}})check(reaches(gp,p[0],p[1],false),"destination inaccessible");
 gp.player.worldX=22*48;gp.player.worldY=7*48;
 for(int tick=0;tick<3600;tick++)for(int i=0;i<12;i++){
 Entity e=gp.monster[2][21+i];e.update();int[] h=ThirdZone.SPAWNS[i];
 check(Math.abs(e.worldX-h[0]*48)<=192&&Math.abs(e.worldY-h[1]*48)<=192,"territory escaped");
 if(i<6)check(e.worldX==h[0]*48&&e.worldY==h[1]*48,"sentry moved");}
 MON_Picaron p=(MON_Picaron)gp.monster[2][27];gp.player.worldX=p.worldX-48;gp.player.worldY=p.worldY;p.setAction();check(p.onPath,"nearby player not detected");gp.player.worldX=22*48;gp.player.worldY=7*48;p.setAction();check(!p.onPath,"distant player pursued");
 p.worldX=ThirdZone.SPAWNS[6][0]*48-96;p.worldY=ThirdZone.SPAWNS[6][1]*48;p.onPath=true;
 for(int t=0;t<900;t++)p.update();check(Math.abs(p.worldX-ThirdZone.SPAWNS[6][0]*48)+Math.abs(p.worldY-ThirdZone.SPAWNS[6][1]*48)<24,"Picaron failed to return home: "+p.worldX+","+p.worldY+" direction="+p.direction+" speed="+p.speed);
 Entity sentry=gp.monster[2][21];gp.player.worldX=sentry.worldX;gp.player.worldY=sentry.worldY+48;
 for(int t=0;t<130;t++)sentry.update();check(sentry.projectile.alive,"third-zone shot absent");boolean found=false;for(Entity shot:gp.projectile[2])found|=shot==sentry.projectile;check(found,"shot registered on wrong map");
 gp.saveLoad=new data.SaveLoad(gp,Files.createTempDirectory(Path.of("build"),"third-save-").resolve("save-v1.dat"));gp.monster[2][28]=null;
 check(gp.saveLoad.trySave()&&gp.saveLoad.tryLoad(),"save roundtrip");check(gp.monster[2][28]==null,"defeated enemy restored");
 Path out=Path.of("build/third-zone-preview");Files.createDirectories(out);ImageIO.write(gp.map.getWorldMap(2),"png",out.resolve("map.png").toFile());
 for(int[] pos:new int[][]{{22,7},{22,18},{23,27},{17,34}}){gp.player.worldX=pos[0]*48;gp.player.worldY=pos[1]*48;gp.drawToTempScreen();ImageIO.write(gp.tempScreen,"png",out.resolve(pos[0]+"-"+pos[1]+".png").toFile());}
 gp.stopGame();System.out.println("THIRD_ZONE PASS: progression, territories, detection, save and previews");
 }
}

