package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/** Non-modal key reminders. The map teaches the situations; no forced dialogue. */
public final class FirstZoneGuide {
    private boolean moved, attacked, guarded, fired;
    private int coverHintTicks;
    private final GamePanel gp;
    private final Font font;

    public FirstZoneGuide(GamePanel gp) { this.gp = gp; font = gp.ui.kong.deriveFont(12f); }

    public void reset() { moved = attacked = guarded = fired = false; coverHintTicks = 0; }

    public void update() {
        if (gp.currentMap == 1) {
            int col=(gp.player.worldX+24)/48,row=(gp.player.worldY+24)/48;
            if (col>=25 && col<=28 && row>=38 && row<=43) coverHintTicks=Math.min(360,coverHintTicks+1);
        }
        if (gp.currentMap != 0) return;
        moved |= Math.abs(gp.player.worldX - 13 * gp.tileSize) > gp.tileSize ||
                 Math.abs(gp.player.worldY - 31 * gp.tileSize) > gp.tileSize;
        attacked |= gp.player.attacking;
        guarded |= gp.player.guarding;
        fired |= gp.player.projectile.alive;
    }

    public String message() {
        if (gp.gameState != gp.playState) return "";
        if (gp.currentMap == 1) {
            int col=(gp.player.worldX+24)/48,row=(gp.player.worldY+24)/48;
            if(col==8 && row==26 && gp.player.direction.equals("up")) return "ENTER  Examine the stone";
            if(col==26 && row==7 && gp.player.direction.equals("up")) return "ENTER  Examine the worn mark";
            if(coverHintTicks<360 && col>=25 && col<=28 && row>=38 && row<=43) return "Use the reef to break the line of fire";
            return "";
        }
        if(gp.currentMap==FourthZone.MAP) {
            int col=(gp.player.worldX+24)/48,row=(gp.player.worldY+24)/48;
            for(int[] point:FourthZone.SECRETS) if(col==point[0] && row==point[1]) return "ENTER  Face up to examine the marks";
        }
        if(gp.currentMap==2 || gp.currentMap==FourthZone.MAP) {
            if(gp.currentMap==2 && (gp.player.worldX+24)/48==27 && (gp.player.worldY+24)/48==33) return "ENTER  Face left to examine the marks";
            for(var rock:gp.iTile[gp.currentMap]) if(rock!=null && rock.destructible &&
                Math.abs(rock.worldX-gp.player.worldX)+Math.abs(rock.worldY-gp.player.worldY)<gp.tileSize*3) {
                return gp.player.currentWeapon.type==4 ? "ENTER  Break the cracked stone" : "C  Inventory / Equip Rocky, then ENTER to strike";
            }
            return "";
        }
        if (gp.currentMap != 0) return "";
        int x = (gp.player.worldX + 24) / gp.tileSize, y = (gp.player.worldY + 24) / gp.tileSize;
        if (x == 23 && y == 39 && gp.player.direction.equals("down")) return "ENTER  Examine the smiling stone";
        if (x == 12 && y == 19 && gp.player.direction.equals("up")) return "ENTER  Examine the worn mark";
        if (Math.abs(x - 15) + Math.abs(y - 29) <= 1) return "ENTER  Talk";
        if (x == 13 && y == 31 && (moved || gp.player.level > 1)) return "ENTER  Rest and save";
        if (gp.player.level > 1) return "";
        if (!moved) return "W A S D  Swim";
        if (!attacked && x >= 18 && x <= 25 && y >= 28 && y <= 34) return "ENTER  Attack";
        if (!guarded && x >= 18 && x <= 25 && y >= 20 && y <= 25) return "SPACE  Guard facing the enemy";
        if (!fired && x >= 29 && x <= 36 && y >= 19 && y <= 24) return "F  Shoot  /  Uses water";
        return "";
    }

    public void draw(Graphics2D target) {
        String text = message();
        if (text.isEmpty()) return;
        Graphics2D g = (Graphics2D) target.create();
        g.setFont(font);
        int width = g.getFontMetrics().stringWidth(text) + 28;
        int x = (gp.screenWidht - width) / 2, y = gp.screenHeight - 43;
        g.setColor(new Color(9, 26, 35, 215)); g.fillRect(x, y, width, 29);
        g.setColor(new Color(127, 192, 185)); g.fillRect(x, y, 3, 29);
        g.setColor(new Color(222, 239, 218)); g.drawString(text, x + 14, y + 19);
        g.dispose();
    }
}
