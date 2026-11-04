package main;

import entity.Entity;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import javax.imageio.ImageIO;
import monster.MON_Sharky;
import object.*;
import tile_interactive.*;

/** Walk real collision rectangles along grid routes, then exercise real portals and chests. */
public final class FifthZoneRegression {
    static int checks;
    static class Quiet extends GamePanel {public void playMusic(int i){} public void playSE(int i){} public void stopMusic(){}}
    static void check(boolean valid,String message){if(!valid)throw new AssertionError(message);checks++;}
    static boolean blocked(Quiet gp,int x,int y) {
        if(x<0||y<0||x>=50||y>=50||gp.tileM.tile[gp.tileM.mapTileNum[gp.currentMap][x][y]].collision)return true;
        for(var e:gp.iTile[gp.currentMap])if(e!=null&&e.destructible&&e.worldX/48==x&&e.worldY/48==y)return true;
        for(var e:gp.obj[gp.currentMap])if(e!=null&&e.collision&&e.worldX/48==x&&e.worldY/48==y)return true;
        return false;
    }
    static List<int[]> path(Quiet gp,int sx,int sy,int tx,int ty) {
        int[][] from=new int[50][50];for(var r:from)Arrays.fill(r,-1);
        ArrayDeque<int[]> q=new ArrayDeque<>();q.add(new int[]{sx,sy});from[sx][sy]=sx+sy*50;
        while(!q.isEmpty()){
            int[] p=q.remove();if(p[0]==tx&&p[1]==ty)break;
            for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int x=p[0]+d[0],y=p[1]+d[1];
                if(x<0||y<0||x>=50||y>=50||from[x][y]!=-1||blocked(gp,x,y))continue;
                from[x][y]=p[0]+p[1]*50;q.add(new int[]{x,y});
            }
        }
        check(from[tx][ty]!=-1,"route missing map "+gp.currentMap+" -> "+tx+","+ty);
        List<int[]> result=new ArrayList<>();int x=tx,y=ty;
        while(x!=sx||y!=sy){result.add(new int[]{x,y});int n=from[x][y];x=n%50;y=n/50;}
        Collections.reverse(result);return result;
    }
    static void walk(Quiet gp,int sx,int sy,int tx,int ty) {
        gp.player.worldX=sx*48;gp.player.worldY=sy*48;gp.player.speed=4;gp.player.knockBack=false;
        for(int[] p:path(gp,sx,sy,tx,ty)) {
            int xx=p[0]*48,yy=p[1]*48;
            while(gp.player.worldX!=xx||gp.player.worldY!=yy){
                gp.player.direction=gp.player.worldX<xx?"right":gp.player.worldX>xx?"left":gp.player.worldY<yy?"down":"up";
                gp.player.collisionOn=false;gp.cChecker.checkTile(gp.player);gp.cChecker.checkEntity(gp.player,gp.iTile);gp.cChecker.checkObject(gp.player,true);
                check(!gp.player.collisionOn,"real player collision on route at "+gp.player.worldX+","+gp.player.worldY);
                switch(gp.player.direction){case "right"->gp.player.worldX+=4;case "left"->gp.player.worldX-=4;case "up"->gp.player.worldY-=4;case "down"->gp.player.worldY+=4;}
            }
        }
    }
    static void portal(Quiet gp,int map,int x,int y,int destination) {
        gp.currentMap=map;gp.player.worldX=x*48;gp.player.worldY=y*48;gp.gameState=gp.playState;
        gp.eHandler=new EventHandler(gp);gp.eHandler.checkEvent();check(gp.gameState==gp.transitionState,"portal trigger");
        for(int i=0;i<50;i++)gp.flow.update();check(gp.currentMap==destination,"portal destination");
        gp.eHandler.checkEvent();check(gp.gameState==gp.playState,"arrival retriggers portal");
    }
    static void capture(Quiet gp,Path dir,String name)throws Exception {
        gp.eManager.lighting.update();gp.drawToTempScreen();ImageIO.write(gp.tempScreen,"png",dir.resolve(name+".png").toFile());
    }

    /** Optimistic world graph: all optional obstacles/actors are ignored, but the gate stays solid.
     * Every hit->teleportMap pair is read from the real handler, including all extra-room returns. */
    static boolean[] relaxedWorld(Quiet gp) throws Exception {
        var transfers=new HashMap<Integer,Integer>();
        String source=Files.readString(Path.of("src/main/java/main/EventHandler.java"));
        Matcher m=Pattern.compile("this\\.hit\\((\\d+|FourthZone\\.MAP),\\s*(\\d+),\\s*(\\d+),\\s*\"any\"\\)\\)\\s*\\{\\s*this\\.teleportMap\\((\\d+|FourthZone\\.MAP),\\s*(\\d+),\\s*(\\d+),").matcher(source);
        while(m.find()) {
            int a=m.group(1).equals("FourthZone.MAP")?7:Integer.parseInt(m.group(1));
            int b=m.group(4).equals("FourthZone.MAP")?7:Integer.parseInt(m.group(4));
            transfers.put(cell(a,Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3))),cell(b,Integer.parseInt(m.group(5)),Integer.parseInt(m.group(6))));
        }
        check(transfers.size()==23,"all 23 actual portal directions included in reachability proof");
        boolean[] seen=new boolean[gp.maxMap*2500];ArrayDeque<Integer> queue=new ArrayDeque<>();
        int start=cell(3,25,23);seen[start]=true;queue.add(start);
        while(!queue.isEmpty()) {
            int pos=queue.remove(),map=pos/2500,x=pos%50,y=pos%2500/50;
            List<Integer> next=new ArrayList<>();
            for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int xx=x+d[0],yy=y+d[1];if(xx>=0&&yy>=0&&xx<50&&yy<50)next.add(cell(map,xx,yy));
            }
            if(transfers.containsKey(pos))next.add(transfers.get(pos));
            for(int n:next) {
                int mm=n/2500,xx=n%50,yy=n%2500/50;
                if(seen[n]||!gp.tileM.isMapLoaded(mm)||gp.tileM.tile[gp.tileM.mapTileNum[mm][xx][yy]].collision)continue;
                boolean closed=false;
                if(mm==3) for(int slot:FifthZone.GATE_SLOTS) {var t=gp.iTile[3][slot];if(t!=null&&t.destructible&&t.worldX/48==xx&&t.worldY/48==yy)closed=true;}
                if(!closed){seen[n]=true;queue.add(n);}
            }
        }
        return seen;
    }
    static int cell(int map,int x,int y){return map*2500+y*50+x;}

    static void gate(Quiet gp)throws Exception {
        check(gp.player.inventory.stream().noneMatch(e->e instanceof OBJ_Iron),"start without Iron");
        var reach=relaxedWorld(gp);
        check(reach[cell(5,28,12)],"Iron chest approach reachable without Iron, including its portal");
        check(!reach[cell(3,9,44)]&&!reach[cell(4,24,44)],"no optional route or portal bypasses the intact gate");
        for(int n=4*2500;n<5*2500;n++)check(!reach[n],"next area cannot be reached anywhere");
        check(gp.tileM.mapTileNum[3][9][5]==86&&gp.tileM.mapTileNum[3][14][40]==86,"both extra doors use original 086");
        check(gp.tileM.mapTileNum[3][9][44]==68,"main exit remains distinct");
        // Try to cross at every four-pixel offset in the two-tile passage using the real checker.
        for(int row=43*48-12;row<=45*48-12;row+=4) {
            gp.player.worldX=17*48;gp.player.worldY=row;gp.player.direction="left";gp.player.speed=4;
            for(int step=0;step<30;step++) {
                gp.player.collisionOn=false;gp.cChecker.checkTile(gp.player);gp.cChecker.checkEntity(gp.player,gp.iTile);
                if(gp.player.collisionOn)break;gp.player.worldX-=4;
            }
            check(gp.player.worldX>15*48,"no real-hitbox gap around/through gate at y="+row);
        }
        for(int slot:FifthZone.GATE_SLOTS) {
            var metal=gp.iTile[3][slot];
            for(Entity tool:new Entity[]{gp.eGenerator.requireObject("normal-bubble"),new OBJ_Rock(gp)}) {
                gp.player.currentWeapon=tool;metal.invincible=false;gp.player.damageInteractiveTile(slot);
                check(metal.life==3&&metal.destructible,"normal weapon and Rocky cannot damage mandatory metal");
            }
        }
        Path out=Path.of("build/fifth-correction-preview");Files.createDirectories(out);
        gp.player.worldX=17*48;gp.player.worldY=44*48;gp.player.currentLight=null;capture(gp,out,"barrier-intact");
        for(int[] p:new int[][]{{9,6},{14,41}}) {
            gp.player.worldX=p[0]*48;gp.player.worldY=p[1]*48;gp.player.currentLight=new OBJ_Lantern(gp);gp.player.lightUpdated=true;
            capture(gp,out,p[0]==9?"iron-door-086":"weapon-door-086");
        }
        gp.player.currentLight=null;gp.player.lightUpdated=true;
        // Obtain the real reward, walk back, then equip it explicitly. No optional walls are broken.
        walk(gp,25,23,9,5);portal(gp,3,9,5,5);walk(gp,40,9,28,12);
        OBJ_Chest reward=(OBJ_Chest)gp.obj[5][8];Entity prior=gp.player.currentWeapon;
        while(gp.player.inventory.size()<gp.player.maxinventorySize)gp.player.inventory.add(new OBJ_Rock(gp));
        reward.interact();check(!reward.opened,"full inventory preserves real Iron while mandatory gate is intact");
        gp.gameState=gp.playState;walk(gp,28,12,40,9);portal(gp,5,40,9,3);walk(gp,9,5,25,23);
        check(!relaxedWorld(gp)[cell(4,24,44)],"leaving full-bag room cannot bypass gate");
        walk(gp,25,23,9,5);portal(gp,3,9,5,5);walk(gp,40,9,28,12);
        gp.player.inventory.remove(gp.player.inventory.size()-1);reward.interact();
        check(reward.opened&&gp.player.currentWeapon==prior,"actual Iron reward does not autoequip");
        Entity iron=gp.player.inventory.stream().filter(e->e instanceof OBJ_Iron).findFirst().orElseThrow();
        gp.gameState=gp.playState;walk(gp,28,12,40,9);portal(gp,5,40,9,3);walk(gp,9,5,17,44);
        gp.player.currentWeapon=iron;
        for(int slot:FifthZone.GATE_SLOTS)for(int hit=0;hit<3;hit++) {gp.iTile[3][slot].invincible=false;gp.player.damageInteractiveTile(slot);}
        gp.player.currentWeapon=gp.eGenerator.requireObject("normal-bubble");
        walk(gp,25,23,9,44);check(relaxedWorld(gp)[cell(4,24,44)],"next area reachable after Iron, even unequipped");
        gp.player.worldX=17*48;gp.player.worldY=44*48;capture(gp,out,"barrier-open");
        reward.opened=false;reward.down=reward.image;gp.player.inventory.removeIf(e->e instanceof OBJ_Iron);
        gp.ui.npc=null;gp.keyH.enterPressed=false;gp.gameState=gp.playState;
        gp.aSetter.setInteractiveTile(); // Leave the original scenario intact for the remaining suite.
    }
    public static void main(String[] args)throws Exception {
        Quiet gp=new Quiet();gp.setupGame();gp.currentMap=3;gp.currentArea=gp.dungeons;gp.gameState=gp.playState;
        check(Arrays.stream(gp.monster[3]).filter(e->e instanceof MON_Sharky).count()==6,"six sharks");
        check(Arrays.stream(gp.monster[3]).filter(Objects::nonNull).count()==9,"nine enemies");
        check(gp.tileM.loadedMapCount()==8,"stable map IDs");
        gate(gp);
        for(int[] safe:new int[][]{{25,23},{25,20},{9,5},{14,40},{9,44},{14,9},{14,8},{17,44}}) {
            gp.player.worldX=safe[0]*48;gp.player.worldY=safe[1]*48;
            for(Entity e:gp.monster[3]) if(e!=null) {for(int t=0;t<200;t++)e.update();
                if(e instanceof MON_Sharky s)check(s.state()!=MON_Sharky.State.PREPARE&&s.state()!=MON_Sharky.State.CHARGE,"unsafe point "+Arrays.toString(safe));
                else check(!e.onPath,"unsafe Picaron aggro");
            }
        }
        gp.aSetter.setMonster();
        // No Iron and no fighting is needed to enter either original reward room or leave it.
        for(int[] dest:new int[][]{{25,20},{9,5},{14,40},{37,22},{30,10},{9,29}})walk(gp,25,23,dest[0],dest[1]);
        check(blocked(gp,14,7)&&blocked(gp,20,26)&&blocked(gp,28,30),"three metal lessons present");
        for(int i=2;i<FifthZone.TILE_SLOTS.length;i++) {
            var tile=gp.iTile[3][FifthZone.TILE_SLOTS[i]];
            gp.player.currentWeapon=new OBJ_Rock(gp);check(!tile.isCorrectItem(gp.player),"Rocky cannot break metal");
            gp.player.currentWeapon=new OBJ_Iron(gp);check(tile.isCorrectItem(gp.player)&&tile.life==3,"Iron breaks metal in three hits");
            for(int hit=0;hit<3;hit++) {
                tile.invincible=false;gp.player.damageInteractiveTile(FifthZone.TILE_SLOTS[i]);
                if(hit<2)check(tile.life==2-hit,"actual metal hit reduces life");
            }
            check(gp.iTile[3][FifthZone.TILE_SLOTS[i]] instanceof IT_Rocky,"third actual hit creates rubble");
        }
        walk(gp,9,9,14,5);walk(gp,18,26,23,26);walk(gp,29,28,29,33);
        gp.player.currentWeapon=new OBJ_Rock(gp);walk(gp,25,23,9,44);
        gp.player.worldX=14*48;gp.player.worldY=5*48;gp.player.direction="up";gp.keyH.enterPressed=false;gp.eHandler.checkEvent();
        check(gp.gameState==gp.playState,"optional Iron mark is not modal on arrival");
        gp.keyH.enterPressed=true;gp.eHandler.checkEvent();check(gp.gameState==gp.dialogueState,"optional Iron mark interactable");
        gp.gameState=gp.playState;gp.keyH.enterPressed=false;
        check(!blocked(gp,20,26),"shortcut opens");
        gp.player.currentWeapon=new OBJ_Iron(gp);check(!gp.iTile[3][4].isCorrectItem(gp.player),"Iron does not replace Rocky");
        gp.player.currentWeapon=new OBJ_Rock(gp);check(gp.iTile[3][4].isCorrectItem(gp.player),"optional Rocky stones");
        gp.iTile[3][4]=gp.iTile[3][4].getDestroyedForm();gp.iTile[3][5]=gp.iTile[3][5].getDestroyedForm();walk(gp,38,34,39,39);
        portal(gp,3,9,5,5);walk(gp,40,9,28,12);
        OBJ_Chest chest=(OBJ_Chest)gp.obj[5][8];check(chest.loot instanceof OBJ_Iron,"original Iron chest");
        while(gp.player.inventory.size()<gp.player.maxinventorySize)gp.player.inventory.add(new OBJ_Rock(gp));
        Entity equipped=gp.player.currentWeapon;chest.interact();check(!chest.opened,"full inventory preserves Iron");
        gp.gameState=gp.playState;walk(gp,28,12,40,9);portal(gp,5,40,9,3);
        portal(gp,3,9,5,5);gp.player.inventory.remove(gp.player.inventory.size()-1);chest=(OBJ_Chest)gp.obj[5][8];chest.interact();
        check(chest.opened&&gp.player.inventory.stream().anyMatch(e->e instanceof OBJ_Iron),"Iron obtained on retry");
        check(gp.player.currentWeapon==equipped,"tool not autoequipped");
        portal(gp,5,40,9,3);portal(gp,3,14,40,5);walk(gp,9,9,20,11);check(gp.obj[5][7].loot instanceof OBJ_Second_Weapon,"weapon reward retained");
        portal(gp,5,9,9,3);portal(gp,7,25,45,3);check(gp.player.worldX==25*48&&gp.player.worldY==23*48,"fourth arrival retained");
        portal(gp,3,9,44,4);portal(gp,4,24,44,3);
        Path out=Path.of("build/fifth-zone-preview");Files.createDirectories(out);
        gp.currentMap=3;gp.currentArea=gp.dungeons;gp.gameState=gp.playState;gp.aSetter.setMonster();gp.aSetter.setInteractiveTile();
        ImageIO.write(gp.map.getWorldMap(3),"png",out.resolve("map.png").toFile());
        for(int[] p:new int[][]{{25,23},{36,22},{30,10},{9,8},{16,20},{9,29},{29,32},{25,41},{14,40},{9,44}}) {
            gp.player.worldX=p[0]*48;gp.player.worldY=p[1]*48;
            gp.player.currentLight=null;capture(gp,out,p[0]+"-"+p[1]+"-fixed");
            gp.player.currentLight=new OBJ_Lantern(gp);gp.player.lightUpdated=true;capture(gp,out,p[0]+"-"+p[1]+"-lantern");
        }
        gp.stopGame();System.out.println("FIFTH_ZONE PASS: "+checks+" assertions; screenshots "+out);
    }
}
