package data;

import entity.Entity;
import entity.NPC_TV;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import main.EventHandler;
import main.GamePanel;
import main.Market;
import main.RefugeChallenges;
import main.Temple;
import main.TempleExtension;

public final class MarketSaveRegression {
    private static final class Quiet extends GamePanel {
        @Override public void playMusic(int id) {}
        @Override public void stopMusic() {}
        @Override public void playSE(int id) {}
    }

    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    private static void write(Path path, Object snapshot) throws Exception {
        try (var out = new ObjectOutputStream(Files.newOutputStream(path))) { out.writeObject(snapshot); }
    }

    private static SaveSnapshot read(Path path) throws Exception {
        try (var in = new ObjectInputStream(Files.newInputStream(path))) { return (SaveSnapshot) in.readObject(); }
    }

    private static void singleTV(GamePanel gp) {
        long copies = Arrays.stream(gp.npc).flatMap(Arrays::stream).filter(NPC_TV.class::isInstance).count();
        check(copies == 1 && gp.npc[Market.MAP][Market.TV_SLOT] instanceof NPC_TV, "one TV at market");
        Entity tv = gp.npc[Market.MAP][Market.TV_SLOT];
        check(tv.worldX == 22 * 48 && tv.worldY == 23 * 48 && tv.inventory.isEmpty(), "migrated TV position and catalog");
    }

    private static void oldShape(SaveSnapshot s) {
        s.marketRevision = 0;
        s.world.objects = Arrays.copyOf(s.world.objects, 10);
        s.world.monsters = Arrays.copyOf(s.world.monsters, 10);
        s.world.tiles = Arrays.copyOf(s.world.tiles, 10);
        SaveSnapshot.Actor tv = s.world.npcs[10][Market.TV_SLOT];
        s.world.npcs = Arrays.copyOf(s.world.npcs, 10);
        tv.x = 26 * 48; tv.y = 40 * 48;
        s.world.npcs[6][16] = tv;
    }

