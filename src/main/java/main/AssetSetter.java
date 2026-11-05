/*
 * Decompiled with CFR 0.152.
 */
package main;

import data.Progress;
import entity.NPC_BigRock;
import entity.NPC_Candle;
import entity.NPC_Chicken;
import entity.NPC_Coco;
import entity.NPC_Flower;
import entity.NPC_Ghost;
import entity.NPC_Handy;
import entity.NPC_Hongui;
import entity.NPC_Ic;
import entity.NPC_Pump;
import entity.NPC_Shadow;
import entity.NPC_Spiky;
import entity.NPC_TV;
import entity.NPC_Tuti;
import main.GamePanel;
import monster.MON_Bomb;
import monster.MON_Enfadillo;
import monster.MON_Flower;
import monster.MON_GodFish;
import monster.MON_Normalillo;
import monster.MON_Picaron;
import monster.MON_Secret;
import monster.MON_Sharky;
import monster.MON_Tella;
import object.OBJ_Chest;
import object.OBJ_Door;
import object.OBJ_DoorIron;
import object.OBJ_Exit;
import object.OBJ_Fishbowl;
import object.OBJ_HealHP;
import object.OBJ_Iron;
import object.OBJ_Lantern;
import object.OBJ_Rock;
import object.OBJ_Second_Weapon;
import object.OBJ_Super_Shield;
import tile_interactive.IT_DestructibleWall;
import tile_interactive.IT_MetalPlate;
import tile_interactive.IT_Rock;

public class AssetSetter {
    GamePanel gp;

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    public void setObject() {
        SixthZone.reward(gp, gp.obj);
        gp.obj[TempleExtension.MAP][0] = Temple.gate(gp,TempleExtension.GATE_X,TempleExtension.GATE_Y);
        int mapNum = 0;
        int i = 0;
        mapNum = 4;
        this.gp.obj[mapNum][i] = Temple.gate(gp);
        mapNum = 5;
        this.gp.obj[mapNum][++i] = new OBJ_Chest(this.gp);
        this.gp.obj[mapNum][i].setLoot(new OBJ_Lantern(this.gp));
        this.gp.obj[mapNum][i].worldX = this.gp.tileSize * 20;
        this.gp.obj[mapNum][i].worldY = this.gp.tileSize * 41;
        this.gp.obj[mapNum][++i] = new OBJ_Chest(this.gp);
        this.gp.obj[mapNum][i].setLoot(new OBJ_HealHP(this.gp));
        this.gp.obj[mapNum][i].worldX = this.gp.tileSize * 28;
        this.gp.obj[mapNum][i].worldY = this.gp.tileSize * 37;
        this.gp.obj[mapNum][++i] = new OBJ_Chest(this.gp);
        this.gp.obj[mapNum][i].setLoot(new OBJ_Fishbowl(this.gp));
        this.gp.obj[mapNum][i].worldX = this.gp.tileSize * 28;
        this.gp.obj[mapNum][i].worldY = this.gp.tileSize * 30;
        this.gp.obj[mapNum][++i] = new OBJ_Chest(this.gp);
        this.gp.obj[mapNum][i].setLoot(new OBJ_HealHP(this.gp));
        this.gp.obj[mapNum][i].worldX = this.gp.tileSize * 20;
        this.gp.obj[mapNum][i].worldY = this.gp.tileSize * 30;
        this.gp.obj[mapNum][++i] = new OBJ_Chest(this.gp);
        this.gp.obj[mapNum][i].setLoot(new OBJ_Rock(this.gp));
        this.gp.obj[mapNum][i].worldX = this.gp.tileSize * 29;
        this.gp.obj[mapNum][i].worldY = this.gp.tileSize * 21;
        this.gp.obj[mapNum][++i] = new OBJ_Chest(this.gp);
        this.gp.obj[mapNum][i].setLoot(new OBJ_Super_Shield(this.gp));
        this.gp.obj[mapNum][i].worldX = this.gp.tileSize * 20;
        this.gp.obj[mapNum][i].worldY = this.gp.tileSize * 20;
        this.gp.obj[mapNum][++i] = new OBJ_Chest(this.gp);
        this.gp.obj[mapNum][i].setLoot(new OBJ_Second_Weapon(this.gp));
        this.gp.obj[mapNum][i].worldX = this.gp.tileSize * 20;
        this.gp.obj[mapNum][i].worldY = this.gp.tileSize * 10;
        this.gp.obj[mapNum][++i] = new OBJ_Chest(this.gp);
        this.gp.obj[mapNum][i].setLoot(new OBJ_Iron(this.gp));
        this.gp.obj[mapNum][i].worldX = this.gp.tileSize * 28;
        this.gp.obj[mapNum][i].worldY = this.gp.tileSize * 11;
        mapNum = 6;
        this.gp.obj[mapNum][++i] = new OBJ_Door(this.gp);
        this.gp.obj[mapNum][i].worldX = this.gp.tileSize * 26;
        this.gp.obj[mapNum][i].worldY = this.gp.tileSize * 12;
        this.gp.obj[mapNum][++i] = new OBJ_Exit(this.gp);
        this.gp.obj[mapNum][i].worldX = this.gp.tileSize * 26;
        this.gp.obj[mapNum][i].worldY = this.gp.tileSize * 5;
        ++i;
    }

