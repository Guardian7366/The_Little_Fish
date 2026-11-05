package main;

import data.Progress;
import data.SaveLoad;
import entity.Entity;
import entity.NPC_TV;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.awt.event.KeyEvent.*;

public final class MarketRegression {
    private static final Path OUT = Path.of("build/mercado-preview");
    private static final FifthZoneRegression.Quiet gp = new FifthZoneRegression.Quiet();

    private static void check(boolean result, String contract) {
        if (!result) throw new AssertionError(contract);
    }

    private static void press(int code) {
        gp.keyH.keyPressed(new KeyEvent(gp, KEY_PRESSED, 0, 0, code, CHAR_UNDEFINED));
    }

    private static void release(int code) {
        gp.keyH.keyReleased(new KeyEvent(gp, KEY_RELEASED, 0, 0, code, CHAR_UNDEFINED));
    }

    private static void tap(int code) { press(code); release(code); }
    private static void confirm() { tap(VK_ENTER); tap(VK_ENTER); }
    private static void capture(String name) throws Exception { FifthZoneRegression.capture(gp, OUT, name); }

    private static void place(int map, int x, int y) {
        gp.currentMap = map;
        gp.currentArea = gp.nextArea = map == Market.MAP ? gp.indoor : gp.dungeons;
        gp.gameState = gp.playState;
        Temple.place(gp.player, x, y);
        gp.eHandler = new EventHandler(gp);
    }

    private static void openCounter() {
        place(Market.MAP, 22, 25);
        gp.player.direction = "up";
        press(VK_ENTER);
        gp.update();
        check(gp.gameState == gp.tradeState && gp.player.worldY == 25 * 48, "counter opens from adjoining floor");
    }

    private static Entity item(String id) { return gp.eGenerator.requireObject(id); }
    private static Entity bag(String name) {
        int index = gp.player.searchItemInInventory(name);
        return index == 999 ? null : gp.player.inventory.get(index);
    }

    public static void main(String[] args) throws Exception {
        gp.setupGame();
        Files.createDirectories(OUT);
        routes();
        trade();
        consumables();
        gp.stopGame();
        System.out.println("MARKET PASS: safe room, both routes, CRT and real bag, all prices, full bag, focus, stale offers, manual consumables and six captures");
    }

    private static void routes() throws Exception {
        place(9, 24, 5);
        gp.eHandler.checkEvent();
        check(gp.gameState == gp.playState, "locked extension cannot enter market");
        for (var lane : TempleExtension.LANES) {
            var rock = Temple.place(gp.npc[9][lane.rockSlot()], lane.end().x(), lane.end().y());
            rock.linkedEntity = gp.iTile[9][lane.plateSlot()];
        }
        gp.obj[9][0] = null;
        FifthZoneRegression.portal(gp, 9, 24, 5, Market.MAP);
        check(gp.player.worldY == 27 * 48 && gp.currentArea == gp.indoor, "market landing");
        check(gp.player.currentLight == null, "market visible without lantern");
        for (Entity enemy : gp.monster[Market.MAP]) check(enemy == null, "no enemies");
        capture("01-mercado");
        place(Market.MAP, 22, 25);
        capture("02-tv");
        gp.player.direction = "up";
        gp.keyH.upPressed = true;
        for (int i = 0; i < 40; i++) gp.update();
        gp.keyH.releaseAll();
        check(gp.player.worldY >= 24 * 48 + 36, "counter is solid during movement");
        FifthZoneRegression.portal(gp, Market.MAP, 24, 29, 9);
        check(gp.player.worldY == TempleExtension.GATE_Y * 48, "return through the open gate");
        gp.keyH.downPressed = true;
        for (int i = 0; i < 25; i++) gp.update();
        gp.keyH.releaseAll();
        check(gp.currentMap == 9 && gp.player.worldY >= 8 * 48, "walk back into octava through the open gate");
        FifthZoneRegression.portal(gp, Market.MAP, 24, 19, 6);
        check(gp.player.worldY == 44 * 48 && gp.player.worldX == 24 * 48, "unchanged boss landing");
        check(Temple.terrainClear(gp, 6, Temple.bounds(gp.player, 23 * 48, 44 * 48)), "arena return on safe floor");
        place(6, 23, 44);
        gp.bossBattleOn = true;
        gp.keyH.enterPressed = true;
        gp.eHandler.checkEvent();
        check(gp.gameState == gp.playState, "no return during battle");
        gp.bossBattleOn = false;
        gp.csManager.sceneNum = gp.csManager.GodFish;
        gp.eHandler.checkEvent();
        check(gp.gameState == gp.playState, "no return during cinematic");
        gp.csManager.reset();
        Progress.GodFishDefeated = true;
        gp.eHandler.checkEvent();
        check(gp.gameState == gp.playState, "completed arena keeps ending route");
        Progress.GodFishDefeated = false;
        gp.eHandler.checkEvent();
        check(gp.gameState == gp.transitionState, "return before battle");
        gp.keyH.enterPressed = false;
        for (int i = 0; i < 50; i++) gp.flow.update();
        check(gp.currentMap == Market.MAP && gp.player.worldY == 21 * 48, "arena return arrives clear of arch");
        gp.eHandler.checkEvent();
        check(gp.gameState == gp.playState, "no bounce");
        check(gp.npc[Market.MAP][Market.TV_SLOT] instanceof NPC_TV, "TV at market");
        for (Entity npc : gp.npc[6]) check(!(npc instanceof NPC_TV), "arena TV removed");
    }

