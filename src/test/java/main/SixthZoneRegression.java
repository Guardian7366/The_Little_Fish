package main;

import data.SaveLoad;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import monster.MON_Tella;
import monster.MON_Sharky;
import object.OBJ_Iron;
import object.OBJ_Lantern;
import object.OBJ_Rock;
import tile_interactive.IT_Rocky;
import static main.FifthZoneRegression.*;
import static main.FishtellaRegression.check;

public final class SixthZoneRegression {
    public static void main(String[] args) throws Exception {
        Quiet gp = new Quiet();
        gp.setupGame();
        portal(gp, 3, 9, 44, 8);
        check(gp.player.worldX == 25 * 48 && gp.player.worldY == 43 * 48, "safe sixth arrival");
        check(Arrays.stream(gp.monster[8]).filter(MON_Tella.class::isInstance).count() == 4, "four Fishtellas");
        check(Arrays.stream(gp.monster[8]).filter(MON_Sharky.class::isInstance).count() == 5, "five Sharkys");
        gp.extraRooms.ending.restore(new FinalRefuges.State(3,0,0,false));
        var start = relaxedWorld(gp, cell(8,25,43));
        check(start[cell(8,24,21)] && start[cell(8,15,28)] && start[cell(8,32,29)], "both forks reach the gate");
        check(start[cell(5,28,12)], "can return to obtain Iron");
        check(!start[cell(8,25,3)] && !start[cell(4,24,42)], "no bypass even ignoring optional obstacles and actors");
        walk(gp,25,43,23,45);
        var ironChest = gp.obj[5][8];
        ironChest.opened = true;
        portal(gp,8,23,45,3);
        check(!ironChest.opened && gp.player.inventory.stream().noneMatch(OBJ_Iron.class::isInstance), "lost Iron offered in its chest on return");
        check(gp.player.worldX == 17 * 48, "fifth return before its Iron gate");
        portal(gp,3,9,44,8);
        for (int slot : SixthZone.SHORTCUT_SLOTS) gp.iTile[8][slot] = gp.iTile[8][slot].getDestroyedForm();
        walk(gp,25,43,25,30);
        check(!relaxedWorld(gp,cell(8,25,43))[cell(8,25,3)], "open shortcut remains on the entry side");
        walk(gp,25,43,24,21);

        Path out = Path.of("build/sixth-preview");
        Files.createDirectories(out);
        gp.player.worldX = 24 * 48;
        gp.player.worldY = 22 * 48;
        capture(gp,out,"iron-fixed");
        gp.player.currentLight = new OBJ_Lantern(gp);
        gp.player.lightUpdated = true;
        capture(gp,out,"iron-lantern");
        gp.player.currentLight = null;
        gp.player.lightUpdated = true;

        for (int x = 24 * 48 - 12; x <= 26 * 48 - 12; x += 4) {
            gp.player.worldX = x;
            gp.player.worldY = 21 * 48;
            gp.player.direction = "up";
            gp.player.speed = 4;
            for (int step = 0; step < 30; step++) {
                gp.player.collisionOn = false;
                gp.cChecker.checkTile(gp.player);
                gp.cChecker.checkEntity(gp.player, gp.iTile);
                if (gp.player.collisionOn) break;
                gp.player.worldY -= 4;
            }
            check(gp.player.worldY > 19 * 48, "real hitbox cannot squeeze through metal");
        }

        var normal = gp.player.currentWeapon;
        var iron = new OBJ_Iron(gp);
        gp.player.inventory.add(iron);
        for (int slot : SixthZone.GATE_SLOTS) {
            var block = gp.iTile[8][slot];
            for (var tool : new entity.Entity[]{normal, new OBJ_Rock(gp)}) {
                gp.player.currentWeapon = tool;
                gp.player.damageInteractiveTile(slot);
                check(block.life == 3, "only Iron damages the gate");
            }
            gp.player.currentWeapon = iron;
            for (int hit = 0; hit < 3; hit++) {
                block.invincible = false;
                gp.player.damageInteractiveTile(slot);
            }
            check(gp.iTile[8][slot] instanceof IT_Rocky, "three strikes create rubble");
        }
        gp.player.currentWeapon = normal;
        walk(gp,25,43,25,3);
        check(relaxedWorld(gp,cell(8,25,43))[cell(4,24,42)], "exit reachable after deselecting Iron");
        Path saveDir = Files.createTempDirectory(Path.of("build"),"sixth-gate-");
        SaveLoad saves = new SaveLoad(gp,saveDir.resolve("save-v1.dat"));
        check(saves.trySave(), saves.getLastError());
        SixthZone.obstacles(gp,gp.iTile);
        check(saves.tryLoad(), saves.getLastError());
        for (int slot : SixthZone.GATE_SLOTS) check(gp.iTile[8][slot] instanceof IT_Rocky, "rubble survives load");
        walk(gp,25,43,25,3);
        portal(gp,8,25,3,4);
        check(gp.player.worldY == 42 * 48, "former sixth arrival separate from trigger");
        walk(gp,24,42,24,44);
        portal(gp,4,24,44,8);
        check(gp.player.worldY == 6 * 48, "return to exit side");
        for (int slot : SixthZone.GATE_SLOTS) check(gp.iTile[8][slot] instanceof IT_Rocky, "travel preserves rubble");
        SixthZone.obstacles(gp,gp.iTile);
        walk(gp,25,6,25,3);
        portal(gp,8,25,3,4);

        portal(gp,3,9,44,8);
        walk(gp,25,43,23,36);
        MON_Tella first = (MON_Tella)gp.monster[8][0];
        for (int i = 0; i < 45; i++) first.update();
        check(first.phase() == MON_Tella.Phase.WARNING && first.bands().size() == 8, "first presentation has both lanes");
        check(Arrays.stream(gp.monster[8]).filter(MON_Sharky.class::isInstance)
            .map(MON_Sharky.class::cast).allMatch(s -> s.state() == MON_Sharky.State.LURK), "presentation before Sharky");
        capture(gp,out,"first-warning");
        walk(gp,23,36,23,37);
        for (int i = 0; i < 45; i++) first.update();
        capture(gp,out,"first-pulse");
        for (int i = 0; i < 10; i++) first.update();
        capture(gp,out,"first-core");
        walk(gp,23,37,24,36);
        for (int i = 0; i < 24; i++) first.update();
        gp.player.direction = "right";
        gp.player.spriteCounter = 0;
        gp.player.attacking = true;
        int coreLife = first.life;
        for (int i = 0; i < 35 && first.life == coreLife; i++) {
            gp.player.attacking();
            first.update();
        }
        check(first.life < coreLife && first.phase() == MON_Tella.Phase.CORE, "normal melee reaches core after stepping back in");
        gp.tileM.reloadCurrentMap();
        check(first.phase() == MON_Tella.Phase.ARMORED && first.bands().isEmpty(), "reload resets cycle");

        MON_Tella mixed = (MON_Tella)gp.monster[8][2];
        MON_Sharky shark = (MON_Sharky)gp.monster[8][6];
        gp.player.worldX = 22 * 48;
        gp.player.worldY = 14 * 48;
        for (int i = 0; i < 90; i++) shark.update();
        for (int i = 0; i < 45; i++) mixed.update();
        check(shark.state() == MON_Sharky.State.PREPARE && mixed.phase() == MON_Tella.Phase.WARNING, "both mixed threats visible");
        capture(gp,out,"mixed-warning");
        int life = gp.player.life;
        walk(gp,22,14,22,15);
        for (int i = 0; i < 55; i++) {
            shark.update();
            mixed.update();
        }
        check(gp.player.life == life, "walkable sidestep avoids both mixed threats");
        gp.stopGame();
        System.out.println("SIXTH_ZONE PASS: physical Iron gate, four portals, safe returns, isolated and mixed encounters");
    }
}
