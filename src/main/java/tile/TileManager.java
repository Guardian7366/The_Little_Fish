package tile;

import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import main.GamePanel;
import main.SpriteCache;

/** Owns the single terrain table used by rendering, collision and map previews. */
public class TileManager {
    private static final AlphaComposite SHALLOW_WATER = AlphaComposite.SrcOver.derive(0.78f);
    private static final String[] INITIAL_MAPS = {
        "/maps/stage1.txt", "/maps/stage2.txt", "/maps/stage3.txt", "/maps/stage4.txt",
        "/maps/temple.txt", "/maps/extraRefugios.txt", "/maps/stageBoss.txt", "/maps/fourthDepths.txt", "/maps/sixthAbyss.txt", "/maps/templeExtension.txt", "/maps/market.txt"
    };
    private final GamePanel gp;
    public Tile[] tile;
    public int[][][] mapTileNum;
    boolean drawPath = false;
    private final List<String> fileNames = new ArrayList<>();
    private final List<Boolean> collisionStatus = new ArrayList<>();
    private final String[] mapResources;
    private final int[] mapRevisions;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        mapResources = new String[gp.maxMap];
        mapRevisions = new int[gp.maxMap];
        readTileDefinitions();
        if(fileNames.size()!=main.FifthZone.WALL) throw new IllegalStateException("Unexpected original tile table size");
        for(String name:new String[]{"abyss-wall","glow-coral-top","glow-coral-bottom","glow-coral-left","glow-coral-right"}) {
            fileNames.add("/quinta/tiles/"+name+".png"); collisionStatus.add(name.equals("abyss-wall"));
        }
        String[] templeTiles = {"wall","wall-frieze","pillar","lamp-wall","floor-0","floor-1","floor-2",
            "moss","rail-horizontal","rail-vertical","rest","return-arch","secret-relief-1","secret-relief-2"};
        for (int index=0; index<templeTiles.length; index++) {
            fileNames.add("/septima/tiles/"+templeTiles[index]+".png");
            collisionStatus.add(index<4 || index>=12);
        }
        for (String room : new String[]{"chicken","flower","spiky"}) {
            String[] parts={"floor-0","floor-1","wall","wall-trim","gate-closed","gate-open"};
            for (int i=0;i<parts.length;i++) {
                fileNames.add("/refugios/"+room+"/"+parts[i]+".png");
                collisionStatus.add(i>=2 && i<=4);
            }
        }
        fileNames.add("/refugios/spiky/cover.png");
        collisionStatus.add(true);
        for (String room : new String[]{"pump","hongui","ghost"}) {
            String[] parts={"floor-0","floor-1","wall","wall-trim","gate-closed","gate-open"};
            for (int i=0;i<parts.length;i++) {
                fileNames.add("/refugios/"+room+"/"+parts[i]+".png");
                collisionStatus.add(i>=2 && i<=4);
            }
        }
        fileNames.add("/refugios/pump/binding-intact.png"); collisionStatus.add(true);
        fileNames.add("/refugios/pump/binding-cut.png"); collisionStatus.add(false);
        for (int i=0;i<3;i++) for (int lit=0;lit<2;lit++) {
            fileNames.add("/refugios/ghost/memory-"+i+"-"+lit+".png");
            collisionStatus.add(false);
        }
        for (String room : new String[]{"handy","ic"}) {
            String[] parts={"floor-0","floor-1","wall","wall-trim","gate-closed","gate-open"};
            for (int i=0;i<parts.length;i++) {
                fileNames.add("/refugios/"+room+"/"+parts[i]+".png");
                collisionStatus.add(i>=2 && i<=4);
            }
        }
        fileNames.add("/refugios/handy/stone-latch-intact.png"); collisionStatus.add(true);
        fileNames.add("/refugios/handy/stone-latch-broken.png"); collisionStatus.add(false);
        for (String tile : new String[]{"ice-0","ice-1","rough-stop","mark-0-0","mark-0-1","mark-1-0","mark-1-1","frost-wall"}) {
            fileNames.add("/refugios/ic/"+tile+".png");
            collisionStatus.add(tile.equals("frost-wall"));
        }
        String[] marketTiles = {"floor-0", "floor-1", "wall", "wall-trim", "pillar", "runner", "arch-return", "arch-forward"};
        if (fileNames.size() != main.Market.FIRST_TILE) throw new IllegalStateException("Unexpected market tile offset");
        for (int i=0; i<marketTiles.length; i++) {
            fileNames.add("/mercado/tiles/" + marketTiles[i] + ".png");
            collisionStatus.add(i >= 2 && i <= 4);
        }
        tile = new Tile[fileNames.size()];
        getTileImage();
        List<String> firstMap = readLines(INITIAL_MAPS[0]);
        gp.maxWorldRow = firstMap.size();
        gp.maxWorldCol = firstMap.get(0).trim().split("\\s+").length;
        mapTileNum = new int[gp.maxMap][gp.maxWorldCol][gp.maxWorldRow];
        for (int map = 0; map < INITIAL_MAPS.length; map++) loadMap(INITIAL_MAPS[map], map);
    }

    private void readTileDefinitions() {
        List<String> lines = readLines("/maps/NewSuperData.txt");
        if (lines.size() % 2 != 0) throw new IllegalStateException("Incomplete terrain definition");
        for (int index = 0; index < lines.size(); index += 2) {
            String image = lines.get(index).trim();
            String collision = lines.get(index + 1).trim();
            if (image.isEmpty() || !(collision.equals("true") || collision.equals("false"))) {
                throw new IllegalStateException("Invalid terrain definition at line " + (index + 1));
            }
            fileNames.add(image);
            collisionStatus.add(Boolean.parseBoolean(collision));
        }
    }

    private static List<String> readLines(String resource) {
        try (InputStream stream = TileManager.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IOException("Resource is missing");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                List<String> lines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) lines.add(line);
                if (lines.isEmpty()) throw new IOException("Resource is empty");
                return lines;
            }
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read " + resource, error);
        }
    }

    public void getTileImage() {
        for (int index = 0; index < fileNames.size(); index++) {
            setup(index, fileNames.get(index), collisionStatus.get(index));
        }
    }

    public void setup(int index, String imageName, boolean collision) {
        Tile definition = new Tile();
        definition.image = SpriteCache.load(imageName.startsWith("/")?imageName:"/tiles/" + imageName, gp.tileSize, gp.tileSize);
        definition.collision = collision;
        tile[index] = definition;
    }

    /** Parse and validate first; a failed reload leaves the current terrain intact. */
    public void loadMap(String resource, int map) {
        if (map < 0 || map >= mapResources.length) throw new IllegalArgumentException("Invalid map: " + map);
        List<String> lines = readLines(resource);
        if (lines.size() != gp.maxWorldRow) {
            throw new IllegalArgumentException(resource + ": expected " + gp.maxWorldRow + " rows");
        }
        int[][] terrain = new int[gp.maxWorldCol][gp.maxWorldRow];
        for (int row = 0; row < lines.size(); row++) {
            String[] numbers = lines.get(row).trim().split("\\s+");
            if (numbers.length != gp.maxWorldCol) {
                throw new IllegalArgumentException(resource + ": invalid width in row " + row);
            }
            for (int col = 0; col < numbers.length; col++) {
                int number = Integer.parseInt(numbers[col]);
                if (number < 0 || number >= tile.length || tile[number] == null) {
                    throw new IllegalArgumentException(resource + ": invalid tile " + number);
                }
                terrain[col][row] = number;
            }
        }
        mapTileNum[map] = terrain;
        mapResources[map] = resource;
        mapRevisions[map]++;
    }

    public void setTerrainCell(int map, int x, int y, int tileId) {
        if (mapTileNum[map][x][y] == tileId) return;
        mapTileNum[map][x][y] = tileId;
        mapRevisions[map]++;
    }

    public void reloadCurrentMap() {
        if (isMapLoaded(gp.currentMap)) loadMap(mapResources[gp.currentMap], gp.currentMap);
        if (gp.currentMap==main.Temple.MAP) gp.temple.syncReliefs();
        if (gp.currentMap==main.ExtraRooms.MAP) {
            gp.extraRooms.syncTerrain();
            gp.extraRooms.later.resetTransient();
            gp.extraRooms.ending.resetTransient();
        }
        if (main.Temple.usesTemple(gp.currentMap)) gp.temple.resetCombat();
        for (var enemy : gp.monster[gp.currentMap]) {
            if (enemy instanceof monster.MON_Sharky shark) shark.resetAmbush();
            if (enemy instanceof monster.MON_Tella tella && tella.usesPulse()) tella.resetPulse();
        }
    }

    public boolean isMapLoaded(int map) {
        return map >= 0 && map < mapResources.length && mapResources[map] != null;
    }

    public int getMapRevision(int map) {
        return isMapLoaded(map) ? mapRevisions[map] : -1;
    }

    public int loadedMapCount() {
        int count = 0;
        for (String resource : mapResources) if (resource != null) count++;
        return count;
    }

    public void draw(Graphics2D graphics) {
        drawTerrain(graphics, false);
    }

    public void drawLayered(Graphics2D graphics) {
        Graphics2D layer = (Graphics2D) graphics.create();
        drawTerrain(layer, gp.currentMap == main.Market.MAP || gp.currentMap == 0 || gp.currentMap == 1 || gp.currentMap == 2 || main.Temple.usesTemple(gp.currentMap) || main.SixthZone.usesAbyss(gp.currentMap) || gp.currentMap == main.FourthZone.MAP);
        layer.dispose();
    }

    private void drawTerrain(Graphics2D graphics, boolean ocean) {
        var room = gp.extraRooms.active();
        if (gp.currentMap==main.ExtraRooms.MAP && room==null) return;
        var view = room==null ? null : room.view();
        int cameraX=gp.cameraX(),cameraY=gp.cameraY();
        int left = view==null ? Math.max(0, cameraX / gp.tileSize - 1) : view.x;
        int right = view==null ? Math.min(gp.maxWorldCol - 1, (cameraX + 2*gp.player.screenX) / gp.tileSize + 1) : view.x+view.width-1;
        int top = view==null ? Math.max(0, cameraY / gp.tileSize - 1) : view.y;
        int bottom = view==null ? Math.min(gp.maxWorldRow - 1, (cameraY + 2*gp.player.screenY) / gp.tileSize + 1) : view.y+view.height-1;
        for (int row = top; row <= bottom; row++) {
            for (int col = left; col <= right; col++) {
                int tileNumber = mapTileNum[gp.currentMap][col][row];
                if ((ocean || room!=null) && tileNumber == 0) continue;
                int screenX = col * gp.tileSize - cameraX;
                int screenY = row * gp.tileSize - cameraY;
                if (gp.currentMap==main.ExtraRooms.MAP && (tileNumber==144 || tileNumber==145))
                    graphics.drawImage(tile[126+(col+row/2)%2].image,screenX,screenY,null);
                if (gp.currentMap==main.ExtraRooms.MAP &&
                    (tileNumber==main.FinalRefuges.LATCH_INTACT || tileNumber==main.FinalRefuges.LATCH_BROKEN))
                    graphics.drawImage(tile[152+(col+row/2)%2].image,screenX,screenY,null);
                if (ocean && (tileNumber == 1 || tileNumber == 2)) {
                    graphics.setComposite(SHALLOW_WATER);
                    graphics.drawImage(tile[tileNumber].image, screenX, screenY, null);
                    graphics.setComposite(AlphaComposite.SrcOver);
                } else graphics.drawImage(tile[tileNumber].image, screenX, screenY, null);
            }
        }
        if (drawPath && room==null) {
            graphics.setColor(new Color(255, 0, 0, 70));
            for (ai.Node cell : gp.pFinder.pathList) {
                int screenX = cell.col * gp.tileSize - cameraX;
                int screenY = cell.row * gp.tileSize - cameraY;
                graphics.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
            }
        }
    }
}
