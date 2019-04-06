package com.mygdx.game.workers;
import com.mygdx.game.gameClasses.Quadtree;

public class Ranger implements Runnable {
    public final Quadtree quad;

    public Ranger (Quadtree quad) {
        this.quad = quad;
    }

    public void run() {
        quad.findTargets();
    }
}
