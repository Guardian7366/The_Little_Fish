package main;

import java.awt.Rectangle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import monster.MON_Tella;
import monster.MON_Tella.Phase;
import tile_interactive.IT_DestructibleWall;

public final class FishtellaRegression {
    public static void main(String[] args) throws Exception {
        var gp = new FifthZoneRegression.Quiet();
        gp.setupGame();
        gp.currentMap = 8;
        gp.gameState = gp.playState;
        for (int[] column : gp.tileM.mapTileNum[8]) Arrays.fill(column, 69);
        Arrays.fill(gp.monster[8], null);
        Arrays.fill(gp.iTile[8], null);
        gp.tileM.mapTileNum[8][18][20] = 88;
        var tella = new MON_Tella(gp);
        tella.worldX = tella.worldY = 20 * 48;
        tella.configureAbyss(tella.worldX, new Rectangle(15 * 48, 15 * 48, 11 * 48, 11 * 48));
        gp.monster[8][0] = tella;
        gp.player.worldX = 22 * 48;
        gp.player.worldY = 20 * 48;
        for (int i = 0; i < 45; i++) tella.update();
        check(tella.phase() == Phase.WARNING, "visible target starts warning");
        var warning = tella.bands();
        check(warning.size() == 5 && warning.stream().noneMatch(b -> b.x <= 18 * 48), "wall clips warning");
        for (int state : new int[]{gp.pauseState, gp.dialogueState}) {
            gp.gameState = state;
            for (int i = 0; i < 70; i++) tella.update();
            check(tella.phase() == Phase.WARNING && tella.bands().equals(warning), "cycle freezes outside play");
        }
        gp.gameState = gp.playState;
        int life = gp.player.life;
        gp.player.contactMonster(0);
        check(gp.player.life == life, "body contact is harmless");
        for (int i = 0; i < 44; i++) tella.update();
        check(tella.phase() == Phase.WARNING && gp.player.life == life, "45 tick warning is harmless");
        tella.update();
        check(tella.phase() == Phase.PULSE && tella.bands().equals(warning), "pulse equals warning");
        int after = gp.player.life;
        check(after < life, "hit from real player bounds");
        for (int i = 0; i < 9; i++) {
            gp.player.invincible = false;
            tella.update();
        }
        check(gp.player.life == after && tella.phase() == Phase.PULSE, "one hit per pulse");
        tella.update();
        check(tella.phase() == Phase.CORE && tella.defense == 2, "core exposed");
        int enemyLife = tella.life;
        for (int i = 0; i < 30; i++) tella.update();
        gp.player.damageMonster(0, gp.player, 6, 2);
        check(tella.life == enemyLife - 4 && tella.phase() == Phase.CORE, "counterattack halfway through the window");
        for (int i = 0; i < 29; i++) tella.update();
        check(tella.phase() == Phase.CORE, "hits do not shorten window");
        tella.update();
        check(tella.phase() == Phase.ARMORED && tella.defense == 10, "window closes at 60");
        gp.player.damageMonster(0, gp.player, 1, 0);
        check(tella.life == enemyLife - 5, "closed armor retains chip damage");
        for (int i = 0; i < 45; i++) tella.update();
        check(tella.phase() == Phase.WARNING && tella.bands().stream().allMatch(b -> b.x == 20 * 48), "next axis is vertical");

        tella.resetPulse();
        gp.iTile[8][0] = new IT_DestructibleWall(gp, 23, 20);
        for (int i = 0; i < 45; i++) tella.update();
        var blocked = tella.bands();
        check(blocked.stream().noneMatch(b -> b.x >= 23 * 48), "metal clips warning");
        gp.iTile[8][0] = null;
        gp.player.guarding = true;
        gp.player.guardCounter = 0;
        gp.player.direction = "left";
        gp.player.invincible = false;
        life = gp.player.life;
        for (int i = 0; i < 45; i++) tella.update();
        check(tella.bands().equals(blocked), "breaking metal never extends the pulse");
        check(gp.player.life == life && !tella.knockBack && !tella.offBalance, "frontal parry uses pulse origin without recoil");
        gp.iTile[8][0] = new IT_DestructibleWall(gp, 21, 20);
        tella.update();
        check(tella.bands().stream().noneMatch(b -> b.x > 20 * 48), "new obstruction truncates pulse");
        tella.worldX++;
        tella.update();
        check(tella.phase() == Phase.ARMORED && tella.bands().isEmpty(), "invalid origin clears attack");

        tella.resetPulse();
        gp.iTile[8][0] = null;
        gp.tileM.mapTileNum[8][21][20] = 88;
        for (int i = 0; i < 60; i++) tella.update();
        check(tella.phase() == Phase.ARMORED, "wall between actor and target prevents starting an attack");
        gp.tileM.mapTileNum[8][21][20] = 69;
        gp.player.worldX = 40 * 48;
        for (int i = 0; i < 60; i++) tella.update();
        check(tella.phase() == Phase.ARMORED, "offscreen actor cannot start an attack");

        gp.currentMap = 4;
        var old = new MON_Tella(gp);
        check(!old.usesPulse() && old.life == 100 && old.speed == 10, "legacy appearance unchanged");
        try (var files = Files.list(Path.of("design/sexta-zona/assets/fishtella"))) {
            var sprites = files.filter(f -> f.toString().endsWith(".png")).toList();
            check(sprites.size() == 12, "twelve sprites in package");
            for (Path p : sprites) {
                var image = javax.imageio.ImageIO.read(p.toFile());
                check(image.getWidth() == 16 && image.getHeight() == 16 && image.getColorModel().hasAlpha(),
                    "original sprite dimensions and alpha");
                check(Files.mismatch(p, Path.of("src/main/resources/sexta/fishtella").resolve(p.getFileName())) == -1,
                    "prepared sprite preserved");
            }
        }
        gp.stopGame();
        System.out.println("FISHTELLA PASS: geometry, occlusion, guard, chip damage, counterattack and pause");
    }

    static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
}
