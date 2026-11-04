package environment;

import java.awt.*;
import java.awt.image.BufferedImage;
import main.GamePanel;
import main.SpriteCache;

/** Prepared native art, loaded once; camera-independent creatures are decoration only. */
public final class FifthAbyss {
    private final BufferedImage far=load("far",320,192), ridges=load("ridges",320,192);
    private final BufferedImage[] particles=new BufferedImage[5];
    private final BufferedImage angler=load("angler",72,40), longfin=load("longfin",72,36);
    private int tick;
    public FifthAbyss() {for(int i=0;i<5;i++) particles[i]=load("particles-"+i,320,192);}
    private static BufferedImage load(String s,int w,int h) {return SpriteCache.load("/quinta/background/"+s+".png",w,h);}
    // Common multiple of the two offscreen travel cycles (4800/4000) and particles (150).
    public void update() {tick=(tick+1)%24000;}
    public int tick() {return tick;}
    public long cachedPixelBytes() {return (7L*320*192+72*40+72*36)*4;}
    private void repeat(Graphics2D g,BufferedImage image,int x,int y,GamePanel gp) {
        int sx=-Math.floorMod(x/3*3,960),sy=-Math.floorMod(y/3*3,576);
        for(int yy=sy;yy<gp.screenHeight;yy+=576) for(int xx=sx;xx<gp.screenWidht;xx+=960)
            g.drawImage(image,xx,yy,960,576,null);
    }
    public void draw(Graphics2D target,GamePanel gp) {
        Graphics2D g=(Graphics2D)target.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int cx=gp.player.worldX-gp.player.screenX,cy=gp.player.worldY-gp.player.screenY;
        repeat(g,far,cx/8,cy/8,gp);repeat(g,ridges,cx/4,cy/4,gp);
        g.setComposite(AlphaComposite.SrcOver.derive(0.32f));
        // Full offscreen wrap: 15 and 18 screen pixels/second at 60 updates/second.
        int x=Math.floorMod(tick/4,1200)-216;
        int x2=960-Math.floorMod(tick*3/10+430,1200);
        g.drawImage(angler,x,105,216,120,null);g.drawImage(longfin,x2,365,216,108,null);
        g.setComposite(AlphaComposite.SrcOver);repeat(g,particles[tick/30%5],cx/6,cy/6,gp);
        g.dispose();
    }
}
