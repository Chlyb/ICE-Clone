package com.mygdx.game.gameClasses;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Objective implements Serializable {
    public static final int RADIUS = 100;
    private final Team team;
    private final Vector2 pos;
    private Flag targetedFlag;

    public Objective(Team team, Vector2 pos) {
        this.team = team;
        this.targetedFlag = null;
        //System.out.println("NOWY OBJECTIVE");

        for(Flag flag : team.gp.getFlags()){
            Vector2 axis = pos.cpy();
            axis.sub(flag.getPos());
            if(axis.len2() <= 50*50){
                targetedFlag = flag;
                break;
            }
        }
        if(targetedFlag != null) this.pos = targetedFlag.pos;//Sooo it turns out that you cant change initialized variable but assigning null it not initializing
        else this.pos = pos;

        team.addObjective(this);
        //team.reassignObjectives();
    }

    public void delete(){
        team.removeObjective(this);
        //team.assignObjectives();
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
