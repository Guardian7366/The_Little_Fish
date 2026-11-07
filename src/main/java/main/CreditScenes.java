package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import tile.TileManager;

/** Camera crops and actor tracks use world tiles. They never reference live map state. */
final class CreditScenes {
    static final int WIDTH=576, HEIGHT=360;
    private static final int TILE=48;
    private static final Color WARNING=new Color(251,190,98,155), IMPACT=new Color(226,246,252,200);
    private static final String[] IDS={"primera-zona","chicken","flower","spiky","picaron","pump","ghost",
        "hongui","sharky","handy","ic","planta","tv","god-fish"};
    private record Crop(int map,double x,double y,double width,String background) {}
    private static final Crop[] CROPS={
        new Crop(0,16,26,13,null),
        room(9,35,13,"chicken"), room(27.5,35,13,"flower"), room(27.5,25,13,"spiky"),
        new Crop(2,19,34,13,null),
        room(9,24,13,"pump"), room(29,14,12,"ghost"), room(9.5,14,13,"hongui"),
        new Crop(3,17,36.5,13,"/quinta/background/far.png"),
        room(28,5,13,"handy"), room(9,5,13,"ic"),
        new Crop(4,18.5,36.3,14,"/septima/background/deep-naves.png"),
        new Crop(10,17,20,13,"/mercado/background/market.png"),
        new Crop(6,15,22,19,"/final/fosa/background/far-trench.png")
    };

    private record Pose(double x,double y,String direction,boolean moving) {}
    private final BufferedImage[] backgrounds=new BufferedImage[IDS.length];

    CreditScenes(TileManager tiles) {
        int[][][] originals=new int[11][][];
        for (int i=0;i<CROPS.length;i++) {
            Crop crop=CROPS[i];
            if (originals[crop.map()]==null) originals[crop.map()]=tiles.originalTerrain(crop.map());
            backgrounds[i]=backdrop(tiles,originals[crop.map()],crop);
        }
    }

    private static Crop room(double x,double y,double width,String name) {
        return new Crop(ExtraRooms.MAP,x,y,width,"/refugios/"+name+"/background.png");
    }

    static void checkId(int index,String id) {
        if (!IDS[index].equals(id)) throw new IllegalArgumentException("Unexpected credits scene: "+id);
    }

