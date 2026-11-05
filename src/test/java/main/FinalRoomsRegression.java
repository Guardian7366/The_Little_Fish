package main;

import entity.Entity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import object.OBJ_Bad_Bubble;
import object.OBJ_Iron;
import object.OBJ_Rock;
import object.OBJ_Second_Weapon;

import static main.ExtraRooms.MAP;
import static main.ExtraRooms.Room.*;
import static main.ExtraRoomsRegression.*;
import static main.FinalRefuges.*;
import static main.TempleRegression.press;
import static main.TempleRegression.swim;

public final class FinalRoomsRegression {
    private static final Path OUT=Path.of("build/refugios-7-8-preview");
    private static final String[] DIRECTIONS={"up","down","left","right"};

    private static void check(boolean ok,String message) {
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        var gp=new FifthZoneRegression.Quiet();
        gp.setupGame();
        Files.createDirectories(OUT);
        for (int[] door : new int[][]{{9,5,40,8},{14,40,9,7}}) {
            var room=door[3]==8?HANDY:IC;
            transition(gp,3,door[0],door[1],MAP,room);
            check(gp.player.worldX==room.arrivalX*48 && gp.player.worldY==9*48,"arrival outside return");
            Entity reward=gp.obj[MAP][door[3]].loot;
            check(room==HANDY ? reward instanceof OBJ_Iron && reward.type==10 :
                reward instanceof OBJ_Second_Weapon && reward.type==3 && reward.attackValue==3 &&
                    reward.motion1_duration==3 && reward.motion2_duration==15,"stable reward "+room);
            gp.npc[MAP][door[3]==8?15:14].speak();
            check(gp.gameState==gp.dialogueState,"original NPC dialogue entry");
            gp.gameState=gp.playState;
            photo(gp,room.art+"-npc");
            if (room==HANDY) checkSconce(gp);
            transition(gp,MAP,door[2],9,3,null);
            check(gp.player.worldX==door[0]*48 && gp.player.worldY==door[1]*48,"original return "+room);
        }
        handy(gp);
        ice(gp);
        corners(gp);
        for (String room : new String[]{"handy","ic"}) for (String corner : new String[]{"no","ne","so","se"}) {
            String name=room+"-esquina-"+corner+".png";
            Files.copy(Path.of("build/refugios-preview",name),OUT.resolve(name),StandardCopyOption.REPLACE_EXISTING);
        }
        gp.stopGame();
        System.out.println("FINAL_REFUGIOS PASS: two real Rocky orders, guard dodge and defeat, two ice orders, wall stops, controls, all-room isolation and full inventory");
    }

    private static void photo(FifthZoneRegression.Quiet gp,String name) throws Exception {
        gp.ui.message.clear(); gp.ui.messageCounter.clear();
        FifthZoneRegression.capture(gp,OUT,name);
    }

    private static void checkSconce(GamePanel gp) {
        for (int col : new int[]{35,37}) {
            int x=col*48+24-gp.cameraX(),y=10*48+24-gp.cameraY();
            int tile=gp.tileM.mapTileNum[MAP][col][10];
            int base=gp.tileM.tile[tile].image.getRGB(24,24);
            check((gp.tempScreen.getRGB(x,y)!=base)==(col==37),"wall occludes sconce light");
        }
    }

    private static void swing(GamePanel gp,String direction) {
        gp.player.direction=direction;
        gp.keyH.enterPressed=true;
        gp.player.update();
        gp.keyH.enterPressed=false;
        for (int i=0;i<90 && gp.player.attacking;i++) gp.player.update();
    }

    private static void equip(GamePanel gp,Entity weapon) {
        gp.player.currentWeapon=weapon;
        gp.player.attack=gp.player.getAttack();
        gp.player.getAttackImage();
    }

    private static long shots(GamePanel gp) {
        return Arrays.stream(gp.projectile[MAP]).filter(e->e instanceof OBJ_Bad_Bubble && e.alive).count();
    }

    private static void cross(FifthZoneRegression.Quiet gp,int sx,int sy,boolean live) {
        if (live) for (int tick=0;tick<120;tick++) gp.update();
        swim(gp,sx,sy,36,8);
        if (!live) {
            swim(gp,36,8,35,7);
            return;
        }
        gp.update();
        check(shots(gp)==1,"sentry fires down the crossing");
        int health=gp.player.life;
        press(gp,"left");
        for (int i=0;i<12;i++) gp.update();
        press(gp,"up");
        for (int i=0;i<12;i++) gp.update();
        press(gp,"");
        check(gp.player.worldX==35*48 && gp.player.worldY==7*48 && gp.player.life==health,
            "sentry crossed without damage or killing");
    }

