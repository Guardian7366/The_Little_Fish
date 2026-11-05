package main;

import data.SaveLoad;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import entity.Entity;
import monster.MON_Flower;
import monster.MON_Secret;
import monster.MON_Tella;
import object.OBJ_Lantern;
import static main.FishtellaRegression.check;

public final class TempleRegression {
    private static final int[][] STEPS={{0,-1},{0,1},{-1,0},{1,0}};
    private static int orders;

    public static void main(String[] args) throws Exception {
        var gp=new FifthZoneRegression.Quiet();
        gp.setupGame();
        FifthZoneRegression.portal(gp,8,25,3,4);
        check(gp.player.worldX==24*48 && gp.player.worldY==42*48,"temple landing");
        Path out=Path.of("build/temple-preview");
        Files.createDirectories(out);
        capture(gp,out,"landing-fixed",24,40,false);
        capture(gp,out,"landing-lantern",24,40,true);
        capture(gp,out,"mural-d",24,10,true);
        gp.player.direction="up"; gp.keyH.enterPressed=true;
        gp.eHandler.checkEvent();
        check(gp.gameState==gp.dialogueState,"D mural opens dialogue from adjacent floor");
        gp.keyH.enterPressed=false; gp.gameState=gp.playState;

        combat(gp,out);
        secrets(gp,out);
        Arrays.fill(gp.monster[4],null);
        gp.temple.reliefs=0; gp.temple.syncReliefs();
        permute(gp,out,new int[4],0,0);
        check(orders==24,"all resolution orders");
        var first=Temple.LANES[0];
        var second=Temple.LANES[1];
        Entity rock=gp.npc[4][first.rockSlot()];
        rock.linkedEntity=gp.iTile[4][second.plateSlot()];
        Temple.place(gp.player,24,5);
        gp.eHandler.checkEvent();
        check(!gp.temple.solved() && gp.gameState==gp.playState,"four occupied positions with a wrong identity still block the portal");
        rock.linkedEntity=gp.iTile[4][first.plateSlot()];
        FifthZoneRegression.portal(gp,4,24,5,9);
        FifthZoneRegression.portal(gp,4,24,44,8);
        gp.stopGame();
        System.out.println("TEMPLE PASS: 24 orders, live push collision, partial checkpoints, vines, mixed encounter, two unique Secrets and portals");
    }

    static void capture(FifthZoneRegression.Quiet gp,Path out,String name,int x,int y,boolean lamp) throws Exception {
        Temple.place(gp.player,x,y);
        gp.player.currentLight=lamp?new OBJ_Lantern(gp):null;
        gp.player.lightUpdated=true;
        gp.ui.message.clear();
        gp.ui.messageCounter.clear();
        FifthZoneRegression.capture(gp,out,name);
        gp.player.currentLight=null;
        gp.player.lightUpdated=true;
    }

    static void press(GamePanel gp,String dir) {
        gp.keyH.upPressed=dir.equals("up"); gp.keyH.downPressed=dir.equals("down");
        gp.keyH.leftPressed=dir.equals("left"); gp.keyH.rightPressed=dir.equals("right");
    }

    private static boolean open(GamePanel gp,int x,int y) {
        return x>=0 && x<50 && y>=0 && y<50 && Temple.clear(gp,gp.player,x*48,y*48);
    }

    static int[] route(GamePanel gp,int sx,int sy) {
        int[] previous=new int[2500];
        Arrays.fill(previous,-1);
        ArrayDeque<Integer> queue=new ArrayDeque<>();
        int start=sy*50+sx;
        previous[start]=start; queue.add(start);
        while(!queue.isEmpty()) {
            int cell=queue.remove();
            for(int[] step:STEPS) {
                int x=cell%50+step[0],y=cell/50+step[1];
                if(x<0 || x>=50 || y<0 || y>=50 || previous[y*50+x]>=0 || !open(gp,x,y)) continue;
                previous[y*50+x]=cell;
                queue.add(y*50+x);
            }
        }
        return previous;
    }

