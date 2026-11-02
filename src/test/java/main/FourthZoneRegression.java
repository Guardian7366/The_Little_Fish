package main;
import entity.Entity;import monster.MON_Picaron;import java.util.*;import java.nio.file.*;import javax.imageio.ImageIO;
public class FourthZoneRegression {
 static class Quiet extends GamePanel {public void playMusic(int i){}public void playSE(int i){}public void stopMusic(){}}
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 static boolean[][] reachable(Quiet gp,boolean rocks){
  boolean[][] seen=new boolean[50][50];ArrayDeque<int[]> q=new ArrayDeque<>();q.add(new int[]{25,7});seen[25][7]=true;
  while(!q.isEmpty()){int[] p=q.remove();for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int x=p[0]+d[0],y=p[1]+d[1];if(x<0||y<0||x>=50||y>=50||seen[x][y]||gp.tileM.tile[gp.tileM.mapTileNum[7][x][y]].collision)continue;
   boolean blocked=false;if(rocks)for(var t:gp.iTile[7])if(t!=null&&t.destructible&&t.worldX/48==x&&t.worldY/48==y)blocked=true;
   if(!blocked){seen[x][y]=true;q.add(new int[]{x,y});}
  }}return seen;
 }
 public static void main(String[] args)throws Exception{
  Quiet gp=new Quiet();gp.setupGame();gp.currentMap=7;gp.gameState=gp.playState;
  check(Arrays.stream(gp.monster[7]).filter(e->e!=null).count()==8,"population");
  boolean[][] open=reachable(gp,false);check(!reachable(gp,true)[25][45],"initial gate bypassed");
  for(int x=0;x<50;x++)for(int y=0;y<50;y++)if(!gp.tileM.tile[gp.tileM.mapTileNum[7][x][y]].collision)check(open[x][y],"isolated floor "+x+","+y);
  gp.iTile[7][0]=null;gp.iTile[7][1]=null;check(!reachable(gp,true)[25][45],"final gate bypassed");
  gp.iTile[7][6]=null;gp.iTile[7][7]=null;check(reachable(gp,true)[25][45],"optional stones block main route");
  Arrays.fill(gp.iTile[7],null);
  for(int[] dest:new int[][]{{25,45},{7,37},{29,29},{40,33}}){
   gp.player.worldX=25*48;gp.player.worldY=7*48;gp.pFinder.setNodes(25,7,dest[0],dest[1],gp.player);check(gp.pFinder.search(),"no player route");
   for(var node:gp.pFinder.pathList){int tx=node.col*48,ty=node.row*48;
    while(gp.player.worldX!=tx || gp.player.worldY!=ty){
     gp.player.direction=gp.player.worldX<tx?"right":gp.player.worldX>tx?"left":gp.player.worldY<ty?"down":"up";
     gp.player.collisionOn=false;gp.cChecker.checkTile(gp.player);check(!gp.player.collisionOn,"player cannot fit in passage");
     switch(gp.player.direction){case "left"->gp.player.worldX-=4;case "right"->gp.player.worldX+=4;case "up"->gp.player.worldY-=4;case "down"->gp.player.worldY+=4;}
    }
   }
  }
  for(int i=0;i<8;i++){
   MON_Picaron p=(MON_Picaron)gp.monster[7][i];int[] h=FourthZone.SPAWNS[i];
   gp.player.worldX=p.worldX+48;gp.player.worldY=p.worldY;p.setAction();check(p.onPath,"detection in new map");
   gp.player.worldX=25*48;gp.player.worldY=7*48;p.worldX=h[0]*48-48;p.onPath=true;
   for(int t=0;t<600;t++) {p.update();check(Math.abs(p.worldX-h[0]*48)<=192&&Math.abs(p.worldY-h[1]*48)<=192,"escaped territory");}
   check(Math.abs(p.worldX-h[0]*48)+Math.abs(p.worldY-h[1]*48)<24,"failed return "+i);
  }
  for(int[] safe:new int[][]{{25,7},{25,8},{25,45}}){gp.player.worldX=safe[0]*48;gp.player.worldY=safe[1]*48;for(Entity e:gp.monster[7])if(e!=null){e.setAction();check(!e.onPath,"unsafe arrival");}}
  for(int[] secret:FourthZone.SECRETS){gp.player.worldX=secret[0]*48;gp.player.worldY=secret[1]*48;gp.player.direction="up";gp.keyH.enterPressed=false;gp.eHandler.checkEvent();check(gp.gameState==gp.playState,"automatic secret");gp.keyH.enterPressed=true;gp.eHandler.checkEvent();check(gp.gameState==gp.dialogueState,"inaccessible secret");gp.gameState=gp.playState;gp.keyH.enterPressed=false;}
  gp.aSetter.setInteractiveTile();gp.aSetter.setMonster();Path out=Path.of("build/fourth-zone-preview");Files.createDirectories(out);ImageIO.write(gp.map.getWorldMap(7),"png",out.resolve("map.png").toFile());
  for(int[] pos:new int[][]{{25,8},{25,17},{12,25},{8,34},{25,33},{25,41}}){gp.player.worldX=pos[0]*48;gp.player.worldY=pos[1]*48;gp.drawToTempScreen();ImageIO.write(gp.tempScreen,"png",out.resolve(pos[0]+"-"+pos[1]+".png").toFile());}
  gp.stopGame();System.out.println("FOURTH_ZONE PASS: routes, optional stones, eight territories, returns, safe arrivals and secrets");
 }
}
