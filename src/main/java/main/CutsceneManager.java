/*
 * Decompiled with CFR 0.152.
 */
package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import main.GamePanel;
import object.OBJ_Exit;

public class CutsceneManager {
    GamePanel gp;
    Graphics2D g2;
    public int sceneNum;
    public int scenePhase;
    private static final int EXIT_FADE=0, BEACH_ARRIVAL=1, BEACH_WALK=2,
        ENDING_DIALOGUE=3, ENDING_COAST=4;
    static final int ENDING_CREDITS=5;
    private static final String INSTRUMENTAL="/credits/tema-instrumental.wav";
    private static final int FADE_TICKS=45;
    private int portalTick;
    private Sound surf, creditsMusic;
    private CreditRoll credits;
    private final Sound.ClipFactory surfClipFactory;
    private final CoastEpilogue coast=new CoastEpilogue();
    int counter = 0;
    private int introTick, introStartY, introTargetY;
    private boolean seenIntro, shortIntro;
    String endCredit;
    public final int NA = 0;
    public final int GodFish = 1;
    public final int ending = 2;

    public CutsceneManager(GamePanel gp) {
        this(gp,index -> Sound.openClip("/ending/oleaje.wav"));
    }

    CutsceneManager(GamePanel gp,Sound.ClipFactory surfClipFactory) {
        this.gp = gp;
        this.surfClipFactory = surfClipFactory;
        this.endCredit = "Encargado de hacer todo: Yo\nDavid Silva Padilla\n\n\n\n\n\n\n\n\n\n\n\n\n\nAgradecimientos especiales\nYo\nMi papa\nMi mama\nMi hermano (un poco)\nMolly\nSammy\nLuny\nKitty\nDina\nMis amigos (no los topo)\nEs broma XD\nLos limones son god\nque\nso\nNumero de cuenta bancaria: \nMe acorde de que no tengo\nAyer que hice?\nNose\nPero mientras les cuento que...\nBla bla bla\nBla bla bla\nBla bla bla\nNo eh dormido nada hoy\nUn saludo a la Grasa :v \nChao Chao \n";
    }

    public void update() {
        if (sceneNum == GodFish) updateFinalIntro();
        else if (sceneNum == ending) scene_Ending();
        else if (gp.currentMap==FinalArena.MAP && gp.gameState==gp.playState) portalTick++;
    }

    public boolean isDialogueActive() {
        return (sceneNum == GodFish && scenePhase == 3) || (sceneNum == ending && scenePhase == ENDING_DIALOGUE);
    }

    public void draw(Graphics2D g2) {
        this.g2 = g2;
        gp.ui.g2 = g2;
        g2.setFont(gp.ui.purisaB);
        g2.setColor(Color.WHITE);
        if (sceneNum == GodFish) drawFinalIntro(g2);
        if (sceneNum != ending) {
            if (isDialogueActive()) gp.ui.drawDialogueScreen();
            return;
        }
        switch (scenePhase) {
            case EXIT_FADE -> drawBlackBackground(counter/(float)FADE_TICKS);
            case BEACH_ARRIVAL, BEACH_WALK, ENDING_DIALOGUE -> {
                coast.drawWalk(g2,gp.player,scenePhase!=ENDING_DIALOGUE);
                if (scenePhase==BEACH_ARRIVAL) drawBlackBackground(1f-counter/(float)FADE_TICKS);
                if (isDialogueActive()) gp.ui.drawDialogueScreen();
            }
            case ENDING_COAST -> {
                coast.drawPortrait(g2,counter,gp.ui.purisaB);
                if (counter>=CoastEpilogue.DURATION-CoastEpilogue.FADE)
                    drawBlackBackground(1f-(CoastEpilogue.DURATION-counter)/(float)CoastEpilogue.FADE);
            }
            case ENDING_CREDITS -> credits.draw(g2,endCredit);
            default -> { }
        }
    }

    public void reset() {
        sceneNum = NA;
        scenePhase = counter = 0;
        introTick = portalTick = 0;
        coast.reset();
        stopEndingAudio();
    }

    public void clearIntroMemory(boolean newSession) {
        if (newSession) seenIntro = false;
    }

