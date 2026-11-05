package main;

import entity.Entity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import monster.MON_Picaron;
import object.*;

import static main.ExtraRooms.MAP;
import static main.ExtraRooms.Room.*;
import static main.RefugeChallenges.*;
import static main.ExtraRoomsRegression.*;
import static main.TempleRegression.swim;
import static main.TempleRegression.route;
import static main.TempleRegression.press;

public final class LaterRoomsRegression {
    private static final Path OUT=Path.of("build/refugios-4-6-preview");
    private static void check(boolean ok,String message) {
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        var gp=new FifthZoneRegression.Quiet();
        gp.setupGame();
        Files.createDirectories(OUT);
        for (int[] door : new int[][]{{39,13,9,29,3,4},{9,17,9,19,4,6},{19,16,40,19,5,5}}) {
            var room=ExtraRooms.Room.values()[door[4]];
            transition(gp,2,door[0],door[1],MAP,room);
            check(gp.player.worldX==room.arrivalX*48 && gp.player.worldY==room.arrivalY*48,"safe arrival "+room);
            check(gp.obj[MAP][door[5]].loot instanceof OBJ_HealHP && room==PUMP ||
                gp.obj[MAP][door[5]].loot instanceof OBJ_Super_Shield && room==HONGUI ||
                gp.obj[MAP][door[5]].loot instanceof OBJ_Rock && room==GHOST,"original pair "+room);
            gp.npc[MAP][8+door[4]].speak();
            check(gp.gameState==gp.dialogueState,"NPC dialogue before challenge");
            gp.gameState=gp.playState;
            photo(gp,room.art+"-npc");
            transition(gp,MAP,door[2],door[3],2,null);
            check(gp.player.worldX==door[0]*48 && gp.player.worldY==door[1]*48,"same return");
        }
        pump(gp);
        hongui(gp);
        ghost(gp);
        corners(gp);
        for (String name : new String[]{"pump","hongui","ghost"})
            Files.copy(Path.of("build/refugios-preview/"+name+"-esquina.png"),OUT.resolve(name+"-esquina.png"),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        gp.stopGame();
        System.out.println("LATER_REFUGIOS PASS: four weapons and two binding orders, distinct guards and parries, early/late defeat, repeatable memory without optional gear, partial saves and 32 corners");
    }

    private static void photo(FifthZoneRegression.Quiet gp,String name) throws Exception {
        gp.ui.message.clear(); gp.ui.messageCounter.clear();
        FifthZoneRegression.capture(gp,OUT,name);
    }

    private static void start(GamePanel gp,ExtraRooms.Room room,int x,int y) {
        setRoom(gp,room,x,y);
        gp.extraRooms.later.resetTransient();
        gp.player.attacking=false;
        gp.player.attackCanceled=false;
    }

    private static void equip(GamePanel gp,Entity weapon) {
        if (!gp.player.inventory.contains(weapon)) gp.player.inventory.add(weapon);
        gp.player.currentWeapon=weapon;
        gp.player.attack=gp.player.getAttack();
        gp.player.getAttackImage();
    }

    private static void swing(GamePanel gp,String direction) {
        gp.player.direction=direction;
        gp.keyH.enterPressed=true;
        gp.player.update();
        gp.keyH.enterPressed=false;
        for (int tick=0;tick<90 && gp.player.attacking;tick++) gp.player.update();
    }

    private static void pump(FifthZoneRegression.Quiet gp) throws Exception {
        Entity[] weapons={new OBJ_Weapon_Normal(gp),new OBJ_Rock(gp),new OBJ_Iron(gp),new OBJ_Second_Weapon(gp)};
        int exp=gp.player.exp;
        for (Entity weapon : weapons) for (int first=0;first<2;first++) {
            gp.extraRooms.later.restore(EMPTY);
            start(gp,PUMP,10,29);
            equip(gp,weapon);
            check(route(gp,10,29)[30*50+20]<0,"Pump chest physically sealed");
            int[][] support={{13,27},{16,30}};
            int x=10,y=29;
            for (int step=0;step<2;step++) {
                int index=step==0?first:1-first;
                swim(gp,x,y,support[index][0],support[index][1]);
                swing(gp,"right");
                x=support[index][0]; y=support[index][1];
                check((gp.extraRooms.later.state().bindings()&(1<<index))!=0,"binding cut by "+weapon.name);
                check(!gp.tileM.tile[gp.tileM.mapTileNum[MAP][BINDINGS[index][0]][BINDINGS[index][1]]].collision,
                    "cut binding has no collision");
                if (step==0) {
                    roundTrip(gp,"pump-partial");
                    check(!gp.extraRooms.isOpen(PUMP),"one cut does not open reward");
                }
            }
            check(gp.extraRooms.isOpen(PUMP) && gp.player.exp==exp,"two cuts unlock without EXP");
            swim(gp,x,y,20,31);
        }
        photo(gp,"pump-reto");
        fullAndClaim(gp,PUMP,4);
        photo(gp,"pump-cofre");
    }

    private static MON_Picaron encounter(GamePanel gp,int blocks) {
        gp.extraRooms.later.restore(new State(3,blocks,false,0));
        populate(gp,gp.monster,gp.extraRooms.later.state());
        start(gp,HONGUI,16,18);
        gp.player.knockBack=false;
        gp.player.speed=gp.player.defaultSpeed;
        gp.player.invincible=false;
        gp.player.life=gp.player.maxLife;
        return (MON_Picaron)gp.monster[MAP][PICARON];
    }

    private static void arm(GamePanel gp,MON_Picaron picaron) {
        Temple.place(picaron,16,17);
        Temple.place(gp.player,16,18);
        gp.player.invincible=false;
        picaron.direction="down";
        picaron.checkAttackOrNot(1,4*48,48);
        check(picaron.attacking,"real Picaron attack cycle");
    }

    private static void guardHit(GamePanel gp,MON_Picaron picaron,boolean parry) {
        gp.keyH.spacePressed=true;
        gp.player.direction="up";
        for (int i=0;i<(parry?1:15);i++) gp.player.update();
        arm(gp,picaron);
        for (int i=0;i<40;i++) picaron.update();
        if (parry) gp.player.guardCounter=0;
        picaron.update();
    }

    private static void hongui(FifthZoneRegression.Quiet gp) throws Exception {
        gp.player.setDefaultValues(); gp.player.setItems();
        check(gp.player.currentShield instanceof OBJ_Normal_Shield,"initial shield");
        MON_Picaron picaron=encounter(gp,0);
        gp.keyH.spacePressed=true;
        gp.player.direction="up";
        for (int i=0;i<80;i++) gp.player.update();
        check(gp.extraRooms.later.state().blocks()==0,"holding guard without impact is not progress");
        int guardExp=gp.player.exp;
        guardHit(gp,picaron,false);
        check(gp.extraRooms.later.state().blocks()==1,"normal front guard counts");
        photo(gp,"hongui-reto");
        for (int tick=0;tick<2;tick++) {
            gp.player.invincible=false;
            picaron.update();
        }
        check(gp.extraRooms.later.state().blocks()==1,"same attack counted once even after another resolved impact");
        gp.keyH.releaseAll();
        roundTrip(gp,"hongui-one-block");
        check(gp.extraRooms.later.state().blocks()==1 && !gp.extraRooms.later.state().defeated(),"partial guard checkpoint");
        picaron=(MON_Picaron)gp.monster[MAP][PICARON];
        guardHit(gp,picaron,false);
        check(gp.extraRooms.isOpen(HONGUI) && !gp.extraRooms.later.state().defeated() && picaron.life==20,
            "two cycles complete without killing");
        check(gp.player.exp==guardExp,"guard victory adds no EXP");
        gp.keyH.releaseAll();
        gp.player.knockBack=false;
        gp.player.speed=gp.player.defaultSpeed;
        roundTrip(gp,"hongui-guard-complete");
        picaron=(MON_Picaron)gp.monster[MAP][PICARON];
        check(picaron!=null && gp.extraRooms.isOpen(HONGUI),"guard victory retains living enemy");
        start(gp,HONGUI,21,16);
        int safeLife=gp.player.life;
        for (int i=0;i<300;i++) gp.update();
        check(gp.player.life==safeLife,"reward alcove outside attack range");
        fullAndClaim(gp,HONGUI,6);
        photo(gp,"hongui-cofre");

        picaron=encounter(gp,0);
        int life=gp.player.life;
        guardHit(gp,picaron,true);
        check(gp.extraRooms.later.state().blocks()==1 && gp.player.life==life,"front parry counts");
        gp.keyH.releaseAll();
        roundTrip(gp,"hongui-parry-partial");
        picaron=(MON_Picaron)gp.monster[MAP][PICARON];
        guardHit(gp,picaron,true);
        check(gp.extraRooms.isOpen(HONGUI) && gp.player.life==life,"two parries complete with initial shield");
        picaron=encounter(gp,0);
        gp.player.direction="down"; gp.player.guarding=true; gp.player.guardCounter=20;
        arm(gp,picaron);
        for (int i=0;i<41;i++) picaron.update();
        check(gp.extraRooms.later.state().blocks()==0,"rear impact is not a guard");
        picaron=encounter(gp,0);
        arm(gp,picaron);
        gp.player.direction="up"; gp.player.guarding=true; gp.player.guardCounter=20; gp.player.invincible=true;
        for (int i=0;i<41;i++) picaron.update();
        check(gp.extraRooms.later.state().blocks()==0,"invulnerability is not a guard");
        for (int blocks : new int[]{0,1,2}) {
            picaron=encounter(gp,blocks);
            gp.player.nextLevelExp=1000;
            int exp=gp.player.exp;
            gp.player.damageMonster(PICARON,gp.player,100,0);
            gp.player.damageMonster(PICARON,gp.player,100,0);
            check(gp.extraRooms.later.state().defeated() && gp.extraRooms.isOpen(HONGUI),"defeat always completes");
            check(gp.player.exp==exp+15,"one normal defeat reward");
            roundTrip(gp,"hongui-defeated-"+blocks);
            gp.changeArea();
            check(gp.monster[MAP][PICARON]==null && gp.player.exp==exp+15,"defeat cannot be farmed");
        }
    }

    private static void walk(GamePanel gp,int sx,int sy,int tx,int ty) {
        int[] path=route(gp,sx,sy);
        int start=sy*50+sx,end=ty*50+tx;
        check(path[end]>=0,"memory route to "+tx+","+ty);
        var cells=new ArrayList<Integer>();
        for (int cell=end;cell!=start;cell=path[cell]) cells.add(cell);
        Collections.reverse(cells);
        Temple.place(gp.player,sx,sy);
        for (int cell : cells) {
            int x=cell%50*48,y=cell/50*48;
            String dir=x>gp.player.worldX?"right":x<gp.player.worldX?"left":y>gp.player.worldY?"down":"up";
            press(gp,dir);
            for (int i=0;i<12;i++) {
                gp.player.update();
                gp.extraRooms.later.update();
            }
            check(gp.player.worldX==x && gp.player.worldY==y,"memory path uses real collision");
        }
        press(gp,"");
    }

    private static void ghost(FifthZoneRegression.Quiet gp) throws Exception {
        gp.player.setDefaultValues(); gp.player.setItems();
        gp.extraRooms.later.restore(new State(3,2,true,0));
        start(gp,GHOST,39,19);
        check(gp.player.inventory.size()==2,"memory challenge starts with only default equipment");
        walk(gp,39,19,37,19);
        interact(gp,"up");
        check(gp.extraRooms.later.observing() && gp.extraRooms.later.displayedStone()==0,"pedestal starts triangle");
        gp.gameState=gp.pauseState;
        for (int i=0;i<60;i++) gp.update();
        check(gp.extraRooms.later.displayedStone()==0,"pause freezes demonstration");
        gp.gameState=gp.playState;
        for (int step=0;step<3;step++) {
            check(gp.extraRooms.later.displayedStone()==SEQUENCE[step],"fixed demonstration order");
            photo(gp,"ghost-demostracion-"+SEQUENCE[step]);
            if (step==0) photo(gp,"ghost-reto");
            for (int i=0;i<60;i++) gp.extraRooms.later.update();
            check(gp.extraRooms.later.displayedStone()==-1,"legible gap between symbols");
            for (int i=0;i<20;i++) gp.extraRooms.later.update();
        }
        walk(gp,37,19,37,16);
        for (int i=0;i<180;i++) gp.extraRooms.later.update();
        check(gp.extraRooms.later.state().prefix()==0,"wrong symbol and standing still cannot solve");
        walk(gp,37,16,34,16);
        check(gp.extraRooms.later.state().prefix()==1,"first complete entry counts");
        roundTrip(gp,"ghost-on-first-stone");
        for (int i=0;i<120;i++) gp.extraRooms.later.update();
        check(gp.extraRooms.later.state().prefix()==1,"loading on a stone does not count again");
        walk(gp,34,16,40,19);
        check(gp.extraRooms.later.state().prefix()==1,"leaving the room preserves partial memory");
        transition(gp,MAP,40,19,2,null);
        transition(gp,2,19,16,MAP,GHOST);
        check(gp.extraRooms.later.state().prefix()==1,"return preserves prefix before new entry");
        walk(gp,39,19,34,18);
        walk(gp,34,18,34,16);
        check(gp.extraRooms.later.state().prefix()==0,"reentering the same first stone is a new wrong answer");
        walk(gp,34,16,33,16);
        walk(gp,33,16,34,16);
        walk(gp,34,16,37,16);
        check(gp.extraRooms.later.state().prefix()==0,"wrong symbol clears a partially solved sequence");
        walk(gp,37,16,34,16);
        check(gp.extraRooms.later.state().prefix()==1,"can retry without leaving room");
        walk(gp,34,16,37,19);
        interact(gp,"up");
        walk(gp,37,19,35,20);
        check(gp.extraRooms.later.state().prefix()==1,"demonstration freezes answer recording only");
        roundTrip(gp,"ghost-during-demo");
        check(!gp.extraRooms.later.observing(),"loading cancels transient demonstration");
        for (int i=0;i<60;i++) gp.extraRooms.later.update();
        check(gp.extraRooms.later.state().prefix()==1,"stationary observer is not an answer");
        walk(gp,35,20,36,20);
        walk(gp,36,20,35,20);
        check(gp.extraRooms.later.state().prefix()==2,"exit and reentry produce one answer");
        roundTrip(gp,"ghost-two-symbols");
        walk(gp,35,20,37,16);
        check(gp.extraRooms.isOpen(GHOST),"three correct symbols open Rocky gate");
        walk(gp,37,16,29,20);
        check(gp.player.inventory.size()==2,"no optional item used to reach Rocky");
        fullAndClaim(gp,GHOST,5);
        photo(gp,"ghost-cofre");
        transition(gp,MAP,40,19,2,null);
        transition(gp,2,19,16,MAP,GHOST);
        check(gp.extraRooms.isOpen(GHOST) && gp.obj[MAP][5].opened,"memory and reward survive return");
    }
}
