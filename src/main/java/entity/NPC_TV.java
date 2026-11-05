package entity;

import main.GamePanel;
import main.Market;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class NPC_TV extends Entity {
    private static final BufferedImage DIM = Market.image("tv/idle-dim");
    private static final BufferedImage LIT = Market.image("tv/idle-lit");
    private static final BufferedImage BLINK = Market.image("tv/blink");
    private static final BufferedImage SALE = Market.image("tv/sale");
    private int frame;

    public NPC_TV(GamePanel gp) {
        super(gp);
        direction = "down";
        down = up = left = right = DIM;
        down2 = up2 = left2 = right2 = LIT;
    }

    @Override public void update() { frame = (frame + 1) % 160; }

    public static BufferedImage portrait(int frame, boolean sale) {
        if (sale) return SALE;
        if (frame >= 150) return BLINK;
        return frame < 72 ? DIM : LIT;
    }

    @Override public void draw(Graphics2D g) {
        g.drawImage(portrait(frame, false), getScreenX() - 24, getScreenY() - 42, 96, 96, null);
    }

    @Override public void speak() {
        if (gp.currentMap == Market.MAP && gp.gameState == gp.playState) gp.shop.open();
    }
}
