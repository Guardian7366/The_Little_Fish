package tile;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import main.ExtraRooms;
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
        drawPreview(graphics,x,y,size,false);
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
            drawPreview(graphics,x,y,size,true);
        } finally {
            graphics.setComposite(previous);
        }
    }

    private void drawPreview(Graphics2D graphics,int x,int y,int size,boolean mini) {
        var room=gp.extraRooms.active();
        if (gp.currentMap==ExtraRooms.MAP && room==null) return;
        Rectangle source=room==null ? new Rectangle(0,0,gp.maxWorldCol,gp.maxWorldRow) : room.view();
        double scale=(double)size/Math.max(source.width,source.height);
        int width=(int)Math.round(source.width*scale),height=(int)Math.round(source.height*scale);
        x+=(size-width)/2;
        y+=(size-height)/2;
        int unit=PREVIEW_TILE_SIZE;
        Graphics2D view=(Graphics2D)graphics.create();
        try {
            view.clipRect(x,y,width,height);
            view.drawImage(getWorldMap(gp.currentMap),x,y,x+width,y+height,source.x*unit,source.y*unit,
                (source.x+source.width)*unit,(source.y+source.height)*unit,null);
            int playerX=x+(int)((gp.player.worldX/(double)gp.tileSize-source.x)*scale);
            int playerY=y+(int)((gp.player.worldY/(double)gp.tileSize-source.y)*scale);
            int markerSize=mini && room==null ? gp.tileSize/3 : (int)scale;
            int offset=mini && room==null ? 6 : 0;
            view.drawImage(gp.player.right,playerX-offset,playerY-offset,markerSize,markerSize,null);
        } finally {
            view.dispose();
        }
    }
}