    private static void handy(FifthZoneRegression.Quiet gp) throws Exception {
        gp.player.setDefaultValues();
        gp.player.nextLevelExp=1000;
        Entity rocky=new OBJ_Rock(gp);
        gp.player.inventory.add(rocky);
        for (int first=0;first<2;first++) {
            gp.extraRooms.ending.restore(EMPTY);
            populate(gp,gp.monster,EMPTY);
            transition(gp,3,9,5,MAP,HANDY);
            int life=gp.player.life;
            for (int i=0;i<120;i++) gp.update();
            check(shots(gp)==0 && gp.player.life==life,"arrival and NPC out of firing line");
            check(!FifthZoneRegression.relaxedWorld(gp)[FifthZoneRegression.cell(5,28,12)],"coffer locked");
            int x=39,y=9;
            boolean west=false;
            for (int step=0;step<2;step++) {
                int latch=step==0?first:1-first;
                if (latch==0 && !west) {
                    cross(gp,x,y,true);
                    x=35; y=7; west=true;
                    if (first==1) {
                        int xp=gp.player.exp;
                        swim(gp,x,y,33,8);
                        gp.player.damageMonster(SENTRY,gp.player,100,0);
                        gp.player.damageMonster(SENTRY,gp.player,100,0);
                        check(gp.extraRooms.ending.state().sentryDefeated() && gp.player.exp==xp+7,"one normal sentry reward");
                        roundTrip(gp,"handy-sentry");
                        gp.changeArea();
                        check(gp.monster[MAP][SENTRY]==null && gp.player.exp==xp+7,"sentry defeat persists");
                        x=33; y=8;
                    }
                } else if (latch==1 && west) {
                    swim(gp,x,y,35,7);
                    int health=gp.player.life;
                    for (String d : new String[]{"down","right","right","down"}) {
                        press(gp,d);
                        for (int i=0;i<12;i++) gp.update();
                    }
                    press(gp,"");
                    check(gp.player.worldX==37*48 && gp.player.worldY==9*48 && gp.player.life==health,
                        "lateral exit on return");
                    x=37; y=9; west=false;
                }
                int tx=latch==0?33:38,ty=latch==0?6:11;
                swim(gp,x,y,tx,ty); x=tx; y=ty;
                Entity normal=gp.player.inventory.stream().filter(e->e.type==3).findFirst().orElseThrow();
                int previous=gp.extraRooms.ending.state().latches();
                for (Entity wrong : new Entity[]{normal,new OBJ_Iron(gp),new OBJ_Second_Weapon(gp)}) {
                    equip(gp,wrong);
                    swing(gp,"left");
                    check(gp.extraRooms.ending.state().damaged()==0 && gp.extraRooms.ending.state().latches()==previous,
                        "wrong weapon cannot damage stone even with Rocky in bag");
                }
                rocky=gp.player.inventory.stream().filter(OBJ_Rock.class::isInstance).findFirst().orElseThrow();
                equip(gp,rocky);
                int experience=gp.player.exp;
                swing(gp,"left");
                check(gp.extraRooms.ending.state().damaged()==1<<latch && gp.extraRooms.ending.state().latches()==previous,
                    "first swing registers one hit");
                roundTrip(gp,"handy-hit-partial");
                check(gp.extraRooms.ending.state().damaged()==1<<latch,"one-hit latch survives load");
                swing(gp,"left");
                check(gp.extraRooms.ending.state().latches()==(previous|(1<<latch)) && gp.player.exp==experience,
                    "second swing breaks latch without reward");
                check(gp.tileM.mapTileNum[MAP][LATCHES[latch][0]][LATCHES[latch][1]]==LATCH_BROKEN &&
                    !gp.tileM.tile[LATCH_BROKEN].collision,"broken latch is walkable");
                if (step==0) {
                    check(!gp.extraRooms.isOpen(HANDY),"one latch keeps coffer locked");
                    photo(gp,"handy-reto");
                }
            }
            check(gp.extraRooms.isOpen(HANDY),"both latch orders open coffer");
            if (!west) { cross(gp,x,y,false); x=35; y=7; }
            swim(gp,x,y,28,12);
            if (first==1) {
                fullAndClaim(gp,HANDY,8);
                photo(gp,"handy-cofre");
            }
        }
        Temple.place(gp.player,28,12);
        swim(gp,28,12,40,9);
        transition(gp,MAP,40,9,3,null);
        check(!FifthZoneRegression.relaxedWorld(gp)[FifthZoneRegression.cell(8,25,43)],"fifth Iron barrier still compulsory");
    }

