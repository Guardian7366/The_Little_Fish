package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** One 60 Hz clock selects the scene, the verse and its drawing. */
final class CreditRoll {
    static final Rectangle PLAY_AGAIN=new Rectangle(397,465,166,39);
    static final int REVEAL_TICKS=102;
    private static final int LYRIC_X=625, LYRIC_WIDTH=310, LYRIC_TOP=107, LYRIC_BOTTOM=378;
    private record Lyrics(Font font,List<String> lines,int step) {}
    record Card(String id,int start,int end,String image,List<String> lines) {}
    record Timeline(int duration,int titleEnd,String title,int dip,int finalFade,List<Card> cards) {}
    static final Timeline TIMELINE=load();
    private final CreditScenes scenes;
    private final TitleBackground sunset=TitleBackground.farewell();
    private final BufferedImage portrait=CreditAssets.image("foto/foto-despedida.png");
    private final List<BufferedImage> drawings=new ArrayList<>();
    private final List<Lyrics> lyrics=new ArrayList<>();
    private final Font font;
    private int ticks;
    private int acknowledgements=-1;

    CreditRoll(GamePanel gp) {
        this.font=gp.ui.purisaB;
        this.scenes=new CreditScenes(gp.tileM);
        var buffer=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
        var g=buffer.createGraphics();
        for (Card card:TIMELINE.cards()) {
            drawings.add(CreditAssets.image(card.image()));
            lyrics.add(layout(g,font,card.lines()));
        }
        g.dispose();
    }

    private static String line(BufferedReader source) throws IOException {
        String value=source.readLine();
        if (value==null) throw new IOException("Truncated credits timeline");
        return value;
    }

    private static int number(BufferedReader source) throws IOException {
        return Integer.parseInt(line(source));
    }

    private static Timeline load() {
        try (var source=new BufferedReader(new InputStreamReader(CreditAssets.open("/credits/timeline.txt"),StandardCharsets.UTF_8))) {
            int duration=number(source), titleStart=number(source), titleEnd=number(source);
            String title=line(source);
            int dip=number(source), finalFade=number(source), count=number(source);
            if (titleStart!=0 || titleEnd<=0 || dip<=0 || finalFade>=duration || count!=14)
                throw new IOException("Invalid credits header");
            var cards=new ArrayList<Card>(count);
            int previous=titleEnd;
            for (int i=0;i<count;i++) {
                String id=line(source);
                CreditScenes.checkId(i,id);
                int start=number(source),end=number(source);
                String image=line(source);
                int lines=number(source);
                if (start!=previous || end<=start || lines<=0) throw new IOException("Invalid card "+id);
                var text=new ArrayList<String>();
                for (int j=0;j<lines;j++) text.add(line(source));
                cards.add(new Card(id,start,end,image,List.copyOf(text)));
                previous=end;
            }
            if (previous!=finalFade) throw new IOException("Cards must end at the final fade");
            return new Timeline(duration,titleEnd,title,dip,finalFade,List.copyOf(cards));
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read the credits timeline",error);
        }
    }

    private static Lyrics layout(Graphics2D g,Font font,List<String> lines) {
        for (int size=21;size>=13;size--) {
            Font choice=font.deriveFont(size+0f);
            var metrics=g.getFontMetrics(choice);
            var wrapped=new ArrayList<String>();
            for (String line:lines) {
                String current="";
                for (String word:line.split(" ")) {
                    String next=current.isEmpty()?word:current+" "+word;
                    if (!current.isEmpty() && metrics.stringWidth(next)>LYRIC_WIDTH) {
                        wrapped.add(current);
                        current=word;
                    } else current=next;
                }
                wrapped.add(current);
            }
            int step=metrics.getHeight()+2;
            if ((wrapped.size()-1)*step+metrics.getHeight()<=LYRIC_BOTTOM-LYRIC_TOP
                    && wrapped.stream().allMatch(line->metrics.stringWidth(line)<=LYRIC_WIDTH))
                return new Lyrics(choice,List.copyOf(wrapped),step);
        }
        throw new IllegalStateException("Lyrics overflow: "+lines);
    }

    void begin() {
        ticks=0;
        acknowledgements=-1;
        sunset.reset();
    }

    void update() {
        ticks++;
        if (farewell()) sunset.update();
        if (acknowledgements>=0) acknowledgements++;
    }

    int elapsed() { return (int)(ticks*1000L/60); }

    boolean farewell() { return elapsed()>=TIMELINE.duration(); }

    int farewellTicks() { return ticks-(TIMELINE.duration()*60+999)/1000; }

    boolean canPlayAgain() { return farewellTicks()>=REVEAL_TICKS && acknowledgements<0; }

    boolean showAcknowledgements() { return acknowledgements>=0; }

    void toggleAcknowledgements() {
        if (farewellTicks()>=REVEAL_TICKS) acknowledgements=acknowledgements<0?0:-1;
    }

