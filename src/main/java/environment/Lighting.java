/*
 * Decompiled with CFR 0.152.
 */
package environment;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;
import main.GamePanel;

public class Lighting {
    GamePanel gp;
    BufferedImage darknessFilter;
    public int dayCounter;
    public float filterAlpha = 0.0f;
    public final int day = 0;
    public final int dusk = 1;
    public final int night = 2;
    public final int dawn = 3;
    public int dayState = 0;
    public final FifthLighting fifth;

    public Lighting(GamePanel gp) {
        this.gp = gp;
        fifth=new FifthLighting(gp);
        this.setLightSource();
    }

    public void setLightSource() {
        this.darknessFilter = new BufferedImage(this.gp.screenWidht, this.gp.screenHeight, 2);
        Graphics2D g2 = (Graphics2D)this.darknessFilter.getGraphics();
        if (this.gp.player.currentLight == null) {
            g2.setColor(new Color(0.0f, 0.0f, 0.2f, 0.98f));
        } else {
            int centerX = this.gp.player.screenX + this.gp.tileSize / 2;
            int centerY = this.gp.player.screenY + this.gp.tileSize / 2;
            Color[] color = new Color[11];
            float[] fraction = new float[11];
            color[0] = new Color(0.0f, 0.0f, 0.2f, 0.0f);
            color[1] = new Color(0.0f, 0.0f, 0.2f, 0.1f);
            color[2] = new Color(0.0f, 0.0f, 0.2f, 0.2f);
            color[3] = new Color(0.0f, 0.0f, 0.2f, 0.3f);
            color[4] = new Color(0.0f, 0.0f, 0.2f, 0.4f);
            color[5] = new Color(0.0f, 0.0f, 0.2f, 0.5f);
            color[6] = new Color(0.0f, 0.0f, 0.2f, 0.6f);
            color[7] = new Color(0.0f, 0.0f, 0.2f, 0.7f);
            color[8] = new Color(0.0f, 0.0f, 0.2f, 0.8f);
            color[9] = new Color(0.0f, 0.0f, 0.2f, 0.98f);
            color[10] = new Color(0.0f, 0.0f, 0.2f, 1.0f);
            fraction[0] = 0.0f;
            fraction[1] = 0.1f;
            fraction[2] = 0.2f;
            fraction[3] = 0.3f;
            fraction[4] = 0.4f;
            fraction[5] = 0.5f;
            fraction[6] = 0.6f;
            fraction[7] = 0.7f;
            fraction[8] = 0.8f;
            fraction[9] = 0.98f;
            fraction[10] = 1.0f;
            RadialGradientPaint gPaint = new RadialGradientPaint(centerX, (float)centerY, this.gp.player.currentLight.lightRadius, fraction, color);
            g2.setPaint(gPaint);
        }
        g2.fillRect(0, 0, this.gp.screenWidht, this.gp.screenHeight);
        g2.dispose();
    }

    public void resetDay() {
        this.dayState = 0;
        this.filterAlpha = 0.0f;
    }

    public void update() {
        if(gp.currentMap==3) fifth.prepare();
        if (this.gp.player.lightUpdated) {
            this.setLightSource();
            this.gp.player.lightUpdated = false;
        }
        if (this.dayState == 0) {
            ++this.dayCounter;
            if (this.dayCounter > 2400) {
                this.dayState = 1;
                this.dayCounter = 0;
            }
        }
        if (this.dayState == 1) {
            this.filterAlpha += 5.0E-4f;
            if (this.filterAlpha > 1.0f) {
                this.filterAlpha = 1.0f;
                this.dayState = 2;
            }
        }
        if (this.dayState == 2) {
            ++this.dayCounter;
            if (this.dayCounter > 2400) {
                this.dayState = 3;
                this.dayCounter = 0;
            }
        }
        if (this.dayState == 3) {
            this.filterAlpha -= 5.0E-4f;
            if (this.filterAlpha < 0.0f) {
                this.filterAlpha = 0.0f;
                this.dayState = 0;
            }
        }
    }

    public void draw(Graphics2D g2) {
        if(gp.currentMap==3) {fifth.draw(g2);return;}
        if (this.gp.currentArea == this.gp.outside) {
            g2.setComposite(AlphaComposite.getInstance(3, this.filterAlpha));
        }
        if (this.gp.currentArea == this.gp.outside || this.gp.currentArea == this.gp.dungeons) {
            g2.drawImage((Image)this.darknessFilter, 0, 0, null);
        }
        g2.setComposite(AlphaComposite.getInstance(3, 1.0f));
        String situation = "";
        switch (this.dayState) {
            case 0: {
                situation = "Day";
                break;
            }
            case 1: {
                situation = "Dusk";
                break;
            }
            case 2: {
                situation = "Night";
                break;
            }
            case 3: {
                situation = "Dawn";
            }
        }
        g2.setColor(Color.white);
        g2.setFont(g2.getFont().deriveFont(34.0f));
        g2.drawString(situation, 750, 500);
    }
}

