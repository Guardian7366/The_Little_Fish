package main;

import data.Progress;
import data.SaveLoad;
import entity.Entity;
import entity.NPC_BigRock;
import entity.NPC_Ic;
import entity.NPC_Pump;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import object.OBJ_Exit;
import object.OBJ_Key;

import static java.awt.event.KeyEvent.*;

public final class LoreRegression {
    private static final class Quiet extends FifthZoneRegression.Quiet {
        final List<Integer> tracks=new ArrayList<>();
        @Override public void playMusic(int id) { tracks.add(id); }
    }
    private static final Quiet gp=new Quiet();
    private static final Path OUT=Path.of("build/beach-preview");
    private static final int[][] PORTALS={{0,28,18,9,40},{1,9,34,40,40},{1,39,23,40,29},
        {2,39,13,9,29},{2,9,17,9,19},{2,19,16,40,19},{3,14,40,9,9},{3,9,5,40,9}};
    private static final String[] ROOMS={"Chicken","Flower","Spiky","Pump","Hongui","Ghost","Ic","Handy"};
    private static final Map<String,List<String>> table=new LinkedHashMap<>();
    private static List<String> god,exit,coast;
    private static Path scratch;
    private static final AtomicInteger SURF_OPENS=new AtomicInteger(), SURF_LOOPS=new AtomicInteger(),
        SURF_CLOSES=new AtomicInteger(), SURF_ACTIVE=new AtomicInteger(), SURF_PEAK=new AtomicInteger();

    public static void main(String[] args) throws Exception {
        String guide=Files.readString(Path.of("design/lore/GUION-Y-ENCARGO.md"));
        parse(guide);
        gp.csManager=new CutsceneManager(gp,index -> {
            SURF_OPENS.incrementAndGet();
            return AudioRegression.fake(SURF_LOOPS,SURF_CLOSES,SURF_ACTIVE,SURF_PEAK,new AtomicBoolean());
        });
        gp.setupGame();
        Files.createDirectories(OUT);
        scratch=Files.createTempDirectory(Path.of("build"),"lore-save-");
        gp.saveLoad=new SaveLoad(gp,scratch.resolve("save-v1.dat"));
        checkLayout();
        checkBeachAssets();
        legacyBeach();
        check(SURF_OPENS.get()==0,"muted effects leave surf silent");
        gp.se.volumeScale=2;
        ending(true); // The open-door checkpoint has no optional readings or refuges.
        gp.se.volumeScale=0;
        gp.resetGame(true);
        npcAndSecrets();
        objectsAndMarket();
        refugeRoute();
        ending(false);
        legacyDialogue();
        gp.stopGame();
        System.out.println("LORE PASS: canonical pages, refuge order, animated portal, beach controls, one photo pickup, credits and legacy dialogue/ending saves");
    }

    private static void parse(String guide) {
        for (String line:guide.split("\\R")) {
            if (!line.startsWith("| ") || !line.endsWith(" |")) continue;
            String[] row=line.substring(2,line.length()-2).split(" \\| ",2);
            if (row.length==2) table.put(row[0],List.of(row[1].split(" \\\\\\| ")));
        }
        god=numbered(guide,"## God Fish · introducción","## Desenlace");
        exit=numbered(guide,"### Diálogo después de la puerta final","### Plano y últimas líneas");
        coast=numbered(guide,"### Plano y últimas líneas","## Integración y aceptación");
        check(god.size()==4 && exit.size()==8 && coast.size()==3,"screenplay sequences parsed");
    }

    private static List<String> numbered(String guide,String start,String end) {
        return guide.substring(guide.indexOf(start),guide.indexOf(end)).lines()
            .filter(line -> line.matches("[0-9]+\\. “.*”"))
            .map(line -> line.substring(line.indexOf('“')+1,line.length()-1)).toList();
    }

    private static void check(boolean condition,String label) {
        if (!condition) throw new AssertionError(label+" (map "+gp.currentMap+", phase "+gp.csManager.scenePhase+")");
    }

