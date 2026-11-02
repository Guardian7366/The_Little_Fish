package tile;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import main.GamePanel;

/** Lazily builds native-pixel map previews from the shared terrain manager. */
public class Map {
    private static final int PREVIEW_TILE_SIZE = 16;
    private final GamePanel gp;
    private BufferedImage[] worldMap;
    private int[] renderedRevision;
    public boolean miniMapOn = false;

    public Map(GamePanel gp) {
        this.gp = gp;
        createWorldMap();
    }

    /** Invalidates previews without allocating large unused bitmaps. */
    public void createWorldMap() {
        worldMap = new BufferedImage[gp.maxMap];
        renderedRevision = new int[gp.maxMap];
        Arrays.fill(renderedRevision, -1);
    }

    public BufferedImage getWorldMap(int map) {
        if (!gp.tileM.isMapLoaded(map)) return null;
        int revision = gp.tileM.getMapRevision(map);
        if (worldMap[map] == null || renderedRevision[map] != revision) {
            BufferedImage preview = new BufferedImage(gp.maxWorldCol * PREVIEW_TILE_SIZE,
                    gp.maxWorldRow * PREVIEW_TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = preview.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                for (int row = 0; row < gp.maxWorldRow; row++) {
                    for (int col = 0; col < gp.maxWorldCol; col++) {
                        int tileNumber = gp.tileM.mapTileNum[map][col][row];
                        graphics.drawImage(gp.tileM.tile[tileNumber].image,
                                col * PREVIEW_TILE_SIZE, row * PREVIEW_TILE_SIZE,
                                PREVIEW_TILE_SIZE, PREVIEW_TILE_SIZE, null);
                    }
                }
            } finally {
                graphics.dispose();
            }
            worldMap[map] = preview;
            renderedRevision[map] = revision;
        }
        return worldMap[map];
    }

    public long cachedPixelBytes() {
        long bytes = 0;
        for (BufferedImage image : worldMap) {
            if (image != null) bytes += (long) image.getWidth() * image.getHeight() * 4;
        }
        return bytes;
    }

    public void drawFullMapScreen(Graphics2D graphics) {
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, gp.screenWidht, gp.screenHeight);
        int size = 500;
        int x = gp.screenWidht / 2 - size / 2;
        int y = gp.screenHeight / 2 - size / 2;
        graphics.drawImage(getWorldMap(gp.currentMap), x, y, size, size, null);
        double scale = (double) (gp.tileSize * gp.maxWorldCol) / size;
        int playerX = (int) (x + gp.player.worldX / scale);
        int playerY = (int) (y + gp.player.worldY / scale);
        int playerSize = (int) (gp.tileSize / scale);
        graphics.drawImage(gp.player.right, playerX, playerY, playerSize, playerSize, null);
        graphics.setFont(gp.ui.purisaB.deriveFont(12.0f));
        graphics.setColor(Color.white);
        graphics.drawString("Press M to close, D...", 750, 550);
    }

    public void drawMiniMap(Graphics2D graphics) {
        if (!miniMapOn) return;
        int size = 200;
        int x = gp.screenWidht - size - 50;
        int y = 50;
        Composite previous = graphics.getComposite();
        try {
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
            graphics.drawImage(getWorldMap(gp.currentMap), x, y, size, size, null);
            double scale = (double) (gp.tileSize * gp.maxWorldCol) / size;
            int playerX = (int) (x + gp.player.worldX / scale);
            int playerY = (int) (y + gp.player.worldY / scale);
            int playerSize = gp.tileSize / 3;
            graphics.drawImage(gp.player.right, playerX - 6, playerY - 6, playerSize, playerSize, null);
        } finally {
            graphics.setComposite(previous);
        }
    }
}
