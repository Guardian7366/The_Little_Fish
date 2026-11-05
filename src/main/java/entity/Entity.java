/*
 * Decompiled with CFR 0.152.
 */
package entity;

import entity.Particle;
import entity.Projectile;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import main.GamePanel;
import main.SpriteCache;

public class Entity {
    GamePanel gp;
    public BufferedImage up;
    public BufferedImage down;
    public BufferedImage left;
    public BufferedImage right;
    public BufferedImage up2;
    public BufferedImage down2;
    public BufferedImage left2;
    public BufferedImage right2;
    public BufferedImage attackUp1;
    public BufferedImage attackUp2;
    public BufferedImage attackDown1;
    public BufferedImage attackDown2;
    public BufferedImage attackLeft1;
    public BufferedImage attackLeft2;
    public BufferedImage attackRight1;
    public BufferedImage attackRight2;
    public BufferedImage guardUp;
    public BufferedImage guardDown;
    public BufferedImage guardLeft;
    public BufferedImage guardRight;
    public BufferedImage image;
    public BufferedImage image2;
    public BufferedImage image3;
    public Rectangle solidArea = new Rectangle(0, 0, 44, 44);
    public Rectangle attackArea = new Rectangle(0, 0, 0, 0);
    public int solidAreaDefaultX;
    public int solidAreaDefaultY;
    public String[][] dialogues = new String[30][30];
    public boolean collision = false;
    public Entity attacker;
    public Entity linkedEntity;
    public boolean temp = false;
    public int worldX;
    public int worldY;
    public String direction = "down";
    public int spriteNum = 1;
    public int dialogueSet = 0;
    public int dialogueIndex = 0;
    public boolean collisionOn = false;
    public boolean invincible = false;
    public boolean attacking = false;
    public boolean alive = true;
    public boolean dying = false;
    public boolean hpBarOn = false;
    public boolean onPath = false;
    public boolean knockBack = false;
    public String knockBackDirection;
    public boolean guarding = false;
    public boolean transparent = false;
    public boolean offBalance = false;
    public Entity loot;
    public boolean opened = false;
    public boolean inRage = false;
    public boolean sleep = false;
    public boolean drawing = true;
    public int spriteCounter = 0;
    public int actionLockCounter = 0;
    public int invincibleCounter = 0;
    public int shotAvailableCounter = 0;
    int dyingCounter = 0;
    public int hpBarCounter = 0;
    int knockBackCounter = 0;
    public int guardCounter = 0;
    int offBalanceCounter = 0;
    public int speed;
    public String name;
    public int defaultSpeed;
    public int maxLife;
    public int life;
    public int maxWater;
    public int ammo;
    public int water;
    public int level;
    public int strength;
    public int dexterity;
    public int attack;
    public int defense;
    public int exp;
    public int nextLevelExp;
    public int coin;
    public int motion1_duration;
    public int motion2_duration;
    public Entity currentWeapon;
    public Entity currentShield;
    public Entity currentLight;
    public Projectile projectile;
    public boolean boss;
    public ArrayList<Entity> inventory = new ArrayList<>();
    public final int maxinventorySize = 20;
    public int value;
    public int attackValue;
    public int defenseValue;
    public String description = "";
    public int useCost;
    public int price;
    public int knockBackPower = 0;
    public boolean stackable = false;
    public int amount = 1;
    public int lightRadius;
    public int type;
    public final int type_player = 0;
    public final int type_npc = 1;
    public final int type_monster = 2;
    public final int type_bubble = 3;
    public final int type_rock = 4;
    public final int type_shield = 5;
    public final int type_consumable = 6;
    public final int type_pickUpOnly = 7;
    public final int type_obstacle = 8;
    public final int type_light = 9;
    public final int type_iron = 10;

    public Entity(GamePanel gp) {
        this.gp = gp;
    }

    public int getScreenX() {
        int screenX = worldX - gp.cameraX();
        return screenX;
    }

    public int getScreenY() {
        int screenY = worldY - gp.cameraY();
        return screenY;
    }

    public int getLeftX() {
        return this.worldX + this.solidArea.x;
    }

