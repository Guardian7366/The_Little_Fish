/*
 * Decompiled with CFR 0.152.
 */
package monster;

import entity.Entity;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import main.GamePanel;
import object.OBJ_Heart;

public class MON_Tella
extends Entity {
    GamePanel gp;

    public enum Phase { ARMORED, WARNING, PULSE, CORE }

    public static final int ABYSS_MAP = 8;
    public static final int ABYSS_MAX_LIFE = 45;
    private static final int WARNING_TICKS = 45;
    private static final int PULSE_TICKS = 10;
    private static final int CORE_TICKS = 60;
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final String[] SPRITES = {"blindado", "carga", "nucleo"};

    private BufferedImage[][] abyssSprites;
    private Rectangle sector;
    private int homeX, originX, originY, ticks;
    private boolean horizontal, hit;
    private Phase phase = Phase.ARMORED;
    private List<Rectangle> bands = List.of();

    public boolean usesPulse() { return gp.currentMap == ABYSS_MAP; }
    public Phase phase() { return phase; }
    public List<Rectangle> bands() { return bands.stream().map(Rectangle::new).toList(); }

    public void configureAbyss(int spawnX, Rectangle sector) {
        this.homeX = spawnX;
        this.sector = new Rectangle(sector);
        maxLife = ABYSS_MAX_LIFE;
        life = Math.min(life, maxLife);
        defaultSpeed = 1;
        name = "Fishtella";
        if (abyssSprites == null) {
            abyssSprites = new BufferedImage[3][4];
            for (int state = 0; state < SPRITES.length; state++)
                for (int facing = 0; facing < DIRECTIONS.length; facing++)
                    abyssSprites[state][facing] = setup("/sexta/fishtella/" + SPRITES[state] + "-" + DIRECTIONS[facing], 48, 48);
        }
        resetPulse();
    }

    public void resetPulse() {
        enter(Phase.ARMORED);
        horizontal = true;
        finishKnockBack();
    }

    private void enter(Phase next) {
        phase = next;
        ticks = 0;
        defense = next == Phase.CORE ? 2 : 10;
        if (next == Phase.ARMORED || next == Phase.CORE) bands = List.of();
    }

    @Override
    public void update() {
        if (!usesPulse()) {
            super.update();
            return;
        }
        if (gp.gameState != gp.playState || sleep || !alive || dying || life <= 0) return;
        if (sector == null) configureAbyss(worldX, new Rectangle(worldX - 192, worldY - 192, 432, 432));
        updateCombatTimers();
        if (knockBack) finishKnockBack();
        if ((phase == Phase.WARNING || phase == Phase.PULSE) && (worldX != originX || worldY != originY)) {
            enter(Phase.ARMORED);
            return;
        }
        ticks++;
        switch (phase) {
            case ARMORED -> {
                if (ticks >= 45 && canSeePlayer()) {
                    facePlayer();
                    originX = worldX;
                    originY = worldY;
                    bands = traceBands();
                    enter(Phase.WARNING);
                } else {
                    patrol();
                }
            }
            case WARNING -> {
                if (ticks >= WARNING_TICKS) {
                    hit = false;
                    enter(Phase.PULSE);
                    updatePulse();
                }
            }
            case PULSE -> {
                if (ticks >= PULSE_TICKS) enter(Phase.CORE);
                else updatePulse();
            }
            case CORE -> {
                if (ticks >= CORE_TICKS) {
                    horizontal = !horizontal;
                    enter(Phase.ARMORED);
                }
            }
        }
    }

    private void patrol() {
        int targetX = homeX + (ticks / 24 % 2 == 0 ? 12 : 0);
        if (targetX == worldX) return;
        direction = targetX > worldX ? "right" : "left";
        if (!sector.contains(worldX + (targetX > worldX ? 1 : -1), worldY)) return;
        checkCollision();
        if (!collisionOn) moveInDirection(direction);
    }

    private boolean canSeePlayer() {
        if (!sector.contains(gp.player.worldX, gp.player.worldY) ||
            getScreenX() < 0 || getScreenY() < 0 ||
            getScreenX() + 48 > gp.screenWidht || getScreenY() + 48 > gp.screenHeight) return false;
        int x = worldX + 22, y = worldY + 22;
        Rectangle player = bounds(gp.player);
        int dx = (int)player.getCenterX() - x, dy = (int)player.getCenterY() - y;
        if ((long)dx * dx + (long)dy * dy > 240L * 240) return false;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        for (int step = 1; step <= steps; step++) {
            int col = Math.floorDiv(x + dx * step / steps, 48);
            int row = Math.floorDiv(y + dy * step / steps, 48);
            if (blocked(col, row)) return false;
        }
        return true;
    }

    private static Rectangle bounds(Entity actor) {
        return new Rectangle(actor.worldX + actor.solidAreaDefaultX,
            actor.worldY + actor.solidAreaDefaultY, actor.solidArea.width, actor.solidArea.height);
    }

    private boolean blocked(int col, int row) {
        if (col < 0 || row < 0 || col >= gp.maxWorldCol || row >= gp.maxWorldRow ||
            gp.tileM.tile[gp.tileM.mapTileNum[gp.currentMap][col][row]].collision) return true;
        Rectangle cell = new Rectangle(col * 48, row * 48, 48, 48);
        for (var tile : gp.iTile[gp.currentMap])
            if (tile != null && (tile.destructible || tile.collision) && cell.intersects(bounds(tile))) return true;
        for (Entity object : gp.obj[gp.currentMap])
            if (object != null && object.collision && cell.intersects(bounds(object))) return true;
        for (Entity npc : gp.npc[gp.currentMap])
            if (npc != null && npc.collision && cell.intersects(bounds(npc))) return true;
        return false;
    }

    private List<Rectangle> traceBands() {
        List<Rectangle> result = new ArrayList<>(8);
        int col = (originX + 22) / 48, row = (originY + 22) / 48;
        for (int sign : new int[]{-1, 1}) {
            for (int step = 1; step <= 4; step++) {
                int x = col + (horizontal ? sign * step : 0);
                int y = row + (horizontal ? 0 : sign * step);
                if (blocked(x, y)) break;
                result.add(new Rectangle(x * 48, y * 48, 48, 48));
            }
        }
        return List.copyOf(result);
    }

    private void updatePulse() {
        List<Rectangle> clear = traceBands();
        bands = bands.stream().filter(clear::contains).toList();
        if (hit) return;
        Rectangle player = bounds(gp.player);
        for (Rectangle band : bands) {
            if (!band.intersects(player)) continue;
            hit = true;
            String fromOrigin = horizontal
                ? (band.x < originX ? "left" : "right")
                : (band.y < originY ? "up" : "down");
            damagePlayer(attack, fromOrigin, false);
            break;
        }
    }

    @Override
    public void damagePlayer(int damage) {
        if (!usesPulse()) super.damagePlayer(damage);
    }

    @Override
    public void updateVisualState() {
        if (usesPulse()) {
            if (gp.gameState != gp.playState) return;
            if (dying || !alive) enter(Phase.ARMORED);
        }
        super.updateVisualState();
    }

    @Override
    public void draw(Graphics2D g) {
        if (!usesPulse() || abyssSprites == null) {
            super.draw(g);
            return;
        }
        if (!inCamera()) return;
        int facing = List.of(DIRECTIONS).indexOf(direction);
        int image = switch (phase) {
            case ARMORED -> 0;
            case WARNING, PULSE -> 1;
            case CORE -> 2;
        };
        Graphics2D copy = (Graphics2D)g.create();
        if (invincible) changeAlpha(copy, 0.4f);
        if (dying) dyingAnimation(copy);
        copy.drawImage(abyssSprites[image][Math.max(0, facing)], getScreenX(), getScreenY(), null);
        copy.dispose();
    }

    public void drawBands(Graphics2D g) {
        if (!usesPulse() || !alive || dying || bands.isEmpty() || gp.gameState == gp.transitionState) return;
        Graphics2D copy = (Graphics2D)g.create();
        boolean warning = phase == Phase.WARNING;
        copy.setStroke(warning ? new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{6, 4}, 0)
            : new BasicStroke(2));
        int cameraX = gp.player.worldX - gp.player.screenX, cameraY = gp.player.worldY - gp.player.screenY;
        for (Rectangle band : bands) {
            int x = band.x - cameraX, y = band.y - cameraY;
            if (!warning) {
                copy.setColor(new Color(83, 218, 245, 75));
                copy.fillRect(x, y, band.width, band.height);
            }
            copy.setColor(warning ? new Color(255, 218, 113) : new Color(165, 250, 255));
            copy.drawRect(x + 1, y + 1, band.width - 3, band.height - 3);
            if (!warning) {
                if (horizontal) copy.drawLine(x, y + 24, x + 47, y + 24);
                else copy.drawLine(x + 24, y, x + 24, y + 47);
            }
        }
        copy.dispose();
    }


    public MON_Tella(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 2;
        this.name = "Fhisty";
        this.speed = this.defaultSpeed = 10;
        this.life = this.maxLife = 100;
        this.attack = 10;
        this.defense = 10;
        this.exp = 30;
        this.solidArea.x = 0;
        this.solidArea.y = 0;
        this.solidArea.width = 44;
        this.solidArea.height = 44;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
        this.getImage();
    }

    public void getImage() {
        this.up = this.setup("/monster/Fishtella", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/monster/Fishtella2", this.gp.tileSize, this.gp.tileSize);
        this.left = this.setup("/monster/Fishtella", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/monster/Fishtella2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/monster/Fishtella", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/monster/Fishtella2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/monster/Fishtella", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/monster/Fishtella2", this.gp.tileSize, this.gp.tileSize);
    }

    @Override
    public void setAction() {
        if (!this.onPath) {
            this.getRandomDirection(60);
        }
    }

    @Override
    public void damageReaction() {
        this.actionLockCounter = 0;
    }

    @Override
    public void checkDrop() {
        int i = new Random().nextInt(50) + 1;
        if (i < 50) {
            this.dropItem(new OBJ_Heart(this.gp));
        }
    }
}

