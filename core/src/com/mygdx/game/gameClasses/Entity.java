package com.mygdx.game.gameClasses;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public abstract class Entity implements Serializable {
    transient protected final GamePacket gp;
    protected Team team;
    protected Vector2 pos;

    public Vector2 getPos() {
        return pos;
    }
    public abstract Vector2 getVel();
    abstract float getHealth();
    abstract void dealDamage(float damage);

    public Entity(GamePacket gp, Team team, Vector2 pos) {
        this.gp = gp;
        this.team = team;
        this.pos = pos;
    }

    Entity() {
        this.gp = null;
    }
}
