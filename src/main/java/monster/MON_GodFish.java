package monster;

import entity.Entity;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import main.FinalArena;
import main.GamePanel;
import main.GodFishArt;
import main.Temple;
import object.OBJ_Iron;
import object.OBJ_Rock;

/** The encounter owns all transient combat state. Only the original victory flag and loot persist. */
public final class MON_GodFish extends Entity {
    private static final int[] FLOORS = {540,430,320,210,105,0};
    private static final Color[] COLORS = {
        new Color(87,225,181), new Color(103,193,255), new Color(190,150,255),
        new Color(243,152,224), new Color(255,117,132), new Color(255,200,105)
    };
    private static final Point STONE_LEFT = cell(19,33), STONE_RIGHT = cell(30,33);
    private static final Point CORE = cell(27,32), VENT = cell(30,35), ALTAR = cell(24,36);
    private static final Point PICARON = cell(20,34), FISHTELLA = cell(29,34);
    private static final Point LEFT_REFUGE = cell(17,33), RIGHT_REFUGE = cell(32,33);
    private static final Rectangle WAVE_LANE = new Rectangle(19*48,19*48,12*48,19*48);
    private static final Rectangle LEFT_SAFE = new Rectangle(16*48,19*48,3*48,19*48);
    private static final Rectangle RIGHT_SAFE = new Rectangle(31*48,19*48,3*48,19*48);
    private static final int[][] LEFT_KNUCKLE={{-21,52},{-62,23},{56,27},{-38,-37}};
    private static final int[][] RIGHT_KNUCKLE={{24,52},{-56,27},{62,23},{37,-37}};
    private static final int STONE_L = 1, STONE_R = 2, GRILLE = 4;
    private static final int LEFT_HAND = 1, RIGHT_HAND = 2;
    private static final int CHARGE_SPEED = 10;

    private enum Facing {
        DOWN(0,1,"down"), LEFT(-1,0,"left"), RIGHT(1,0,"right"), UP(0,-1,"up");
        final int dx,dy;
        final String direction;
        Facing(int dx,int dy,String direction) { this.dx=dx; this.dy=dy; this.direction=direction; }
    }
    private enum Move { SWEEP, VOLLEY, CHARGE, SUMMON, FIST_L, FIST_R, SLAM, WEAK }
    private enum Step { WARN, IMPACT, HOLD, RECOVER }
    private record Ray(Point start, int range) {}
    private record Plan(Move move, Facing aim, Rectangle danger, Point target, List<Ray> rays,
                        int warn, int impact, int hold, int recover, int travel) {}
    private final GamePanel gp;
    private int phase=1, flags, elapsed, rest, cycle, clock, traveled, summon, hitTicks, auraTicks, deathTicks;
    private boolean exposed, impacted, lodged;
    private Facing lastCharge = Facing.DOWN;
    private Plan plan;
    private Step step;

    public MON_GodFish(GamePanel gp) {
        super(gp);
        this.gp=gp;
        type=type_monster;
        name="God Fish";
        boss=true;
        sleep=true;
        maxLife=life=650;
        exp=500;
        knockBackPower=2;
        worldX=FinalArena.BOSS_X;
        worldY=FinalArena.BOSS_Y;
        solidArea.setBounds(-58,-43,116,106);
        solidAreaDefaultX=solidArea.x;
        solidAreaDefaultY=solidArea.y;
        dialogues[0][0]="So you've finally arrived, D.";
        dialogues[0][1]="The temple remembers what you left behind.";
        dialogues[0][2]="You swam all this way to hear it again?";
        dialogues[0][3]="Very well. Come closer,\nif you still want to know.";
    }

    @Override public int getCenterX() { return worldX; }
    @Override public int getCenterY() { return worldY; }

    public int phase() { return phase; }
    public boolean exposed() { return exposed; }

    public void beginFight() {
        sleep=false;
        rest=90;
    }

