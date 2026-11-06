package main;

import data.Progress;
import data.SaveLoad;
import entity.Entity;
import entity.NPC_TV;
import entity.NPC_BigRock;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import object.OBJ_Chest;
import object.OBJ_Exit;
import object.OBJ_Fishbowl;
import tile_interactive.IT_MetalPlate;

/** Integration checks with explicit fixtures, not a simulated human playthrough. */
public final class StoryRegression {
    static final class QuietPanel extends GamePanel {
        int lastMusic = -1;
        @Override public void playSE(int index) { }
        @Override public void playMusic(int index) { lastMusic = index; }
        @Override public void stopMusic() { lastMusic = -1; }
    }
    interface Test { void run() throws Exception; }
    static QuietPanel gp;
    static int passed, failed;
    static Path saveDirectory;

    static void check(String name, Test test) {
        gp.resetGame(true);
        gp.gameState = gp.playState;
        gp.saveLoad = new SaveLoad(gp, saveDirectory.resolve("checkpoint-" + (passed + failed) + ".dat"));
        gp.saveLoad.captureInitialWorld();
        try { test.run(); passed++; System.out.println("PASS " + name); }
        catch (Throwable failure) { failed++; System.out.println("FAIL " + name + ": " + failure); failure.printStackTrace(System.out); }
    }
    static void truth(boolean value, String label) { if (!value) throw new AssertionError(label); }
    static void equal(int expected, int actual, String label) {
        if (expected != actual) throw new AssertionError(label + " expected=" + expected + " actual=" + actual);
    }
    static void place(int map, int col, int row) {
        gp.currentMap = map;
        gp.player.worldX = col * gp.tileSize;
        gp.player.worldY = row * gp.tileSize;
        gp.eHandler.canTouchEvent = true;
        gp.eHandler.previousEventX = -9999;
        gp.eHandler.previousEventY = -9999;
        gp.keyH.releaseAll();
    }
    static void render(int times) { for (int i = 0; i < times; i++) gp.drawToTempScreen(); }
    static void ticks(int times) { for (int i = 0; i < times; i++) gp.update(); }
    static int find(Entity[] objects, String name) {
        for (int i = 0; i < objects.length; i++) if (objects[i] != null && name.equals(objects[i].name)) return i;
        return -1;
    }
    static void finishDialogue() {
        for (int i = 0; i < 100 && gp.gameState == gp.dialogueState; i++) {
            gp.keyH.enterPressed = true;
            gp.update();
        }
        truth(gp.gameState != gp.dialogueState, "dialogue terminates");
        gp.keyH.releaseAll();
    }
    static int count(Entity[][] values) {
        int result = 0;
        for (Entity[] map : values) for (Entity value : map) if (value != null) result++;
        return result;
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("build", "story-fixtures"));
        saveDirectory = Files.createTempDirectory(Path.of("build", "story-fixtures"), "run-");
        gp = new QuietPanel();
        gp.setupGame();
        check("expanded world populations and eleven playable maps", () -> {
            equal(11, gp.tileM.loadedMapCount(), "maps");
            equal(87, count(gp.monster), "enemies including God Fish");
            equal(20, count(gp.npc), "NPC placements including TV and rocks");
            equal(13, count(gp.obj), "objects");
            equal(34, count(gp.iTile), "interactive tiles");
        });
        // Source map, trigger tile, destination map/tile/area. The main route and every side-room return.
        int[][] routes = {
            {0,38,25,1,9,41,50}, {1,39,7,2,22,7,50}, {2,20,44,7,25,7,50}, {7,25,45,3,25,23,52},
            {3,9,44,8,25,43,52}, {8,23,45,3,17,44,52}, {8,25,3,4,24,42,52}, {4,24,44,8,25,6,52},
            {0,28,18,5,10,40,51}, {5,9,40,0,28,18,50},
            {1,9,34,5,39,40,51}, {5,40,40,1,9,34,50},
            {1,39,23,5,39,29,51}, {5,40,29,1,39,23,50},
            {2,39,13,5,10,29,51}, {5,9,29,2,39,13,50},
            {2,9,17,5,10,19,51}, {5,9,19,2,9,17,50},
            {2,19,16,5,39,19,51}, {5,40,19,2,19,16,50},
            {3,9,5,5,39,9,51}, {5,40,9,3,9,5,52},
            {3,14,40,5,10,9,51}, {5,9,9,3,14,40,52}
        };
        for (int[] route : routes) check("portal " + route[0] + ":" + route[1] + "," + route[2], () -> {
            place(route[0], route[1], route[2]);
            gp.eHandler.checkEvent();
            equal(gp.transitionState, gp.gameState, "portal starts transition");
            render(8);
            equal(route[0], gp.currentMap, "rendering cannot teleport");
            equal(0, gp.ui.counter, "rendering cannot advance fade");
            ticks(49);
            equal(route[0], gp.currentMap, "transition waits fifty ticks");
            ticks(1);
            equal(route[3], gp.currentMap, "destination map");
            equal(route[4] * gp.tileSize, gp.player.worldX, "destination X");
            equal(route[5] * gp.tileSize, gp.player.worldY, "destination Y");
            equal(route[6], gp.currentArea, "destination area");
            gp.eHandler.checkEvent();
            equal(gp.playState, gp.gameState, "arrival cannot immediately bounce back");
        });
        int[][] checkpoints = {{0,13,31},{1,14,41},{2,17,6},{2,28,6},{3,25,20},{4,24,30},{4,25,30},{7,25,8}};
        for (int[] checkpoint : checkpoints) check("checkpoint " + checkpoint[0] + ":" + checkpoint[1] + "," + checkpoint[2], () -> {
            place(checkpoint[0], checkpoint[1], checkpoint[2]);
            gp.currentArea = checkpoint[0] >= 3 && checkpoint[0] != 7 ? gp.dungeons : gp.outside;
            gp.player.life = 1; gp.player.water = 0;
            gp.keyH.enterPressed = true;
            gp.eHandler.checkEvent();
            equal(gp.player.maxLife, gp.player.life, "checkpoint heals");
            equal(gp.player.maxWater, gp.player.water, "checkpoint restores water");
            truth(Files.exists(gp.saveLoad.getSavePath()), "checkpoint persisted: " + gp.saveLoad.getLastError());
            gp.player.worldX = 0; gp.player.worldY = 0; gp.currentMap = 0;
            truth(gp.saveLoad.tryLoad(), gp.saveLoad.getLastError());
            equal(checkpoint[0], gp.currentMap, "checkpoint map restored");
            equal(checkpoint[1] * gp.tileSize, gp.player.worldX, "checkpoint X restored");
            equal(checkpoint[2] * gp.tileSize, gp.player.worldY, "checkpoint Y restored");
        });
        check("damage event fires once until leaving tile", () -> {
            place(0,5,6);
            gp.eHandler.checkEvent();
            equal(5, gp.player.life, "pit damage");
            gp.eHandler.checkEvent();
            equal(5, gp.player.life, "no repeated event at same position");
            finishDialogue();
        });
        check("all placed NPC dialogue and trader entry are usable", () -> {
            int spoken = 0;
            for (int map = 0; map < gp.npc.length; map++) {
                for (Entity npc : gp.npc[map]) {
                    if (npc == null) continue;
                    gp.currentMap = map;
                    gp.gameState = gp.playState;
                    gp.flow.resetDialogue();
                    npc.speak();
                    if (npc instanceof NPC_TV) equal(gp.tradeState, gp.gameState, "TV opens trade");
                    else {
                        equal(gp.dialogueState, gp.gameState, "NPC starts dialogue");
                        int index = npc.dialogueIndex;
                        render(5);
                        equal(index, npc.dialogueIndex, "drawing cannot skip a line");
                        finishDialogue();
                    }
                    spoken++;
                }
            }
            equal(20, spoken, "every placed NPC checked");
        });
        check("eight reward chests retain their original loot and open once", () -> {
            int chests = 0;
            for (int map = 0; map < gp.obj.length; map++) for (Entity item : gp.obj[map]) {
                if (!(item instanceof OBJ_Chest chest)) continue;
                gp.currentMap = map;
                int before = gp.player.inventory.size();
                truth(chest.loot != null, "reward exists");
                chest.interact();
                truth(chest.opened, "chest opens");
                int after = gp.player.inventory.size();
                truth(after >= before && after <= before + 1, "one reward or stack");
                finishDialogue();
                chest.interact(); finishDialogue();
                equal(after, gp.player.inventory.size(), "no duplicate reward");
                chests++;
            }
            equal(8, chests, "all reward chests");
        });
        check("four rocks on their paired plates open the temple gate", () -> {
            gp.currentMap = 4;
            var previous=gp.monster[4].clone();
            Arrays.fill(gp.monster[4],null);
            gp.gameState=gp.playState;
            gp.eHandler=new EventHandler(gp);
            Temple.fixtures(gp,gp.npc,gp.iTile);
            gp.obj[4][0]=Temple.gate(gp);
            for (int i=0;i<Temple.LANES.length;i++) {
                TempleRegression.pushLane(gp,Temple.LANES[i]);
                if (i<3) truth(find(gp.obj[4], "Iron Door")>=0,"door waits for all four");
            }
            gp.monster[4]=previous;
            equal(-1, find(gp.obj[4], "Iron Door"), "puzzle door removed");
        });
        check("sleep uses ticks and resumes with original sprites", () -> {
            new OBJ_Fishbowl(gp).use(gp.player);
            render(15);
            equal(0, gp.ui.counter, "rendering cannot advance sleep");
            for (int i = 0; i < 250 && gp.gameState == gp.sleepState; i++) gp.update();
            equal(gp.playState, gp.gameState, "sleep finishes");
            truth(gp.eManager.lighting.filterAlpha == 0f, "sleep restores day");
        });
        check("Rocky and Iron retain their distinct destructible obstacles", () -> {
            int checked = 0;
            for (int map = 0; map < gp.iTile.length; map++) {
                for (int slot = 0; slot < gp.iTile[map].length; slot++) {
                    var obstacle = gp.iTile[map][slot];
                    if (obstacle == null || !obstacle.destructible) continue;
                    gp.currentMap = map;
                    gp.player.currentWeapon = gp.eGenerator.getObject("Normal buuble");
                    int life = obstacle.life;
                    gp.player.damageInteractiveTile(slot);
                    equal(life, obstacle.life, "normal attack cannot break an obstacle");
                    String tool = obstacle instanceof tile_interactive.IT_Rock ? "Rocky" : "Iron";
                    gp.player.currentWeapon = gp.eGenerator.getObject(tool);
                    truth(gp.player.currentWeapon != null, "original tool registered");
                    for (int hit = 0; hit < life; hit++) {
                        obstacle.invincible = false;
                        gp.player.damageInteractiveTile(slot);
                        if (hit == 0 && life > 1) {
                            gp.player.damageInteractiveTile(slot);
                            equal(life - 1, obstacle.life, "tile invulnerability prevents duplicate damage");
                        }
                    }
                    truth(gp.iTile[map][slot] instanceof tile_interactive.IT_Rocky, "obstacle becomes original rubble");
                    truth(!gp.iTile[map][slot].collision, "destroyed obstacle is passable");
                    checked++;
                }
            }
            equal(27, checked, "destructible placements including mandatory Iron gate");
        });
        check("a full bag does not consume a chest reward", () -> {
            OBJ_Chest chest = null;
            for (Entity item : gp.obj[5]) if (item instanceof OBJ_Chest candidate) { chest = candidate; break; }
            truth(chest != null, "reward chest found");
            while (gp.player.inventory.size() < 20)
                gp.player.inventory.add(gp.eGenerator.getObject("Normal buuble"));
            chest.interact();
            truth(!chest.opened && chest.loot != null, "full bag leaves reward available");
            finishDialogue();
            gp.player.inventory.remove(gp.player.inventory.size() - 1);
            chest.interact();
            truth(chest.opened, "reward can be collected after making space");
        });
        check("retry preserves world progress and new game restores the original world", () -> {
            gp.currentMap = 4;
            NPC_BigRock rock = null;
            for (Entity npc : gp.npc[4]) if (npc instanceof NPC_BigRock candidate) { rock = candidate; break; }
            truth(rock != null, "puzzle rock exists");
            rock.worldX += gp.tileSize;
            int x = rock.worldX;
            int door = find(gp.obj[4], "Iron Door");
            truth(door >= 0, "puzzle door exists");
            gp.obj[4][door] = null;
            Progress.GodFishDefeated = true;
            gp.player.level = 7;
            gp.projectile[4][0] = gp.player.projectile;
            gp.player.projectile.alive = true;
            gp.resetGame(false);
            equal(7, gp.player.level, "retry retains player progression");
            equal(x, rock.worldX, "retry retains puzzle position");
            equal(-1, find(gp.obj[4], "Iron Door"), "retry keeps puzzle door open");
            truth(Progress.GodFishDefeated, "retry retains boss victory");
            equal(-1, find(gp.monster[6], "God Fish"), "retry cannot resurrect defeated boss");
            equal(0, count(gp.projectile), "retry clears transient projectiles");
            gp.resetGame(true);
            equal(1, gp.player.level, "new game resets progression");
            truth(!Progress.GodFishDefeated, "new game resets boss victory");
            equal(87, count(gp.monster), "new game restores current population");
            truth(find(gp.obj[4], "Iron Door") >= 0, "new game restores puzzle door");
        });
        check("victory key, final door and original credits", StoryRegression::completeEnding);
        gp.stopGame();
        System.out.printf("STORY RESULT passed=%d failed=%d%n", passed, failed);
        if (failed != 0) System.exit(1);
    }

    private static void completeEnding() {
        place(FinalArena.MAP,FinalArena.KEY_X,FinalArena.KEY_Y);
        gp.currentArea=gp.dungeons;
        Progress.GodFishDefeated=true;
        gp.monster[FinalArena.MAP][FinalArena.BOSS_SLOT]=null;
        gp.obj[FinalArena.MAP][FinalArena.KEY_SLOT]=Temple.place(new object.OBJ_Key(gp),FinalArena.KEY_X,FinalArena.KEY_Y);
        int key = find(gp.obj[6], "Key");
        truth(key >= 0, "boss drops key");
        gp.player.pickUpObject(key);
        int keySlot = gp.player.searchItemInInventory("Key");
        truth(keySlot != 999, "key collected");
        int door = find(gp.obj[6], "Door");
        truth(door >= 0, "final locked door exists");
        gp.player.worldX = gp.obj[6][door].worldX;
        gp.player.worldY = gp.obj[6][door].worldY + gp.tileSize - gp.player.solidArea.y + gp.player.speed - 1;
        gp.player.direction = "up";
        gp.ui.playerSlotCol = keySlot % 5; gp.ui.playerSlotRow = keySlot / 5;
        gp.player.selectItem();
        truth(gp.obj[6][door] == null, "key opens final door");
        equal(999, gp.player.searchItemInInventory("Key"), "key consumed once");
        finishDialogue();
        int exit = find(gp.obj[6], "Exit");
        truth(exit >= 0 && gp.obj[6][exit] instanceof OBJ_Exit, "portal retains the original exit identity");
        gp.player.pickUpObject(exit);
        equal(gp.cutsceneState, gp.gameState, "ending begins");
        ticks(100);
        truth(gp.csManager.isBeachWalk(),"portal opens into the walk");
        gp.keyH.rightPressed=true;
        for (int i=0;i<600 && !gp.csManager.isDialogueActive();i++) gp.update();
        truth(gp.csManager.isDialogueActive(),"photo starts the dialogue");
        for (int i=0;i<18 && gp.csManager.isDialogueActive();i++) {
            gp.keyH.enterPressed=true;
            gp.update();
        }
        gp.keyH.releaseAll();
        truth(gp.csManager.isCoast(), "ending dialogue finished");
        for (int i = 0; i < 2000 && gp.csManager.scenePhase < CutsceneManager.ENDING_CREDITS; i++) gp.update();
        equal(CutsceneManager.ENDING_CREDITS, gp.csManager.scenePhase, "credits reached");
        equal(24, gp.lastMusic, "credits music");
        int y = gp.csManager.y;
        render(30);
        equal(y, gp.csManager.y, "drawing cannot scroll credits");
        ticks(10);
        equal(y - 10, gp.csManager.y, "credits scroll once per tick");
    }
}