    private static String normalized(String line) { return line.replaceAll("\\s+"," ").trim(); }
    private static void tick(int n) { for (int i=0;i<n;i++) gp.update(); }
    private static void waitFor(java.util.function.BooleanSupplier condition,int limit,String label) {
        for (int i=0;i<limit && !condition.getAsBoolean();i++) tick(1);
        check(condition.getAsBoolean(),label);
    }

    private static void key(int code,boolean pressed) {
        var event=new KeyEvent(gp,pressed?KEY_PRESSED:KEY_RELEASED,0,0,code,CHAR_UNDEFINED);
        if (pressed) gp.keyH.keyPressed(event); else gp.keyH.keyReleased(event);
    }

    private static void tap(int code) {
        key(code,true);
        tick(1);
        key(code,false);
    }

    private static void place(int map,int x,int y,String direction) {
        gp.keyH.releaseAll();
        gp.currentMap=map;
        gp.currentArea=gp.nextArea=map==5 || map==10?gp.indoor:map<=2 || map==7?gp.outside:gp.dungeons;
        gp.gameState=gp.playState;
        gp.eHandler=new EventHandler(gp);
        gp.player.worldX=x;
        gp.player.worldY=y;
        gp.player.direction=direction;
        gp.player.attacking=gp.player.knockBack=false;
        gp.player.life=gp.player.maxLife;
    }

    private static void faceActor(int map,Entity actor) {
        var target=Temple.bounds(actor,actor.worldX,actor.worldY);
        String[] direction={"up","down","right","left"};
        int[][] offsets={{0,1},{0,-1},{-1,0},{1,0}};
        for (int i=0;i<offsets.length;i++) for (int n=16;n<=62;n++) {
            int x=actor.worldX+offsets[i][0]*n,y=actor.worldY+offsets[i][1]*n;
            var body=Temple.bounds(gp.player,x,y);
            if (body.intersects(target) || !Temple.terrainClear(gp,map,body)) continue;
            var next=new Rectangle(body.x-offsets[i][0]*gp.player.speed,body.y-offsets[i][1]*gp.player.speed,
                body.width,body.height);
            if (!next.intersects(target)) continue;
            if (map==5 && ExtraRooms.at((x+24)/48,(y+24)/48)!=ExtraRooms.at(actor.worldX/48,actor.worldY/48)) continue;
            place(map,x,y,direction[i]);
            tap(VK_ENTER);
            check(gp.ui.npc==actor && gp.gameState==gp.dialogueState,"normal interaction with "+actor.getClass().getSimpleName());
            return;
        }
        throw new AssertionError("No floor beside "+actor.getClass().getSimpleName());
    }

    private static List<String> source(String label) {
        var pages=table.get(label);
        if (pages==null) throw new AssertionError("Missing screenplay table row "+label);
        return pages;
    }

    private static void readPages(List<String> pages) {
        readPages(pages,0);
    }

    private static void readPages(List<String> pages,int first) {
        for (int i=first;i<pages.size();i++) {
            final int index=i;
            String expected=pages.get(i);
            waitFor(() -> gp.ui.npc!=null && gp.ui.npc.dialogueIndex==index
                && normalized(gp.ui.currentDialogue).equals(expected),160,"read page "+expected);
            tap(VK_ENTER);
        }
        tick(2);
    }

    private static void readSource(String label) {
        readPages(source(label));
        check(gp.gameState==gp.playState,"conversation concludes");
    }

