package main;

import data.SaveLoad;
import entity.Entity;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import object.OBJ_Bad_Bubble;
import object.OBJ_Chest;
import object.OBJ_Rock;

import static main.ExtraRooms.*;
import static main.ExtraRooms.Room.*;
import static main.TempleRegression.swim;
import static main.TempleRegression.route;
import static main.TempleRegression.press;

public final class ExtraRoomsRegression {
    private static final Path OUT=Path.of("build/refugios-preview");
    private static final int[][] PORTALS={
        {0,28,18,9,40},{1,9,34,40,40},{1,39,23,40,29},{2,39,13,9,29},
        {2,9,17,9,19},{2,19,16,40,19},{3,14,40,9,9},{3,9,5,40,9}
    };
    private static final String[] LOOT={"lantern","red-potion","bed","red-potion","super-shield","rock-tool","toxic-bubble","iron-tool"};
    private static final int[] CHESTS={1,2,3,4,6,5,7,8};

    private static void check(boolean ok,String label) {
        if (!ok) throw new AssertionError(label);
    }

    private static final class Probe extends Entity {
        int updates,draws;
        Probe(GamePanel gp) { super(gp); alive=true; }
        @Override public void update() { updates++; }
        @Override public void draw(Graphics2D g) { draws++; g.fillRect(0,0,960,576); }
    }

    public static void main(String[] args) throws Exception {
        var gp=new FifthZoneRegression.Quiet();
        gp.setupGame();
        Files.createDirectories(OUT);
        for (int i=0;i<PORTALS.length;i++) {
            int[] p=PORTALS[i];
            transition(gp,p[0],p[1],p[2],MAP,Room.values()[i]);
            check(gp.extraRooms.active()==Room.values()[i],"original entry to "+Room.values()[i]);
            if (i<3) {
                Room room=Room.values()[i];
                Entity npc=npc(gp,room);
                check(npc.worldX==room.npcX*48 && npc.worldY==room.npcY*48,"correct NPC slot");
                capture(gp,room.art+"-npc");
            }
            var chest=gp.obj[MAP][CHESTS[i]];
            check(chest instanceof OBJ_Chest && ExtraRooms.at(chest.worldX/48,chest.worldY/48)==Room.values()[i] &&
                gp.eGenerator.idOf(chest.loot).equals(LOOT[i]),"original reward "+i+" got "+chest.loot.name);
            if (i==1) {
                int health=gp.player.life;
                gp.player.life=0;
                chest.loot.use(gp.player);
                check(gp.player.life==5,"original potion keeps its five-point heal");
                gp.player.life=health;
                gp.gameState=gp.playState;
            }
            transition(gp,MAP,p[3],p[4],p[0],null);
            check(gp.player.worldX==p[1]*48 && gp.player.worldY==p[2]*48,"original return "+i);
        }
        chicken(gp);
        flower(gp);
        spiky(gp);
        corners(gp);
        int completed=gp.extraRooms.flags();
        gp.resetGame(false);
        check(gp.extraRooms.flags()==completed,"retry preserves refuge progress");
        gp.resetGame(true);
        check(gp.extraRooms.flags()==0 && gp.monster[MAP][GREENY]!=null && !gp.obj[MAP][1].opened,
            "new game resets optional challenges and chests");
        gp.stopGame();
        System.out.println("REFUGIOS PASS: 8 round trips and rewards, 32 corners, transition and map isolation, lamps, Greeny, two sentries, saves and full inventory");
    }

    private static Entity npc(GamePanel gp,Room room) {
        return Arrays.stream(gp.npc[MAP]).filter(e->e!=null && ExtraRooms.at(e.worldX/48,e.worldY/48)==room)
            .findFirst().orElseThrow();
    }

    static void transition(FifthZoneRegression.Quiet gp,int map,int x,int y,int dest,Room room) {
        gp.currentMap=map;
        gp.gameState=gp.playState;
        gp.eHandler=new EventHandler(gp);
        Temple.place(gp.player,x,y);
        Room source=gp.extraRooms.active();
        gp.eHandler.checkEvent();
        check(gp.gameState==gp.transitionState,"transition starts from "+map+" "+x+","+y);
        for (int i=0;i<49;i++) gp.flow.update();
        check(gp.currentMap==map && gp.extraRooms.active()==source,"source held until transition midpoint");
        gp.drawToTempScreen();
        gp.flow.update();
        check(gp.currentMap==dest && gp.extraRooms.active()==room,"room and camera switch together");
        gp.eHandler.checkEvent();
        check(gp.gameState==gp.playState,"no portal bounce");
        gp.drawToTempScreen();
    }

