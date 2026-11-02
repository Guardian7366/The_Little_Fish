/*
 * Decompiled with CFR 0.152.
 */
package tile_interactive;

import entity.Entity;
import java.awt.Color;
import java.util.Random;
import main.GamePanel;
import object.OBJ_Coin;
import tile_interactive.IT_Rocky;
import tile_interactive.InteractiveTile;

public class IT_Rock
extends InteractiveTile {
    GamePanel gp;

    public IT_Rock(GamePanel gp, int col, int row) {
        super(gp, col, row);
        this.gp = gp;
        this.worldX = gp.tileSize * col;
        this.worldY = gp.tileSize * row;
        this.down = this.setup("/tiles_interactive/rockTile", gp.tileSize, gp.tileSize);
        this.destructible = true;
        this.life = 2;
    }

    @Override
    public boolean isCorrectItem(Entity entity) {
        boolean isCorrectItem = false;
        if (entity.currentWeapon.type == 4) {
            isCorrectItem = true;
        }
        return isCorrectItem;
    }

    @Override
    public void playSE() {
        this.gp.playSE(14);
    }

    @Override
    public InteractiveTile getDestroyedForm() {
        IT_Rocky tile = new IT_Rocky(this.gp, this.worldX / this.gp.tileSize, this.worldY / this.gp.tileSize);
        return tile;
    }

    @Override
    public Color getparticleColor() {
        Color color = new Color(58, 59, 80);
        return color;
    }

    @Override
    public int getParticleSize() {
        int size = 6;
        return size;
    }

    @Override
    public int getParticleSpeed() {
        int speed = 1;
        return speed;
    }

    @Override
    public int getParticleMaxLife() {
        int maxLife = 20;
        return maxLife;
    }

    @Override
    public void checkDrop() {
        int i = new Random().nextInt(50) + 1;
        if (i < 50) {
            this.dropItem(new OBJ_Coin(this.gp));
        }
    }
}

