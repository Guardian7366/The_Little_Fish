package main;

import data.Progress;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javax.imageio.ImageIO;

public final class CreditsRegression {
    private static final Path OUT=Path.of("build/credits-preview");
    private static final int[] BEATS={1800,2350,2950,3000,3300,3670,4200,3190,3580,5310,2700,3720,4400,2910};
    private static final FifthZoneRegression.Quiet gp=new FifthZoneRegression.Quiet();

    public static void main(String[] args) throws Exception {
        gp.setupGame();
        if (args.length>0 && args[0].equals("--video")) {
            video(new CreditRoll(gp));
        } else {
            captures(new CreditRoll(gp));
            controls();
            System.out.println("CREDITS PASS: 14 scene/lyric/drawing cuts, 120 seconds, GIF, deterministic frames, widescreen and fresh Enter/click");
        }
        gp.stopGame();
    }

    private static void check(boolean ok,String message) {
        if (!ok) throw new AssertionError(message);
    }

    private static BufferedImage render(CreditRoll roll,BufferedImage frame) {
        Graphics2D g=frame.createGraphics();
        roll.draw(g,gp.csManager.endCredit);
        g.dispose();
        return frame;
    }

    private static void captures(CreditRoll roll) throws Exception {
        Files.createDirectories(OUT);
        BufferedImage frame=new BufferedImage(960,576,BufferedImage.TYPE_INT_RGB);
        int shown=0,lines=0;
        roll.begin();
        ImageIO.write(render(roll,frame),"png",OUT.resolve("00-titulo.png").toFile());
        for (CreditRoll.Card card:CreditRoll.TIMELINE.cards()) {
            int index=shown++;
            lines+=card.lines().size();
            while (roll.elapsed()<card.start()) roll.update();
            check(roll.elapsed()==card.start() && CreditRoll.cardAt(roll.elapsed())==index,"first tick "+card.id());
            while (roll.elapsed()<card.start()+BEATS[index]) roll.update();
            render(roll,frame);
            int[] original=((DataBufferInt)frame.getRaster().getDataBuffer()).getData().clone();
            int time=roll.elapsed();
            int tile=gp.tileM.mapTileNum[0][18][30];
            gp.player.worldX+=211;
            gp.tileM.mapTileNum[0][18][30]=0;
            render(roll,frame);
            check(time==roll.elapsed() && Arrays.equals(original,((DataBufferInt)frame.getRaster().getDataBuffer()).getData()),
                "independent drawing "+card.id());
            gp.player.worldX-=211;
            gp.tileM.mapTileNum[0][18][30]=tile;
            ImageIO.write(frame,"png",OUT.resolve(String.format("%02d-%s.png",shown,card.id())).toFile());
            while (roll.elapsed()<card.end()-17) roll.update();
            check(CreditRoll.cardAt(roll.elapsed())==index,"last tick "+card.id());
        }
        check(shown==14 && lines==71,"all cards and original lyric lines");
        while (roll.elapsed()<120000) {
            check(!roll.farewell(),"farewell does not begin early");
            roll.update();
        }
        check(roll.elapsed()==120000 && roll.farewell() && !roll.canPlayAgain(),"farewell starts on tick 7200");
        for (int i=0;i<CreditRoll.REVEAL_TICKS;i++) roll.update();
        check(roll.canPlayAgain(),"button becomes active after reveal");
        ImageIO.write(render(roll,frame),"png",OUT.resolve("15-despedida.png").toFile());
    }

    private static void key(int code,boolean pressed) {
        var event=new KeyEvent(gp,pressed?KeyEvent.KEY_PRESSED:KeyEvent.KEY_RELEASED,0,0,code,KeyEvent.CHAR_UNDEFINED);
        if (pressed) gp.keyH.keyPressed(event); else gp.keyH.keyReleased(event);
    }

    private static void click(int x,int y) {
        var click=new MouseEvent(gp,MouseEvent.MOUSE_PRESSED,0,0,x,y,1,false,MouseEvent.BUTTON1);
        for (var listener:gp.getMouseListeners()) listener.mousePressed(click);
    }