    static void setRoom(GamePanel gp,Room room,int x,int y) {
        gp.currentMap=MAP;
        gp.currentArea=gp.nextArea=gp.indoor;
        gp.gameState=gp.playState;
        gp.eHandler=new EventHandler(gp);
        Temple.place(gp.player,x,y);
        gp.keyH.releaseAll();
        check(gp.extraRooms.active()==room,"active room "+room);
    }

    static void interact(GamePanel gp,String direction) {
        gp.player.direction=direction;
        gp.keyH.enterPressed=true;
        gp.eHandler.checkEvent();
        gp.keyH.enterPressed=false;
        gp.player.attackCanceled=false;
    }

    static SaveLoad roundTrip(GamePanel gp,String tag) throws Exception {
        Path file=Files.createTempDirectory(Path.of("build"),"refugios-"+tag+"-").resolve("save-v1.dat");
        SaveLoad save=new SaveLoad(gp,file);
        check(save.trySave(),save.getLastError());
        byte[] data=Files.readAllBytes(file);
        gp.extraRooms.restore(0);
        check(save.tryLoad(),save.getLastError());
        check(Arrays.equals(data,Files.readAllBytes(file)),"load does not rewrite "+tag);
        gp.gameState=gp.playState;
        return save;
    }

    private static boolean reach(GamePanel gp,int x,int y,int tx,int ty) {
        return route(gp,x,y)[ty*50+tx]>=0;
    }

    static void capture(FifthZoneRegression.Quiet gp,String name) throws Exception {
        gp.ui.message.clear();
        gp.ui.messageCounter.clear();
        FifthZoneRegression.capture(gp,OUT,name);
    }

    static void fullAndClaim(FifthZoneRegression.Quiet gp,Room room,int slot) throws Exception {
        Entity chest=gp.obj[MAP][slot];
        var inventory=new ArrayList<>(gp.player.inventory);
        while (gp.player.inventory.size()<gp.player.maxinventorySize) gp.player.inventory.add(new OBJ_Rock(gp));
        useChest(gp,chest);
        check(!chest.opened,"full inventory does not consume "+room+" loot");
        gp.player.inventory.clear();
        gp.player.inventory.addAll(inventory);
        gp.gameState=gp.playState;
        useChest(gp,chest);
        check(chest.opened,"chest opened "+room);
        gp.gameState=gp.playState;
        capture(gp,room.art+"-cofre");
        roundTrip(gp,room.art+"-claimed");
        check(gp.obj[MAP][slot].opened,"claimed reward persists "+room);
    }

    static void useChest(GamePanel gp,Entity chest) {
        int dx=chest.worldX-gp.player.worldX,dy=chest.worldY-gp.player.worldY;
        gp.player.direction=Math.abs(dx)>Math.abs(dy) ? (dx<0?"left":"right") : (dy<0?"up":"down");
        press(gp,gp.player.direction);
        for (int step=0;step<20;step++) {
            int target=gp.cChecker.checkObject(gp.player,true);
            if (target!=999 && gp.obj[MAP][target]==chest) break;
            gp.player.update();
        }
        press(gp,"");
        gp.keyH.enterPressed=true;
        gp.player.update();
        gp.keyH.enterPressed=false;
        check(gp.gameState==gp.dialogueState,"chest interacts through player collision");
        gp.player.attackCanceled=false;
    }