    private static void npcAndSecrets() {
        for (int group=0;group<3;group++) {
            faceActor(0,gp.npc[0][0]);
            readSource("Shadow, grupo "+group);
        }
        for (String npc:new String[]{"Tuti","Coco","Candle"}) {
            Entity actor=null; int map=-1;
            for (int m=0;m<gp.npc.length;m++) for (Entity candidate:gp.npc[m])
                if (candidate!=null && candidate.getClass().getSimpleName().equals("NPC_"+npc)) { actor=candidate; map=m; }
            check(actor!=null,"NPC exists "+npc);
            faceActor(map,actor);
            readSource(npc);
        }
        for (int map:new int[]{Temple.MAP,TempleExtension.MAP}) for (Entity rock:gp.npc[map]) {
            if (!(rock instanceof NPC_BigRock)) continue;
            int x=rock.worldX,y=rock.worldY;
            faceActor(map,rock);
            readSource("BigRock, todas sus instancias");
            check(rock.worldX==x && rock.worldY==y,"reading does not push a puzzle rock");
        }
        int[][] early={{0,23,39},{0,12,19},{1,8,26},{1,26,7},{2,27,33}};
        String[] facing={"down","up","up","up","left"};
        for (int i=0;i<early.length;i++)
            secret(3+i,early[i][0],early[i][1],early[i][2],facing[i]);
        for (int i=0;i<FourthZone.SECRETS.length;i++) {
            int[] cell=FourthZone.SECRETS[i];
            secret(8+i,FourthZone.MAP,cell[0],cell[1],"up");
        }
        secret(11,3,14,5,"up");
        for (int i=0;i<Temple.MURALS.length;i++)
            secret(12+i,Temple.MAP,Temple.MURALS[i][0],Temple.MURALS[i][1]+2,"up");
        for (int i=0;i<TempleExtension.INSCRIPTIONS.length;i++) {
            var spot=TempleExtension.INSCRIPTIONS[i];
            secret(15+i,TempleExtension.MAP,spot.readX(),spot.readY(),spot.facing());
        }
    }

    private static void secret(int group,int map,int x,int y,String direction) {
        check(Temple.terrainClear(gp,map,Temple.bounds(gp.player,x*48,y*48)),"floor beside inscription "+group);
        place(map,x*48,y*48,direction);
        tap(VK_ENTER);
        check(gp.gameState==gp.dialogueState && gp.ui.npc==gp.eHandler.eventMaster,"inscription responds "+group);
        for (String label:table.keySet()) if (label.startsWith(group+" · ")) { readSource(label); return; }
        throw new AssertionError("Missing inscription "+group);
    }

    private static void refugeRoute() {
        int[] order={5,7,0,6,2,4,3,1};
        for (int index:order) {
            int[] door=PORTALS[index];
            place(door[0],door[1]*48,door[2]*48,"up");
            gp.eHandler.checkEvent();
            check(gp.gameState==gp.transitionState,"original entrance "+ROOMS[index]);
            tick(50);
            var room=ExtraRooms.Room.values()[index];
            check(gp.extraRooms.active()==room,"correct refuge "+ROOMS[index]);
            faceActor(5,roomNpc(room));
            readSource(ROOMS[index]);
            place(5,door[3]*48,door[4]*48,"up");
            gp.eHandler.checkEvent();
            tick(50);
            check(gp.currentMap==door[0],"return from "+ROOMS[index]);
        }
        check(gp.extraRooms.flags()==0 && gp.extraRooms.later.state().equals(RefugeChallenges.EMPTY)
            && gp.extraRooms.ending.state().equals(FinalRefuges.EMPTY),"talking did not advance challenges");
        gp.extraRooms.ending.restore(new FinalRefuges.State(3,0,3,true));
        faceActor(5,roomNpc(ExtraRooms.Room.HANDY));
        readPages(List.of("Keep your hands steady, D. You can still open what's ahead."));
        faceActor(5,roomNpc(ExtraRooms.Room.IC));
        readPages(List.of("The ice has thawed. I can still tell you where the prints were."));
    }

    private static Entity roomNpc(ExtraRooms.Room room) {
        for (Entity npc:gp.npc[5]) if (npc!=null && npc.worldX==room.npcX*48 && npc.worldY==room.npcY*48) return npc;
        throw new AssertionError("Missing NPC "+room);
    }