    private record Point(int x,int y) {
        static Point tile(int x,int y) { return new Point(x*48,y*48); }
    }
    private record Move(String direction,Point end) {}
    private record Network(Map<Point,List<Move>> paths,Set<Point> stops,Set<Point> wallStops) {}
    private static Point position(GamePanel gp) { return new Point(gp.player.worldX,gp.player.worldY); }

    private static void place(GamePanel gp,Point point) {
        gp.player.worldX=point.x;
        gp.player.worldY=point.y;
    }

    private static void finish(GamePanel gp) {
        for (int i=0;i<200 && gp.extraRooms.ending.sliding();i++) gp.player.update();
        check(!gp.extraRooms.ending.sliding(),"slide reaches a stop");
    }

    private static boolean command(GamePanel gp,String direction) {
        gp.keyH.releaseAll();
        gp.eHandler.canTouchEvent=false;
        gp.eHandler.previousEventX=gp.player.worldX;
        gp.eHandler.previousEventY=gp.player.worldY;
        press(gp,direction);
        gp.player.update();
        boolean slide=gp.extraRooms.ending.sliding();
        finish(gp);
        return slide;
    }

    private static Network explore(GamePanel gp,int marks) {
        State initial=new State(3,0,marks,true);
        gp.extraRooms.ending.restore(initial);
        Map<Point,List<Move>> graph=new HashMap<>();
        Set<Point> stops=new HashSet<>(),walls=new HashSet<>();
        ArrayDeque<Point> pending=new ArrayDeque<>();
        Point arrival=Point.tile(10,9);
        pending.add(arrival);
        graph.put(arrival,new ArrayList<>());
        while (!pending.isEmpty()) {
            Point from=pending.remove();
            List<Move> moves=graph.get(from);
            for (String direction : DIRECTIONS) {
                place(gp,from);
                boolean slide=command(gp,direction);
                Point end=position(gp);
                if (slide) {
                    stops.add(end);
                    int tile=gp.tileM.mapTileNum[MAP][(end.x+24)/48][(end.y+24)/48];
                    if (tile==ICE_A || tile==ICE_B) walls.add(end);
                    gp.player.update();
                    check(position(gp).equals(end),"repeated key cannot leave stop");
                }
                check(Temple.clear(gp,gp.player,end.x,end.y),"all stops physically valid");
                if (!end.equals(from)) {
                    moves.add(new Move(direction,end));
                    if (!graph.containsKey(end)) {
                        graph.put(end,new ArrayList<>());
                        pending.add(end);
                    }
                }
                if (gp.extraRooms.ending.state().marks()!=marks) gp.extraRooms.ending.restore(initial);
            }
        }
        check(!walls.isEmpty(),"enumerates actual wall stops");
        return new Network(graph,stops,walls);
    }

    private static void allReach(Network network,Point target) {
        check(network.paths.containsKey(target),"target is reachable");
        Map<Point,List<Point>> reverse=new HashMap<>();
        for (var entry : network.paths.entrySet()) for (Move move : entry.getValue())
            reverse.computeIfAbsent(move.end,k->new ArrayList<>()).add(entry.getKey());
        Set<Point> seen=new HashSet<>();
        var pending=new ArrayDeque<Point>();
        seen.add(target); pending.add(target);
        while (!pending.isEmpty())
            for (Point from : reverse.getOrDefault(pending.remove(),List.of())) if (seen.add(from)) pending.add(from);
        check(seen.size()==network.paths.size(),"every position and wall stop has a route to "+target);
    }

