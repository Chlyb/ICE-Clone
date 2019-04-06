package com.mygdx.game.workers;

import com.mygdx.game.gameClasses.Flag;
import com.mygdx.game.gameClasses.GamePacket;
import com.mygdx.game.gameClasses.Quadtree;
import com.mygdx.game.gameClasses.Ship;

public class RangeUpdate implements Runnable {
    private final GamePacket gp;

    public RangeUpdate (GamePacket gp) {
        this.gp = gp;
    }

    public void run() {
        Quadtree rangeTree = new Quadtree(1920/2-100,1080/2-100,1920+200,1080+200,0);

        for(Ship ship : gp.getShips()){
            rangeTree.addShipRange(ship);
        }

        for(Flag flag : gp.getFlags()){
            rangeTree.addFlagRange(flag);
        }

        rangeTree.findTargets();
    }
}