package data;

import entity.Entity;
import entity.NPC_BigRock;
import entity.Player;
import main.GamePanel;
import monster.MON_GodFish;
import object.OBJ_Chest;
import object.OBJ_Exit;
import object.OBJ_Lantern;
import tile_interactive.IT_MetalPlate;
import tile_interactive.IT_Rocky;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

/** Real serialization, invalid-load rollback and original-JAR migration without test libraries. */
public final class SaveRegression {
    private static int assertions;
    private static final class QuietPanel extends GamePanel {
        @Override public void playMusic(int index) { }
        @Override public void playSE(int index) { }
        @Override public void stopMusic() { }
    }

    private static QuietPanel panel() {
        QuietPanel gp = new QuietPanel();
        Progress.GodFishDefeated = false;
        gp.aSetter.setObject();
        gp.aSetter.setNPC();
        gp.aSetter.setMonster();
        gp.aSetter.setInteractiveTile();
        gp.currentArea = gp.outside;
        return gp;
    }

    private static void check(boolean valid, String contract) {
        if (!valid) throw new AssertionError(contract);
        assertions++;
        System.out.println("PASS " + contract);
    }

    public static void main(String[] args) throws Exception {
        Path fixtureRoot = Path.of("build", "save-test-fixtures").toAbsolutePath();
        Files.createDirectories(fixtureRoot);
        Path scratch = Files.createTempDirectory(fixtureRoot, "run-");
        roundTripAndRollback(scratch.resolve("current"));
        bossState(scratch.resolve("boss"));
        firstZoneMigration(scratch.resolve("first-zone"));
        secondZoneMigration(scratch.resolve("second-zone"));
        fourthZoneMigration(scratch.resolve("fourth-zone"));
        if (args.length != 0) legacy(scratch.resolve("legacy"), Path.of(args[0]).toAbsolutePath());
        else System.out.println("SKIP original-JAR import: pass the original MyGame.jar path");
        System.out.println("Save regression: " + assertions + " assertions passed. Fixtures: " + scratch);
    }

