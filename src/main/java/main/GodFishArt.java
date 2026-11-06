package main;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;

/** Atlas geometry mirrors the reviewed manifest. All cells are cut and scaled once. */
public final class GodFishArt {
    private static final Map<String, Atlas> ATLASES = Map.ofEntries(
        Map.entry("boss/fase-1", new Atlas("boss/fase-1", 144, 128, 72, 63, 4, 1)),
        Map.entry("boss/fase-2", new Atlas("boss/fase-2", 144, 128, 72, 63, 4, 1)),
        Map.entry("boss/fase-3", new Atlas("boss/fase-3", 144, 128, 72, 63, 4, 1)),
        Map.entry("boss/fase-4", new Atlas("boss/fase-4", 144, 128, 72, 63, 4, 1)),
        Map.entry("boss/fase-5", new Atlas("boss/fase-5", 144, 128, 72, 63, 4, 1)),
        Map.entry("boss/fase-6", new Atlas("boss/fase-6", 144, 128, 72, 63, 4, 1)),
        Map.entry("boss/1-sweep", new Atlas("boss/1-sweep", 144, 128, 72, 63, 4, 4)),
        Map.entry("boss/2-volley", new Atlas("boss/2-volley", 144, 128, 72, 63, 4, 4)),
        Map.entry("boss/3-charge", new Atlas("boss/3-charge", 144, 128, 72, 63, 4, 4)),
        Map.entry("boss/4-cast", new Atlas("boss/4-cast", 144, 128, 72, 63, 4, 1)),
        Map.entry("boss/5-fist-l", new Atlas("boss/5-fist-l", 144, 128, 72, 63, 4, 4)),
        Map.entry("boss/5-fist-r", new Atlas("boss/5-fist-r", 144, 128, 72, 63, 4, 4)),
        Map.entry("boss/6-fist-l", new Atlas("boss/6-fist-l", 144, 128, 72, 63, 4, 4)),
        Map.entry("boss/6-fist-r", new Atlas("boss/6-fist-r", 144, 128, 72, 63, 4, 4)),
        Map.entry("boss/6-slam", new Atlas("boss/6-slam", 144, 128, 72, 63, 4, 1)),
        Map.entry("boss/3-tridente-clavado", new Atlas("boss/3-tridente-clavado", 144, 128, 72, 63, 2, 4)),
        Map.entry("boss/4-desarme", new Atlas("boss/4-desarme", 144, 128, 72, 63, 2, 1)),
        Map.entry("boss/6-final", new Atlas("boss/6-final", 144, 128, 72, 63, 4, 1)),
        Map.entry("fx/aura", new Atlas("fx/aura", 160, 144, 80, 72, 5, 6)),
        Map.entry("fx/marcas", new Atlas("fx/marcas", 32, 32, 16, 16, 3, 12)),
        Map.entry("fx/proyectil", new Atlas("fx/proyectil", 32, 32, 16, 16, 5, 3)),
        Map.entry("escena/mecanismos", new Atlas("escena/mecanismos", 32, 32, 16, 24, 3, 8)),
        Map.entry("escena/armas", new Atlas("escena/armas", 80, 80, 40, 40, 3, 4)),
        Map.entry("boss/4-sweep", new Atlas("boss/4-sweep", 144, 128, 72, 63, 4, 4)),
        Map.entry("boss/nado-1", new Atlas("boss/nado-1", 144, 128, 72, 63, 4, 4)),
        Map.entry("boss/nado-2", new Atlas("boss/nado-2", 144, 128, 72, 63, 4, 4)),
        Map.entry("boss/nado-3", new Atlas("boss/nado-3", 144, 128, 72, 63, 4, 4)),
        Map.entry("boss/nado-4", new Atlas("boss/nado-4", 144, 128, 72, 63, 4, 4)),
        Map.entry("boss/nado-5", new Atlas("boss/nado-5", 144, 128, 72, 63, 4, 4)),
        Map.entry("boss/nado-6", new Atlas("boss/nado-6", 144, 128, 72, 63, 4, 4)),
        Map.entry("esbirros/aguijon", new Atlas("esbirros/aguijon",32,32,16,16,8,4)),
        Map.entry("esbirros/baluarte", new Atlas("esbirros/baluarte",32,32,16,16,8,4)),
        Map.entry("esbirros/efectos", new Atlas("esbirros/efectos",16,16,8,8,5,2))
    );
    private static final BufferedImage[] VOLLEY = nativeVolley();
    private static final BufferedImage EYE = ATLASES.get("boss/fase-1").cells[0][0].getSubimage(63*3, 52*3, 19*3, 17*3);

