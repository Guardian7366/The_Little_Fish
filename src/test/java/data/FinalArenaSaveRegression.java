package data;

import entity.Entity;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import main.GamePanel;
import main.Temple;
import object.OBJ_Door;
import object.OBJ_Key;

import static main.FinalArena.*;

public final class FinalArenaSaveRegression {
    private static final class Quiet extends GamePanel {
        @Override public void playMusic(int id) {}
        @Override public void stopMusic() {}
        @Override public void playSE(int id) {}
    }

    private static void check(boolean ok,String message) {
        if (!ok) throw new AssertionError(message);
    }
    private static SaveSnapshot read(Path path) throws Exception {
        try (var in=new ObjectInputStream(Files.newInputStream(path))) { return (SaveSnapshot)in.readObject(); }
    }
    private static void write(Path path,SaveSnapshot state) throws Exception {
        try (var out=new ObjectOutputStream(Files.newOutputStream(path))) { out.writeObject(state); }
    }
    private static SaveSnapshot.Actor actor(String id,int x,int y) {
        var a=new SaveSnapshot.Actor();
        a.id=id; a.x=x*48; a.y=y*48; a.direction="down"; a.amount=1; a.alive=true;
        a.inventory=new SaveSnapshot.Item[0];
        return a;
    }

    public static void main(String[] args) throws Exception {
        var gp=new Quiet();
        gp.setupGame();
        var file=Files.createTempDirectory(Path.of("build"),"final-save-").resolve("save-v1.dat");
        var save=new SaveLoad(gp,file);
        save.captureInitialWorld();
        int[][] cells={{23,44},{26,40},{25,23},{23,44},{26,8},{26,8}};
        int originals=0;
        for (int i=0;i<cells.length;i++) {
            gp.resetGame(true);
            gp.currentMap=MAP;
            gp.currentArea=gp.nextArea=gp.dungeons;
            gp.gameState=gp.playState;
            Temple.place(gp.player,23,44);
            gp.player.coin=42;
            gp.player.exp=73;
            gp.player.inventory.add(gp.eGenerator.requireObject("rock-tool"));
            if (i>=2) gp.player.inventory.add(gp.eGenerator.requireObject("iron-tool"));
            check(save.trySave(),save.getLastError());
            var state=read(file);
            state.arenaRevision=0;
            state.x=cells[i][0]*48; state.y=cells[i][1]*48;
            state.world.monsters[MAP][BOSS_SLOT].x=23*48;
            state.world.monsters[MAP][BOSS_SLOT].y=14*48;
            if (i==2) {
                state.bossBattleOn=true;
                state.world.monsters[MAP][BOSS_SLOT].life=299;
                state.world.monsters[MAP][BOSS_SLOT].inRage=true;
                state.world.monsters[MAP][BOSS_SLOT].sleep=false;
                state.world.objects[MAP][0]=actor("iron-door",25,26);
                state.world.objects[MAP][0].temp=true;
            } else if (i>=3) {
                state.world.monsters[MAP][BOSS_SLOT]=null;
                state.godFishDefeated=true;
                if (i==3) state.world.objects[MAP][0]=actor("key",20,14);
                if (i>=4) state.world.objects[MAP][DOOR_SLOT]=null;
                if (i==5) state.world.objects[MAP][DOOR_SLOT+1]=null;
            }
            write(file,state);
            Path original=Path.of("build/final-baseline/case"+i+".dat");
            if (Files.isRegularFile(original)) {
                Files.copy(original,file,java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                originals++;
            }
            byte[] bytes=Files.readAllBytes(file);
            gp.csManager.sceneNum=gp.csManager.GodFish;
            gp.gameState=gp.cutsceneState;
            gp.keyH.enterPressed=true;
            check(save.tryLoad(),save.getLastError());
            check(Arrays.equals(bytes,Files.readAllBytes(file)),"loading never rewrites the fixture");
            check(gp.currentMap==MAP && gp.player.exp==73 && gp.player.coin==42
                && gp.player.searchItemInInventory("Rocky")!=999 && (gp.player.searchItemInInventory("Iron")!=999)==(i>=2),
                "world, experience, balance and equipment stay intact");
            check(!gp.bossBattleOn && gp.csManager.sceneNum==gp.csManager.NA && !gp.keyH.enterPressed
                && gp.obj[MAP][SEAL_SLOT]==null,"stale combat, camera, input and gate clear on load");
            if (i<=2) {
                Entity boss=gp.monster[MAP][BOSS_SLOT];
                check(boss!=null && boss.life==650 && boss.sleep && !boss.inRage,"fresh fight for legacy undefeated save");
                check(gp.obj[MAP][DOOR_SLOT] instanceof OBJ_Door && gp.obj[MAP][0]==null,"only old temporary gate removed");
                check(gp.player.worldY==(i==0?44:SAFE_Y)*48,"valid start preserved and old unsafe position migrated");
            } else {
                check(Progress.GodFishDefeated && gp.monster[MAP][BOSS_SLOT]==null,"legacy victory stays won");
                check((gp.obj[MAP][DOOR_SLOT]!=null)==(i==3) && (gp.obj[MAP][DOOR_SLOT+1]!=null)==(i!=5),"door and ending state preserved");
                long keys=Arrays.stream(gp.obj[MAP]).filter(OBJ_Key.class::isInstance).count();
                check(keys==(i==3?1:0),"one pending key preserved, none duplicated");
                if (i==3) {
                    Entity key=Arrays.stream(gp.obj[MAP]).filter(OBJ_Key.class::isInstance).findFirst().orElseThrow();
                    check(key.worldX==KEY_X*48 && key.worldY==KEY_Y*48,"pending key from old void recovered");
                }
            }
            gp.gameState=gp.playState;
            check(save.trySave() && save.tryLoad(),save.getLastError());
            check(read(file).arenaRevision==REVISION,"re-save writes current arena revision");
        }
        gp.gameState=gp.cutsceneState;
        byte[] good=Files.readAllBytes(file);
        check(!save.trySave() && Arrays.equals(good,Files.readAllBytes(file)),"no save during cutscene");
        gp.gameState=gp.transitionState;
        check(!save.trySave() && Arrays.equals(good,Files.readAllBytes(file)),"no save during transition");
        gp.stopGame();
        System.out.println("FINAL_ARENA_SAVE PASS: six pre-arena cases, retry migration, pending key, completed credits, roundtrip; historical="+originals);
    }
}
