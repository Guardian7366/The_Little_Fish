package main;

import data.Progress;
import data.SaveLoad;
import entity.Entity;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.function.BooleanSupplier;
import monster.MON_GodFish;
import monster.FinalSummon;
import monster.FinalSummon.Kind;
import object.OBJ_Key;

import static java.awt.event.KeyEvent.*;
import static main.FinalArena.*;

public final class FinalArenaRegression {
    private static final class Quiet extends FifthZoneRegression.Quiet {
        int music23, lastMusic;
        @Override public void playMusic(int id) { lastMusic=id; if (id==23) music23++; }
    }

    private static final Path OUT=Path.of("build/esbirros-preview");
    private static final Quiet gp=new Quiet();
    private static final Field PLAN=field("plan"), STEP=field("step"), FLAGS=field("flags");

    private static Field field(String name) {
        try {
            var field=MON_GodFish.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }

    private static Object read(Field field) {
        try { return field.get(gp.finalArena.boss()); }
        catch (IllegalAccessException error) { throw new AssertionError(error); }
    }

    private static Object plan(String field) {
        Object plan=read(PLAN);
        if (plan==null) return null;
        try {
            Method accessor=plan.getClass().getDeclaredMethod(field);
            accessor.setAccessible(true);
            return accessor.invoke(plan);
        } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }

    private static boolean at(String attack,String stage) {
        return attack.equals(String.valueOf(plan("move"))) && stage.equals(String.valueOf(read(STEP)));
    }

    private static void check(boolean ok,String message) {
        if (!ok) throw new AssertionError(message + " at " + gp.currentMap + ":" + gp.player.worldX + "," + gp.player.worldY
            + " state=" + gp.gameState + " life=" + gp.player.life + " phase="
            + (gp.finalArena.boss()==null ? "-" : gp.finalArena.boss().phase()));
    }

    private static void tick(int count) { for (int i=0;i<count;i++) gp.update(); }

    private static void until(BooleanSupplier done,int limit,String label) {
        for (int i=0; i<limit && !done.getAsBoolean(); i++) gp.update();
        check(done.getAsBoolean(),label);
    }

    private static void key(int code,boolean pressed) {
        var event=new KeyEvent(gp,pressed?KEY_PRESSED:KEY_RELEASED,0,0,code,CHAR_UNDEFINED);
        if (pressed) gp.keyH.keyPressed(event); else gp.keyH.keyReleased(event);
    }
    private static void tap(int key) { key(key,true); tick(1); key(key,false); }
    private static void capture(String name) throws Exception { FifthZoneRegression.capture(gp,OUT,name); }
    private static MON_GodFish boss() { return gp.finalArena.boss(); }

    private static void walk(int target,boolean horizontal) {
        int coordinate=horizontal?gp.player.worldX:gp.player.worldY;
        if (Math.abs(coordinate-target)<=3) return;
        boolean positive=target>coordinate;
        int key=horizontal?(positive?VK_D:VK_A):(positive?VK_S:VK_W);
        int startMap=gp.currentMap;
        key(key,true);
        try {
            until(() -> {
                int current=horizontal?gp.player.worldX:gp.player.worldY;
                return (positive?current>=target-3:current<=target+3)
                    || gp.gameState!=gp.playState || gp.currentMap!=startMap;
            },300,"reachable walking target " + target);
        } finally { key(key,false); }
    }

    private static void xy(int col,int row) {
        walk(col*48,true);
        walk(row*48,false);
    }

    private static void equip(String name) {
        int slot=gp.player.searchItemInInventory(name);
        check(slot!=999,"inventory contains " + name);
        tap(VK_C);
        gp.ui.playerSlotCol=slot%5;
        gp.ui.playerSlotRow=slot/5;
        tap(VK_ENTER);
        if (gp.gameState==gp.characterState) tap(VK_C);
        finishDialogue();
    }

    private static void face(int direction) {
        tap(direction);
        tick(1);
    }

    private static void strike() {
        tap(VK_ENTER);
        until(() -> !gp.player.attacking || gp.gameState!=gp.playState,90,"melee swing completes");
    }

    private static void finishDialogue() {
        for (int i=0; i<20 && gp.gameState==gp.dialogueState; i++) tap(VK_ENTER);
    }

    private static void marketToArena() {
        check(gp.currentMap==Market.MAP,"start in market");
        xy(24,19);
        until(() -> gp.currentMap==MAP && gp.gameState==gp.playState,55,"north arch leads to arena");
        check(gp.player.worldX==ARRIVAL_X*48 && gp.player.worldY==ARRIVAL_Y*48,"original landing");
    }

    private static void introduction(boolean abbreviated) throws Exception {
        walk(THRESHOLD_Y*48-8,false);
        check(gp.gameState==gp.cutsceneState && gp.csManager.sceneNum==gp.csManager.GodFish,"threshold starts scene");
        check(gp.obj[MAP][SEAL_SLOT]!=null && !gp.bossBattleOn && gp.player.drawing,"door seals behind visible player");
        int x=gp.player.worldX,y=gp.player.worldY,camera=gp.cameraY();
        key(VK_ENTER,true);
        tick(44);
        check(gp.cameraY()!=camera && gp.player.worldX==x && gp.player.worldY<y,"camera and player advance independently");
        if (!abbreviated) { tick(29); capture("01-presencia"); }
        for (int i=0; i<85; i++) {
            key(VK_ENTER,true);
            gp.update();
        }
        check(gp.csManager.isDialogueActive() && boss().dialogueIndex==(abbreviated?3:0),"held Enter cannot skip pages");
        key(VK_ENTER,false);
        y=gp.player.worldY;
        check(y==INTRO_Y,"player reaches the final shot by swimming on the floor");
        if (abbreviated) {
            until(() -> gp.ui.currentDialogue.replace('\n',' ').equals(
                "Come closer, D. Show me what you came here for."),100,"retry shows the fourth lore page");
        } else {
            tick(40);
            capture("02-reconocimiento");
        }
        int pages=abbreviated?1:4;
        for (int page=0;page<pages;page++) {
            int index=boss().dialogueIndex;
            tap(VK_ENTER);
            check(boss().dialogueIndex==index+1 || !gp.csManager.isDialogueActive(),"each fresh Enter advances a page");
        }
        for (int t=0; t<65 && gp.gameState==gp.cutsceneState; t++) {
            int previousY=gp.cameraY();
            tick(1);
            check(Math.abs(gp.cameraY()-previousY)<12,"camera handoff stays smooth through control return");
        }
        check(gp.gameState==gp.playState,"control returns after dialogue");
        check(gp.bossBattleOn && !boss().sleep && gp.player.worldX==x && gp.player.worldY==y && read(PLAN)==null,
            "control returns before first telegraph");
        check(gp.music23==(abbreviated?2:1),"one combat track request per introduction");
        if (!abbreviated) capture("02b-entrega-de-control");
    }

    public static void main(String[] args) throws Exception {
        gp.setupGame();
        Files.createDirectories(OUT);
        gp.saveLoad=new SaveLoad(gp,Files.createTempDirectory(Path.of("build"),"final-walkthrough-").resolve("save-v1.dat"));
        gp.gameState=gp.playState;
        gp.currentMap=Market.MAP;
        gp.currentArea=gp.nextArea=gp.indoor;
        Temple.place(gp.player,24,27);
        marketToArena();
        key(VK_W,true);
        tick(135);
        key(VK_W,false);
        check(!gp.bossBattleOn && gp.gameState==gp.playState && gp.player.worldY>=THRESHOLD_Y*48-8,"missing tools stop at threshold");
        xy(24,44);
        walk(RETURN_X*48,true);
        tap(VK_ENTER);
        until(() -> gp.currentMap==Market.MAP && gp.gameState==gp.playState,55,"return works without tools");
        buildPlayer();
        marketToArena();
        introduction(false);
        retry();
        phaseOne();
        phaseTwo();
        phaseThree();
        phaseFour();
        phaseFive();
        phaseSix();
        finish();
        cornersAndBody();
        mantleContact();
        summonAttacks();
        blockedSpawnAndRetry();
        gp.stopGame();
        System.out.println("FINAL_ARENA PASS: movement from market to credits, retry, moving intro, six revised phases, Aguijon and Baluarte in order, four links, two waves, four corners and one reward");
    }

    private static void retry() throws Exception {
        gp.player.life=1;
        until(() -> gp.gameState==gp.gameOverState,1000,"stationary player eventually hit");
        check(!gp.bossBattleOn && gp.obj[MAP][SEAL_SLOT]==null,"death clears encounter ownership");
        tap(VK_S);
        tap(VK_ENTER);
        check(gp.currentMap==Market.MAP && gp.player.life==gp.player.maxLife,"retry restores gear and health in the market");
        marketToArena();
        introduction(true);
        check(boss().phase()==1 && boss().life==650 && !boss().exposed(),"retry restores phase 1");
    }

    private static void buildPlayer() {
        gp.player.level=10;
        gp.player.strength=10;
        gp.player.dexterity=10;
        gp.player.maxLife=gp.player.life=30;
        gp.player.nextLevelExp=1024;
        gp.player.exp=512;
        gp.player.defense=gp.player.getDefense();
        gp.player.inventory.add(gp.eGenerator.requireObject("rock-tool"));
        gp.player.inventory.add(gp.eGenerator.requireObject("iron-tool"));
        Entity pot=gp.eGenerator.requireObject("strong-potion");
        pot.amount=5;
        gp.player.inventory.add(pot);
    }

    private static int flags() { return (int)read(FLAGS); }
    private static int[] counters(String name) { return (int[])read(field(name)); }
    private static boolean returning() { return (boolean)read(field("returning")); }
    private static Point cell(int x,int y) { return new Point(x*48+24,y*48+24); }
    private static int px() { return gp.player.worldX+24; }
    private static int py() { return gp.player.worldY+24; }

    private static void heal() {
        if (gp.player.life<18 && gp.player.searchItemInInventory("Strong Potion")!=999) equip("Strong Potion");
    }

    private static boolean route(int x,int y,int limit) {
        for (int n=0;n<limit;n++) {
            if (gp.gameState!=gp.playState) return false;
            int dx=x-px(),dy=y-py();
            if (Math.abs(dx)<=3 && Math.abs(dy)<=3) return true;
            int[] axes=Math.abs(dx)>Math.abs(dy)?new int[]{0,1}:new int[]{1,0};
            boolean moved=false;
            for (int axis:axes) {
                int amount=axis==0?dx:dy;
                if (Math.abs(amount)<=3) continue;
                int oldX=px(),oldY=py();
                int dir=axis==0?(amount<0?VK_A:VK_D):(amount<0?VK_W:VK_S);
                tap(dir);
                if (oldX!=px() || oldY!=py()) { moved=true; break; }
            }
            if (!moved && !gp.player.knockBack && !gp.player.attacking) return false;
        }
        return false;
    }

    private static boolean touch(Point target) {
        int[][] sides=boss().phase()==6
            ? (px()<target.x?new int[][]{{-40,0,VK_D},{40,0,VK_A}}:new int[][]{{40,0,VK_A},{-40,0,VK_D}})
            : new int[][]{{0,40,VK_W},{0,-40,VK_S},{-40,0,VK_D},{40,0,VK_A}};
        for (int[] side:sides) {
            int x=target.x+side[0],y=target.y+side[1];
            var standing=Temple.bounds(gp.player,x-24,y-24);
            if (!gp.finalArena.clear(standing) || (boss().protectedBody()
                    && standing.intersects(Temple.bounds(boss(),boss().worldX,boss().worldY)))) continue;
            if (route(x,y,235)) {
                face(side[2]);
                return true;
            }
        }
        return false;
    }

    private static java.awt.Shape danger() {
        Object track=plan("track");
        if (track==null) return (Rectangle)plan("danger");
        try {
            Method accessor=track.getClass().getDeclaredMethod("warning");
            accessor.setAccessible(true);
            return (java.awt.Shape)accessor.invoke(track);
        } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }

    private static void dodge() {
        java.awt.Shape warning=danger();
        if (warning==null) return;
        var body=Temple.bounds(gp.player,gp.player.worldX,gp.player.worldY);
        if (!warning.intersects(body)) return;
        for (int n=16;n<=196;n+=4) for (int[] side:new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
            var landing=new Rectangle(body.x+n*side[0],body.y+n*side[1],body.width,body.height);
            if (warning.intersects(landing) || !gp.finalArena.clear(landing)) continue;
            boolean clear=true;
            for (int k=4;k<=n;k+=4) {
                var path=new Rectangle(body.x+k*side[0],body.y+k*side[1],body.width,body.height);
                if (!gp.finalArena.clear(path) || (boss().protectedBody()
                        && path.intersects(Temple.bounds(boss(),boss().worldX,boss().worldY)))) { clear=false; break; }
            }
            if (clear) {
                route(px()+n*side[0],py()+n*side[1],65);
                return;
            }
        }
    }

    private static void phaseOne() throws Exception {
        walk(34*48,false);
        equip("Rocky");
        for (int i=0;i<2;i++) {
            Point stone=cell(i==0?19:30,33);
            for (int count=1;count<=3;count++) {
                int old=counters("stoneHits")[i];
                for (int tries=0;tries<9 && counters("stoneHits")[i]==old;tries++) {
                    heal();
                    if (touch(stone)) strike();
                    else tick(12);
                }
                check(counters("stoneHits")[i]==count,"one Rocky increment per whole swing");
            }
        }
        check(boss().exposed(),"both anchors required");
        equip("Iron");
        finishSegment(1);
    }

    private static void finishSegment(int phase) {
        for (int attempts=0;attempts<45 && boss().phase()==phase && boss().life>0 && !returning();attempts++) {
            heal();
            int offset=phase==6?24:0;
            if (touch(new Point(boss().worldX,boss().worldY+offset))) strike();
            else tick(8);
            if (phase==6 && !boss().exposed()) break;
        }
        if (phase==6) return;
        check(returning(),"threshold announces return before next phase");
        route(16*48+36,32*48+24,240);
        until(() -> boss().phase()==phase+1,180,"next phase materializes at center");
        check(!boss().exposed(),"new phase protected");
    }

    private static void phaseTwo() throws Exception {
        gp.player.water=0;
        for (Point block:new Point[]{new Point(BOSS_X-240,27*48+24),new Point(BOSS_X+240,27*48+24),
                new Point(BOSS_X,33*48+24)}) {
            int before=flags();
            for (int i=0;i<10 && flags()==before;i++) {
                heal();
                if (touch(block)) strike();
                else tick(14);
            }
            check(Integer.bitCount(flags())==Integer.bitCount(before)+1,"each Iron block is separate");
        }
        check(!boss().exposed() && flags()==7,"core waits for bubble");
        for (int i=0;i<4 && gp.player.water==0;i++) route(cell(31,35).x,cell(31,35).y,260);
        check(gp.player.water==2,"vent restores water from zero");
        for (int i=0;i<6 && !boss().exposed();i++) {
            heal();
            if (gp.player.water==0) route(cell(31,35).x,cell(31,35).y,300);
            Point core=new Point(BOSS_X,22*48+24);
            if (route(core.x,core.y+70,280)) {
                face(VK_W);
                key(VK_F,true);
                until(() -> boss().exposed() || gp.player.water==0,160,"fire toward the exposed core");
                key(VK_F,false);
            }
        }
        check(boss().exposed(),"bubble opens barrier after three iron blocks");
        finishSegment(2);
    }

    private static void phaseThree() throws Exception {
        until(() -> at("CHARGE","WARN"),400,"charge announces");
        dodge();
        until(() -> plan("move")==null,210,"failed charge recovers");
        check(flags()==0,"charge without an altar cannot open the defense");
        for (Point altar:new Point[]{cell(18,23),cell(31,23),cell(29,35)}) {
            int before=flags();
            for (int tries=0;tries<8 && flags()==before;tries++) {
                heal();
                while (plan("move")!=null && flags()==before) {
                    if (at("CHARGE","WARN")) dodge();
                    tick(1);
                }
                if (flags()!=before) break;
                route(altar.x,altar.y,310);
                until(() -> at("CHARGE","WARN"),400,"altar charge commits");
                dodge();
                until(() -> flags()!=before || plan("move")==null,220,"charge resolves");
            }
            check(Integer.bitCount(flags())==Integer.bitCount(before)+1,"one distinct altar cracks");
        }
        check(boss().exposed(),"third altar lodges trident");
        capture("03-altares");
        finishSegment(3);
    }

    private static void defeatSummon(Kind expected) throws Exception {
        until(() -> gp.monster[MAP][SUMMON_SLOT]!=null,450,"summon appears after visible seal");
        Entity enemy=gp.monster[MAP][SUMMON_SLOT];
        check(enemy instanceof FinalSummon minion && minion.kind()==expected,"summon order");
        int before=flags();
        check(gp.finalArena.clear(Temple.bounds(enemy,enemy.worldX,enemy.worldY)),"summon starts on clear floor");
        for (int attempt=0;attempt<700 && !enemy.dying;attempt++) {
            heal();
            if (gp.player.knockBack) { tick(4); continue; }
            var choice=routeToSummon(enemy);
            if (choice==null) { tick(6); continue; }
            if (choice.first()!=0) {
                key(choice.first(),true);
                tick(3);
                key(choice.first(),false);
            } else {
                face(choice.facing());
                strike();
            }
        }
        check(enemy.dying,"live melee defeats " + enemy.name);
        check(flags()==before,"death pose still owns its seal");
        until(() -> gp.monster[MAP][SUMMON_SLOT]==null,70,"summoned enemy removed");
        boss().summonDefeated(enemy);
        check(flags()==before+1,"removed summon cannot count twice");
    }

    private record Search(int x,int y,int first) {}
    private record Approach(int first,int facing) {}
    private static final int[][] STEPS={{0,-1,VK_W},{-1,0,VK_A},{1,0,VK_D},{0,1,VK_S}};

    private static Approach routeToSummon(Entity enemy) {
        var goal=Temple.bounds(enemy,enemy.worldX,enemy.worldY);
        var mantle=Temple.bounds(boss(),boss().worldX,boss().worldY);
        var player=gp.player;
        var queue=new ArrayDeque<Search>();
        var seen=new HashSet<Point>();
        queue.add(new Search(player.worldX,player.worldY,0));
        seen.add(new Point(player.worldX,player.worldY));
        while (!queue.isEmpty()) {
            Search node=queue.remove();
            for (int[] move:STEPS) {
                var strike=new Rectangle(node.x()+player.solidArea.x+move[0]*player.attackArea.width,
                    node.y()+player.solidArea.y+move[1]*player.attackArea.height,
                    player.attackArea.width,player.attackArea.height);
                if (strike.intersects(goal)) return new Approach(node.first(),move[2]);
                int x=node.x()+12*move[0],y=node.y()+12*move[1];
                Point next=new Point(x,y);
                if (seen.contains(next)) continue;
                boolean clear=true;
                for (int n=4;n<=12;n+=4) {
                    var step=Temple.bounds(player,node.x()+n*move[0],node.y()+n*move[1]);
                    if (!gp.finalArena.clear(step) || step.intersects(goal) || step.intersects(mantle)) {
                        clear=false;
                        break;
                    }
                }
                if (clear) {
                    seen.add(next);
                    queue.add(new Search(x,y,node.first()==0?move[2]:node.first()));
                }
            }
        }
        return null;
    }

    private static void phaseFour() throws Exception {
        for (int i=0;i<4;i++) {
            defeatSummon(i%2==0?Kind.AGUIJON:Kind.BALUARTE);
            check(flags()==i+1 && boss().exposed()==(i==3),"only the fourth confirmed defeat opens the defense");
        }
        check(boss().exposed(),"four summons open phase");
        finishSegment(4);
    }

    private static Point plantedHand() {
        try {
            Method method=boss().getClass().getDeclaredMethod("plantedHand",read(PLAN).getClass());
            method.setAccessible(true);
            return (Point)method.invoke(boss(),read(PLAN));
        } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }

    private static void phaseFive() throws Exception {
        for (int attack=0;attack<55 && !boss().exposed();attack++) {
            heal();
            until(() -> at("FIST_L","WARN") || at("FIST_R","WARN"),350,"next marked fist");
            String move=String.valueOf(plan("move"));
            int hand=move.equals("FIST_L")?0:1;
            dodge();
            until(() -> at(move,"HOLD"),115,"fist is planted");
            Point target=plantedHand();
            if (attack==0) capture("05-puno");
            for (int n=0;n<5 && at(move,"HOLD") && counters("fistHits")[hand]<10;n++) {
                int before=counters("fistHits")[hand];
                if (touch(target) && at(move,"HOLD")) strike();
                else break;
                check(counters("fistHits")[hand]<=before+1,"one count per whole melee swing");
            }
            if (!boss().exposed()) until(() -> plan("move")==null,220,"recover between fists");
        }
        check(counters("fistHits")[0]==10 && counters("fistHits")[1]==10 && boss().exposed(),"10 hits on both fists");
        finishSegment(5);
    }

    private static boolean inRefuge() {
        Rectangle body=Temple.bounds(gp.player,gp.player.worldX,gp.player.worldY);
        return body.x+body.width<=19*48 || body.x>=31*48;
    }

    private static void approachRefuge(int side,int limit) {
        int x=cell(side,33).x;
        for (int n=0;n<limit && Math.abs(px()-x)>4;n++) {
            Rectangle body=Temple.bounds(gp.player,gp.player.worldX,gp.player.worldY);
            Rectangle mantle=Temple.bounds(boss(),boss().worldX,boss().worldY);
            Rectangle path=body.union(new Rectangle(body.x+x-px(),body.y,body.width,body.height));
            if (boss().protectedBody() && path.intersects(mantle)) {
                int north=mantle.y-24,south=mantle.y+mantle.height+24;
                boolean northFits=north>=19*48+22,southFits=south<=38*48-22;
                boolean goNorth=northFits && (!southFits || Math.abs(py()-north)<Math.abs(py()-south));
                tap(goNorth?VK_W:VK_S);
            } else tap(px()<x?VK_D:VK_A);
            if (at("SLAM","IMPACT")) return;
        }
    }

    private static void waitWave(int side) throws Exception {
        approachRefuge(side,115);
        for (int n=0;n<4 && !at("SLAM","WARN");n++) {
            heal();
            until(() -> at("FIST_L","WARN") || at("FIST_R","WARN") || at("SLAM","WARN"),440,"furia commits a move");
            if (at("SLAM","WARN")) break;
            dodge();
            until(() -> plan("move")==null,210,"directed fist ends");
            approachRefuge(side,44);
        }
        check(at("SLAM","WARN"),"two fists precede wave");
        if (!inRefuge()) approachRefuge(px()<BOSS_X?17:32,82);
        check(inRefuge() && at("SLAM","WARN"),"player reaches a marked refuge before the wave");
        capture(side==17?"06-onda-izquierda":"06-onda-derecha");
        int hp=gp.player.life;
        until(() -> boss().exposed(),220,"wave exposes eye");
        check(gp.player.life==hp,"marked refuge avoids wave");
    }

    private static void phaseSix() throws Exception {
        waitWave(17);
        int initial=boss().life;
        for (int i=0;i<3 && boss().life==initial;i++) {
            touch(new Point(boss().worldX,boss().worldY+24));
            strike();
        }
        check(boss().life<initial && boss().life>0,"damage eye without finishing");
        int carried=boss().life;
        until(() -> !boss().exposed(),330,"eye closes after its opening");
        check(boss().life==carried,"life persists when eye closes");
        waitWave(32);
        check(boss().life==carried,"second wave preserves eye damage");
        for (int window=0;window<5 && boss().life>0;window++) {
            if (!boss().exposed()) waitWave(window%2==0?17:32);
            finishSegment(6);
        }
        check(boss().life==0,"final eye defeated through normal hits");
        until(() -> Progress.GodFishDefeated,80,"victory finalized");
        finishDialogue();
    }

    private static void finish() throws Exception {
        check(gp.obj[MAP][SEAL_SLOT]==null && gp.monster[MAP][BOSS_SLOT]==null && gp.lastMusic==22,"victory clears combat and restores ambience");
        check(gp.player.exp==1012,"only one 500 XP boss reward");
        long keys=java.util.Arrays.stream(gp.obj[MAP]).filter(OBJ_Key.class::isInstance).count();
        check(keys==1,"single key drop");
        while (gp.player.inventory.size()<gp.player.maxinventorySize)
            gp.player.inventory.add(gp.eGenerator.requireObject("normal-bubble"));
        xy(KEY_X,KEY_Y);
        check(gp.obj[MAP][KEY_SLOT]!=null && gp.player.searchItemInInventory("Key")==999,"full bag preserves unique key");
        gp.player.inventory.remove(gp.player.inventory.size()-1);
        tap(VK_W);
        check(gp.player.searchItemInInventory("Key")!=999,"key picked up by movement");
        walk(26*48,true);
        walk(DOOR_Y*48+40,false);
        face(VK_W);
        check(gp.obj[MAP][DOOR_SLOT]!=null && !gp.finalArena.clear(new Rectangle(25*48+12,DOOR_Y*48+12,20,20))
            && !gp.finalArena.clear(new Rectangle(27*48+12,DOOR_Y*48+12,20,20)),"north passage cannot bypass the key gate");
        capture("08-pasillo-norte");
        equip("Key");
        check(gp.obj[MAP][DOOR_SLOT]==null && gp.player.searchItemInInventory("Key")==999,"key opens north door once");
        walk(EXIT_Y*48,false);
        check(gp.csManager.sceneNum==gp.csManager.ending,"exit initiates original ending");
        until(gp.csManager::isBeachWalk,100,"portal fades into the beach");
        key(VK_D,true);
        until(gp.csManager::isDialogueActive,600,"walking into the photo starts the dialogue");
        key(VK_D,false);
        for (int i=0; i<8; i++) tap(VK_ENTER);
        until(() -> gp.csManager.scenePhase==CutsceneManager.ENDING_CREDITS,2000,"original credits reached");
        check(gp.lastMusic==24,"credits music");
        capture("14-creditos");
    }
    private static void setBoss(String field,int value) {
        try { field(field).setInt(boss(),value); }
        catch (IllegalAccessException error) { throw new AssertionError(error); }
    }

    private static void arenaAt(int col,int row) {
        gp.resetGame(true);
        gp.currentMap=MAP;
        gp.currentArea=gp.nextArea=gp.dungeons;
        gp.gameState=gp.playState;
        Temple.place(gp.player,col,row);
        gp.player.maxLife=gp.player.life=30;
        gp.finalArena.beginCombatCamera();
    }


    private static void cornersAndBody() throws Exception {
        for (int[] corner:new int[][]{{16,19},{33,19},{16,37},{33,37}}) {
            arenaAt(corner[0],corner[1]);
            gp.bossBattleOn=true;
            boss().beginFight();
            setBoss("rest",0);
            int hp=gp.player.life;
            until(() -> (boolean)read(field("swimming")),60,"boss swims toward corner");
            if (corner[0]==16 && corner[1]==19) {
                until(() -> (int)read(field("swimDistance"))>=10,15,"swim frame advances with distance");
                until(() -> Math.hypot(boss().worldX-BOSS_X,boss().worldY-BOSS_Y)>120
                    && (boolean)read(field("swimming")),150,"swims into the corner framing");
                capture("07-nado");
            }
            until(() -> gp.player.life<hp,1500,"stationary corner is threatened");
            check(gp.finalArena.clear(Temple.bounds(gp.player,gp.player.worldX,gp.player.worldY)),"corner impact stays on floor");
        }
        arenaAt(24,28);
        gp.bossBattleOn=true;
        boss().beginFight();
        int x=px(),y=py(),hp=gp.player.life;
        setBoss("returnTicks",1);
        field("returning").setBoolean(boss(),true);
        tick(90);
        check(boss().phase()==1 && px()==x && py()==y && gp.player.life==hp && !boss().protectedBody(),
            "materialization waits harmlessly if the center is occupied");
        walk(20*48,true);
        until(() -> boss().phase()==2,6,"return resumes after leaving center");
        check(boss().worldX==BOSS_X && boss().worldY==BOSS_Y,"returns to marked center");
        for (int[] side:new int[][]{{16,28,1,0},{33,28,-1,0},{24,19,0,1},{24,37,0,-1}}) {
            arenaAt(side[0],side[1]);
            boss().worldX=px()+side[2]*65;
            boss().worldY=py()+side[3]*59;
            gp.bossBattleOn=true;
            boss().beginFight();
            tick(24);
            check(gp.player.life==30 && gp.finalArena.clear(Temple.bounds(gp.player,gp.player.worldX,gp.player.worldY)),
                "protected contact next to a wall is safe");
        }
        arenaAt(22,28);
        gp.bossBattleOn=true;
        boss().beginFight();
        field("exposed").setBoolean(boss(),true);
        walk(27*48,true);
        check(px()>boss().worldX+80 && gp.player.life==30,"the player crosses the exposed body without a push");
        arenaAt(24,28);
        gp.bossBattleOn=true;
        boss().beginFight();
        setBoss("phase",6);
        field("exposed").setBoolean(boss(),true);
        setBoss("eyeTicks",1);
        tick(1);
        check(!boss().exposed() && !boss().protectedBody(),"reforming armor cannot trap an overlapping player");
        walk(21*48,true);
        until(() -> boss().protectedBody(),4,"body returns as soon as the player swims clear");
        for (int[] edge:new int[][]{{16,27,VK_A},{33,27,VK_D},{23,19,VK_W},{23,37,VK_S}}) {
            arenaAt(edge[0],edge[1]);
            face(edge[2]);
            gp.player.shotAvailableCounter=50;
            gp.player.projectile.speed=110;
            tap(VK_F);
            check(!gp.player.projectile.alive && gp.player.water==5,"fast player bubble hits the solid rim");
            gp.player.projectile.speed=6;
        }
    }
    private static void mantleContact() throws Exception {
        arenaAt(22,28);
        gp.bossBattleOn=true;
        boss().beginFight();
        // The test moves the stationary boss away from its spawn before approaching with D.
        boss().worldX+=48;
        walk(boss().worldX-24-44,true);
        check(gp.player.life==30 && plan("move")==null,"the outer mantle is harmless between attacks");
        int before=gp.player.worldX;
        while (gp.player.life==30 && gp.player.worldX<boss().worldX-24) {
            before=gp.player.worldX;
            tap(VK_D);
            check(plan("move")==null,"central contact is independent of attack plans");
            if (gp.player.life==30) check(gp.player.worldX>before,"can swim into the mantle center");
        }
        check(gp.player.life==29 && gp.player.invincible && gp.player.worldX<before+4,
            "central contact costs one HP and pushes outward");
        gp.player.worldX=boss().worldX-24;
        tick(12);
        check(gp.player.life==29,"normal damage invulnerability suppresses repeated contact");

        arenaAt(22,28);
        gp.bossBattleOn=true;
        boss().beginFight();
        field("exposed").setBoolean(boss(),true);
        walk(boss().worldX-24,true);
        int x=gp.player.worldX,y=gp.player.worldY;
        tick(8);
        check(gp.player.life==30 && gp.player.worldX==x && gp.player.worldY==y,
            "overlap while exposed applies neither damage nor push");

        arenaAt(16,28);
        gp.bossBattleOn=true;
        boss().beginFight();
        gp.player.worldX=16*48-gp.player.solidArea.x;
        boss().worldX=px()+25;
        boss().worldY=py()+26;
        x=gp.player.worldX;
        y=gp.player.worldY;
        check(!gp.finalArena.clear(Temple.bounds(gp.player,x-1,y-1)),"wall blocks direct outward push");
        tick(1);
        check(gp.player.life==29 && gp.player.worldX>x && gp.player.worldY<y
            && gp.finalArena.clear(Temple.bounds(gp.player,gp.player.worldX,gp.player.worldY)),
            "contact beside a wall uses a clear lateral exit");
        System.out.println("GOD_FISH_CONTACT PASS: central overlap between attacks, invulnerability, exposed overlap and wall fallback");
    }

    private static Object member(Object object,String name) {
        try {
            var field=object.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(object);
        } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }

    @SuppressWarnings("unchecked")
    private static java.util.List<Rectangle> zones(FinalSummon minion) {
        return (java.util.List<Rectangle>)member(member(minion,"strike"),"zones");
    }

    private static boolean state(FinalSummon minion,String name) {
        return name.equals(String.valueOf(member(minion,"state")));
    }

    private static FinalSummon attackFixture(Kind kind) {
        arenaAt(20,27);
        gp.player.strength=8;
        gp.player.getAttack();
        gp.bossBattleOn=true;
        boss().beginFight();
        setBoss("phase",4);
        setBoss("rest",10000);
        setBoss("summon",1);
        FinalSummon minion=new FinalSummon(gp,kind);
        Temple.place(minion,19,27);
        gp.monster[MAP][SUMMON_SLOT]=minion;
        check(minion.maxLife==(kind==Kind.AGUIJON?45:55) && minion.attack==(kind==Kind.AGUIJON?7:9),
            "manifest balance");
        until(() -> state(minion,"WARN"),100,"summon warns before attack");
        return minion;
    }

    private static void summonAttacks() throws Exception {
        for (int target=-1;target<3;target++) {
            var spike=attackFixture(Kind.AGUIJON);
            var warned=zones(spike).stream().map(Rectangle::new).toList();
            check(warned.size()==3 && warned.stream().distinct().count()==3,"three separate spike warnings");
            int x=spike.worldX,y=spike.worldY,hp=gp.player.life;
            var stand=warned.get(Math.max(0,target));
            if (target<0) walk(gp.player.worldY+48,false);
            walk((int)stand.getCenterX()-24,true);
            check(gp.player.life==hp,"warning never hits");
            if (target==1) capture("esbirro-aguijon-aviso");
            until(() -> state(spike,"IMPACT"),40,"spikes begin");
            while (state(spike,"IMPACT") && gp.player.life==hp) {
                check(spike.worldX==x && spike.worldY==y && warned.equals(zones(spike)),"committed spike centers stay fixed");
                tick(1);
            }
            if (target<0) check(gp.player.life==hp,"outside spike zones stays safe");
            else {
                check(gp.player.life==hp-(7-gp.player.defense)
                    && (int)member(spike,"tick")/11==target,"only the scheduled spike at the warned center hits");
                if (target==1) capture("esbirro-aguijon-impacto");
            }
            until(() -> state(spike,"RECOVER"),40,"Aguijon recovers");
            check(spike.defense==0 && spike.worldX==x && spike.worldY==y,"trident recovery exposes a stationary Aguijon");
        }

        var shield=attackFixture(Kind.BALUARTE);
        var warned=zones(shield).stream().map(Rectangle::new).toList();
        check(warned.size()==1 && gp.finalArena.clear(warned.get(0)),"shield warns its committed arc");
        int hp=shield.life;
        face(VK_A);
        gp.player.shotAvailableCounter=50;
        tap(VK_F);
        until(() -> !gp.player.projectile.alive,20,"front bubble collides with shield");
        check(shield.life==hp,"raised shield blocks front bubble");
        strike();
        check(shield.life==hp,"raised shield blocks front melee");
        final var firstShield=shield;
        until(() -> state(firstShield,"RECOVER"),55,"Baluarte plants shield");
        check(warned.equals(zones(shield)) && shield.defense==2,"recovery lowers the shield");
        capture("esbirro-baluarte-recuperacion");
        check(route(shield.getCenterX()+40,shield.getCenterY(),70),"reach planted shield from the front");
        face(VK_A);
        strike();
        check(shield.life<hp,"front melee damages Baluarte during recovery");

        shield=attackFixture(Kind.BALUARTE);
        int playerHp=gp.player.life;
        var arc=zones(shield).get(0);
        check(arc.intersects(Temple.bounds(gp.player,gp.player.worldX,gp.player.worldY)),"player stands in warned arc");
        final var striking=shield;
        until(() -> state(striking,"IMPACT"),40,"shield strike resolves");
        check(gp.player.life==playerHp-(9-gp.player.defense) && zones(shield).get(0).equals(arc),
            "shield damage matches its warning");
        capture("esbirro-baluarte-impacto");

        final var evaded=attackFixture(Kind.BALUARTE);
        playerHp=gp.player.life;
        walk(gp.player.worldY+48,false);
        until(() -> state(evaded,"RECOVER"),50,"shield strike finishes after sidestep");
        check(gp.player.life==playerHp,"outside the shield warning stays safe");
    }

    private static void blockedSpawnAndRetry() throws Exception {
        arenaAt(24,33);
        gp.bossBattleOn=true;
        boss().beginFight();
        setBoss("phase",4);
        setBoss("rest",0);
        int[][] blocked={{20,34},{20,33},{20,35},{18,34},{19,32},{19,36},{21,34},{17,34}};
        int[] old=new int[blocked.length];
        for (int i=0;i<blocked.length;i++) {
            int x=blocked[i][0],y=blocked[i][1];
            old[i]=gp.tileM.mapTileNum[MAP][x][y];
            gp.tileM.mapTileNum[MAP][x][y]=194;
        }
        try {
            until(() -> at("SUMMON","RECOVER"),100,"blocked cast recovers");
            check(gp.monster[MAP][SUMMON_SLOT]==null && flags()==0 && (int)read(field("summon"))==0,
                "blocked appearance consumes no summon or link");
        } finally {
            for (int i=0;i<blocked.length;i++)
                gp.tileM.mapTileNum[MAP][blocked[i][0]][blocked[i][1]]=old[i];
        }
        until(() -> gp.monster[MAP][SUMMON_SLOT] instanceof FinalSummon,190,"the same seal retries on free floor");
        check(((FinalSummon)gp.monster[MAP][SUMMON_SLOT]).kind()==Kind.AGUIJON && flags()==0,"retry preserves first seal");
        gp.player.life=0;
        tick(1);
        check(gp.gameState==gp.gameOverState && gp.monster[MAP][SUMMON_SLOT]==null,"death clears active summon");
        tap(VK_S);
        tap(VK_ENTER);
        check(gp.currentMap==Market.MAP && gp.monster[MAP][SUMMON_SLOT]==null && boss().phase()==1,
            "normal retry clears summons and restarts the boss");
    }
}
