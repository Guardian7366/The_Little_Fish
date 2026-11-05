package data;

import java.io.Serializable;

/** Data only. No game objects, images, audio resources or live references are serialized. */
final class SaveSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;
    static final int CURRENT_VERSION = 1;
    int version = CURRENT_VERSION;
    // Missing in previous saves => 0. Keep the serialization UID and schema compatible.
    int firstZoneRevision;
    int secondZoneRevision;
    int thirdZoneRevision;
    int fourthZoneRevision;
    int sixthZoneRevision;
    int templeRevision, templeReliefs, templeDefeats;
    int templeExtensionRevision;
    int extraRoomsRevision, extraRoomsFlags;
    int fifthZoneRevision;
    int map, area, x, y;
    String direction;
    int level, maxLife, life, maxWater, water, strength, dexterity, exp, nextLevelExp, coin;
    int weaponSlot, shieldSlot, lightSlot = -1;
    boolean godFishDefeated, bossBattleOn;
    Item[] inventory;
    World world;

    static final class Item implements Serializable {
        private static final long serialVersionUID = 1L;
        String id;
        int amount;
    }

    static final class World implements Serializable {
        private static final long serialVersionUID = 1L;
        Actor[][] objects, npcs, monsters, tiles;
    }

    static final class Actor implements Serializable {
        private static final long serialVersionUID = 1L;
        String id, direction;
        int x, y, life, amount, dialogueSet, dialogueIndex;
        boolean alive, dying, opened, temp, sleep, inRage, onPath;
        Item loot;
        Item[] inventory;
        int linkedTile = -1;
    }
}