    @Override public void update() {
        if (sleep || !gp.bossBattleOn || gp.gameState!=gp.playState) return;
        clock++;
        if (hitTicks>0) hitTicks--;
        if (auraTicks>0) auraTicks--;
        if (life==0) {
            if (++deathTicks>=64) alive=false;
            return;
        }
        if (phase==2 && !exposed && gp.player.water==0 && closeTo(VENT,32)) {
            gp.player.water=Math.min(2,gp.player.maxWater);
            if (gp.player.water>0) gp.ui.addMessage("The vent restores water.");
        }
        if (phase==4 && summon>flags && gp.monster[FinalArena.MAP][FinalArena.SUMMON_SLOT]==null) {
            flags=summon;
            if (flags==2) { open(); return; }
            auraTicks=36;
            rest=Math.max(rest,48);
        }
        if (plan!=null) tickPlan();
        else if (rest>0) rest--;
        else if (!exposed || phase==6) choosePlan();
    }

    private boolean closeTo(Point point,int distance) {
        return point.distanceSq(playerX(),playerY())<=distance*distance;
    }

    private int playerX() { return gp.player.worldX+24; }
    private int playerY() { return gp.player.worldY+24; }

    private static Point cell(int x,int y) { return new Point(x*48+24,y*48+24); }
    private static Rectangle box(Point center,int width,int height) {
        return new Rectangle(center.x-width/2,center.y-height/2,width,height);
    }
    private static Point center(Rectangle box) { return new Point(box.x+box.width/2,box.y+box.height/2); }
    private Rectangle playerBody() { return Temple.bounds(gp.player,gp.player.worldX,gp.player.worldY); }
    private Rectangle bodyAt(int x,int y) { return Temple.bounds(this,x,y); }
    private Rectangle mantle() { return box(new Point(worldX,worldY),132,134); }
    private Rectangle eye() { return box(new Point(worldX,worldY+24),54,44); }

    private Facing aim() {
        int dx=playerX()-worldX, dy=playerY()-worldY;
        return Math.abs(dx)>Math.abs(dy) ? (dx<0?Facing.LEFT:Facing.RIGHT) : (dy<0?Facing.UP:Facing.DOWN);
    }

    private void choosePlan() {
        Facing aim=aim();
        int index=cycle++;
        plan=switch (phase) {
            case 1 -> sweep(aim);
            case 2 -> volley(aim);
            case 3 -> charge(aim);
            case 4 -> summon==flags && summon<2 ? summon(aim) : sweep(aim);
            case 5 -> fist(aim,index%2==0);
            case 6 -> exposed ? new Plan(Move.WEAK,aim,box(new Point(worldX,worldY+64),136,90),
                    null,List.of(),50,10,10,32,0)
                : index%3==2 ? new Plan(Move.SLAM,aim,WAVE_LANE,null,List.of(),88,50,14,30,0)
                : fist(aim,index%3==0);
            default -> throw new IllegalStateException("Invalid final phase");
        };
        elapsed=traveled=0;
        impacted=false;
        step=Step.WARN;
    }

    private Plan sweep(Facing aim) {
        Point tip=new Point(worldX+aim.dx*92,worldY+aim.dy*92);
        int reach=range(tip,aim,172,46);
        return new Plan(Move.SWEEP,aim,lane(tip,aim,reach,92),null,List.of(),60,12,16,28,0);
    }

    private Plan volley(Facing aim) {
        List<Ray> rays=new ArrayList<>(3);
        for (int lateral : new int[]{-48,0,48}) {
            Point start=new Point(worldX+aim.dx*75+aim.dy*lateral,worldY+aim.dy*75+aim.dx*lateral);
            int distance=0;
            while (distance<540 && gp.finalArena.clear(box(pointOn(start,aim,distance+1),24,24))) distance++;
            if (!gp.finalArena.clear(box(start,24,24))) distance=0;
            rays.add(new Ray(start,distance));
        }
        int duration=rays.stream().mapToInt(ray->ray.range()/12).max().orElse(0)+23;
        return new Plan(Move.VOLLEY,aim,null,null,List.copyOf(rays),54,duration,10,30,0);
    }

