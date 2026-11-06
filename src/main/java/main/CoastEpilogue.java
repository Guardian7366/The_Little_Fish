package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/** The beach uses its own horizontal position and never updates the arena player. */
final class CoastEpilogue {
    static final int QUIET=120, LINE=240, LOWER=60, HOLD=180, FADE=60;
    static final String[] CAPTIONS={
        "I looked at her photograph one last time.",
        "The tide was coming in.",
        "The light on the water was beautiful.\nI stayed to watch it."
    };
    private static final int LOWER_START=QUIET+CAPTIONS.length*LINE;
    static final int DURATION=LOWER_START+LOWER+HOLD+FADE;
    private static final int VIEW=320, WORLD=3*VIEW, FOOT=185, PHOTO_X=WORLD-46;
    private static final BufferedImage BACKDROP=load("atardecer"), SAND=load("arena-detalle"),
        PICKUP=load("foto-item"), PHOTO=load("foto");
    private static final BufferedImage[] SEA=frames(load("mar"),320,82,4,16),
        PORTAL=frames(load("portal"),32,48,6,6);

    private int fishX, seaTick, swimDistance;
    private boolean facingRight, moving;

    void reset() {
        fishX=34*3;
        seaTick=swimDistance=0;
        facingRight=true;
        moving=false;
    }

    void advanceSea() { seaTick++; }

    boolean move(boolean left,boolean right) {
        int step=(right?5:0)-(left?5:0);
        int next=Math.max(12*3,Math.min((WORLD-12)*3,fishX+step));
        moving=next!=fishX;
        if (moving) {
            swimDistance+=Math.abs(next-fishX);
            facingRight=next>fishX;
            fishX=next;
        }
        return Math.abs(fishX-PHOTO_X*3)<=13*3;
    }

    void stopFish() { moving=false; }

    static int captionAt(int tick) {
        return tick>=QUIET && tick<LOWER_START ? (tick-QUIET)/LINE : -1;
    }

    static int photoOffset(int tick) {
        return Math.max(0,Math.min(86,(tick-LOWER_START)*86/LOWER));
    }

    private static BufferedImage load(String name) {
        try (var stream=CoastEpilogue.class.getResourceAsStream("/ending/"+name+".png")) {
            if (stream==null) throw new IOException("Missing image");
            return ImageIO.read(stream);
        } catch (IOException error) {
            throw new IllegalStateException("Unable to load coast image: "+name,error);
        }
    }

    private static BufferedImage[] frames(BufferedImage atlas,int width,int height,int columns,int count) {
        var result=new BufferedImage[count];
        for (int i=0;i<count;i++)
            result[i]=atlas.getSubimage(i%columns*width,i/columns*height,width,height);
        return result;
    }

    static int frame(int tick,int milliseconds,int count) {
        return (int)((tick*1000L/(60L*milliseconds))%count);
    }

    static void drawPortal(Graphics2D graphics,int x,int y,int tick) {
        var g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(PORTAL[frame(tick,110,PORTAL.length)],x-24,y-66,48,72,null);
        g.dispose();
    }

    private Graphics2D pixels(Graphics2D graphics) {
        var g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.scale(3,3);
        return g;
    }

    private void drawScenery(Graphics2D g,int camera) {
        g.drawImage(BACKDROP,0,0,null);
        g.drawImage(SEA[frame(seaTick,90,SEA.length)],0,100,null);
        int offset=camera%VIEW;
        g.drawImage(SAND,-offset,176,null);
        g.drawImage(SAND,VIEW-offset,176,null);
    }

    void drawWalk(Graphics2D graphics,entity.Player player,boolean showPickup) {
        int x=fishX/3;
        // The camera follows the same rail in both directions, without turning jumps.
        int camera=Math.max(0,Math.min(WORLD-VIEW,x-116));
        var g=pixels(graphics);
        drawScenery(g,camera);
        if (showPickup) g.drawImage(PICKUP,PHOTO_X-camera-12,FOOT-22,null);
        boolean second=moving && (swimDistance/22)%2!=0;
        var fish=facingRight?(second?player.right2:player.right):(second?player.left2:player.left);
        g.drawImage(fish,x-camera-8,FOOT-16,16,16,null);
        g.dispose();
    }

    void drawPortrait(Graphics2D graphics,int tick,Font font) {
        var pixels=pixels(graphics);
        drawScenery(pixels,0);
        int offset=photoOffset(tick);
        if (offset<86) pixels.drawImage(PHOTO,228,102+offset,null);
        pixels.dispose();

        int caption=captionAt(tick);
        if (caption<0) return;
        var g=(Graphics2D)graphics.create();
        g.setColor(new Color(18,24,37,218));
        g.fillRect(33,425,630,92);
        g.setColor(new Color(255,236,207));
        g.setFont(font.deriveFont(Font.PLAIN,21f));
        int y=458;
        for (String line:CAPTIONS[caption].split("\\n")) {
            g.drawString(line,55,y);
            y+=30;
        }
        g.dispose();
    }
}
