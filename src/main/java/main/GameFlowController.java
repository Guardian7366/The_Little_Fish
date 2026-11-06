package main;

import entity.Entity;

/** Advances menus, dialogue and transitions on simulation ticks, never while painting. */
public final class GameFlowController {
    private final GamePanel gp;
    private final UI ui;

    public GameFlowController(GamePanel gp) { this.gp = gp; this.ui = gp.ui; }

    public void update() {
        switch (gp.gameState) {
            case 1 -> updateMessages();
            case 3 -> updateDialogue();
            case 5 -> updateOptions();
            case 7 -> updateTransition();
            case 8 -> gp.shop.update();
            case 9 -> updateSleep();
            default -> { }
        }
    }

    private void updateMessages() {
        for (int i = ui.message.size() - 1; i >= 0; i--) {
            int age = ui.messageCounter.get(i) + 1;
            if (age > 120) { ui.message.remove(i); ui.messageCounter.remove(i); }
            else ui.messageCounter.set(i, age);
        }
    }

    private int dialogueGroup;

    public void beginDialogue(Entity speaker,int group,int firstPage) {
        resetDialogue();
        ui.npc=speaker;
        dialogueGroup=group;
        speaker.dialogueIndex=firstPage;
    }

    public void updateDialogue() {
        if (ui.npc == null) return;
        String[] lines = ui.npc.dialogues[dialogueGroup];
        String line = ui.npc.dialogueIndex < lines.length ? lines[ui.npc.dialogueIndex] : null;
        if (line == null) {
            resetDialogue();
            if (gp.gameState == gp.dialogueState) gp.gameState = gp.playState;
            else if (gp.gameState == gp.cutsceneState) gp.csManager.scenePhase++;
            gp.keyH.enterPressed = false;
            return;
        }
        if (ui.charIndex < line.length()) {
            gp.playSE(21);
            ui.combinedText += line.charAt(ui.charIndex++);
            ui.currentDialogue = ui.combinedText;
        }
        if (gp.keyH.enterPressed && (gp.gameState == gp.dialogueState || gp.gameState == gp.cutsceneState)) {
            ui.charIndex = 0;
            ui.combinedText = "";
            ui.npc.dialogueIndex++;
            gp.keyH.enterPressed = false;
        }
    }

    private void updateOptions() {
        if (!gp.keyH.enterPressed) return;
        gp.keyH.enterPressed = false;
        switch (ui.subState) {
            case 0 -> {
                switch (ui.commandNum) {
                    case 0 -> { gp.fullScreenOn = !gp.fullScreenOn; ui.subState = 1; }
                    case 3 -> { ui.subState = 2; ui.commandNum = 0; }
                    case 4 -> { ui.subState = 3; ui.commandNum = 0; }
                    case 5 -> { gp.gameState = gp.playState; ui.commandNum = 0; }
                    default -> { }
                }
                gp.config.saveConfig();
            }
            case 1 -> ui.subState = 0;
            case 2 -> { ui.subState = 0; ui.commandNum = 3; }
            case 3 -> {
                if (ui.commandNum == 0) {
                    gp.resetGame(true);
                    gp.gameState = gp.titleState;
                    ui.commandNum = 0;
                } else ui.commandNum = 4;
                ui.subState = 0;
            }
            default -> { }
        }
    }

    private void updateTransition() {
        if (++ui.counter < 50) return;
        ui.counter = 0;
        if (gp.currentMap == SixthZone.MAP && gp.eHandler.tempMap == FifthZone.MAP &&
            SixthZone.gateIntact(gp.iTile)) {
            FifthZone.restoreIronReward(gp.player, gp.obj);
        }
        gp.currentMap = gp.eHandler.tempMap;
        gp.player.worldX = gp.tileSize * gp.eHandler.tempCol;
        gp.player.worldY = gp.tileSize * gp.eHandler.tempRow;
        gp.eHandler.previousEventX = gp.player.worldX;
        gp.eHandler.previousEventY = gp.player.worldY;
        gp.changeArea();
        gp.prepareAreaLighting();
        gp.gameState = gp.playState;
    }

    public void resetDialogue() {
        ui.charIndex = 0;
        ui.combinedText = "";
        ui.currentDialogue = "";
        if (ui.npc != null) ui.npc.dialogueIndex = 0;
    }

    private void updateSleep() {
        ui.counter++;
        if (ui.counter < 120) {
            gp.eManager.lighting.filterAlpha = Math.min(1f, gp.eManager.lighting.filterAlpha + .01f);
        } else {
            gp.eManager.lighting.filterAlpha = Math.max(0f, gp.eManager.lighting.filterAlpha - .01f);
            if (gp.eManager.lighting.filterAlpha == 0f) {
                ui.counter = 0;
                gp.eManager.lighting.dayState = gp.eManager.lighting.day;
                gp.eManager.lighting.dayCounter = 0;
                gp.gameState = gp.playState;
                gp.player.getImage();
            }
        }
    }

    public void reset() {
        ui.counter = ui.subState = ui.commandNum = 0;
        ui.notice = "";
        ui.playerSlotCol = ui.playerSlotRow = ui.npcSlotCol = ui.npcSlotRow = 0;
        ui.message.clear(); ui.messageCounter.clear();
        resetDialogue(); ui.npc = null;
    }
}
