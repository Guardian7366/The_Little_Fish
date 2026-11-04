import main.GamePanel;
import main.FifthZone;
import data.SaveLoad;
import object.OBJ_Coin;
import tile_interactive.IT_Rocky;
import java.nio.file.*;

/** Generate before this correction, against the preserved local revision-1 compiled classes. */
public final class FifthRevisionOneFixture {
    static class Quiet extends GamePanel {public void playMusic(int i){} public void stopMusic(){} public void playSE(int i){}}
    public static void main(String[] args)throws Exception {
        if(FifthZone.class.getField("REVISION").getInt(null)!=1)throw new AssertionError("Requires preserved revision-1 classes");
        for(int[] p:new int[][]{{3,16,43},{3,12,44},{3,18,45},{3,25,23},{2,22,7},{4,24,44},{5,9,9}}) {
            Quiet gp=new Quiet();gp.setupGame();gp.currentMap=p[0];gp.currentArea=gp.dungeons;
            gp.player.worldX=p[1]*48;gp.player.worldY=p[2]*48;gp.player.coin=73;
            if(p[0]==3&&p[1]==25) {gp.obj[5][8].opened=true;gp.obj[5][8].down=gp.obj[5][8].image2;}
            gp.monster[3][40].life=7;gp.monster[3][41]=null;gp.iTile[3][13]=new IT_Rocky(gp,20,26);
            gp.obj[3][0]=new OBJ_Coin(gp);gp.obj[3][0].worldX=18*48;gp.obj[3][0].worldY=45*48;
            Path file=Path.of("build/fifth-revision1-fixtures/"+p[0]+"-"+p[1]+"-"+p[2]+"/save-v1.dat");
            SaveLoad save=new SaveLoad(gp,file);if(!save.trySave())throw new AssertionError(save.getLastError());
            gp.stopGame();
        }
        System.out.println("REVISION_1 fixtures written with the previous local implementation, before correction");
    }
}
