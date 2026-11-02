/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import entity.Projectile;
import java.awt.Color;
import main.GamePanel;

public class OBJ_Bubble_Ball
extends Projectile {
    GamePanel gp;
    public static final String objName = "Bubble Ball";

    public OBJ_Bubble_Ball(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.name = objName;
        this.speed = 6;
        this.life = this.maxLife = 40;
        this.attack = 2;
        this.knockBackPower = 5;
        this.useCost = 1;
        this.alive = false;
        this.getImage();
    }

    public void getImage() {
        this.up = this.setup("/proyectile/proUp", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/proyectile/proUp2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/proyectile/proDown", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/proyectile/proDown2", this.gp.tileSize, this.gp.tileSize);
        this.left = this.setup("/proyectile/proLeft", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/proyectile/proLeft2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/proyectile/proRight", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/proyectile/proRight2", this.gp.tileSize, this.gp.tileSize);
    }

    @Override
    public boolean haveResourse(Entity user) {
        boolean haveResource = false;
        if (user.water >= this.useCost) {
            haveResource = true;
        }
        return haveResource;
    }

    @Override
    public void substractResource(Entity user) {
        user.water -= this.useCost;
    }

    @Override
    public Color getparticleColor() {
        Color color = new Color(0, 0, 139);
        return color;
    }

    @Override
    public int getParticleSize() {
        int size = 10;
        return size;
    }

    @Override
    public int getParticleSpeed() {
        int speed = 1;
        return speed;
    }

    @Override
    public int getParticleMaxLife() {
        int maxLife = 10;
        return maxLife;
    }
}

