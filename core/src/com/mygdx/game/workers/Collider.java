package com.mygdx.game.workers;
import com.mygdx.game.gameClasses.Quadtree;

public class Collider implements Runnable {
    public final Quadtree quad;

    public Collider (Quadtree quad) {
        this.quad = quad;
    }

    public void run() {
        quad.collide();
    }
}
