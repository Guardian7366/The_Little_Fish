/*
 * Decompiled with CFR 0.152.
 */
package tile_interactive;

import main.GamePanel;
import tile_interactive.InteractiveTile;

public class IT_MetalPlate
extends InteractiveTile {
    GamePanel gp;
    public static final String itName = "Metal Plate";

    public IT_MetalPlate(GamePanel gp, int col, int row) {
        super(gp, col, row);
        this.gp = gp;
        this.worldX = gp.tileSize * col;
        this.worldY = gp.tileSize * row;
        this.name = itName;
        this.down = this.setup("/tiles_interactive/metalPlate", gp.tileSize, gp.tileSize);
        this.solidArea.x = 0;
        this.solidArea.y = 0;
        this.solidArea.width = 0;
        this.solidArea.height = 0;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.x;
    }
}

