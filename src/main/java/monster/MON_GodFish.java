/*
 * Decompiled with CFR 0.152.
 */
package monster;

import data.Progress;
import entity.Entity;
import java.util.Random;
import main.GamePanel;
import object.OBJ_Key;

public class MON_GodFish
extends Entity {
    GamePanel gp;
    public static final String monName = "God Fish";

    public MON_GodFish(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 2;
        this.name = monName;
        this.boss = true;
        this.speed = this.defaultSpeed = 1;
        this.life = this.maxLife = 650;
        this.attack = 35;
        this.defense = 5;
        this.exp = 500;
        this.knockBackPower = 5;
        this.sleep = true;
        int size = gp.tileSize * 5;
        this.solidArea.x = 48;
        this.solidArea.y = 48;
        this.solidArea.width = size - 96;
        this.solidArea.height = size - 48;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
        this.attackArea.width = 150;
        this.attackArea.height = 150;
        this.motion1_duration = 25;
        this.motion2_duration = 50;
        this.getImage();
        this.getAttackImage();
        this.setDialogue();
    }

    public void getImage() {
        int i = 5;
        if (!this.inRage) {
            this.up = this.setup("/monster/BoosUp1", this.gp.tileSize * i, this.gp.tileSize * i);
            this.up2 = this.setup("/monster/BoosUp2", this.gp.tileSize * i, this.gp.tileSize * i);
            this.left = this.setup("/monster/BoosLeft1", this.gp.tileSize * i, this.gp.tileSize * i);
            this.left2 = this.setup("/monster/BoosLeft2", this.gp.tileSize * i, this.gp.tileSize * i);
            this.down = this.setup("/monster/BoosDown1", this.gp.tileSize * i, this.gp.tileSize * i);
            this.down2 = this.setup("/monster/BoosDown2", this.gp.tileSize * i, this.gp.tileSize * i);
            this.right = this.setup("/monster/BoosRight1", this.gp.tileSize * i, this.gp.tileSize * i);
            this.right2 = this.setup("/monster/BoosRight2", this.gp.tileSize * i, this.gp.tileSize * i);
        }
        if (this.inRage) {
            this.up = this.setup("/monster/BoosUp1Phase", this.gp.tileSize * i, this.gp.tileSize * i);
            this.up2 = this.setup("/monster/BoosUp2Phase", this.gp.tileSize * i, this.gp.tileSize * i);
            this.left = this.setup("/monster/BoosLeft1Phase", this.gp.tileSize * i, this.gp.tileSize * i);
            this.left2 = this.setup("/monster/BoosLeft2Phase", this.gp.tileSize * i, this.gp.tileSize * i);
            this.down = this.setup("/monster/BoosDown1Phase", this.gp.tileSize * i, this.gp.tileSize * i);
            this.down2 = this.setup("/monster/BoosDown2Phase", this.gp.tileSize * i, this.gp.tileSize * i);
            this.right = this.setup("/monster/BoosRight1Phase", this.gp.tileSize * i, this.gp.tileSize * i);
            this.right2 = this.setup("/monster/BoosRight2Phase", this.gp.tileSize * i, this.gp.tileSize * i);
        }
    }

    public void getAttackImage() {
        int i = 5;
        if (!this.inRage) {
            this.attackUp1 = this.setup("/monster/BoosAUp1", this.gp.tileSize * i, this.gp.tileSize * i * 2);
            this.attackUp2 = this.setup("/monster/BoosAUp2", this.gp.tileSize * i, this.gp.tileSize * i * 2);
            this.attackDown1 = this.setup("/monster/BoosADown1", this.gp.tileSize * i, this.gp.tileSize * i * 2);
            this.attackDown2 = this.setup("/monster/BossADown2", this.gp.tileSize * i, this.gp.tileSize * i * 2);
            this.attackLeft1 = this.setup("/monster/BoosALeft1", this.gp.tileSize * i * 2, this.gp.tileSize * i);
            this.attackLeft2 = this.setup("/monster/BoosALeft2", this.gp.tileSize * i * 2, this.gp.tileSize * i);
            this.attackRight1 = this.setup("/monster/BoosARight1", this.gp.tileSize * i * 2, this.gp.tileSize * i);
            this.attackRight2 = this.setup("/monster/BoosARight2", this.gp.tileSize * i * 2, this.gp.tileSize * i);
        }
        if (this.inRage) {
            this.attackUp1 = this.setup("/monster/BoosAUp1Phase", this.gp.tileSize * i, this.gp.tileSize * i * 2);
            this.attackUp2 = this.setup("/monster/BoosAUp2Phase", this.gp.tileSize * i, this.gp.tileSize * i * 2);
            this.attackDown1 = this.setup("/monster/BoosADown1Phase", this.gp.tileSize * i, this.gp.tileSize * i * 2);
            this.attackDown2 = this.setup("/monster/BossADown2Phase", this.gp.tileSize * i, this.gp.tileSize * i * 2);
            this.attackLeft1 = this.setup("/monster/BoosALeft1Phase", this.gp.tileSize * i * 2, this.gp.tileSize * i);
            this.attackLeft2 = this.setup("/monster/BoosALeft2Phase", this.gp.tileSize * i * 2, this.gp.tileSize * i);
            this.attackRight1 = this.setup("/monster/BoosARight1Phase", this.gp.tileSize * i * 2, this.gp.tileSize * i);
            this.attackRight2 = this.setup("/monster/BoosARight2Phase", this.gp.tileSize * i * 2, this.gp.tileSize * i);
        }
    }

    public void setDialogue() {
        this.dialogues[0][0] = "So you've finally arrived?";
        this.dialogues[0][1] = "Do you really want this D?";
        this.dialogues[0][2] = "But.....";
        this.dialogues[0][3] = "If you really want this....";
        this.dialogues[0][4] = "I WILL GO WITH EVERYTHING";
    }

    @Override
    public void setAction() {
        if (!this.inRage && this.life < this.maxLife / 2) {
            this.inRage = true;
            this.getImage();
            this.getAttackImage();
            ++this.defaultSpeed;
            this.speed = this.defaultSpeed;
            this.attack += 5;
        }
        if (this.getTileDistance(this.gp.player) < 10) {
            this.moveTowardPlayer(60);
        } else {
            this.getRandomDirection(80);
        }
        if (!this.attacking) {
            this.checkAttackOrNot(60, this.gp.tileSize * 7, this.gp.tileSize * 5);
        }
    }

    @Override
    public void damageReaction() {
        this.actionLockCounter = 0;
    }

    @Override
    public void checkDrop() {
        this.gp.bossBattleOn = false;
        Progress.GodFishDefeated = true;
        this.gp.stopMusic();
        this.gp.playMusic(22);
        int i = 0;
        while (i < this.gp.obj[1].length) {
            if (this.gp.obj[this.gp.currentMap][i] != null && this.gp.obj[this.gp.currentMap][i].name.equals("Iron Door")) {
                this.gp.playSE(3);
                this.gp.obj[this.gp.currentMap][i] = null;
            }
            ++i;
        }
        i = new Random().nextInt(50) + 1;
        if (i <= 50) {
            this.dropItem(new OBJ_Key(this.gp));
        }
    }
}

