/*
 * Decompiled with CFR 0.152.
 */
package main;

import entity.Entity;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import main.GamePanel;
import object.OBJ_Coin;
import object.OBJ_Heart;
import object.OBJ_Key;
import object.OBJ_WaterMana;

public class UI {
    GamePanel gp;
    final TitleBackground titleBackground = new TitleBackground();
    Graphics2D g2;
    public Font purisaB;
    public Font kong;
    Font arial_40;
    Font arial_80B;
    BufferedImage keyImage;
    BufferedImage coin;
    BufferedImage fullHeart;
    BufferedImage halfHeart;
    BufferedImage voidHeart;
    BufferedImage water_full;
    BufferedImage water_blank;
    public boolean messageOn = false;
    ArrayList<String> message = new ArrayList();
    ArrayList<Integer> messageCounter = new ArrayList();
    public String notice = "";
    public boolean gameFinished = false;
    public String currentDialogue = "";
    public int commandNum = 0;
    public int titleScreenState = 0;
    public int playerSlotCol = 0;
    public int playerSlotRow = 0;
    public int npcSlotCol = 0;
    public int npcSlotRow = 0;
    int subState = 0;
    int counter = 0;
    public Entity npc;
    int charIndex = 0;
    String combinedText = "";
    double playTime;
    DecimalFormat dFormat = new DecimalFormat("#0.00");

    public UI(GamePanel gp) {
        this.gp = gp;
        this.arial_40 = new Font("Arial", 0, 40);
        this.arial_80B = new Font("Arial", 0, 70);
        this.purisaB = this.arial_40;
        this.kong = this.arial_40;
        try (InputStream purisa = UI.class.getResourceAsStream("/font/Purisa.ttf");
             InputStream pixel = UI.class.getResourceAsStream("/font/kongtext.ttf")) {
            this.purisaB = Font.createFont(Font.TRUETYPE_FONT, purisa);
            this.kong = Font.createFont(Font.TRUETYPE_FONT, pixel);
        } catch (FontFormatException | IOException ex) {
            System.err.println("Could not load game fonts: " + ex.getMessage());
        }
        OBJ_Key key = new OBJ_Key(gp);
        this.keyImage = key.image;
        OBJ_Heart heart = new OBJ_Heart(gp);
        this.fullHeart = heart.image;
        this.halfHeart = heart.image2;
        this.voidHeart = heart.image3;
        OBJ_WaterMana water = new OBJ_WaterMana(gp);
        this.water_full = water.image;
        this.water_blank = water.image2;
        OBJ_Coin Coins = new OBJ_Coin(gp);
        this.coin = Coins.down;
    }

    public void addMessage(String text) {
        this.message.add(text);
        this.messageCounter.add(0);
    }

