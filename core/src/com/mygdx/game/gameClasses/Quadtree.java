package com.mygdx.game.gameClasses;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.workers.Collider;
import com.mygdx.game.workers.Ranger;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class Quadtree {

    private final int x;
    private final int y;
    private final int w;
    private final int h;
    private List<Ship> ships;
    private List<Flag> flags;
    private Boolean divided;
    private final int depth;
    private Quadtree quad1;
    private Quadtree quad2;
    private Quadtree quad3;
    private Quadtree quad4;

    private final int Wdiv2;
    private final int Hdiv2;
    private final int Wdiv4;
    private final int Hdiv4;
    private final int depth_plus_1;

    private final int X_sub_Wdiv2_sub_25;
    private final int X_plus_Wdiv2_plus_25;
    private final int Y_sub_Hdiv2_sub_25;
    private final int Y_plus_Hdiv2_plus_25;

    private final int X_sub_Wdiv2_sub_10;
    private final int X_plus_Wdiv2_plus_10;
    private final int Y_sub_Hdiv2_sub_10;
    private final int Y_plus_Hdiv2_plus_10;

    public Quadtree(int x, int y, int w, int h, int depth) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        ships = new ArrayList<Ship>();
        flags = new ArrayList<Flag>();
        divided = false;
        this.depth = depth;

        Wdiv2 = w>>1;
        Hdiv2 = h>>1;
        Wdiv4 = Wdiv2>>1;
        Hdiv4 = Hdiv2>>1;
        depth_plus_1 = depth + 1;

        X_sub_Wdiv2_sub_25 = x - Wdiv2 - 25;
        X_plus_Wdiv2_plus_25 = x + Wdiv2 + 25;
        Y_sub_Hdiv2_sub_25 = y - Hdiv2 - 25;
        Y_plus_Hdiv2_plus_25 = y + Hdiv2 + 25;

        X_sub_Wdiv2_sub_10 = X_sub_Wdiv2_sub_25 + 15;
        X_plus_Wdiv2_plus_10 = X_plus_Wdiv2_plus_25 - 15;
        Y_sub_Hdiv2_sub_10 = Y_sub_Hdiv2_sub_25 + 15;
        Y_plus_Hdiv2_plus_10 = Y_plus_Hdiv2_plus_25 - 15;
    }

    private void subdivideColl(){
        divided = true;
        quad1 = new Quadtree( x + Wdiv4, y + Hdiv4, Wdiv2, Hdiv2, depth_plus_1);
        quad2 = new Quadtree( x - Wdiv4, y + Hdiv4, Wdiv2, Hdiv2, depth_plus_1);
        quad3 = new Quadtree( x - Wdiv4, y - Hdiv4, Wdiv2, Hdiv2, depth_plus_1);
        quad4 = new Quadtree( x + Wdiv4, y - Hdiv4, Wdiv2, Hdiv2, depth_plus_1);

        for(Ship ship : ships){
            quad1.addShipColl(ship);
            quad2.addShipColl(ship);
            quad3.addShipColl(ship);
            quad4.addShipColl(ship);
        }

        for(Flag flag : flags){
            quad1.addFlagColl(flag);
            quad2.addFlagColl(flag);
            quad3.addFlagColl(flag);
            quad4.addFlagColl(flag);
        }
    }

    public void addShipColl(Ship ship){
        //if(x - w/2 - 10 < ship.getPos().x && ship.getPos().x < x + w/2 + 10 && y - h/2 - 10 < ship.getPos().y && ship.getPos().y < y + h/2 + 10){
        if(X_sub_Wdiv2_sub_10 < ship.getPos().x && ship.getPos().x < X_plus_Wdiv2_plus_10 && Y_sub_Hdiv2_sub_10 < ship.getPos().y && ship.getPos().y < Y_plus_Hdiv2_plus_10){
            if(!divided){
                if(ships.size() < 4 || depth >= 5){
                    ships.add(ship);
                }
                else{
                    subdivideColl();

                    quad1.addShipColl(ship);
                    quad2.addShipColl(ship);
                    quad3.addShipColl(ship);
                    quad4.addShipColl(ship);
                }
            }
            else{

                quad1.addShipColl(ship);
                quad2.addShipColl(ship);
                quad3.addShipColl(ship);
                quad4.addShipColl(ship);
            }
        }
    }

    public void addFlagColl(Flag flag){
        if(x - Wdiv2 - 30 < flag.getPos().x && flag.getPos().x < x + Wdiv2 + 30 && y - Hdiv2 - 30 < flag.getPos().y && flag.getPos().y < y + Hdiv2 + 30){
            if(!divided) {
                flags.add(flag);
            }
            else{
                quad1.addFlagColl(flag);
                quad2.addFlagColl(flag);
                quad3.addFlagColl(flag);
                quad4.addFlagColl(flag);
            }
        }
    }

    public void collide(){
        if(divided){
            if(depth == 0){
                Thread t1 = new Thread(new Collider(quad1));
                Thread t2 = new Thread(new Collider(quad2));
                Thread t3 = new Thread(new Collider(quad3));
                Thread t4 = new Thread(new Collider(quad4));

                t1.start();
                t2.start();
                t3.start();
                t4.start();

                try {
                    t1.join();
                    t2.join();
                    t3.join();
                    t4.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else{
                quad1.collide();
                quad2.collide();
                quad3.collide();
                quad4.collide();
            }
        }
        else{
            for(int i = 0; i < ships.size(); ++i){
                float ship1x = ships.get(i).getPos().x;
                float ship1y = ships.get(i).getPos().y;
                for(int j = i + 1; j < ships.size(); ++j){
                    if(abs(ship1x - ships.get(j).getPos().x) < 20){
                        if(abs(ship1y - ships.get(j).getPos().y) < 20){
                            collideShip( ships.get(i), ships.get(j));
                        }
                    }
                }
            }

            for(Flag flag : flags){
                for(Ship ship : ships){
                    collideFlag( flag, ship);
                }
            }
        }
    }

    private void subdivideRange(){
        divided = true;
        quad1 = new Quadtree( x + Wdiv4, y + Hdiv4, Wdiv2, Hdiv2, depth_plus_1);
        quad2 = new Quadtree( x - Wdiv4, y + Hdiv4, Wdiv2, Hdiv2, depth_plus_1);
        quad3 = new Quadtree( x - Wdiv4, y - Hdiv4, Wdiv2, Hdiv2, depth_plus_1);
        quad4 = new Quadtree( x + Wdiv4, y - Hdiv4, Wdiv2, Hdiv2, depth_plus_1);

        for(Ship ship : ships){
            quad1.addShipRange(ship);
            quad2.addShipRange(ship);
            quad3.addShipRange(ship);
            quad4.addShipRange(ship);
        }

        for(Flag flag : flags){
            quad1.addFlagRange(flag);
            quad2.addFlagRange(flag);
            quad3.addFlagRange(flag);
            quad4.addFlagRange(flag);
        }
        //ships.clear();
        //flags.clear();
    }

    public void addShipRange(Ship ship){
        //if(x - w/2 - 25 < ship.getPos().x && ship.getPos().x < x + w/2 + 25 && y - h/2 - 25 < ship.getPos().y && ship.getPos().y < y + h/2 + 25){
        if(X_sub_Wdiv2_sub_25 < ship.getPos().x && ship.getPos().x < X_plus_Wdiv2_plus_25 && Y_sub_Hdiv2_sub_25 < ship.getPos().y && ship.getPos().y < Y_plus_Hdiv2_plus_25){
            if(!divided){
                if(ships.size() < 4 || depth >= 4){
                    ships.add(ship);
                }
                else{
                    subdivideRange();
                    quad1.addShipRange(ship);
                    quad2.addShipRange(ship);
                    quad3.addShipRange(ship);
                    quad4.addShipRange(ship);
                }
            }
            else{
                quad1.addShipRange(ship);
                quad2.addShipRange(ship);
                quad3.addShipRange(ship);
                quad4.addShipRange(ship);
            }
        }
    }

    public void addFlagRange(Flag flag){
        //if(x - w/2 - 20 < flag.getPos().x && flag.getPos().x < x + w/2 + 20 && y - h/2 - 20 < flag.getPos().y && flag.getPos().y < y + h/2 + 20){
        if(X_sub_Wdiv2_sub_25 < flag.getPos().x && flag.getPos().x < X_plus_Wdiv2_plus_25 && Y_sub_Hdiv2_sub_25 < flag.getPos().y && flag.getPos().y < Y_plus_Hdiv2_plus_25){
            if(!divided) {
                flags.add(flag);
            }
            else{
                quad1.addFlagRange(flag);
                quad2.addFlagRange(flag);
                quad3.addFlagRange(flag);
                quad4.addFlagRange(flag);
            }
        }
    }

    public void findTargets(){
        if(divided){
            //if(depth == 0 || depth == 1){
            if(depth==0){
                Thread t1 = new Thread(new Ranger(quad1));
                Thread t2 = new Thread(new Ranger(quad2));
                Thread t3 = new Thread(new Ranger(quad3));
                Thread t4 = new Thread(new Ranger(quad4));

                t1.start();
                t2.start();
                t3.start();
                t4.start();

                try {
                    t1.join();
                    t2.join();
                    t3.join();
                    t4.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else{
                quad1.findTargets();
                quad2.findTargets();
                quad3.findTargets();
                quad4.findTargets();
            }
        }
        else{
            for(int i = 0; i < ships.size(); ++i){
                float ship1x = ships.get(i).getPos().x;
                float ship1y = ships.get(i).getPos().y;
                Team ship1team = ships.get(i).getTeam();
                for(int j = i + 1; j < ships.size(); ++j){
                    if(ship1team != ships.get(j).getTeam()){
                        if(ships.get(i).getTarget() == null || ships.get(j).getTarget() == null){
                            if(abs(ship1x - ships.get(j).getPos().x) < 50){
                                if(abs(ship1y - ships.get(j).getPos().y) < 50){
                                    findTargetShip(ships.get(i), ships.get(j));
                                }
                            }
                        }
                    }
                }
            }
            for(Ship ship : ships){
                if(ship.getTarget() == null){
                    for(Flag flag : flags){
                        if(flag.getTeam() != ship.getTeam()){
                            findTargetFlag(flag, ship);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void collideShip(Ship ship1, Ship ship2){
        Vector2 axis = ship1.getPos().cpy();
        axis.sub(ship2.getPos());
        float distance = axis.len2();
        if(distance < 400){
            distance = (float)Math.sqrt(distance);
            axis.scl((20 - distance)/20/distance);
            //axis.scl(2*(20 - distance)/distance);
            ship1.addVel(axis);
            ship2.addVel(axis.scl(-1));
        }
    }

    private static void collideFlag(Flag flag, Ship ship) {
        Vector2 axis = ship.getPos().cpy();
        axis.sub(flag.getPos());
        float distance = axis.len();
        if (distance < 45) {
            if (ship.getObjective() != null && ship.getObjective().getTargetedFlag() == flag) {
                //axis.rotate(5);// its dt dependent, to rework
                axis.rotate( 50f * ship.gp.getTime() / 2f / (float)Math.PI / 37f * 360f);
                //axis.rotate( 360f * ship.team.getSpeed() * ship.team.getSpeed() * ship.gp.getTime() / 2f / (float)Math.PI / 37f);
                axis.nor();
                axis.scl(37);
                axis.add(flag.getPos());
                axis.sub(ship.getPos());
                axis.scl(2);
                ship.addVel(axis);
            } else {
                //final int k = 10;
                final int k = 1;
                final int d = 45;
                axis.nor();
                axis.scl((d - distance) / k);
                ship.addVel(axis);
            }
        }
    }

    private static void findTargetShip(Ship ship1, Ship ship2) {
        Vector2 dis = ship1.getPos().cpy();
        dis.sub(ship2.getPos());
        if (dis.len2() < 50 * 50) {
            if (ship1.getTarget() instanceof Ship) {
                ship2.setTarget(ship1);
            } else if (ship2.getTarget() instanceof Ship) {
                ship1.setTarget(ship2);
            } else {
                ship1.setTarget(ship2);
                ship2.setTarget(ship1);
            }
        }
    }

    private static void findTargetFlag(Flag flag, Ship ship) {
        if (ship.getTeam() != flag.getTeam()) {
            Vector2 v = ship.getPos().cpy();
            v.sub(flag.getPos());
            if (v.len2() < 55 * 55) ship.setTarget(flag);
        }
    }

    public void draw(ShapeRenderer sr){
        if(divided){
            quad1.draw(sr);
            quad2.draw(sr);
            quad3.draw(sr);
            quad4.draw(sr);
        }
        else{
            sr.rect(x - w/2, y - h/2, w, h);
        }
    }
}