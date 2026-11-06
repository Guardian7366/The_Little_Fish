package monster;

import entity.Entity;
import entity.Projectile;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import main.FinalArena;
import main.GamePanel;
import main.GodFishArt;
import main.Temple;

/** Transient enemies owned by God Fish's fourth phase. */
public final class FinalSummon extends Entity {
    public enum Kind {
        AGUIJON("Aguijón","aguijon",45,3,7,0,36,33,85),
        BALUARTE("Baluarte","baluarte",55,5,9,2,34,8,96);

        final String label, art;
        final int hp, armor, damage, exposedArmor, warning, impact, recovery;
        Kind(String label,String art,int hp,int armor,int damage,int exposedArmor,int warning,int impact,int recovery) {
            this.label=label;
            this.art=art;
            this.hp=hp;
            this.armor=armor;
            this.damage=damage;
            this.exposedArmor=exposedArmor;
            this.warning=warning;
            this.impact=impact;
            this.recovery=recovery;
        }
    }

    private enum Facing {
        DOWN(0,1,"down",2), LEFT(-1,0,"left",3), RIGHT(1,0,"right",1), UP(0,-1,"up",0);

        final int dx,dy,quarterTurns;
        final String key;
        Facing(int dx,int dy,String key,int quarterTurns) {
            this.dx=dx;
            this.dy=dy;
            this.key=key;
            this.quarterTurns=quarterTurns;
        }

        static Facing from(String direction) {
            return switch (direction) {
                case "left" -> LEFT;
                case "right" -> RIGHT;
                case "up" -> UP;
                default -> DOWN;
            };
        }
    }

    private enum State { SWIM, WARN, IMPACT, RECOVER }
    private record Strike(Facing facing,List<Rectangle> zones,int advance) {}
    private final GamePanel panel;
    private final Kind kind;
    private Facing facing=Facing.DOWN;
    private State state=State.SWIM;
    private Strike strike;
    private int tick, rest=22, blockSound, swimFrame, hitZones;

    public FinalSummon(GamePanel gp,Kind kind) {
        super(gp);
        panel=gp;
        this.kind=kind;
        name=kind.label;
        type=type_monster;
        life=maxLife=kind.hp;
        defense=kind.armor;
        attack=kind.damage;
        knockBackPower=2;
        solidArea.setBounds(10,10,28,28);
        solidAreaDefaultX=solidArea.x;
        solidAreaDefaultY=solidArea.y;
    }

    public Kind kind() { return kind; }
    @Override public int getCenterX() { return worldX+24; }
    @Override public int getCenterY() { return worldY+24; }

    @Override public void update() {
        if (!panel.bossBattleOn || panel.currentMap!=FinalArena.MAP || !alive || dying) return;
        updateCombatTimers();
        if (blockSound>0) blockSound--;
        switch (state) {
            case SWIM -> swim();
            case WARN -> {
                if (++tick>=kind.warning) {
                    if (kind==Kind.BALUARTE && !advanceShield()) recover();
                    else {
                        state=State.IMPACT;
                        tick=0;
                        hitZones=0;
                        panel.playSE(9);
                        impact();
                    }
                }
            }
            case IMPACT -> {
                if (++tick>=kind.impact) recover();
                else impact();
            }
            case RECOVER -> {
                if (++tick>=kind.recovery) {
                    defense=kind.armor;
                    state=State.SWIM;
                    strike=null;
                    rest=15;
                }
            }
        }
    }

    private void swim() {
        swimFrame=0;
        Rectangle player=playerBody();
        int dx=(int)player.getCenterX()-getCenterX(),dy=(int)player.getCenterY()-getCenterY();
        if (rest>0) rest--;
        else {
            for (Facing aim:Facing.values()) {
                if (dx*aim.dx+dy*aim.dy<=0) continue;
                Strike candidate=kind==Kind.AGUIJON ? spikes(aim) : shield(aim);
                if (candidate!=null && candidate.zones.stream().anyMatch(player::intersects)) {
                    strike=candidate;
                    face(aim);
                    state=State.WARN;
                    tick=0;
                    return;
                }
            }
        }
        Facing horizontal=dx<0?Facing.LEFT:Facing.RIGHT;
        Facing vertical=dy<0?Facing.UP:Facing.DOWN;
        if (Math.abs(dx)>Math.abs(dy)) {
            if (!step(horizontal) && Math.abs(dy)>6) step(vertical);
        } else if (!step(vertical) && Math.abs(dx)>6) step(horizontal);
    }

    private boolean step(Facing aim) {
        face(aim);
        int x=worldX+2*aim.dx,y=worldY+2*aim.dy;
        if (!openBody(x,y)) return false;
        worldX=x;
        worldY=y;
        swimFrame=1+(spriteCounter++/8)%2;
        return true;
    }

    private boolean openBody(int x,int y) {
        var body=Temple.bounds(this,x,y);
        var titan=panel.finalArena.boss();
        return panel.finalArena.clear(body) && !body.intersects(playerBody())
            && (titan==null || !titan.protectedBody() || !body.intersects(Temple.bounds(titan,titan.worldX,titan.worldY)));
    }