    private static void roundTripAndRollback(Path folder) throws Exception {
        QuietPanel gp = panel();
        SaveLoad saves = new SaveLoad(gp, folder.resolve("save-v1.dat"));
        saves.captureInitialWorld();
        gp.currentMap = 4; gp.currentArea = gp.dungeons;
        gp.player.worldX = 24 * 48; gp.player.worldY = 30 * 48;
        gp.player.dexterity = 7; gp.player.defense = gp.player.getDefense();
        OBJ_Lantern light = new OBJ_Lantern(gp);
        gp.player.inventory.add(light); gp.player.currentLight = light;
        gp.obj[5][1].opened = true;
        gp.obj[5][1].down = gp.obj[5][1].image2;
        gp.iTile[2][0] = new IT_Rocky(gp, 18, 43);
        NPC_BigRock rock = null; IT_MetalPlate plate = null;
        int rockSlot = -1, plateSlot = main.Temple.LANES[0].plateSlot();
        for (int i = 0; i < gp.npc[4].length; i++) if (gp.npc[4][i] instanceof NPC_BigRock r) { rock = r; rockSlot = i; break; }
        plate = (IT_MetalPlate)gp.iTile[4][plateSlot];
        rock.worldX = plate.worldX; rock.worldY = plate.worldY; rock.linkedEntity = plate;
        gp.monster[0][0].life = 2;
        check(saves.trySave(), "initial checkpoint writes: " + saves.getLastError());
        byte[] checkpoint = Files.readAllBytes(saves.getSavePath());
        SaveSnapshot snapshot = read(saves.getSavePath());
        check(snapshot.inventory[0].id.equals("normal-bubble"), "save uses stable object IDs");
        gp.player.dexterity = 1; gp.currentMap = 0; gp.obj[6][10] = null;
        gp.iTile[2][0] = null; gp.npc[4][rockSlot] = null;
        check(saves.tryLoad(), "complete checkpoint loads: " + saves.getLastError());
        check(gp.player.dexterity == 7 && gp.player.defense == 7, "dexterity and defense survive save/load");
        check(gp.currentMap == 4 && gp.currentArea == gp.dungeons && gp.player.worldX == 24 * 48 && gp.player.worldY == 30 * 48, "map, area and position restored");
        check(gp.obj[6][10] instanceof OBJ_Exit, "ending Exit survives save/load");
        check(gp.obj[5][1] instanceof OBJ_Chest && gp.obj[5][1].opened && gp.obj[5][1].down == gp.obj[5][1].image2, "open chest and artwork restored");
        check(gp.player.currentLight == gp.player.inventory.get(2) && gp.player.lightUpdated, "equipped lantern restored by inventory identity");
        check(gp.iTile[2][0] instanceof IT_Rocky, "destroyed rock remains destroyed");
        check(gp.npc[4][rockSlot].linkedEntity == gp.iTile[4][plateSlot], "rock/plate references reconstructed");
        check(gp.monster[0][0].life == 2, "enemy health restored at checkpoint");
        check(saves.trySave(), "subsequent checkpoint writes");
        check(Arrays.equals(checkpoint, Files.readAllBytes(saves.getSavePath().resolveSibling("save-v1.dat.bak"))), "backup preserves previous checkpoint bytes");

        Player livePlayer = gp.player; Entity[][] liveObjects = gp.obj;
        int liveCoin = gp.player.coin;
        snapshot.inventory[0].id = "unknown-future-object";
        write(saves.getSavePath(), snapshot);
        check(!saves.tryLoad(), "unknown object ID rejected");
        check(gp.player == livePlayer && gp.obj == liveObjects && gp.player.coin == liveCoin, "unknown-ID load leaves live world untouched");
        Files.write(saves.getSavePath(), new byte[] { 1, 2, 3 });
        check(!saves.tryLoad() && gp.player == livePlayer && gp.obj == liveObjects, "truncated file rejected without partial load");
        Files.write(saves.getSavePath(), checkpoint);
        snapshot = read(saves.getSavePath()); snapshot.version = 999;
        write(saves.getSavePath(), snapshot);
        check(!saves.tryLoad() && gp.player == livePlayer, "unknown schema version rejected");
        Files.write(saves.getSavePath(), checkpoint);
        snapshot = read(saves.getSavePath()); snapshot.world.npcs[4][rockSlot].linkedTile = 0;
        write(saves.getSavePath(), snapshot);
        check(!saves.tryLoad() && gp.player == livePlayer, "invalid rock/plate link rejected before publish");
        Files.write(saves.getSavePath(), checkpoint);
        gp.player.currentWeapon = new object.OBJ_Weapon_Normal(gp);
        check(!saves.trySave() && Arrays.equals(checkpoint, Files.readAllBytes(saves.getSavePath())), "invalid equipment cannot overwrite good checkpoint");
        gp.player.currentWeapon = gp.player.inventory.get(0);
        byte[] lastGoodBackup = Files.readAllBytes(saves.getSavePath().resolveSibling("save-v1.dat.bak"));
        Files.write(saves.getSavePath(), new byte[] { 0, 1, 2 });
        check(saves.trySave(), "valid game state can replace a damaged current file");
        check(Arrays.equals(lastGoodBackup, Files.readAllBytes(saves.getSavePath().resolveSibling("save-v1.dat.bak"))), "damaged previous file cannot overwrite good backup");
    }

