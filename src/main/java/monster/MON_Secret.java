/*
 * Decompiled with CFR 0.152.
 */
package monster;

import entity.Entity;
import java.util.Random;
import main.GamePanel;
import object.OBJ_Coin;

public class MON_Secret
extends Entity {
    GamePanel gp;

    public enum Escape { HIDDEN, FLEE, TIRED }
    private Escape escape=Escape.HIDDEN;
    private int identity=-1, ticks, stepCounter;
    private java.awt.Rectangle niche;
    private final String[] dirs={"up","down","left","right"};
    private final int[] dx={0,0,-1,1},dy={-1,1,0,0};
    private java.awt.image.BufferedImage[][][] poses;
    private java.awt.image.BufferedImage[] bubbles;
    private final int[] age={0,0}, bubbleX=new int[2],bubbleY=new int[2];

    public boolean templeSecret() { return identity>=0 && gp.currentMap==main.Temple.MAP; }
    public Escape escape() { return escape; }
    public int decoys() { return (age[0]>0?1:0)+(age[1]>0?1:0); }

    public void configureTemple(int id, java.awt.Rectangle box) {
        identity=id; niche=new java.awt.Rectangle(box);
        name="Secret";
        if (poses==null) {
            poses=new java.awt.image.BufferedImage[3][4][2];
            String[] states={"oculto","huida","agotado"};
            for (int s=0;s<3;s++) for(int d=0;d<4;d++) for(int f=0;f<2;f++)
                poses[s][d][f]=setup("/septima/secret/"+states[s]+"-"+dirs[d]+"-"+f,48,48);
            bubbles=new java.awt.image.BufferedImage[]{setup("/septima/secret/decoy-0",48,48),setup("/septima/secret/decoy-1",48,48)};
        }
        resetEscape();
    }

    public void resetEscape() {
        escape=Escape.HIDDEN; ticks=stepCounter=0;
        age[0]=age[1]=0;
        finishKnockBack();
    }

    public boolean claimReward() { return identity<0 || gp.temple.grantSecret(identity); }

    @Override public void update() {
        if (!templeSecret()) { super.update(); return; }
        if (gp.gameState!=gp.playState || !alive || dying || life<=0) return;
        updateCombatTimers();
        if (knockBack) finishKnockBack();
        for (int i=0;i<2;i++) if (age[i]>0) age[i]--;
        if ((gp.temple.reliefs & (1<<identity))==0) return;
        ticks++;
        switch (escape) {
            case HIDDEN -> {
                if (niche.contains(gp.player.worldX,gp.player.worldY) &&
                    Math.abs(gp.player.worldX-worldX)+Math.abs(gp.player.worldY-worldY)<=144) {
                    escape=Escape.FLEE; ticks=stepCounter=0;
                }
            }
            case FLEE -> {
                if (ticks>=180) { escape=Escape.TIRED; ticks=0; return; }
                if (ticks%45==1) for (int i=0;i<2;i++) if(age[i]==0) {
                    age[i]=86; bubbleX[i]=worldX; bubbleY[i]=worldY; break;
                }
                fleeStep();
            }
            case TIRED -> {
                if (ticks>=90) { escape=Escape.HIDDEN; ticks=0; }
            }
        }
    }

    private void fleeStep() {
        int current=java.util.List.of(dirs).indexOf(direction), chosen=-1;
        int best=Integer.MIN_VALUE;
        for (int d=0;d<4;d++) {
            int x=worldX+dx[d]*4,y=worldY+dy[d]*4;
            if (!niche.contains(main.Temple.bounds(this,x,y)) || !main.Temple.clear(gp,this,x,y)) continue;
            if (stepCounter>0 && d==current) { chosen=d; break; }
            int score=Math.abs(x-gp.player.worldX)+Math.abs(y-gp.player.worldY);
            if (score>best) { best=score; chosen=d; }
        }
        if (chosen<0) return;
        if (stepCounter<=0 || chosen!=current) stepCounter=10;
        stepCounter--;
        direction=dirs[chosen];
        worldX+=dx[chosen]*4; worldY+=dy[chosen]*4;
    }

    @Override public void draw(java.awt.Graphics2D g) {
        if (!templeSecret()) { super.draw(g); return; }
        if ((gp.temple.reliefs & (1<<identity))==0 || !inCamera()) return;
        for (int i=0;i<2;i++) if(age[i]>0 && !dying) {
            int elapsed=86-age[i], side=i==0?-1:1;
            g.drawImage(bubbles[elapsed/10%2],bubbleX[i]+side*elapsed/5-gp.player.worldX+gp.player.screenX,
                bubbleY[i]-gp.player.worldY+gp.player.screenY,null);
        }
        var copy=(java.awt.Graphics2D)g.create();
        if (invincible) changeAlpha(copy,0.4f);
        if (dying) dyingAnimation(copy);
        int facing=java.util.List.of(dirs).indexOf(direction);
        copy.drawImage(poses[escape.ordinal()][facing][ticks/10%2],getScreenX(),getScreenY(),null);
        copy.dispose();
    }

    @Override public void updateVisualState() {
        if (templeSecret() && gp.gameState!=gp.playState) return;
        super.updateVisualState();
    }


    public MON_Secret(GamePanel gp) {
        super(gp);
        this.gp = gp;
        this.type = 2;
        this.name = "Fhisty";
        this.speed = this.defaultSpeed = 1;
        this.life = this.maxLife = 100;
        this.attack = 1;
        this.defense = 0;
        this.exp = 100;
        this.solidArea.x = 0;
        this.solidArea.y = 0;
        this.solidArea.width = 48;
        this.solidArea.height = 48;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;
        this.getImage();
    }

    public void getImage() {
        this.up = this.setup("/monster/SecretUp", this.gp.tileSize, this.gp.tileSize);
        this.up2 = this.setup("/monster/SecretUp2", this.gp.tileSize, this.gp.tileSize);
        this.left = this.setup("/monster/SecretLeft", this.gp.tileSize, this.gp.tileSize);
        this.left2 = this.setup("/monster/SecretLeft2", this.gp.tileSize, this.gp.tileSize);
        this.down = this.setup("/monster/SecretDown", this.gp.tileSize, this.gp.tileSize);
        this.down2 = this.setup("/monster/SecretDown2", this.gp.tileSize, this.gp.tileSize);
        this.right = this.setup("/monster/SecretRight", this.gp.tileSize, this.gp.tileSize);
        this.right2 = this.setup("/monster/SecretRigh2", this.gp.tileSize, this.gp.tileSize);
    }

    @Override
    public void setAction() {
        if (!this.onPath) {
            this.getRandomDirection(80);
        }
    }

    @Override
    public void damageReaction() {
        this.actionLockCounter = 0;
    }

    @Override
    public void checkDrop() {
        int i = new Random().nextInt(50) + 1;
        if (i < 50) {
            this.dropItem(new OBJ_Coin(this.gp));
        }
    }
}