    private GodFishArt() {}

    public static BufferedImage load(String path) {
        try (var stream = GodFishArt.class.getResourceAsStream("/final/" + path + ".png")) {
            if (stream == null) throw new IllegalStateException("Missing final encounter image: " + path);
            BufferedImage image = ImageIO.read(stream);
            if (image == null) throw new IllegalStateException("Invalid final encounter image: " + path);
            return image;
        } catch (IOException error) {
            throw new IllegalStateException("Cannot load final encounter image: " + path, error);
        }
    }

    static BufferedImage scaled(BufferedImage source) {
        BufferedImage result = new BufferedImage(source.getWidth()*3, source.getHeight()*3, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(source, 0, 0, result.getWidth(), result.getHeight(), null);
        g.dispose();
        return result;
    }

    public static void draw(Graphics2D g, String atlas, int col, int row, int x, int y) {
        Atlas a = ATLASES.get(atlas);
        g.drawImage(a.cells[row][col], x-a.x, y-a.y, null);
    }

    private static BufferedImage[] nativeVolley() {
        BufferedImage source=load("god-fish/fx/proyectil");
        return new BufferedImage[]{source.getSubimage(32,0,32,32),source.getSubimage(64,0,32,32)};
    }

    public static void volley(Graphics2D g,int frame,int x,int y) {
        g.drawImage(VOLLEY[frame],x-16,y-16,null);
    }

    static void eye(Graphics2D g, int x, int y) {
        g.drawImage(EYE, x-27, y-33, null);
    }

    private static final class Atlas {
        final int x, y;
        final BufferedImage[][] cells;

        Atlas(String name, int width, int height, int anchorX, int anchorY, int cols, int rows) {
            BufferedImage source = load("god-fish/" + name);
            if (source.getWidth()!=width*cols || source.getHeight()!=height*rows)
                throw new IllegalStateException("Unexpected God Fish atlas size: " + name);
            x = anchorX*3;
            y = anchorY*3;
            int stride=source.getWidth();
            int[] pixels=source.getRGB(0,0,stride,source.getHeight(),null,0,stride);
            var palette=new LinkedHashMap<Integer,Integer>();
            for (int pixel:pixels) if (!palette.containsKey(pixel)) palette.put(pixel,palette.size());
            if (palette.size()>256) throw new IllegalStateException("Unexpected atlas palette: "+name);
            int[] colors=palette.keySet().stream().mapToInt(Integer::intValue).toArray();
            var model=new IndexColorModel(8,colors.length,colors,0,true,-1,DataBuffer.TYPE_BYTE);
            for (int i=0;i<pixels.length;i++) pixels[i]=palette.get(pixels[i]);
            cells = new BufferedImage[rows][cols];
            int span=width*3;
            for (int row=0; row<rows; row++) for (int col=0; col<cols; col++) {
                BufferedImage cell=new BufferedImage(span,height*3,BufferedImage.TYPE_BYTE_INDEXED,model);
                byte[] dest=((DataBufferByte)cell.getRaster().getDataBuffer()).getData();
                for (int sy=0;sy<height;sy++) {
                    int from=(row*height+sy)*stride+col*width;
                    int to=sy*3*span;
                    for (int sx=0;sx<width;sx++)
                        Arrays.fill(dest,to+sx*3,to+sx*3+3,(byte)pixels[from+sx]);
                    System.arraycopy(dest,to,dest,to+span,span);
                    System.arraycopy(dest,to,dest,to+span*2,span);
                }
                cells[row][col]=cell;
            }
        }
    }
}