    private Plan charge(Facing aim) {
        Point from=new Point(worldX,worldY);
        int distance=0;
        for (int n=1; n<=310; n++) {
            int x=from.x+aim.dx*n, y=from.y+aim.dy*n;
            if (!gp.finalArena.clear(bodyAt(x,y)) || !gp.finalArena.clear(tipAt(x,y,aim))) break;
            distance=n;
            if (tipAt(x,y,aim).intersects(box(ALTAR,52,43))) break;
        }
        Rectangle initial=bodyAt(worldX,worldY);
        Rectangle danger=initial.union(bodyAt(worldX+aim.dx*distance,worldY+aim.dy*distance));
        danger.add(tipAt(worldX,worldY,aim));
        danger.add(tipAt(worldX+aim.dx*distance,worldY+aim.dy*distance,aim));
        return new Plan(Move.CHARGE,aim,danger,ALTAR,List.of(),72,Math.max(1,(distance+9)/10),20,
            Math.max(24,(distance+9)/10+8),distance);
    }

    private Rectangle tipAt(int x,int y,Facing aim) {
        Point tip=new Point(x+aim.dx*144,y+aim.dy*144);
        return box(tip,aim.dx==0?70:30,aim.dy==0?70:30);
    }

    private Plan summon(Facing aim) {
        Point at=summon==0 ? PICARON : FISHTELLA;
        return new Plan(Move.SUMMON,aim,null,at,List.of(),72,1,18,30,0);
    }

    private Plan fist(Facing aim,boolean left) {
        // Native knuckle centers measured from the atlas anchor in its planted frames.
        int[] offset=(left?LEFT_KNUCKLE:RIGHT_KNUCKLE)[aim.ordinal()];
        Point target=new Point(worldX+offset[0]*3,worldY+offset[1]*3);
        return new Plan(left?Move.FIST_L:Move.FIST_R,aim,box(target,68,64),target,List.of(),
            64,10,phase==5?100:22,28,0);
    }

    private int range(Point start,Facing aim,int limit,int radius) {
        for (int i=0; i<=limit; i++) {
            Point p=new Point(start.x+aim.dx*i,start.y+aim.dy*i);
            if (!gp.finalArena.clear(box(p,aim.dx==0?radius*2:2,aim.dy==0?radius*2:2))) return Math.max(0,i-1);
        }
        return limit;
    }

    private static Rectangle lane(Point start,Facing aim,int length,int width) {
        Rectangle a=box(start,aim.dx==0?width:1,aim.dy==0?width:1);
        return a.union(new Rectangle(a.x+aim.dx*length,a.y+aim.dy*length,a.width,a.height));
    }

    private void tickPlan() {
        Plan active=plan;
        int t=elapsed++;
        int impact=t-active.warn;
        int hold=impact-active.impact;
        int recovery=hold-active.hold;
        if (impact<0) step=Step.WARN;
        else if (hold<0) {
            step=Step.IMPACT;
            execute(active,impact);
        } else if (recovery<0) step=Step.HOLD;
        else {
            step=Step.RECOVER;
            if (active.move==Move.CHARGE) moveHome(active.aim);
            if (recovery+1>=active.recover) {
                plan=null;
                rest=phase==6 && exposed?92:40;
                if (active.move==Move.SLAM) open();
            }
        }
    }

    private void execute(Plan p,int tick) {
        switch (p.move) {
            case VOLLEY -> {
                for (int i=0; i<p.rays.size(); i++) {
                    Ray ray=p.rays.get(i);
                    int position=(tick-i*11)*12;
                    if (position>=0 && position-12<=ray.range && ray.range>0) {
                        Rectangle swept=box(pointOn(ray.start,p.aim,Math.max(0,position-12)),24,24)
                            .union(box(pointOn(ray.start,p.aim,Math.min(position,ray.range)),24,24));
                        hurt(swept,7,p.aim);
                    }
                }
            }
            case CHARGE -> moveCharge(p);
            case SUMMON -> {
                if (tick==0) {
                    if (closeTo(p.target,62)) gp.ui.addMessage("Step away from the summon seal.");
                    else spawn(p.target);
                }
            }
            case SLAM -> {
                hurt(wave(tick,true),13,Facing.DOWN);
                hurt(wave(tick,false),13,Facing.UP);
            }
            case FIST_L,FIST_R -> hurt(p.danger,phase==5?9:10,p.aim);
            case SWEEP -> hurt(p.danger,phase==1?7:8,p.aim);
            case WEAK -> hurt(p.danger,7,Facing.DOWN);
        }
    }