    private static void secondZoneMigration(Path folder) throws Exception {
        QuietPanel gp=panel();gp.currentMap=1;gp.player.worldX=36*48;gp.player.worldY=41*48;
        gp.monster[1][7]=null;gp.monster[1][6].life=2;gp.obj[5][1].opened=true;
        SaveLoad saves=new SaveLoad(gp,folder.resolve("save-v1.dat"));
        check(saves.trySave(),"second zone writes");SaveSnapshot snapshot=read(saves.getSavePath());
        check(snapshot.secondZoneRevision==1,"second-zone revision saved");snapshot.secondZoneRevision=0;
        snapshot.world.monsters[1][6].x=26*48;snapshot.world.monsters[1][6].y=41*48;write(saves.getSavePath(),snapshot);
        byte[] before=Files.readAllBytes(saves.getSavePath());
        check(saves.tryLoad()&&gp.player.worldX==14*48&&gp.player.worldY==41*48,"old second zone relocates player safely");
        check(gp.monster[1][7]==null&&gp.monster[1][6].life==2&&gp.monster[1][6].worldX==22*48&&gp.obj[5][1].opened,"second zone preserves defeats, health and rewards");
        check(Arrays.equals(before,Files.readAllBytes(saves.getSavePath())),"second-zone load does not rewrite save");
        gp.player.worldX=23*48;gp.player.worldY=35*48;
        check(saves.trySave()&&saves.tryLoad()&&gp.player.worldX==23*48&&gp.player.worldY==35*48,"second-zone new layout roundtrip exact");
        snapshot.map=3;snapshot.area=gp.dungeons;snapshot.x=25*48;snapshot.y=23*48;write(saves.getSavePath(),snapshot);
        check(saves.tryLoad()&&gp.currentMap==3&&gp.player.worldX==25*48,"other zone unaffected by migration");
        Player live=gp.player;snapshot.secondZoneRevision=999;write(saves.getSavePath(),snapshot);
        check(!saves.tryLoad()&&gp.player==live,"future second-zone revision rejected transactionally");
    }

    private static void firstZoneMigration(Path folder) throws Exception {
        QuietPanel gp=panel();
        SaveLoad saves=new SaveLoad(gp,folder.resolve("save-v1.dat"));
        gp.monster[0][1]=null;
        gp.monster[0][0].life=2;
        gp.obj[5][1].opened=true;
        check(saves.trySave(), "new reef snapshot writes");
        SaveSnapshot snapshot=read(saves.getSavePath());
        check(snapshot.firstZoneRevision==main.FirstZone.REVISION,"new reef revision recorded");
        snapshot.firstZoneRevision=0;
        snapshot.x=37*48; snapshot.y=19*48; // Old floor, now outside the new reef.
        snapshot.world.monsters[0][0].x=13*48; snapshot.world.monsters[0][0].y=20*48;
        write(saves.getSavePath(),snapshot);
        byte[] original=Files.readAllBytes(saves.getSavePath());
        check(saves.tryLoad(),"old layout checkpoint adapts");
        check(gp.player.worldX==13*48 && gp.player.worldY==31*48,"old layout player returns to safe emerald");
        check(gp.monster[0][0].worldX==22*48 && gp.monster[0][0].life==2 && gp.monster[0][1]==null,
            "migration relocates survivors, preserves health and defeated enemies");
        check(gp.obj[5][1].opened,"migration preserves other-zone rewards");
        check(Arrays.equals(original,Files.readAllBytes(saves.getSavePath())),"load never rewrites previous checkpoint");
        gp.player.worldX=24*48;gp.player.worldY=32*48;
        check(saves.trySave() && saves.tryLoad() && gp.player.worldX==24*48 && gp.player.worldY==32*48,
            "new layout roundtrip keeps exact player position");
        snapshot.firstZoneRevision=0;snapshot.map=4;snapshot.area=gp.dungeons;snapshot.x=24*48;snapshot.y=30*48;
        write(saves.getSavePath(),snapshot);
        check(saves.tryLoad() && gp.currentMap==4 && gp.player.worldX==24*48 && gp.player.worldY==30*48,
            "old layout saved in another zone stays there");
        Player live=gp.player;snapshot.firstZoneRevision=999;write(saves.getSavePath(),snapshot);
        check(!saves.tryLoad() && gp.player==live,"future layout rejected transactionally");
    }

    private static void bossState(Path folder) throws Exception {
        QuietPanel gp = panel();
        SaveLoad saves = new SaveLoad(gp, folder.resolve("save-v1.dat"));
        MON_GodFish boss = null; int slot = -1;
        for (int i = 0; i < gp.monster[6].length; i++) if (gp.monster[6][i] instanceof MON_GodFish b) { boss = b; slot = i; break; }
        gp.currentMap = 6; gp.currentArea = gp.dungeons;
        main.Temple.place(gp.player,24,44);
        check(saves.trySave() && saves.tryLoad(), "pre-encounter checkpoint roundtrip");
        boss = (MON_GodFish)gp.monster[6][slot];
        check(boss.life==650 && boss.sleep && !gp.bossBattleOn,"pre-encounter boss state");
        gp.bossBattleOn = true;
        check(!saves.trySave(),"new active fight cannot replace an earlier checkpoint");
        gp.monster[6][slot] = null; gp.bossBattleOn = false; Progress.GodFishDefeated = true;
        check(saves.trySave(), "defeated boss checkpoint writes");
        Progress.GodFishDefeated = false;
        check(saves.tryLoad() && Progress.GodFishDefeated && gp.monster[6][slot] == null, "defeated boss does not resurrect on load");
        Progress.GodFishDefeated = false;
    }

