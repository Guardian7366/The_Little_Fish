/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import main.GamePanel;

public class PlayerDummy
extends Entity {
    public static final String npcName = "Dummy";

    public PlayerDummy(GamePanel gp) {
        super(gp);
        this.name = npcName;
        this.getImage();
    }

    public void getImage() {
        this.left = this.setup("/player/HaciaIquierda", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/player/BocaIzquierdaNormal", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/player/HaciaDerecha", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/player/BocaDerechaNormal", this.gp.tileSize, this.gp.tileSize);
        this.up = this.setup("/player/HaciaArriba", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/player/BocaArribaNormal", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/player/HaciaAbajo", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/player/BocaAbajoNormal", this.gp.tileSize, this.gp.tileSize);
    }
}

