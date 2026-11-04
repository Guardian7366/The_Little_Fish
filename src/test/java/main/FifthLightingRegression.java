package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import object.OBJ_Lantern;

public final class FifthLightingRegression {
    static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
    public static void main(String[] args) {
        var gp=new FifthZoneRegression.Quiet();gp.setupGame();gp.currentMap=3;gp.gameState=gp.playState;
        var lighting=gp.eManager.lighting;var f=lighting.fifth;
        check(f.fixedAlpha(25*48+24,19*48+24)<50,"actual light source");
        check(f.fixedAlpha(25*48+24,17*48+24)==165,"wall occludes light");
        check(gp.tileM.tile[89].collision==false&&gp.tileM.tile[88].collision,"separate visual/collision contracts");
        int counter=gp.ocean.fifth.tick();gp.gameState=gp.pauseState;gp.update();check(gp.ocean.fifth.tick()==counter,"frozen background");
        gp.gameState=gp.playState;gp.player.worldX=25*48;gp.player.worldY=23*48;
        BufferedImage dark=new BufferedImage(960,576,2),light=new BufferedImage(960,576,2);
        Graphics2D g=dark.createGraphics();f.draw(g);g.dispose();
        gp.player.currentLight=new OBJ_Lantern(gp);f.prepare();g=light.createGraphics();f.draw(g);g.dispose();
        check(gp.player.currentLight.lightRadius==400,"original lantern radius");
        check((light.getRGB(480,288)>>>24)<(dark.getRGB(480,288)>>>24),"lantern improves gaps");
        for(int y=0;y<576;y+=3)for(int x=0;x<960;x+=3)check((light.getRGB(x,y)>>>24)<=(dark.getRGB(x,y)>>>24),"minimum darkness composition");
        int count=SpriteCache.cachedImageCount();long memory=f.cachedPixelBytes()+gp.ocean.fifth.cachedPixelBytes();
        long[] update=new long[180],draw=new long[180];
        for(int i=0;i<210;i++) {
            long a=System.nanoTime();gp.update();long b=System.nanoTime();gp.drawToTempScreen();long c=System.nanoTime();
            if(i>=30){update[i-30]=b-a;draw[i-30]=c-b;}
        }
        check(SpriteCache.cachedImageCount()==count,"no new decoded assets in repeated draw");
        Arrays.sort(update);Arrays.sort(draw);
        System.out.printf(Locale.ROOT,"FIFTH_LIGHT PASS: occlusion, min-alpha, radius400, pause, bounded cache; fixed+background cache=%d bytes; 180 warmed frames update p50=%.3f p95=%.3f ms; draw p50=%.3f p95=%.3f ms%n",memory,update[90]/1e6,update[171]/1e6,draw[90]/1e6,draw[171]/1e6);
        // Deliberate upper-load fixture: 40 sources and 32 enemies, never a production map edit.
        gp.tileM.loadMap("/maps/stage4.txt",3); // revision invalidation tested before stress terrain
        for(int[] col:gp.tileM.mapTileNum[3])Arrays.fill(col,69);
        for(int i=0;i<40;i++)gp.tileM.mapTileNum[3][8+i%8*4][8+i/8*7]=FifthZone.GLOW_TOP;
        Arrays.fill(gp.monster[3],null);Arrays.fill(gp.iTile[3],null);
        for(int i=0;i<32;i++) {
            var s=new monster.MON_Sharky(gp);s.worldX=(10+i%8*4)*48;s.worldY=(12+i/8*7)*48;
            s.setHome(s.worldX,s.worldY,new Rectangle(48,48,48*48,48*48),i/2);gp.monster[3][i]=s;
        }
        gp.player.life=gp.player.maxLife=10000;gp.player.worldX=26*48;gp.player.worldY=26*48;
        gp.player.currentLight=new OBJ_Lantern(gp);f.prepare();
        check(f.cachedPixelBytes()+gp.ocean.fifth.cachedPixelBytes()==memory,"memory independent of light count");
        count=SpriteCache.cachedImageCount();
        int attackTicks=0;
        for(int i=0;i<210;i++) {
            long a=System.nanoTime();gp.update();long b=System.nanoTime();gp.drawToTempScreen();long c=System.nanoTime();
            if(i>=30){update[i-30]=b-a;draw[i-30]=c-b;}
            for(var e:gp.monster[3])if(e instanceof monster.MON_Sharky s &&
                (s.state()==monster.MON_Sharky.State.PREPARE||s.state()==monster.MON_Sharky.State.CHARGE))attackTicks++;
        }
        check(attackTicks>0,"stress fixture includes active ambushes");
        check(SpriteCache.cachedImageCount()==count,"stress drawing decodes no new images");
        Arrays.sort(update);Arrays.sort(draw);
        System.out.printf(Locale.ROOT,"FIFTH_STRESS PASS: 40 lights/32 Sharkys; active attack-ticks=%d; cache=%d bytes; update p50=%.3f p95=%.3f ms; draw p50=%.3f p95=%.3f ms%n",attackTicks,memory,update[90]/1e6,update[171]/1e6,draw[90]/1e6,draw[171]/1e6);
        gp.stopGame();
    }
}