    private static void objectsAndMarket() throws Exception {
        place(0,13*48,31*48,"up");
        tap(VK_ENTER);
        readPages(List.of("Life and water restored. Game saved."));
        check(Files.isRegularFile(gp.saveLoad.getSavePath()),"emerald confirms written checkpoint");
        new OBJ_Key(gp).use(gp.player);
        readPages(List.of("There is no lock for this key here."));
        place(Market.MAP,22*48,25*48,"up");
        tap(VK_ENTER);
        check(gp.gameState==gp.tradeState,"same counter opens shop");
        var notice=MarketShop.class.getDeclaredField("notice");
        notice.setAccessible(true);
        check(normalized((String)notice.get(gp.shop)).equals(source("TV Supply, saludo del escaparate").get(0)),"new TV greeting");
        var image=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
        var graphics=image.createGraphics();
        graphics.setFont(gp.ui.purisaB.deriveFont(Font.PLAIN,7.5f));
        for (String line:((String)notice.get(gp.shop)).split("\\n"))
            check(graphics.getFontMetrics().stringWidth(line)<=102,"TV greeting fits");
        graphics.dispose();
        tap(VK_ESCAPE);

        place(0,13*48,31*48,"up");
        var chest=new object.OBJ_Chest(gp);
        chest.setLoot(gp.eGenerator.requireObject("red-potion"));
        int slots=gp.player.inventory.size();
        while (gp.player.inventory.size()<gp.player.maxinventorySize)
            gp.player.inventory.add(gp.eGenerator.requireObject("normal-bubble"));
        chest.interact();
        readPages(List.of("You cannot carry anymore."));
        check(!chest.opened,"full bag leaves loot intact");
        gp.player.inventory.subList(slots,gp.player.inventory.size()).clear();
        chest.interact();
        readPages(List.of("You found Red Poti!"));
        check(chest.opened,"loot delivered before confirmation");
        chest.interact();
        readPages(List.of("It's empty."));
    }

    private static void ending(boolean screenshots) throws Exception {
        gp.csManager.reset();
        gp.tracks.clear();
        gp.bossBattleOn=false;
        Progress.GodFishDefeated=true;
        gp.monster[FinalArena.MAP][FinalArena.BOSS_SLOT]=null;
        gp.obj[FinalArena.MAP][FinalArena.DOOR_SLOT]=null;
        place(FinalArena.MAP,FinalArena.EXIT_X*48,(FinalArena.EXIT_Y+2)*48,"up");
        portalAndPhoto(screenshots);
        if (screenshots) {
            waitFor(() -> normalized(gp.ui.currentDialogue).equals(exit.get(0)),160,"first ending page");
            tap(VK_ENTER);
            waitFor(() -> normalized(gp.ui.currentDialogue).equals(exit.get(1)),160,"Echo's first page");
            capture("03b-eco");
            tap(VK_ENTER);
            readPages(exit,2);
        } else readPages(exit);
        check(gp.csManager.isCoast(),"dialogue leads to the portrait on the same coast");
        waitFor(() -> gp.csManager.counter==CoastEpilogue.QUIET-1,130,"quiet pause");
        check(gp.tracks.isEmpty() && CoastEpilogue.captionAt(gp.csManager.counter)==-1,"ocean pause starts without words or music");
        if (screenshots) capture("04-retrato-oleaje");
        for (int i=0;i<coast.size();i++) {
            tick(1);
            check(CoastEpilogue.captionAt(gp.csManager.counter)==i
                && normalized(CoastEpilogue.CAPTIONS[i]).equals(coast.get(i)),"canonical coast caption "+i);
            check(CoastEpilogue.photoOffset(gp.csManager.counter)==0,"photo stays visible through the captions");
            if (screenshots) {
                capture("0"+(i+5)+"-costa-linea-"+(i+1));
                if (i==0) captureWide();
            }
            tick(CoastEpilogue.LINE-1);
            check(CoastEpilogue.captionAt(gp.csManager.counter)==i,"full caption reading interval");
        }
        tick(CoastEpilogue.LOWER+1);
        check(gp.csManager.isCoast() && CoastEpilogue.photoOffset(gp.csManager.counter)==86
            && CoastEpilogue.captionAt(gp.csManager.counter)==-1,"photo lowers before the sea hold");
        if (screenshots) capture("08-costa-sin-foto");
        waitFor(() -> !gp.csManager.isCoast(),300,"coast reaches title");
        check(gp.tracks.equals(List.of(24)),"original track begins once after the coast");
        if (screenshots) {
            awaitAudio(SURF_CLOSES,1);
            check(SURF_OPENS.get()==1 && SURF_LOOPS.get()==1 && SURF_ACTIVE.get()==0 && SURF_PEAK.get()==1,
                "one surf loop survives walk and dialogue and closes before credits");
            capture("09-titulo");
        }
        waitFor(() -> gp.csManager.scenePhase==CutsceneManager.ENDING_CREDITS,260,"title leads to credits");
        check(gp.csManager.endCredit.contains("David Silva Padilla") && gp.csManager.endCredit.contains("Agradecimientos especiales"),
            "original credits");
        if (screenshots) capture("10-creditos");
    }

