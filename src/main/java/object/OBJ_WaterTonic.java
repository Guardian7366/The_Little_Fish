package object;

import entity.Entity;
import main.GamePanel;

public final class OBJ_WaterTonic extends Entity {
    private final GamePanel panel;

    public OBJ_WaterTonic(GamePanel gp) {
        super(gp);
        panel = gp;
        type = 6;
        name = "Water Tonic";
        down = setup("/mercado/items/water-tonic", gp.tileSize, gp.tileSize);
        description = "[" + name + "]\nRestores 3 water.";
        price = 5;
        stackable = true;
    }

    @Override public boolean use(Entity entity) {
        if (entity.water >= entity.maxWater) return false;
        entity.water += Math.min(3, entity.maxWater - entity.water);
        panel.playSE(12);
        return true;
    }
}