    private static Point pointOn(Point start,Facing facing,int distance) {
        return new Point(start.x+facing.dx*distance,start.y+facing.dy*distance);
    }

    private Rectangle wave(int tick,boolean south) {
        int previous=12*Math.max(0,tick-1), next=12*tick;
        int y=south?worldY+previous:worldY-next;
        return new Rectangle(WAVE_LANE.x,y-10,WAVE_LANE.width,next-previous+20).intersection(WAVE_LANE);
    }

    private void moveCharge(Plan p) {
        Rectangle before=bodyAt(worldX,worldY).union(tipAt(worldX,worldY,p.aim));
        int next=Math.min(p.travel-traveled,CHARGE_SPEED);
        for (int i=0; i<next; i++) {
            int x=worldX+p.aim.dx, y=worldY+p.aim.dy;
            if (!gp.finalArena.clear(bodyAt(x,y)) || !gp.finalArena.clear(tipAt(x,y,p.aim))) break;
            worldX+=p.aim.dx;
            worldY+=p.aim.dy;
            traveled++;
            if (tipAt(worldX,worldY,p.aim).intersects(box(ALTAR,52,43))) {
                lodged=true;
                lastCharge=p.aim;
                break;
            }
        }
        hurt(before.union(bodyAt(worldX,worldY)).union(tipAt(worldX,worldY,p.aim)),11,p.aim);
        if (lodged) open();
    }

    private void moveHome(Facing facing) {
        int distance=Math.min(traveled,CHARGE_SPEED);
        worldX-=facing.dx*distance;
        worldY-=facing.dy*distance;
        traveled-=distance;
    }

    private void hurt(Rectangle bounds,int damage,Facing aim) {
        if (!impacted && bounds.intersects(playerBody())) {
            impacted=true;
            if (!gp.keyH.godModeOn) damagePlayer(damage,aim.direction,false);
        }
    }

    private void spawn(Point center) {
        if (gp.monster[FinalArena.MAP][FinalArena.SUMMON_SLOT]!=null || summon>=2) return;
        Entity enemy;
        if (summon==0) enemy=new MON_Picaron(gp);
        else {
            MON_Tella tella=new MON_Tella(gp);
            tella.configureAbyss(center.x-24,new Rectangle(24*48,28*48,10*48,10*48));
            enemy=tella;
        }
        enemy.worldX=center.x-24;
        enemy.worldY=center.y-24;
        gp.monster[FinalArena.MAP][FinalArena.SUMMON_SLOT]=enemy;
        summon++;
        gp.playSE(16);
    }

    /** A null weapon is the player's bubble. Returns true if a shot is consumed. */
    public boolean hit(Rectangle strike,int fromX,int fromY,Entity weapon,int attack) {
        if (!gp.bossBattleOn || life<=0) return false;
        if (!exposed) {
            if (phase==1) {
                if (strikeStone(strike,fromX,fromY,weapon,STONE_LEFT,STONE_L)
                    || strikeStone(strike,fromX,fromY,weapon,STONE_RIGHT,STONE_R)) {
                    if (flags==(STONE_L|STONE_R)) open();
                    return true;
                }
            } else if (phase==2 && strike.intersects(box(CORE,60,54))
                    && gp.finalArena.visibleFrom(fromX,fromY,CORE.x,CORE.y)) {
                if ((flags&GRILLE)==0) {
                    if (weapon instanceof OBJ_Iron) { flags|=GRILLE; feedback("The grille opens."); }
                    return true;
                }
                if (weapon==null) open();
                return true;
            } else if (phase==5 && step==Step.HOLD && plan!=null && isFist(plan)
                    && strike.intersects(box(plan.target,54,48))
                    && gp.finalArena.visibleFrom(fromX,fromY,plan.target.x,plan.target.y)) {
                int hand=plan.move==Move.FIST_L?LEFT_HAND:RIGHT_HAND;
                if ((flags&hand)==0) {
                    flags|=hand;
                    feedback(hand==LEFT_HAND?"Left fist marked.":"Right fist marked.");
                    if (flags==(LEFT_HAND|RIGHT_HAND)) open();
                }
                return true;
            }
            return strike.intersects(mantle());
        }
        Rectangle weak=phase==6?eye():mantle();
        Point target=center(weak);
        if (!strike.intersects(weak) || !gp.finalArena.visibleFrom(fromX,fromY,target.x,target.y)) return false;
        if (hitTicks>0) return true;
        int damage=Math.min(life-FLOORS[phase-1],Math.max(8,attack));
        life-=damage;
        hitTicks=18;
        gp.playSE(10);
        if (life==FLOORS[phase-1]) advancePhase();
        return true;
    }