    private static void portalAndPhoto(boolean screenshots) throws Exception {
        int exitSlot=FinalArena.DOOR_SLOT+1;
        check(gp.obj[FinalArena.MAP][exitSlot] instanceof OBJ_Exit,"portal exists after load");
        if (screenshots) {
            capture("00-portal");
            int[] first=gp.tempScreen.getRGB(450,145,60,75,null,0,60);
            tick(14);
            gp.drawToTempScreen();
            check(!Arrays.equals(first,gp.tempScreen.getRGB(450,145,60,75,null,0,60)),
                "portal atlas advances on game ticks");
        }
        key(VK_ENTER,true);
        key(VK_W,true);
        waitFor(() -> gp.csManager.sceneNum==gp.csManager.ending,70,"walk to portal");
        key(VK_W,false);
        int x=gp.player.worldX,y=gp.player.worldY;
        tick(20);
        check(!gp.csManager.isBeachWalk() && !gp.csManager.isDialogueActive(),"portal starts with a fade only");
        waitFor(gp.csManager::isBeachWalk,90,"portal reaches playable coast");
        if (screenshots) {
            awaitAudio(SURF_LOOPS,1);
            capture("01-llegada");
        }
        int items=gp.player.inventory.size(), life=gp.player.life, water=gp.player.water;
        var save=gp.saveLoad;
        byte[] before=Files.readAllBytes(save.getSavePath());
        for (int code:new int[]{VK_W,VK_S,VK_F,VK_SPACE,VK_ESCAPE,VK_C,VK_M,VK_P})
            tap(code);
        check(gp.csManager.isBeachWalk() && gp.player.life==life && gp.player.water==water,
            "arena controls and damage are inactive");
        check(!save.trySave() && Arrays.equals(before,Files.readAllBytes(save.getSavePath())),
            "beach cannot write a checkpoint");

        key(VK_D,true);
        tick(160);
        key(VK_D,false);
        int forward=beachPosition();
        tick(100);
        check(beachPosition()==forward,"fish stays put while observing the sea");
        key(VK_A,true);
        tick(70);
        key(VK_A,false);
        check(beachPosition()<forward,"player can backtrack");
        check(gp.csManager.isBeachWalk(),"backtracking cannot reopen the portal");
        if (screenshots) capture("02-paseo");
        key(VK_D,true);
        tick(420);
        check(gp.csManager.isBeachWalk(),"photo is still visible shortly before contact");
        if (screenshots) capture("02b-foto-en-la-arena");
        waitFor(gp.csManager::isDialogueActive,90,"touching the photo starts dialogue");
        key(VK_D,false);
        for (int i=0;i<80;i++) { key(VK_ENTER,true); tick(1); }
        check(gp.ui.npc!=null && gp.ui.npc.dialogueIndex==0,"held Enter cannot skip pickup dialogue");
        key(VK_ENTER,false);
        check(gp.player.worldX==x && gp.player.worldY==y && gp.player.inventory.size()==items,
            "photo is independent of arena position and inventory");
        if (screenshots) capture("03-recogida");
    }

    private static int beachPosition() throws Exception {
        var scene=CutsceneManager.class.getDeclaredField("coast");
        scene.setAccessible(true);
        var position=CoastEpilogue.class.getDeclaredField("fishX");
        position.setAccessible(true);
        return position.getInt(scene.get(gp.csManager));
    }

    private static void awaitAudio(AtomicInteger count,int expected) throws InterruptedException {
        for (int i=0;i<200 && count.get()<expected;i++) Thread.sleep(10);
        check(count.get()==expected,"surf worker completed");
    }