    private static void chicken(FifthZoneRegression.Quiet gp) throws Exception {
        for (int first=0;first<2;first++) {
            gp.extraRooms.restore(0);
            setRoom(gp,CHICKEN,10,40);
            check(!reach(gp,10,40,20,40),"lantern gate starts solid");
            int[][] support={{11,40},{15,36}};
            swim(gp,10,40,support[first][0],support[first][1]);
            interact(gp,"right");
            check(gp.extraRooms.flags()==1<<first,"first lamp of order "+first);
            roundTrip(gp,"chicken-mid");
            check(gp.extraRooms.flags()==1<<first && !open(CHICKEN,gp.extraRooms.flags()),"partial lamps restored");
            int other=1-first;
            swim(gp,support[first][0],support[first][1],support[other][0],support[other][1]);
            interact(gp,"right");
            check(open(CHICKEN,gp.extraRooms.flags()),"both lamps open gate");
            swim(gp,support[other][0],support[other][1],20,40);
            check(reach(gp,20,40,9,40),"return free");
        }
        fullAndClaim(gp,CHICKEN,FIRST_CHEST);
    }

    private static void flower(FifthZoneRegression.Quiet gp) throws Exception {
        setRoom(gp,FLOWER,39,40);
        check(!reach(gp,39,40,29,37),"remedy gate starts solid");
        Entity greeny=gp.monster[MAP][GREENY];
        for (int i=0;i<220;i++) gp.update();
        check(greeny.worldX>=32*48 && greeny.worldX<=37*48 && greeny.worldY>=36*48 && greeny.worldY<=40*48,
            "Greeny remains in its own arena");
        gp.player.damageMonster(GREENY,gp.player,1,0);
        roundTrip(gp,"flower-mid");
        greeny=gp.monster[MAP][GREENY];
        check(greeny.life==3,"partial fight survives checkpoint");
        gp.player.nextLevelExp=1000;
        int exp=gp.player.exp;
        gp.player.damageMonster(GREENY,gp.player,20,0);
        gp.player.damageMonster(GREENY,gp.player,20,0);
        check(gp.player.exp==exp+greeny.exp && open(FLOWER,gp.extraRooms.flags()),"one reward and open gate");
        roundTrip(gp,"flower-defeat");
        gp.changeArea();
        check(gp.monster[MAP][GREENY]==null && gp.player.exp==exp+greeny.exp,"return and load cannot farm Greeny");
        swim(gp,39,40,29,37);
        check(reach(gp,29,37,40,40),"Flower return free");
        fullAndClaim(gp,FLOWER,FIRST_CHEST+1);
    }

    private static void spiky(FifthZoneRegression.Quiet gp) throws Exception {
        setRoom(gp,SPIKY,39,29);
        int[] from=route(gp,39,29);
        check(from[31*50+36]<0,"shortcut starts solid");
        int cell=32*50+33;
        boolean upper=false,lower=false;
        for (;cell!=29*50+39;cell=from[cell]) {
            check(cell>=0,"chest reachable by the two tracks");
            upper|=cell==26*50+36; lower|=cell==30*50+30;
        }
        check(upper && lower,"initial route goes through both tracks");
        for (int i=0;i<120;i++) gp.update();
        check(shots(gp)==0,"Spiky arrival is protected");

        swim(gp,39,29,36,26);
        gp.update();
        check(shots(gp)==1,"first sentry fires into crossing");
        press(gp,"left");
        for (int i=0;i<12;i++) gp.update();
        press(gp,"up");
        for (int i=0;i<12;i++) gp.update();
        press(gp,"");
        check(gp.player.worldX==35*48 && gp.player.worldY==25*48 && gp.player.life==gp.player.maxLife,
            "first dodge reaches cover without damage");
        roundTrip(gp,"spiky-mid");
        check(!open(SPIKY,gp.extraRooms.flags()) && gp.monster[MAP][GUARD_ONE]!=null,"mid-trial save retains sentry");
        for (int i=0;i<120;i++) gp.update();

        swim(gp,35,25,30,30);
        gp.update();
        check(shots(gp)==1,"second sentry fires alone");
        press(gp,"right");
        for (int i=0;i<12;i++) gp.update();
        press(gp,"down");
        for (int i=0;i<12;i++) gp.update();
        press(gp,"");
        check(gp.player.worldX==31*48 && gp.player.worldY==31*48 && gp.player.life==gp.player.maxLife,
            "second dodge reaches cover without damage");
        swim(gp,31,31,35,31);
        interact(gp,"right");
        check(open(SPIKY,gp.extraRooms.flags()),"shortcut opens from safe side with both sentries alive");
        roundTrip(gp,"spiky-shortcut");
        check(open(SPIKY,gp.extraRooms.flags()) && reach(gp,35,31,40,29),"open shortcut and return survive load");
        swim(gp,35,31,33,32);
        fullAndClaim(gp,SPIKY,FIRST_CHEST+2);

        gp.extraRooms.restore(gp.extraRooms.flags() & ~SHORTCUT);
        setRoom(gp,SPIKY,39,29);
        gp.player.damageMonster(GUARD_ONE,gp.player,100,0);
        roundTrip(gp,"spiky-one-defeat");
        check(gp.monster[MAP][GUARD_ONE]==null && gp.monster[MAP][GUARD_TWO]!=null,
            "individual sentry defeat survives partial save");
        gp.player.damageMonster(GUARD_TWO,gp.player,100,0);
        roundTrip(gp,"spiky-defeated");
        gp.changeArea();
        check(gp.monster[MAP][GUARD_ONE]==null && gp.monster[MAP][GUARD_TWO]==null,"sentry defeats survive return");
        swim(gp,39,29,35,31);
        interact(gp,"right");
        check(open(SPIKY,gp.extraRooms.flags()),"shortcut can open after defeating both sentries");
    }

