package main;
import entity.*;import monster.*;import object.*;import java.awt.*;import java.nio.file.*;import javax.imageio.*;import java.util.*;
public class SecondZoneRegression {
 static int n;static void check(boolean b,String message){if(!b)throw new AssertionError(message);n++;}
 static class Quiet extends GamePanel{public void playSE(int i){}public void playMusic(int i){}public void stopMusic(){}}
 public static void main(String[] args)throws Exception{
 Quiet gp=new Quiet();gp.setupGame();gp.currentMap=1;gp.gameState=gp.playState;
 int greens=0,angry=0;for(Entity e:gp.monster[1]){if(e instanceof MON_Normalillo)greens++;if(e instanceof MON_Enfadillo)angry++;}
 check(greens==9&&angry==6,"population preserved");
 boolean[][] seen=new boolean[50][50];ArrayDeque<int[]> q=new ArrayDeque<>();q.add(new int[]{9,41});seen[9][41]=true;
 while(!q.isEmpty()){int[] p=q.remove();for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int x=p[0]+d[0],y=p[1]+d[1];if(x<0||y<0||x>=50||y>=50||seen[x][y]||gp.tileM.tile[gp.tileM.mapTileNum[1][x][y]].collision)continue;seen[x][y]=true;q.add(new int[]{x,y});}}
 for(int x=0;x<50;x++)for(int y=0;y<50;y++)if(!gp.tileM.tile[gp.tileM.mapTileNum[1][x][y]].collision)check(seen[x][y],"isolated floor "+x+","+y);
 for(int[] dest:new int[][]{{14,41},{23,35},{9,34},{39,23},{39,7},{8,26},{26,7}}){
 gp.player.worldX=9*48;gp.player.worldY=41*48;gp.pFinder.setNodes(9,41,dest[0],dest[1],gp.player);check(gp.pFinder.search(),"unreachable destination");
 for(ai.Node node:gp.pFinder.pathList){int tx=node.col*48,ty=node.row*48;while(gp.player.worldX!=tx||gp.player.worldY!=ty){gp.player.direction=gp.player.worldX<tx?"right":gp.player.worldX>tx?"left":gp.player.worldY<ty?"down":"up";gp.player.collisionOn=false;gp.cChecker.checkTile(gp.player);check(!gp.player.collisionOn,"route fails actual collision");switch(gp.player.direction){case "left"->gp.player.worldX-=4;case "right"->gp.player.worldX+=4;case "up"->gp.player.worldY-=4;case "down"->gp.player.worldY+=4;}}}}
 // No shot can enter any protected arrival or conversation tile, including adjacent exits.
 for(int[] safe:new int[][]{{9,41},{14,41},{23,34},{23,35},{9,34},{39,23},{39,7}}){
 gp.player.worldX=safe[0]*48;gp.player.worldY=safe[1]*48;
 for(Entity e:gp.monster[1])if(e instanceof MON_Enfadillo){int dx=gp.player.getCenterX()-e.getCenterX(),dy=gp.player.getCenterY()-e.getCenterY();e.direction=Math.abs(dx)>=Math.abs(dy)?(dx<0?"left":"right"):(dy<0?"up":"down");boolean inRange=Math.max(Math.abs(dx),Math.abs(dy))<=192&&Math.min(Math.abs(dx),Math.abs(dy))<=16;
 check(!inRange||!((OBJ_Bad_Bubble)e.projectile).clearShot(e,Math.max(Math.abs(dx),Math.abs(dy))),"sentry threatens safe tile");}}
 gp.player.worldX=14*48;gp.player.worldY=41*48;
 int[][] rooms={{20,39,24,43},{9,29,12,32},{14,31,16,33},{20,24,22,28},{28,24,30,26},{24,28,26,30},{34,23,36,25},{20,10,23,14},{29,10,32,14}};
 for(int t=0;t<7200;t++)for(int i=0;i<9;i++){Entity e=gp.monster[1][6+i];e.update();int[] b=rooms[i];check(e.worldX>=b[0]*48&&e.worldX<=b[2]*48&&e.worldY>=b[1]*48&&e.worldY<=b[3]*48,"Greeny escaped encounter");}
 check(gp.player.life==gp.player.maxLife,"refuge invaded");gp.aSetter.setMonster();
 for(int[] secret:new int[][]{{8,26},{26,7}}){gp.player.worldX=secret[0]*48;gp.player.worldY=secret[1]*48;gp.player.direction="up";gp.keyH.enterPressed=false;gp.eHandler.checkEvent();check(gp.gameState==gp.playState,"secret opens automatically");gp.keyH.enterPressed=true;gp.eHandler.checkEvent();check(gp.gameState==gp.dialogueState,"secret inaccessible");gp.gameState=gp.playState;gp.keyH.enterPressed=false;gp.player.attackCanceled=false;gp.ui.npc=null;}
 gp.player.worldX=26*48;gp.player.worldY=40*48;
 check(!gp.firstZoneGuide.message().isEmpty(),"cover reminder appears");for(int i=0;i<360;i++)gp.firstZoneGuide.update();check(gp.firstZoneGuide.message().isEmpty(),"cover reminder expires");gp.firstZoneGuide.reset();
 Path out=Path.of("build/second-zone-preview");Files.createDirectories(out);ImageIO.write(gp.map.getWorldMap(1),"png",out.resolve("map.png").toFile());
 for(int[] p:new int[][]{{14,41},{26,40},{11,34},{23,34},{24,27},{39,23},{32,10},{26,7}}){gp.player.worldX=p[0]*48;gp.player.worldY=p[1]*48;gp.drawToTempScreen();ImageIO.write(gp.tempScreen,"png",out.resolve(p[0]+"-"+p[1]+".png").toFile());}
 gp.stopGame();System.out.println("SECOND_ZONE PASS "+n+" assertions; routes, safe arrivals, 7200 ticks, secrets and previews");
 }
}