    private static void nearest(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

    private static Graphics2D world(Graphics2D g,Crop crop) {
        var copy=(Graphics2D)g.create();
        nearest(copy);
        double scale=WIDTH/(crop.width()*TILE);
        copy.scale(scale,scale);
        copy.translate(-crop.x()*TILE,-crop.y()*TILE);
        return copy;
    }

    private static BufferedImage backdrop(TileManager tiles,int[][] terrain,Crop crop) {
        var image=new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
        var base=image.createGraphics();
        nearest(base);
        base.setColor(new Color(13,27,46));
        base.fillRect(0,0,WIDTH,HEIGHT);
        if (crop.background()!=null) base.drawImage(CreditAssets.art(crop.background()),0,0,WIDTH,HEIGHT,null);
        var g=world(base,crop);
        int right=(int)Math.ceil(crop.x()+crop.width());
        int bottom=(int)Math.ceil(crop.y()+crop.width()*HEIGHT/WIDTH);
        for (int x=(int)crop.x();x<right;x++) for (int y=(int)crop.y();y<bottom;y++) {
            int id=terrain[x][y];
            if (id==0 && crop.background()!=null) continue;
            if (id==144) g.drawImage(tiles.tile[126+(x+y/2)%2].image,x*TILE,y*TILE,null);
            if (id==FinalRefuges.LATCH_INTACT)
                g.drawImage(tiles.tile[152+(x+y/2)%2].image,x*TILE,y*TILE,null);
            g.drawImage(tiles.tile[id].image,x*TILE,y*TILE,null);
        }
        g.dispose();
        base.dispose();
        return image;
    }

    void draw(Graphics2D graphics,int index,int ms) {
        var canvas=(Graphics2D)graphics.create();
        canvas.clipRect(0,0,WIDTH,HEIGHT);
        canvas.drawImage(backgrounds[index],0,0,null);
        var g=world(canvas,CROPS[index]);
        switch (index) {
            case 0 -> reef(g,ms);
            case 1 -> chicken(g,ms);
            case 2 -> flower(g,ms);
            case 3 -> spiky(g,ms);
            case 4 -> picaron(g,ms,false);
            case 5 -> pump(g,ms);
            case 6 -> ghost(g,ms);
            case 7 -> picaron(g,ms,true);
            case 8 -> sharky(g,ms);
            case 9 -> handy(g,ms);
            case 10 -> ic(g,ms);
            case 11 -> planta(g,ms);
            case 12 -> tv(g,ms);
            case 13 -> godFish(g,ms);
            default -> throw new IllegalArgumentException("Scene "+index);
        }
        g.dispose();
        canvas.dispose();
    }

    /** Triples are time in ms and the actor center in world tiles. A repeated position is a hold. */
    private static Pose track(int t,double... keys) {
        String direction="right";
        for (int i=3;i<keys.length;i+=3) {
            double x=keys[i-2],y=keys[i-1],dx=keys[i+1]-x,dy=keys[i+2]-y;
            boolean moving=dx!=0 || dy!=0;
            if (moving) direction=Math.abs(dx)>=Math.abs(dy)?(dx>0?"right":"left"):(dy>0?"down":"up");
            if (t<keys[i]) {
                double p=Math.max(0,(t-keys[i-3])/(keys[i]-keys[i-3]));
                return new Pose(x+p*dx,y+p*dy,direction,moving);
            }
        }
        return new Pose(keys[keys.length-2],keys[keys.length-1],direction,false);
    }

    private static int px(double tiles) { return (int)Math.round(tiles*TILE); }
    private static boolean second(int t) { return t/165%2!=0; }

    private static void prop(Graphics2D g,String path,double x,double y,int width,int height) {
        var image=CreditAssets.art("/"+path+".png");
        g.drawImage(image,px(x),px(y),width,height,null);
    }

    private static void nativeProp(Graphics2D g,String path,double x,double y) {
        var image=CreditAssets.art("/"+path+".png");
        g.drawImage(image,px(x),px(y),image.getWidth()*3,image.getHeight()*3,null);
    }

    private static void sprite(Graphics2D g,String path,double x,double y) {
        prop(g,path,x-.5,y-.5,48,48);
    }

    private static void host(Graphics2D g,ExtraRooms.Room room,int t) {
        prop(g,"refugios/"+room.art+"/npc-"+(t/440%2),room.npcX,room.npcY,48,48);
    }

    private static void fish(Graphics2D g,Pose p,int t) {
        boolean mouth=p.moving() && second(t);
        String file=switch (p.direction()) {
            case "left" -> mouth?"BocaIzquierdaNormal":"HaciaIquierda";
            case "up" -> mouth?"BocaArribaNormal":"HaciaArriba";
            case "down" -> mouth?"BocaAbajoNormal":"HaciaAbajo";
            default -> mouth?"BocaDerechaNormal":"HaciaDerecha";
        };
        sprite(g,"player/"+file,p.x(),p.y());
    }

    private static void face(Graphics2D g,double x,double y,String direction,int t) {
        fish(g,new Pose(x,y,direction,false),t);
    }

    private static void greeny(Graphics2D g,Pose p,int t) {
        String name=switch (p.direction()) {
            case "up" -> "NormalilloUp";
            case "down" -> "NormalilloDown";
            case "right" -> "NormalilloRight";
            default -> "Normalillobase";
        };
        sprite(g,"monster/"+name+(second(t)?"2":""),p.x(),p.y());
    }

    private static void glow(Graphics2D g,double x,double y,int radius,Color color) {
        int cx=px(x),cy=px(y);
        var copy=(Graphics2D)g.create();
        copy.setPaint(new RadialGradientPaint(cx,cy,radius,new float[]{0,1},new Color[]{
            color,new Color(color.getRed(),color.getGreen(),color.getBlue(),0)}));
        copy.fillOval(cx-radius,cy-radius,radius*2,radius*2);
        copy.dispose();
    }

    private static void lane(Graphics2D g,double x,double y,double width,double height,boolean impact,int t) {
        var copy=(Graphics2D)g.create();
        Color c=impact?IMPACT:WARNING;
        copy.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),impact?100:42));
        copy.fillRect(px(x),px(y),px(width),px(height));
        copy.setColor(c);
        copy.setStroke(new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,1,new float[]{10,7},t/22%17));
        copy.drawRect(px(x),px(y),px(width),px(height));
        copy.dispose();
    }

    private static void spark(Graphics2D g,double x,double y,int ms) {
        int spread=6+ms/10;
        g.setColor(new Color(255,235,160,Math.max(0,230-ms)));
        for (int i=0;i<6;i++) {
            double angle=i*Math.PI/3;
            int xx=px(x)+(int)(Math.cos(angle)*spread),yy=px(y)+(int)(Math.sin(angle)*spread);
            g.fillRect(xx-2,yy-2,5,5);
        }
    }

    private static void reef(Graphics2D g,int t) {
        glow(g,21.6,29.5,235,new Color(102,218,187,44));
        greeny(g,track(t,0,22.5,31.5,1550,24.6,31.5,1780,24.6,31.5,
            4050,21.7,31.5,4320,21.7,31.5,6460,24.3,31.5),t);
        fish(g,track(t,0,17.5,30.5,700,17.5,30.5,4900,24.7,30.5,5600,24.7,29.6),t);
    }

    private static void chicken(Graphics2D g,int t) {
        prop(g,"refugios/chicken/table-two",13,38,96,48);
        nativeProp(g,"refugios/chicken/jar",10,36);
        nativeProp(g,"refugios/chicken/basket",21,36);
        if (t>=1550) glow(g,12.5,40.4,82,new Color(255,174,71,115));
        nativeProp(g,"refugios/chicken/lantern-"+(t>=1550?"on":"off"),12,40);
        if (t>=4850) glow(g,16.5,36.4,82,new Color(255,174,71,115));
        nativeProp(g,"refugios/chicken/lantern-"+(t>=4850?"on":"off"),16,36);
        host(g,ExtraRooms.Room.CHICKEN,t);
        fish(g,track(t,0,10.3,40.5,600,10.3,40.5,2030,13.4,40.5,3250,15.8,40.5,
            4400,16.5,38.1,5400,16.5,36.5,6200,16.5,36.5,7100,17.4,37.2),t);
    }

    private static void flower(Graphics2D g,int t) {
        prop(g,"refugios/flower/remedy-shelf",28,35,96,48);
        nativeProp(g,"refugios/flower/pot-0",32,35);
        nativeProp(g,"refugios/flower/pot-1",37,41);
        nativeProp(g,"refugios/flower/pot-0",39,35);
        host(g,ExtraRooms.Room.FLOWER,t);
        greeny(g,track(t,0,35.5,39.5,1900,37.2,39.5,2250,37.2,39.5,
            4900,34.7,39.5,5200,34.7,39.5,7450,36.9,39.5),t);
        fish(g,track(t,0,38.8,42.6,2250,34.4,42.6,2970,33.5,41.5,4750,33.5,38.3,
            6600,37.3,38.3),t);
    }

    private static void spiky(Graphics2D g,int t) {
        host(g,ExtraRooms.Room.SPIKY,t);
        nativeProp(g,"refugios/spiky/banner",39,24);
        prop(g,"refugios/spiky/cot",32,33,96,48);
        if (t>=1640 && t<2470) lane(g,29.2,30.28,5.18,.44,false,t);
        sprite(g,"monster/"+(second(t)?"EnfadilloMove":"Endadillo"),34.5,30.5);
        if (t>=2470 && t<3640) {
            double x=34.18-4.94*(t-2470)/1170.0;
            sprite(g,"proyectile/enemyPro1",x,30.5);
            glow(g,x,30.5,29,new Color(151,203,239,100));
        }
        if (t>=3640 && t<3860) spark(g,29.24,30.5,t-3640);
        fish(g,track(t,0,31.55,30.5,1250,32.25,30.5,1800,32.25,30.5,
            2450,32.25,29.55,3700,32.25,29.55,5700,35.5,29.55,6680,35.5,31.55),t);
    }

    private static void picaron(Graphics2D g,int t,boolean block) {
        double ex,ey;
        String direction=block?"Left":"Up";
        if (block) {
            host(g,ExtraRooms.Room.HONGUI,t);
            prop(g,"refugios/hongui/shelter",10,16,96,48);
            nativeProp(g,"refugios/hongui/glow-mushroom",14,14);
            nativeProp(g,"refugios/hongui/guard-mark-"+(t>=3150?1:0),20,14);
            var enemy=track(t,0,17.2,17.5,1770,15.1,17.5,4400,15.1,17.5,5750,16.4,17.5);
            ex=enemy.x(); ey=enemy.y();
        } else {
            var enemy=track(t,0,24.5,39.5,1770,24.5,37.8,4300,24.5,37.8,5800,24.5,38.8);
            ex=enemy.x(); ey=enemy.y();
            glow(g,24.5,37.7,220,new Color(106,213,212,47));
        }
        if (t>=1770 && t<3590) {
            if (block) lane(g,13.65,17.18,1.04,.64,t>=3150,t);
            else lane(g,24.15,36.36,.7,1.02,t>=3150,t);
        }
        if (t>=2430 && t<3840) {
            boolean contact=t>=3150 && t<3560;
            prop(g,"monster/PicaronA"+direction+(contact?2:1),ex-(block?1.5:.5),ey-(block?.5:1.5),
                block?96:48,block?48:96);
        } else {
            String walk=t>4400?(block?"Right":"Down"):direction;
            if (walk.equals("Up")) walk="Left"; // Up uses the original three-quarter left walk art.
            sprite(g,"monster/Picaron"+walk+(second(t)?"2":""),ex,ey);
        }
        if (block && t>=2550 && t<3940) {
            sprite(g,"player/NormalRight",13.82,17.5);
            if (t>=3150 && t<3360) spark(g,14.23,17.5,t-3150);
        } else if (block) fish(g,track(t,0,13.25,17.5,1380,13.82,17.5,4050,13.82,17.5,
            5200,13.6,18.5,6200,13.6,18.5),t);
        else fish(g,track(t,0,24.5,35.65,1360,24.5,36.62,1900,24.5,36.62,2950,26.3,36.62,
            3950,26.3,36.62,5450,26.8,35.55,6900,27.5,35.55),t);
    }

    private static void pump(Graphics2D g,int t) {
        host(g,ExtraRooms.Room.PUMP,t);
        nativeProp(g,"refugios/pump/planter",15,28);
        nativeProp(g,"refugios/pump/planter",16,29);
        nativeProp(g,"refugios/pump/seed-jar",10,24);
        if (t>=3650) {
            nativeProp(g,"refugios/pump/floor-0",14,27);
            nativeProp(g,"refugios/pump/binding-cut",14,27);
        }
        if (t>=3200 && t<4080) {
            prop(g,"player/ataqueRight"+(t<3650?1:2),12.89,27,96,48);
            if (t>=3650 && t<3860) spark(g,14.45,27.5,t-3650);
        } else fish(g,track(t,0,10.5,29.5,1700,13.39,29.5,2960,13.39,27.5,
            4300,13.39,27.5,6100,15.5,27.5),t);
    }

    private static void ghost(Graphics2D g,int t) {
        host(g,ExtraRooms.Room.GHOST,t);
        nativeProp(g,"refugios/ghost/echo-pedestal-"+(t>=1860?"1":"0"),37,18);
        for (int order=0;order<RefugeChallenges.SEQUENCE.length;order++) {
            int i=RefugeChallenges.SEQUENCE[order];
            int[] stone=RefugeChallenges.STONES[i];
            boolean lit=t>=2300+order*930;
            if (lit) glow(g,stone[0]+.5,stone[1]+.5,58,new Color(114,213,255,115));
            nativeProp(g,"refugios/ghost/memory-"+i+"-"+(lit?1:0),stone[0],stone[1]);
        }
        fish(g,track(t,0,39.6,19.5,1000,38.2,19.5,1920,37.5,19.5,2400,37.5,19.5,
            4200,35.5,18.1,5700,34.5,17.2,7100,34.5,17.2),t);
    }

    private static void sharky(Graphics2D g,int t) {
        double x=t<3240?25.35:track(t,3240,25.35,41.5,4050,19.45,41.5).x();
        if (t>=1760 && t<4050) lane(g,19.45,41.08,5.9,.84,t>=3240,t);
        glow(g,22.5,41.6,250,new Color(87,211,216,67));
        sprite(g,"monster/sharky"+(t>=5700?"Right":"Left")+(second(t)?"2":""),x,41.5);
        if (t>=1760 && t<5660) {
            String phase=t<3240?"prepare-left":t<4050?"charge-left":"stunned";
            prop(g,"quinta/sharky/"+phase+"-"+(second(t)?1:0),x-1,40.5,96,96);
        }
        if (t>=5700) {
            // Idle and recovery keep the same body anchor where the charge stopped.
            glow(g,19.45,41.5,36,new Color(84,168,212,43));
        }
        fish(g,track(t,0,20.6,41.5,1300,21.8,41.5,2080,21.8,41.5,
            3110,21.8,43.0,4300,21.8,43.0,6450,25.1,43.0),t);
    }

    private static void handy(Graphics2D g,int t) {
        host(g,ExtraRooms.Room.HANDY,t);
        nativeProp(g,"refugios/handy/tool-rack",39,5);
        nativeProp(g,"refugios/handy/anvil",39,12);
        nativeProp(g,"refugios/handy/chain",30,10);
        nativeProp(g,"refugios/handy/sconce",36,10);
        glow(g,36.5,10.5,95,new Color(251,174,105,72));
        if (t>=5250) {
            nativeProp(g,"refugios/handy/floor-0",37,11);
            nativeProp(g,"refugios/handy/stone-latch-broken",37,11);
        }
        double x=38.5,y=11.5;
        if (t>=2780 && t<3690 || t>=4780 && t<5710) {
            int impact=t<4000?3240:5250;
            prop(g,"player/rockLeft"+(t<impact?1:2),x-1.5,y-.5,96,48);
            if (t>=impact && t<impact+210) spark(g,37.52,y,t-impact);
        } else if (t>=3690 && t<4780) face(g,x,y,"left",t);
        else fish(g,track(t,0,38.5,8.7,2370,x,y,5900,x,y,7110,37.05,y),t);
    }

    private static void ic(Graphics2D g,int t) {
        host(g,ExtraRooms.Room.IC,t);
        for (int x=12;x<=18;x+=3) nativeProp(g,"refugios/ic/icicles",x,5);
        if (t>=2470) {
            glow(g,13.5,6.5,62,new Color(134,238,254,102));
            nativeProp(g,"refugios/ic/mark-0-1",13,6);
        }
        var p=track(t,0,11.5,8.5,1550,11.5,6.5,1800,11.5,6.5,
            3210,15.55,6.5,4980,15.55,6.5,6470,15.55,8.5,8290,14.5,9.55);
        if (t>=1810 && t<3210) {
            g.setColor(new Color(212,251,255,154));
            for (int i=0;i<3;i++) g.fillRect(px(p.x())-24-i*9,px(p.y())+13+i*3,6,3);
        }
        fish(g,p,t);
    }

    private static void planta(Graphics2D g,int t) {
        prop(g,"septima/murals/procession",22,35,144,96);
        glow(g,24.55,40.6,198,new Color(243,195,102,92));
        double x=24.5,y=39.5;
        if (t>=1630 && t<3180) lane(g,24.22,39.75,.56,3.48,false,t);
        if (t>=3180 && t<4910) {
            double span=t<3650?3.73*(t-3180)/470.0:t<4030?3.73:3.73*(4910-t)/880.0;
            for (double step=.45;step<span-.24;step+=.39) sprite(g,"septima/flower/vine-mid-down",x,y+step);
            if (span>.12) sprite(g,"septima/flower/vine-tip-down",x,y+span);
        }
        String pose=t<1630 || t>=4910?"idle-"+(second(t)?1:0):t<3180?"aim-down":t<4030?"aim-down":"retract";
        sprite(g,"septima/flower/"+pose,x,y);
        fish(g,track(t,0,24.5,44.2,1280,24.5,42.5,2110,24.5,42.5,3030,26.45,42.5,
            4800,26.45,42.5,6520,26.6,40.8),t);
    }

    private static void tv(Graphics2D g,int t) {
        nativeProp(g,"mercado/props/canopy",21,21);
        nativeProp(g,"mercado/props/shelf",18,22);
        nativeProp(g,"mercado/props/crate",19,24);
        nativeProp(g,"mercado/props/shelf",27,22);
        nativeProp(g,"mercado/props/lamp",23,20);
        nativeProp(g,"mercado/props/shop-sign",19,25);
        boolean lit=t>=3580 && (t>=3960 || t/70%2==0);
        if (lit) glow(g,22.5,23.6,84,new Color(115,238,225,105));
        nativeProp(g,"mercado/tv/idle-"+(lit?"lit":"dim"),22,23);
        nativeProp(g,"mercado/props/counter",21,24);
        fish(g,track(t,0,25.5,27.25,2200,22.5,27.25,3550,22.5,25.55),t);
    }

    private static void godFish(Graphics2D g,int t) {
        nativeProp(g,"final/fosa/props/broken-column",16,20);
        nativeProp(g,"final/fosa/props/broken-column",33,20);
        double x=FinalArena.BOSS_X/(double)TILE,y=FinalArena.BOSS_Y/(double)TILE;
        if (t>=1100 && t<3170) lane(g,x-1.25,y+44.0/TILE,2.5,350.0/TILE,t>=2800,t);
        if (t>=1100 && t<3900) {
            int frame=t<2400?0:t<2800?1:t<3170?2:3;
            GodFishArt.draw(g,"boss/1-sweep",frame,0,px(x),px(y));
        } else GodFishArt.draw(g,"boss/fase-1",t>=4750?2:t/650%2,0,px(x),px(y));
        fish(g,track(t,0,25,32.4,1100,25,33.15,1640,25,33.15,2720,27.9,33.15,
            3800,27.9,33.15,4730,28.7,32.8,5840,28.7,32.8),t);
        if (t>=4700) GodFishArt.eye(g,px(x),px(y));
    }
}
