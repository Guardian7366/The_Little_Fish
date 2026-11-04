package main;

import java.awt.Rectangle;
import java.nio.file.*;
import java.util.Arrays;
import javax.imageio.ImageIO;
import entity.Entity;
import monster.MON_Sharky;
import object.OBJ_Chest;
import tile_interactive.IT_Rock;

/** Deterministic, isolated state-machine contracts, using real collision and guard logic. */
public final class SharkyRegression {
    static int checks;
    static class Quiet extends GamePanel {
        public void playMusic(int i) {} public void stopMusic() {} public void playSE(int i) {}
    }
    static void check(boolean ok,String text) { if(!ok) throw new AssertionError(text); checks++; }
    static Quiet panel() {
        Quiet gp=new Quiet();gp.setupGame();gp.currentMap=3;gp.currentArea=gp.dungeons;gp.gameState=gp.playState;
        for(int[] col:gp.tileM.mapTileNum[3]) Arrays.fill(col,69);
        Arrays.fill(gp.monster[3],null);Arrays.fill(gp.obj[3],null);Arrays.fill(gp.npc[3],null);Arrays.fill(gp.iTile[3],null);
        gp.player.worldX=24*48;gp.player.worldY=20*48; return gp;
    }
    static MON_Sharky shark(Quiet gp,int x,int y,int slot,int group) {
        MON_Sharky s=new MON_Sharky(gp);s.worldX=x*48;s.worldY=y*48;
        s.setHome(s.worldX,s.worldY,new Rectangle(14*48,12*48,24*48,20*48),group);
        gp.monster[3][slot]=s;return s;
    }
    static void ticks(MON_Sharky s,int n) {for(int i=0;i<n;i++)s.update();}
    static void ready(MON_Sharky s) {ticks(s,90);check(s.state()==MON_Sharky.State.PREPARE,"visible preparation after cooldown");}
    static void capture(Quiet gp,String name)throws Exception {
        // Isolated combat plate deliberately excludes the dungeon filter; zone QA uses real lighting.
        var image=new java.awt.image.BufferedImage(960,576,2);var g=image.createGraphics();gp.tileM.draw(g);
        for(Entity e:gp.monster[3])if(e!=null)e.draw(g);gp.player.draw(g);g.dispose();
        Path out=Path.of("build/sharky-preview");Files.createDirectories(out);
        ImageIO.write(image,"png",out.resolve(name+".png").toFile());
    }
    public static void main(String[] args)throws Exception {
        Quiet gp=panel();MON_Sharky s=shark(gp,20,20,0,-1);
        check(s.life==20&&s.attack==9&&s.defense==0&&s.exp==28,"original stats");
        int life=gp.player.life;gp.player.contactMonster(0);check(gp.player.life==life,"no passive contact damage");
        ready(s);capture(gp,"prepare");
        for(int state:new int[]{gp.pauseState,gp.dialogueState,gp.optionsState,gp.cutsceneState}) {
            gp.gameState=state;int t=s.stateTicks();ticks(s,70);check(s.stateTicks()==t,"frozen outside play");
        }
        gp.gameState=gp.playState;ticks(s,30);check(s.state()==MON_Sharky.State.CHARGE,"30-tick warning");
        ticks(s,5);capture(gp,"charge");String locked=s.direction;
        gp.player.worldY+=48;ticks(s,25);check(s.direction.equals(locked),"straight charge never turns");
        check(s.travelled()<=240,"five-tile cap");check(s.state()==MON_Sharky.State.RECOVER,"miss recovers");
        s.life=7;capture(gp,"miss");ticks(s,700);check(s.life==7,"return never heals");
        check(Math.abs(s.worldX-20*48)+Math.abs(s.worldY-20*48)<=14,"returns home then slow local patrol");
        gp.stopGame();

        for(int distance:new int[]{2,5}) {
            gp=panel();s=shark(gp,24-distance,20,0,-1);ready(s);ticks(s,29);
            check(s.state()==MON_Sharky.State.PREPARE,"full reaction window at "+distance+" tiles");
            ticks(s,1);int before=gp.player.life;
            for(int t=0;t<40;t++){gp.player.invincible=false;s.update();gp.player.contactMonster(0);}
            check(gp.player.life==before-Math.max(1,9-gp.player.defense),"one hit per pass at "+distance);
            gp.stopGame();
        }
        for(boolean parry:new boolean[]{true,false}) {
            gp=panel();s=shark(gp,22,20,0,-1);ready(s);ticks(s,30);
            gp.player.guarding=true;gp.player.guardCounter=parry?0:20;gp.player.direction="left";int before=gp.player.life;
            ticks(s,20);
            check(s.state()==(parry?MON_Sharky.State.STUNNED:MON_Sharky.State.RECOVER),"guard/parry interrupts pass");
            check(gp.player.life==before-(parry?0:Math.max(0,(9-gp.player.defense)/3)),"front guard damage");
            if(parry)capture(gp,"parry");gp.stopGame();
        }
        gp=panel();s=shark(gp,22,20,0,-1);ready(s);ticks(s,30);gp.player.invincible=true;life=gp.player.life;ticks(s,20);
        check(gp.player.life==life&&s.state()==MON_Sharky.State.RECOVER,"invulnerability consumes pass");gp.stopGame();

        for(int obstacle=0;obstacle<4;obstacle++) {
            gp=panel();s=shark(gp,20,20,0,-1);ready(s);ticks(s,30);
            switch(obstacle) {
                case 0->gp.tileM.mapTileNum[3][22][20]=48;
                case 1->{OBJ_Chest o=new OBJ_Chest(gp);o.worldX=22*48;o.worldY=20*48;gp.obj[3][0]=o;}
                case 2->gp.iTile[3][0]=new IT_Rock(gp,22,20);
                case 3->shark(gp,22,20,1,-1);
            }
            ticks(s,30);check(s.worldX+42<=22*48+(obstacle==1?4:0),"swept obstacle "+obstacle);
            check(s.state()==MON_Sharky.State.STUNNED,"collision punishment "+obstacle);
            if(obstacle==0){capture(gp,"wall-stunned");ticks(s,44);check(s.state()==MON_Sharky.State.STUNNED,"wall recovery longer than miss");}
            gp.stopGame();
        }
        gp=panel();s=shark(gp,20,20,0,-1);gp.tileM.mapTileNum[3][22][20]=48;ticks(s,300);
        check(s.state()==MON_Sharky.State.LURK,"no attacks through walls");
        gp.tileM.mapTileNum[3][22][20]=69;gp.player.worldX=35*48;ticks(s,300);
        check(s.state()==MON_Sharky.State.LURK,"no offscreen attacks");gp.stopGame();
        gp=panel();s=shark(gp,22,20,0,1);MON_Sharky mate=shark(gp,26,20,1,1);
        int a=0,b=0;for(int t=0;t<1200;t++){s.update();mate.update();
            boolean aa=s.state()==MON_Sharky.State.PREPARE||s.state()==MON_Sharky.State.CHARGE;
            boolean bb=mate.state()==MON_Sharky.State.PREPARE||mate.state()==MON_Sharky.State.CHARGE;
            check(!(aa&&bb),"pair coordination");if(aa)a++;if(bb)b++;
        }check(a>0&&b>0,"both partners get turns");
        s.resetAmbush();check(s.state()==MON_Sharky.State.LURK,"reload reset");
        gp.stopGame();
        gp=panel();s=shark(gp,22,20,0,1);mate=shark(gp,26,20,1,2);ready(s);ready(mate);
        check(s.state()==MON_Sharky.State.PREPARE&&mate.state()==MON_Sharky.State.PREPARE,"no global encounter lock");
        int beforeTicks=s.stateTicks();capture(gp,"independent-encounters");check(s.stateTicks()==beforeTicks,"rendering does not advance state");gp.stopGame();
        for(int[] pos:new int[][]{{24,18},{24,22},{22,20},{26,20}}) {
            gp=panel();s=shark(gp,pos[0],pos[1],0,-1);ready(s);ticks(s,30);
            gp.player.guarding=true;gp.player.guardCounter=0;gp.player.direction=s.getOppositeDirection(s.direction);life=gp.player.life;
            ticks(s,25);check(s.state()==MON_Sharky.State.STUNNED&&gp.player.life==life,"cardinal parry "+s.direction);gp.stopGame();
        }
        gp=panel();s=shark(gp,20,20,0,-1);ready(s);s.knockBack=true;s.knockBackDirection="left";s.speed=8;int oldX=s.worldX;s.life=4;s.update();
        check(s.worldX<oldX&&s.life==4&&s.state()==MON_Sharky.State.STUNNED,"knockback interrupts preparation safely");
        s.dying=true;s.updateVisualState();check(s.state()==MON_Sharky.State.LURK,"death clears transients through engine visual update");
        gp.gameState=gp.dialogueState;for(int t=0;t<60;t++)s.updateVisualState();check(s.alive,"dialogue freezes death timer");
        gp.gameState=gp.playState;for(int t=0;t<41;t++)s.updateVisualState();check(!s.alive,"active death completes");gp.stopGame();
        System.out.println("SHARKY PASS: "+checks+" assertions; screenshots build/sharky-preview");
    }
}
