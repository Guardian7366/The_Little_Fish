/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Entity;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import main.GamePanel;
import main.KeyHandler;
import object.OBJ_Bubble_Ball;
import object.OBJ_Normal_Shield;
import object.OBJ_Weapon_Normal;

public class Player
extends Entity {
    KeyHandler keyH;
    public final int screenX;
    public final int screenY;
    public int hasKey = 0;
    public boolean attackCanceled = false;
    public boolean lightUpdated = false;

    public Player(GamePanel gp, KeyHandler keyH) {
        super(gp);
        this.keyH = keyH;
        this.screenX = gp.screenWidht / 2 - gp.tileSize / 2;
        this.screenY = gp.screenHeight / 2 - gp.tileSize / 2;
        this.solidArea = new Rectangle();
        this.solidArea.x = 13;
        this.solidArea.y = 12;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
        this.solidArea.width = 20;
        this.solidArea.height = 20;
        this.setDefaultValues();
    }

    public void setDefaultValues() {
        this.worldX = this.gp.tileSize * 13;
        this.worldY = this.gp.tileSize * 31;
        this.direction = "right";
        this.level = 1;
        this.strength = 1;
        this.dexterity = 1;
        this.exp = 0;
        this.speed = this.defaultSpeed = 4;
        this.maxLife = 6;
        this.water = this.maxWater = 6;
        this.life = this.maxLife;
        this.nextLevelExp = 5;
        this.coin = 1;
        this.currentLight = null;
        this.currentWeapon = new OBJ_Weapon_Normal(this.gp);
        this.currentShield = new OBJ_Normal_Shield(this.gp);
        this.projectile = new OBJ_Bubble_Ball(this.gp);
        this.attack = this.getAttack();
        this.defense = this.getDefense();
        this.getImage();
        this.getAttackImage();
        this.getGuardImage();
        this.setItems();
        this.setDialogue();
    }

    public void setDefaultPositions() {
        this.gp.currentArea = this.gp.outside;
        this.worldX = this.gp.tileSize * 13;
        this.worldY = this.gp.tileSize * 31;
        this.direction = "right";
    }

    public void setDialogue() {
        this.dialogues[0][0] = "You are level " + this.level + " now!\nYou feel determinated!";
    }

    public void restoreStatus() {
        this.life = this.maxLife;
        this.water = this.maxWater;
        this.speed = this.defaultSpeed;
        this.invincible = false;
        this.transparent = false;
        this.attacking = false;
        this.guarding = false;
        this.knockBack = false;
        this.attackCanceled = false;
        this.resetCounter();
        this.lightUpdated = true;
    }

    public void setItems() {
        this.inventory.clear();
        this.inventory.add(this.currentWeapon);
        this.inventory.add(this.currentShield);
    }

    public int getAttack() {
        this.attackArea = this.currentWeapon.attackArea;
        this.motion1_duration = this.currentWeapon.motion1_duration;
        this.motion2_duration = this.currentWeapon.motion2_duration;
        this.attack = this.strength * this.currentWeapon.attackValue;
        return this.attack;
    }

    public int getDefense() {
        this.defense = this.dexterity * this.currentShield.defenseValue;
        return this.defense;
    }

    public int getCurrentWeaponSlot() {
        int currentWeaponSlot = 0;
        int i = 0;
        while (i < this.inventory.size()) {
            if (this.inventory.get(i) == this.currentWeapon) {
                currentWeaponSlot = i;
            }
            ++i;
        }
        return currentWeaponSlot;
    }

    public int getCurrentShieldSlot() {
        int currentShieldSlot = 0;
        int i = 0;
        while (i < this.inventory.size()) {
            if (this.inventory.get(i) == this.currentShield) {
                currentShieldSlot = i;
            }
            ++i;
        }
        return currentShieldSlot;
    }

    public void getImage() {
        this.left = this.setup("/player/HaciaIquierda", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/player/BocaIzquierdaNormal", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/player/HaciaDerecha", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/player/BocaDerechaNormal", this.gp.tileSize, this.gp.tileSize);
        this.up = this.setup("/player/HaciaArriba", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/player/BocaArribaNormal", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/player/HaciaAbajo", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/player/BocaAbajoNormal", this.gp.tileSize, this.gp.tileSize);
    }

    public void getSleepingImage(BufferedImage image) {
        this.left = image;
        this.left2 = image;
        this.right = image;
        this.right2 = image;
        this.up = image;
        this.up2 = image;
        this.down = image;
        this.down2 = image;
    }

    public void getAttackImage() {
        if (this.currentWeapon.type == 3) {
            this.attackUp1 = this.setup("/player/ataqueUp1", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackUp2 = this.setup("/player/ataqueUp2", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackDown1 = this.setup("/player/ataqueDown1", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackDown2 = this.setup("/player/ataqueDown2", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackLeft1 = this.setup("/player/ataqueLeft1", this.gp.tileSize * 2, this.gp.tileSize);
            this.attackLeft2 = this.setup("/player/ataqueLeft2", this.gp.tileSize * 2, this.gp.tileSize);
            this.attackRight1 = this.setup("/player/ataqueRight1", this.gp.tileSize * 2, this.gp.tileSize);
            this.attackRight2 = this.setup("/player/ataqueRight2", this.gp.tileSize * 2, this.gp.tileSize);
        }
        if ("Toxic Bubble".equals(this.currentWeapon.name)) {
            this.attackUp1 = this.setup("/player/secondUp1", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackUp2 = this.setup("/player/secondUp2", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackDown1 = this.setup("/player/secondDown1", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackDown2 = this.setup("/player/secondDown2", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackLeft1 = this.setup("/player/secondLeft1", this.gp.tileSize * 2, this.gp.tileSize);
            this.attackLeft2 = this.setup("/player/secondLeft2", this.gp.tileSize * 2, this.gp.tileSize);
            this.attackRight1 = this.setup("/player/secondRight1", this.gp.tileSize * 2, this.gp.tileSize);
            this.attackRight2 = this.setup("/player/secondRight2", this.gp.tileSize * 2, this.gp.tileSize);
        }
        if ("Rocky".equals(this.currentWeapon.name)) {
            this.attackUp1 = this.setup("/player/rockUp1", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackUp2 = this.setup("/player/rockUp2", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackDown1 = this.setup("/player/rockDown1", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackDown2 = this.setup("/player/rockDown2", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackLeft1 = this.setup("/player/rockLeft1", this.gp.tileSize * 2, this.gp.tileSize);
            this.attackLeft2 = this.setup("/player/rockLeft2", this.gp.tileSize * 2, this.gp.tileSize);
            this.attackRight1 = this.setup("/player/rockRight1", this.gp.tileSize * 2, this.gp.tileSize);
            this.attackRight2 = this.setup("/player/rockRight2", this.gp.tileSize * 2, this.gp.tileSize);
        }
        if ("Iron".equals(this.currentWeapon.name)) {
            this.attackUp1 = this.setup("/player/ironUp1", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackUp2 = this.setup("/player/ironUp2", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackDown1 = this.setup("/player/ironDown1", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackDown2 = this.setup("/player/ironDown2", this.gp.tileSize, this.gp.tileSize * 2);
            this.attackLeft1 = this.setup("/player/ironLeft1", this.gp.tileSize * 2, this.gp.tileSize);
            this.attackLeft2 = this.setup("/player/ironLeft2", this.gp.tileSize * 2, this.gp.tileSize);
            this.attackRight1 = this.setup("/player/ironRight1", this.gp.tileSize * 2, this.gp.tileSize);
            this.attackRight2 = this.setup("/player/ironRight2", this.gp.tileSize * 2, this.gp.tileSize);
        }
    }

    public void getGuardImage() {
        this.guardUp = this.setup("/player/NormalUp", this.gp.tileSize, this.gp.tileSize);
        this.guardDown = this.setup("/player/NormalDown", this.gp.tileSize, this.gp.tileSize);
        this.guardLeft = this.setup("/player/NormalLeft", this.gp.tileSize, this.gp.tileSize);
        this.guardRight = this.setup("/player/NormalRight", this.gp.tileSize, this.gp.tileSize);
    }

    @Override

    public void update() {
        // Releasing Space must end a guard even while standing still or being pushed.
        if (!keyH.spacePressed) {
            guarding = false;
            guardCounter = 0;
        }
        if (knockBack) {
            updatePlayerKnockBack();
        } else if (attacking) {
            attacking();
        } else if (keyH.spacePressed) {
            guarding = true;
            guardCounter++;
        } else if (gp.currentMap == main.Temple.MAP && gp.temple.pulled()) {
            if (keyH.upPressed) direction = "up";
            else if (keyH.downPressed) direction = "down";
            else if (keyH.leftPressed) direction = "left";
            else if (keyH.rightPressed) direction = "right";
            if (keyH.enterPressed) {
                attacking = true;
                spriteCounter = 0;
                keyH.enterPressed = false;
                gp.playSE(9);
            }
        } else if (hasMovementOrInteractionInput()) {
            updateMovementAndInteractions();
        }
        tryShoot();
        updatePlayerTimers();
        life = Math.min(life, maxLife);
        water = Math.min(water, maxWater);
        if (!keyH.godModeOn && life <= 0) {
            if (gp.currentMap==main.Temple.MAP) gp.temple.resetCombat();
            gp.stopMusic();
            gp.gameState = gp.gameOverState;
            gp.ui.commandNum = -1;
            gp.playMusic(15);
        }
    }

    private void updatePlayerKnockBack() {
        collisionOn = false;
        gp.cChecker.checkTile(this);
        gp.cChecker.checkObject(this, true);
        gp.cChecker.checkEntity(this, gp.npc);
        gp.cChecker.checkEntity(this, gp.monster);
        gp.cChecker.checkEntity(this, gp.iTile);
        if (collisionOn) {
            finishKnockBack();
        } else {
            moveInDirection(knockBackDirection);
        }
        if (++knockBackCounter == 10) finishKnockBack();
    }

    private boolean hasMovementOrInteractionInput() {
        return keyH.upPressed || keyH.downPressed || keyH.leftPressed
                || keyH.rightPressed || keyH.enterPressed;
    }

    private void updateMovementAndInteractions() {
        if (keyH.upPressed) direction = "up";
        else if (keyH.downPressed) direction = "down";
        else if (keyH.leftPressed) direction = "left";
        else if (keyH.rightPressed) direction = "right";

        collisionOn = false;
        gp.cChecker.checkTile(this);
        pickUpObject(gp.cChecker.checkObject(this, true));
        interactNPC(gp.cChecker.checkEntity(this, gp.npc));
        contactMonster(gp.cChecker.checkEntity(this, gp.monster));
        gp.cChecker.checkEntity(this, gp.iTile);
        gp.eHandler.checkEvent();
        if (!collisionOn && !keyH.enterPressed) moveInDirection(direction);

        if (keyH.enterPressed && !attackCanceled) {
            gp.playSE(9);
            attacking = true;
            spriteCounter = 0;
        }
        attackCanceled = false;
        keyH.enterPressed = false;
        guarding = false;
        guardCounter = 0;
        advanceWalkAnimation(11);
    }

    private void tryShoot() {
        if (!keyH.shotKeyPressed || projectile.alive || shotAvailableCounter != 50
                || !projectile.haveResourse(this)) return;
        Entity[] shots = gp.projectile[gp.currentMap];
        for (int i = 0; i < shots.length; i++) {
            if (shots[i] != null) continue;
            projectile.set(worldX, worldY, direction, true, this);
            projectile.substractResource(this);
            shots[i] = projectile;
            shotAvailableCounter = 0;
            gp.playSE(13);
            return;
        }
        // A full projectile pool must not consume water or leave an invisible live shot.
    }

    private void updatePlayerTimers() {
        if (invincible && ++invincibleCounter > 60) {
            invincible = false;
            transparent = false;
            invincibleCounter = 0;
        }
        if (shotAvailableCounter < 50) shotAvailableCounter++;
    }

    public void pickUpObject(int i) {
        if (i != 999) {
            if (this.gp.obj[this.gp.currentMap][i].type == 7) {
                this.gp.obj[this.gp.currentMap][i].use(this);
                this.gp.obj[this.gp.currentMap][i] = null;
            } else if (this.gp.obj[this.gp.currentMap][i].type == 8) {
                if (this.keyH.enterPressed) {
                    this.attackCanceled = true;
                    this.gp.obj[this.gp.currentMap][i].interact();
                }
            } else {
                String itemName = this.gp.obj[this.gp.currentMap][i].name;
                String text;
                if (this.canObtainItem(this.gp.obj[this.gp.currentMap][i])) {
                    this.gp.playSE(4);
                    this.gp.obj[this.gp.currentMap][i] = null;
                    text = "Got a " + itemName + "!";
                } else {
                    text = "You cannot carry anymore";
                }
                this.gp.ui.addMessage(text);
            }
        }
    }

    public void interactNPC(int i) {
        if (i != 999) {
            if (this.gp.keyH.enterPressed) {
                if (gp.currentMap==main.Temple.MAP && gp.npc[gp.currentMap][i] instanceof NPC_BigRock) return;
                this.attackCanceled = true;
                this.gp.npc[this.gp.currentMap][i].speak();
            }
            this.gp.npc[this.gp.currentMap][i].move(this.direction);
        }
    }

    public void contactMonster(int i) {
        if (i!=999 && gp.currentMap==main.Temple.MAP &&
            (gp.monster[gp.currentMap][i] instanceof monster.MON_Flower ||
             gp.monster[gp.currentMap][i] instanceof monster.MON_Secret)) return;
        if (i != 999 && this.gp.monster[this.gp.currentMap][i] instanceof monster.MON_Tella tella && tella.usesPulse()) return;
        if (i != 999 && this.gp.monster[this.gp.currentMap][i] instanceof monster.MON_Sharky shark) {
            if (!shark.dying && shark.alive) shark.damagePlayer(shark.attack);
            return;
        }
        if (i != 999 && !this.invincible && !this.gp.monster[this.gp.currentMap][i].dying) {
            this.gp.playSE(8);
            int damage = this.gp.monster[this.gp.currentMap][i].attack - this.defense;
            if (damage < 1) {
                damage = 1;
            }
            this.life -= damage;
            this.invincible = true;
            this.transparent = true;
        }
    }

    public void damageMonster(int i, Entity attacker, int attack, int knockBackPower) {
        if (i != 999 && !this.gp.monster[this.gp.currentMap][i].invincible &&
            this.gp.monster[this.gp.currentMap][i].alive && !this.gp.monster[this.gp.currentMap][i].dying) {
            int damage;
            this.gp.playSE(10);
            if (knockBackPower > 0) {
                this.setKnockBack(this.gp.monster[this.gp.currentMap][i], attacker, knockBackPower);
            }
            if (this.gp.monster[this.gp.currentMap][i].offBalance) {
                attack *= 4;
            }
            if ((damage = attack - this.gp.monster[this.gp.currentMap][i].defense) < 0) {
                damage = 0;
            }
            if (this.gp.monster[this.gp.currentMap][i] instanceof monster.MON_Tella tella && tella.usesPulse()) damage = Math.max(1, damage);
            this.gp.monster[this.gp.currentMap][i].life -= damage;
            this.gp.ui.addMessage(damage + " damage!");
            this.gp.monster[this.gp.currentMap][i].invincible = true;
            this.gp.monster[this.gp.currentMap][i].damageReaction();
            if (this.gp.monster[this.gp.currentMap][i].life <= 0) {
                this.gp.monster[this.gp.currentMap][i].dying = true;
                this.gp.ui.addMessage("killed the " + this.gp.monster[this.gp.currentMap][i].name + "!");
                if (gp.monster[gp.currentMap][i] instanceof monster.MON_Secret secret && !secret.claimReward()) return;
                this.gp.ui.addMessage("Exp + " + this.gp.monster[this.gp.currentMap][i].exp + "!");
                this.exp = (int)Math.min(Integer.MAX_VALUE, (long)this.exp + this.gp.monster[this.gp.currentMap][i].exp);
                this.checkLevelUp();
            }
        }
    }

    public void damageInteractiveTile(int i) {
        if (i != 999 && this.gp.iTile[this.gp.currentMap][i].destructible && this.gp.iTile[this.gp.currentMap][i].isCorrectItem(this) && !this.gp.iTile[this.gp.currentMap][i].invincible) {
            this.gp.iTile[this.gp.currentMap][i].playSE();
            --this.gp.iTile[this.gp.currentMap][i].life;
            this.gp.iTile[this.gp.currentMap][i].invincible = true;
            this.generateParticle(this.gp.iTile[this.gp.currentMap][i], this.gp.iTile[this.gp.currentMap][i]);
            if (this.gp.iTile[this.gp.currentMap][i].life == 0) {
                this.gp.iTile[this.gp.currentMap][i].checkDrop();
                this.gp.iTile[this.gp.currentMap][i] = this.gp.iTile[this.gp.currentMap][i].getDestroyedForm();
            }
        }
    }

    public void damageProjectile(int i) {
        if (i != 999) {
            Entity projectile = this.gp.projectile[this.gp.currentMap][i];
            projectile.alive = false;
            this.generateParticle(projectile, projectile);
        }
    }


    public void checkLevelUp() {
        boolean leveledUp = false;
        // Experience is cumulative in the original game: 5, 10, 20, 40, ...
        while (nextLevelExp > 0 && nextLevelExp != Integer.MAX_VALUE && exp >= nextLevelExp) {
            level++;
            maxLife += 2;
            strength++;
            dexterity++;
            leveledUp = true;
            if (nextLevelExp > Integer.MAX_VALUE / 2) {
                nextLevelExp = Integer.MAX_VALUE;
                break;
            }
            nextLevelExp *= 2;
        }
        if (leveledUp) {
            attack = getAttack();
            defense = getDefense();
            gp.playSE(11);
            dialogues[0][0] = "You are level " + level + " now!\nYou feel stronger!";
            startDialogue(this, 0);
        }
    }

    public void selectItem() {
        int itemIndex = this.gp.ui.getItemIndexOnSlot(this.gp.ui.playerSlotCol, this.gp.ui.playerSlotRow);
        if (itemIndex < this.inventory.size()) {
            Entity selectedItem = (Entity)this.inventory.get(itemIndex);
            if (selectedItem.type == 3 || selectedItem.type == 4 || selectedItem.type == 10) {
                this.currentWeapon = selectedItem;
                this.attack = this.getAttack();
                this.getAttackImage();
            }
            if (selectedItem.type == 5) {
                this.currentShield = selectedItem;
                this.defense = this.getDefense();
            }
            if (selectedItem.type == 9) {
                this.currentLight = this.currentLight == selectedItem ? null : selectedItem;
                this.lightUpdated = true;
            }
            if (selectedItem.type == 6 && selectedItem.use(this)) {
                if (selectedItem.amount > 1) {
                    --selectedItem.amount;
                } else {
                    this.inventory.remove(itemIndex);
                }
            }
        }
    }

    public int searchItemInInventory(String itemName) {
        int itemIndex = 999;
        int i = 0;
        while (i < this.inventory.size()) {
            if (((Entity)this.inventory.get((int)i)).name.equals(itemName)) {
                itemIndex = i;
                break;
            }
            ++i;
        }
        return itemIndex;
    }

    public boolean canObtainItem(Entity item) {
        boolean canObtain = false;
        Entity newItem = this.gp.eGenerator.getObject(item.name);
        if (newItem.stackable) {
            int index = this.searchItemInInventory(newItem.name);
            if (index != 999) {
                ++((Entity)this.inventory.get((int)index)).amount;
                canObtain = true;
            } else if (this.inventory.size() < maxinventorySize) {
                this.inventory.add(newItem);
                canObtain = true;
            }
        } else if (this.inventory.size() < maxinventorySize) {
            this.inventory.add(newItem);
            canObtain = true;
        }
        return canObtain;
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        int tempScreenX = this.screenX;
        int tempScreenY = this.screenY;
        switch (this.direction) {
            case "up": {
                if (!this.attacking) {
                    if (this.spriteNum == 1) {
                        image = this.up;
                    }
                    if (this.spriteNum == 2) {
                        image = this.up2;
                    }
                }
                if (this.attacking) {
                    tempScreenY = this.screenY - this.gp.tileSize;
                    if (this.spriteNum == 1) {
                        image = this.attackUp1;
                    }
                    if (this.spriteNum == 2) {
                        image = this.attackUp2;
                    }
                }
                if (!this.guarding) break;
                image = this.guardUp;
                break;
            }
            case "down": {
                if (!this.attacking) {
                    if (this.spriteNum == 1) {
                        image = this.down;
                    }
                    if (this.spriteNum == 2) {
                        image = this.down2;
                    }
                }
                if (this.attacking) {
                    if (this.spriteNum == 1) {
                        image = this.attackDown1;
                    }
                    if (this.spriteNum == 2) {
                        image = this.attackDown2;
                    }
                }
                if (!this.guarding) break;
                image = this.guardDown;
                break;
            }
            case "left": {
                if (!this.attacking) {
                    if (this.spriteNum == 1) {
                        image = this.left;
                    }
                    if (this.spriteNum == 2) {
                        image = this.left2;
                    }
                }
                if (this.attacking) {
                    tempScreenX = this.screenX - this.gp.tileSize;
                    if (this.spriteNum == 1) {
                        image = this.attackLeft1;
                    }
                    if (this.spriteNum == 2) {
                        image = this.attackLeft2;
                    }
                }
                if (!this.guarding) break;
                image = this.guardLeft;
                break;
            }
            case "right": {
                if (!this.attacking) {
                    if (this.spriteNum == 1) {
                        image = this.right;
                    }
                    if (this.spriteNum == 2) {
                        image = this.right2;
                    }
                }
                if (this.attacking) {
                    if (this.spriteNum == 1) {
                        image = this.attackRight1;
                    }
                    if (this.spriteNum == 2) {
                        image = this.attackRight2;
                    }
                }
                if (!this.guarding) break;
                image = this.guardRight;
            }
        }
        if (this.transparent) {
            g2.setComposite(AlphaComposite.getInstance(3, 0.3f));
        }
        if (this.drawing) {
            g2.drawImage(image, tempScreenX, tempScreenY, null);
        }
        g2.setComposite(AlphaComposite.getInstance(3, 1.0f));
    }
}

