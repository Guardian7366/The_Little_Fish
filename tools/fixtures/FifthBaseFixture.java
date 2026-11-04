import main.GamePanel;
import data.SaveLoad;
import object.OBJ_Iron;
import tile_interactive.IT_Rocky;
import java.nio.file.*;

/** Compile against ca4b3ad sources, not current classes: actual pre-fifth serialized schema. */
public final class FifthBaseFixture {
    static class Quiet extends GamePanel {public void playMusic(int i){} public void stopMusic(){} public void playSE(int i){}}
    public static void main(String[] args)throws Exception {
        Quiet gp=new Quiet();gp.setupGame();gp.currentArea=gp.dungeons;
        gp.player.worldX=25*48;gp.player.worldY=23*48;gp.player.coin=37;
        gp.player.inventory.add(new OBJ_Iron(gp));gp.obj[5][8].opened=true;gp.obj[5][8].down=gp.obj[5][8].image2;
        gp.monster[3][40].life=7;gp.monster[3][41]=null;gp.monster[3][33]=null;gp.monster[3][36].life=3;
        gp.monster[1][6].life=2;
        gp.iTile[3][4]=new IT_Rocky(gp,25,28);gp.iTile[3][11]=new IT_Rocky(gp,13,42);gp.iTile[3][12].life=1;
        for(int map:new int[]{3,2}){
            gp.currentMap=map;
            SaveLoad save=new SaveLoad(gp,Path.of("build/fifth-base-fixtures/map-"+map+"/save-v1.dat"));
            if(!save.trySave())throw new AssertionError(save.getLastError());
        }
        gp.stopGame();System.out.println("BASE ca4b3ad fixtures written from pre-fifth Java classes");
    }
}
