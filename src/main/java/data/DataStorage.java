/*
 * Decompiled with CFR 0.152.
 */
package data;

import java.io.Serializable;
import java.util.ArrayList;

public class DataStorage
implements Serializable {
    // Keep the exact UID of the 2024 JAR for read-only import of existing saves.
    private static final long serialVersionUID = -663080577270750248L;
    int level;
    int maxLife;
    int life;
    int maxWater;
    int water;
    int strenght;
    int dexterity;
    int exp;
    int nextLevelExp;
    int coin;
    ArrayList<String> itemNames = new ArrayList<>();
    ArrayList<Integer> itemAmounts = new ArrayList<>();
    int currentWeaponSlot;
    int currentShieldSlot;
    String[][] mapObjectNames;
    int[][] mapObjectWorldX;
    int[][] mapObjectWorldY;
    String[][] mapObjectLootNames;
    boolean[][] mapObjectOpened;
}

