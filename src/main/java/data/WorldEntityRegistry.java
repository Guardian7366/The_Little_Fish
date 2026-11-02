package data;

import entity.*;
import main.GamePanel;
import monster.*;
import tile_interactive.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Explicit, bounded world factory. A save can never name an arbitrary Java class. */
final class WorldEntityRegistry {
    private final Map<String, Function<GamePanel, Entity>> factories = new HashMap<>();
    private final Map<Class<?>, String> ids = new HashMap<>();

    WorldEntityRegistry() {
        add("npc:big-rock", NPC_BigRock.class, NPC_BigRock::new);
        add("npc:candle", NPC_Candle.class, NPC_Candle::new);
        add("npc:chicken", NPC_Chicken.class, NPC_Chicken::new);
        add("npc:coco", NPC_Coco.class, NPC_Coco::new);
        add("npc:flower", NPC_Flower.class, NPC_Flower::new);
        add("npc:ghost", NPC_Ghost.class, NPC_Ghost::new);
        add("npc:handy", NPC_Handy.class, NPC_Handy::new);
        add("npc:hongui", NPC_Hongui.class, NPC_Hongui::new);
        add("npc:ic", NPC_Ic.class, NPC_Ic::new);
        add("npc:pump", NPC_Pump.class, NPC_Pump::new);
        add("npc:shadow", NPC_Shadow.class, NPC_Shadow::new);
        add("npc:spiky", NPC_Spiky.class, NPC_Spiky::new);
        add("npc:square", NPC_Square.class, NPC_Square::new);
        add("npc:tuti", NPC_Tuti.class, NPC_Tuti::new);
        add("npc:tv", NPC_TV.class, NPC_TV::new);
        add("monster:bomb", MON_Bomb.class, MON_Bomb::new);
        add("monster:angry", MON_Enfadillo.class, MON_Enfadillo::new);
        add("monster:flower", MON_Flower.class, MON_Flower::new);
        add("monster:god-fish", MON_GodFish.class, MON_GodFish::new);
        add("monster:green", MON_Normalillo.class, MON_Normalillo::new);
        add("monster:purple", MON_Picaron.class, MON_Picaron::new);
        add("monster:secret", MON_Secret.class, MON_Secret::new);
        add("monster:shark", MON_Sharky.class, MON_Sharky::new);
        add("monster:tella", MON_Tella.class, MON_Tella::new);
        add("tile:wall", IT_DestructibleWall.class, p -> new IT_DestructibleWall(p, 0, 0));
        add("tile:plate", IT_MetalPlate.class, p -> new IT_MetalPlate(p, 0, 0));
        add("tile:rock", IT_Rock.class, p -> new IT_Rock(p, 0, 0));
        add("tile:rubble", IT_Rocky.class, p -> new IT_Rocky(p, 0, 0));
    }

    private void add(String id, Class<?> type, Function<GamePanel, Entity> factory) {
        ids.put(type, id);
        factories.put(id, factory);
    }

    String idOf(Entity entity) {
        String id = ids.get(entity.getClass());
        if (id == null) throw new IllegalArgumentException("Unregistered world entity: " + entity.getClass().getName());
        return id;
    }

    Entity create(String id, String category, GamePanel gp) {
        Function<GamePanel, Entity> factory = factories.get(id);
        if (id == null || !id.startsWith(category + ":") || factory == null)
            throw new IllegalArgumentException("Unknown " + category + ": " + id);
        return factory.apply(gp);
    }
}
