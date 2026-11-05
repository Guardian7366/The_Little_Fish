/*
 * Decompiled with CFR 0.152.
 */
package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import main.GamePanel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KeyHandler
implements KeyListener {
    GamePanel gp;
    private record Input(int code, boolean pressed) {}
    private final ConcurrentLinkedQueue<Input> pending = new ConcurrentLinkedQueue<>();

    public void processPendingInput() {
        Input input;
        while ((input = pending.poll()) != null) {
            if (input.code() == -1) clearHeldKeys();
            else if (input.pressed()) handlePressed(input.code());
            else handleReleased(input.code());
        }
    }

    public void releaseAll() {
        if (gp.gameThread != null && Thread.currentThread() != gp.gameThread) pending.add(new Input(-1, false));
        else clearHeldKeys();
    }

    private void clearHeldKeys() {
        upPressed = downPressed = leftPressed = rightPressed = false;
        gp.extraRooms.ending.released(15);
        enterPressed = shotKeyPressed = spacePressed = false;
    }
    public boolean upPressed;
    public boolean downPressed;
    public boolean leftPressed;
    public boolean rightPressed;
    public boolean enterPressed;
    public boolean shotKeyPressed;
    public boolean spacePressed;
    boolean showDebugText = false;
    public boolean godModeOn = false;

    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gp.gameThread != null) pending.add(new Input(e.getKeyCode(), true));
        else handlePressed(e.getKeyCode());
    }

    private void handlePressed(int code) {
        if (this.gp.gameState == this.gp.titleState) {
            this.titleState(code);
        } else if (this.gp.gameState == this.gp.playState) {
            this.playState(code);
        } else if (this.gp.gameState == this.gp.pauseState) {
            this.pauseState(code);
        } else {
            int n = this.gp.gameState;
            this.gp.getClass();
            if (n == 3 || this.gp.gameState == this.gp.cutsceneState) {
                this.dialogueState(code);
            } else if (this.gp.gameState == this.gp.characterState) {
                this.characterState(code);
            } else if (this.gp.gameState == this.gp.optionsState) {
                this.optionsState(code);
            } else if (this.gp.gameState == this.gp.gameOverState) {
                this.gameOverState(code);
            } else if (this.gp.gameState == this.gp.tradeState) {
                this.tradeState(code);
            } else {
                int n2 = this.gp.gameState;
                this.gp.getClass();
                if (n2 == 10) {
                    this.mapState(code);
                }
            }
        }
    }

    public void titleState(int code) {
        if (this.gp.ui.titleScreenState == 0) {
            if (code == KeyEvent.VK_W) {
                --this.gp.ui.commandNum;
                if (this.gp.ui.commandNum < 0) {
                    this.gp.ui.commandNum = 2;
                }
            }
            if (code == KeyEvent.VK_S) {
                ++this.gp.ui.commandNum;
                if (this.gp.ui.commandNum > 2) {
                    this.gp.ui.commandNum = 0;
                }
            }
            if (code == KeyEvent.VK_ENTER) {
                if (this.gp.ui.commandNum == 0) {
                    this.gp.resetGame(true);
                    this.gp.gameState = this.gp.playState;
                    this.gp.playSE(7);
                    this.gp.playMusic(6);
                }
                if (this.gp.ui.commandNum == 1) {
                    this.gp.playSE(7);
                    if (this.gp.saveLoad.tryLoad()) {
                        this.gp.flow.reset();
                        this.gp.csManager.reset();
                        this.gp.gameState = this.gp.playState;
                        this.gp.playAreaMusic();
                        if (!this.gp.saveLoad.getLastWarning().isEmpty())
                            this.gp.ui.addMessage("Original save imported; resume at the starting area.");
                    } else {
                        this.gp.ui.notice = "Could not load: " + this.gp.saveLoad.getLastError();
                    }
                }
                if (this.gp.ui.commandNum == 2) {
                    this.gp.playSE(7);
                    System.exit(0);
                }
            }
        }
    }

    public void playState(int code) {
        if (code == KeyEvent.VK_W) {
            this.upPressed = true;
        }
        if (code == KeyEvent.VK_S) {
            this.downPressed = true;
        }
        if (code == KeyEvent.VK_A) {
            this.leftPressed = true;
        }
        if (code == KeyEvent.VK_D) {
            this.rightPressed = true;
        }
        if (code == KeyEvent.VK_P || code == KeyEvent.VK_Y) {
            this.enterPressed = false;
            this.gp.gameState = this.gp.pauseState;
        }
        if (code == KeyEvent.VK_C) {
            this.enterPressed = false;
            this.gp.gameState = this.gp.characterState;
        }
        if (code == KeyEvent.VK_ENTER) {
            this.enterPressed = true;
        }
        if (code == KeyEvent.VK_F) {
            this.shotKeyPressed = true;
        }
        if (code == KeyEvent.VK_ESCAPE) {
            this.enterPressed = false;
            this.gp.ui.commandNum = 0;
            this.gp.ui.subState = 0;
            this.gp.gameState = this.gp.optionsState;
        }
        if (code == KeyEvent.VK_M) {
            this.enterPressed = false;
            this.gp.getClass();
            this.gp.gameState = 10;
        }
        if (code == KeyEvent.VK_SPACE) {
            this.spacePressed = true;
        }
        if (code == KeyEvent.VK_X) {
            this.gp.map.miniMapOn = !this.gp.map.miniMapOn;
        }
        if (code == KeyEvent.VK_T) {
            if (!this.showDebugText) {
                this.showDebugText = true;
            } else if (this.showDebugText) {
                this.showDebugText = false;
            }
        }
        if (code == KeyEvent.VK_R) this.gp.tileM.reloadCurrentMap();
        if (code == KeyEvent.VK_G) {
            if (!this.godModeOn) {
                this.godModeOn = true;
            } else if (this.godModeOn) {
                this.godModeOn = false;
            }
        }
    }

    public void pauseState(int code) {
        if (code == KeyEvent.VK_P || code == KeyEvent.VK_Y) {
            this.gp.gameState = this.gp.playState;
        }
    }

    public void dialogueState(int code) {
        if (gp.gameState == gp.cutsceneState && !gp.csManager.isDialogueActive()) return;
        if (code == KeyEvent.VK_ENTER) {
            this.enterPressed = true;
        }
    }

    public void characterState(int code) {
        if (code == KeyEvent.VK_C) {
            this.gp.gameState = this.gp.playState;
        }
        if (code == KeyEvent.VK_ENTER) {
            this.gp.player.selectItem();
        }
        this.playerInventory(code);
    }

    public void optionsState(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            this.gp.config.saveConfig();
            this.gp.gameState = this.gp.playState;
        }
        if (code == KeyEvent.VK_ENTER) {
            this.enterPressed = true;
        }
        int maxCommandNum = 0;
        switch (this.gp.ui.subState) {
            case 0: {
                maxCommandNum = 5;
                break;
            }
            case 3: {
                maxCommandNum = 1;
            }
        }
        if (code == KeyEvent.VK_W) {
            --this.gp.ui.commandNum;
            this.gp.playSE(2);
            if (this.gp.ui.commandNum < 0) {
                this.gp.ui.commandNum = maxCommandNum;
            }
        }
        if (code == KeyEvent.VK_S) {
            ++this.gp.ui.commandNum;
            this.gp.playSE(2);
            if (this.gp.ui.commandNum > maxCommandNum) {
                this.gp.ui.commandNum = 0;
            }
        }
        if (code == KeyEvent.VK_A && this.gp.ui.subState == 0) {
            if (this.gp.ui.commandNum == 1 && this.gp.music.volumeScale > 0) {
                --this.gp.music.volumeScale;
                this.gp.music.checkVolume();
                this.gp.playSE(2);
                this.gp.config.saveConfig();
            }
            if (this.gp.ui.commandNum == 2 && this.gp.se.volumeScale > 0) {
                --this.gp.se.volumeScale;
                this.gp.playSE(2);
                this.gp.config.saveConfig();
            }
        }
        if (code == KeyEvent.VK_D && this.gp.ui.subState == 0) {
            if (this.gp.ui.commandNum == 1 && this.gp.music.volumeScale < 5) {
                ++this.gp.music.volumeScale;
                this.gp.music.checkVolume();
                this.gp.playSE(2);
                this.gp.config.saveConfig();
            }
            if (this.gp.ui.commandNum == 2 && this.gp.se.volumeScale < 5) {
                ++this.gp.se.volumeScale;
                this.gp.playSE(2);
                this.gp.config.saveConfig();
            }
        }
    }

    public void gameOverState(int code) {
        if (code == KeyEvent.VK_W) {
            --this.gp.ui.commandNum;
            if (this.gp.ui.commandNum < 0) {
                this.gp.ui.commandNum = 1;
            }
            this.gp.playSE(2);
        }
        if (code == KeyEvent.VK_S) {
            ++this.gp.ui.commandNum;
            if (this.gp.ui.commandNum > 1) {
                this.gp.ui.commandNum = 0;
            }
            this.gp.playSE(2);
        }
        if (code == KeyEvent.VK_ENTER) {
            if (this.gp.ui.commandNum == 0) {
                this.gp.gameState = this.gp.playState;
                this.gp.stopMusic();
                this.gp.currentMap = 0;
                this.gp.resetGame(false);
                this.gp.playMusic(6);
            } else if (this.gp.ui.commandNum == 1) {
                this.gp.gameState = this.gp.titleState;
                this.gp.stopMusic();
                this.gp.resetGame(true);
                this.gp.currentMap = 0;
            }
        }
    }

    public void tradeState(int code) {
        if (code == KeyEvent.VK_ENTER) {
            this.enterPressed = true;
        }
        if (this.gp.ui.subState == 0) {
            if (code == KeyEvent.VK_W) {
                --this.gp.ui.commandNum;
                if (this.gp.ui.commandNum < 0) {
                    this.gp.ui.commandNum = 2;
                }
                this.gp.playSE(2);
            }
            if (code == KeyEvent.VK_S) {
                ++this.gp.ui.commandNum;
                if (this.gp.ui.commandNum > 2) {
                    this.gp.ui.commandNum = 0;
                }
                this.gp.playSE(2);
            }
        }
        if (this.gp.ui.subState == 1) {
            this.npcInventory(code);
            if (code == KeyEvent.VK_ESCAPE) {
                this.gp.ui.subState = 0;
            }
        }
        if (this.gp.ui.subState == 2) {
            this.playerInventory(code);
            if (code == KeyEvent.VK_ESCAPE) {
                this.gp.ui.subState = 0;
            }
        }
    }

    public void mapState(int code) {
        if (code == KeyEvent.VK_M) {
            this.gp.gameState = this.gp.playState;
        }
    }

    public void playerInventory(int code) {
        if (code == KeyEvent.VK_W && this.gp.ui.playerSlotRow != 0) {
            --this.gp.ui.playerSlotRow;
            this.gp.playSE(2);
        }
        if (code == KeyEvent.VK_A && this.gp.ui.playerSlotCol != 0) {
            --this.gp.ui.playerSlotCol;
            this.gp.playSE(2);
        }
        if (code == KeyEvent.VK_S && this.gp.ui.playerSlotRow != 3) {
            ++this.gp.ui.playerSlotRow;
            this.gp.playSE(2);
        }
        if (code == KeyEvent.VK_D && this.gp.ui.playerSlotCol != 4) {
            ++this.gp.ui.playerSlotCol;
            this.gp.playSE(2);
        }
    }

    public void npcInventory(int code) {
        if (code == KeyEvent.VK_W && this.gp.ui.npcSlotRow != 0) {
            --this.gp.ui.npcSlotRow;
            this.gp.playSE(2);
        }
        if (code == KeyEvent.VK_A && this.gp.ui.npcSlotCol != 0) {
            --this.gp.ui.npcSlotCol;
            this.gp.playSE(2);
        }
        if (code == KeyEvent.VK_S && this.gp.ui.npcSlotRow != 3) {
            ++this.gp.ui.npcSlotRow;
            this.gp.playSE(2);
        }
        if (code == KeyEvent.VK_D && this.gp.ui.npcSlotCol != 4) {
            ++this.gp.ui.npcSlotCol;
            this.gp.playSE(2);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gp.gameThread != null) pending.add(new Input(e.getKeyCode(), false));
        else handleReleased(e.getKeyCode());
    }

    private void handleReleased(int code) {
        if (code == KeyEvent.VK_W) {
            this.upPressed = false;
            gp.extraRooms.ending.released(1);
        }
        if (code == KeyEvent.VK_S) {
            this.downPressed = false;
            gp.extraRooms.ending.released(2);
        }
        if (code == KeyEvent.VK_A) {
            this.leftPressed = false;
            gp.extraRooms.ending.released(4);
        }
        if (code == KeyEvent.VK_D) {
            this.rightPressed = false;
            gp.extraRooms.ending.released(8);
        }
        if (code == KeyEvent.VK_F) {
            this.shotKeyPressed = false;
        }
        if (code == KeyEvent.VK_SPACE) {
            this.spacePressed = false;
        }
    }
}