    public void draw(Graphics2D g2) {
        this.g2 = g2;
        g2.setFont(this.purisaB);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.white);
        if (this.gp.gameState == this.gp.titleState) {
            this.drawTitleScreen();
            if (!this.notice.isEmpty()) {
                g2.setFont(this.purisaB.deriveFont(17f));
                g2.setColor(Color.WHITE);
                g2.drawString(this.notice, 24, this.gp.screenHeight - 22);
            }
        }
        if (this.gp.gameState == this.gp.playState) {
            if (gp.currentMap==FinalArena.MAP && gp.bossBattleOn) drawArenaResources();
            else this.drawPlayerLife();
            this.drawMonsterLife();
            this.drawMessage();
        }
        if (this.gp.gameState == this.gp.pauseState) {
            this.drawPauseScreen();
        }
        int n = this.gp.gameState;
        this.gp.getClass();
        if (n == 3) {
            this.drawDialogueScreen();
        }
        if (this.gp.gameState == this.gp.characterState) {
            this.drawCharacterScreen();
            this.drawInventory(this.gp.player, true);
        }
        if (this.gp.gameState == this.gp.optionsState) {
            this.drawOptionsState();
        }
        if (this.gp.gameState == this.gp.gameOverState) {
            this.drawGameOverScreen();
        }
        if (this.gp.gameState == this.gp.transitionState) {
            this.drawTransition();
        }
        if (this.gp.gameState == this.gp.tradeState) {
            gp.shop.draw(g2);
        }
        if (this.gp.gameState == this.gp.sleepState) {
            this.drawSleepScreen();
        }
    }

    public void drawMonsterLife() {
        int i = 0;
        while (i < this.gp.monster[1].length) {
            Entity monster = this.gp.monster[this.gp.currentMap][i];
            if (monster instanceof monster.MON_GodFish godFish) godFish.drawStatus(g2);
            else if (monster != null && monster.inCamera()) {
                if (monster.hpBarOn && !monster.boss) {
                    double oneScale = (double)this.gp.tileSize / (double)monster.maxLife;
                    double hpBarValue = oneScale * (double)monster.life;
                    this.g2.setColor(new Color(35, 35, 35));
                    this.g2.fillRect(monster.getScreenX() - 1, monster.getScreenY() - 16, this.gp.tileSize + 2, 12);
                    this.g2.setColor(new Color(255, 0, 30));
                    this.g2.fillRect(monster.getScreenX(), monster.getScreenY() - 15, (int)hpBarValue, 10);

                } else if (monster.boss) {
                    double oneScale = (double)this.gp.tileSize * 8.0 / (double)monster.maxLife;
                    double hpBarValue = oneScale * (double)monster.life;
                    int x = this.gp.screenWidht / 2 - this.gp.tileSize * 4;
                    int y = this.gp.tileSize * 10;
                    this.g2.setColor(new Color(35, 35, 35));
                    this.g2.fillRect(x - 1, y - 1, this.gp.tileSize * 8 + 2, 22);
                    this.g2.setColor(new Color(255, 0, 30));
                    this.g2.fillRect(x, y, (int)hpBarValue, 20);
                    this.g2.setFont(this.kong.deriveFont(1, 18.0f));
                    this.g2.setColor(Color.white);
                    this.g2.drawString(monster.name, x + 4, y - 10);
                }
            }
            ++i;
        }
    }

    public void drawMessage() {
        int messageX = this.gp.tileSize;
        int messageY = this.gp.tileSize * 4;
        this.g2.setFont(this.kong.deriveFont(1, 10.0f));
        int i = 0;
        while (i < this.message.size()) {
            if (this.message.get(i) != null) {
                this.g2.setColor(Color.black);
                this.g2.drawString(this.message.get(i), messageX + 2, messageY + 2);
                this.g2.setColor(Color.white);
                this.g2.drawString(this.message.get(i), messageX, messageY);
                messageY += 50;
            }
            ++i;
        }
    }

    private void drawArenaResources() {
        g2.setColor(new Color(11,18,30,230));
        g2.fillRoundRect(14,14,185,70,12,12);
        g2.setFont(purisaB.deriveFont(19f));
        g2.setColor(new Color(255,162,167));
        g2.drawString("HP     " + gp.player.life + " / " + gp.player.maxLife,27,42);
        g2.setColor(new Color(155,216,255));
        g2.drawString("Water  " + gp.player.water + " / " + gp.player.maxWater,27,69);
    }

    public void drawPlayerLife() {
        int x = this.gp.tileSize / 2;
        int y = this.gp.tileSize / 2;
        int i = 0;
        while (i < this.gp.player.maxLife / 2) {
            this.g2.drawImage((Image)this.voidHeart, x, y, null);
            ++i;
            x += this.gp.tileSize;
        }
        x = this.gp.tileSize / 2;
        y = this.gp.tileSize / 2;
        i = 0;
        while (i < this.gp.player.life) {
            this.g2.drawImage((Image)this.halfHeart, x, y, null);
            if (++i < this.gp.player.life) {
                this.g2.drawImage((Image)this.fullHeart, x, y, null);
            }
            ++i;
            x += this.gp.tileSize;
        }
        x = this.gp.tileSize / 2 - 5;
        y = (int)((double)this.gp.tileSize * 1.5);
        i = 0;
        while (i < this.gp.player.maxWater) {
            this.g2.drawImage((Image)this.water_blank, x, y, null);
            ++i;
            x += 35;
        }
        x = this.gp.tileSize / 2 - 5;
        y = (int)((double)this.gp.tileSize * 1.5);
        i = 0;
        while (i < this.gp.player.water) {
            this.g2.drawImage((Image)this.water_full, x, y, null);
            ++i;
            x += 35;
        }
    }

    private void drawTitleText(String text, int x, int y) {
        g2.setColor(new Color(3, 14, 24));
        g2.drawString(text, x-2, y); g2.drawString(text, x+2, y);
        g2.drawString(text, x, y-2); g2.drawString(text, x, y+2);
        g2.setColor(Color.white); g2.drawString(text, x, y);
    }

    public void drawTitleScreen() {
        titleBackground.draw(g2);
        g2.setColor(new Color(0, 12, 27, 48));
        g2.fillRect(0, 0, gp.screenWidht, gp.screenHeight);
        g2.setColor(new Color(0, 12, 27, 95));
        g2.fillRoundRect(56, 58, 848, 117, 24, 24);
        g2.fillRoundRect(286, 354, 388, 182, 24, 24);
        if (this.titleScreenState == 0) {
            this.g2.setFont(this.g2.getFont().deriveFont(1, 80.0f));
            String text = "The Little Fish";
            int x = this.getXforCenteredText(text);
            int y = this.gp.tileSize * 3;
            this.g2.setColor(Color.black);
            this.g2.drawString(text, x + 5, y + 5);
            this.g2.setColor(Color.white);
            this.g2.drawString(text, x, y);
            x = this.gp.screenWidht / 2 - this.gp.tileSize * 2 / 2;
            this.g2.drawImage(this.gp.player.right, x, y += this.gp.tileSize * 2, this.gp.tileSize * 2, this.gp.tileSize * 2, null);
            this.g2.setFont(this.g2.getFont().deriveFont(1, 40.0f));
            text = "NEW GAME";
            x = this.getXforCenteredText(text);
            y = (int)((double)y + (double)this.gp.tileSize * 3.5);
            drawTitleText(text, x, y);
            if (this.commandNum == 0) {
                drawTitleText(">", x - this.gp.tileSize, y);
            }
            text = "LOAD GAME";
            x = this.getXforCenteredText(text);
            drawTitleText(text, x, y += this.gp.tileSize);
            if (this.commandNum == 1) {
                drawTitleText(">", x - this.gp.tileSize, y);
            }
            text = "EXIT";
            x = this.getXforCenteredText(text);
            drawTitleText(text, x, y += this.gp.tileSize);
            if (this.commandNum == 2) {
                drawTitleText(">", x - this.gp.tileSize, y);
            }
        } else if (this.titleScreenState == 1) {
            this.g2.setColor(Color.white);
            this.g2.setFont(this.g2.getFont().deriveFont(40.0f));
            String text = "Select your skin";
            int x = this.getXforCenteredText(text);
            int y = this.gp.tileSize * 3;
            drawTitleText(text, x, y);
            text = "NORMAL";
            x = this.getXforCenteredText(text);
            drawTitleText(text, x, y += this.gp.tileSize * 3);
            if (this.commandNum == 0) {
                drawTitleText(">", x - this.gp.tileSize, y);
            }
            text = "SOSA";
            x = this.getXforCenteredText(text);
            drawTitleText(text, x, y += this.gp.tileSize);
            if (this.commandNum == 1) {
                drawTitleText(">", x - this.gp.tileSize, y);
            }
            text = "KING";
            x = this.getXforCenteredText(text);
            drawTitleText(text, x, y += this.gp.tileSize);
            if (this.commandNum == 2) {
                drawTitleText(">", x - this.gp.tileSize, y);
            }
            text = "Back";
            x = this.getXforCenteredText(text);
            drawTitleText(text, x, y += this.gp.tileSize * 2);
            if (this.commandNum == 3) {
                drawTitleText(">", x - this.gp.tileSize, y);
            }
        }
    }

    public void drawPauseScreen() {
        this.g2.setFont(this.g2.getFont().deriveFont(0, 80.0f));
        String text = "PAUSED";
        int x = this.getXforCenteredText(text);
        int y = this.gp.screenHeight / 2;
        this.g2.drawString(text, x, y);
    }

    public void drawDialogueScreen() {
        int x = this.gp.tileSize * 3;
        boolean finalIntro = gp.csManager.sceneNum==gp.csManager.GodFish;
        int y = finalIntro ? 395 : 68;
        int width = this.gp.screenWidht - this.gp.tileSize * 6;
        int height = finalIntro ? 111 : this.gp.tileSize * 4;
        this.drawSubWindow(x, y, width, height);
        this.g2.setFont(this.g2.getFont().deriveFont(0, 23.0f));
        x += this.gp.tileSize;
        y += this.gp.tileSize;
        String[] stringArray = this.currentDialogue.split("\n");
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String line = stringArray[n2];
            this.g2.drawString(line, x, y);
            y += 40;
            ++n2;
        }
    }

    public void drawCharacterScreen() {
        int frameX = this.gp.tileSize * 2;
        int frameY = this.gp.tileSize;
        int frameWidth = this.gp.tileSize * 5;
        int frameHeight = this.gp.tileSize * 10;
        this.drawSubWindow(frameX, frameY, frameWidth, frameHeight);
        this.g2.setColor(Color.white);
        this.g2.setFont(this.kong.deriveFont(15.0f));
        int textX = frameX + 20;
        int textY = frameY + this.gp.tileSize;
        int lineHeight = 34;
        this.g2.drawString("Level", textX, textY);
        this.g2.drawString("Life", textX, textY += 34);
        this.g2.drawString("Water", textX, textY += 34);
        this.g2.drawString("Strength", textX, textY += 34);
        this.g2.drawString("Dexterity", textX, textY += 34);
        this.g2.drawString("Attack", textX, textY += 34);
        this.g2.drawString("Defense", textX, textY += 34);
        this.g2.drawString("Exp", textX, textY += 34);
        this.g2.drawString("Next Level", textX, textY += 34);
        this.g2.drawString("Coins", textX, textY += 34);
        this.g2.drawString("Weapon", textX, textY += 54);
        this.g2.drawString("Shield", textX, textY += 54);
        textY += 34;
        int tailX = frameX + frameWidth - 30;
        textY = frameY + this.gp.tileSize;
        String value = String.valueOf(this.gp.player.level);
        textX = this.getXforAligntToRightText(value, tailX);
        this.g2.drawString(value, textX, textY);
        value = String.valueOf(this.gp.player.life + "/" + this.gp.player.maxLife);
        textX = this.getXforAligntToRightText(value, tailX);
        this.g2.drawString(value, textX, textY += 34);
        value = String.valueOf(this.gp.player.water + "/" + this.gp.player.maxWater);
        textX = this.getXforAligntToRightText(value, tailX);
        this.g2.drawString(value, textX, textY += 34);
        value = String.valueOf(this.gp.player.strength);
        textX = this.getXforAligntToRightText(value, tailX);
        this.g2.drawString(value, textX, textY += 34);
        value = String.valueOf(this.gp.player.dexterity);
        textX = this.getXforAligntToRightText(value, tailX);
        this.g2.drawString(value, textX, textY += 34);
        value = String.valueOf(this.gp.player.attack);
        textX = this.getXforAligntToRightText(value, tailX);
        this.g2.drawString(value, textX, textY += 34);
        value = String.valueOf(this.gp.player.defense);
        textX = this.getXforAligntToRightText(value, tailX);
        this.g2.drawString(value, textX, textY += 34);
        value = String.valueOf(this.gp.player.exp);
        textX = this.getXforAligntToRightText(value, tailX);
        this.g2.drawString(value, textX, textY += 34);
        value = String.valueOf(this.gp.player.nextLevelExp);
        textX = this.getXforAligntToRightText(value, tailX);
        this.g2.drawString(value, textX, textY += 34);
        value = String.valueOf(this.gp.player.coin);
        textX = this.getXforAligntToRightText(value, tailX);
        this.g2.drawString(value, textX, textY += 34);
        this.g2.drawImage((Image)this.gp.player.currentWeapon.down, tailX - this.gp.tileSize, (textY += 34) - 14, null);
        this.g2.drawImage((Image)this.gp.player.currentShield.down, tailX - this.gp.tileSize, (textY += this.gp.tileSize) - 14, null);
    }

    public void drawInventory(Entity entity, boolean cursor) {
        int frameX = 0;
        int frameY = 0;
        int frameWidth = 0;
        int frameHeight = 0;
        int slotCol = 0;
        int slotRow = 0;
        if (entity == this.gp.player) {
            frameX = this.gp.tileSize * 12;
            frameY = this.gp.tileSize;
            frameWidth = this.gp.tileSize * 6;
            frameHeight = this.gp.tileSize * 5;
            slotCol = this.playerSlotCol;
            slotRow = this.playerSlotRow;
        } else {
            frameX = this.gp.tileSize * 2;
            frameY = this.gp.tileSize;
            frameWidth = this.gp.tileSize * 6;
            frameHeight = this.gp.tileSize * 5;
            slotCol = this.npcSlotCol;
            slotRow = this.npcSlotRow;
        }
        this.drawSubWindow(frameX, frameY, frameWidth, frameHeight);
        int slotXstart = frameX + 20;
        int slotYstart = frameY + 20;
        int slotX = slotXstart;
        int slotY = slotYstart;
        int slotSize = this.gp.tileSize + 3;
        int i = 0;
        while (i < entity.inventory.size()) {
            if (entity.inventory.get(i) == entity.currentWeapon || entity.inventory.get(i) == entity.currentShield || entity.inventory.get(i) == entity.currentLight) {
                this.g2.setColor(new Color(170, 90, 190));
                this.g2.fillRoundRect(slotX, slotY, this.gp.tileSize, this.gp.tileSize, 10, 10);
            }
            this.g2.drawImage((Image)entity.inventory.get((int)i).down, slotX, slotY, null);
            if (entity == this.gp.player && entity.inventory.get((int)i).amount > 1) {
                this.g2.setFont(this.kong.deriveFont(20.0f));
                String s = "" + entity.inventory.get((int)i).amount;
                int amountX = this.getXforAligntToRightText(s, slotX + 44);
                int amountY = slotY + this.gp.tileSize;
                this.g2.setColor(new Color(60, 60, 60));
                this.g2.drawString(s, amountX, amountY);
                this.g2.setColor(Color.white);
                this.g2.drawString(s, amountX - 3, amountY - 3);
            }
            slotX += slotSize;
            if (i == 4 || i == 9 || i == 14) {
                slotX = slotXstart;
                slotY += slotSize;
            }
            ++i;
        }
        if (cursor) {
            int cursorX = slotXstart + slotSize * slotCol;
            int cursorY = slotYstart + slotSize * slotRow;
            int cursorWidth = this.gp.tileSize;
            int cursorHeight = this.gp.tileSize;
            this.g2.setColor(Color.white);
            this.g2.setStroke(new BasicStroke(3.0f));
            this.g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 10, 10);
            int dFrameX = frameX;
            int dFrameY = frameY + frameHeight;
            int dFrameWidth = frameWidth;
            int dFrameHeight = this.gp.tileSize * 3;
            int textX = dFrameX + 20;
            int textY = dFrameY + this.gp.tileSize;
            this.g2.setFont(this.kong.deriveFont(10.0f));
            int itemIndex = this.getItemIndexOnSlot(slotCol, slotRow);
            if (itemIndex < entity.inventory.size()) {
                this.drawSubWindow(dFrameX, dFrameY, dFrameWidth, dFrameHeight);
                String[] stringArray = entity.inventory.get((int)itemIndex).description.split("\n");
                int n = stringArray.length;
                int n2 = 0;
                while (n2 < n) {
                    String line = stringArray[n2];
                    this.g2.drawString(line, textX, textY);
                    textY += 32;
                    ++n2;
                }
            }
        }
    }

    public void drawGameOverScreen() {
        this.g2.setColor(new Color(0, 0, 0, 150));
        this.g2.fillRect(0, 0, this.gp.screenWidht, this.gp.screenHeight);
        this.g2.setFont(this.purisaB.deriveFont(1, 80.0f));
        String text = "Game Over...";
        this.g2.setColor(Color.black);
        int x = this.getXforCenteredText(text);
        int y = this.gp.tileSize * 4;
        this.g2.drawString(text, x, y);
        this.g2.setColor(Color.white);
        this.g2.drawString(text, x - 4, y - 4);
        this.g2.setFont(this.purisaB.deriveFont(40.0f));
        text = "Retry?";
        x = this.getXforCenteredText(text);
        this.g2.drawString(text, x, y += this.gp.tileSize * 4);
        if (this.commandNum == 0) {
            this.g2.drawString(">", x - 40, y);
        }
        text = "Quit";
        x = this.getXforCenteredText(text);
        this.g2.drawString(text, x, y += 70);
        if (this.commandNum == 1) {
            this.g2.drawString(">", x - 40, y);
        }
    }

    public void drawOptionsState() {
        this.g2.setColor(Color.white);
        this.g2.setFont(this.kong.deriveFont(15.0f));
        int frameX = this.gp.tileSize * 6;
        int frameY = this.gp.tileSize;
        int frameWidth = this.gp.tileSize * 8;
        int frameHeight = this.gp.tileSize * 10;
        this.drawSubWindow(frameX, frameY, frameWidth, frameHeight);
        switch (this.subState) {
            case 0: {
                this.options_top(frameX, frameY);
                break;
            }
            case 1: {
                this.option_fullScreenNotification(frameX, frameY);
                break;
            }
            case 2: {
                this.options_control(frameX, frameY);
                break;
            }
            case 3: {
                this.options_endGameConfirmation(frameX, frameY);
            }
        }
    }

    public void options_top(int frameX, int frameY) {
        String text = "Options";
        int textX = this.getXforCenteredText(text);
        int textY = frameY + this.gp.tileSize;
        this.g2.drawString(text, textX, textY);
        textX = frameX + this.gp.tileSize;
        this.g2.drawString("Full Screen", textX, textY += this.gp.tileSize * 2);
        if (this.commandNum == 0) {
            this.g2.drawString(">", textX - 25, textY);

        }
        this.g2.drawString("Music", textX, textY += this.gp.tileSize);
        if (this.commandNum == 1) {
            this.g2.drawString(">", textX - 25, textY);
        }
        this.g2.drawString("SE", textX, textY += this.gp.tileSize);
        if (this.commandNum == 2) {
            this.g2.drawString(">", textX - 25, textY);
        }
        this.g2.drawString("Control", textX, textY += this.gp.tileSize);
        if (this.commandNum == 3) {
            this.g2.drawString(">", textX - 25, textY);

        }
        this.g2.drawString("End Game", textX, textY += this.gp.tileSize);
        if (this.commandNum == 4) {
            this.g2.drawString(">", textX - 25, textY);

        }
        this.g2.drawString("Back", textX, textY += this.gp.tileSize * 2);
        if (this.commandNum == 5) {
            this.g2.drawString(">", textX - 25, textY);

        }
        textX = frameX + (int)((double)this.gp.tileSize * 4.5) + 24;
        textY = frameY + this.gp.tileSize * 2 + 24;
        this.g2.setStroke(new BasicStroke(3.0f));
        this.g2.drawRect(textX, textY, 24, 24);
        if (this.gp.fullScreenOn) {
            this.g2.fillRect(textX, textY, 24, 24);
        }
        this.g2.drawRect(textX, textY += this.gp.tileSize, 100, 24);
        int volumeWidth = 20 * this.gp.music.volumeScale;
        this.g2.fillRect(textX, textY, volumeWidth, 24);
        this.g2.drawRect(textX, textY += this.gp.tileSize, 100, 24);
        volumeWidth = 20 * this.gp.se.volumeScale;
        this.g2.fillRect(textX, textY, volumeWidth, 24);
    }

    public void option_fullScreenNotification(int frameX, int frameY) {
        int textX = frameX + 24;
        int textY = frameY + this.gp.tileSize * 3;
        String notification = "The change will take \neffect after restaring \nthe game :p.";
        String[] stringArray = notification.split("\n");
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String line = stringArray[n2];
            this.g2.drawString(line, textX, textY);
            textY += 40;
            ++n2;
        }
        textY = frameY + this.gp.tileSize * 9;
        this.g2.drawString("Back", textX + 24, textY);
        if (this.commandNum == 0) {
            this.g2.drawString(">", textX, textY);

        }
    }

    public void options_control(int frameX, int frameY) {
        String text = "Control";
        int textX = this.getXforCenteredText(text);
        int textY = frameY + this.gp.tileSize;
        this.g2.drawString(text, textX, textY);
        textX = frameX + this.gp.tileSize;
        this.g2.drawString("Move", textX, textY += this.gp.tileSize);
        this.g2.drawString("Confirm/Attack", textX, textY += this.gp.tileSize);
        this.g2.drawString("Shoot/Cast", textX, textY += this.gp.tileSize);
        this.g2.drawString("Inventory", textX, textY += this.gp.tileSize);
        this.g2.drawString("Pause", textX, textY += this.gp.tileSize);
        this.g2.drawString("Options", textX, textY += this.gp.tileSize);
        textY += this.gp.tileSize;
        textX = frameX + this.gp.tileSize * 6;
        textY = frameY + this.gp.tileSize * 2;
        this.g2.drawString("WASD", textX, textY);
        this.g2.drawString("ENTER", textX, textY += this.gp.tileSize);
        this.g2.drawString("F", textX, textY += this.gp.tileSize);
        this.g2.drawString("C", textX, textY += this.gp.tileSize);
        this.g2.drawString("P", textX, textY += this.gp.tileSize);
        this.g2.drawString("ESC", textX, textY += this.gp.tileSize);
        textY += this.gp.tileSize;
        textX = frameX + this.gp.tileSize;
        textY = frameY + this.gp.tileSize * 9;
        this.g2.drawString("Back", textX, textY);
        textY += this.gp.tileSize;
        if (this.commandNum == 0) {
            this.g2.drawString(">", textX - 25, textY - 45);

        }
    }

    public void options_endGameConfirmation(int frameX, int frameY) {
        int textX = frameX + this.gp.tileSize;
        int textY = frameY + this.gp.tileSize;
        String notification = "Quit the game and \nreturn to the \ntitle screeen?";
        String[] stringArray = notification.split("\n");
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String line = stringArray[n2];
            this.g2.drawString(line, textX, textY);
            textY += 40;
            ++n2;
        }
        String text = "Yes";
        textX = this.getXforCenteredText(text);
        this.g2.drawString(text, textX, textY += this.gp.tileSize * 3);
        if (this.commandNum == 0) {
            this.g2.drawString(">", textX - 25, textY);

        }
        text = "No";
        textX = this.getXforCenteredText(text);
        this.g2.drawString(text, textX, textY += this.gp.tileSize);
        if (this.commandNum == 1) {
            this.g2.drawString(">", textX - 25, textY);

        }
    }

    public void drawTransition() {
        this.g2.setColor(new Color(0, 0, 0, Math.min(255, this.counter * 5)));
        this.g2.fillRect(0, 0, this.gp.screenWidht, this.gp.screenHeight);
    }

    public void drawSleepScreen() {
        // Lighting is rendered by EnvironmentManager; sleeping advances in GameFlowController.
    }

    public int getItemIndexOnSlot(int slotCol, int slotRow) {
        int itemIndex = slotCol + slotRow * 5;
        return itemIndex;
    }

    public void drawSubWindow(int x, int y, int width, int height) {
        Color c = new Color(0, 0, 0, 220);
        this.g2.setColor(c);
        this.g2.fillRoundRect(x, y, width, height, 35, 35);
        c = new Color(255, 255, 255);
        this.g2.setColor(c);
        this.g2.setStroke(new BasicStroke(5.0f));
        this.g2.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25);
    }

    public int getXforCenteredText(String text) {
        int length = (int)this.g2.getFontMetrics().getStringBounds(text, this.g2).getWidth();
        int x = this.gp.screenWidht / 2 - length / 2;
        return x;
    }

    public int getXforAligntToRightText(String text, int tailX) {
        int length = (int)this.g2.getFontMetrics().getStringBounds(text, this.g2).getWidth();
        int x = tailX - length;
        return x;
    }
}

