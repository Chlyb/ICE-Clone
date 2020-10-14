package com.mygdx.game.workers;

import com.mygdx.game.gameClasses.Flag;
import com.mygdx.game.gameClasses.GamePacket;
import com.mygdx.game.gameClasses.Quadtree;
import com.mygdx.game.gameClasses.Ship;

public class CollisionUpdate implements Runnable {
    private final GamePacket gp;

    public CollisionUpdate ( GamePacket gp) {
        this.gp = gp;
    }

    public void run() {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        Quadtree collisionTree = new Quadtree(1920/2-100,1080/2-100,1920+200,1080+200,0);

        for(Ship ship : gp.getShips()){
            collisionTree.addShipCollider2(ship);
        }

        for(Flag flag : gp.getFlags()){
            collisionTree.addFlagColl(flag);
        }

        //gp.collisionTree = collisionTree;
        collisionTree.collide();
    }
}
