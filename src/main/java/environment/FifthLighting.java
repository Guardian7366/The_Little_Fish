package environment;

import java.awt.*;
import java.awt.image.*;
import java.util.Arrays;
import main.FifthZone;
import main.GamePanel;

/** Native-pixel, occluded fixed light field. Combine darkness by min, never additive alpha. */
public final class FifthLighting {
    private static final int AMBIENT_ALPHA=165;
    private final GamePanel gp;
    private final int width,height;
    private final byte[] worldAlpha;
    private final BufferedImage screen=new BufferedImage(320,192,BufferedImage.TYPE_INT_ARGB);
    private final int[] pixels=((DataBufferInt)screen.getRaster().getDataBuffer()).getData();
    private final byte[] lamp=new byte[320*192];
    private int revision=-1, lampRadius=-1, cachedMap=-1;
    public FifthLighting(GamePanel gp) {
        this.gp=gp;width=gp.maxWorldCol*16;height=gp.maxWorldRow*16;
        worldAlpha=new byte[width*height];prepare();
    }
    public long cachedPixelBytes() {return worldAlpha.length+4L*pixels.length+lamp.length;}
    private boolean clear(int x,int y,int tx,int ty) {
        int dx=tx-x,dy=ty-y,n=Math.max(Math.abs(dx),Math.abs(dy));
        // Stop on a solid tile before the target: the first wall itself can receive light.
        for(int i=1;i<n;i++) {
            int col=(x+dx*i/n)/16,row=(y+dy*i/n)/16;
            if(col<0||row<0||col>=gp.maxWorldCol||row>=gp.maxWorldRow||
                gp.tileM.tile[gp.tileM.mapTileNum[cachedMap][col][row]].collision) return false;
        }
        return true;
    }
    public void prepare() {
        int map = main.SixthZone.usesAbyss(gp.currentMap) ? gp.currentMap : FifthZone.MAP;
        int next=gp.tileM.getMapRevision(map);
        if(next!=revision || map!=cachedMap) {
            cachedMap=map;
            Arrays.fill(worldAlpha,(byte)AMBIENT_ALPHA);
            final int radius=44; // 2.75 tiles, native pixels.
            for(int col=0;col<gp.maxWorldCol;col++) for(int row=0;row<gp.maxWorldRow;row++) {
                int tile=gp.tileM.mapTileNum[map][col][row];
                if(tile<FifthZone.GLOW_TOP||tile>FifthZone.GLOW_TOP+3) continue;
                int cx=col*16+8,cy=row*16+8;
                for(int y=Math.max(0,cy-radius);y<Math.min(height,cy+radius+1);y++)
                    for(int x=Math.max(0,cx-radius);x<Math.min(width,cx+radius+1);x++) {
                        double distance=Math.hypot(x-cx,y-cy)/radius;
                        if(distance>=1||!clear(cx,cy,x,y)) continue;
                        int alpha=(int)Math.round(30+(AMBIENT_ALPHA-30)*distance);
                        int i=y*width+x;worldAlpha[i]=(byte)Math.min(worldAlpha[i]&255,alpha);
                    }
            }
            revision=next;
        }
        int radius=gp.player.currentLight==null?0:gp.player.currentLight.lightRadius;
        if(radius!=lampRadius) {
            double cx=(gp.player.screenX+24)/3.0,cy=(gp.player.screenY+24)/3.0;
            for(int y=0;y<192;y++) for(int x=0;x<320;x++)
                lamp[y*320+x]=(byte)(radius==0?255:Math.min(255,(int)Math.round(Math.hypot(x-cx,y-cy)*3/radius*255)));
            lampRadius=radius;
        }
    }
    public int fixedAlpha(int worldX,int worldY) {return worldAlpha[(worldY/3)*width+worldX/3]&255;}
    public void draw(Graphics2D target) {
        int cameraX=gp.player.worldX-gp.player.screenX,cameraY=gp.player.worldY-gp.player.screenY;
        for(int y=0;y<192;y++) for(int x=0;x<320;x++) {
            int wx=Math.floorDiv(cameraX+x*3,3),wy=Math.floorDiv(cameraY+y*3,3),i=y*320+x;
            int fixed=wx<0||wy<0||wx>=width||wy>=height?AMBIENT_ALPHA:worldAlpha[wy*width+wx]&255;
            int alpha=Math.min(fixed,lamp[i]&255);
            pixels[i]=(alpha<<24)|0x070419;
        }
        Graphics2D g=(Graphics2D)target.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(screen,0,0,960,576,null);g.dispose();
    }
}
