package data;

import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import main.ExtraRooms;
import main.GamePanel;
import main.RefugeChallenges;
import main.Temple;

public final class LaterRoomsSaveRegression {
    private static void check(boolean ok,String message) {
        if(!ok) throw new AssertionError(message);
    }
    private static void write(Path file,SaveSnapshot snapshot) throws Exception {
        try(var out=new ObjectOutputStream(Files.newOutputStream(file))) { out.writeObject(snapshot); }
    }
    public static void main(String[] args) throws Exception {
        var gp=new GamePanel() {
            public void playMusic(int i) {}
            public void playSE(int i) {}
            public void stopMusic() {}
        };
        gp.setupGame();
        Path file=Files.createTempDirectory(Path.of("build"),"later-refugios-saves-").resolve("save-v1.dat");
        SaveLoad save=new SaveLoad(gp,file);
        var capture=SaveLoad.class.getDeclaredMethod("capture");
        capture.setAccessible(true);
        int[] slots={4,6,5};
        String[] rewards={"red-potion","super-shield","rock-tool"};
        int[][] positions={{19,28},{12,17},{31,19}};
        int historical=0;
        for (int i=0;i<3;i++) for (boolean opened : new boolean[]{false,true}) {
            Path source=Path.of("build/later-refugios-baseline/room"+i+"-"+opened+".dat");
            if (Files.isRegularFile(source)) {
                Files.copy(source,file,StandardCopyOption.REPLACE_EXISTING);
                historical++;
            } else {
                gp.currentMap=5;
                gp.currentArea=gp.indoor;
                gp.extraRooms.restore(31);
                ExtraRooms.configure(gp.monster,31);
                Temple.place(gp.player,positions[i][0],positions[i][1]);
                SaveSnapshot old=(SaveSnapshot)capture.invoke(save);
                old.extraRoomsRevision=1;
                old.pumpBindings=old.honguiBlocks=old.ghostPrefix=0; old.honguiDefeated=false;
                old.world.objects[5][slots[i]].opened=opened;
                write(file,old);
            }
            byte[] bytes=Files.readAllBytes(file);
            check(save.tryLoad(),save.getLastError());
            var room=ExtraRooms.Room.values()[3+i];
            check(gp.extraRooms.active()==room && gp.player.worldX==room.arrivalX*48 &&
                gp.player.worldY==room.arrivalY*48,"same-room migration "+room);
            check(gp.obj[5][slots[i]].opened==opened &&
                gp.eGenerator.idOf(gp.obj[5][slots[i]].loot).equals(rewards[i]),"original reward "+room);
            check(gp.extraRooms.isOpen(room)==opened,"old opened chest completes challenge");
            check(gp.extraRooms.flags()==31 && gp.monster[5][65]==null && gp.monster[5][69]==null &&
                gp.monster[5][70]!=null,"first three challenges unchanged");
            check(Arrays.equals(bytes,Files.readAllBytes(file)),"load never rewrites fixture");
        }
        gp.currentMap=0;
        Temple.place(gp.player,13,31);
        SaveSnapshot bad=(SaveSnapshot)capture.invoke(save);
        bad.honguiBlocks=3;
        write(file,bad);
        check(!save.tryLoad() && gp.currentMap==0,"invalid guard count rejected transactionally");
        bad=(SaveSnapshot)capture.invoke(save); bad.ghostPrefix=4;
        write(file,bad);
        check(!save.tryLoad(),"invalid memory prefix rejected");
        bad=(SaveSnapshot)capture.invoke(save); bad.pumpBindings=4;
        write(file,bad);
        check(!save.tryLoad(),"invalid binding flags rejected");
        bad=(SaveSnapshot)capture.invoke(save); bad.honguiBlocks=1; bad.honguiDefeated=true;
        write(file,bad);
        check(!save.tryLoad(),"defeat and completion must agree");
        gp.stopGame();
        System.out.println("LATER_REFUGIOS_SAVE PASS: six previous snapshots, same-room migrations, original rewards, first-three flags and invalid-state rejection; historical="+historical);
    }
}
