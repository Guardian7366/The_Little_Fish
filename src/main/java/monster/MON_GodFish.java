package monster;

import entity.Entity;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;
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
    private static final Point[] STONES = {cell(19,33),cell(30,33)};
    private static final Point[] IRON = {
        new Point(FinalArena.BOSS_X-240,27*48+24), new Point(FinalArena.BOSS_X+240,27*48+24),
        new Point(FinalArena.BOSS_X,33*48+24)
    };
    private static final Point CORE = new Point(FinalArena.BOSS_X,22*48+24), VENT = cell(31,35);
    private static final Point[] ALTARS = {cell(18,23),cell(31,23),cell(29,35)};
    private static final Point[] SEALS = {cell(19,34),cell(19,24),cell(30,24),cell(30,34)};
    private static final Point HOME = new Point(FinalArena.BOSS_X,FinalArena.BOSS_Y);
    private static final Rectangle ARENA = new Rectangle(16*48,19*48,18*48,19*48);
    private static final int ANCHOR_HITS=3, FIST_HITS=10, EYE_WINDOW=300;
    private static final Point LEFT_REFUGE = cell(17,33), RIGHT_REFUGE = cell(32,33);
    private static final Rectangle WAVE_LANE = new Rectangle(19*48,19*48,12*48,19*48);
    private static final Rectangle LEFT_SAFE = new Rectangle(16*48,19*48,3*48,19*48);
    private static final Rectangle RIGHT_SAFE = new Rectangle(31*48,19*48,3*48,19*48);
    private static final int[][] LEFT_KNUCKLE={{-21,52},{-62,23},{56,27},{-38,-37}};
    private static final int[][] RIGHT_KNUCKLE={{24,52},{-56,27},{62,23},{37,-37}};
    private enum Facing {
        DOWN(0,1,"down"), LEFT(-1,0,"left"), RIGHT(1,0,"right"), UP(0,-1,"up");
        final int dx,dy;
        final String direction;
        Facing(int dx,int dy,String direction) { this.dx=dx; this.dy=dy; this.direction=direction; }
    }
    private enum Move { SWEEP, VOLLEY, CHARGE, SUMMON, FIST_L, FIST_R, SLAM, WEAK }
    private enum Step { WARN, IMPACT, HOLD, RECOVER }
    private record Ray(Point start, int range) {}
    private record Track(List<Point> positions, List<Rectangle> contacts, Area warning, int altar) {}
    private record Plan(Move move, Facing aim, Rectangle danger, Point target, List<Ray> rays,
                        int warn, int impact, int hold, int recover, Track track) {}
    private final GamePanel gp;
    private int phase=1, flags, elapsed, rest, cycle, clock, traveled, summon, hitTicks, auraTicks, deathTicks;
    private final int[] stoneHits=new int[2], fistHits=new int[2];
    private int approachTicks, pushCooldown, returnTicks, eyeTicks, swimDistance, altarIndex;
    private long usedSwing=Long.MIN_VALUE;
    private boolean exposed, impacted, lodged, swimming, returning, bodyReforming;
    private Facing swimFacing=Facing.DOWN;
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
        dialogues[0][0] = "There you are, D.";
        dialogues[0][1] = "You gave the little fish one task. Then\nyou gave it everything you remembered.";
        dialogues[0][2] = "You kept swimming down, as if peace\nwere something you could win from me.";
        dialogues[0][3] = "Come closer, D. Show me what\nyou came here for.";
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
        swimming=false;
        if (hitTicks>0) hitTicks--;
        if (auraTicks>0) auraTicks--;
        if (pushCooldown>0) pushCooldown--;
        if (life==0) {
            if (++deathTicks>=64) alive=false;
            return;
        }
        if (returning) {
            if (returnTicks>0) returnTicks--;
            if (returnTicks==0 && !bodyAt(HOME.x,HOME.y).intersects(playerBody())) finishReturn();
            return;
        }
        if (exposed && phase==6 && --eyeTicks==0) {
            exposed=false;
            bodyReforming=bodyAt(worldX,worldY).intersects(playerBody());
            plan=null;
            cycle=0;
            rest=38;
            approachTicks=0;
        }
        if (phase==2 && !exposed && gp.player.water==0 && closeTo(VENT,32)) {
            gp.player.water=Math.min(2,gp.player.maxWater);
            if (gp.player.water>0) gp.ui.addMessage("The vent restores water.");
        }
        if (phase==4 && summon>flags && gp.monster[FinalArena.MAP][FinalArena.SUMMON_SLOT]==null) {
            summon=flags;
            plan=null;
            rest=24;
        }
        if (bodyReforming && !bodyAt(worldX,worldY).intersects(playerBody())) bodyReforming=false;
        mantleContact();
        if (plan!=null) tickPlan();
        else if (rest>0) rest--;
        else if (exposed) {
            if (phase==6) choosePlan();
        } else if (shouldApproach() && approach()) {
            approachTicks++;
        } else choosePlan();
    }

    private void mantleContact() {
        var player=gp.player;
        if (!protectedBody() || player.invincible || player.life<=0 || gp.keyH.godModeOn) return;
        Rectangle core=box(new Point(worldX,worldY),40,40);
        if (!core.intersects(playerBody())) return;

        player.life--;
        player.invincible=true;
        player.invincibleCounter=0;
        player.transparent=true;
        gp.playSE(8);
        if (gp.finalArena.pushPlayer(core,playerX()-worldX,playerY()-worldY)) pushCooldown=12;
    }

    public boolean protectedBody() {
        return gp.bossBattleOn && !sleep && !exposed && !returning && !bodyReforming && life>0;
    }

    public boolean holdsCamera() { return plan!=null && step==Step.IMPACT; }

    public int cameraFocusY() {
        if (returning) return HOME.y;
        Entity minion=gp.monster[FinalArena.MAP][FinalArena.SUMMON_SLOT];
        if (phase==4 && minion!=null && Math.abs(minion.worldY-gp.player.worldY)<380)
            return minion.worldY+24;
        if (plan!=null && plan.target!=null) return plan.target.y;
        return worldY;
    }

    public void summonDefeated(Entity enemy) {
        if (phase!=4 || !protectedBody() || !(enemy instanceof FinalSummon)
                || gp.monster[FinalArena.MAP][FinalArena.SUMMON_SLOT]!=enemy
                || enemy.life>0 || !enemy.dying || enemy.alive || summon!=flags+1) return;
        flags++;
        auraTicks=36;
        rest=Math.max(rest,32);
        if (flags==SEALS.length) open();
    }

    private boolean shouldApproach() {
        return !(phase==4 && summon==flags && summon<SEALS.length)
            && !(phase==6 && cycle%3==2);
    }

    private boolean approach() {
        int dx=playerX()-worldX, dy=playerY()-worldY;
        int distance=Math.max(Math.abs(dx),Math.abs(dy));
        int preferred=phase==3?225:phase>=5?126:157;
        int altar=baitedAltar();
        if (phase==3 && altar>=0 && Math.hypot(worldX-ALTARS[altar].x,worldY-ALTARS[altar].y)<157) {
            dx=worldX-ALTARS[altar].x;
            dy=worldY-ALTARS[altar].y;
            if (dx==0 && dy==0) dy=-1;
            return approachTicks<82 && swim(dx,dy,3);
        }
        boolean flank=approachTicks<16 && cycle%2==1 && distance>preferred-30;
        if (flank && swim(-dy,dx,phase>=5?3:2)) return true;
        if (distance>preferred && approachTicks<170) return swim(dx,dy,phase>=5?3:2);
        if ((phase==1 || phase==2 || phase==4) && approachTicks<170) {
            if (Math.abs(dx)>=Math.abs(dy) && Math.abs(dy)>42) return swim(0,dy,2);
            if (Math.abs(dy)>Math.abs(dx) && Math.abs(dx)>42) return swim(dx,0,2);
        }
        return false;
    }

    private boolean swim(int dx,int dy,int speed) {
        double distance=Math.hypot(dx,dy);
        if (distance==0) return false;
        int x=(int)Math.round(speed*dx/distance),y=(int)Math.round(speed*dy/distance);
        if (stepSwim(x,y)) return true;
        if (x!=0 && y!=0 && (stepSwim(x,0) || stepSwim(0,y))) return true;
        return stepSwim(-Integer.signum(dy)*speed,Integer.signum(dx)*speed)
            || stepSwim(Integer.signum(dy)*speed,-Integer.signum(dx)*speed);
    }

    private boolean stepSwim(int dx,int dy) {
        int n=Math.max(Math.abs(dx),Math.abs(dy));
        for (int i=1;i<=n;i++)
            if (!canOccupy(worldX+dx*i/n,worldY+dy*i/n)) return false;
        if (n==0 || !moveProtected(worldX+dx,worldY+dy)) return false;
        swimFacing=facing(dx,dy);
        swimDistance=(swimDistance+Math.max(1,(int)Math.round(Math.hypot(dx,dy))))%40;
        swimming=true;
        return true;
    }

    private boolean canOccupy(int x,int y) {
        Rectangle body=bodyAt(x,y);
        if (!ARENA.contains(body) || !gp.finalArena.clear(body)) return false;
        if (phase==1) {
            for (int i=0;i<STONES.length;i++)
                if (stoneHits[i]<ANCHOR_HITS && body.intersects(box(STONES[i],40,34))) return false;
        } else if (phase==2) {
            for (int i=0;i<IRON.length;i++)
                if ((flags&(1<<i))==0 && body.intersects(box(IRON[i],40,34))) return false;
        } else if (phase==3) {
            for (int i=0;i<ALTARS.length;i++)
                if ((flags&(1<<i))==0 && body.intersects(box(ALTARS[i],42,36))) return false;
        }
        Entity minion=gp.monster[FinalArena.MAP][FinalArena.SUMMON_SLOT];
        return minion==null || !body.intersects(Temple.bounds(minion,minion.worldX,minion.worldY));
    }

    private boolean moveProtected(int x,int y) {
        Rectangle body=bodyAt(x,y);
        if (protectedBody() && body.intersects(playerBody())) {
            if (pushCooldown>0 || !gp.finalArena.pushPlayer(body,x-worldX,y-worldY)) return false;
            pushCooldown=12;
        }
        worldX=x;
        worldY=y;
        return true;
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
    private Rectangle eye() {
        int offset=24;
        if (plan!=null && plan.move==Move.WEAK) {
            if (step==Step.WARN) offset=-4;
            if (step==Step.IMPACT) offset=5;
        }
        return box(new Point(worldX,worldY+offset),54,44);
    }

    private static Facing facing(int dx,int dy) {
        return Math.abs(dx)>Math.abs(dy) ? (dx<0?Facing.LEFT:Facing.RIGHT) : (dy<0?Facing.UP:Facing.DOWN);
    }

    private void choosePlan() {
        Facing aim=facing(playerX()-worldX,playerY()-worldY);
        int index=cycle++;
        plan=switch (phase) {
            case 1 -> sweep(aim);
            case 2 -> volley(aim);
            case 3 -> charge();
            case 4 -> summon==flags && summon<SEALS.length ? summon(aim) : sweep(aim);
            case 5 -> fist(index%2==0);
            case 6 -> exposed ? new Plan(Move.WEAK,Facing.DOWN,box(new Point(worldX,worldY+82),120,62),
                    null,List.of(),46,10,10,32,null)
                : index%3==2 ? new Plan(Move.SLAM,aim,WAVE_LANE,null,List.of(),88,76,14,24,null)
                : fist(index%3==0);
            default -> throw new IllegalStateException("Invalid final phase");
        };
        elapsed=traveled=0;
        impacted=false;
        approachTicks=0;
        step=Step.WARN;
    }

    private Plan sweep(Facing aim) {
        Point tip=new Point(worldX+aim.dx*44,worldY+aim.dy*44);
        Rectangle lane=lane(tip,aim,350,120).intersection(ARENA);
        return new Plan(Move.SWEEP,aim,lane,center(lane),List.of(),54,12,12,22,null);
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
        return new Plan(Move.VOLLEY,aim,null,null,List.copyOf(rays),54,duration,10,24,null);
    }

    private int baitedAltar() {
        int best=-1;
        double distance=88*88;
        for (int i=0;i<ALTARS.length;i++) {
            double candidate=ALTARS[i].distanceSq(playerX(),playerY());
            if ((flags&(1<<i))==0 && candidate<distance) { best=i; distance=candidate; }
        }
        return best;
    }

    private Plan charge() {
        int altar=baitedAltar();
        Point target=altar<0?new Point(playerX(),playerY()):ALTARS[altar];
        int dx=target.x-worldX,dy=target.y-worldY;
        Facing aim=facing(dx,dy);
        double length=Math.max(1,Math.hypot(dx,dy));
        int tipX=(int)Math.round(132*dx/length),tipY=(int)Math.round(132*dy/length);
        if (dx==0 && dy==0) tipY=132;
        Track track=trace(dx,dy,490,tipX,tipY,64,58,true);
        return new Plan(Move.CHARGE,aim,track.warning.getBounds(),target,List.of(),
            62,track.positions.size(),18,26,track);
    }

    private Plan summon(Facing aim) {
        return new Plan(Move.SUMMON,aim,null,SEALS[summon],List.of(),65,1,14,21,null);
    }

    private Plan fist(boolean left) {
        Facing preferred=facing(playerX()-worldX,playerY()-worldY);
        Plan best=null;
        double bestDistance=Double.MAX_VALUE;
        for (Facing aim : Facing.values()) {
            int[] offset=(left?LEFT_KNUCKLE:RIGHT_KNUCKLE)[aim.ordinal()];
            int ox=offset[0]*3,oy=offset[1]*3;
            int dx=playerX()-worldX-ox,dy=playerY()-worldY-oy;
            Track track=trace(dx,dy,(int)Math.min(156,Math.hypot(dx,dy)),ox,oy,64,56,false);
            Point target=center(track.contacts.get(track.contacts.size()-1));
            double distance=target.distanceSq(playerX(),playerY())+(aim==preferred?0:72);
            if (distance<bestDistance) {
                bestDistance=distance;
                best=new Plan(left?Move.FIST_L:Move.FIST_R,aim,track.warning.getBounds(),target,List.of(),
                    phase==5?44:40,track.positions.size(),phase==5?140:16,18,track);
            }
        }
        return best;
    }

    private Track trace(int dx,int dy,int range,int offsetX,int offsetY,int w,int h,boolean charge) {
        double distance=Math.max(1,Math.hypot(dx,dy));
        Point start=new Point(worldX,worldY);
        Point end=start;
        List<Point> positions=new ArrayList<>();
        List<Rectangle> contacts=new ArrayList<>();
        Area warning=new Area();
        int altar=-1;
        for (int n=0;n<=range;n++) {
            int x=start.x+(int)Math.round(n*dx/distance);
            int y=start.y+(int)Math.round(n*dy/distance);
            Rectangle contact=box(new Point(x+offsetX,y+offsetY),w,h).intersection(ARENA);
            if (n>0 && (!canOccupy(x,y) || (charge && !ARENA.contains(x+offsetX,y+offsetY)))) break;
            end=new Point(x,y);
            if (charge) for (int i=0;i<ALTARS.length;i++)
                if ((flags&(1<<i))==0 && contact.intersects(box(ALTARS[i],49,41))) { altar=i; break; }
            if (n%9==0 || altar>=0) {
                positions.add(end);
                contacts.add(contact);
                warning.add(new Area(contact));
            }
            if (altar>=0) break;
        }
        if (!positions.get(positions.size()-1).equals(end)) {
            Rectangle contact=box(new Point(end.x+offsetX,end.y+offsetY),w,h).intersection(ARENA);
            positions.add(end);
            contacts.add(contact);
            warning.add(new Area(contact));
        }
        return new Track(List.copyOf(positions),List.copyOf(contacts),warning,altar);
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
            if (recovery+1>=active.recover) {
                plan=null;
                rest=phase==6 && exposed?62:phase>=5?14:23;
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
            case CHARGE,FIST_L,FIST_R -> executeTrack(p,tick);
            case SUMMON -> {
                if (tick==0) {
                    if (!spawn()) gp.ui.addMessage("Leave space near the summoning seal.");
                }
            }
            case SLAM -> {
                hurt(wave(tick,true),13,Facing.DOWN);
                hurt(wave(tick,false),13,Facing.UP);
            }
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

    private void executeTrack(Plan p,int tick) {
        if (traveled!=tick) return;
        Point next=p.track.positions.get(tick);
        if (!moveProtected(next.x,next.y)) return;
        traveled++;
        int damage=p.move==Move.CHARGE?11:phase==5?9:10;
        hurt(p.track.contacts.get(tick),damage,p.aim);
        if (p.move==Move.CHARGE && traveled==p.track.positions.size() && p.track.altar>=0) {
            altarIndex=p.track.altar;
            flags|=1<<altarIndex;
            lastCharge=p.aim;
            feedback("An altar breaks.");
            if (flags==7) {
                lodged=true;
                open();
            }
        }
    }

    private Point plantedHand(Plan p) {
        return center(p.track.contacts.get(Math.max(0,traveled-1)));
    }

    private void hurt(Rectangle bounds,int damage,Facing aim) {
        if (!impacted && bounds.intersects(playerBody())) {
            impacted=true;
            if (!gp.keyH.godModeOn) damagePlayer(damage,aim.direction,false);
        }
    }

    private boolean spawn() {
        if (gp.monster[FinalArena.MAP][FinalArena.SUMMON_SLOT]!=null || summon>=SEALS.length) return false;
        Point seal=SEALS[summon];
        int inward=summon<2?48:-48;
        int[][] offsets={{inward,0},{inward,-48},{inward,48},{-inward,0},
            {0,-96},{0,96},{2*inward,0},{-2*inward,0}};
        Point center=null;
        for (int[] offset:offsets) {
            Point candidate=new Point(seal.x+offset[0],seal.y+offset[1]);
            Rectangle entry=box(candidate,52,52);
            if (!entry.intersects(playerBody()) && !entry.intersects(bodyAt(worldX,worldY))
                    && gp.finalArena.clear(entry)) {
                center=candidate;
                break;
            }
        }
        if (center==null) return false;
        var kind=summon%2==0 ? FinalSummon.Kind.AGUIJON : FinalSummon.Kind.BALUARTE;
        Entity enemy=new FinalSummon(gp,kind);
        enemy.worldX=center.x-24;
        enemy.worldY=center.y-24;
        gp.monster[FinalArena.MAP][FinalArena.SUMMON_SLOT]=enemy;
        summon++;
        gp.playSE(16);
        return true;
    }

    /** A null weapon is the player's bubble. Returns true if a shot is consumed. */
    public boolean hit(Rectangle strike,int fromX,int fromY,Entity weapon,int attack) {
        return hit(strike,fromX,fromY,weapon,attack,Long.MIN_VALUE);
    }

    public boolean hit(Rectangle strike,int fromX,int fromY,Entity weapon,int attack,long swing) {
        if (!gp.bossBattleOn || life<=0 || returning) return false;
        if (!exposed) {
            if (phase==1) {
                for (int i=0;i<STONES.length;i++) if (stoneHits[i]<ANCHOR_HITS && touches(strike,fromX,fromY,STONES[i],54,48)) {
                    if (weapon instanceof OBJ_Rock && fresh(swing)) {
                        stoneHits[i]++;
                        feedback(stoneHits[i]==ANCHOR_HITS?"An anchor breaks.":"The anchor cracks.");
                        if (stoneHits[0]==ANCHOR_HITS && stoneHits[1]==ANCHOR_HITS) open();
                    }
                    return true;
                }
            } else if (phase==2) {
                for (int i=0;i<IRON.length;i++) if ((flags&(1<<i))==0 && touches(strike,fromX,fromY,IRON[i],56,48)) {
                    if (weapon instanceof OBJ_Iron && fresh(swing)) { flags|=1<<i; feedback("An iron block breaks."); }
                    return true;
                }
                if (flags==7 && weapon==null && touches(strike,fromX,fromY,CORE,58,52)) { open(); return true; }
            } else if (phase==5 && step==Step.HOLD && plan!=null && isFist(plan)
                    && touches(strike,fromX,fromY,plantedHand(plan),60,50)) {
                int hand=plan.move==Move.FIST_L?0:1;
                if (fistHits[hand]<FIST_HITS && fresh(swing)) {
                    fistHits[hand]++;
                    feedback(hand==0?"Left fist marked.":"Right fist marked.");
                    if (fistHits[0]==FIST_HITS && fistHits[1]==FIST_HITS) open();
                }
                return true;
            }
            return strike.intersects(mantle());
        }
        Rectangle weak=phase==6?eye():mantle();
        Point target=center(weak);
        if (!strike.intersects(weak) || !gp.finalArena.visibleFrom(fromX,fromY,target.x,target.y)) return false;
        if (hitTicks>0 || !fresh(swing)) return true;
        int damage=Math.min(life-FLOORS[phase-1],Math.max(8,attack));
        life-=damage;
        hitTicks=18;
        gp.playSE(10);
        if (life==FLOORS[phase-1]) advancePhase();
        return true;
    }

    private boolean fresh(long swing) {
        if (swing==Long.MIN_VALUE) return true;
        if (usedSwing==swing) return false;
        usedSwing=swing;
        return true;
    }

    private boolean touches(Rectangle strike,int fromX,int fromY,Point at,int width,int height) {
        return strike.intersects(box(at,width,height)) && gp.finalArena.visibleFrom(fromX,fromY,at.x,at.y);
    }

    private void feedback(String message) {
        gp.playSE(3);
        auraTicks=28;
        gp.ui.addMessage(message);
    }

    private void open() {
        exposed=true;
        plan=null;
        rest=phase==6?58:0;
        eyeTicks=phase==6?EYE_WINDOW:0;
        auraTicks=38;
        feedback(phase==6?"The eye is exposed.":"The barrier breaks.");
    }

    private void advancePhase() {
        gp.finalArena.clearHazards();
        plan=null;
        if (life==0) return;
        returning=true;
        returnTicks=60;
        gp.playSE(16);
    }

    private void finishReturn() {
        worldX=HOME.x;
        worldY=HOME.y;
        phase++;
        flags=cycle=summon=approachTicks=0;
        returning=exposed=lodged=false;
        auraTicks=48;
        rest=phase==5?85:46;
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
        else if (returning) { frame=2; }
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
        else if (swimming && !exposed) { art="boss/nado-"+phase; frame=swimDistance/10; row=swimFacing.ordinal(); }
        sprite(g,art,frame,row,worldX,worldY);
        g.dispose();
    }

    public void drawFloor(Graphics2D graphics) {
        if ((!gp.bossBattleOn && !(gp.csManager.sceneNum==gp.csManager.GodFish && gp.csManager.scenePhase>=4))
                || life<=0) return;
        Graphics2D g=(Graphics2D)graphics.create();
        if (returning) {
            tint(g,bodyAt(HOME.x,HOME.y),COLORS[phase-1],true);
            effect(g,(clock/12)%2,10,HOME);
            int y=HOME.y-gp.cameraY();
            if (y<42 || y>470) {
                int markerY=Math.max(42,Math.min(470,y));
                int sign=y<42?-1:1;
                int x=HOME.x-gp.cameraX();
                g.setColor(COLORS[phase-1]);
                g.fillPolygon(new int[]{x-10,x+10,x},new int[]{markerY-sign*7,markerY-sign*7,markerY+sign*7},3);
            }
            g.dispose();
            return;
        }
        if (phase==1) {
            for (int i=0;i<STONES.length;i++) {
                mechanism(g,stoneHits[i]==0?0:stoneHits[i]<ANCHOR_HITS?1:2,0,STONES[i]);
                if (stoneHits[i]<ANCHOR_HITS) link(g,STONES[i]);
            }
        } else if (phase==2) {
            for (int i=0;i<IRON.length;i++) mechanism(g,(flags&(1<<i))==0?0:2,1,IRON[i]);
            if (flags==7) mechanism(g,exposed?1:0,2,CORE);
            if (!exposed && gp.player.water==0) mechanism(g,1,3,VENT);
        } else if (phase==3) {
            for (int i=0;i<ALTARS.length;i++) mechanism(g,(flags&(1<<i))==0?1:2,4,ALTARS[i]);
            if (lodged) {
                Point altar=ALTARS[altarIndex];
                sprite(g,"escena/armas",0,lastCharge.ordinal(),altar.x,altar.y);
            }
        } else if (phase==4) {
            for (int i=0;i<SEALS.length;i++) {
                mechanism(g,i<flags?2:i==summon-1?1:0,5,SEALS[i]);
                if (i>=flags) link(g,SEALS[i]);
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
                if (plan.track==null) tint(g,plan.danger,danger,step==Step.WARN);
                else if (step==Step.WARN) tintTrack(g,plan.track.warning,danger);
                else if (traveled>0) tint(g,plan.track.contacts.get(traveled-1),danger,false);
                if (isFist(plan)) effect(g,step==Step.WARN?0:1,6,plan.target);
                if (plan.move==Move.CHARGE) effect(g,step==Step.WARN?0:1,11,plan.target);
            }
            if (plan.move==Move.SUMMON) effect(g,step==Step.WARN?0:1,step==Step.WARN?0:1,plan.target);
        }
        g.dispose();
    }

    private void tintTrack(Graphics2D g,Area warning,Color color) {
        var shape=AffineTransform.getTranslateInstance(-gp.cameraX(),-gp.cameraY()).createTransformedShape(warning);
        g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),64));
        g.fill(shape);
        g.setStroke(new BasicStroke(2));
        g.setColor(color);
        g.draw(shape);
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
            int hand=plan.move==Move.FIST_L?0:1;
            Rectangle mark=box(plantedHand(plan),60,54);
            g.setColor(fistHits[hand]<FIST_HITS?new Color(255,215,158):new Color(149,255,215));
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
            } else if (plan.move==Move.SWEEP) {
                Rectangle lane=plan.danger;
                var clip=g.getClip();
                g.clipRect(lane.x-gp.cameraX(),lane.y-gp.cameraY(),lane.width,lane.height);
                if (plan.aim.dx==0) {
                    for (int y=lane.y+20;y<lane.y+lane.height;y+=42)
                        effect(g,1,3,new Point((int)lane.getCenterX(),y));
                } else {
                    for (int x=lane.x+20;x<lane.x+lane.width;x+=42)
                        effect(g,1,3,new Point(x,(int)lane.getCenterY()));
                }
                g.setClip(clip);
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
        if (returning) return "God Fish returns to the marked center";
        if (exposed) return phase==6?"Strike the eye  /  "+((eyeTicks+59)/60)+" s":"Barrier open";
        return switch (phase) {
            case 1 -> "Rocky   L "+stoneHits[0]+"/3   R "+stoneHits[1]+"/3";
            case 2 -> flags!=7?"Iron blocks   "+Integer.bitCount(flags)+"/3":gp.player.water==0
                ?"Dry? Swim over the lit vent":"Shoot the exposed core";
            case 3 -> "Bait and dodge at each altar   "+Integer.bitCount(flags)+"/3";
            case 4 -> "Defeat the summons   "+flags+"/4";
            case 5 -> "Planted fists   L "+fistHits[0]+"/10   R "+fistHits[1]+"/10";
            case 6 -> plan!=null && plan.move==Move.SLAM?"Double strike! Blue side bands are safe":"Watch the fists and find a side refuge";
            default -> "";
        };
    }
}