    static int cardAt(int ms) {
        if (ms<TIMELINE.titleEnd() || ms>=TIMELINE.duration()) return -1;
        List<Card> cards=TIMELINE.cards();
        for (int i=0;i<cards.size()-1;i++) if (ms<cards.get(i).end()) return i;
        return cards.size()-1;
    }

    void draw(Graphics2D target,String attribution) {
        var g=(Graphics2D)target.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setColor(Color.BLACK);
        g.fillRect(0,0,960,576);
        if (farewell()) drawFarewell(g,attribution);
        else drawCards(g);
        g.dispose();
    }

    private void drawCards(Graphics2D g) {
        int ms=elapsed(), card=cardAt(ms);
        var white=new Color(247,241,230);
        g.setColor(white);
        if (card<0) {
            centered(g,TIMELINE.title(),font.deriveFont(70f),480,296);
            centered(g,"David Silva Padilla",font.deriveFont(19f),480,350);
            fade(g,(ms-(TIMELINE.titleEnd()-TIMELINE.dip()))/(float)TIMELINE.dip());
            return;
        }

        g.fillRect(26,102,CreditScenes.WIDTH+8,CreditScenes.HEIGHT+8);
        var frame=(Graphics2D)g.create(30,106,CreditScenes.WIDTH,CreditScenes.HEIGHT);
        scenes.draw(frame,card,Math.min(ms,TIMELINE.cards().get(card).end()-1)-TIMELINE.cards().get(card).start());
        frame.dispose();

        Lyrics verse=lyrics.get(card);
        g.setFont(verse.font());
        int y=LYRIC_TOP+g.getFontMetrics().getAscent();
        for (String line:verse.lines()) {
            g.drawString(line,LYRIC_X,y);
            y+=verse.step();
        }
        var doodle=drawings.get(card);
        int factor=Math.min(152/doodle.getWidth(),118/doodle.getHeight());
        factor=Math.max(1,factor);
        int w=doodle.getWidth()*factor,h=doodle.getHeight()*factor;
        g.drawImage(doodle,778-w/2,442-h/2,w,h,null);

        if (ms<TIMELINE.titleEnd()+TIMELINE.dip())
            fade(g,1-(ms-TIMELINE.titleEnd())/(float)TIMELINE.dip());
        if (ms>=TIMELINE.finalFade())
            fade(g,(ms-TIMELINE.finalFade())/(float)(TIMELINE.duration()-TIMELINE.finalFade()));
    }

    private void drawFarewell(Graphics2D g,String attribution) {
        sunset.draw(g);
        g.setColor(new Color(252,234,204));
        g.fillRect(331,34,298,231);
        g.drawImage(portrait,336,39,288,221,null);
        g.setColor(new Color(12,26,35,158));
        g.fillRect(0,359,960,217);
        g.setColor(new Color(255,241,211));
        centered(g,"Thanks for playing",font.deriveFont(42f),480,410);
        g.setColor(new Color(24,43,47));
        g.fillRoundRect(PLAY_AGAIN.x,PLAY_AGAIN.y,PLAY_AGAIN.width,PLAY_AGAIN.height,12,12);
        g.setColor(new Color(255,231,176));
        g.setStroke(new java.awt.BasicStroke(2));
        g.drawRoundRect(PLAY_AGAIN.x,PLAY_AGAIN.y,PLAY_AGAIN.width,PLAY_AGAIN.height,12,12);
        centered(g,"Play Again",font.deriveFont(22f),480,492);
        centered(g,"C · Credits and thanks",font.deriveFont(16f),480,548);
        if (acknowledgements>=0) {
            g.setColor(new Color(8,16,26,235));
            g.fillRect(0,0,960,576);
            g.setColor(Color.WHITE);
            centered(g,"C / Esc · Back",font.deriveFont(16f),480,552);
            var scroll=(Graphics2D)g.create();
            scroll.clipRect(0,34,960,479);
            int y=78-acknowledgements%((attribution.split("\n").length+4)*32);
            for (String line:attribution.split("\n")) {
                centered(scroll,line,font.deriveFont(21f),480,y);
                y+=32;
            }
            scroll.dispose();
        }
        fade(g,1-farewellTicks()/(float)REVEAL_TICKS);
    }

    private static void centered(Graphics2D g,String text,Font font,int x,int y) {
        g.setFont(font);
        g.drawString(text,x-g.getFontMetrics().stringWidth(text)/2,y);
    }

    private static void fade(Graphics2D g,float alpha) {
        if (alpha<=0) return;
        g.setComposite(AlphaComposite.SrcOver.derive(Math.min(1,alpha)));
        g.setColor(Color.BLACK);
        g.fillRect(0,0,960,576);
        g.setComposite(AlphaComposite.SrcOver);
    }
}