    public static void main(String[] args) throws Exception {
        var gp = new Quiet();
        gp.setupGame();
        Path scratch = Files.createTempDirectory(Path.of("build"), "market-save-");
        Path path = scratch.resolve("save-v1.dat");
        var save = new SaveLoad(gp, path);
        save.captureInitialWorld();
        singleTV(gp);
        int originals = 0;
        for (int scenario = 0; scenario < 4; scenario++) {
            gp.resetGame(true);
            gp.currentMap = scenario == 0 ? 9 : 6;
            gp.currentArea = gp.nextArea = gp.dungeons;
            Temple.place(gp.player, scenario == 0 ? 24 : 23, scenario == 0 ? 4 : 44);
            gp.player.coin = 32;
            gp.extraRooms.restore(31);
            gp.extraRooms.later.restore(new RefugeChallenges.State(1, 2, true, 2));
            if (scenario == 0) {
                for (var lane : TempleExtension.LANES) {
                    var rock = Temple.place(gp.npc[9][lane.rockSlot()], lane.end().x(), lane.end().y());
                    rock.linkedEntity = gp.iTile[9][lane.plateSlot()];
                }
                gp.obj[9][0] = null;
            } else if (scenario == 2) {
                gp.bossBattleOn = true;
                gp.monster[6][87].life = 17;
            } else if (scenario == 3) {
                Progress.GodFishDefeated = true;
                gp.monster[6][87] = null;
                gp.obj[6][9] = null;
                gp.player.inventory.add(gp.eGenerator.requireObject("key"));
            }
            check(save.trySave(), save.getLastError());
            var previous = read(path);
            oldShape(previous);
            write(path, previous);
            Path original = Path.of("build/market-baseline/scenario" + scenario + ".dat");
            if (Files.isRegularFile(original)) {
                Files.copy(original, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                originals++;
            }
            byte[] oldBytes = Files.readAllBytes(path);
            check(save.tryLoad(), save.getLastError());
            check(Arrays.equals(oldBytes, Files.readAllBytes(path)), "loading does not rewrite legacy bytes");
            singleTV(gp);
            check(gp.currentMap == previous.map && gp.player.worldX == previous.x && gp.player.worldY == previous.y
                && gp.player.coin == 32, "older position and balance");
            check(gp.bossBattleOn == (scenario == 2) && Progress.GodFishDefeated == (scenario == 3), "boss progress");
            if (scenario == 2) check(gp.monster[6][87].life == 17, "active boss life");
            if (scenario == 3) check(gp.monster[6][87] == null && gp.obj[6][9] == null
                && gp.player.searchItemInInventory("Key") != 999 && gp.obj[6][10] != null, "arena key, open door and exit");
            check(gp.extraRooms.flags() == 31 && gp.extraRooms.later.state().equals(new RefugeChallenges.State(1, 2, true, 2)),
                "approved refuge progress");
            if (scenario == 0) check(TempleExtension.solved(gp.npc, gp.iTile), "octava solved remains solved");
            if (scenario == 1) {
                gp.gameState = gp.playState;
                gp.keyH.enterPressed = true;
                gp.eHandler.checkEvent();
                check(gp.gameState == gp.transitionState, "old pre-battle player can visit market");
                gp.keyH.enterPressed = false;
                for (int i = 0; i < 50; i++) gp.flow.update();
                Temple.place(gp.player, 24, 29);
                gp.eHandler = new EventHandler(gp);
                gp.eHandler.checkEvent();
                for (int i = 0; i < 50; i++) gp.flow.update();
                check(gp.currentMap == 9 && gp.player.worldY == 7 * 48 && gp.obj[9][0] != null,
                    "return from old arena lands south of unresolved gate");
            }
            gp.resetGame(false);
            singleTV(gp);
        }

        gp.resetGame(true);
        singleTV(gp);
        gp.currentMap = Market.MAP;
        gp.currentArea = gp.nextArea = gp.indoor;
        Temple.place(gp.player, 24, 27);
        check(save.trySave(), save.getLastError());
        var modern = read(path);
        check(modern.marketRevision == 1 && modern.world.objects.length == 11, "eleven-map save revision");
        var live = gp.player;
        modern.world.monsters = Arrays.copyOf(modern.world.monsters, 10);
        write(path, modern);
        check(!save.tryLoad() && gp.player == live, "invalid mixed shape leaves live state alone");
        check(save.trySave(), save.getLastError());
        modern = read(path);
        oldShape(modern);
        modern.map = 0;
        modern.world.npcs[0] = new SaveSnapshot.Actor[1];
        write(path, modern);
        check(!save.tryLoad() && gp.player == live, "invalid legacy slots leave live state alone");
        check(save.trySave(), save.getLastError());
        modern = read(path);
        modern.world.npcs[6][16] = modern.world.npcs[10][Market.TV_SLOT];
        write(path, modern);
        check(!save.tryLoad() && gp.player == live, "duplicate TV rejected");

        Path legacy = scratch.resolve("save.dat");
        Files.delete(path);
        var old = new DataStorage();
        old.level = 1; old.life = old.maxLife = 8; old.maxWater = 5;
        old.strenght = 1; old.nextLevelExp = 5;
        old.currentShieldSlot = 1;
        old.itemNames.add("Normal buuble"); old.itemNames.add("Bubble shield");
        old.itemAmounts.add(1); old.itemAmounts.add(1);
        old.mapObjectNames = new String[10][30];
        for (String[] row : old.mapObjectNames) Arrays.fill(row, "NA");
        old.mapObjectWorldX = new int[10][30]; old.mapObjectWorldY = new int[10][30];
        old.mapObjectLootNames = new String[10][30]; old.mapObjectOpened = new boolean[10][30];
        write(legacy, old);
        byte[] bytes = Files.readAllBytes(legacy);
        check(save.tryLoad(), save.getLastError());
        singleTV(gp);
        check(gp.currentMap == 0 && gp.player.worldX == 13 * 48 && Arrays.equals(bytes, Files.readAllBytes(legacy)),
            "original-format ten rows import to new world");
        old.mapObjectOpened = new boolean[9][30];
        write(legacy, old);
        live = gp.player;
        check(!save.tryLoad() && gp.player == live, "original malformed parallel array rejected");
        gp.stopGame();
        System.out.println("MARKET_SAVE PASS: four pre-market checkpoints, stable boss and refuge progress, original ten-row format and invalid rollback; historical=" + originals);
    }
}
