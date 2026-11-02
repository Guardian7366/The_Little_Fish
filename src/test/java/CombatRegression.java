import entity.Entity;
import entity.Projectile;
import main.GamePanel;
import object.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/** Behavioral contracts; run against either the original or reconstructed game. */
public final class CombatRegression {
    static final class QuietPanel extends GamePanel {
        @Override public void playSE(int index) { }
        @Override public void playMusic(int index) { }
        @Override public void stopMusic() { }
    }
    static QuietPanel gp;
    static int passed, failed;

    static void reset() {
        gp.player.setDefaultValues();
        gp.player.restoreStatus();
        gp.player.guardCounter = 0;
        gp.player.invincibleCounter = 0;
        gp.player.spriteCounter = 0;
        gp.player.shotAvailableCounter = 50;
        gp.player.attackCanceled = false;
        gp.currentMap = 0;
        gp.gameState = gp.playState;
        gp.keyH.upPressed = gp.keyH.downPressed = gp.keyH.leftPressed = gp.keyH.rightPressed = false;
        gp.keyH.enterPressed = gp.keyH.spacePressed = gp.keyH.shotKeyPressed = false;
        gp.keyH.godModeOn = false;
        gp.ui.playerSlotCol = gp.ui.playerSlotRow = 0;
        for (Entity[] row : gp.monster) Arrays.fill(row, null);
        for (Entity[] row : gp.npc) Arrays.fill(row, null);
        for (Entity[] row : gp.obj) Arrays.fill(row, null);
        for (Entity[] row : gp.projectile) Arrays.fill(row, null);
        for (Entity[] row : gp.iTile) Arrays.fill(row, null);
        gp.particleList.clear();
    }

