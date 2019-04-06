package com.mygdx.game.workers;
import com.mygdx.game.gameClasses.Quadtree;
import com.mygdx.game.gameClasses.Ship;

public class ShipCollAdder implements Runnable {
    public final Ship ship;
    public final Quadtree quad;

    public ShipCollAdder (Ship ship, Quadtree quad) {
        this.ship = ship;
        this.quad = quad;
    }

    public void run() {
        quad.addShipColl(ship);
    }
}