    private static void checkBeachAssets() throws Exception {
        Path source=Path.of("design/lore/playa/assets");
        for (String name:List.of("atardecer.png","mar.png","arena-detalle.png","portal.png",
                "foto-item.png","foto.png","oleaje.wav")) {
            try (var stream=LoreRegression.class.getResourceAsStream("/ending/"+name)) {
                check(stream!=null && Arrays.equals(Files.readAllBytes(source.resolve(name)),stream.readAllBytes()),
                    "prepared beach asset preserved: "+name);
            }
        }
        try (var stream=javax.sound.sampled.AudioSystem.getAudioInputStream(
                LoreRegression.class.getResource("/ending/oleaje.wav"))) {
            var format=stream.getFormat();
            check(format.getSampleRate()==22050 && format.getChannels()==1 && format.getSampleSizeInBits()==16
                && stream.getFrameLength()==22050*12,"manifest wave audio matches packaged PCM");
        }
        int frame=0, wrap=0;
        for (int tick=1;tick<175;tick++) {
            int next=CoastEpilogue.frame(tick,90,16);
            check(next==frame || next==(frame+1)%16,"sea advances each consecutive frame");
            if (next==0 && frame==15) wrap++;
            frame=next;
        }
        check(wrap==2,"sea loops twice without skipping frames");
    }

    private static void legacyBeach() throws Exception {
        gp.se.volumeScale=0;
        int slot=FinalArena.DOOR_SLOT+1;
        for (String name:List.of("pending","consumed","open")) {
            gp.resetGame(true);
            Path file=scratch.resolve(name+".dat");
            Path old=Path.of("build/beach-baseline",name+".dat");
            var save=new SaveLoad(gp,file);
            if (Files.isRegularFile(old)) Files.copy(old,file);
            else {
                place(FinalArena.MAP,FinalArena.KEY_X*48,(FinalArena.KEY_Y+1)*48,"up");
                Progress.GodFishDefeated=true;
                gp.monster[FinalArena.MAP][FinalArena.BOSS_SLOT]=null;
                if (name.equals("pending"))
                    gp.obj[FinalArena.MAP][FinalArena.KEY_SLOT]=Temple.place(new OBJ_Key(gp),FinalArena.KEY_X,FinalArena.KEY_Y);
                else gp.obj[FinalArena.MAP][FinalArena.DOOR_SLOT]=null;
                if (name.equals("consumed")) gp.obj[FinalArena.MAP][slot]=null;
                check(save.trySave(),save.getLastError());
            }
            gp.saveLoad=save;
            check(save.tryLoad(),save.getLastError());
            check(Progress.GodFishDefeated,"loaded victory "+name);
            if (name.equals("pending")) {
                gp.player.pickUpObject(slot);
                check(gp.csManager.sceneNum==gp.csManager.NA && gp.obj[FinalArena.MAP][slot]!=null,
                    "locked portal rejects without disappearing");
                key(VK_W,true);
                waitFor(() -> gp.player.searchItemInInventory("Key")!=999,40,"pending key picked up");
                key(VK_W,false);
                var door=gp.obj[FinalArena.MAP][FinalArena.DOOR_SLOT];
                place(FinalArena.MAP,door.worldX,door.worldY+48-gp.player.solidArea.y+gp.player.speed-1,"up");
                tap(VK_C);
                int index=gp.player.searchItemInInventory("Key");
                gp.ui.playerSlotCol=index%5;
                gp.ui.playerSlotRow=index/5;
                tap(VK_ENTER);
                check(gp.obj[FinalArena.MAP][FinalArena.DOOR_SLOT]==null,"pending key opens original door");
                readPages(List.of("You use the Key and open the door"));
                place(FinalArena.MAP,FinalArena.EXIT_X*48,(FinalArena.EXIT_Y+2)*48,"up");
                portalAndPhoto(false);
                readPages(exit);
                waitFor(() -> gp.csManager.scenePhase==CutsceneManager.ENDING_CREDITS,1600,
                    "pending-key save reaches credits");
            } else {
                check(gp.obj[FinalArena.MAP][FinalArena.DOOR_SLOT]==null
                    && (gp.obj[FinalArena.MAP][slot]==null)==name.equals("consumed"),
                    "open door and consumed ending keep their saved state");
            }
            System.out.println("BEACH save "+name+" from 356bb1b="+Files.isRegularFile(old));
        }
    }

