package environment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import main.GamePanel;

/** Pixel geometry drawn once, then reused. No I/O, randomness or simulation in draw(). */
public final class OceanLayers {
    private static final int WIDTH = 384, HEIGHT = 256, SCALE = 3;
    private final BufferedImage[] deep = new BufferedImage[5];
    private final BufferedImage[] reef = new BufferedImage[5];
    private final BufferedImage abyss = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    private final BufferedImage abyssRidges = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
    private int tick;
    private int deepTick;
    public final FifthAbyss fifth=new FifthAbyss();
    private final BufferedImage[] templeLayers = {
        main.SpriteCache.load("/septima/background/deep-naves.png",1280,768),
        main.SpriteCache.load("/septima/background/arcades.png",1280,768),
        main.SpriteCache.load("/septima/background/statues.png",1280,768)
    };

    public OceanLayers() {
        Graphics2D a=abyss.createGraphics();
        a.setColor(new Color(7,14,36));a.fillRect(0,0,WIDTH,HEIGHT);
        for(int y=0;y<HEIGHT;y+=16){a.setColor(new Color(9,18+(HEIGHT-y)/40,42+(HEIGHT-y)/20));a.fillRect(0,y,WIDTH,16);}
        // Distant vertical rock strata, with broad dark gaps between them.
        for(int i=0;i<7;i++){
            int x=i*59-12,y=40+(i*47)%130;
            a.setColor(new Color(12,25,52));a.fillRect(x,y,28,HEIGHT-y);
            a.setColor(new Color(15,30,59));a.fillRect(x+4,y+9,4,HEIGHT-y-9);
            a.setColor(new Color(8,19,42));a.fillRect(x+17,y+27,13,HEIGHT-y-27);
        }
        for(int i=0;i<64;i++){a.setColor(new Color(24,42,69));a.fillRect(i*71%WIDTH,i*43%HEIGHT,1,1);}
        a.dispose();a=abyssRidges.createGraphics();
        for(int i=0;i<6;i++){
            int x=i*67,y=100+(i*37)%120;
            a.setColor(new Color(10,22,47,180));a.fillRect(x,y,40,HEIGHT-y);a.fillRect(x+8,y-8,23,8);
            // Sparse cold bioluminescent points on the distant rock face.
            a.setColor(new Color(37,75,103,150));a.fillRect(x+11,y+7,1,2);a.fillRect(x+25,y+21,2,1);
        }
        a.dispose();
        for (int frame = 0; frame < 5; frame++) {
            deep[frame] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            reef[frame] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = deep[frame].createGraphics();
            g.setColor(new Color(16, 41, 64)); g.fillRect(0, 0, WIDTH, HEIGHT);
            // Distant ledges and broken shafts of light, always below the playable reef.
            for (int i = 0; i < 12; i++) {
                int x = (i * 97 + 17) % WIDTH, y = (i * 59 + 23) % HEIGHT;
                g.setColor(new Color(20, 49, 73)); g.fillRect(x, y, 50, 3);
                g.fillRect(x + 9, y + 3, 36, 3);
                g.setColor(new Color(18, 45, 69)); g.fillRect(x - 5, y + 6, 64, 6);
            }
            for (int i = 0; i < 90; i++) {
                int x = Math.floorMod(i * 83 + frame, WIDTH);
                int y = Math.floorMod(i * 47 - frame, HEIGHT);
                g.setColor(i % 4 == 0 ? new Color(38, 69, 89) : new Color(25, 55, 78));
                g.fillRect(x, y, 1, 1);
            }
            g.dispose();
            g = reef[frame].createGraphics();
            int sway = new int[]{0, 1, 1, 0, -1}[frame];
            for (int i = 0; i < 9; i++) {
                int x = (i * 103 + 29) % WIDTH, y = (i * 67 + 50) % HEIGHT;
                g.setColor(new Color(22, 58, 76));
                g.fillRect(x - 13, y + 17, 30, 5);
                g.fillRect(x - 9, y + 12, 21, 5);
                g.setColor(new Color(27, 70, 83));
                for (int stem = -1; stem <= 1; stem++) {
                    int height = 12 + (i + stem + 9) % 4 * 4;
                    for (int h = 0; h < height; h += 3) {
                        int bend = h > height / 2 ? sway : 0;
                        int sx = x + stem * 6 + bend;
                        g.fillRect(sx, y + 13 - h, 2, 4);
                        if (h % 6 == 0) g.fillRect(sx - 2, y + 13 - h, 6, 1);
                    }
                }
            }
            g.dispose();
        }
    }

