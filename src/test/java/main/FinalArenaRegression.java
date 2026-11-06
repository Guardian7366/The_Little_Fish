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
import java.util.function.BooleanSupplier;
import monster.MON_GodFish;
import monster.MON_Picaron;
import monster.MON_Tella;
import object.OBJ_Key;

import static java.awt.event.KeyEvent.*;
import static main.FinalArena.*;

public final class FinalArenaRegression {
    private static final class Quiet extends FifthZoneRegression.Quiet {
        int music23, lastMusic;
        @Override public void playMusic(int id) { lastMusic=id; if (id==23) music23++; }
    }

    private static final Path OUT=Path.of("build/final-preview");
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
        check(gp.cameraY()!=camera && gp.player.worldX==x && gp.player.worldY==y,"camera pans without moving fish");
        if (!abbreviated) { tick(29); capture("01-presencia"); }
        for (int i=0; i<85; i++) {
            key(VK_ENTER,true);
            gp.update();
        }
        check(gp.csManager.isDialogueActive() && boss().dialogueIndex==(abbreviated?3:0),"held Enter cannot skip pages");
        key(VK_ENTER,false);
        if (!abbreviated) { tick(40); capture("02-reconocimiento"); }
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
        bordersAndWave();
        directions();
        gp.stopGame();
        System.out.println("FINAL_ARENA PASS: movement from market to credits, retry, four-page intro, six objectives and one reward");
    }

    private static void retry() throws Exception {
        walk(34*48,false);
        gp.player.life=1;
        until(() -> at("SWEEP","WARN"),110,"first sweep is warned");
        check(gp.player.life==1,"warning does not hurt");
        until(() -> gp.gameState==gp.gameOverState,90,"unavoided sweep can kill");
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
    }

    private static void phaseOne() throws Exception {
        int life=boss().life;
        check(!boss().hit(new Rectangle(BOSS_X-30,BOSS_Y-30,60,60),BOSS_X,BOSS_Y,null,10000)
            || boss().life==life,"closed barrier prevents a huge direct hit");
        walk(34*48,false);
        xy(19,34);
        face(VK_W);
        strike();
        check((int)read(FLAGS)==0,"ordinary weapon cannot break anchor");
        equip("Rocky");
        capture("03-anclajes");
        strike();
        check((int)read(FLAGS)==1 && !boss().exposed(),"one Rocky anchor leaves the other linked");
        xy(30,34);
        face(VK_W);
        strike();
        check(boss().exposed(),"second Rocky anchor opens phase 1");
        equip("Iron");
        finishSegment(1);
    }

    private static void finishSegment(int phase) {
        for (int attempts=0; attempts<32 && boss().phase()==phase && boss().life>0; attempts++) {
            until(() -> !gp.player.knockBack,18,"recover from the preceding impact");
            walk(BOSS_X-24,true);
            walk(boss().worldY+(phase==6?49:67),false);
            face(VK_W);
            strike();
        }
        check(phase==6?boss().life==0:boss().phase()==phase+1,"exactly one life segment consumed");
    }

    private static void phaseTwo() throws Exception {
        xy(27,33);
        face(VK_W);
        equip("Rocky");
        strike();
        check((int)read(FLAGS)==0 && !boss().exposed(),"Rocky cannot open grille");
        gp.player.water=0;
        capture("04-rejilla-y-respiradero");
        equip("Iron");
        strike();
        check((int)read(FLAGS)==4 && !boss().exposed(),"Iron opens grille without activating core");
        xy(30,35);
        tick(2);
        check(gp.player.water==2,"empty water can be restored in the arena");
        xy(27,33);
        face(VK_W);
        tap(VK_F);
        until(() -> boss().exposed(),60,"player bubble hits the open core");
        finishSegment(2);
    }

    private static void phaseThree() throws Exception {
        walk(20*48,true);
        until(() -> at("CHARGE","WARN"),180,"leftward charge is telegraphed");
        check("LEFT".equals(String.valueOf(plan("aim"))),"first charge aims toward the player");
        walk(34*48,false);
        until(() -> at("CHARGE","IMPACT"),140,"charge executes on its fixed heading");
        while (at("CHARGE","IMPACT")) {
            check(gp.finalArena.clear(Temple.bounds(boss(),boss().worldX,boss().worldY)),"whole charging body stays on floor");
            tick(1);
        }
        check(!boss().exposed(),"missed charge keeps barrier");
        xy(20,36);
        walk(24*48,true);
        until(() -> at("CHARGE","WARN") && "DOWN".equals(String.valueOf(plan("aim"))),480,"next attempt can target altar");
        capture("05-aviso-embestida");
        walk(27*48,true);
        until(() -> boss().exposed(),180,"trident must hit altar even after player dodges");
        capture("06-tridente-clavado");
        walk(36*48,false);
        finishSegment(3);
    }

    private static void defeatSummon(Class<?> expected,String image) throws Exception {
        until(() -> gp.monster[MAP][SUMMON_SLOT]!=null,450,"summon appears after visible seal");
        Entity enemy=gp.monster[MAP][SUMMON_SLOT];
        check(expected.isInstance(enemy),"summon order");
        capture(image);
        for (int attempt=0; attempt<70 && !enemy.dying; attempt++) {
            int dx=enemy.worldX-gp.player.worldX, dy=enemy.worldY-gp.player.worldY;
            int toward=Math.abs(dx)>Math.abs(dy)?(dx<0?VK_A:VK_D):(dy<0?VK_W:VK_S);
            key(toward,true);
            tick(Math.max(Math.abs(dx),Math.abs(dy))>57?8:2);
            key(toward,false);
            if (Math.abs(enemy.worldX-gp.player.worldX)<75 && Math.abs(enemy.worldY-gp.player.worldY)<75)
                strike();
        }
        check(enemy.dying,"live melee defeats summoned " + enemy.name);
        until(() -> gp.monster[MAP][SUMMON_SLOT]==null,70,"summoned enemy removed");
    }

    private static void phaseFour() throws Exception {
        xy(20,36);
        defeatSummon(MON_Picaron.class,"07-picaron");
        check(!boss().exposed(),"one minion is insufficient");
        xy(29,36);
        defeatSummon(MON_Tella.class,"08-fishtella");
        until(() -> boss().exposed(),5,"second defeat opens phase");
        xy(29,33);
        finishSegment(4);
    }

    private static void phaseFive() throws Exception {
        walk(36*48,false);
        for (String attack:new String[]{"FIST_L","FIST_R"}) {
            until(() -> at(attack,"HOLD"),620,"wait for planted " + attack);
            Point hand=(Point)plan("target");
            check("DOWN".equals(String.valueOf(plan("aim"))),"down-facing planted fist accessible from south");
            walk(hand.x-24,true);
            walk(hand.y+7,false);
            face(VK_W);
            capture(attack.endsWith("L")?"09-puno-izquierdo":"10-puno-derecho");
            strike();
            if (attack.endsWith("L")) {
                check((int)read(FLAGS)==1 && !boss().exposed(),"left fist marks once");
                walk(35*48,false);
            }
        }
        check(boss().exposed(),"two distinct fists expose phase 5");
        finishSegment(5);
    }

    private static void phaseSix() throws Exception {
        walk(33*48,false);
        until(() -> at("SLAM","WARN"),670,"two directed attacks precede slam");
        capture("11-refugios");
        walk(17*48,true);
        int hp=gp.player.life;
        until(() -> boss().exposed(),220,"wave finishes and exposes the eye");
        check(gp.player.life==hp,"left marked refuge avoids entire wave");
        capture("12-ojo");
        walk(33*48,false);
        finishSegment(6);
        tick(10);
        capture("13-derrota");
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
        equip("Key");
        check(gp.obj[MAP][DOOR_SLOT]==null && gp.player.searchItemInInventory("Key")==999,"key opens north door once");
        walk(EXIT_Y*48,false);
        check(gp.csManager.sceneNum==gp.csManager.ending,"exit initiates original ending");
        tick(2);
        for (int i=0; i<8; i++) tap(VK_ENTER);
        until(() -> gp.csManager.scenePhase==8,2000,"original credits reached");
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
    }

    private static void bordersAndWave() throws Exception {
        int[][] edges={{16,27,VK_A},{33,27,VK_D},{23,19,VK_W},{23,37,VK_S}};
        for (int[] edge:edges) {
            arenaAt(edge[0],edge[1]);
            face(edge[2]);
            gp.player.shotAvailableCounter=50;
            gp.player.projectile.speed=110;
            tap(VK_F);
            check(!gp.player.projectile.alive && gp.player.water==5,"even a fast shot hits the solid rim");
            gp.player.projectile.speed=6;
        }
        for (int tool=0;tool<2;tool++) {
            arenaAt(24,44);
            gp.player.inventory.add(gp.eGenerator.requireObject(tool==0?"rock-tool":"iron-tool"));
            key(VK_W,true);
            tick(135);
            key(VK_W,false);
            check(gp.gameState==gp.playState && gp.obj[MAP][SEAL_SLOT]==null,"each missing tool leaves the vestibule open");
            xy(24,44);
            walk(23*48,true);
            tap(VK_ENTER);
            until(() -> gp.currentMap==Market.MAP,55,"retreat to market with one missing tool");
        }
        for (int col:new int[]{24,32}) {
            arenaAt(col,35);
            gp.bossBattleOn=true;
            boss().beginFight();
            setBoss("phase",6);
            boss().life=105;
            setBoss("cycle",2);
            setBoss("rest",0);
            until(() -> at("SLAM","WARN"),4,"wave preview");
            tick(70);
            check(gp.player.life==30,"telegraph never damages");
            until(() -> at("SLAM","IMPACT"),40,"wave emitted");
            if (col==32) { tick(21); capture("15-onda-refugio-derecho"); }
            until(() -> boss().exposed(),190,"eye after wave");
            check(gp.player.life==(col==32?30:18),"central wave hits exactly once and right refuge stays safe");
        }
    }
    private static void directions() throws Exception {
        int[][] baits={{24,35},{20,31},{28,31},{24,27}};
        String[] aims={"DOWN","LEFT","RIGHT","UP"};
        for (int phase:new int[]{1,3,5}) {
            for (int i=0; i<aims.length; i++) {
                arenaAt(baits[i][0],baits[i][1]);
                gp.bossBattleOn=true;
                boss().beginFight();
                setBoss("phase",phase);
                setBoss("rest",0);
                String move=phase==1?"SWEEP":phase==3?"CHARGE":"FIST_L";
                until(() -> at(move,"WARN"),3,"directional telegraph");
                check(aims[i].equals(String.valueOf(plan("aim"))),"correct committed " + move + " heading");
                Rectangle danger=(Rectangle)plan("danger");
                check(!danger.isEmpty(),"telegraph has a bounded hit area");
                int life=gp.player.life;
                boolean vertical=i==0 || i==3;
                int position=vertical?gp.player.worldX:gp.player.worldY;
                double dangerCenter=vertical?danger.getCenterX():danger.getCenterY();
                int escape=position+24<dangerCenter ? -144 : 144;
                if (phase!=5 && i>=2) escape=-144;
                walk(position+escape,vertical);
                until(() -> !at(move,"WARN"),100,"attack follows its warning");
                for (int t=0; t<45 && at(move,"IMPACT"); t++) {
                    check(gp.finalArena.clear(Temple.bounds(boss(),boss().worldX,boss().worldY)),
                        "body respects basalt in every direction");
                    tick(1);
                }
                check(gp.player.life==life,"perpendicular dodge avoids " + move + " " + aims[i]);
            }
        }
        arenaAt(24,35);
        gp.bossBattleOn=true;
        boss().beginFight();
        setBoss("phase",2);
        setBoss("rest",0);
        until(() -> at("VOLLEY","WARN"),4,"volley announced before any bubble");
        int hp=gp.player.life;
        tick(35);
        check(gp.player.life==hp,"volley warning is harmless");
        until(() -> at("VOLLEY","IMPACT"),25,"three shots begin after warning");
        tick(12);
        capture("04b-salva");
        until(() -> gp.player.life<hp,65,"standing in warned line is hit by volley");
    }
}