    public int getRightX() {
        return this.worldX + this.solidArea.x + this.solidArea.width;
    }

    public int getTopY() {
        return this.worldY + this.solidArea.y;
    }

    public int getBottomY() {
        return this.worldY + this.solidArea.y + this.solidArea.height;
    }

    public int getCol() {
        return (this.worldX + this.solidArea.x) / this.gp.tileSize;
    }

    public int getRow() {
        return (this.worldY + this.solidArea.y) / this.gp.tileSize;
    }

    public int getCenterX() {
        int centerX = this.worldX + this.left.getWidth() / 2;
        return centerX;
    }

    public int getCenterY() {
        int centerY = this.worldY + this.up.getHeight() / 2;
        return centerY;
    }

    public int getXdistance(Entity target) {
        int xDistance = Math.abs(this.getCenterX() - target.getCenterX());
        return xDistance;
    }

    public int getYdistance(Entity target) {
        int yDistance = Math.abs(this.getCenterY() - target.getCenterY());
        return yDistance;
    }

    public int getTileDistance(Entity target) {
        int tileDistance = (this.getXdistance(target) + this.getYdistance(target)) / this.gp.tileSize;
        return tileDistance;
    }

    public int getGoalCol(Entity target) {
        int goalCol = (target.worldX + target.solidArea.x) / this.gp.tileSize;
        return goalCol;
    }

    public int getGoalRow(Entity target) {
        int goalRow = (target.worldY + target.solidArea.y) / this.gp.tileSize;
        return goalRow;
    }

    public void resetCounter() {
        this.spriteCounter = 0;
        this.actionLockCounter = 0;
        this.invincibleCounter = 0;
        this.shotAvailableCounter = 0;
        this.dyingCounter = 0;
        this.hpBarCounter = 0;
        this.knockBackCounter = 0;
        this.guardCounter = 0;
        this.offBalanceCounter = 0;
    }

    public void setLoot(Entity loot) {
    }

    public void setAction() {
    }

    public void move(String direction) {
    }

    public void damageReaction() {
    }

    public void speak() {
    }

    public void facePlayer() {
        switch (this.gp.player.direction) {
            case "up": {
                this.direction = "down";
                break;
            }
            case "down": {
                this.direction = "up";
                break;
            }
            case "left": {
                this.direction = "right";
                break;
            }
            case "right": {
                this.direction = "left";
            }
        }
    }

    public void startDialogue(Entity entity, int setNum) {
        this.gp.gameState = this.gp.dialogueState;
        this.gp.ui.npc = entity;
        this.dialogueSet = setNum;
    }

    public void checkDrop() {
    }

    public void interact() {
    }

    public void dropItem(Entity droppedItem) {
        int i = 0;
        while (i < this.gp.obj[1].length) {
            if (this.gp.obj[this.gp.currentMap][i] == null) {
                this.gp.obj[this.gp.currentMap][i] = droppedItem;
                this.gp.obj[this.gp.currentMap][i].worldX = this.worldX;
                this.gp.obj[this.gp.currentMap][i].worldY = this.worldY;
                break;
            }
            ++i;
        }
    }

    public Color getparticleColor() {
        Color color = null;
        return color;
    }

    public int getParticleSize() {
        int size = 0;
        return size;
    }

    public int getParticleSpeed() {
        int speed = 0;
        return speed;
    }

    public int getParticleMaxLife() {
        int maxLife = 0;
        return maxLife;
    }

    public void generateParticle(Entity generator, Entity target) {
        Color color = generator.getparticleColor();
        int size = generator.getParticleSize();
        int speed = generator.getParticleSpeed();
        int maxLife = generator.getParticleMaxLife();
        Particle p1 = new Particle(this.gp, target, color, size, speed, maxLife, -2, -1);
        Particle p2 = new Particle(this.gp, target, color, size, speed, maxLife, 2, -1);
        Particle p3 = new Particle(this.gp, target, color, size, speed, maxLife, -2, 1);
        Particle p4 = new Particle(this.gp, target, color, size, speed, maxLife, 2, 1);
        this.gp.particleList.add(p1);
        this.gp.particleList.add(p2);
        this.gp.particleList.add(p3);
        this.gp.particleList.add(p4);
    }