    public void update() { tick = (tick + 1) % 150; deepTick=(deepTick+1)%3600; }
    public int frameIndex() { return tick / 30; }
    public long cachedPixelBytes() { return 12L * WIDTH * HEIGHT * 4; }

    public void drawBackground(Graphics2D target, GamePanel gp) {
        if (main.Temple.usesTemple(gp.currentMap)) {
            for (int layer=0; layer<templeLayers.length; layer++) {
                int x=-80-gp.player.worldX*(layer+1)/55, y=-48-gp.player.worldY*(layer+1)/55;
                target.drawImage(templeLayers[layer],x,y,null);
            }
            return;
        }
        if(main.SixthZone.usesAbyss(gp.currentMap)) {fifth.draw(target,gp);return;}
        if (gp.currentMap != 0 && gp.currentMap != 1 && gp.currentMap != 2 && gp.currentMap != main.FourthZone.MAP) return;
        Graphics2D g = (Graphics2D) target.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int cameraX = gp.player.worldX - gp.player.screenX;
        int cameraY = gp.player.worldY - gp.player.screenY;
        if(gp.currentMap==2 || gp.currentMap==main.FourthZone.MAP) {
            repeat(g, abyss, cameraX/8, cameraY/8, gp);
            repeat(g, abyssRidges, cameraX/4, cameraY/4, gp);
        } else {
            repeat(g, deep[frameIndex()], cameraX / 6, cameraY / 6, gp);
            repeat(g, reef[frameIndex()], cameraX / 3, cameraY / 3, gp);
        }
        if (gp.currentMap == 1) { g.setColor(new Color(6, 34, 51, 28)); g.fillRect(0, 0, gp.screenWidht, gp.screenHeight); }
        if(gp.currentMap==2 || gp.currentMap==main.FourthZone.MAP) {

            g.setColor(new Color(19,37,65));
            for(int i=0;i<3;i++) {
                int x=Math.floorMod(i*433+deepTick/3-cameraX/8,1200)-120;
                int y=80+i*160-cameraY/12;
                g.fillOval(x,y,81,24);g.fillPolygon(new int[]{x+66,x+99,x+99},new int[]{y+12,y,y+24},3);
            }
        }
        g.dispose();
    }

    private void repeat(Graphics2D g, BufferedImage frame, int x, int y, GamePanel gp) {
        int width = WIDTH * SCALE, height = HEIGHT * SCALE;
        int offsetX = -Math.floorMod(x / SCALE * SCALE, width);
        int offsetY = -Math.floorMod(y / SCALE * SCALE, height);
        for (int sy = offsetY; sy < gp.screenHeight; sy += height)
            for (int sx = offsetX; sx < gp.screenWidht; sx += width)
                g.drawImage(frame, sx, sy, width, height, null);
    }

    /** Sparse near-water glints over the outer ocean only, never over actors or UI. */
    public void drawForeground(Graphics2D target, GamePanel gp) {
        if (gp.currentMap != 0 && gp.currentMap != 1 && gp.currentMap != 2 && gp.currentMap != main.FourthZone.MAP) return;
        Graphics2D g = (Graphics2D) target.create();
        g.setColor(new Color(68, 120, 137, 150));
        for (int i = 0; i < 18; i++) {
            int wx = (7 + (i * 11) % 36) * gp.tileSize + 12;
            int wy = (15 + (i * 7) % 28) * gp.tileSize + 12 - frameIndex() * 3;
            if (gp.tileM.mapTileNum[gp.currentMap][wx / gp.tileSize][wy / gp.tileSize] != 0) continue;
            int x = wx - gp.player.worldX + gp.player.screenX;
            int y = wy - gp.player.worldY + gp.player.screenY;
            g.fillRect(x, y, 3, 3);
        }
        g.dispose();
    }
}
