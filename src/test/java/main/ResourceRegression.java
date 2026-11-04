package main;

import ai.Node;
import entity.Entity;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import tile.Map;
import tile_interactive.InteractiveTile;

/** Dependency-free checks for preserved pixels, bounded resources and correct routes. */
public final class ResourceRegression {
    private static int assertions;
    private static final class QuietPanel extends GamePanel {
        @Override public void playSE(int sound) { }
        @Override public void playMusic(int sound) { }
        @Override public void stopMusic() { }
    }
    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        Path assets = args.length == 0 ? Path.of("src/main/resources") : Path.of(args[0]);
        checkSpritePixels(assets);
        QuietPanel gp = new QuietPanel();
        gp.aSetter.setObject(); gp.aSetter.setNPC(); gp.aSetter.setMonster(); gp.aSetter.setInteractiveTile();
        check(gp.monster[0][0].down == gp.monster[0][1].down, "Identical enemies share sprite data");
        checkMaps(gp);
        checkTerrainCulling(gp);
        checkRoutes(gp);
        checkAudio();
        System.out.println("RESOURCE_REGRESSION PASS assertions=" + assertions);
    }
    private static void checkSpritePixels(Path assets) throws Exception {
        int images = 0;
        try (var files = Files.walk(assets)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".png")).toList()) {
                String resource = "/" + assets.relativize(file).toString().replace('\\', '/');
                if(resource.startsWith("/quinta/") || resource.startsWith("/sexta/")) continue; // Native candidate art has its own manifest/hash suite.
                BufferedImage original = ImageIO.read(file.toFile());
                int width = original.getWidth() * 3, height = original.getHeight() * 3;
                BufferedImage reference = new UtilityTool().scaleImage(original, width, height);
                BufferedImage cached = SpriteCache.load(resource, width, height);
                check(Arrays.equals(reference.getRGB(0, 0, width, height, null, 0, width),
                        cached.getRGB(0, 0, width, height, null, 0, width)), "Changed sprite pixels: " + resource);
                check(cached == SpriteCache.load(resource, width, height), "Cache did not reuse image: " + resource);
                images++;
            }
        }
        check(images == 291, "Expected 291 original PNG resources, got " + images);
        System.out.println("SPRITES exact original scaling pixels=" + images);
    }
    private static void checkMaps(QuietPanel gp) {
        Map preview = gp.map;
        check(gp.tileM.loadedMapCount() == 9, "Nine maps must be loaded");
        check(preview.cachedPixelBytes() == 0, "Map constructor must not allocate previews");
        for (int map = 0; map < 9; map++) {
            BufferedImage image = preview.getWorldMap(map);
            check(image.getWidth() == 800 && image.getHeight() == 800, "Native terrain preview dimensions");
            check(image == preview.getWorldMap(map), "Preview must be cached");
        }
        check(preview.getWorldMap(9) == null, "Unused map must not allocate a preview");
        check(preview.cachedPixelBytes() == 9L * 800 * 800 * 4, "Unexpected map cache size");
        long allMaps = preview.cachedPixelBytes();
        gp.currentMap = 2;
        BufferedImage before = preview.getWorldMap(2);
        gp.tileM.reloadCurrentMap();
        BufferedImage after = preview.getWorldMap(2);
        check(before != after, "Reload must invalidate the corresponding preview");
        check(Arrays.equals(before.getRGB(0, 0, 800, 800, null, 0, 800),
                after.getRGB(0, 0, 800, 800, null, 0, 800)), "Reload changed original terrain");
        int[][] unchanged = gp.tileM.mapTileNum[2];
        try {
            gp.tileM.loadMap("/maps/does-not-exist.txt", 2);
            throw new AssertionError("A missing map must report failure");
        } catch (IllegalStateException expected) {
            check(gp.tileM.mapTileNum[2] == unchanged, "Failed reload changed terrain");
        }
        BufferedImage frame = new BufferedImage(960, 576, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = frame.createGraphics();
        try { preview.drawFullMapScreen(graphics); preview.miniMapOn = true; preview.drawMiniMap(graphics); }
        finally { graphics.dispose(); }
        System.out.printf("MAP_CACHE initial=0 one=2560000 all_eight=%d bytes (%.2f MiB)%n", allMaps, allMaps / 1048576.0);
    }
    private static void checkTerrainCulling(QuietPanel gp) {
        int[][] positions = {{0,0}, {624,1488}, {628,1492}, {647,1511}, {671,1535}, {2352,2352}};
        for (int map = 0; map < 9; map++) {
            gp.currentMap = map;
            for (int[] position : positions) {
                gp.player.worldX = position[0]; gp.player.worldY = position[1];
                BufferedImage expected = new BufferedImage(960, 576, BufferedImage.TYPE_INT_ARGB);
                BufferedImage actual = new BufferedImage(960, 576, BufferedImage.TYPE_INT_ARGB);
                Graphics2D reference = expected.createGraphics(), rendered = actual.createGraphics();
                try {
                    for (int x = 0; x < 50; x++) for (int y = 0; y < 50; y++) {
                        int id = gp.tileM.mapTileNum[map][x][y];
                        reference.drawImage(gp.tileM.tile[id].image,
                                x * 48 - gp.player.worldX + gp.player.screenX,
                                y * 48 - gp.player.worldY + gp.player.screenY, null);
                    }
                    gp.tileM.draw(rendered);
                } finally { reference.dispose(); rendered.dispose(); }
                check(Arrays.equals(expected.getRGB(0,0,960,576,null,0,960),
                        actual.getRGB(0,0,960,576,null,0,960)), "Culled terrain omitted visible pixels");
            }
        }
        System.out.println("TERRAIN viewport matches uncropped reference at all tested map/camera positions");
    }
    private static void checkRoutes(QuietPanel gp) {
        Random random = new Random(20261028);
        int queries = 0;
        for (int map = 0; map < 9; map++) {
            gp.currentMap = map;
            boolean[][] blocked = obstacles(gp);
            for (int i = 0; i < 40; i++) {
                int sx, sy, tx, ty;
                do { sx = random.nextInt(50); sy = random.nextInt(50); } while (blocked[sx][sy]);
                do { tx = random.nextInt(50); ty = random.nextInt(50); } while (blocked[tx][ty] || sx == tx && sy == ty);
                int expected = breadthFirstDistance(blocked, sx, sy, tx, ty);
                gp.pFinder.setNodes(sx, sy, tx, ty, gp.player);
                boolean found = gp.pFinder.search();
                check(found == (expected >= 0), "Reachability mismatch on map " + map);
                if (found) {
                    check(gp.pFinder.pathList.size() == expected, "Route is not shortest on map " + map);
                    int x = sx, y = sy;
                    for (Node step : gp.pFinder.pathList) {
                        check(!blocked[step.col][step.row], "Route crosses obstacle");
                        check(Math.abs(step.col - x) + Math.abs(step.row - y) == 1, "Non-cardinal step");
                        x = step.col; y = step.row;
                    }
                    check(x == tx && y == ty, "Route does not end at target");
                }
                queries++;
            }
        }
        gp.pFinder.setNodes(-1, 0, 0, 0, gp.player);
        check(!gp.pFinder.search(), "Out-of-bounds source should return no route");
        gp.pFinder.setNodes(2, 2, 2, 2, gp.player);
        check(!gp.pFinder.search() && gp.pFinder.pathList.isEmpty(), "Already at target requires no movement");
        System.out.println("PATHS reachability and shortest length match BFS for " + queries + " actual-map queries");
    }
    private static boolean[][] obstacles(QuietPanel gp) {
        boolean[][] blocked = new boolean[50][50];
        for (int x = 0; x < 50; x++) for (int y = 0; y < 50; y++)
            blocked[x][y] = gp.tileM.tile[gp.tileM.mapTileNum[gp.currentMap][x][y]].collision;
        for (InteractiveTile tile : gp.iTile[gp.currentMap]) {
            if (tile != null && tile.destructible) blocked[tile.worldX / 48][tile.worldY / 48] = true;
        }
        return blocked;
    }
    private static int breadthFirstDistance(boolean[][] blocked, int sx, int sy, int tx, int ty) {
        int[][] distance = new int[50][50];
        for (int[] row : distance) Arrays.fill(row, -1);
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{sx, sy}); distance[sx][sy] = 0;
        int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
        while (!queue.isEmpty()) {
            int[] point = queue.remove();
            if (point[0] == tx && point[1] == ty) return distance[tx][ty];
            for (int[] d : directions) {
                int x = point[0] + d[0], y = point[1] + d[1];
                if (x < 0 || y < 0 || x >= 50 || y >= 50 || blocked[x][y] || distance[x][y] >= 0) continue;
                distance[x][y] = distance[point[0]][point[1]] + 1;
                queue.add(new int[]{x,y});
            }
        }
        return -1;
    }
    private static int openAudioLines() {
        int count = 0;
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            for (Line line : AudioSystem.getMixer(info).getSourceLines()) if (line.isOpen()) count++;
        }
        return count;
    }
    private static void checkAudio() throws Exception {
        // Real playback at zero gain exercises paths that the old open-only tests missed.
        int initial = openAudioLines();
        Sound effects = new Sound(8);
        effects.volumeScale = 0;
        try {
            for (int index : new int[]{21, 11, 2, 21, 2, 21}) {
                effects.requestPlay(index, false);
                check(effects.awaitIdle(10000), "Audio worker timeout");
            }
            check(effects.openVoiceCount() == 3, "One cached clip per requested resource");
            effects.stop();
            check(effects.awaitIdle(10000), "Stop timeout");
            check(effects.openVoiceCount() == 3, "Stop retains clips to avoid native close stalls");
            effects.volumeScale = 100; effects.checkVolume();
            check(effects.volumeScale == 5, "Volume upper bound");
            effects.volumeScale = -100; effects.checkVolume();
            check(effects.volumeScale == 0, "Volume lower bound");
        } finally { effects.close(); check(effects.awaitClosed(15000), "Audio cleanup timeout"); }
        check(effects.openVoiceCount() == 0 && openAudioLines() == initial, "Shutdown releases audio lines");
        System.out.println("AUDIO real playback at zero gain; cache reused; all lines closed");
    }
}