    public void checkCollision() {
        this.collisionOn = false;
        this.gp.cChecker.checkTile(this);
        this.gp.cChecker.checkObject(this, false);
        this.gp.cChecker.checkEntity(this, this.gp.npc);
        this.gp.cChecker.checkEntity(this, this.gp.monster);
        this.gp.cChecker.checkEntity(this, this.gp.iTile);
        boolean contactPlayer = this.gp.cChecker.checkPlayer(this);
        if (this.type == 2 && contactPlayer) {
            this.damagePlayer(this.attack);
        }
    }


    public void update() {
        if (sleep) return;

        if (knockBack) {
            updateKnockBack();
        } else if (attacking) {
            attacking();
        } else {
            setAction();
            checkCollision();
            if (!collisionOn) moveInDirection(direction);
            advanceWalkAnimation(24);
        }
        updateCombatTimers();
    }

    protected void moveInDirection(String movementDirection) {
        switch (movementDirection) {
            case "up" -> worldY -= speed;
            case "down" -> worldY += speed;
            case "left" -> worldX -= speed;
            case "right" -> worldX += speed;
        }
    }

    protected void advanceWalkAnimation(int frameDuration) {
        if (++spriteCounter > frameDuration) {
            spriteNum = spriteNum == 1 ? 2 : 1;
            spriteCounter = 0;
        }
    }

    private void updateKnockBack() {
        checkCollision();
        if (collisionOn) {
            finishKnockBack();
        } else {
            moveInDirection(knockBackDirection);
        }
        if (++knockBackCounter == 10) finishKnockBack();
    }

    protected void finishKnockBack() {
        knockBackCounter = 0;
        knockBack = false;
        speed = defaultSpeed;
    }

    protected void updateCombatTimers() {
        if (invincible && ++invincibleCounter > 20) {
            invincible = false;
            invincibleCounter = 0;
        }
        if (shotAvailableCounter < 70) shotAvailableCounter++;
        if (offBalance && ++offBalanceCounter > 60) {
            offBalance = false;
            offBalanceCounter = 0;
        }
    }

    /** Advances presentation-related lifetimes once per simulation tick, never per draw. */
    public void updateVisualState() {
        if (dying && ++dyingCounter > 40) alive = false;
        if (type != type_monster || boss) return;
        if (invincible) {
            hpBarOn = true;
            hpBarCounter = 0;
        }
        if (hpBarOn && ++hpBarCounter > 360) {
            hpBarOn = false;
            hpBarCounter = 0;
        }
    }

    public void checkAttackOrNot(int rate, int straight, int horizontal) {
        int i;
        boolean targetInRange = false;
        int xDis = this.getXdistance(this.gp.player);
        int yDis = this.getYdistance(this.gp.player);
        switch (this.direction) {
            case "up": {
                if (this.gp.player.getCenterY() >= this.getCenterY() || yDis >= straight || xDis >= horizontal) break;
                targetInRange = true;
                break;
            }
            case "down": {
                if (this.gp.player.getCenterY() <= this.getCenterY() || yDis >= straight || xDis >= horizontal) break;
                targetInRange = true;
                break;
            }
            case "left": {
                if (this.gp.player.getCenterX() >= this.getCenterX() || xDis >= straight || yDis >= horizontal) break;
                targetInRange = true;
                break;
            }
            case "right": {
                if (this.gp.player.getCenterX() <= this.getCenterX() || xDis >= straight || yDis >= horizontal) break;
                targetInRange = true;
            }
        }
        if (targetInRange && (i = new Random().nextInt(rate)) == 0) {
            this.attacking = true;
            this.spriteNum = 1;
            this.spriteCounter = 0;
            this.shotAvailableCounter = 0;
        }
    }