    private static void trade() throws Exception {
        int initial = gp.player.inventory.size();
        gp.player.coin = 4;
        openCounter();
        capture("03-comprar");
        press(VK_ENTER);
        press(VK_ENTER);
        check(gp.player.coin == 4 && gp.player.inventory.size() == initial, "opening Enter repeats cannot confirm");
        release(VK_ENTER);
        press(VK_ENTER);
        capture("04-confirmar");
        for (int i = 0; i < 3; i++) gp.drawToTempScreen();
        check(gp.player.coin == 4 && gp.player.inventory.size() == initial, "draw is read-only");
        release(VK_ENTER);
        press(VK_ENTER);
        for (int i = 0; i < 10; i++) press(VK_ENTER);
        check(gp.player.coin == 0 && bag("Red Poti").amount == 1, "exact balance and one purchase per confirmation");
        release(VK_ENTER);
        confirm();
        check(gp.player.coin == 0 && bag("Red Poti").amount == 1, "insufficient balance");
        capture("06-aviso");
        gp.player.coin = 40;
        tap(VK_S);
        tap(VK_ENTER);
        tap(VK_ESCAPE);
        check(gp.gameState == gp.tradeState && gp.player.coin == 40 && bag("Strong Potion") == null, "cancel keeps list");
        tap(VK_ENTER);
        gp.keyH.releaseAll();
        tap(VK_ENTER);
        check(gp.player.coin == 40 && bag("Strong Potion") == null, "focus loss cancels offer");
        tap(VK_ENTER);
        check(gp.player.coin == 31 && bag("Strong Potion").amount == 1, "strong potion price");
        tap(VK_S);
        confirm();
        check(gp.player.coin == 26 && bag("Water Tonic").amount == 1, "water tonic price");

        Entity tonic = bag("Water Tonic");
        gp.player.inventory.remove(tonic);
        while (gp.player.inventory.size() < gp.player.maxinventorySize) gp.player.inventory.add(item("normal-bubble"));
        confirm();
        check(gp.player.coin == 26 && bag("Water Tonic") == null, "full bag rejects a new stack");
        tap(VK_Q);
        tap(VK_W);
        confirm();
        check(gp.player.coin == 17 && bag("Strong Potion").amount == 2, "full bag accepts existing stack");
        gp.player.inventory.subList(initial + 2, gp.player.inventory.size()).clear();
        gp.player.inventory.add(tonic);
        tap(VK_E);
        confirm();
        check(gp.player.inventory.contains(gp.player.currentWeapon) && gp.player.coin == 17, "equipped weapon locked");
        tap(VK_D);
        confirm();
        check(gp.player.inventory.contains(gp.player.currentShield) && gp.player.coin == 17, "equipped shield locked");
        capture("05-vender");
        tap(VK_D);
        confirm();
        check(bag("Red Poti") == null && gp.player.coin == 19, "one unit sold at half price");
        tap(VK_ENTER);
        Entity strong = gp.player.inventory.set(2, item("red-potion"));
        tap(VK_ENTER);
        check(gp.player.coin == 19 && strong.amount == 2, "stale offer cannot sell replacement");
        gp.player.inventory.set(2, strong);
        confirm();
        check(gp.player.coin == 23 && strong.amount == 1, "sale decrements stack by one");
        tap(VK_S);
        confirm();
        check(gp.player.coin == 23, "empty slot rejected");
        gp.player.inventory.add(item("rock-tool"));
        for (String id : new String[]{"rock-tool", "iron-tool", "key", "lantern", "toxic-bubble", "super-shield"}) {
            Entity unique = item(id);
            gp.player.inventory.set(4, unique);
            tap(VK_W);
            tap(VK_D);
            tap(VK_D);
            confirm();
            check(gp.player.inventory.get(4) == unique && gp.player.coin == 23, id + " protected");
            tap(VK_A);
            tap(VK_A);
            tap(VK_S);
        }
        gp.player.inventory.remove(4);
        tap(VK_ESCAPE);
        check(gp.gameState == gp.playState && !gp.keyH.enterPressed, "leaving shop clears action");
    }

    private static void consumables() throws Exception {
        Entity strong = bag("Strong Potion"), tonic = bag("Water Tonic");
        int strongIndex = gp.player.inventory.indexOf(strong), tonicIndex = gp.player.inventory.indexOf(tonic);
        gp.player.maxLife = 30;
        gp.player.maxWater = 10;
        gp.player.life = 17;
        gp.player.water = 9;
        strong.amount = 3;
        tonic.amount = 3;
        gp.ui.playerSlotRow = 0;
        gp.ui.playerSlotCol = strongIndex;
        gp.player.selectItem();
        check(gp.player.life == 27 && strong.amount == 2, "+10 HP");
        gp.player.selectItem();
        gp.player.selectItem();
        check(gp.player.life == 30 && strong.amount == 1, "HP capped, full bar preserves unit");
        gp.ui.playerSlotCol = tonicIndex;
        gp.player.selectItem();
        gp.player.selectItem();
        check(gp.player.water == 10 && tonic.amount == 2, "water capped, full bar preserves unit");
        gp.player.water = 1;
        gp.player.selectItem();
        check(gp.player.water == 4 && tonic.amount == 1, "+3 water");

        var folder = Files.createTempDirectory(Path.of("build"), "market-checkpoint-");
        gp.saveLoad = new SaveLoad(gp, folder.resolve("save-v1.dat"));
        place(Market.MAP, 28, 27);
        gp.keyH.enterPressed = true;
        gp.eHandler.checkEvent();
        check(gp.gameState == gp.dialogueState && Files.exists(gp.saveLoad.getSavePath()), "market emerald saves in play");
        check(gp.saveLoad.tryLoad(), gp.saveLoad.getLastError());
        check(gp.currentMap == Market.MAP && bag("Strong Potion").amount == 1 && bag("Water Tonic").amount == 1,
            "market and both consumable stacks survive reload");
    }
}