    private boolean strikeStone(Rectangle strike,int fromX,int fromY,Entity weapon,Point stone,int mask) {
        if ((flags&mask)!=0 || !strike.intersects(box(stone,54,48))
                || !gp.finalArena.visibleFrom(fromX,fromY,stone.x,stone.y)) return false;
        if (weapon instanceof OBJ_Rock) { flags|=mask; feedback("An anchor breaks."); }
        return true;
    }

    private void feedback(String message) {
        gp.playSE(3);
        auraTicks=28;
        gp.ui.addMessage(message);
    }

    private void open() {
        exposed=true;
        plan=null;
        rest=phase==6?110:0;
        auraTicks=38;
        feedback(phase==6?"The eye is exposed.":"The barrier breaks.");
    }

    private void advancePhase() {
        gp.finalArena.clearHazards();
        plan=null;
        if (life==0) { gp.playSE(10); return; }
        if (phase==3) {
            worldX=FinalArena.BOSS_X;
            worldY=FinalArena.BOSS_Y;
        }
        phase++;
        flags=cycle=summon=0;
        exposed=lodged=false;
        auraTicks=48;
        rest=phase==5?105:84;
        gp.playSE(16);
    }

    private static boolean isFist(Plan p) { return p.move==Move.FIST_L || p.move==Move.FIST_R; }

    @Override public void checkDrop() { gp.finalArena.victory(); }

    private void sprite(Graphics2D g,String atlas,int col,int row,int x,int y) {
        GodFishArt.draw(g,atlas,col,row,x-gp.cameraX(),y-gp.cameraY());
    }
    private void effect(Graphics2D g,int col,int row,Point at) {
        sprite(g,"fx/marcas",col,row,at.x,at.y);
    }
    private void mechanism(Graphics2D g,int col,int row,Point at) {
        // Mechanisms are anchored at their base, with the target centered just above it.
        sprite(g,"escena/mecanismos",col,row,at.x,at.y+18);
    }

    @Override public void draw(Graphics2D graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        if (deathTicks>0) g.setComposite(java.awt.AlphaComposite.SrcOver.derive(Math.max(0f,1f-deathTicks/64f)));
        String art="boss/fase-"+phase;
        int frame=exposed?(hitTicks>0?3:2):(clock/38)%2, row=0;
        if (life==0) { art="boss/6-final"; frame=3; }
        else if (lodged) { art="boss/3-tridente-clavado"; frame=0; row=lastCharge.ordinal(); }
        else if (plan!=null) {
            art="boss/"+switch (plan.move) {
                case SWEEP -> phase+"-sweep";
                case VOLLEY -> "2-volley";
                case CHARGE -> "3-charge";
                case SUMMON -> "4-cast";
                case FIST_L -> phase+"-fist-l";
                case FIST_R -> phase+"-fist-r";
                case SLAM -> "6-slam";
                case WEAK -> "6-final";
            };
            frame=plan.move==Move.WEAK ? (step==Step.WARN?1:step==Step.IMPACT?2:0) : step.ordinal();
            if (plan.move!=Move.SUMMON && plan.move!=Move.SLAM && plan.move!=Move.WEAK) row=plan.aim.ordinal();
        } else if (phase==6 && exposed) { art="boss/6-final"; frame=0; }
        else if (phase==5 && rest>65 && cycle==0) { art="boss/4-desarme"; frame=rest>85?0:1; }
        sprite(g,art,frame,row,worldX,worldY);
        g.dispose();
    }