    void beginFinalIntro() {
        if (sceneNum!=NA || gp.finalArena.boss()==null) return;
        gp.flow.resetDialogue();
        gp.keyH.clearPlayKeys();
        gp.player.direction = "up";
        gp.player.attacking = gp.player.guarding = false;
        gp.player.projectile.alive = false;
        introStartY = gp.player.worldY-gp.player.screenY;
        introTargetY = FinalArena.BOSS_Y-174;
        shortIntro = seenIntro;
        introTick = 0;
        sceneNum = GodFish;
        scenePhase = 0;
        gp.gameState = gp.cutsceneState;
        gp.finalArena.seal();
    }

    int finalCameraY() {
        if (scenePhase==0) return introStartY;
        if (scenePhase==1) return smooth(introStartY,introTargetY,introTick/(shortIntro?42f:60f));
        if (scenePhase==4) return smooth(introTargetY,gp.finalArena.targetCameraY(),(introTick-18)/42f);
        return introTargetY;
    }

    private static int smooth(int from, int to, float t) {
        t = Math.max(0f,Math.min(1f,t));
        return Math.round(from+(to-from)*t*t*(3-2*t));
    }

    private void updateFinalIntro() {
        var boss = gp.finalArena.boss();
        if (boss==null || gp.currentMap!=FinalArena.MAP) {
            gp.finalArena.releaseSeal();
            gp.flow.resetDialogue();
            gp.ui.npc = null;
            gp.keyH.clearPlayKeys();
            gp.gameState = gp.playState;
            reset();
            return;
        }
        switch (scenePhase) {
            case 0 -> {
                if (++introTick >= (shortIntro ? 8 : 18)) advanceIntro();
            }
            case 1 -> {
                gp.finalArena.advanceIntroPlayer();
                if (++introTick >= (shortIntro ? 42 : 60)) advanceIntro();
            }
            case 2 -> {
                if (++introTick >= (shortIntro ? 14 : 28)) {
                    boss.dialogueSet = 0;
                    gp.flow.beginDialogue(boss,0,shortIntro ? 3 : 0);
                    advanceIntro();
                }
            }
            case 3 -> gp.flow.updateDialogue();
            case 4 -> {
                if (++introTick >= 60) {
                    seenIntro = true;
                    gp.ui.npc = null;
                    gp.keyH.clearPlayKeys();
                    reset();
                    gp.gameState = gp.playState;
                    gp.bossBattleOn = true;
                    gp.finalArena.beginCombatCamera();
                    boss.beginFight();
                    gp.stopMusic();
                    gp.playMusic(23);
                }
            }
            default -> throw new IllegalStateException("Unexpected final introduction beat");
        }
    }

    private void advanceIntro() { scenePhase++; introTick = 0; }

    private void drawFinalIntro(Graphics2D g) {
        if (scenePhase>=3) return;
        float darkness = switch (scenePhase) {
            case 0, 1 -> .86f;
            default -> Math.max(0f, .86f-introTick/(shortIntro?14f:28f));
        };
        if (darkness>0) {
            g.setColor(new Color(0,9,18,Math.round(darkness*255)));
            g.fillRect(0,0,gp.screenWidht,gp.screenHeight);
        }
        var boss = gp.finalArena.boss();
        if (boss!=null && scenePhase>=1 && (scenePhase==2 || introTick>12))
            GodFishArt.eye(g,boss.getScreenX(),boss.getScreenY());
        // The real player stays legible during the pan.
        gp.player.draw(g);
    }

    public boolean enterBeach() {
        if (sceneNum!=NA || gp.gameState!=gp.playState || gp.currentMap!=FinalArena.MAP
                || !data.Progress.GodFishDefeated || gp.obj[FinalArena.MAP][FinalArena.DOOR_SLOT]!=null)
            return false;
        gp.keyH.clearPlayKeys();
        gp.flow.resetDialogue();
        gp.stopMusic();
        coast.reset();
        sceneNum=ending;
        scenePhase=EXIT_FADE;
        counter=0;
        gp.gameState=gp.cutsceneState;
        return true;
    }

    public void drawExitPortal(Graphics2D graphics,int x,int y) {
        CoastEpilogue.drawPortal(graphics,x,y,portalTick);
    }

    boolean isBeachWalk() { return sceneNum==ending && scenePhase==BEACH_WALK; }

    boolean drawsBeach() { return sceneNum==ending && scenePhase>=BEACH_ARRIVAL; }

