package main;
import entity.*;import monster.*;import object.*;import java.util.Arrays;
public class EnemyShotRegression {
 static int checks;static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static class Quiet extends GamePanel {int sounds;public void playSE(int i){if(i==26)sounds++;}public void playMusic(int i){}public void stopMusic(){}}
 public static void main(String[] args)throws Exception{
 Quiet gp=new Quiet();gp.setupGame();gp.currentMap=1;gp.gameState=gp.playState;
 for(int[] col:gp.tileM.mapTileNum[1])Arrays.fill(col,1);
 Arrays.fill(gp.monster[1],null);Arrays.fill(gp.npc[1],null);Arrays.fill(gp.obj[1],null);
 MON_Enfadillo e=new MON_Enfadillo(gp);e.worldX=480;e.worldY=480;gp.monster[1][0]=e;
 gp.player.worldX=576;gp.player.worldY=480;gp.player.invincible=false;
 for(int i=0;i<120;i++)e.update();
 check(e.projectile.alive && gp.projectile[1][0]==e.projectile && gp.sounds==1,"shot registered exactly once with audio");
 check(e.worldX==480&&e.worldY==480,"stationary");
 int life=gp.player.life;for(int i=0;i<30;i++)e.projectile.update();check(!e.projectile.alive&&gp.player.life==life-2,"single enemy damage then disappears");
 Arrays.fill(gp.projectile[1],null);for(int i=0;i<120;i++)e.update();check(e.projectile.alive&&gp.sounds==2,"repeat shot");
 e.projectile.alive=false;Arrays.fill(gp.projectile[1],new Entity(gp));for(int i=0;i<150;i++)e.update();check(!e.projectile.alive&&gp.sounds==2,"full pool does not create ghost or audio");gp.projectile[1][4]=null;e.update();check(gp.projectile[1][4]==e.projectile&&e.projectile.alive,"recovery after pool full");
 e.projectile.alive=false;Arrays.fill(gp.projectile[1],null);gp.tileM.mapTileNum[1][11][10]=3;for(int i=0;i<150;i++)e.update();check(!e.projectile.alive,"wall blocks firing");
 e.projectile.set(480,480,"right",true,e);gp.player.invincible=false;life=gp.player.life;for(int i=0;i<30;i++)e.projectile.update();check(!e.projectile.alive&&life==gp.player.life,"wall absorbs before player");gp.tileM.mapTileNum[1][11][10]=1;
 for(String dir:new String[]{"up","down","left","right"}){int dx=dir.equals("left")?-1:dir.equals("right")?1:0,dy=dir.equals("up")?-1:dir.equals("down")?1:0;gp.player.worldX=480+dx*96;gp.player.worldY=480+dy*96;gp.player.invincible=false;gp.player.guarding=true;gp.player.direction=e.getOppositeDirection(dir);gp.player.guardCounter=1;life=gp.player.life;e.projectile.set(480,480,dir,true,e);for(int i=0;i<30;i++)e.projectile.update();check(gp.player.life==life&&!e.projectile.alive,"frontal parry "+dir);}
 gp.player.guarding=false;gp.player.worldX=576;gp.player.worldY=480;gp.player.invincible=true;e.projectile.set(480,480,"right",true,e);life=gp.player.life;for(int i=0;i<30;i++)e.projectile.update();check(!e.projectile.alive&&life==gp.player.life,"invincible contact consumes shot");
 e.projectile.set(480,480,"right",true,e);gp.gameState=gp.pauseState;e.projectile.update();check(e.projectile.worldX==480,"pause freezes shot");gp.gameState=gp.playState;gp.currentMap=2;e.projectile.update();check(!e.projectile.alive,"off-map shot retired");
 gp.currentMap=1;e.projectile.set(0,0,"left",true,e);gp.player.worldX=1000;for(int i=0;i<20;i++)e.projectile.update();check(!e.projectile.alive,"map boundary safe");
 e.projectile.set(480,480,"right",true,e);e.alive=false;for(int i=0;i<30;i++)e.projectile.update();check(!e.projectile.alive,"shot expires after owner dies");
 e.alive=true;e.setKnockBack(e,gp.player,5);for(int i=0;i<15;i++)e.update();check(e.worldX==480&&e.worldY==480,"knockback cannot move stationary enemy");

 gp.obj[1][0]=new OBJ_Door(gp);gp.obj[1][0].worldX=528;gp.obj[1][0].worldY=480;
 e.projectile.set(480,480,"right",true,e);for(int i=0;i<30;i++)e.projectile.update();check(!e.projectile.alive&&e.projectile.worldX<528,"closed door stops shot");gp.obj[1][0]=null;
 gp.iTile[1][0]=new tile_interactive.IT_Rock(gp,11,10);gp.iTile[1][0].worldX=528;gp.iTile[1][0].worldY=480;
 e.projectile.set(480,480,"right",true,e);for(int i=0;i<30;i++)e.projectile.update();check(!e.projectile.alive&&e.projectile.worldX<528,"breakable rock stops shot");gp.iTile[1][0]=new tile_interactive.IT_Rocky(gp,11,10);
 e.projectile.set(480,480,"right",true,e);for(int i=0;i<10;i++)e.projectile.update();check(e.projectile.worldX>=528,"rubble does not stop shot");gp.iTile[1][0]=null;
 gp.projectile[1][0]=e.projectile;gp.player.damageProjectile(0);e.projectile.update();check(!e.projectile.alive,"player attack destroys projectile");
 gp.currentMap=2;e.direction="right";int x=e.worldX;for(int i=0;i<10;i++)e.update();check(e.worldX==x&&e.worldY==480,"third-zone sentry remains stationary");gp.stopGame();
 Quiet saved=new Quiet();saved.setupGame();saved.currentMap=1;saved.gameState=saved.playState;
 saved.saveLoad=new data.SaveLoad(saved,java.nio.file.Files.createTempDirectory(java.nio.file.Path.of("build"),"enemy-save-").resolve("save-v1.dat"));
 check(saved.saveLoad.trySave()&&saved.saveLoad.tryLoad(),"enemy save/load");
 int count=0;for(Entity enemy:saved.monster[1])if(enemy instanceof MON_Enfadillo){count++;int ex=enemy.worldX,ey=enemy.worldY;for(int i=0;i<160;i++)enemy.update();check(enemy.worldX==ex&&enemy.worldY==ey,"stationary after loading");}
 check(count==6,"six existing second-zone enemies retained");saved.nextArea=saved.outside;saved.changeArea();check(!saved.player.projectile.alive,"map transition clears shots");saved.stopGame();System.out.println("ENEMY_SHOT_REGRESSION PASS "+checks);
 }
}
