package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Silent layout study for the ending. The clock is owned by the cutscene controller. */
final class CoastEpilogue {
    static final int QUIET=120, LINE=240, LOWER=60, HOLD=180, FADE=60;
    static final String[] CAPTIONS={
        "I looked at her photograph one last time.",
        "The tide was coming in.",
        "The light on the water was beautiful.\nI stayed to watch it."
    };
    private static final int LOWER_START=QUIET+CAPTIONS.length*LINE;
    static final int DURATION=LOWER_START+LOWER+HOLD+FADE;
    private static final BufferedImage BACKDROP=backdrop();

    static int captionAt(int tick) {
        return tick>=QUIET && tick<LOWER_START ? (tick-QUIET)/LINE : -1;
    }

    static int photoOffset(int tick) {
        return Math.max(0,Math.min(86,(tick-LOWER_START)*86/LOWER));
    }

    void draw(Graphics2D graphics,int tick,Font font) {
        var g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(BACKDROP,0,0,960,576,null);
        var pixels=(Graphics2D)g.create();
        pixels.scale(3,3);
        waves(pixels,tick);
        if (photoOffset(tick)<86) photograph(pixels,photoOffset(tick));
        pixels.dispose();

        fill(g,new Color(18,24,37,214),18,16,348,32);
        g.setColor(new Color(247,218,181));
        g.setFont(font.deriveFont(Font.BOLD,14f));
        g.drawString("COAST PREVIEW  /  AUDIO PENDING",29,38);
        int caption=captionAt(tick);
        if (caption>=0) {
            fill(g,new Color(18,24,37,218),33,437,630,92);
            g.setColor(new Color(255,236,207));
            g.setFont(font.deriveFont(Font.PLAIN,21f));
            int y=470;
            for (String line:CAPTIONS[caption].split("\n")) {
                g.drawString(line,55,y);
                y+=30;
            }
        }
        int dark=0;
        if (tick<45) dark=255*(45-tick)/45;
        else if (tick>=DURATION-FADE) dark=255*(tick-(DURATION-FADE))/FADE;
        if (dark>0) fill(g,new Color(0,0,0,dark),0,0,960,576);
        g.dispose();
    }

    private static BufferedImage backdrop() {
        var image=new BufferedImage(320,192,BufferedImage.TYPE_INT_RGB);
        var g=image.createGraphics();
        Color high=new Color(34,42,71), low=new Color(228,139,106);
        for (int y=0;y<98;y++) {
            fill(g,mix(high,low,y/97f),0,y,320,1);
        }
        fill(g,new Color(211,117,109),0,83,320,15);
        g.setColor(new Color(252,188,120));
        g.fillOval(182,55,45,45);
        g.setColor(new Color(255,214,148));
        g.fillOval(189,59,30,36);
        fill(g,new Color(123,85,104),0,96,320,3);
        for (int y=99;y<183;y++)
            fill(g,mix(new Color(88,92,116),new Color(26,55,72),(y-99)/83f),0,y,320,1);
        g.setColor(new Color(41,39,51));
        g.fillPolygon(new int[]{0,82,150,213,320,320,0},new int[]{161,170,175,180,182,192,192},7);
        g.setColor(new Color(117,108,114));
        g.drawPolyline(new int[]{0,43,82,119,150,213,259,319},new int[]{160,165,169,170,174,179,179,181},8);
        g.dispose();
        return image;
    }

    private static void waves(Graphics2D g,int tick) {
        for (int i=0;i<15;i++) {
            int y=100+i*4;
            int drift=tick/(12+i%4);
            int width=8+i%3*5;
            for (int x=-50+(drift+i*19)%42;x<320;x+=42)
                fill(g,i<6?new Color(155,125,132):new Color(72,104,119),x,y,width,1);
            fill(g,new Color(215-i*4,148-i*3,119),193-i+(drift%11),y,17+i%5*3,1);
        }
    }

    private static void photograph(Graphics2D g,int offset) {
        int x=230,y=111+offset;
        fill(g,new Color(34,31,46),x-2,y-2,54,59);
        fill(g,new Color(232,212,181),x,y,50,55);
        fill(g,new Color(165,156,148),x+4,y+4,42,41);
        fill(g,new Color(200,192,174),x+5,y+5,40,16);
        fill(g,new Color(117,112,112),x+5,y+34,40,10);
        g.setColor(new Color(78,74,78));
        g.fillOval(x+14,y+25,26,17);
        g.fillOval(x+12,y+14,20,16);
        g.fillPolygon(new int[]{x+12,x+20,x+14},new int[]{y+19,y+16,y+9},3);
        g.fillPolygon(new int[]{x+23,x+31,x+30},new int[]{y+16,y+19,y+9},3);
        g.fillRect(x+36,y+24,4,12);
        fill(g,new Color(222,212,176),x+17,y+21,2,1);
        fill(g,new Color(222,212,176),x+25,y+21,2,1);
        g.setColor(new Color(51,41,50));
        g.fillPolygon(new int[]{210,222,224,229,238,241,231,227,223},
            new int[]{192,y+44,y+38,y+37,y+42,y+49,y+57,192,192},9);
        g.fillPolygon(new int[]{309,292,285,282,273,268,275,284,290},
            new int[]{192,y+49,y+40,y+36,y+39,y+46,y+54,y+57,192},9);
    }

    private static Color mix(Color a,Color b,float amount) {
        return new Color(a.getRed()+Math.round((b.getRed()-a.getRed())*amount),
            a.getGreen()+Math.round((b.getGreen()-a.getGreen())*amount),
            a.getBlue()+Math.round((b.getBlue()-a.getBlue())*amount));
    }

    private static void fill(Graphics2D g,Color color,int x,int y,int w,int h) {
        g.setColor(color);
        g.fillRect(x,y,w,h);
    }
}