    private synchronized void startSurf() {
        if (gp.se.volumeScale==0) return;
        surf=new Sound(1,surfClipFactory);
        surf.volumeScale=gp.se.volumeScale;
        surf.requestPlay(0,true);
    }

    synchronized void stopSurf() {
        if (surf==null) return;
        surf.close();
        surf=null;
    }

    synchronized void stopEndingAudio() {
        stopSurf();
        stopCreditsMusic();
    }

    private synchronized void startCreditsMusic() {
        if (gp.music.volumeScale==0 || CutsceneManager.class.getResource(INSTRUMENTAL)==null) return;
        creditsMusic=new Sound(1,index -> Sound.openClip(INSTRUMENTAL));
        creditsMusic.volumeScale=gp.music.volumeScale;
        creditsMusic.requestPlay(0,false);
    }

    private synchronized void stopCreditsMusic() {
        if (creditsMusic==null) return;
        creditsMusic.close();
        creditsMusic=null;
    }

    private synchronized void fadeSurf(float amount) {
        if (surf!=null) surf.setAttenuation(50f*amount);
    }

    public void scene_Ending() {
        if (scenePhase>=BEACH_ARRIVAL && scenePhase<=ENDING_COAST) coast.advanceSea();
        switch (scenePhase) {
            case EXIT_FADE -> {
                if (++counter>=FADE_TICKS) {
                    if (credits==null) credits=new CreditRoll(gp);
                    startSurf();
                    nextEndingBeat();
                }
            }
            case BEACH_ARRIVAL -> {
                if (++counter>=FADE_TICKS) {
                    gp.keyH.clearPlayKeys();
                    nextEndingBeat();
                }
            }
            case BEACH_WALK -> {
                if (coast.move(gp.keyH.leftPressed,gp.keyH.rightPressed)) {
                    coast.stopFish();
                    gp.keyH.clearPlayKeys();
                    gp.flow.beginDialogue(new OBJ_Exit(gp),0,0);
                    nextEndingBeat();
                }
            }
            case ENDING_DIALOGUE -> {
                gp.flow.updateDialogue();
                if (scenePhase==ENDING_COAST) {
                    gp.ui.npc=null;
                    gp.keyH.clearPlayKeys();
                }
            }
            case ENDING_COAST -> {
                if (++counter>=CoastEpilogue.DURATION) {
                    stopSurf();
                    credits.begin();
                    startCreditsMusic();
                    nextEndingBeat();
                } else if (counter>=CoastEpilogue.DURATION-CoastEpilogue.FADE && counter%3==0) {
                    fadeSurf(1f-(CoastEpilogue.DURATION-counter)/(float)CoastEpilogue.FADE);
                }
            }
            case ENDING_CREDITS -> {
                boolean wasFarewell=credits.farewell();
                credits.update();
                if (!wasFarewell && credits.farewell()) stopCreditsMusic();
            }
            default -> throw new IllegalStateException("Unknown ending beat");
        }
    }

    private void nextEndingBeat() {
        scenePhase++;
        counter=0;
    }

    boolean isCoast() { return sceneNum==ending && scenePhase==ENDING_COAST; }

    boolean inCredits() { return sceneNum==ending && scenePhase==ENDING_CREDITS; }

    int creditsElapsed() { return inCredits()?credits.elapsed():-1; }

    boolean canPlayAgain() { return inCredits() && credits.canPlayAgain(); }

    void toggleAcknowledgements(boolean onlyClose) {
        if (inCredits() && (!onlyClose || credits.showAcknowledgements())) credits.toggleAcknowledgements();
    }

    void playAgain() {
        if (!canPlayAgain()) return;
        gp.keyH.clearPlayKeys();
        gp.flow.reset();
        reset();
        gp.stopMusic();
        gp.ui.titleScreenState=0;
        gp.ui.commandNum=0;
        gp.gameState=gp.titleState;
    }

    public void drawBlackBackground(float alpha) {
        this.g2.setComposite(AlphaComposite.getInstance(3, alpha));
        this.g2.setColor(Color.black);
        this.g2.fillRect(0, 0, this.gp.screenWidht, this.gp.screenHeight);
        this.g2.setComposite(AlphaComposite.getInstance(3, 1.0f));
    }

}
