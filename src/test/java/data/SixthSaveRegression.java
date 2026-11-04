package data;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import main.GamePanel;
import main.SixthZone;
import monster.MON_Tella;

public final class SixthSaveRegression {
    public static void main(String[] args) throws Exception {
        Path folder = Files.createTempDirectory(Path.of("build"), "sixth-migration-");
        int real = 0;
        for (int map : new int[]{3,4,6}) {
            GamePanel gp = new GamePanel();
            gp.setupGame();
            Path target = folder.resolve("map-" + map + ".dat");
            Path legacy = Path.of("build/sixth-legacy-" + map + "/save-v1.dat");
            int expectedY = (map == 3 ? 23 : 42) * 48;
            SaveLoad saves = new SaveLoad(gp,target);
            if (Files.isRegularFile(legacy)) {
                Files.copy(legacy,target);
                real++;
            } else {
                gp.currentMap = map;
                gp.currentArea = gp.dungeons;
                gp.player.worldX = 24 * 48;
                gp.player.worldY = expectedY;
                gp.player.coin = 81;
                gp.monster[3][40] = null;
                check(saves.trySave(), saves.getLastError());
                SaveSnapshot snapshot;
                try (var input = new ObjectInputStream(Files.newInputStream(target))) {
                    snapshot = (SaveSnapshot)input.readObject();
                }
                snapshot.sixthZoneRevision = 0;
                Arrays.fill(snapshot.world.monsters[8],null);
                Arrays.fill(snapshot.world.tiles[8],null);
                Arrays.fill(snapshot.world.objects[8],null);
                try (var output = new ObjectOutputStream(Files.newOutputStream(target))) {
                    output.writeObject(snapshot);
                }
            }
            byte[] original = Files.readAllBytes(target);
            check(saves.tryLoad(), saves.getLastError());
            check(gp.currentMap == map && gp.player.worldX == 24 * 48 && gp.player.worldY == expectedY,
                "old position preserved on map " + map);
            check(gp.player.coin == 81 && gp.monster[3][40] == null, "old progression preserved");
            check(Arrays.stream(gp.monster[8]).filter(MON_Tella.class::isInstance).count() == 4, "new area initialized");
            check(gp.iTile[8][0].destructible && gp.iTile[8][1].destructible, "old saves start with closed sixth gate");
            check(Arrays.equals(original,Files.readAllBytes(target)), "load does not rewrite old bytes");
            var tella = (MON_Tella)gp.monster[8][0];
            tella.life = 31;
            gp.monster[8][1] = null;
            gp.obj[8][0] = null;
            check(saves.trySave() && saves.tryLoad(), "new revision roundtrip");
            tella = (MON_Tella)gp.monster[8][0];
            check(tella.life == 31 && tella.phase() == MON_Tella.Phase.ARMORED && tella.bands().isEmpty(), "Tella resumes safely");
            check(gp.monster[8][1] == null && gp.obj[8][0] == null, "new progress is not repopulated on load");
            gp.stopGame();
        }
        System.out.println("SIXTH_SAVE PASS: maps 3, 4, 6; real pre-sixth fixtures=" + real);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