    public void checkShootOrNot(int rate, int shotInterval) {
        int i = new Random().nextInt(rate);
        if (i == 0 && !this.projectile.alive && this.shotAvailableCounter == shotInterval) {
            this.projectile.set(this.worldX, this.worldY, this.direction, true, this);
            int ii = 0;
            while (ii < this.gp.projectile[1].length) {
                if (this.gp.projectile[this.gp.currentMap][ii] == null) {
                    this.gp.projectile[this.gp.currentMap][ii] = this.projectile;
                    break;
                }
                ++ii;
            }
            this.shotAvailableCounter = 0;
        }
    }

    public void checkStartChasingOrNot(Entity target, int distance, int rate) {
        int i;
        if (this.getTileDistance(target) < distance && (i = new Random().nextInt(rate)) == 0) {
            this.onPath = true;
        }
    }

    public void checkStopChasingOrNot(Entity target, int distance, int rate) {
        int i;
        if (this.getTileDistance(target) > distance && (i = new Random().nextInt(rate)) == 0) {
            this.onPath = false;
        }
    }

    public void getRandomDirection(int interval) {
        ++this.actionLockCounter;
        if (this.actionLockCounter > interval) {
            Random random = new Random();
            int i = random.nextInt(100) + 1;
            if (i <= 25) {
                this.direction = "up";
            }
            if (i > 25 && i <= 50) {
                this.direction = "down";
            }
            if (i > 50 && i <= 75) {
                this.direction = "left";
            }
            if (i > 75 && i <= 100) {
                this.direction = "right";
            }
            this.actionLockCounter = 0;
        }
    }

    public void moveTowardPlayer(int interval) {
        ++this.actionLockCounter;
        if (this.actionLockCounter > interval) {
            if (this.getXdistance(this.gp.player) > this.getYdistance(this.gp.player)) {
                this.direction = this.gp.player.getCenterX() < this.getCenterX() ? "left" : "right";
            } else if (this.getXdistance(this.gp.player) < this.getYdistance(this.gp.player)) {
                this.direction = this.gp.player.getCenterY() < this.getCenterY() ? "up" : "down";
            }
            this.actionLockCounter = 0;
        }
    }

    public String getOppositeDirection(String direction) {
        String oppositeDirection = "";
        switch (direction) {
            case "up": {
                oppositeDirection = "down";
                break;
            }
            case "down": {
                oppositeDirection = "up";
                break;
            }
            case "left": {
                oppositeDirection = "right";
                break;
            }
            case "right": {
                oppositeDirection = "left";
            }
        }
        return oppositeDirection;
    }

    public void attacking() {
        block21: {
            ++this.spriteCounter;
            if (this.spriteCounter <= this.motion1_duration) {
                this.spriteNum = 1;
            }
            if (this.spriteCounter <= this.motion1_duration || this.spriteCounter > this.motion2_duration) break block21;
            this.spriteNum = 2;
            var room = gp.extraRooms.active();
            int currentWorldX = this.worldX;
            int currentWorldY = this.worldY;
            int solidAreaWidth = this.solidArea.width;
            int solidAreaHeight = this.solidArea.height;
            switch (this.direction) {
                case "up": {
                    this.worldY -= this.attackArea.height;
                    break;
                }
                case "down": {
                    this.worldY += this.attackArea.height;
                    break;
                }
                case "left": {
                    this.worldX -= this.attackArea.width;
                    break;
                }
                case "right": {
                    this.worldX += this.attackArea.width;
                }
            }
            this.solidArea.width = this.attackArea.width;
            this.solidArea.height = this.attackArea.height;
            if (this.type == 2) {
                if (this.gp.cChecker.checkPlayer(this)) {
                    this.damagePlayer(this.attack);
                }
            } else {
                if (this == gp.player && main.Temple.usesTemple(gp.currentMap) && currentWeapon.type==type_bubble)
                    gp.temple.cutVines(main.Temple.bounds(this,worldX,worldY));
                int monsterIndex = this.gp.cChecker.checkEntity(this, this.gp.monster, room);
                this.gp.player.damageMonster(monsterIndex, this, this.attack, this.currentWeapon.knockBackPower);
                int iTileIndex = this.gp.cChecker.checkEntity(this, this.gp.iTile, room);
                this.gp.player.damageInteractiveTile(iTileIndex);
                int projectileIndex = this.gp.cChecker.checkEntity(this, this.gp.projectile, room);
                this.gp.player.damageProjectile(projectileIndex);
            }
            this.worldX = currentWorldX;
            this.worldY = currentWorldY;
            this.solidArea.width = solidAreaWidth;
            this.solidArea.height = solidAreaHeight;
        }
        if (this.spriteCounter > this.motion2_duration) {
            this.spriteNum = 1;
            this.spriteCounter = 0;
            this.attacking = false;
        }
    }