    private static int shots(GamePanel gp) {
        return (int)Arrays.stream(gp.projectile[MAP]).filter(e->e instanceof OBJ_Bad_Bubble && e.alive).count();
    }

    private static int[] pixels(BufferedImage image) {
        return image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth());
    }

    static void corners(FifthZoneRegression.Quiet gp) throws Exception {
        gp.map.miniMapOn=true;
        for (Room room : Room.values()) {
            Room foreign=room.x==9 ? FLOWER : CHICKEN;
            for (int x : new int[]{room.x,room.x+13}) for (int y : new int[]{room.y,room.bottom}) {
                setRoom(gp,room,x,y);
                Probe enemy=new Probe(gp),shot=new Probe(gp),prop=new Probe(gp);
                for (Probe probe : new Probe[]{enemy,shot,prop}) Temple.place(probe,foreign.x+2,foreign.y+2);
                gp.drawToTempScreen();
                int[] frame=pixels(gp.tempScreen);
                gp.monster[MAP][99]=enemy;
                gp.projectile[MAP][49]=shot;
                gp.obj[MAP][29]=prop;
                gp.drawToTempScreen();
                check(Arrays.equals(frame,pixels(gp.tempScreen)),"no foreign actors in corner "+room+" "+x+","+y);
                gp.gameState=gp.mapState;
                gp.drawToTempScreen();
                int[] full=pixels(gp.tempScreen);
                int fx=foreign.x+2,fy=foreign.y+2;
                int old=gp.tileM.mapTileNum[MAP][fx][fy];
                gp.tileM.setTerrainCell(MAP,fx,fy,124);
                gp.drawToTempScreen();
                check(Arrays.equals(full,pixels(gp.tempScreen)),"full map only shows active room");
                gp.gameState=gp.playState;
                // The map screen selects its own font. Warm one frame before comparing world pixels.
                gp.drawToTempScreen();
                gp.drawToTempScreen();
                if (!Arrays.equals(frame,pixels(gp.tempScreen))) {
                    int[] changed=pixels(gp.tempScreen);
                    int mismatch=Arrays.mismatch(frame,changed);
                    javax.imageio.ImageIO.write(gp.tempScreen,"png",OUT.resolve("isolation-failure.png").toFile());
                    throw new AssertionError("world isolation "+room+" "+x+","+y+" first differing pixel "+mismatch%960+","+mismatch/960);
                }
                gp.tileM.setTerrainCell(MAP,fx,fy,old);
                gp.update();
                check(enemy.updates==0 && shot.updates==0 && enemy.draws==0 && shot.draws==0 && prop.draws==0,"foreign actors inactive");
                gp.monster[MAP][99]=null; gp.projectile[MAP][49]=null; gp.obj[MAP][29]=null;
                if (room.art!=null && x==room.x && y==room.y) capture(gp,room.art+"-esquina");
                if (room==IC || room==HANDY)
                    capture(gp,room.art+"-esquina-"+(y==room.y?"n":"s")+(x==room.x?"o":"e"));
            }
        }
    }
}