    private static void capture(String name) throws Exception {
        int counter=gp.csManager.counter;
        FifthZoneRegression.capture(gp,OUT,name);
        check(gp.csManager.counter==counter,"draw does not change ending time");
    }

    private static void captureWide() throws Exception {
        gp.setSize(1280,720);
        var wide=new BufferedImage(1280,720,BufferedImage.TYPE_INT_ARGB);
        var graphics=wide.createGraphics();
        gp.paintComponent(graphics);
        graphics.dispose();
        check(wide.getRGB(0,360)==Color.BLACK.getRGB() && wide.getRGB(1279,360)==Color.BLACK.getRGB(),
            "the wide coast preserves aspect ratio");
        ImageIO.write(wide,"png",OUT.resolve("11-costa-pantalla-ancha.png").toFile());
    }

    private static void checkLayout() throws Exception {
        BufferedImage image=new BufferedImage(960,576,BufferedImage.TYPE_INT_ARGB);
        var graphics=image.createGraphics();
        graphics.setFont(gp.ui.purisaB.deriveFont(Font.PLAIN,23f));
        var fm=graphics.getFontMetrics();
        List<Entity> actors=new ArrayList<>();
        for (var map:gp.npc) for (Entity actor:map) if (actor!=null) actors.add(actor);
        actors.add(gp.eHandler.eventMaster);
        actors.add(new OBJ_Exit(gp));
        actors.add(gp.finalArena.boss());
        for (Entity actor:actors) for (String[] group:actor.dialogues) for (String page:group) {
            if (page==null) continue;
            var lines=page.split("\\n");
            check(lines.length<=(actor==gp.finalArena.boss()?2:4),"page fits dialogue height: "+page);
            for (String line:lines) check(fm.stringWidth(line)<=576,"page fits dialogue width: "+line);
        }
        for (int i=0;i<god.size();i++)
            check(normalized(gp.finalArena.boss().dialogues[0][i]).equals(god.get(i)),"intro page "+i);
        check(gp.finalArena.boss().dialogues[0][4]==null,"intro has four pages");
        graphics.dispose();
    }

    private static void legacyDialogue() throws Exception {
        gp.resetGame(true);
        Path checkpoint=scratch.resolve("dialogue-v1.dat");
        var save=new SaveLoad(gp,checkpoint);
        Path original=Path.of("build/lore-baseline/save-v1.dat");
        boolean historical=Files.isRegularFile(original);
        if (historical) Files.copy(original,checkpoint);
        else {
            gp.npc[0][0].dialogueSet=1;
            gp.npc[0][0].dialogueIndex=4;
            for (var npc:gp.npc[5]) if (npc instanceof NPC_Ic || npc instanceof NPC_Pump) npc.dialogueIndex=3;
            gp.player.coin=73;
            check(save.trySave(),save.getLastError());
        }
        byte[] old=Files.readAllBytes(checkpoint);
        gp.player.coin=0;
        check(save.tryLoad(),save.getLastError());
        check(gp.player.coin==73 && Arrays.equals(old,Files.readAllBytes(checkpoint)),"legacy save stays intact");
        check(gp.npc[0][0].dialogueIndex==4 && gp.npc[0][0].dialogueSet==1,"old cursor and group loaded");
        faceActor(0,gp.npc[0][0]);
        readSource("Shadow, grupo 1");
        faceActor(0,gp.npc[0][0]);
        readSource("Shadow, grupo 2");
        for (int index:new int[]{3,6}) {
            Entity actor=roomNpc(ExtraRooms.Room.values()[index]);
            check(actor.dialogueIndex==3,"old refuge index loaded");
            faceActor(5,actor);
            readSource(ROOMS[index]);
        }
        System.out.println("LORE legacy dialogue checkpoint from 1a609a8="+historical);
    }
}
