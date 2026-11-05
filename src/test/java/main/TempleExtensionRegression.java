package main;

import data.SaveLoad;
import entity.Entity;
import java.awt.Rectangle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import monster.MON_Flower;
import monster.MON_Tella;

import static main.TempleExtension.*;
import static main.TempleRegression.capture;
import static main.TempleRegression.press;
import static main.TempleRegression.route;
import static main.TempleRegression.swim;

public final class TempleExtensionRegression {
    private static final Cell LANDING = new Cell(24,43);
    private static final Path OUT = Path.of("build/octava-preview");

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        var gp = new FifthZoneRegression.Quiet();
        gp.setupGame();
        Files.createDirectories(OUT);
        gp.currentMap=MAP;
        gp.currentArea=gp.nextArea=gp.dungeons;
        gp.gameState=gp.playState;
        Temple.place(gp.player,24,43);
        int[] reach = route(gp,24,43);
        for (var inscription : INSCRIPTIONS)
            check(reach[index(inscription.readX(),inscription.readY())]>=0,"reachable "+inscription.statue());
        combat(gp);
        inscriptions(gp);

        for (int a=0; a<3; a++) for (int b=0; b<3; b++) {
            if (a!=b) solve(gp,new int[]{a,b,3-a-b},a==0 && b==1);
        }
        for (var lane : Temple.LANES) {
            var rock = Temple.place(gp.npc[4][lane.rockSlot()],lane.endX(),lane.endY());
            rock.linkedEntity=gp.iTile[4][lane.plateSlot()];
        }
        gp.obj[4][0]=null;
        FifthZoneRegression.portal(gp,4,24,5,MAP);
        check(gp.player.worldX==24*48 && gp.player.worldY==43*48,"safe landing");
        FifthZoneRegression.portal(gp,MAP,24,45,4);
        check(gp.player.worldY==6*48 && gp.temple.solved(),"return south of the seventh gate");
        FifthZoneRegression.portal(gp,4,24,5,MAP);
        FifthZoneRegression.portal(gp,MAP,24,5,Market.MAP);
        FifthZoneRegression.portal(gp,Market.MAP,24,19,6);
        check(gp.player.worldY==44*48,"original boss arrival");
        gp.resetGame(false);
        check(solved(gp.npc,gp.iTile) && gp.obj[MAP][0]==null,"retry retains resolved extension");
        gp.resetGame(true);
        check(!solved(gp.npc,gp.iTile) && gp.obj[MAP][0]!=null,"new game resets extension");
        gp.stopGame();
        System.out.println("OCTAVA PASS: six orders, live collisions, elbow save, two wings, combat and three readings, bidirectional temple and boss");
    }

    private static void inscriptions(FifthZoneRegression.Quiet gp) throws Exception {
        for (var inscription : INSCRIPTIONS) {
            capture(gp,OUT,inscription.statue(),inscription.readX(),inscription.readY(),true);
            gp.player.direction=inscription.facing();
            gp.keyH.enterPressed=true;
            gp.eHandler.checkEvent();
            check(gp.gameState==gp.dialogueState,"read "+inscription.statue());
            gp.keyH.enterPressed=false;
            gp.gameState=gp.playState;
        }
        capture(gp,OUT,"velo-sin-linterna",8,22,false);
    }

    private static void combat(FifthZoneRegression.Quiet gp) throws Exception {
        Cell[] plantDetection={new Cell(24,39),new Cell(15,35),new Cell(14,25),new Cell(23,25),
            new Cell(16,13),new Cell(34,18),new Cell(42,14)};
        Cell[] tellaDetection={new Cell(36,27),new Cell(23,25),new Cell(16,13),new Cell(32,9)};
        for (int i=0;i<FLOWERS.length;i++) {
            Temple.place(gp.player,plantDetection[i].x(),plantDetection[i].y());
            tickEnemies(gp,45);
            check(((MON_Flower)gp.monster[MAP][i]).vine()==MON_Flower.Vine.AIM,"Plant detects from "+plantDetection[i]);
            check(warnings(gp)<=2,"Plant approach has at most two threats");
            if (i==0) {
                check(warnings(gp)==1,"first Plant is isolated");
                capture(gp,OUT,"primer-encuentro",24,39,false);
                swim(gp,24,39,25,39);
                tickEnemies(gp,50);
                check(!gp.temple.pulled(),"lateral escape from first vine");
            }
            if (i==3) {
                check(((MON_Tella)gp.monster[MAP][FLOWERS.length+1]).phase()==MON_Tella.Phase.WARNING,"mixed crossing warning");
                tickEnemies(gp,40);
                check(((MON_Flower)gp.monster[MAP][i]).vine()==MON_Flower.Vine.EXTEND,"mixed vine overlaps the pulse warning");
                capture(gp,OUT,"encuentro-mixto",23,25,true);
            }
            restoreActors(gp);
        }
        for (int i=0;i<TELLAS.length;i++) {
            Cell approach=tellaDetection[i];
            Temple.place(gp.player,approach.x(),approach.y());
            tickEnemies(gp,45);
            MON_Tella tella=(MON_Tella)gp.monster[MAP][FLOWERS.length+i];
            check(tella.phase()==MON_Tella.Phase.WARNING,"Fishtella detects from "+approach);
            check(warnings(gp)<=2,"pulse approach has at most two threats");
            check(!tella.bands().isEmpty(),"pulse has visible cells");
            for (Rectangle band : tella.bands())
                check(Temple.terrainClear(gp,MAP,band),"pulse clipped by walls");
            restoreActors(gp);
        }
        eastDodges(gp);
        var protectedCells = new java.util.ArrayList<>(java.util.List.of(LANDING,new Cell(22,41),
            new Cell(8,22),new Cell(43,24),new Cell(28,9),new Cell(24,9)));
        for (Lane lane : LANES) for (int i=0; i<lane.path().length; i++) {
            Cell cell=lane.path()[i];
            for (int[] offset : new int[][]{{0,0},{1,0},{-1,0},{0,1},{0,-1}}) {
                Cell nearby=new Cell(cell.x()+offset[0],cell.y()+offset[1]);
                if (Temple.clear(gp,gp.player,nearby.x()*48,nearby.y()*48)) protectedCells.add(nearby);
            }
        }
        for (Cell safe : protectedCells) {
            Temple.place(gp.player,safe.x(),safe.y());
            tickEnemies(gp,100);
            check(warnings(gp)==0 && !gp.temple.pulled(),"quiet cell "+safe);
            restoreActors(gp);
        }
        int[] rest=route(gp,24,43);
        check(rest[index(22,41)]>=0 && rest[index(24,8)]>=0,"rest and antechamber reachable from refuge");
        swim(gp,24,43,14,25);
        swim(gp,14,25,22,41);
        swim(gp,22,41,36,27);
        swim(gp,36,27,22,41);
        gp.player.life=1;
        gp.saveLoad=new SaveLoad(gp,Files.createTempDirectory(Path.of("build"),"octava-refugio-").resolve("save-v1.dat"));
        gp.keyH.enterPressed=true;
        gp.eHandler.checkEvent();
        gp.keyH.enterPressed=false;
        check(gp.player.life==gp.player.maxLife && gp.gameState==gp.dialogueState,"rest recovers and saves");
        check(gp.saveLoad.tryLoad(),gp.saveLoad.getLastError());
        gp.gameState=gp.playState;
    }

    private static void eastDodges(GamePanel gp) {
        MON_Tella tella=(MON_Tella)gp.monster[MAP][FLOWERS.length];
        Temple.place(gp.player,38,25);
        tickEnemies(gp,45);
        check(tella.phase()==MON_Tella.Phase.WARNING && tella.bands().stream().anyMatch(
            band -> band.contains(38*48+24,25*48+24)),"east approach crosses horizontal warning");
        int life=gp.player.life;
        swim(gp,38,25,38,26);
        tickEnemies(gp,45);
        check(gp.player.life==life && tella.phase()==MON_Tella.Phase.PULSE,"lateral tile evades horizontal pulse");
        tickEnemies(gp,10);
        check(tella.phase()==MON_Tella.Phase.CORE,"escape leaves time to counterattack");
        Temple.place(gp.player,36,27);
        for (int i=0;i<220 && tella.phase()!=MON_Tella.Phase.WARNING;i++) tickEnemies(gp,1);
        check(tella.phase()==MON_Tella.Phase.WARNING && tella.bands().stream().anyMatch(
            band -> band.contains(36*48+24,27*48+24)),"second approach crosses vertical warning");
        swim(gp,36,27,37,27);
        tickEnemies(gp,45);
        check(gp.player.life==life && tella.phase()==MON_Tella.Phase.PULSE,"lateral tile evades vertical pulse");
        restoreActors(gp);
    }

    private static int warnings(GamePanel gp) {
        int result=0;
        for (Entity e : gp.monster[MAP]) {
            if (e instanceof MON_Flower plant && plant.vine()!=MON_Flower.Vine.IDLE) result++;
            if (e instanceof MON_Tella tella && tella.phase()==MON_Tella.Phase.WARNING) result++;
        }
        return result;
    }

    private static void tickEnemies(GamePanel gp,int ticks) {
        for (int tick=0;tick<ticks;tick++) for (Entity e : gp.monster[MAP]) if (e!=null) e.update();
    }

    private static void restoreActors(GamePanel gp) {
        gp.temple.resetCombat();
        populate(gp,gp.monster);
    }

    private static int index(int x,int y) { return 50*y+x; }

    private static void solve(FifthZoneRegression.Quiet gp,int[] order,boolean fixture) throws Exception {
        Arrays.fill(gp.monster[MAP],null);
        rocks(gp,gp.npc); plates(gp,gp.iTile);
        gp.obj[MAP][0]=Temple.gate(gp,GATE_X,GATE_Y);
        gp.eHandler=new EventHandler(gp);
        gp.player.life=gp.player.maxLife;
        if (fixture) {
            capture(gp,OUT,"porton-cerrado",24,7,true);
            capture(gp,OUT,"carril-este-inicial",37,33,true);
        }
        for (int placed=0;placed<order.length;placed++) {
            check(route(gp,24,43)[index(24,5)]<0,"no physical shortcut");
            Temple.place(gp.player,24,5);
            gp.eHandler.checkEvent();
            check(gp.gameState==gp.playState,"locked boss event checks exact pairs");
            Lane lane=LANES[order[placed]];
            Entity rock=gp.npc[MAP][lane.rockSlot()];
            Cell[] path=lane.path();
            for (int i=0;i<path.length-1;i++) {
                Cell from=path[i],to=path[i+1];
                int dx=to.x()-from.x(),dy=to.y()-from.y();
                Cell support=new Cell(from.x()-dx,from.y()-dy);
                swim(gp,24,43,support.x(),support.y());
                if (fixture && lane==LANES[2] && i==3) {
                    swim(gp,support.x(),support.y(),37,32);
                    press(gp,"right");
                    for(int t=0;t<14;t++)gp.player.update();
                    press(gp,"");
                    check(lane.step(rock)==3,"cannot continue straight at elbow");
                    swim(gp,24,43,support.x(),support.y());
                    capture(gp,OUT,"giro",support.x(),support.y(),true);
                    Path saveFile=Files.createTempDirectory(Path.of("build"),"octava-codo-").resolve("save-v1.dat");
                    SaveLoad save=new SaveLoad(gp,saveFile);
                    check(save.trySave(),save.getLastError());
                    byte[] bytes=Files.readAllBytes(saveFile);
                    rocks(gp,gp.npc);
                    check(save.tryLoad(),save.getLastError());
                    check(Arrays.equals(bytes,Files.readAllBytes(saveFile)),"read leaves elbow save byte-identical");
                    rock=gp.npc[MAP][lane.rockSlot()];
                    check(lane.step(rock)==3 && gp.player.worldY==support.y()*48,"elbow position and support restored");
                }
                press(gp,dx>0?"right":dx<0?"left":dy>0?"down":"up");
                for (int t=0;t<18 && lane.step(rock)==i;t++) gp.player.update();
                press(gp,"");
                check(lane.step(rock)==i+1,"exact atomic step "+from+" to "+to);
                int px=gp.player.worldX/48,py=gp.player.worldY/48;
                check(route(gp,px,py)[index(24,43)]>=0,"escape from "+to);
                int[] around=route(gp,24,43);
                check(around[index(to.x()+dx,to.y()+dy)]>=0,"far side accessible at "+to);
            }
            check(valid(rock,gp.iTile[MAP][lane.plateSlot()],lane),"correct pair");
            check(solved(gp.npc,gp.iTile)==(placed==2),"gate waits for third pair");
        }
        check(route(gp,24,43)[index(24,5)]>=0,"opened gate physically traversable");
        swim(gp,24,43,24,6);
        if (fixture) {
            capture(gp,OUT,"carril-este-resuelto",37,33,true);
            capture(gp,OUT,"porton-abierto",24,7,true);
            SaveLoad save=new SaveLoad(gp,Files.createTempDirectory(Path.of("build"),"octava-completo-").resolve("save-v1.dat"));
            check(save.trySave(),save.getLastError());
            rocks(gp,gp.npc);
            check(save.tryLoad(),save.getLastError());
            check(solved(gp.npc,gp.iTile) && gp.obj[MAP][0]==null,"load retains open extension gate");
        }
    }
}