    static void swim(GamePanel gp,int sx,int sy,int tx,int ty) {
        int[] route=route(gp,sx,sy);
        int start=sy*50+sx,target=ty*50+tx;
        check(route[target]>=0,"route to "+tx+","+ty);
        ArrayList<Integer> steps=new ArrayList<>();
        for(int cell=target;cell!=start;cell=route[cell])steps.add(cell);
        Collections.reverse(steps);
        Temple.place(gp.player,sx,sy);
        for(int cell:steps) {
            int x=cell%50*48,y=cell/50*48;
            String dir=x>gp.player.worldX?"right":x<gp.player.worldX?"left":y>gp.player.worldY?"down":"up";
            press(gp,dir);
            for(int tick=0;tick<12;tick++)gp.player.update();
            check(gp.player.worldX==x && gp.player.worldY==y,"real player collision at "+cell);
        }
        press(gp,"");
    }

    static void pushLane(GamePanel gp,Temple.Lane lane) {
        Entity rock=gp.npc[4][lane.rockSlot()];
        int px=lane.x()-lane.dx(),py=lane.y()-lane.dy();
        swim(gp,24,42,px,py);
        for(int step=0;step<5;step++) {
            int oldX=rock.worldX,oldY=rock.worldY;
            press(gp,lane.direction());
            for(int i=0;i<18 && rock.worldX==oldX && rock.worldY==oldY;i++)gp.player.update();
            press(gp,"");
            check(rock.worldX==oldX+lane.dx()*48 && rock.worldY==oldY+lane.dy()*48,"one aligned push");
            int rearX=oldX/48-lane.dx(),rearY=oldY/48-lane.dy();
            check(route(gp,rearX,rearY)[42*50+24]>=0,"side passage from each stage");
        }
        check(Temple.paired(rock,gp.iTile[4][lane.plateSlot()],lane),"correct rune pair");
        check(route(gp,lane.endX()+lane.dx(),lane.endY()+lane.dy())[42*50+24]>=0,"side passage from the far end");
        swim(gp,lane.endX()-lane.dx(),lane.endY()-lane.dy(),24,42);
    }

    private static void permute(FifthZoneRegression.Quiet gp,Path out,int[] order,int at,int used) throws Exception {
        if(at<4) {
            for(int i=0;i<4;i++)if((used&(1<<i))==0) {
                order[at]=i; permute(gp,out,order,at+1,used|(1<<i));
            }
            return;
        }
        gp.currentMap=4; gp.gameState=gp.playState;
        Temple.fixtures(gp,gp.npc,gp.iTile);
        gp.obj[4][0]=Temple.gate(gp);
        gp.eHandler=new EventHandler(gp);
        for(int k=0;k<4;k++) {
            Temple.Lane lane=Temple.LANES[order[k]];
            Entity rock=gp.npc[4][lane.rockSlot()];
            for(int[] offset:STEPS) {
                if(offset[0]==-lane.dx() && offset[1]==-lane.dy())continue;
                int x=lane.x()+offset[0],y=lane.y()+offset[1];
                check(open(gp,x,y),"each wrong-side test stands on reachable floor");
                Temple.place(gp.player,x,y);
                press(gp,offset[0]==1?"left":offset[0]==-1?"right":offset[1]==1?"up":"down");
                for(int i=0;i<14;i++)gp.player.update();
                press(gp,"");
                check(rock.worldX==lane.x()*48 && rock.worldY==lane.y()*48,"wrong-side push blocked");
            }
            check(route(gp,24,42)[5*50+24]<0,"no path to boss with fewer than four pairs");
            Temple.place(gp.player,24,5);
            gp.eHandler.checkEvent();
            check(gp.gameState==gp.playState,"boss event checks pairs even at trigger");
            pushLane(gp,lane);
            check(gp.temple.solved()==(k==3),"gate waits for all exact pairs");
            if(orders==0) {
                if(k==0)capture(gp,out,"plate-active",lane.endX()-lane.dx(),lane.endY()-lane.dy(),true);
                if(k==2)capture(gp,out,"gate-closed",24,6,true);
                Path folder=Files.createTempDirectory(Path.of("build"),"temple-order-");
                SaveLoad save=new SaveLoad(gp,folder.resolve("save-v1.dat"));
                Temple.place(gp.player,24,42);
                check(save.trySave(),save.getLastError());
                Temple.fixtures(gp,gp.npc,gp.iTile);
                check(save.tryLoad(),save.getLastError());
                check(gp.temple.solved()==(k==3),"partial and completed saves preserve the gate");
                for(int i=0;i<=k;i++){
                    var p=Temple.LANES[order[i]];
                    check(Temple.paired(gp.npc[4][p.rockSlot()],gp.iTile[4][p.plateSlot()],p),"saved pair remains linked");
                }
                Arrays.fill(gp.monster[4],null);
            }
        }
        check(gp.obj[4][0]==null && route(gp,24,42)[5*50+24]>=0,"open gate is physically passable");
        if(orders==0)capture(gp,out,"gate-open",24,6,true);
        swim(gp,24,42,24,6);
        orders++;
    }