    private static void reachCredits() {
        gp.resetGame(true);
        gp.currentMap=FinalArena.MAP;
        gp.gameState=gp.playState;
        gp.obj[FinalArena.MAP][FinalArena.DOOR_SLOT]=null;
        Progress.GodFishDefeated=true;
        gp.se.volumeScale=0;
        check(gp.csManager.enterBeach(),"start ending");
        while (!gp.csManager.isBeachWalk()) gp.update();
        key(KeyEvent.VK_D,true);
        while (!gp.csManager.isDialogueActive()) gp.update();
        key(KeyEvent.VK_D,false);
        for (int i=0;i<8;i++) {
            key(KeyEvent.VK_ENTER,true);
            gp.update();
            if (i<7) key(KeyEvent.VK_ENTER,false);
        }
        while (!gp.csManager.inCredits()) gp.update();
    }

    private static void controls() throws Exception {
        gp.resetGame(true);
        gp.gameState=gp.playState;
        Path scratch=Files.createTempDirectory(Path.of("build"),"credits-save-").resolve("save-v1.dat");
        gp.saveLoad=new data.SaveLoad(gp,scratch);
        check(gp.saveLoad.trySave(),"save for preservation test");
        byte[] before=Files.readAllBytes(scratch);
        reachCredits();
        int inventory=gp.player.inventory.size();
        while (!gp.csManager.canPlayAgain()) {
            key(KeyEvent.VK_ENTER,true); // repeat a key still held since the last dialogue page
            gp.update();
        }
        key(KeyEvent.VK_ENTER,true);
        gp.update();
        check(gp.csManager.canPlayAgain(),"held Enter cannot return to menu");
        gp.setSize(1280,720);
        gp.drawToTempScreen();
        var wide=new BufferedImage(1280,720,BufferedImage.TYPE_INT_RGB);
        var graphics=wide.createGraphics();
        gp.paintComponent(graphics);
        graphics.dispose();
        check(wide.getRGB(0,360)==Color.BLACK.getRGB() && wide.getRGB(1279,360)==Color.BLACK.getRGB(),"wide frame letterboxes");
        ImageIO.write(wide,"png",OUT.resolve("16-despedida-ancha.png").toFile());
        click(20,600);
        check(gp.csManager.canPlayAgain(),"click outside button ignored");
        key(KeyEvent.VK_ENTER,false);
        key(KeyEvent.VK_C,true);
        check(!gp.csManager.canPlayAgain(),"original acknowledgements available");
        key(KeyEvent.VK_ESCAPE,true);
        check(gp.csManager.canPlayAgain(),"close acknowledgements");
        key(KeyEvent.VK_ENTER,true);
        check(gp.gameState==gp.titleState,"new Enter returns to title");
        key(KeyEvent.VK_ENTER,true);
        gp.update();
        check(gp.gameState==gp.titleState,"held Enter cannot launch new game from the menu");
        key(KeyEvent.VK_ENTER,false);
        check(Progress.GodFishDefeated && gp.player.inventory.size()==inventory,"world and inventory stay intact");
        check(Arrays.equals(before,Files.readAllBytes(scratch)),"save file stays intact");
        reachCredits();
        while (!gp.csManager.canPlayAgain()) gp.update();
        click(640,607);
        check(gp.gameState==gp.titleState,"fresh click inside the scaled button works");
        key(KeyEvent.VK_ENTER,false);
        key(KeyEvent.VK_ENTER,true);
        check(gp.gameState==gp.playState && !Progress.GodFishDefeated,"new game is selectable");
    }

    private static void video(CreditRoll roll) throws Exception {
        var frame=new BufferedImage(960,576,BufferedImage.TYPE_3BYTE_BGR);
        var pixels=((DataBufferByte)frame.getRaster().getDataBuffer()).getData();
        roll.begin();
        try (var output=new BufferedOutputStream(System.out,1<<20)) {
            for (int tick=0;tick<126*60;tick+=2) {
                render(roll,frame);
                output.write(pixels);
                roll.update();
                roll.update();
            }
        }
    }
}