    static void check(String name, Runnable body) {
        reset();
        try {
            body.run();
            passed++;
            System.out.println("PASS " + name);
        } catch (Throwable failure) {
            failed++;
            System.out.println("FAIL " + name + ": " + failure);
        }
    }
    static void equal(int expected, int actual, String label) {
        if (expected != actual) throw new AssertionError(label + " expected=" + expected + " actual=" + actual);
    }
    static void truth(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
    static void key(int code, boolean pressed) {
        KeyEvent event = new KeyEvent(gp, pressed ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED,
                System.currentTimeMillis(), 0, code, KeyEvent.CHAR_UNDEFINED);
        if (pressed) gp.keyH.keyPressed(event); else gp.keyH.keyReleased(event);
    }
    static Entity enemy(String direction) {
        Entity enemy = new Entity(gp);
        enemy.direction = direction;
        enemy.knockBackPower = 2;
        return enemy;
    }
    static Entity target(int defense) {
        Entity target = new Entity(gp);
        target.type = 2;
        target.life = 100;
        target.defense = defense;
        target.worldX = 20 * gp.tileSize;
        target.worldY = 20 * gp.tileSize;
        gp.monster[0][0] = target;
        return target;
    }
    static void select(Entity item) {
        gp.player.inventory.add(item);
        int slot = gp.player.inventory.size() - 1;
        gp.ui.playerSlotCol = slot % 5;
        gp.ui.playerSlotRow = slot / 5;
        gp.player.selectItem();
    }

    public static void main(String[] args) {
        gp = new QuietPanel();
        check("initial player stats and gear", () -> {
            equal(1, gp.player.level, "level");
            equal(6, gp.player.life, "life");
            equal(6, gp.player.water, "water");
            equal(2, gp.player.attack, "attack");
            equal(1, gp.player.defense, "defense");
            equal(2, gp.player.inventory.size(), "inventory");
        });
        check("guard release while idle", () -> {
            key(KeyEvent.VK_SPACE, true);
            gp.player.update();
            truth(gp.player.guarding, "held space guards");
            key(KeyEvent.VK_SPACE, false);
            for (int i = 0; i < 120; i++) gp.player.update();
            truth(!gp.player.guarding, "released space must stop guarding without movement");
            equal(0, gp.player.guardCounter, "released guard timer");
        });
        check("guard timer advances while held", () -> {
            key(KeyEvent.VK_SPACE, true);
            for (int i = 0; i < 12; i++) gp.player.update();
            truth(gp.player.guarding, "guard held");
            equal(12, gp.player.guardCounter, "guard ticks");
        });
        check("front parry prevents damage and staggers enemy", () -> {
            gp.player.direction = "up";
            gp.player.guarding = true;
            gp.player.guardCounter = 9;
            Entity enemy = enemy("down");
            enemy.damagePlayer(20);
            equal(6, gp.player.life, "life after parry");
            truth(enemy.offBalance && enemy.knockBack, "parry must stagger and push attacker");
        });
        check("parry window ends at tick ten", () -> {
            gp.player.direction = "up";
            gp.player.guarding = true;
            gp.player.guardCounter = 10;
            Entity enemy = enemy("down");
            enemy.damagePlayer(10);
            equal(3, gp.player.life, "(10 attack - 1 defense) / 3 blocked damage");
            truth(!enemy.offBalance, "late block must not parry");
        });
        check("high defense blocking cannot heal", () -> {
            gp.player.life = 2;
            gp.player.defense = 10;
            gp.player.direction = "up";
            gp.player.guarding = true;
            gp.player.guardCounter = 20;
            enemy("down").damagePlayer(2);
            equal(2, gp.player.life, "negative blocked damage clamps to zero");
        });
        check("rear attack bypasses guard", () -> {
            gp.player.direction = "up";
            gp.player.guarding = true;
            gp.player.guardCounter = 1;
            enemy("up").damagePlayer(4);
            equal(3, gp.player.life, "rear hit damage");
        });
        check("unguarded minimum damage remains one", () -> {
            gp.player.defense = 20;
            enemy("down").damagePlayer(2);
            equal(5, gp.player.life, "minimum contact damage");
        });
        check("invulnerability prevents repeated contact hits", () -> {
            Entity enemy = enemy("down");
            enemy.damagePlayer(3);
            enemy.damagePlayer(3);
            equal(4, gp.player.life, "only first hit applies");
        });
        check("melee respects enemy defense and stagger multiplier", () -> {
            Entity target = target(3);
            gp.player.damageMonster(0, gp.player, 5, 0);
            equal(98, target.life, "normal melee damage");
            target.invincible = false;
            target.offBalance = true;
            gp.player.damageMonster(0, gp.player, 5, 0);
            equal(81, target.life, "stagger multiplies attack by four before defense");
        });
        check("melee cannot heal armored enemies", () -> {
            Entity target = target(30);
            gp.player.damageMonster(0, gp.player, 5, 0);
            equal(100, target.life, "armored target life");
        });
        for (int level : new int[]{1, 2, 3, 4, 8}) {
            check("bubble damage at level " + level, () -> {
                Entity target = target(0);
                gp.player.level = level;
                Projectile bubble = gp.player.projectile;
                bubble.set(target.worldX, target.worldY, "right", true, gp.player);
                bubble.update();
                equal(bubble.attack * Math.max(1, level / 2), 100 - target.life, "projectile damage");
                truth(!bubble.alive, "bubble consumed on impact");
            });
        }
        check("shooting consumes water once", () -> {
            key(KeyEvent.VK_F, true);
            gp.player.update();
            equal(5, gp.player.water, "shot cost");
            truth(gp.player.projectile.alive, "shot spawned");
            gp.player.update();
            equal(5, gp.player.water, "no double shot while projectile alive");
        });
        check("zero water prevents shooting", () -> {
            gp.player.water = 0;
            key(KeyEvent.VK_F, true);
            gp.player.update();
            equal(0, gp.player.water, "empty water unchanged");
            truth(!gp.player.projectile.alive, "no free projectile");
        });
        check("full projectile pool cannot consume water", () -> {
            Arrays.fill(gp.projectile[0], new Entity(gp));
            key(KeyEvent.VK_F, true);
            gp.player.update();
            equal(6, gp.player.water, "failed shot must keep water");
            truth(!gp.player.projectile.alive, "failed shot must not leave an invisible projectile");
        });
        check("single level threshold preserves stat gains", () -> {
            gp.player.exp = 5;
            gp.player.checkLevelUp();
            equal(2, gp.player.level, "level");
            equal(10, gp.player.nextLevelExp, "next threshold");
            equal(8, gp.player.maxLife, "max life");
            equal(2, gp.player.strength, "strength");
            equal(2, gp.player.dexterity, "dexterity");
            equal(4, gp.player.attack, "attack");
            equal(2, gp.player.defense, "defense");
            equal(5, gp.player.exp, "cumulative experience retained");
        });
        check("large XP reward crosses every reached threshold", () -> {
            gp.player.exp = 100;
            gp.player.checkLevelUp();
            equal(6, gp.player.level, "level");
            equal(160, gp.player.nextLevelExp, "next threshold");
            equal(16, gp.player.maxLife, "max life");
            equal(6, gp.player.strength, "strength");
            equal(6, gp.player.dexterity, "dexterity");
            equal(100, gp.player.exp, "cumulative experience retained");
        });
        check("weapon and shield selection recomputes stats", () -> {
            Entity weapon = new OBJ_Second_Weapon(gp);
            gp.player.strength = 3;
            select(weapon);
            truth(gp.player.currentWeapon == weapon, "selected weapon equipped");
            equal(3 * weapon.attackValue, gp.player.attack, "weapon attack");
            Entity shield = new OBJ_Super_Shield(gp);
            gp.player.dexterity = 4;
            select(shield);
            truth(gp.player.currentShield == shield, "selected shield equipped");
            equal(4 * shield.defenseValue, gp.player.defense, "shield defense");
        });
        check("lantern toggles without being consumed", () -> {
            Entity lantern = new OBJ_Lantern(gp);
            select(lantern);
            truth(gp.player.currentLight == lantern, "light equipped");
            gp.player.selectItem();
            truth(gp.player.currentLight == null, "light unequipped");
            truth(gp.player.inventory.contains(lantern), "lantern retained");
        });
        check("new game clears equipment from previous inventory", () -> {
            select(new OBJ_Lantern(gp));
            gp.player.setDefaultValues();
            truth(gp.player.currentLight == null, "new game must not inherit an unequipped lantern");
        });
        check("stackable item can be collected in full inventory", () -> {
            Entity potion = new OBJ_HealHP(gp);
            truth(gp.player.canObtainItem(potion), "first potion accepted");
            while (gp.player.inventory.size() < 20) gp.player.inventory.add(new OBJ_Weapon_Normal(gp));
            int slot = gp.player.searchItemInInventory(potion.name);
            int amount = gp.player.inventory.get(slot).amount;
            truth(gp.player.canObtainItem(potion), "stacking allowed at capacity");
            equal(amount + 1, gp.player.inventory.get(slot).amount, "stack grows");
            truth(!gp.player.canObtainItem(new OBJ_Weapon_Normal(gp)), "new slot rejected at capacity");
            equal(20, gp.player.inventory.size(), "inventory limit");
        });
        check("full inventory leaves rejected pickup in world", () -> {
            while (gp.player.inventory.size() < 20) gp.player.inventory.add(new OBJ_Weapon_Normal(gp));
            Entity loot = new OBJ_Lantern(gp);
            gp.obj[0][0] = loot;
            gp.player.pickUpObject(0);
            truth(gp.obj[0][0] == loot, "rejected item must stay available");
            gp.player.inventory.remove(19);
            gp.player.pickUpObject(0);
            truth(gp.obj[0][0] == null, "accepted item removed from world");
            equal(20, gp.player.inventory.size(), "one item obtained after freeing a slot");
        });
        check("chest loot can only be obtained once", () -> {
            OBJ_Chest chest = new OBJ_Chest(gp);
            chest.setLoot(new OBJ_Lantern(gp));
            chest.interact();
            truth(chest.opened, "chest opens");
            equal(3, gp.player.inventory.size(), "loot received");
            chest.interact();
            equal(3, gp.player.inventory.size(), "no duplicate loot");
        });
        check("sleeping restores health and water", () -> {
            gp.player.life = 1;
            gp.player.water = 0;
            new OBJ_Fishbowl(gp).use(gp.player);
            equal(gp.player.maxLife, gp.player.life, "sleep heals");
            equal(gp.player.maxWater, gp.player.water, "sleep restores water");
            equal(gp.sleepState, gp.gameState, "sleep sequence starts");
        });
        check("destructible obstacles still require their own tools", () -> {
            var rock = new tile_interactive.IT_Rock(gp, 20, 20);
            var wall = new tile_interactive.IT_DestructibleWall(gp, 20, 20);
            truth(!rock.isCorrectItem(gp.player) && !wall.isCorrectItem(gp.player), "normal weapon cannot break obstacles");
            select(new OBJ_Rock(gp));
            truth(rock.isCorrectItem(gp.player) && !wall.isCorrectItem(gp.player), "Rocky breaks rock only");
            select(new OBJ_Iron(gp));
            truth(!rock.isCorrectItem(gp.player) && wall.isCorrectItem(gp.player), "Iron breaks wall only");
        });
        for (int code : new int[]{KeyEvent.VK_P, KeyEvent.VK_Y}) {
            check("pause and resume using " + KeyEvent.getKeyText(code), () -> {
                key(code, true);
                equal(gp.pauseState, gp.gameState, "pause");
                key(code, false);
                key(code, true);
                equal(gp.playState, gp.gameState, "resume");
            });
        }
        check("inventory and map shortcuts preserved", () -> {
            key(KeyEvent.VK_C, true);
            equal(gp.characterState, gp.gameState, "inventory opens");
            key(KeyEvent.VK_C, false);
            key(KeyEvent.VK_C, true);
            equal(gp.playState, gp.gameState, "inventory closes");
            key(KeyEvent.VK_M, true);
            equal(gp.mapState, gp.gameState, "map opens");
            key(KeyEvent.VK_M, false);
            key(KeyEvent.VK_M, true);
            equal(gp.playState, gp.gameState, "map closes");
        });
        check("retry starts in a valid original area", () -> {
            gp.currentArea = gp.dungeons;
            gp.player.setDefaultPositions();
            equal(gp.outside, gp.currentArea, "initial area");
            equal(13 * gp.tileSize, gp.player.worldX, "initial X");
            equal(31 * gp.tileSize, gp.player.worldY, "initial Y");
        });
        check("drawing cannot advance a death or health bar timer", () -> {
            Entity monster = new monster.MON_Normalillo(gp);
            monster.worldX = gp.player.worldX;
            monster.worldY = gp.player.worldY;
            monster.dying = true;
            monster.hpBarOn = true;
            monster.hpBarCounter = 12;
            BufferedImage screen = new BufferedImage(960, 576, BufferedImage.TYPE_INT_ARGB);
            var graphics = screen.createGraphics();
            try {
                for (int i = 0; i < 120; i++) monster.draw(graphics);
            } finally { graphics.dispose(); }
            truth(monster.alive, "rendering must not finish death");
            equal(12, monster.hpBarCounter, "rendering must not advance HP bar");
            for (int i = 0; i < 40; i++) monster.updateVisualState();
            truth(monster.alive, "original death flashes last forty ticks");
            monster.updateVisualState();
            truth(!monster.alive, "simulation finishes death at tick forty-one");
        });
        check("offscreen death still finishes in simulation", () -> {
            Entity monster = new monster.MON_Normalillo(gp);
            monster.worldX = 49 * gp.tileSize;
            monster.worldY = 49 * gp.tileSize;
            monster.dying = true;
            truth(!monster.inCamera(), "fixture is offscreen");
            for (int i = 0; i < 41; i++) monster.updateVisualState();
            truth(!monster.alive, "offscreen death cannot remain stuck");
        });
        check("health bar timing survives repeated rendering", () -> {
            Entity monster = new monster.MON_Normalillo(gp);
            monster.invincible = true;
            monster.updateVisualState();
            truth(monster.hpBarOn, "hit reveals health bar");
            equal(1, monster.hpBarCounter, "fresh bar age");
            monster.invincible = false;
            for (int i = 0; i < 359; i++) monster.updateVisualState();
            truth(monster.hpBarOn, "bar stays through tick 360");
            monster.updateVisualState();
            truth(!monster.hpBarOn, "bar expires at 361");
        });
        check("XP saturation cannot grant infinite repeated levels", () -> {
            gp.player.exp = Integer.MAX_VALUE;
            gp.player.checkLevelUp();
            int level = gp.player.level;
            gp.player.checkLevelUp();
            equal(level, gp.player.level, "saturated XP cannot repeat a threshold");
        });
        System.out.printf("RESULT passed=%d failed=%d%n", passed, failed);
        if (failed > 0) System.exit(1);
    }
}
