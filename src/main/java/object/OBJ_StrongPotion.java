package object;

import entity.Entity;
import main.GamePanel;

public final class OBJ_StrongPotion extends Entity {
    private final GamePanel panel;

    public OBJ_StrongPotion(GamePanel gp) {
        super(gp);
        panel = gp;
        type = 6;
        name = "Strong Potion";
        down = setup("/mercado/items/strong-potion", gp.tileSize, gp.tileSize);
        description = "[" + name + "]\nRestores 10 HP.";
        price = 9;
        stackable = true;
    }

    @Override public boolean use(Entity entity) {
        if (entity.life >= entity.maxLife) return false;
        entity.life += Math.min(10, entity.maxLife - entity.life);
        panel.playSE(12);
        return true;
    }
}