    private static void travel(GamePanel gp,Network network,Point target,Point avoid) {
        Point from=position(gp);
        var pending=new ArrayDeque<Point>();
        var previous=new HashMap<Point,Point>();
        var incoming=new HashMap<Point,Move>();
        pending.add(from); previous.put(from,from);
        while (!pending.isEmpty() && !previous.containsKey(target)) {
            Point here=pending.remove();
            for (Move move : network.paths.get(here)) if (!move.end.equals(avoid) && !previous.containsKey(move.end)) {
                previous.put(move.end,here);
                incoming.put(move.end,move);
                pending.add(move.end);
            }
        }
        check(previous.containsKey(target),"planned route avoids other mark");
        List<Move> steps=new ArrayList<>();
        for (Point p=target;!p.equals(from);p=previous.get(p)) steps.add(incoming.get(p));
        Collections.reverse(steps);
        for (Move move : steps) {
            command(gp,move.direction);
            check(position(gp).equals(move.end),"actual motion follows verified route");
        }
    }

    private static void ice(FifthZoneRegression.Quiet gp) throws Exception {
        gp.player.setDefaultValues();
        transition(gp,3,14,40,MAP,IC);
        gp.extraRooms.ending.restore(new State(3,0,0,true));
        Temple.place(gp.player,11,9);
        press(gp,"right");
        for (int i=0;i<4;i++) gp.player.update();
        check(!gp.extraRooms.ending.sliding(),"partial ice entry is still normal motion");
        for (int i=0;i<20 && !gp.extraRooms.ending.sliding();i++) gp.player.update();
        check(gp.extraRooms.ending.sliding(),"complete ice entry retains direction");
        photo(gp,"ic-reto");
        Point moving=position(gp);
        gp.keyH.upPressed=true; gp.keyH.leftPressed=true;
        gp.player.update();
        check(position(gp).equals(new Point(moving.x+4,moving.y)),"opposite and diagonal input cannot turn or accelerate");
        Point paused=position(gp);
        for (int state : new int[]{gp.pauseState,gp.characterState,gp.optionsState}) {
            gp.gameState=state;
            for (int i=0;i<20;i++) gp.update();
            check(position(gp).equals(paused) && gp.extraRooms.ending.sliding(),"menu freezes slide");
        }
        gp.gameState=gp.playState;
        gp.keyH.releaseAll();
        gp.player.update();
        check(position(gp).equals(new Point(paused.x+4,paused.y)),"focus release clears held keys but inertia remains");
        Point mid=position(gp);
        roundTrip(gp,"ic-mid-slide");
        gp.player.update();
        check(position(gp).equals(mid) && !gp.extraRooms.ending.sliding(),"load preserves position and cancels inertia");

        Network closed=explore(gp,0),open=explore(gp,3);
        Point exit=Point.tile(9,9),coffer=Point.tile(21,11);
        allReach(closed,exit);
        allReach(open,exit);
        allReach(open,coffer);
        check(!closed.paths.containsKey(coffer),"closed gate isolates reward");
        System.out.printf("IC NETWORK: closed=%d positions, %d stops, %d wall stops; open=%d positions, %d stops, %d wall stops%n",
            closed.paths.size(),closed.stops.size(),closed.wallStops.size(),open.paths.size(),open.stops.size(),open.wallStops.size());

        for (int first=0;first<2;first++) {
            gp.extraRooms.ending.restore(new State(3,0,0,true));
            Temple.place(gp.player,10,9);
            Point one=Point.tile(MARKS[first][0],MARKS[first][1]);
            Point two=Point.tile(MARKS[1-first][0],MARKS[1-first][1]);
            travel(gp,closed,one,two);
            check(gp.extraRooms.ending.state().marks()==1<<first,"first mark after physical landing");
            roundTrip(gp,"ic-first-mark");
            check(gp.extraRooms.ending.state().marks()==1<<first && !gp.extraRooms.isOpen(IC),"partial mark restored");
            travel(gp,closed,two,null);
            check(gp.extraRooms.isOpen(IC),"both mark orders open gate");
            travel(gp,open,coffer,null);
            if (first==1) {
                check(gp.player.inventory.size()==2,"only starting equipment before reward");
                fullAndClaim(gp,IC,7);
                photo(gp,"ic-cofre");
            }
        }
        transition(gp,MAP,9,9,3,null);
        transition(gp,3,14,40,MAP,IC);
        check(gp.extraRooms.isOpen(IC) && gp.obj[MAP][7].opened,"both marks and claimed coffer survive return");
    }
}
