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
        this.dialogues[0][0] = "D: I finally found you";
        this.dialogues[0][1] = "Da: Thanks, but I'm not real you know?";
        this.dialogues[0][2] = "D: ???";
        this.dialogues[0][3] = "Da: Remember D, the past that \nyou hate so much";
        this.dialogues[0][4] = "Suddenly you remember everything...";
        this.dialogues[0][5] = "El pasado es pasado, el futuro una \nposibilidad y el presente el \u00fanico \nmomento que sientes y vives...";
    }

    @Override
    public boolean use(Entity entity) {
        this.gp.gameState = this.gp.cutsceneState;
        this.gp.csManager.sceneNum = this.gp.csManager.ending;
        return true;
    }
}

