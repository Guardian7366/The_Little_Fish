/*
 * Decompiled with CFR 0.152.
 */
package main;

import ai.PathFinder;
import data.Progress;
import data.SaveLoad;
import entity.Entity;
import entity.Player;
import environment.EnvironmentManager;
import environment.OceanLayers;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JPanel;
import main.AssetSetter;
import main.CollisionChecker;
import main.Config;
import main.CutsceneManager;
import main.EntityGenerator;
import main.EventHandler;
import main.KeyHandler;
import main.Main;
import main.Sound;
import main.UI;
import tile.Map;
import tile.TileManager;
import tile_interactive.InteractiveTile;

public class GamePanel
extends JPanel
implements Runnable {
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = 48;
    public final int maxScreenCol = 20;
    public final int maxScreenRow = 12;
    public final int screenWidht = 960;
    public final int screenHeight = 576;
    Color testing = new Color(0, 0, 139);
    public int maxWorldCol;
    public int maxWorldRow;
    public final int maxMap = 10;
    public int currentMap = 0;
    int screenWidth2 = 960;
    int screenHeight2 = 576;
    BufferedImage tempScreen;
    Graphics2D g2;
    public boolean fullScreenOn = false;
    int FPS = 60;
    public TileManager tileM = new TileManager(this);
    public KeyHandler keyH = new KeyHandler(this);
    Sound music = new Sound();
    Sound se = new Sound(8);
    public CollisionChecker cChecker = new CollisionChecker(this);
    public AssetSetter aSetter = new AssetSetter(this);
    public UI ui = new UI(this);
    public final OceanLayers ocean = new OceanLayers();
    public final FirstZoneGuide firstZoneGuide = new FirstZoneGuide(this);
    public EventHandler eHandler = new EventHandler(this);
    Config config = new Config(this);
    public PathFinder pFinder = new PathFinder(this);
    EnvironmentManager eManager = new EnvironmentManager(this);
    Map map = new Map(this);
    SaveLoad saveLoad = new SaveLoad(this);
    public EntityGenerator eGenerator = new EntityGenerator(this);
    public CutsceneManager csManager = new CutsceneManager(this);
    volatile Thread gameThread;
    private final Object frameLock = new Object();
    public final GameFlowController flow = new GameFlowController(this);
    public Player player = new Player(this, this.keyH);
    public Entity[][] obj = new Entity[10][30];
    public Entity[][] npc = new Entity[10][40];
    public Entity[][] monster = new Entity[10][100];
    public InteractiveTile[][] iTile = new InteractiveTile[10][50];
    public Entity[][] projectile = new Entity[10][50];
    public ArrayList<Entity> particleList = new ArrayList<>();
    ArrayList<Entity> entityList = new ArrayList<>();
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int dialogueState = 3;
    public final int characterState = 4;
    public final int optionsState = 5;
    public final int gameOverState = 6;
    public final int transitionState = 7;
    public final int tradeState = 8;
    public final int sleepState = 9;
    public final int mapState = 10;
    public final int cutsceneState = 11;
    public boolean bossBattleOn = false;
    public int currentArea;
    public int nextArea;
    public final int outside = 50;
    public final int indoor = 51;
    public final int dungeons = 52;

    public GamePanel() {
        this.setPreferredSize(new Dimension(960, 576));
        this.setBackground(this.testing);
        this.setDoubleBuffered(true);
        this.addKeyListener(this.keyH);
        this.setFocusable(true);
        addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent event) { keyH.releaseAll(); }
        });
    }

    public void setupGame() {
        this.aSetter.setObject();
        this.aSetter.setNPC();
        this.aSetter.setMonster();
        this.aSetter.setInteractiveTile();
        this.saveLoad.captureInitialWorld();
        this.eManager.setup();
        this.gameState = 0;
        this.currentArea = 50;
        this.tempScreen = new BufferedImage(960, 576, 2);
        this.g2 = (Graphics2D)this.tempScreen.getGraphics();
        if (this.fullScreenOn) {
            this.setFullScreen();
        }
    }

    public void resetGame(boolean restart) {
        stopMusic();
        keyH.releaseAll();
        flow.reset();
        csManager.reset();
        currentArea = nextArea = outside;
        currentMap = 0;
        bossBattleOn = false;
        removeTempEntity();
        for (Entity[] mapProjectiles : projectile) Arrays.fill(mapProjectiles, null);
        particleList.clear();
        player.projectile.alive = false;
        player.drawing = true;
        if (restart) {
            firstZoneGuide.reset();
            Progress.GodFishDefeated = false;
            player.setDefaultValues();
            player.setItems();
            for (Entity[] mapObjects : obj) Arrays.fill(mapObjects, null);
            for (Entity[] mapNpcs : npc) Arrays.fill(mapNpcs, null);
            for (InteractiveTile[] mapTiles : iTile) Arrays.fill(mapTiles, null);
            aSetter.setObject();
            aSetter.setNPC();
            aSetter.setInteractiveTile();
            if (eManager.lighting != null) eManager.lighting.resetDay();
        }
        player.setDefaultPositions();
        player.restoreStatus();
        for (Entity[] mapMonsters : monster) Arrays.fill(mapMonsters, null);
        aSetter.setMonster();
        eHandler = new EventHandler(this);
    }

    public void setFullScreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        gd.setFullScreenWindow(Main.window);
        this.screenWidth2 = Main.window.getWidth();
        this.screenHeight2 = Main.window.getHeight();
    }

    public void startGameThread() {
        if (gameThread != null) return;
        gameThread = new Thread(this, "little-fish-game");
        gameThread.start();
    }

    @Override
    public void run() {
        final long frameNanos = 1_000_000_000L / FPS;
        long deadline = System.nanoTime();
        try {
            while (gameThread != null && !Thread.currentThread().isInterrupted()) {
                update();
                synchronized (frameLock) { drawToTempScreen(); }
                drawToScreen();
                deadline += frameNanos;
                long remaining;
                while (gameThread != null && (remaining = deadline - System.nanoTime()) > 0) {
                    LockSupport.parkNanos(remaining);
                    if (Thread.currentThread().isInterrupted()) return;
                }
                // Do not accumulate an unlimited backlog after a breakpoint or suspended laptop.
                if (System.nanoTime() - deadline > frameNanos * 5) deadline = System.nanoTime();
            }
        } finally {
            music.close();
            se.close();
        }
    }

    public void stopGame() {
        Thread running = gameThread;
        gameThread = null;
        if (running != null) running.interrupt();
        music.close();
        se.close();
    }

    public void update() {
        keyH.processPendingInput();
        if (gameState == titleState) ui.titleBackground.update();
        if (this.gameState == 1) {
            this.player.update();
            if(currentMap==FifthZone.MAP) ocean.fifth.update();
            if (currentMap == 0 || currentMap == 1 || currentMap == 2 || currentMap == FourthZone.MAP) ocean.update();
            firstZoneGuide.update();
            int i = 0;
            while (i < this.npc[1].length) {
                if (this.npc[this.currentMap][i] != null) {
                    this.npc[this.currentMap][i].update();
                }
                ++i;
            }
            i = 0;
            while (i < this.monster[1].length) {
                if (this.monster[this.currentMap][i] != null) {
                    if (this.monster[this.currentMap][i].alive && !this.monster[this.currentMap][i].dying) {
                        this.monster[this.currentMap][i].update();
                    }
                    if (!this.monster[this.currentMap][i].alive) {
                        this.monster[this.currentMap][i].checkDrop();
                        this.monster[this.currentMap][i] = null;
                    }
                }
                ++i;
            }
            i = 0;
            while (i < this.projectile[1].length) {
                if (this.projectile[this.currentMap][i] != null) {
                    if (this.projectile[this.currentMap][i].alive) {
                        this.projectile[this.currentMap][i].update();
                    }
                    if (!this.projectile[this.currentMap][i].alive) {
                        this.projectile[this.currentMap][i] = null;
                    }
                }
                ++i;
            }
            for (i = particleList.size() - 1; i >= 0; i--) {
                Entity particle = particleList.get(i);
                if (particle.alive) particle.update();
                if (!particle.alive) particleList.remove(i);
            }
            i = 0;
            while (i < this.iTile[1].length) {
                if (this.iTile[this.currentMap][i] != null) {
                    this.iTile[this.currentMap][i].update();
                }
                ++i;
            }
            this.eManager.update();
        }
        if (gameState != titleState && gameState != mapState && gameState != pauseState) {
            for (Entity entity : monster[currentMap]) if (entity != null) entity.updateVisualState();
        }
        csManager.update();
        flow.update();
    }

    public void drawToTempScreen() {
        g2.setColor(testing);
        g2.fillRect(0, 0, screenWidht, screenHeight);
        long drawStart = 0L;
        if (this.keyH.showDebugText) {
            drawStart = System.nanoTime();
        }
        if (this.gameState == 0) {
            this.ui.draw(this.g2);
        } else if (this.gameState == 10) {
            this.map.drawFullMapScreen(this.g2);
        } else {
            this.ocean.drawBackground(this.g2, this);
            this.tileM.drawLayered(this.g2);
            int i = 0;
            while (i < this.iTile[1].length) {
                if (this.iTile[this.currentMap][i] != null) {
                    this.iTile[this.currentMap][i].draw(this.g2);
                }
                ++i;
            }
            this.entityList.add(this.player);
            i = 0;
            while (i < this.npc[1].length) {
                if (this.npc[this.currentMap][i] != null) {
                    this.entityList.add(this.npc[this.currentMap][i]);
                }
                ++i;
            }
            i = 0;
            while (i < this.obj[1].length) {
                if (this.obj[this.currentMap][i] != null) {
                    this.entityList.add(this.obj[this.currentMap][i]);
                }
                ++i;
            }
            i = 0;
            while (i < this.monster[1].length) {
                if (this.monster[this.currentMap][i] != null) {
                    this.entityList.add(this.monster[this.currentMap][i]);
                }
                ++i;
            }
            i = 0;
            while (i < this.projectile[1].length) {
                if (this.projectile[this.currentMap][i] != null) {
                    this.entityList.add(this.projectile[this.currentMap][i]);
                }
                ++i;
            }
            i = 0;
            while (i < this.particleList.size()) {
                if (this.particleList.get(i) != null) {
                    this.entityList.add(this.particleList.get(i));
                }
                ++i;
            }
            Collections.sort(this.entityList, new Comparator<Entity>(){

                @Override
                public int compare(Entity e1, Entity e2) {
                    int result = Integer.compare(e1.worldY, e2.worldY);
                    return result;
                }
            });
            i = 0;
            while (i < this.entityList.size()) {
                this.entityList.get(i).draw(this.g2);
                ++i;
            }
            this.entityList.clear();
            this.ocean.drawForeground(this.g2, this);
            this.eManager.draw(this.g2);
            this.map.drawMiniMap(this.g2);
            this.csManager.draw(this.g2);
            this.ui.draw(this.g2);
            this.firstZoneGuide.draw(this.g2);
        }
        if (this.keyH.showDebugText) {
            long drawEnd = System.nanoTime();
            long passed = drawEnd - drawStart;
            this.g2.setFont(new Font("Arial", 0, 20));
            this.g2.setColor(Color.white);
            int x = 10;
            int y = 400;
            int lineHeight = 20;
            this.g2.drawString("WorldX" + this.player.worldX, x, y);
            this.g2.drawString("WorldY" + this.player.worldY, x, y += lineHeight);
            this.g2.drawString("COL" + (this.player.worldX + this.player.solidArea.x) / 48, x, y += lineHeight);
            this.g2.drawString("ROW" + (this.player.worldY + this.player.solidArea.y) / 48, x, y += lineHeight);
            this.g2.drawString("DrawTime: " + passed, x, y += lineHeight);
            this.g2.drawString("God Mode:" + this.keyH.godModeOn, x, y += lineHeight);
        }
    }

    public void drawToScreen() { repaint(); }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        synchronized (frameLock) {
            if (tempScreen != null) {
                Graphics2D screen = (Graphics2D) graphics.create();
                if (gameState == titleState) {
                    screen.setColor(Color.black); screen.fillRect(0, 0, getWidth(), getHeight());
                    double scale = Math.min(getWidth() / 960.0, getHeight() / 576.0);
                    int width = (int)Math.round(960 * scale), height = (int)Math.round(576 * scale);
                    screen.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                        java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    screen.drawImage(tempScreen, (getWidth()-width)/2, (getHeight()-height)/2, width, height, null);
                } else screen.drawImage(tempScreen, 0, 0, getWidth(), getHeight(), null);
                screen.dispose();
            }
        }
    }

    public void playAreaMusic() {
        playMusic(bossBattleOn ? 23 : currentArea == dungeons ? 22 : currentArea == indoor ? 0 : 6);
    }

    public void playMusic(int i) {
        this.music.requestPlay(i, true);
    }

    public void stopMusic() {
        this.music.stop();
    }

    public void playSE(int i) {
        this.se.requestPlay(i, false);
    }

    public void changeArea() {
        // Off-map projectiles no longer receive updates; release the reusable player shot.
        for (Entity[] mapProjectiles : projectile) Arrays.fill(mapProjectiles, null);
        player.projectile.alive = false;
        particleList.clear();
        if (this.nextArea != this.currentArea) {
            this.stopMusic();
            if (this.nextArea == 50) {
                this.playMusic(6);
            }
            if (this.nextArea == 51) {
                this.playMusic(0);
            }
            if (this.nextArea == 52) {
                this.playMusic(22);
            }
        }
        this.currentArea = this.nextArea;
        for (Entity[] mapMonsters : monster) Arrays.fill(mapMonsters, null);
        this.aSetter.setMonster();
    }

    public void removeTempEntity() {
        int mapNum = 0;
        while (mapNum < 10) {
            int i = 0;
            while (i < this.obj[1].length) {
                if (this.obj[mapNum][i] != null && this.obj[mapNum][i].temp) {
                    this.obj[mapNum][i] = null;
                }
                ++i;
            }
            ++mapNum;
        }
    }
}
