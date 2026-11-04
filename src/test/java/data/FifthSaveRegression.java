package data;

import main.*;
import monster.MON_Sharky;
import object.OBJ_Iron;
import tile_interactive.IT_Rocky;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public final class FifthSaveRegression {
    static int checks;
    static class Quiet extends GamePanel {public void playMusic(int i){} public void playSE(int i){} public void stopMusic(){}}
    static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
    static SaveSnapshot read(Path p)throws Exception{try(var in=new ObjectInputStream(Files.newInputStream(p))){return (SaveSnapshot)in.readObject();}}
    static void write(Path p,SaveSnapshot s)throws Exception{try(var out=new ObjectOutputStream(Files.newOutputStream(p))){out.writeObject(s);}}
    /** Always runs, even on a fresh checkout without the historical build/ fixtures. */
    static void simulatedRevisionOne(Path run)throws Exception {
        for(int[] p:new int[][]{{3,16,43},{3,12,44},{3,18,45},{3,25,23},{2,22,7},{4,24,44},{6,24,44},{5,9,9}}) {
            Quiet gp=new Quiet();gp.setupGame();gp.currentMap=p[0];gp.currentArea=gp.dungeons;
            gp.player.worldX=p[1]*48;gp.player.worldY=p[2]*48;gp.monster[3][40].life=7;gp.monster[3][41]=null;
            gp.iTile[3][13]=new IT_Rocky(gp,20,26);gp.obj[5][8].opened=true;
            SaveLoad save=new SaveLoad(gp,run.resolve("simulated-revision1-"+p[0]+"-"+p[1]+"-"+p[2]+".dat"));
            check(save.trySave(),"prepare synthetic previous-schema fixture");
            SaveSnapshot s=read(save.getSavePath());s.fifthZoneRevision=1;
            for(int slot:FifthZone.GATE_SLOTS)s.world.tiles[3][slot]=null;
            write(save.getSavePath(),s);byte[] bytes=Files.readAllBytes(save.getSavePath());
            check(save.tryLoad(),"revision 1 migration always covered without external fixtures");
            boolean move=p[0]==3&&p[1]!=25;
            check(gp.currentMap==p[0]&&gp.player.worldX==(move?17:p[1])*48&&gp.player.worldY==(move?44:p[2])*48,"synthetic position migration");
            for(int slot:FifthZone.GATE_SLOTS)check(p[0]==4||p[0]==6?gp.iTile[3][slot] instanceof IT_Rocky:gp.iTile[3][slot].destructible,"synthetic downstream progress");
            check(!gp.obj[5][8].opened&&gp.player.inventory.stream().noneMatch(e->e instanceof OBJ_Iron),"recover missing Iron through chest, never auto-grant");
            check(gp.monster[3][40].life==7&&gp.monster[3][41]==null&&gp.iTile[3][13] instanceof IT_Rocky,"synthetic old progress retained");
            check(Arrays.equals(bytes,Files.readAllBytes(save.getSavePath())),"synthetic old file not rewritten");
            check(save.trySave()&&read(save.getSavePath()).fifthZoneRevision==2&&save.tryLoad(),"synthetic migration roundtrip");gp.stopGame();
        }
    }
    public static void main(String[] args)throws Exception {
        Path root=Path.of("build/fifth-save-fixtures");Files.createDirectories(root);
        Path run=Files.createTempDirectory(root,"run-");
        simulatedRevisionOne(run);
        Quiet gp=new Quiet();gp.setupGame();gp.currentMap=3;gp.currentArea=gp.dungeons;
        SaveLoad save=new SaveLoad(gp,run.resolve("save-v1.dat"));
        gp.monster[3][40].life=6;gp.monster[3][41]=null;gp.iTile[3][11]=new IT_Rocky(gp,14,7);
        var iron=new OBJ_Iron(gp);gp.player.inventory.add(iron);gp.player.currentWeapon=iron;
        for(int slot:FifthZone.GATE_SLOTS)for(int hit=0;hit<3;hit++){gp.iTile[3][slot].invincible=false;gp.player.damageInteractiveTile(slot);}
        gp.player.currentWeapon=gp.player.inventory.get(0);gp.player.worldX=17*48;gp.player.worldY=44*48;
        check(save.trySave(),"new save");SaveSnapshot snapshot=read(save.getSavePath());
        check(snapshot.fifthZoneRevision==2&&snapshot.version==1,"revision without schema bump");
        gp.monster[3][40].worldX+=48;
        gp.aSetter.setInteractiveTile();
        check(save.tryLoad(),"new load");check(gp.monster[3][40].life==6&&gp.monster[3][41]==null,"new enemy progress");
        check(gp.iTile[3][11] instanceof IT_Rocky,"new rubble preserved");
        for(int slot:FifthZone.GATE_SLOTS)check(gp.iTile[3][slot] instanceof IT_Rocky,"destroyed mandatory metal survives load with normal weapon");
        gp.nextArea=gp.currentArea;gp.changeArea();
        for(int slot:FifthZone.GATE_SLOTS)check(gp.iTile[3][slot] instanceof IT_Rocky,"area repopulation does not repair gate");
        // Return the enemy checkpoint used by the remaining atomic-load tests.
        check(save.tryLoad(),"restore full checkpoint after area transition");
        check(((MON_Sharky)gp.monster[3][40]).state()==MON_Sharky.State.LURK,"transients resume safely");
        byte[] same=Files.readAllBytes(save.getSavePath());save.tryLoad();check(Arrays.equals(same,Files.readAllBytes(save.getSavePath())),"load does not rewrite");
        snapshot.fifthZoneRevision=99;write(save.getSavePath(),snapshot);
        var player=gp.player;var world=gp.monster;
        check(!save.tryLoad()&&gp.player==player&&gp.monster==world,"invalid revision rolls back atomically");
        snapshot.fifthZoneRevision=0;snapshot.world.monsters[3][40].id="invalid-monster";write(save.getSavePath(),snapshot);
        check(!save.tryLoad()&&gp.player==player&&gp.monster==world,"invalid old actor rolls back before migration");
        gp.stopGame();
        Path base=Path.of("build/fifth-base-fixtures");
        if(Files.exists(base.resolve("map-3/save-v1.dat"))) {
            for(int map:new int[]{3,2}) {
                Path file=run.resolve("base-"+map+".dat");Files.copy(base.resolve("map-"+map+"/save-v1.dat"),file);
                check(read(file).fifthZoneRevision==0,"actual old schema defaults revision to zero");
                gp=new Quiet();gp.setupGame();save=new SaveLoad(gp,file);byte[] old=Files.readAllBytes(file);
                check(save.tryLoad(),"actual ca4b3ad load: "+save.getLastError());
                check(Arrays.equals(old,Files.readAllBytes(file)),"actual base not rewritten");
                check(gp.currentMap==map&&gp.player.worldX==25*48&&gp.player.worldY==23*48,"old player position in map "+map);
                check(gp.player.coin==37&&gp.player.inventory.stream().anyMatch(e->e instanceof OBJ_Iron),"inventory/progression retained");
                check(gp.obj[5][8].opened&&gp.obj[5][8].loot instanceof OBJ_Iron,"opened original chest retained");
                check(gp.monster[3][41]==null&&gp.monster[3][33]==null&&gp.monster[3][40].life==7&&gp.monster[3][36].life==3,"dead and hurt actors retained");
                check(gp.monster[3][40].worldX==38*48&&gp.monster[3][40].worldY==22*48,"survivors moved to new territory");
                check(gp.monster[3][46]==null&&gp.monster[3][34]==null,"retired slots removed explicitly");
                check(gp.monster[1][6].life==2,"other map enemy untouched");
                check(gp.iTile[3][4] instanceof IT_Rocky&&gp.iTile[3][11] instanceof IT_Rocky&&gp.iTile[3][12].life==1,"old rubble and hurt wall retained");
                check(gp.iTile[3][11].worldX==14*48&&gp.iTile[3][11].worldY==7*48,"rubble moved with its slot");
                check(gp.iTile[3][6]==null&&gp.iTile[3][17].destructible,"retired and genuinely new obstacles");
                check(save.trySave()&&read(file).fifthZoneRevision==2&&save.tryLoad(),"migrated roundtrip");
                gp.stopGame();
            }
        } else System.out.println("SKIP actual ca4b3ad fixture: run ./tools/verify_fifth_baseline.ps1 first");
        Path previous=Path.of("build/fifth-revision1-fixtures");
        if(Files.exists(previous)) {
            for(int[] p:new int[][]{{3,16,43},{3,12,44},{3,18,45},{3,25,23},{2,22,7},{4,24,44},{5,9,9}}) {
                String name=p[0]+"-"+p[1]+"-"+p[2];Path file=run.resolve("revision1-"+name+".dat");
                Files.copy(previous.resolve(name+"/save-v1.dat"),file);byte[] original=Files.readAllBytes(file);
                check(read(file).fifthZoneRevision==1,"real previous local save revision");
                check(read(file).world.objects[5][8].opened==(p[0]==3&&p[1]==25),"fixture includes previously opened Iron chest without the item");
                gp=new Quiet();gp.setupGame();save=new SaveLoad(gp,file);check(save.tryLoad(),"load real revision 1: "+name);
                boolean relocate=p[0]==3&&p[1]!=25;
                check(gp.currentMap==p[0]&&gp.player.worldX==(relocate?17:p[1])*48&&gp.player.worldY==(relocate?44:p[2])*48,"safe old player position "+name);
                if(gp.currentMap==3) {
                    int speed=gp.player.speed;gp.player.speed=0;
                    for(String direction:new String[]{"up","down","left","right"}) {
                        gp.player.direction=direction;gp.player.collisionOn=false;
                        gp.cChecker.checkTile(gp.player);gp.cChecker.checkEntity(gp.player,gp.iTile);
                        check(!gp.player.collisionOn,"migrated body fits outside terrain and metal");
                    }
                    gp.player.speed=speed;
                }
                check(Arrays.equals(original,Files.readAllBytes(file)),"revision 1 load does not rewrite file");
                for(int slot:FifthZone.GATE_SLOTS)check(p[0]==4?gp.iTile[3][slot] instanceof IT_Rocky:gp.iTile[3][slot].destructible,"new gate respects old downstream progress "+name);
                check(gp.monster[3][40].life==7&&gp.monster[3][41]==null&&gp.iTile[3][13] instanceof IT_Rocky,"approved world progress retained");
                check(gp.obj[3][0].worldX==17*48&&gp.obj[3][0].worldY==44*48,"old loose drop not embedded in new wall");
                check(gp.player.coin==73&&gp.player.inventory.stream().noneMatch(e->e instanceof OBJ_Iron),"no invented inventory items");
                check(!gp.obj[5][8].opened,"missing old Iron can still be obtained from original chest");
                check(save.trySave()&&read(file).fifthZoneRevision==2&&save.tryLoad(),"revision 1 -> 2 roundtrip");
                gp.stopGame();
            }
        } else System.out.println("SKIP real local revision-1 fixture: preserve/generate it before compiling revision 2");
        System.out.println("FIFTH_SAVE PASS: "+checks+" assertions; fixtures "+run);
    }
}
