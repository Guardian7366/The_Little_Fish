package data;

import entity.Entity;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import main.GamePanel;
import main.Temple;
import monster.MON_Secret;
import object.OBJ_Coin;

public final class TempleSaveRegression {
    public static void main(String[] args) throws Exception {
        Path out=Files.createTempDirectory(Path.of("build"),"temple-migration-");
        int real=0;
        for(boolean open:new boolean[]{false,true}) {
            GamePanel gp=new GamePanel();
            gp.setupGame();
            String kind=open?"open":"partial";
            Path file=out.resolve(kind+".dat");
            Path prior=Path.of("build/temple-legacy-"+kind+"/save-v1.dat");
            SaveLoad save=new SaveLoad(gp,file);
            if(Files.exists(prior)) {
                Files.copy(prior,file);
                real++;
            } else {
                gp.currentMap=open?6:4;
                Temple.place(gp.player,24,open?44:42);
                check(save.trySave(),save.getLastError());
                SaveSnapshot s;
                try(var input=new ObjectInputStream(Files.newInputStream(file))) { s=(SaveSnapshot)input.readObject(); }
                s.templeRevision=0;
                s.world.monsters[5][81]=null;
                s.world.monsters[5][82]=s.world.monsters[4][82];
                var lane=Temple.LANES[0];
                var rock=s.world.npcs[4][lane.rockSlot()];
                var plate=s.world.tiles[4][lane.plateSlot()];
                rock.x=plate.x;rock.y=plate.y;rock.linkedTile=lane.plateSlot();
                if(open)s.world.objects[4][0]=null;
                try(var output=new ObjectOutputStream(Files.newOutputStream(file))) { output.writeObject(s); }
            }
            byte[] original=Files.readAllBytes(file);
            check(save.tryLoad(),save.getLastError());
            check(Arrays.equals(original,Files.readAllBytes(file)),"old bytes unchanged");
            check(gp.currentMap==(open?6:4) && gp.player.worldX==24*48 && gp.player.worldY==(open?44:42)*48,"valid position kept");
            var lane=Temple.LANES[0];
            check(Temple.paired(gp.npc[4][lane.rockSlot()],gp.iTile[4][lane.plateSlot()],lane),"old active plate mapped");
            check(gp.temple.solved()==open,"old gate progression mapped");
            check(gp.temple.defeats==1 && gp.monster[4][81]==null && gp.monster[4][82] instanceof MON_Secret,"only surviving legacy Secret moved");
            check(gp.monster[5][81]==null && gp.monster[5][82]==null,"no duplicate legacy encounters");
            if(!open) {
                var next=Temple.LANES[1];
                Entity moved=gp.npc[4][next.rockSlot()];
                moved.worldX+=48;
                check(save.trySave() && save.tryLoad(),"checkpoint between tile pushes");
                check(gp.npc[4][next.rockSlot()].worldX==(next.x()+1)*48,"in-progress lane preserved");
            } else {
                gp.obj[4][0]=Temple.place(new OBJ_Coin(gp),25,12);
                check(save.trySave() && save.tryLoad(),"open temple roundtrip");
                check(gp.obj[4][0] instanceof OBJ_Coin,"gate migration preserves unrelated drop");
            }
            gp.stopGame();
        }
        System.out.println("TEMPLE_SAVE PASS: partial plates, mid-lane checkpoint, open gate, valid positions, Secret identity; real legacy fixtures="+real);
    }

    private static void check(boolean condition,String message) {
        if(!condition)throw new AssertionError(message);
    }
}