    public void drawFloor(Graphics2D graphics) {
        if ((!gp.bossBattleOn && !(gp.csManager.sceneNum==gp.csManager.GodFish && gp.csManager.scenePhase>=4))
                || life<=0) return;
        Graphics2D g=(Graphics2D)graphics.create();
        if (phase==1) {
            stone(g,STONE_LEFT,STONE_L);
            stone(g,STONE_RIGHT,STONE_R);
        } else if (phase==2) {
            mechanism(g,exposed?1:0,2,CORE);
            if ((flags&GRILLE)==0) mechanism(g,0,1,CORE);
            if (!exposed && gp.player.water==0) mechanism(g,1,3,VENT);
        } else if (phase==3) {
            mechanism(g,lodged?2:1,4,ALTAR);
            if (lodged) sprite(g,"escena/armas",0,lastCharge.ordinal(),ALTAR.x,ALTAR.y);
        } else if (phase==4) {
            Point target=flags==0?PICARON:FISHTELLA;
            if (!exposed) {
                mechanism(g,summon>flags?1:0,5,target);
                link(g,target);
            }
        } else if (phase>=5) {
            sprite(g,"escena/armas",1,0,17*48+24,26*48+24);
            sprite(g,"escena/armas",2,0,32*48+24,26*48+24);
            if (phase==6 && !exposed) {
                tint(g,LEFT_SAFE,new Color(75,212,210),true);
                tint(g,RIGHT_SAFE,new Color(75,212,210),true);
                mechanism(g,1,7,LEFT_REFUGE);
                mechanism(g,1,7,RIGHT_REFUGE);
            }
        }
        if (plan!=null && (step==Step.WARN || step==Step.IMPACT)) {
            Color danger=COLORS[phase-1];
            if (plan.move==Move.VOLLEY) {
                for (Ray ray:plan.rays)
                    if (ray.range>0) tint(g,box(ray.start,24,24)
                        .union(box(pointOn(ray.start,plan.aim,ray.range),24,24)),danger,step==Step.WARN);
            } else if (plan.danger!=null && (plan.move!=Move.SLAM || step==Step.WARN)) {
                tint(g,plan.danger,danger,step==Step.WARN);
                if (isFist(plan)) effect(g,step==Step.WARN?0:1,6,plan.target);
                if (plan.move==Move.CHARGE) effect(g,step==Step.WARN?0:1,11,plan.target);
            }
            if (plan.move==Move.SUMMON) effect(g,step==Step.WARN?0:1,step==Step.WARN?0:1,plan.target);
        }
        g.dispose();
    }

    private void stone(Graphics2D g,Point stone,int mask) {
        mechanism(g,(flags&mask)==0?1:2,0,stone);
        if ((flags&mask)==0) link(g,stone);
    }

    private void link(Graphics2D g,Point target) {
        g.setStroke(new BasicStroke(3));
        g.setColor(COLORS[phase-1]);
        g.drawLine(worldX-gp.cameraX(),worldY-gp.cameraY(),target.x-gp.cameraX(),target.y-gp.cameraY());
        effect(g,(clock/18)%2,2,target);
    }

