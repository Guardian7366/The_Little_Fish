package main;
import java.awt.*;import java.awt.image.*;import java.nio.file.*;import javax.imageio.*;
public final class TitleRegression {
 static int checks;
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static class Quiet extends GamePanel {public void playSE(int i){}public void playMusic(int i){}public void stopMusic(){}}
 public static void main(String[] args)throws Exception{
 Quiet gp=new Quiet();gp.setupGame();TitleBackground bg=gp.ui.titleBackground;
 check(bg.frameCount()==64,"Approved 64 frames");
 BufferedImage canvas=new BufferedImage(966,582,2);Graphics2D g=canvas.createGraphics();g.setColor(Color.MAGENTA);g.fillRect(0,0,966,582);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);bg.draw(g);
 check(g.getRenderingHint(RenderingHints.KEY_INTERPOLATION)==RenderingHints.VALUE_INTERPOLATION_BILINEAR,"Drawing restores interpolation");g.dispose();
 BufferedImage src=ImageIO.read(TitleRegression.class.getResource("/menu/ocean.gif"));
 for(int y=0;y<576;y++)for(int x=0;x<960;x++)check(canvas.getRGB(x,y)==src.getRGB(x/3,y/3),"Exact 3x source pixels");
 check(canvas.getRGB(960,575)==Color.MAGENTA.getRGB() && canvas.getRGB(959,576)==Color.MAGENTA.getRGB(),"Background never paints outside viewport");
 check(bg.frameIndex()==0,"Drawing never advances animation");
 for(int i=0;i<7;i++)gp.update();check(bg.frameIndex()==0,"Frame not advanced early");gp.update();check(bg.frameIndex()==1,"120ms frame advances");
 for(int i=8;i<480;i++)gp.update();check(bg.frameIndex()==0,"Eight second loop exact");
 gp.gameState=gp.pauseState;for(int i=0;i<60;i++)gp.update();check(bg.frameIndex()==0,"Animation does not advance outside menu");gp.gameState=gp.titleState;
 Path out=Path.of("build/menu-preview");Files.createDirectories(out);
 for(int frame:new int[]{0,15,31,47}){while(bg.frameIndex()!=frame)bg.update();gp.drawToTempScreen();ImageIO.write(gp.tempScreen,"png",out.resolve("menu-"+frame+".png").toFile());}
 gp.setSize(1280,720);BufferedImage wide=new BufferedImage(1280,720,2);g=wide.createGraphics();gp.paintComponent(g);g.dispose();check(wide.getRGB(0,360)==Color.BLACK.getRGB()&&wide.getRGB(1279,360)==Color.BLACK.getRGB(),"Wide screen letterboxes, no stretch or crop");ImageIO.write(wide,"png",out.resolve("wide.png").toFile());
 gp.ui.commandNum=0;gp.keyH.titleState(java.awt.event.KeyEvent.VK_S);check(gp.ui.commandNum==1,"Load selectable");gp.keyH.titleState(java.awt.event.KeyEvent.VK_W);gp.keyH.titleState(java.awt.event.KeyEvent.VK_ENTER);check(gp.gameState==gp.playState,"New game still starts");
 gp.stopGame();System.out.println("TITLE_REGRESSION PASS "+checks+" assertions (mostly pixel comparison)");
 }
}
