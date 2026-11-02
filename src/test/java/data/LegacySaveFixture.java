package data;

import main.GamePanel;
import object.OBJ_Lantern;

/** Executed with the ORIGINAL JAR first on the class path, never the rebuilt SaveLoad. */
public final class LegacySaveFixture {
    public static void main(String[] args) {
        GamePanel gp = new GamePanel();
        gp.aSetter.setObject();
        gp.aSetter.setNPC();
        gp.aSetter.setMonster();
        gp.aSetter.setInteractiveTile();
        gp.player.level = 6;
        gp.player.strength = 6;
        gp.player.dexterity = 6;
        gp.player.maxLife = 16;
        gp.player.life = 12;
        gp.player.exp = 100;
        gp.player.nextLevelExp = 160;
        gp.player.coin = 42;
        gp.player.inventory.add(new OBJ_Lantern(gp));
        gp.obj[5][1].opened = true;
        new SaveLoad(gp).save();
    }
}
