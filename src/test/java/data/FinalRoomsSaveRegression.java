package data;

import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import main.ExtraRooms;
import main.FinalRefuges;
import main.GamePanel;
import main.RefugeChallenges;
import main.Temple;
import object.OBJ_Rock;

public final class FinalRoomsSaveRegression {
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
        Path file=Files.createTempDirectory(Path.of("build"),"final-refugios-saves-").resolve("save-v1.dat");
        SaveLoad save=new SaveLoad(gp,file);
        var capture=SaveLoad.class.getDeclaredMethod("capture");
        capture.setAccessible(true);
        int[][] cases={{5,36,6,8,0,0,3,0},{5,36,6,8,1,0,0,0},{5,36,6,8,1,1,3,0},
            {5,19,7,7,1,0,0,0},{5,19,7,7,1,1,0,3},{3,25,23,8,0,0,3,0},
            {2,22,7,8,0,0,0,0},{6,24,44,8,0,0,3,0}};
        int historical=0;
        for (int i=0;i<cases.length;i++) {
            int[] c=cases[i];
            Path source=Path.of("build/final-refugios-baseline/case"+i+".dat");
            if (Files.isRegularFile(source)) {
                Files.copy(source,file,StandardCopyOption.REPLACE_EXISTING);
                historical++;
            } else {
                gp.currentMap=c[0];
                gp.currentArea=c[0]==5?gp.indoor:c[0]==2?gp.outside:gp.dungeons;
                gp.extraRooms.restore(31);
                gp.extraRooms.later.restore(new RefugeChallenges.State(1,2,true,2));
                ExtraRooms.configure(gp.monster,31);
                RefugeChallenges.configure(gp.monster,gp.extraRooms.later.state());
                gp.player.inventory.removeIf(OBJ_Rock.class::isInstance);
                if (c[4]==1) gp.player.inventory.add(new OBJ_Rock(gp));
                Temple.place(gp.player,c[1],c[2]);
                SaveSnapshot old=(SaveSnapshot)capture.invoke(save);
                old.extraRoomsRevision=2;
                old.handyLatches=old.handyDamaged=old.icMarks=0; old.handySentryDefeated=false;
                old.world.objects[5][7].opened=false; old.world.objects[5][8].opened=false;
                old.world.objects[5][c[3]].opened=c[5]==1;
                write(file,old);
            }
            byte[] bytes=Files.readAllBytes(file);
            check(save.tryLoad(),save.getLastError());
            check(gp.currentMap==c[0],"map preserved");
            if (c[0]==5) {
                var room=c[3]==8?ExtraRooms.Room.HANDY:ExtraRooms.Room.IC;
                check(gp.extraRooms.active()==room && gp.player.worldX==room.arrivalX*48 &&
                    gp.player.worldY==room.arrivalY*48,"incompatible position relocated within same room");
            } else check(gp.player.worldX==c[1]*48 && gp.player.worldY==c[2]*48,"other coordinates preserved");
            var ending=gp.extraRooms.ending.state();
            check(ending.latches()==c[6] && ending.marks()==c[7] && ending.damaged()==0,"correct migration "+i);
            check(gp.obj[5][c[3]].opened==(c[5]==1),"claimed state preserved");
            check(gp.eGenerator.idOf(gp.obj[5][8].loot).equals("iron-tool") &&
                gp.eGenerator.idOf(gp.obj[5][7].loot).equals("toxic-bubble"),"reward IDs preserved");
            check(gp.player.inventory.stream().anyMatch(OBJ_Rock.class::isInstance)==(c[4]==1),"no gear granted");
            check(gp.extraRooms.flags()==31 && gp.extraRooms.later.state().equals(new RefugeChallenges.State(1,2,true,2)),
                "all six earlier challenges preserved");
            check(Arrays.equals(bytes,Files.readAllBytes(file)),"migration doesn't rewrite file");
            if (i==0) {
                check(save.trySave(),save.getLastError());
                gp.extraRooms.ending.restore(FinalRefuges.EMPTY);
                check(save.tryLoad(),save.getLastError());
                check(gp.extraRooms.isOpen(ExtraRooms.Room.HANDY) && !gp.obj[5][8].opened,
                    "migration exemption persists with closed reward");
            }
        }
        SaveSnapshot bad=(SaveSnapshot)capture.invoke(save);
        bad.icMarks=4;
        write(file,bad);
        check(!save.tryLoad(),"invalid mark mask rejected");
        bad=(SaveSnapshot)capture.invoke(save);
        bad.handyLatches=1; bad.handyDamaged=1;
        write(file,bad);
        check(!save.tryLoad(),"broken latch cannot also be damaged");
        gp.stopGame();
        System.out.println("FINAL_REFUGIOS_SAVE PASS: eight legacy cases, missing Rocky, claimed rewards, six previous challenges and invalid masks; historical="+historical);
    }
}
