package com.mygdx.game.gameClasses;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Ship extends Entity implements Serializable {
    private Vector2 vel;
    public float angle;
    private Entity target;
    private Objective objective;

    public Ship(GamePacket gp, Team team, Vector2 pos) {
        super(gp, team, pos);

        vel = new Vector2(0,0);
        angle = 0;
        health = team.getHealth();
        target = null;
        objective = team.giveObjective();

        gp.addShip(this);
        team.addShip(this);
    }

    public void shoot(float dt){
        target.health -= team.getDamage() * dt;
        if(target instanceof Flag){
            ((Flag)target).shotBy = this;
            if(target.health < 0) team.addScore(5);
        }
        else if(target.health < 0) team.addScore(1);
    }

    public void updateMoving(){
        if(target != null){
            if( target.health <= 0 || pos.cpy().sub(target.getPos()).len2() > 50*50) target = null;
        }

        vel.scl(0.9f);
        if(objective != null) {
            vel.scl(0.9f);
            Vector2 v1 = new Vector2(objective.getX(), objective.getY()).sub(pos);
            v1.scl(0.003f);
            if (v1.len2() > 1) v1.nor();

            if (target == null) angle = v1.angle() - 90;

            vel.add(v1);
        }
    }


    public void updateShooting(float dt){
        if(target != null){
            shoot(dt);
            angle = target.getPos().cpy().sub(pos).angle() - 90;
        }
    }

    public void move(float dt){
        if(vel.len2() > 1) vel.nor();
        Vector2 scaled = vel.cpy();
        scaled.scl( team.getSpeed() * dt);
        pos.add(scaled);
    }

    public void delete(){
        gp.removeShip(this);
        team.removeShip(this);
    }

    public Objective getObjective() {
        return objective;
    }
    public Team getTeam(){
        return team;
    }
    public Entity getTarget(){
        return target;
    }
    public float getAngle(){return angle;}
    public void setTarget(Entity target){
        this.target = target;
    }
    public void addVel(Vector2 vel){ this.vel.add(vel);}
    public Vector2 getVel(){return vel;}
    public void setObjective(Objective objective) {
        this.objective = objective;
    }
}
