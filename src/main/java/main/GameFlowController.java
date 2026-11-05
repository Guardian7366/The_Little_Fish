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
            case 8 -> updateTrade();
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

    public void updateDialogue() {
        if (ui.npc == null) return;
        String[] lines = ui.npc.dialogues[ui.npc.dialogueSet];
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

    private void updateTrade() {
        if (ui.npc == null) return;
        if (ui.subState == 0) { ui.npc.dialogueSet = 0; updateDialogue(); }
        if (!gp.keyH.enterPressed) return;
        gp.keyH.enterPressed = false;
        if (ui.subState == 0) {
            if (ui.commandNum == 0) ui.subState = 1;
            else if (ui.commandNum == 1) ui.subState = 2;
            else { ui.commandNum = 0; resetDialogue(); ui.npc.startDialogue(ui.npc, 1); }
        } else if (ui.subState == 1) {
            int index = ui.getItemIndexOnSlot(ui.npcSlotCol, ui.npcSlotRow);
            if (index >= ui.npc.inventory.size()) return;
            Entity item = ui.npc.inventory.get(index);
            if (item.price > gp.player.coin) tradeError(2);
            else if (gp.player.canObtainItem(item)) gp.player.coin -= item.price;
            else tradeError(3);
        } else if (ui.subState == 2) {
            int index = ui.getItemIndexOnSlot(ui.playerSlotCol, ui.playerSlotRow);
            if (index >= gp.player.inventory.size()) return;
            Entity item = gp.player.inventory.get(index);
            if (item == gp.player.currentWeapon || item == gp.player.currentShield || item == gp.player.currentLight) {
                ui.commandNum = 0; tradeError(4);
            } else {
                if (item.amount > 1) item.amount--;
                else gp.player.inventory.remove(index);
                gp.player.coin += item.price / 2;
            }
        }
    }

    private void tradeError(int dialogue) {
        ui.subState = 0;
        resetDialogue();
        ui.npc.startDialogue(ui.npc, dialogue);
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
