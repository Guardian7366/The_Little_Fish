package main;

import entity.Entity;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import javax.imageio.ImageIO;

/** Real map/actors, two minutes of simulation, non-modal interaction and rendering. */
public final class FirstZoneRegression {
    private static int assertions;
    private static final class QuietPanel extends GamePanel {
        @Override public void playSE(int i) { }
        @Override public void playMusic(int i) { }
        @Override public void stopMusic() { }
    }
    private static void check(boolean condition, String contract) {
        assertions++;
        if (!condition) throw new AssertionError(contract);
    }
    public static void main(String[] args) throws Exception {
        QuietPanel gp = new QuietPanel();
        gp.setupGame(); gp.gameState = gp.playState;
        routes(gp); territories(gp); hintsAndSecrets(gp); layers(gp);
        preview(gp);
        gp.music.close(); gp.se.close();
        System.out.println("FIRST_ZONE_REGRESSION PASS assertions=" + assertions);
    }

    private static void routes(QuietPanel gp) {
        boolean[][] seen = new boolean[50][50];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{13,31}); seen[13][31] = true;
        while (!queue.isEmpty()) {
            int[] p = queue.remove();
            for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int x = p[0]+d[0], y = p[1]+d[1];
                if (x < 0 || y < 0 || x >= 50 || y >= 50 || seen[x][y] ||
                    gp.tileM.tile[gp.tileM.mapTileNum[0][x][y]].collision) continue;
                seen[x][y] = true; queue.add(new int[]{x,y});
            }
        }
        for (int[] p : new int[][]{{38,25},{28,18},{12,19},{23,39},{15,29},
                {22,31},{22,22},{33,21},{33,30},{37,29},{13,22}})
            check(seen[p[0]][p[1]], "Unreachable encounter, landmark or exit " + Arrays.toString(p));
        for (int x=0; x<50; x++) for (int y=0; y<50; y++)
            if (!gp.tileM.tile[gp.tileM.mapTileNum[0][x][y]].collision)
                check(seen[x][y], "Isolated walkable tile " + x + "," + y);
        // Walk actual player collision boxes from the refuge to each destination.
        gp.npc[0][0].worldX = 15*48; // The guide is off the central walkways.
        for (int[] goal : new int[][]{{38,25},{28,18},{12,19},{23,39}}) {
            gp.pFinder.setNodes(13,31,goal[0],goal[1],gp.player);
            check(gp.pFinder.search(), "No path to destination");
            gp.player.worldX=13*48; gp.player.worldY=31*48;
            for (ai.Node node : gp.pFinder.pathList) {
                int tx=node.col*48, ty=node.row*48;
                while (gp.player.worldX != tx || gp.player.worldY != ty) {
                    gp.player.direction = gp.player.worldX < tx ? "right" : gp.player.worldX > tx ? "left" :
                        gp.player.worldY < ty ? "down" : "up";
                    gp.player.collisionOn = false;
                    gp.cChecker.checkTile(gp.player);
                    check(!gp.player.collisionOn, "Player collision blocks route");
                    switch (gp.player.direction) {
                        case "right" -> gp.player.worldX += 4; case "left" -> gp.player.worldX -= 4;
                        case "down" -> gp.player.worldY += 4; case "up" -> gp.player.worldY -= 4;
                    }
                }
            }
        }
        System.out.println("FIRST_ZONE all floor connected; both exits and secrets walked with player collision box");
    }

    private static void territories(QuietPanel gp) {
        gp.player.setDefaultPositions();
        int[][] bounds={{19,28,25,34},{19,20,25,24},{30,19,36,24},{31,27,35,33},{35,27,38,33},{10,20,15,24}};
        for (int tick=0; tick<7200; tick++) for (int i=0; i<6; i++) {
            Entity e=gp.monster[0][i];
            if (tick % 300 == 0) { e.knockBack=true; e.knockBackDirection="left"; e.speed=e.defaultSpeed+5; }
            e.update();
            int[] b=bounds[i];
            check(e.worldX>=b[0]*48 && e.worldX<=b[2]*48 && e.worldY>=b[1]*48 && e.worldY<=b[3]*48,
                "Enemy escaped encounter " + i);
            check(!gp.tileM.tile[gp.tileM.mapTileNum[0][e.worldX/48][e.worldY/48]].collision,
                "Enemy entered reef");
        }
        check(gp.player.life==gp.player.maxLife, "Refuge invaded");
        gp.aSetter.setMonster();
        System.out.println("FIRST_ZONE six territories hold for 7200 ticks with repeated knockback; refuge safe");
    }

    private static void hintsAndSecrets(QuietPanel gp) {
        gp.firstZoneGuide.reset(); gp.player.setDefaultPositions();
        check(gp.firstZoneGuide.message().contains("W A S D"), "Missing movement hint");
        gp.gameState=gp.optionsState;
        check(gp.firstZoneGuide.message().isEmpty(), "Hint overlays menu");
        gp.gameState=gp.playState;
        for (int[] point : new int[][]{{23,39},{12,19}}) {
            gp.player.worldX=point[0]*48; gp.player.worldY=point[1]*48;
            gp.player.direction=point[0]==23?"down":"up";
            gp.keyH.enterPressed=false; gp.eHandler.checkEvent();
            check(gp.gameState==gp.playState, "Secret forced a dialogue");
            gp.keyH.enterPressed=true; gp.eHandler.checkEvent();
            check(gp.gameState==gp.dialogueState && gp.player.attackCanceled, "Secret cannot be inspected");
            gp.keyH.enterPressed=false; gp.player.attackCanceled=false; gp.gameState=gp.playState;
        }
        gp.ui.npc=null;
    }

    private static BufferedImage background(QuietPanel gp) {
        BufferedImage image=new BufferedImage(960,576,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics(); gp.ocean.drawBackground(g,gp); g.dispose();
        return image;
    }
    private static void layers(QuietPanel gp) {
        HashSet<Integer> hashes=new HashSet<>();
        for(int frame=0;frame<5;frame++) {
            BufferedImage image=background(gp);
            int[] pixels=image.getRGB(0,0,960,576,null,0,960);
            hashes.add(Arrays.hashCode(pixels));
            check(Arrays.equals(pixels,background(gp).getRGB(0,0,960,576,null,0,960)), "Drawing advances animation");
            for(int i=0;i<30;i++) gp.ocean.update();
        }
        check(hashes.size()==5 && gp.ocean.frameIndex()==0, "Ocean does not have five looping frames");
        check(gp.ocean.cachedPixelBytes()<5*1024*1024, "Ocean cache too large");
        gp.gameState=gp.pauseState; gp.update();
        check(gp.ocean.frameIndex()==0, "Paused ocean keeps updating");
        gp.gameState=gp.playState;
        // Fifth (map 3) now intentionally has its own layers, covered by FifthZoneRegression.
        for(int map=5;map<7;map++) {
            gp.currentMap=map;
            BufferedImage a=new BufferedImage(960,576,2), b=new BufferedImage(960,576,2);
            Graphics2D ga=a.createGraphics(), gb=b.createGraphics();
            gp.tileM.draw(ga); gp.ocean.drawBackground(gb,gp); gp.tileM.drawLayered(gb); gp.ocean.drawForeground(gb,gp);
            ga.dispose();gb.dispose();
            check(Arrays.equals(a.getRGB(0,0,960,576,null,0,960), b.getRGB(0,0,960,576,null,0,960)),
                "Layers changed a later map");
        }
        gp.currentMap=0;
        // Independent uncropped layered reference, including edges and sub-tile camera motion.
        for (int[] pos : new int[][]{{0,0},{624,1488},{647,1511},{2352,2352}}) {
            gp.player.worldX=pos[0]; gp.player.worldY=pos[1];
            BufferedImage expected=background(gp),actual=background(gp);
            Graphics2D ref=expected.createGraphics(),draw=actual.createGraphics();
            for(int x=0;x<50;x++) for(int y=0;y<50;y++) {
                int id=gp.tileM.mapTileNum[0][x][y];
                if(id==0) continue;
                ref.setComposite(java.awt.AlphaComposite.SrcOver.derive(id==1||id==2?0.78f:1f));
                ref.drawImage(gp.tileM.tile[id].image,x*48-pos[0]+gp.player.screenX,y*48-pos[1]+gp.player.screenY,null);
            }
            gp.tileM.drawLayered(draw);ref.dispose();draw.dispose();
            check(Arrays.equals(expected.getRGB(0,0,960,576,null,0,960),actual.getRGB(0,0,960,576,null,0,960)),
                "Layered culling omitted or altered visible pixels");
        }
    }

    private static void preview(QuietPanel gp) throws Exception {
        Path folder=Path.of("build/first-zone-preview");Files.createDirectories(folder);
        ImageIO.write(gp.map.getWorldMap(0),"png",folder.resolve("map.png").toFile());
        int[][] positions={{13,31},{20,31},{21,24},{31,23},{35,32},{23,39},{12,19},{28,18}};
        gp.firstZoneGuide.reset();
        for(int[] position:positions) {
            gp.player.worldX=position[0]*48; gp.player.worldY=position[1]*48;
            gp.player.direction=position[0]==23?"down":position[0]==12?"up":"right";
            gp.firstZoneGuide.update();
            gp.drawToTempScreen();
            ImageIO.write(gp.tempScreen,"png",folder.resolve(position[0]+"-"+position[1]+".png").toFile());
        }
    }
}