    private static void combat(FifthZoneRegression.Quiet gp,Path out) throws Exception {
        MON_Flower plant=(MON_Flower)gp.monster[4][49];
        int healthy=gp.player.life=gp.player.maxLife;
        Temple.place(gp.player,24,41);
        for(int i=0;i<45;i++)plant.update();
        check(plant.vine()==MON_Flower.Vine.AIM,"isolated plant starts warning");
        capture(gp,out,"vine-aim",24,41,false);
        gp.gameState=gp.pauseState;
        for(int i=0;i<60;i++)plant.update();
        check(plant.vine()==MON_Flower.Vine.AIM,"pause freezes vine");
        gp.gameState=gp.playState;
        for(int i=0;i<40;i++)plant.update();
        check(plant.vine()==MON_Flower.Vine.EXTEND,"vine extends");
        capture(gp,out,"vine-extend",24,41,true);
        while(plant.vine()==MON_Flower.Vine.EXTEND)plant.update();
        check(gp.temple.attached(plant),"vine catches player");
        var other=new MON_Flower(gp);
        check(!gp.temple.attach(other),"second plant cannot attach");
        int start=gp.player.worldY;
        press(gp,"up"); gp.keyH.enterPressed=true;
        for(int i=0;i<20 && gp.temple.pulled();i++) {
            gp.player.update(); plant.update();
        }
        press(gp,"");
        check(gp.player.worldY<start && !gp.temple.pulled() && plant.vine()==MON_Flower.Vine.RETRACT &&
            gp.player.life==healthy,"real melee cuts the active tether before capullo damage");
        capture(gp,out,"vine-retract",24,40,true);
        gp.player.attacking=false;
        gp.player.spriteCounter=0;

        plant.resetVine();
        Temple.place(gp.player,24,41);
        for(int i=0;i<81;i++)plant.update();
        gp.player.guarding=true; gp.player.guardCounter=0; gp.player.direction="up";
        while(plant.vine()==MON_Flower.Vine.EXTEND)plant.update();
        check(!gp.temple.pulled() && plant.vine()==MON_Flower.Vine.RETRACT,"frontal parry releases");
        gp.player.guarding=false;

        plant.resetVine();
        int floor=gp.tileM.mapTileNum[4][24][40];
        gp.tileM.setTerrainCell(4,24,40,Temple.WALL);
        for(int i=0;i<50;i++)plant.update();
        check(plant.vine()==MON_Flower.Vine.IDLE,"wall blocks detection and vine");
        gp.tileM.setTerrainCell(4,24,40,floor);
        plant.resetVine();
        for(int i=0;i<100 && !gp.temple.pulled();i++)plant.update();
        check(gp.temple.pulled(),"second catch");
        int heldY=gp.player.worldY;
        gp.tileM.setTerrainCell(4,24,40,Temple.WALL);
        plant.update();
        check(!gp.temple.pulled() && gp.player.worldY==heldY,"new obstruction interrupts pull without moving through it");
        gp.tileM.setTerrainCell(4,24,40,floor);
        plant.resetVine();
        for(int i=0;i<100 && !gp.temple.pulled();i++)plant.update();
        check(gp.temple.pulled(),"catch before reload");
        gp.tileM.reloadCurrentMap();
        check(!gp.temple.pulled() && plant.vine()==MON_Flower.Vine.IDLE,"map reload clears tether");
        plant.resetVine();
        for(int i=0;i<100 && !gp.temple.pulled();i++)plant.update();
        check(gp.temple.pulled(),"catch before death");
        gp.player.damageMonster(49,gp.player,9999,0);
        check(!gp.temple.pulled(),"death releases player");
        gp.aSetter.setMonster();
        gp.gameState=gp.playState;
        plant=(MON_Flower)gp.monster[4][49];
        gp.player.maxLife=gp.player.life=120;
        gp.player.invincible=false;
        Temple.place(gp.player,24,41);
        for(int i=0;i<100 && !gp.temple.pulled();i++)plant.update();
        check(gp.temple.pulled(),"catch before contact");
        for(int i=0;i<24 && gp.temple.pulled();i++)plant.update();
        check(!gp.temple.pulled() && gp.player.life<120,"pull reaches the flower and releases after one hit");
        int life=gp.player.life;
        gp.player.invincible=false;
        gp.player.contactMonster(49);
        check(gp.player.life==life,"flower body does not add contact damage");
        gp.player.maxLife=gp.player.life=6;
        gp.player.knockBack=false;
        gp.player.speed=4;
        gp.aSetter.setMonster();
        gp.gameState=gp.playState;

        var mixed=(MON_Flower)gp.monster[4][51];
        var tella=(MON_Tella)gp.monster[4][56];
        Temple.place(gp.player,30,32);
        for(int i=0;i<45;i++) { mixed.update(); tella.update(); }
        check(mixed.vine()==MON_Flower.Vine.AIM && tella.phase()==MON_Tella.Phase.WARNING,"mixed threats prepared: "+mixed.vine()+"/"+tella.phase()+" state="+gp.gameState);
        capture(gp,out,"mixed",30,32,true);
    }

