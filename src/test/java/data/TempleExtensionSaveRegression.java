package data;

import entity.Entity;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import main.GamePanel;
import main.Temple;
import main.TempleExtension;

public final class TempleExtensionSaveRegression {
    private static void check(boolean condition,String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void write(Path file,SaveSnapshot snapshot) throws Exception {
        try (var out=new ObjectOutputStream(Files.newOutputStream(file))) { out.writeObject(snapshot); }
    }

    public static void main(String[] args) throws Exception {
        var gp=new GamePanel() {
            @Override public void playMusic(int i) {}
            @Override public void playSE(int i) {}
            @Override public void stopMusic() {}
        };
        gp.setupGame();
        Path file=Files.createTempDirectory(Path.of("build"),"octava-migracion-").resolve("save-v1.dat");
        SaveLoad save=new SaveLoad(gp,file);
        gp.temple.reliefs=3;
        gp.temple.defeats=1;
        gp.monster[4][Temple.SECRET_SLOTS[0]]=null;
        for (var lane : Temple.LANES) {
            Entity rock=Temple.place(gp.npc[4][lane.rockSlot()],lane.endX(),lane.endY());
            rock.linkedEntity=gp.iTile[4][lane.plateSlot()];
        }
        gp.obj[4][0]=null;
        var capture=SaveLoad.class.getDeclaredMethod("capture");
        capture.setAccessible(true);
        int realFixtures=0;
        for (int map : new int[]{3,4,6}) {
            gp.currentMap=map;
            gp.currentArea=gp.dungeons;
            Temple.place(gp.player,map==3?25:24,map==3?23:map==4?6:44);
            Path historical=Path.of("build/octava-baseline/map"+map+".dat");
            if (Files.isRegularFile(historical)) {
                Files.copy(historical,file,java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                realFixtures++;
            } else {
                SaveSnapshot old=(SaveSnapshot)capture.invoke(save);
                old.templeExtensionRevision=0;
                Arrays.fill(old.world.objects[9],null);
                Arrays.fill(old.world.npcs[9],null);
                Arrays.fill(old.world.monsters[9],null);
                Arrays.fill(old.world.tiles[9],null);
                write(file,old);
            }
            byte[] bytes=Files.readAllBytes(file);
            check(save.tryLoad(),save.getLastError());
            check(gp.currentMap==map && gp.player.worldX==(map==3?25:24)*48 && gp.player.worldY==(map==3?23:map==4?6:44)*48,"preserves old player position");
            check(gp.temple.solved() && gp.temple.reliefs==3 && gp.temple.defeats==1,"seventh progress survives");
            check(gp.monster[4][Temple.SECRET_SLOTS[0]]==null && gp.monster[4][Temple.SECRET_SLOTS[1]]!=null,"seventh Secrets retain identities");
            check(gp.obj[9][0]!=null && !TempleExtension.solved(gp.npc,gp.iTile),"new gate starts closed");
            for (var lane : TempleExtension.LANES)
                check(lane.step(gp.npc[9][lane.rockSlot()])==0,"new lanes start at origin");
            check(gp.monster[9][10]!=null,"new encounters populated");
            check(Arrays.equals(bytes,Files.readAllBytes(file)),"migration does not rewrite input");
        }
        SaveSnapshot bad=(SaveSnapshot)capture.invoke(save);
        bad.world.npcs[9][2].x=37*48;
        bad.world.npcs[9][2].y=33*48;
        write(file,bad);
        check(!save.tryLoad() && gp.currentMap==6,"off-path extension rock rejected transactionally");
        bad=(SaveSnapshot)capture.invoke(save);
        bad.world.npcs[9][2].x=38*48;
        bad.world.npcs[9][2].y=36*48;
        write(file,bad);
        check(!save.tryLoad() && gp.currentMap==6,"unlinked end rejected transactionally");
        gp.stopGame();
        System.out.println("OCTAVA_SAVE PASS: pre-extension snapshots on maps 3, 4 and 6, temple flags, off-path and unlinked endpoint. Historical fixtures: "+realFixtures);
    }
}
