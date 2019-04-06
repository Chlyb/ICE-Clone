package com.mygdx.game.gameClasses;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public abstract class Entity implements Serializable {
    protected final GamePacket gp;
    protected Team team;
    protected Vector2 pos;
    protected float health;

    public Vector2 getPos() {
        return pos;
    }
    public float getHealth(){return health;}
    public abstract Vector2 getVel();

    public Entity(GamePacket gp, Team team, Vector2 pos) {
        this.gp = gp;
        this.team = team;
        this.pos = pos;
    }
}
