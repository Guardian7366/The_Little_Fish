package data;

import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import main.ExtraRooms;
import main.GamePanel;
import main.Temple;
import object.OBJ_Chest;

import static main.ExtraRooms.*;

public final class ExtraRoomsSaveRegression {
    private static void check(boolean ok,String message) {
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        var gp=new GamePanel() {
            @Override public void playMusic(int i) {}
            @Override public void playSE(int i) {}
            @Override public void stopMusic() {}
        };
        gp.setupGame();
        Path file=Files.createTempDirectory(Path.of("build"),"refugios-legacy-").resolve("save-v1.dat");
        SaveLoad save=new SaveLoad(gp,file);
        var capture=SaveLoad.class.getDeclaredMethod("capture");
        capture.setAccessible(true);
        gp.currentMap=MAP;
        gp.currentArea=gp.nextArea=gp.indoor;
        int[][] obsoletePositions={{18,40},{38,40},{36,30}};
        int[][] oldNpcPositions={{20,37},{28,41},{28,26}};
        String[] ids={"lantern","red-potion","bed"};
        int historical=0;
        for (int i=0;i<3;i++) {
            Room room=Room.values()[i];
            for (boolean opened : new boolean[]{false,true}) {
                Path baseline=Path.of("build/refugios-baseline/room"+i+"-"+opened+".dat");
                if (Files.isRegularFile(baseline)) {
                    Files.copy(baseline,file,StandardCopyOption.REPLACE_EXISTING);
                    historical++;
                } else {
                    Temple.place(gp.player,obsoletePositions[i][0],obsoletePositions[i][1]);
                    SaveSnapshot old=(SaveSnapshot)capture.invoke(save);
                    old.extraRoomsRevision=old.extraRoomsFlags=0;
                    old.world.objects[MAP][FIRST_CHEST+i].opened=opened;
                    old.world.npcs[MAP][8+i].x=oldNpcPositions[i][0]*48;
                    old.world.npcs[MAP][8+i].y=oldNpcPositions[i][1]*48;
                    try (var out=new ObjectOutputStream(Files.newOutputStream(file))) { out.writeObject(old); }
                }
                byte[] bytes=Files.readAllBytes(file);
                check(save.tryLoad(),save.getLastError());
                check(gp.extraRooms.active()==room && gp.player.worldX==room.arrivalX*48 &&
                    gp.player.worldY==room.arrivalY*48,"relocated within same room "+room);
                var chest=gp.obj[MAP][FIRST_CHEST+i];
                check(chest instanceof OBJ_Chest && chest.opened==opened && gp.eGenerator.idOf(chest.loot).equals(ids[i]),
                    "unchanged chest and item "+room);
                check(ExtraRooms.open(room,gp.extraRooms.flags())==opened,"opened legacy chest implies completed challenge");
                if (room==Room.FLOWER) check((gp.monster[MAP][GREENY]==null)==opened,"legacy completed Greeny is not farmable");
                check(Arrays.equals(bytes,Files.readAllBytes(file)),"legacy file remains byte-identical");
                for (int slot=4;slot<=8;slot++) check(gp.obj[MAP][slot] instanceof OBJ_Chest,"other reward "+slot);
            }
        }
        gp.stopGame();
        System.out.println("REFUGIOS_SAVE PASS: six old checkpoints, open and closed chests, same-room relocations, historical fixtures="+historical);
    }
}
