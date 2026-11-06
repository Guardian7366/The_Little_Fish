/*
 * Decompiled with CFR 0.152.
 */
package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Exit
extends Entity {
    GamePanel gp;
    public static final String objName = "Exit";

    public OBJ_Exit(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.name = objName;
        this.type = 7;
        this.down = this.setup("/objects/Diana", gp.tileSize, gp.tileSize);
        this.collision = true;
        this.setDialogues();
    }

    public void setDialogues() {
        dialogues[0][0] = "D: The water's quiet. I thought I'd\nfeel different.";
        dialogues[0][1] = "Echo: This was a game about a fish.\nThen you put your memories here.";
        dialogues[0][2] = "Echo: When the fish moved, I thought\nit was you.";
        dialogues[0][3] = "D: I kept looking for a way to put it\nall back.";
        dialogues[0][4] = "D: She used to sleep by the window.\nThe cat I brought home from the\nrain.";
        dialogues[0][5] = "D: She grew old. One morning she\nstopped breathing in her sleep.";
        dialogues[0][6] = "D: I remember holding her. Then I\nremember how empty my hands felt.";
        dialogues[0][7] = "D: I can carry something again. I\ndon't have to leave her here.";
    }

    @Override
    public boolean use(Entity entity) {
        this.gp.gameState = this.gp.cutsceneState;
        this.gp.csManager.sceneNum = this.gp.csManager.ending;
        return true;
    }
}