    private void tint(Graphics2D g,Rectangle r,Color color,boolean warning) {
        if (r.isEmpty()) return;
        int x=r.x-gp.cameraX(), y=r.y-gp.cameraY();
        g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),warning?64:108));
        g.fillRect(x,y,r.width,r.height);
        g.setStroke(new BasicStroke(2));
        g.setColor(color);
        g.drawRect(x,y,r.width-1,r.height-1);
    }

    public void drawEffects(Graphics2D graphics) {
        if ((!gp.bossBattleOn && !(gp.csManager.sceneNum==gp.csManager.GodFish && gp.csManager.scenePhase>=4))
                || life<=0) return;
        Graphics2D g=(Graphics2D)graphics.create();
        if (!exposed || auraTicks>0) {
            int frame=exposed?(auraTicks>15?3:4):auraTicks>0?2:(clock/18)%2;
            sprite(g,"fx/aura",frame,phase-1,worldX,worldY);
        }
        if (phase==6 && exposed) {
            Rectangle eye=eye();
            g.setColor(COLORS[5]);
            g.drawOval(eye.x-gp.cameraX(),eye.y-gp.cameraY(),eye.width,eye.height);
        }
        if (plan!=null && step==Step.HOLD && phase==5 && isFist(plan)) {
            int hand=plan.move==Move.FIST_L?LEFT_HAND:RIGHT_HAND;
            Rectangle mark=box(plan.target,60,54);
            g.setColor((flags&hand)==0?new Color(255,215,158):new Color(149,255,215));
            g.setStroke(new BasicStroke(3));
            g.drawOval(mark.x-gp.cameraX(),mark.y-gp.cameraY(),mark.width,mark.height);
        }
        if (plan!=null && step==Step.IMPACT) {
            int t=elapsed-plan.warn-1;
            if (plan.move==Move.VOLLEY) {
                for (int i=0; i<plan.rays.size(); i++) {
                    Ray ray=plan.rays.get(i);
                    int distance=(t-i*11)*12;
                    if (distance>=0 && distance<=ray.range && ray.range>0) {
                        Point p=pointOn(ray.start,plan.aim,distance);
                        GodFishArt.volley(g,t%2,p.x-gp.cameraX(),p.y-gp.cameraY());
                    }
                }
            } else if (plan.move==Move.SLAM) {
                for (boolean south : new boolean[]{true,false}) {
                    Rectangle r=wave(t,south);
                    if (r.isEmpty()) continue;
                    tint(g,r,COLORS[5],false);
                    var clip=g.getClip();
                    g.clipRect(r.x-gp.cameraX(),r.y-gp.cameraY(),r.width,r.height);
                    for (int x=r.x+24;x<r.x+r.width;x+=48)
                        sprite(g,"fx/proyectil",1+t%2,2,x,r.y+r.height/2);
                    g.setClip(clip);
                }
            }
        }
        g.dispose();
    }

    public void drawStatus(Graphics2D graphics) {
        if (!gp.bossBattleOn) return;
        Graphics2D g=(Graphics2D)graphics.create();
        Color color=COLORS[phase-1];
        int x=240,y=538,width=480;
        g.setColor(new Color(11,18,30,239));
        g.fillRoundRect(220,493,520,71,12,12);
        g.setColor(new Color(41,51,68));
        g.fillRect(x,y,width,12);
        g.setColor(color);
        g.fillRect(x,y,width*life/maxLife,12);
        g.setColor(Color.BLACK);
        for (int floor:FLOORS) if (floor>0) g.fillRect(x+width*floor/maxLife,y,2,12);
        g.setFont(gp.ui.purisaB.deriveFont(17f));
        g.setColor(Color.WHITE);
        g.drawString("God Fish  /  " + phase + " of 6",x,514);
        g.setFont(gp.ui.purisaB.deriveFont(14f));
        g.setColor(color);
        g.drawString(hint(),x,532);
        g.dispose();
    }

    private String hint() {
        if (life==0) return "The trench falls silent";
        if (exposed) return phase==6?"Strike the exposed eye":"Barrier open";
        return switch (phase) {
            case 1 -> "Rocky: break both anchors";
            case 2 -> (flags&GRILLE)==0?"Iron: break the grille":gp.player.water==0
                ?"Dry? Swim over the lit vent":"Shoot the exposed core";
            case 3 -> "Bait the trident toward the altar, then move aside";
            case 4 -> flags==0?"Break the first summon link":"Break the second summon link";
            case 5 -> "Strike each planted fist   L " + ((flags&1)==0?"[ ]":"[x]") + "   R " + ((flags&2)==0?"[ ]":"[x]");
            case 6 -> plan!=null && plan.move==Move.SLAM?"Double strike! Blue side bands are safe":"Watch the fists and find a side refuge";
            default -> "";
        };
    }
}
