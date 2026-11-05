package main;

import entity.Entity;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import object.*;

/** Object definitions use permanent IDs; display names remain aliases for old saves. */
public final class EntityGenerator {
    private final GamePanel gp;
    private final Map<String, Function<GamePanel, Entity>> factories = new LinkedHashMap<>();
    private final Map<String, String> aliases = new LinkedHashMap<>();
    private final Map<Class<?>, String> ids = new LinkedHashMap<>();

    public EntityGenerator(GamePanel gp) {
        this.gp = gp;
        register("bed", "bed", OBJ_Fishbowl.class, OBJ_Fishbowl::new);
        register("rock-tool", "Rocky", OBJ_Rock.class, OBJ_Rock::new);
        register("bubble-projectile", "Bubble Ball", OBJ_Bubble_Ball.class, OBJ_Bubble_Ball::new);
        register("bad-bubble", "Bad Bubble", OBJ_Bad_Bubble.class, OBJ_Bad_Bubble::new);
        register("coin", "Coin", OBJ_Coin.class, OBJ_Coin::new);
        register("bust", "Bust", OBJ_Bust.class, OBJ_Bust::new);
        register("water", "Water Crystal", OBJ_WaterMana.class, OBJ_WaterMana::new);
        register("iron-tool", "Iron", OBJ_Iron.class, OBJ_Iron::new);
        register("iron-door", "Iron Door", OBJ_DoorIron.class, OBJ_DoorIron::new);
        register("bubble-shield", "Bubble shield", OBJ_Normal_Shield.class, OBJ_Normal_Shield::new);
        register("lantern", "Lantern", OBJ_Lantern.class, OBJ_Lantern::new);
        register("key", "Key", OBJ_Key.class, OBJ_Key::new);
        register("red-potion", "Red Poti", OBJ_HealHP.class, OBJ_HealHP::new);
        register("strong-potion", "Strong Potion", OBJ_StrongPotion.class, OBJ_StrongPotion::new);
        register("water-tonic", "Water Tonic", OBJ_WaterTonic.class, OBJ_WaterTonic::new);
        register("toxic-bubble", "Toxic Bubble", OBJ_Second_Weapon.class, OBJ_Second_Weapon::new);
        register("super-shield", "Super Bubble", OBJ_Super_Shield.class, OBJ_Super_Shield::new);
        register("normal-bubble", "Normal buuble", OBJ_Weapon_Normal.class, OBJ_Weapon_Normal::new);
        register("door", "Door", OBJ_Door.class, OBJ_Door::new);
        register("chest", "Chest", OBJ_Chest.class, OBJ_Chest::new);
        register("exit", "Exit", OBJ_Exit.class, OBJ_Exit::new);
        register("heart", "Heart", OBJ_Heart.class, OBJ_Heart::new);
    }

    private void register(String id, String oldName, Class<?> type, Function<GamePanel, Entity> factory) {
        factories.put(id, factory);
        aliases.put(oldName, id);
        ids.put(type, id);
    }

    /** Compatibility lookup: unknown IDs return null; save validation uses requireObject. */
    public Entity getObject(String idOrName) {
        if (idOrName == null) return null;
        Function<GamePanel, Entity> factory = factories.get(aliases.getOrDefault(idOrName, idOrName));
        return factory == null ? null : factory.apply(gp);
    }

    public Entity requireObject(String idOrName) {
        Entity result = getObject(idOrName);
        if (result == null) throw new IllegalArgumentException("Unknown object: " + idOrName);
        return result;
    }

    public String idOf(Entity entity) {
        String id = ids.get(entity.getClass());
        if (id == null) throw new IllegalArgumentException("Unregistered object: " + entity.getClass().getName());
        return id;
    }
}