    public void setNPC() {
        TempleExtension.rocks(gp,gp.npc);
        int mapNum = 0;
        int i = 0;
        this.gp.npc[mapNum][i] = new NPC_Shadow(this.gp);
        this.gp.npc[mapNum][i].worldX = this.gp.tileSize * 15;
        this.gp.npc[mapNum][i].worldY = this.gp.tileSize * 29;
        mapNum = 1;
        this.gp.npc[mapNum][++i] = new NPC_Tuti(this.gp);
        this.gp.npc[mapNum][i].worldX = this.gp.tileSize * 23;
        this.gp.npc[mapNum][i].worldY = this.gp.tileSize * 34;
        mapNum = 2;
        this.gp.npc[mapNum][++i] = new NPC_Coco(this.gp);
        this.gp.npc[mapNum][i].worldX = this.gp.tileSize * 17;
        this.gp.npc[mapNum][i].worldY = this.gp.tileSize * 34;
        mapNum = 4;
        this.gp.npc[mapNum][++i] = new NPC_Candle(this.gp);
        this.gp.npc[mapNum][i].worldX = this.gp.tileSize * 21;
        this.gp.npc[mapNum][i].worldY = this.gp.tileSize * 38;
        for (Temple.Lane lane : Temple.LANES)
            gp.npc[4][++i] = Temple.place(new NPC_BigRock(gp),lane.x(),lane.y());
        mapNum = 5;
        this.gp.npc[mapNum][++i] = new NPC_Chicken(this.gp);
        this.gp.npc[mapNum][i].worldX = this.gp.tileSize * 20;
        this.gp.npc[mapNum][i].worldY = this.gp.tileSize * 37;
        this.gp.npc[mapNum][++i] = new NPC_Flower(this.gp);
        this.gp.npc[mapNum][i].worldX = this.gp.tileSize * 28;
        this.gp.npc[mapNum][i].worldY = this.gp.tileSize * 41;
        this.gp.npc[mapNum][++i] = new NPC_Spiky(this.gp);
        this.gp.npc[mapNum][i].worldX = this.gp.tileSize * 28;
        this.gp.npc[mapNum][i].worldY = this.gp.tileSize * 26;
        this.gp.npc[mapNum][++i] = new NPC_Pump(this.gp);
        this.gp.npc[mapNum][i].worldX = this.gp.tileSize * 20;
        this.gp.npc[mapNum][i].worldY = this.gp.tileSize * 26;
        this.gp.npc[mapNum][++i] = new NPC_Hongui(this.gp);
        this.gp.npc[mapNum][i].worldX = this.gp.tileSize * 20;
        this.gp.npc[mapNum][i].worldY = this.gp.tileSize * 15;
        this.gp.npc[mapNum][++i] = new NPC_Ghost(this.gp);
        this.gp.npc[mapNum][i].worldX = this.gp.tileSize * 29;
        this.gp.npc[mapNum][i].worldY = this.gp.tileSize * 15;
        this.gp.npc[mapNum][++i] = new NPC_Ic(this.gp);
        this.gp.npc[mapNum][i].worldX = this.gp.tileSize * 20;
        this.gp.npc[mapNum][i].worldY = this.gp.tileSize * 7;
        this.gp.npc[mapNum][++i] = new NPC_Handy(this.gp);
        this.gp.npc[mapNum][i].worldX = this.gp.tileSize * 28;
        this.gp.npc[mapNum][i].worldY = this.gp.tileSize * 6;
        mapNum = 6;
        this.gp.npc[mapNum][++i] = new NPC_TV(this.gp);
        this.gp.npc[mapNum][i].worldX = this.gp.tileSize * 26;
        this.gp.npc[mapNum][i].worldY = this.gp.tileSize * 40;
        ++i;
    }