    private static void legacy(Path folder, Path originalJar) throws Exception {
        Files.createDirectories(folder);
        String java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
        Path testClasses = Path.of(SaveRegression.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Process process = new ProcessBuilder(java, "-Djava.awt.headless=true", "-cp", originalJar + File.pathSeparator + testClasses,
                "data.LegacySaveFixture").directory(folder.toFile()).inheritIO().start();
        check(process.waitFor() == 0 && Files.exists(folder.resolve("save.dat")), "legacy fixture produced by original JAR");
        byte[] originalBytes = Files.readAllBytes(folder.resolve("save.dat"));
        QuietPanel gp = panel();
        SaveLoad saves = new SaveLoad(gp, folder.resolve("save-v1.dat"));
        saves.captureInitialWorld();
        check(saves.tryLoad(), "original serialized class is compatible: " + saves.getLastError());
        check(gp.player.level == 6 && gp.player.dexterity == 6 && gp.player.defense == 6 && gp.player.coin == 42, "legacy missing dexterity recovered from original level rule");
        check(gp.obj[6][10] instanceof OBJ_Exit && gp.obj[5][1].opened, "legacy Exit and opened chests recovered");
        check(gp.currentMap == 0 && gp.player.worldX == 13 * 48 && !saves.getLastWarning().isEmpty(), "legacy incomplete position resumes at original start with explanation");
        check(saves.trySave() && Arrays.equals(originalBytes, Files.readAllBytes(folder.resolve("save.dat"))), "migration writes new format while preserving legacy bytes");
    }

    private static void fourthZoneMigration(Path folder) throws Exception {
        QuietPanel gp=panel();SaveLoad saves=new SaveLoad(gp,folder.resolve("save-v1.dat"));
        gp.currentMap=3;gp.currentArea=gp.dungeons;gp.player.worldX=25*48;gp.player.worldY=23*48;
        gp.monster[2][27]=null;
        check(saves.trySave(),"save before fourth-zone migration");
        SaveSnapshot old=read(saves.getSavePath());old.fourthZoneRevision=0;
        Arrays.fill(old.world.monsters[7],null);Arrays.fill(old.world.tiles[7],null);write(saves.getSavePath(),old);
        byte[] bytes=Files.readAllBytes(saves.getSavePath());
        check(saves.tryLoad(),"old save initializes fourth zone");
        check(gp.currentMap==3 && gp.player.worldX==25*48 && gp.monster[2][27]==null,"migration keeps old map and defeated enemy");
        check(Arrays.stream(gp.monster[7]).filter(e->e!=null).count()==8 && Arrays.stream(gp.iTile[7]).filter(e->e!=null).count()==8,"new area populated once");
        check(Arrays.equals(bytes,Files.readAllBytes(saves.getSavePath())),"migration does not rewrite source save");
        gp.currentMap=7;gp.currentArea=gp.outside;gp.player.worldX=25*48;gp.player.worldY=8*48;
        gp.monster[7][0]=null;gp.monster[7][1].life=3;gp.iTile[7][0]=new IT_Rocky(gp,25,11);
        check(saves.trySave()&&saves.tryLoad(),"fourth-zone save and load");
        check(gp.currentMap==7 && gp.monster[7][0]==null && gp.monster[7][1].life==3 && gp.iTile[7][0] instanceof IT_Rocky,"fourth-zone progress is not repopulated on load");
        gp.stopGame();
    }

    private static SaveSnapshot read(Path file) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) { return (SaveSnapshot)in.readObject(); }
    }

    private static void write(Path file, SaveSnapshot snapshot) throws Exception {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) { out.writeObject(snapshot); }
    }
}