    private Rectangle playerBody() { return Temple.bounds(panel.player,panel.player.worldX,panel.player.worldY); }

    private void face(Facing aim) {
        facing=aim;
        direction=aim.key;
    }

    private Strike spikes(Facing aim) {
        List<Rectangle> zones=new ArrayList<>(3);
        for (int distance=32;distance<=112;distance+=40) {
            int x=getCenterX()+aim.dx*distance,y=getCenterY()+aim.dy*distance;
            var zone=new Rectangle(x-16,y-16,32,32);
            if (!panel.finalArena.clear(zone) || !panel.finalArena.visibleFrom(getCenterX(),getCenterY(),x,y)) return null;
            zones.add(zone);
        }
        return new Strike(aim,List.copyOf(zones),0);
    }

    private Strike shield(Facing aim) {
        int advance=0;
        while (advance<12 && openBody(worldX+aim.dx*(advance+1),worldY+aim.dy*(advance+1))) advance++;
        for (;advance>=0;advance--) {
            int x=getCenterX()+aim.dx*(advance+35),y=getCenterY()+aim.dy*(advance+35);
            var zone=new Rectangle(x-20,y-20,40,40);
            if (panel.finalArena.clear(zone) && panel.finalArena.visibleFrom(getCenterX(),getCenterY(),x,y))
                return new Strike(aim,List.of(zone),advance);
        }
        return null;
    }

    private boolean advanceShield() {
        Facing aim=strike.facing;
        for (int n=1;n<=strike.advance;n++)
            if (!openBody(worldX+aim.dx*n,worldY+aim.dy*n)) return false;
        worldX+=aim.dx*strike.advance;
        worldY+=aim.dy*strike.advance;
        return true;
    }

    private void impact() {
        int index=kind==Kind.AGUIJON ? tick/11 : 0;
        if (kind==Kind.AGUIJON && tick%11>=8) return;
        int mask=1<<index;
        if ((hitZones&mask)==0 && strike.zones.get(index).intersects(playerBody()) && !panel.player.invincible) {
            hitZones|=mask;
            if (!panel.keyH.godModeOn) damagePlayer(attack,strike.facing.key,false);
        }
    }

    private void recover() {
        state=State.RECOVER;
        tick=0;
        defense=kind.exposedArmor;
    }

    /** Uses the orientation drawn by the raised shield, including during a committed attack. */
    public boolean blocks(Entity attacker) {
        if (kind!=Kind.BALUARTE || state==State.RECOVER || dying) return false;
        Facing incoming=Facing.from(attacker.direction);
        boolean frontal;
        if (attacker instanceof Projectile) {
            frontal=incoming.dx*facing.dx+incoming.dy*facing.dy<0;
        } else {
            int dx=attacker.worldX+24-incoming.dx*attacker.attackArea.width-getCenterX();
            int dy=attacker.worldY+24-incoming.dy*attacker.attackArea.height-getCenterY();
            int ahead=dx*facing.dx+dy*facing.dy;
            frontal=ahead>0 && ahead>=Math.abs(dx*facing.dy-dy*facing.dx);
        }
        if (frontal && blockSound==0) {
            panel.playSE(18);
            blockSound=12;
        }
        return frontal;
    }

    @Override public void draw(Graphics2D g) {
        int frame=dying?7:switch (state) {
            case SWIM -> swimFrame;
            case WARN -> 3;
            case IMPACT -> 4;
            case RECOVER -> tick<kind.recovery/2?5:6;
        };
        GodFishArt.draw(g,"esbirros/"+kind.art,frame,facing.ordinal(),getScreenX()+24,getScreenY()+24);
        if (dying || strike==null) return;
        for (int i=0;i<strike.zones.size();i++) {
            int effect=-1;
            if (state==State.WARN) effect=(tick/7)%2;
            else if (state==State.IMPACT) {
                int age=kind==Kind.AGUIJON?tick-i*11:tick;
                if (age<0) effect=(tick/7)%2;
                else if (age<8) effect=2+Math.min(1,age/4);
                else if (age<11) effect=4;
            }
            if (effect>=0) drawZone(g,strike.zones.get(i),effect);
        }
    }

    private void drawZone(Graphics2D graphics,Rectangle zone,int effect) {
        var g=(Graphics2D)graphics.create();
        int x=(int)zone.getCenterX()-panel.cameraX(),y=(int)zone.getCenterY()-panel.cameraY();
        g.setColor(effect<2 ? new Color(254,194,117,155) : new Color(255,235,167,195));
        g.drawRect(zone.x-panel.cameraX(),zone.y-panel.cameraY(),zone.width-1,zone.height-1);
        if (kind==Kind.BALUARTE) g.rotate(strike.facing.quarterTurns*Math.PI/2,x,y);
        GodFishArt.draw(g,"esbirros/efectos",effect,kind==Kind.AGUIJON?0:1,x,y);
        g.dispose();
    }
}
