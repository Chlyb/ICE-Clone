package com.mygdx.game.gameClasses;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Objective implements Serializable {
    public static final int RADIUS = 100;
    transient private final Team team;
    private final Vector2 pos;
    transient private Flag targetedFlag;

    public Objective(Team team, Vector2 pos) {
        this.team = team;
        this.targetedFlag = null;

        for(Flag flag : team.gp.getFlags()){
            Vector2 axis = pos.cpy();
            axis.sub(flag.getPos());
            if(axis.len2() <= 50*50){
                targetedFlag = flag;
                break;
            }
        }
        if(targetedFlag != null)
            this.pos = targetedFlag.pos;
        else
            this.pos = pos;

        team.addObjective(this);
        //team.reassignObjectives();
    }

    public void delete(){
        team.removeObjective(this);
    }

    public Flag getTargetedFlag() {
        return targetedFlag;
    }

    public float getX(){
        return pos.x;
    }

    public float getY(){
        return pos.y;
    }
}
