package com.mygdx.game.gameClasses;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Flag extends Entity implements Serializable {
    private List<Flag> linkedFlags;
    public Ship shotBy; //to rework

    public float angle;
    private float cooldown;

    public Flag(GamePacket gp, Team team, Vector2 pos) {
        super(gp, team, pos);
        cooldown = 0;
        health = 1000;
        linkedFlags = new ArrayList<Flag>();
        shotBy = null;
        team.addFlag(this);
        gp.addFlag(this);

        angle = (int) (Math.random() * 360);
    }
    public void update(){
        if(cooldown <= 0){
            if(team.spawnShip()){
                Vector2 v = new Vector2(0, 60);
                float angle = (float) Math.random() * 360;
                v.rotate(angle);
                Ship ship = new Ship(gp, team, v.add(pos));
                ship.angle = angle - 180;
                cooldown = 1.5f;
            }
        }
    }

    public void move(float dt){
        if(health < 1000){
            if(health <= 0){
                changeTeam(shotBy.getTeam());
            }
            else health += 5 * dt;
        }
        angle += dt;
        cooldown -= dt;
    }

    private void changeTeam(Team newTeam){
        Team prevTeam = team;
        team.removeFlag(this);
        team = newTeam;
        team.addFlag(this);
        prevTeam.updateLinkedFlags();
        team.updateLinkedFlags();
        health = 500;

        team.removeTarget(this);
    }

    public static void generateFlags(GamePacket gp, int width, int height, int flagCount){
        int teamCount = gp.getTeams().size();
        width -= 100;
        height -= 100;
        int j = 0;
        while(j < flagCount){
            float x0 = (float) Math.random() * width;
            float y0 = (float) Math.random() * height;
            if(correctPossition(gp, x0, y0, flagCount)){
                new Flag(gp, gp.getNeutralTeam(), new Vector2(x0, y0));
                ++j;
            }
        }

        for(Flag flag1: gp.getFlags()){
            for(Flag flag2 : flag1.nearbyFlags( 5 * width / flagCount)){
                new Link(gp, flag1, flag2);
            }
        }

        --flagCount;

        for(Team team: gp.getTeams()){
            if(team != gp.getNeutralTeam()){
                int i = (int) (Math.random()*(flagCount + 1));
                while( gp.getFlags().get(i).getTeam() != gp.getNeutralTeam()){
                    i = (int) (Math.random()*(flagCount + 1));
                }
                gp.getFlags().get(i).changeTeam(team);
                gp.getFlags().get(i).setHealth(1000);
            }
        }
    }

    public static Boolean correctPossition(GamePacket gp, float x, float y, int n){
        for(Flag flag : gp.getFlags()){
            //if( Math.pow(x - flag.getPos().x,2) + Math.pow(y - flag.getPos().y,2) < 200*200) return false;
            //System.out.println( Math.sqrt( Game.WIDTH*Game.HEIGHT / n / n * 7 ));
            //if( Math.pow(x - flag.getPos().x,2) + Math.pow(y - flag.getPos().y,2) < 1920*1080 / n / n * 7) return false;
            if( Math.pow(x - flag.getPos().x,2) + Math.pow(y - flag.getPos().y,2) < 1920*1080 / n / Math.log(n) * 1) return false;
        }
        return true;
    }

    public List<Flag> nearbyFlags(float maxDis){
        List<Tuple> tuples = new ArrayList<Tuple>();
        for(Flag flag : gp.getFlags()){
            float dis = flag.getPos().cpy().sub(getPos()).len();
            if(dis <= maxDis && flag != this){
                tuples.add(new Tuple(dis, flag));
            }
        }

        Collections.sort(tuples, new Comparator<Tuple>() {
            @Override
            public int compare(Tuple tuple, Tuple t1) {
                if(tuple.dis < t1.dis) return -1;
                if(tuple.dis == t1.dis) return 0;
                return 1;
            }
        });
        List<Flag> flags = new ArrayList<Flag>();

        int nof = tuples.size();
        int sumDis = 0;
        for(int i = 0; i < nof; ++i){
            sumDis += tuples.get(i).getDis();
        }
        float lim = sumDis / maxDis; //#Even though I came up with it I don't understand how it works

        for(int i = 0; i < lim; ++i){
            flags.add(tuples.get(i).flag);
        }
        return flags;
    }

    class Tuple{
        final float dis;
        final Flag flag;
        public Tuple(float dis, Flag flag){
            this.dis = dis;
            this.flag = flag;
        }
        public float getDis(){
            return dis;
        }

    }

    public void addLinkedFlag(Flag flag){
        linkedFlags.add(flag);
    }

    public List<Flag> getLinkedFlags() {
        return linkedFlags;
    }
    public Team getTeam() {
        return team;
    }
    public float getCooldown(){return cooldown;}
    public void setHealth(int health){this.health = health;}
    public Vector2 getVel(){return Vector2.Zero;}
}