    public void damagePlayer(int attack) {
        damagePlayer(attack, direction, true);
    }

    protected void damagePlayer(int attack, String attackDirection, boolean recoilOnParry) {
        if (!this.gp.player.invincible) {
            int damage = attack - this.gp.player.defense;
            String canGuardDirection = this.getOppositeDirection(attackDirection);
            if (this.gp.player.guarding && this.gp.player.direction.equals(canGuardDirection)) {
                if (this.gp.player.guardCounter < 10) {
                    damage = 0;
                    this.gp.playSE(19);
                    if (recoilOnParry) {
                        this.setKnockBack(this, this.gp.player, this.knockBackPower);
                        this.offBalance = true;
                        this.spriteCounter = -120;
                    }
                } else {
                    damage = Math.max(0, damage / 3);
                    this.gp.playSE(18);
                }
            } else {
                this.gp.playSE(8);
                if (damage < 1) {
                    damage = 1;
                }
            }
            if (damage != 0) {
                this.gp.player.transparent = true;
                this.setKnockBack(this.gp.player, this, this.knockBackPower);
                this.gp.player.knockBackDirection = attackDirection;
            }
            this.gp.player.life -= damage;
            this.gp.player.invincible = true;
        }
    }

    public void setKnockBack(Entity target, Entity attacker, int knockBackPower) {
        this.attacker = attacker;
        target.knockBackDirection = attacker.direction;
        target.speed += knockBackPower;
        target.knockBack = true;
    }

    public boolean use(Entity entity) {
        return false;
    }

    public boolean inCamera() {
        if (gp.currentMap==main.ExtraRooms.MAP) return gp.extraRooms.visible(this);
        boolean inCamera = false;
        if (this.worldX + this.gp.tileSize * 5 > this.gp.player.worldX - this.gp.player.screenX && this.worldX - this.gp.tileSize < this.gp.player.worldX + this.gp.player.screenX && this.worldY + this.gp.tileSize * 5 > this.gp.player.worldY - this.gp.player.screenY && this.worldY - this.gp.tileSize < this.gp.player.worldY + this.gp.player.screenY) {
            inCamera = true;
        }
        return inCamera;
    }

