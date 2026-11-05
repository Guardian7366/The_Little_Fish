package main;

import entity.NPC_TV;
import data.Progress;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public final class Market {
    public static final int MAP = 10, FIRST_TILE = 174, TV_SLOT = 16;
    private static final int ARENA = 6, ARENA_EXIT_COL = 23, ARENA_EXIT_ROW = 44;
    private final GamePanel gp;
    private final BufferedImage background = image("background/market");
    private final BufferedImage canopy = image("props/canopy"), shelf = image("props/shelf");
    private final BufferedImage lamp = image("props/lamp"), crate = image("props/crate");
    private final BufferedImage counter = image("props/counter"), sign = image("props/shop-sign");

    Market(GamePanel gp) { this.gp = gp; }

    public static BufferedImage image(String name) {
        try (var stream = Market.class.getResourceAsStream("/mercado/" + name + ".png")) {
            if (stream == null) throw new IllegalStateException("Missing market image: " + name);
            BufferedImage image = ImageIO.read(stream);
            if (image == null) throw new IllegalStateException("Invalid market image: " + name);
            return image;
        } catch (IOException error) {
            throw new IllegalStateException("Cannot load market image: " + name, error);
        }
    }

    public static NPC_TV placeTV(NPC_TV tv, int tileSize) {
        tv.worldX = 22 * tileSize;
        tv.worldY = 23 * tileSize;
        tv.direction = "down";
        return tv;
    }

    private boolean arenaReturnAvailable() {
        return gp.currentMap == ARENA && !gp.bossBattleOn && !Progress.GodFishDefeated
                && gp.csManager.sceneNum == gp.csManager.NA;
    }

    private boolean at(int col, int row) {
        return (gp.player.worldX + gp.tileSize / 2) / gp.tileSize == col
                && (gp.player.worldY + gp.tileSize / 2) / gp.tileSize == row;
    }

    boolean interact() {
        if (gp.gameState != gp.playState || !gp.keyH.enterPressed) return false;
        if (gp.currentMap == MAP && at(22, 25) && "up".equals(gp.player.direction)
                && gp.npc[MAP][TV_SLOT] instanceof NPC_TV tv) {
            gp.player.attackCanceled = true;
            tv.speak();
            return true;
        }
        if (arenaReturnAvailable() && at(ARENA_EXIT_COL, ARENA_EXIT_ROW)) {
            gp.player.attackCanceled = true;
            gp.eHandler.teleportMap(MAP, 24, 21, gp.indoor);
            return true;
        }
        return false;
    }

    int cameraX() { return 24 * gp.tileSize - gp.maxScreenCol * gp.tileSize / 2; }
    int cameraY() { return 24 * gp.tileSize + gp.tileSize / 2 - gp.maxScreenRow * gp.tileSize / 2; }

    void drawBackground(Graphics2D g) { draw(g, background, 0, 0); }

    void drawStalls(Graphics2D g) {
        prop(g, canopy, 21, 21);
        prop(g, shelf, 18, 22);
        prop(g, crate, 19, 24);
        prop(g, shelf, 27, 22);
        prop(g, crate, 28, 24);
        prop(g, lamp, 23, 20);
        prop(g, lamp, 25, 20);
        prop(g, lamp, 23, 28);
        prop(g, lamp, 25, 28);
        prop(g, sign, 19, 25);
    }

    void drawCounter(Graphics2D graphics) {
        prop(graphics, counter, 21, 24);
        Graphics2D g = (Graphics2D) graphics.create();
        glow(g, 22, 24, 105, 44);
        glow(g, 24, 19, 72, 35);
        glow(g, 24, 29, 72, 35);
        g.dispose();
    }

    private void glow(Graphics2D g, int col, int row, int radius, int alpha) {
        int x = col * gp.tileSize + gp.tileSize / 2 - gp.cameraX();
        int y = row * gp.tileSize + gp.tileSize / 2 - gp.cameraY();
        g.setPaint(new RadialGradientPaint(x, y, radius, new float[]{0f, 1f},
            new Color[]{new Color(255, 193, 110, alpha), new Color(255, 193, 110, 0)}));
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    void drawPrompt(Graphics2D graphics) {
        boolean shopPrompt = gp.currentMap == MAP && at(22, 25);
        boolean savePrompt = gp.currentMap == MAP && at(28, 27);
        boolean returnPrompt = arenaReturnAvailable();
        if (gp.gameState != gp.playState || !(shopPrompt || savePrompt || returnPrompt)) return;
        Graphics2D g = (Graphics2D) graphics.create();
        if (returnPrompt) prop(g, sign, ARENA_EXIT_COL, ARENA_EXIT_ROW - 1);
        if (shopPrompt || savePrompt || at(ARENA_EXIT_COL, ARENA_EXIT_ROW)) {
            String message = shopPrompt ? "Face the counter and press Enter"
                : savePrompt ? "Enter - Rest and save" : "Enter - Return to TV Supply";
            g.setFont(gp.ui.purisaB.deriveFont(Font.BOLD, 18f));
            int width = g.getFontMetrics().stringWidth(message) + 24;
            int x = (gp.maxScreenCol * gp.tileSize - width) / 2;
            g.setColor(new Color(12, 22, 28, 230));
            g.fillRoundRect(x, 480, width, 32, 10, 10);
            g.setColor(new Color(255, 226, 164));
            g.drawString(message, x + 12, 502);
        }
        g.dispose();
    }

    private void prop(Graphics2D g, BufferedImage image, int col, int row) {
        draw(g, image, col * gp.tileSize - gp.cameraX(), row * gp.tileSize - gp.cameraY());
    }

    private static void draw(Graphics2D graphics, BufferedImage image, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(image, x, y, image.getWidth() * 3, image.getHeight() * 3, null);
        g.dispose();
    }
}