    public void setMonster() {
        FourthZone.populate(gp,gp.monster);
        SixthZone.populate(gp, gp.monster);
        Temple.monsters(gp, gp.monster, gp.temple.defeats);
        TempleExtension.populate(gp,gp.monster);
        int mapNum = 0;
        int i = 0;
        FirstZone.populate(gp);
        i = 5; // Preserve the original slots on every later map.
        SecondZone.populate(gp);
        i = 20; // Keep later-map actor slots stable.
        mapNum = 2;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 12;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 7;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 36;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 7;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 20;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 23;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 15;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 32;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 36;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 41;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 38;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 30;
        this.gp.monster[mapNum][++i] = new MON_Picaron(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 29;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 18;
        this.gp.monster[mapNum][++i] = new MON_Picaron(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 21;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 24;
        this.gp.monster[mapNum][++i] = new MON_Picaron(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 24;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 38;
        this.gp.monster[mapNum][++i] = new MON_Picaron(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 12;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 42;
        this.gp.monster[mapNum][++i] = new MON_Picaron(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 32;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 41;
        this.gp.monster[mapNum][++i] = new MON_Picaron(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 35;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 25;
        ThirdZone.configure(gp,gp.monster,true);
        FifthZone.populate(gp,gp.monster);
        i = 60; // Keep all map 5 actor slots stable.
        mapNum = 5;
        this.gp.monster[mapNum][++i] = new MON_Normalillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 13;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 41;
        this.gp.monster[mapNum][++i] = new MON_Normalillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 17;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 37;
        this.gp.monster[mapNum][++i] = new MON_Normalillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 17;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 42;
        this.gp.monster[mapNum][++i] = new MON_Normalillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 14;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 38;
        this.gp.monster[mapNum][++i] = new MON_Normalillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 33;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 37;
        this.gp.monster[mapNum][++i] = new MON_Normalillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 33;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 39;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 30;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 42;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 36;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 37;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 32;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 26;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 35;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 27;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 31;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 26;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 35;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 25;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 13;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 26;
        this.gp.monster[mapNum][++i] = new MON_Enfadillo(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 13;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 31;
        this.gp.monster[mapNum][++i] = new MON_Picaron(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 17;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 26;
        this.gp.monster[mapNum][++i] = new MON_Picaron(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 18;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 30;
        this.gp.monster[mapNum][++i] = new MON_Picaron(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 14;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 17;
        this.gp.monster[mapNum][++i] = new MON_Picaron(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 13;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 18;
        this.gp.monster[mapNum][++i] = new MON_Picaron(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 18;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 22;
        this.gp.monster[mapNum][++i] = new MON_Picaron(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 18;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 15;
        i += 2; // Secrets keep their identities in the temple.
        this.gp.monster[mapNum][++i] = new MON_Sharky(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 14;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 6;
        this.gp.monster[mapNum][++i] = new MON_Flower(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 14;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 11;
        this.gp.monster[mapNum][++i] = new MON_Tella(this.gp);
        this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 18;
        this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 12;
        ++i;
        mapNum = 6;
        ++i;
        if (!Progress.GodFishDefeated) {
            this.gp.monster[mapNum][i] = new MON_GodFish(this.gp);
            this.gp.monster[mapNum][i].worldX = this.gp.tileSize * 23;
            this.gp.monster[mapNum][i].worldY = this.gp.tileSize * 14;
            ++i;
        }
    }

    public void setInteractiveTile() {
        TempleExtension.plates(gp,gp.iTile);
        FourthZone.rocks(gp,gp.iTile);
        SixthZone.obstacles(gp, gp.iTile);
        int mapNum = 0;
        int i = 0;
        mapNum = 2;
        this.gp.iTile[mapNum][i] = new IT_Rock(this.gp, 22, 20);
        this.gp.iTile[mapNum][++i] = new IT_Rock(this.gp, 23, 20);
        this.gp.iTile[mapNum][++i] = new IT_Rock(this.gp, 27, 30);
        this.gp.iTile[mapNum][++i] = new IT_Rock(this.gp, 27, 31);
        FifthZone.obstacles(gp,gp.iTile);
        for (Temple.Lane lane : Temple.LANES)
            gp.iTile[4][lane.plateSlot()] = new IT_MetalPlate(gp,lane.endX(),lane.endY());
    }
}