    public void draw(Graphics2D g2) {
        block34: {
            BufferedImage image = null;
            if (!this.inCamera()) break block34;
            int tempScreenX = this.getScreenX();
            int tempScreenY = this.getScreenY();
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
                    if (!this.attacking) break;
                    tempScreenY = this.getScreenY() - this.up.getHeight();
                    if (this.spriteNum == 1) {
                        image = this.attackUp1;
                    }
                    if (this.spriteNum != 2) break;
                    image = this.attackUp2;
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
                    if (!this.attacking) break;
                    if (this.spriteNum == 1) {
                        image = this.attackDown1;
                    }
                    if (this.spriteNum != 2) break;
                    image = this.attackDown2;
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
                    if (!this.attacking) break;
                    tempScreenX = this.getScreenX() - this.left.getWidth();
                    if (this.spriteNum == 1) {
                        image = this.attackLeft1;
                    }
                    if (this.spriteNum != 2) break;
                    image = this.attackLeft2;
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
                    if (!this.attacking) break;
                    if (this.spriteNum == 1) {
                        image = this.attackRight1;
                    }
                    if (this.spriteNum != 2) break;
                    image = this.attackRight2;
                }
            }
            if (this.invincible) {
                this.changeAlpha(g2, 0.4f);
            }
            if (this.dying) {
                this.dyingAnimation(g2);
            }
            g2.drawImage(image, tempScreenX, tempScreenY, null);
            this.changeAlpha(g2, 1.0f);
        }
    }


    public void dyingAnimation(Graphics2D g2) {
        // Keep the original five-tick flashes; repeated rendering cannot finish a death.
        int flash = Math.max(0, dyingCounter - 1) / 5;
        changeAlpha(g2, flash % 2 == 0 ? 0.0f : 1.0f);
    }

    public void changeAlpha(Graphics2D g2, float alphaValue) {
        g2.setComposite(AlphaComposite.getInstance(3, alphaValue));
    }


    public BufferedImage setup(String imagePath, int width, int height) {
        return SpriteCache.load(imagePath + ".png", width, height);
    }

    public void searchPath(int goalCol, int goalRow) {
        int startCol = (this.worldX + this.solidArea.x) / this.gp.tileSize;
        int startRow = (this.worldY + this.solidArea.y) / this.gp.tileSize;
        this.gp.pFinder.setNodes(startCol, startRow, goalCol, goalRow, this);
        if (this.gp.pFinder.search() && !this.gp.pFinder.pathList.isEmpty()) {
            int nextX = this.gp.pFinder.pathList.get((int)0).col * this.gp.tileSize;
            int nextY = this.gp.pFinder.pathList.get((int)0).row * this.gp.tileSize;
            int enLeftX = this.worldX + this.solidArea.x;
            int enRightX = this.worldX + this.solidArea.x + this.solidArea.width;
            int enTopY = this.worldY + this.solidArea.y;
            int enBottomY = this.worldY + this.solidArea.y + this.solidArea.height;
            if (enTopY > nextY && enLeftX >= nextX && enRightX < nextX + this.gp.tileSize) {
                this.direction = "up";
            } else if (enTopY < nextY && enLeftX >= nextX && enRightX < nextX + this.gp.tileSize) {
                this.direction = "down";
            } else if (enTopY >= nextY && enBottomY < nextY + this.gp.tileSize) {
                if (enLeftX > nextX) {
                    this.direction = "left";
                }
                if (enLeftX < nextX) {
                    this.direction = "right";
                }
            } else if (enTopY > nextY && enLeftX > nextX) {
                this.direction = "up";
                this.checkCollision();
                if (this.collisionOn) {
                    this.direction = "left";
                }
            } else if (enTopY > nextY && enLeftX < nextX) {
                this.direction = "up";
                this.checkCollision();
                if (this.collisionOn) {
                    this.direction = "right";
                }
            } else if (enTopY < nextY && enLeftX > nextX) {
                this.direction = "down";
                this.checkCollision();
                if (this.collisionOn) {
                    this.direction = "left";
                }
            } else if (enTopY < nextY && enLeftX < nextX) {
                this.direction = "down";
                this.checkCollision();
                if (this.collisionOn) {
                    this.direction = "right";
                }
            }
        }
    }

    public int getDetected(Entity user, Entity[][] target, String targetName) {
        int index = 999;
        int nextWorldX = user.getLeftX();
        int nextWorldY = user.getTopY();
        switch (user.direction) {
            case "up": {
                nextWorldY = user.getTopY() - this.gp.player.speed;
                break;
            }
            case "down": {
                nextWorldY = user.getBottomY() + this.gp.player.speed;
                break;
            }
            case "left": {
                nextWorldX = user.getLeftX() - this.gp.player.speed;
                break;
            }
            case "right": {
                nextWorldX = user.getRightX() + this.gp.player.speed;
            }
        }
        int col = nextWorldX / this.gp.tileSize;
        int row = nextWorldY / this.gp.tileSize;
        int i = 0;
        while (i < target[1].length) {
            if (target[this.gp.currentMap][i] != null && target[this.gp.currentMap][i].getCol() == col && target[this.gp.currentMap][i].getRow() == row && target[this.gp.currentMap][i].name.equals(targetName)) {
                index = i;
                break;
            }
            ++i;
        }
        return index;
    }
}