    private static void secrets(FifthZoneRegression.Quiet gp,Path out) throws Exception {
        for(int i=0;i<2;i++) {
            var secret=(MON_Secret)gp.monster[4][Temple.SECRET_SLOTS[i]];
            int x=i==0?8:41,y=i==0?21:23;
            Temple.place(gp.player,x,y);
            gp.player.direction=i==0?"left":"right";
            gp.keyH.enterPressed=true;
            gp.eHandler.checkEvent();
            gp.keyH.enterPressed=false;
            check(gp.temple.reliefs==(1<<(i+1))-1,"reliefs open independently");
            check(!gp.tileM.tile[gp.tileM.mapTileNum[4][Temple.RELIEFS[i][0]][y]].collision,"opened niche passable");
            Temple.place(gp.player,i==0?5:44,y);
            int peak=0;
            for(int t=0;t<181;t++) {
                secret.update();
                peak=Math.max(peak,secret.decoys());
                check(secret.decoys()<=2,"at most two harmless decoys");
            }
            check(secret.escape()==MON_Secret.Escape.TIRED,"180 ticks of fatigue across direction changes");
            check(peak==2,"both decoy frames participate in the escape");
            if(i==0)capture(gp,out,"secret-tired",5,y,true);
            int tiredX=secret.worldX,tiredY=secret.worldY;
            for (int t=0;t<89;t++) secret.update();
            check(secret.escape()==MON_Secret.Escape.TIRED && secret.worldX==tiredX && secret.worldY==tiredY &&
                secret.decoys()==0,"rest holds position and decoys expire");
            secret.update();
            check(secret.escape()==MON_Secret.Escape.HIDDEN,"rest lasts 90 ticks");
            int exp=gp.player.exp;
            gp.player.damageMonster(Temple.SECRET_SLOTS[i],gp.player,9999,0);
            check(gp.player.exp==exp+100,"exactly 100 EXP for each Secret");
            secret.invincible=false;
            gp.player.damageMonster(Temple.SECRET_SLOTS[i],gp.player,9999,0);
            check(gp.player.exp==exp+100,"dying enemy cannot pay again");
            gp.changeArea();
            gp.gameState=gp.playState;
            check(gp.monster[4][Temple.SECRET_SLOTS[i]]==null,"defeated Secret stays absent after travel");
        }
        Path folder=Files.createTempDirectory(Path.of("build"),"temple-secrets-");
        SaveLoad save=new SaveLoad(gp,folder.resolve("save-v1.dat"));
        check(save.trySave() && save.tryLoad(),"secret progress roundtrip");
        check(gp.temple.defeats==3 && gp.temple.reliefs==3,"both independent flags survive");
        gp.changeArea();
        check(gp.monster[4][81]==null && gp.monster[4][82]==null,"no reappearance on load or travel");
        gp.resetGame(true);
        gp.currentMap=4; gp.gameState=gp.playState; gp.currentArea=gp.dungeons;
        check(gp.temple.defeats==0 && gp.monster[4][81]!=null && gp.monster[4][82]!=null,"new game resets both bonuses");
    }
}
