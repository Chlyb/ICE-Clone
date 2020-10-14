package com.mygdx.game.gameClasses;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static java.lang.Math.abs;

public class Quadtree implements Serializable {

    private final int x;
    private final int y;
    private final int w;
    private final int h;
    transient private List<Ship> ships;
    transient private List<Flag> flags;
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

    private final int X_plus_10;
    private final int X_min_10;
    private final int Y_plus_10;
    private final int Y_min_10;

    private final int X_plus_25;
    private final int X_min_25;
    private final int Y_plus_25;
    private final int Y_min_25;

    public Quadtree(int x, int y, int w, int h, int depth) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        ships = new LinkedList<Ship>();
        flags = new LinkedList<Flag>();
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

        X_plus_10 = x + 10;
        X_min_10 = x - 10;
        Y_plus_10 = y + 10;
        Y_min_10 = y - 10;

        X_plus_25 = x + 25;
        X_min_25 = x - 25;
        Y_plus_25 = y + 25;
        Y_min_25 = y - 25;
    }

    private void subdivideColliderTree(){
        divided = true;
        quad1 = new Quadtree( x + Wdiv4, y + Hdiv4, Wdiv2, Hdiv2, depth_plus_1);
        quad2 = new Quadtree( x - Wdiv4, y + Hdiv4, Wdiv2, Hdiv2, depth_plus_1);
        quad3 = new Quadtree( x - Wdiv4, y - Hdiv4, Wdiv2, Hdiv2, depth_plus_1);
        quad4 = new Quadtree( x + Wdiv4, y - Hdiv4, Wdiv2, Hdiv2, depth_plus_1);

        for(Ship ship : ships){
            this.addShipColl2Divided(ship);
        }

        for(Flag flag : flags){
            quad1.addFlagColl(flag);
            quad2.addFlagColl(flag);
            quad3.addFlagColl(flag);
            quad4.addFlagColl(flag);
        }
    }

    public void addShipCollider2(Ship ship){
        if(!divided){
            if(ships.size() < 4 || depth >= 5){
                ships.add(ship);
            }
            else{
                ships.add(ship);
                subdivideColliderTree();
            }
            return;
        }

        if(ship.pos.x < X_min_10 ) {
            if(ship.pos.y < Y_min_10) {
                quad3.addShipCollider2(ship);
            }
            else if(ship.pos.y > Y_plus_10) {
                quad2.addShipCollider2(ship);
            }
            else {
                quad3.addShipCollider2(ship);
                quad2.addShipCollider2(ship);
            }
        }else if(ship.pos.x > X_plus_10){
            if(ship.pos.y < Y_min_10) {
                quad4.addShipCollider2(ship);
            }
            else if(ship.pos.y > Y_plus_10) {
                quad1.addShipCollider2(ship);
            }
            else {
                quad4.addShipCollider2(ship);
                quad1.addShipCollider2(ship);
            }
        }else {
            if(ship.pos.y < Y_min_10) {
                quad3.addShipCollider2(ship);
                quad4.addShipCollider2(ship);
            }
            else if(ship.pos.y > Y_plus_10) {
                quad2.addShipCollider2(ship);
                quad1.addShipCollider2(ship);
            }
            else {
                quad1.addShipCollider2(ship);
                quad2.addShipCollider2(ship);
                quad3.addShipCollider2(ship);
                quad4.addShipCollider2(ship);
            }
        }
    }

    public void addShipColl2Divided(Ship ship){
        if(ship.pos.x < X_min_10 ) {
            if(ship.pos.y < Y_min_10) {
                quad3.addShipCollider2(ship);
            }
            else if(ship.pos.y > Y_plus_10) {
                quad2.addShipCollider2(ship);
            }
            else {
                quad3.addShipCollider2(ship);
                quad2.addShipCollider2(ship);
            }
        }else if(ship.pos.x > X_plus_10){
            if(ship.pos.y < Y_min_10) {
                quad4.addShipCollider2(ship);
            }
            else if(ship.pos.y > Y_plus_10) {
                quad1.addShipCollider2(ship);
            }
            else {
                quad4.addShipCollider2(ship);
                quad1.addShipCollider2(ship);
            }
        }else {
            if(ship.pos.y < Y_min_10) {
                quad3.addShipCollider2(ship);
                quad4.addShipCollider2(ship);
            }
            else if(ship.pos.y > Y_plus_10) {
                quad2.addShipCollider2(ship);
                quad1.addShipCollider2(ship);
            }
            else {
                quad1.addShipCollider2(ship);
                quad2.addShipCollider2(ship);
                quad3.addShipCollider2(ship);
                quad4.addShipCollider2(ship);
            }
        }
    }

    public int cellCount(){
        if(!divided) return 1;
        return quad1.cellCount() + quad2.cellCount() + quad3.cellCount() + quad4.cellCount();
    }

    public int shipCount(){
        if(!divided) return ships.size();
        return quad1.shipCount() + quad2.shipCount() + quad3.shipCount() + quad4.shipCount();
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
            quad1.collide();
            quad2.collide();
            quad3.collide();
            quad4.collide();
        }
        else{

            /*
            for(int i = 0; i < ships.size(); ++i) {
                float ship1x = ships.get(i).getPos().x;
                float ship1y = ships.get(i).getPos().y;
                for (int j = i + 1; j < ships.size(); ++j) {
                    if (abs(ship1x - ships.get(j).getPos().x) < 20) {
                        if (abs(ship1y - ships.get(j).getPos().y) < 20) {
                            collideShip(ships.get(i), ships.get(j));
                        }
                    }
                }
            }
            */

            ListIterator<Ship> it = ships.listIterator();
            while(it.hasNext()) {
                Ship ship = it.next();
                float ship1x = ship.getPos().x;
                float ship1y = ship.getPos().y;
                ListIterator <Ship> it2 = ships.listIterator(it.nextIndex());
                while(it2.hasNext()) {
                    Ship ship2 = it2.next();
                    if (abs(ship1x - ship2.getPos().x) < 20) {
                        if (abs(ship1y - ship2.getPos().y) < 20) {
                            collideShip(ship, ship2);
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
            this.addShipRange2(ship);
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

    public void addShipRange2(Ship ship){
        if(!divided){
            if(ships.size() < 4 || depth >= 4){
                ships.add(ship);
            }
            else{
                ships.add(ship);
                subdivideRange();
            }
            return;
        }

        if(ship.pos.x < X_min_25 ) {
            if(ship.pos.y < Y_min_25) {
                quad3.addShipRange2(ship);
            }
            else if(ship.pos.y > Y_plus_25) {
                quad2.addShipRange2(ship);
            }
            else {
                quad3.addShipRange2(ship);
                quad2.addShipRange2(ship);
            }
        }else if(ship.pos.x > X_plus_25){
            if(ship.pos.y < Y_min_25) {
                quad4.addShipRange2(ship);
            }
            else if(ship.pos.y > Y_plus_25) {
                quad1.addShipRange2(ship);
            }
            else {
                quad4.addShipRange2(ship);
                quad1.addShipRange2(ship);
            }
        }else {
            if(ship.pos.y < Y_min_25) {
                quad3.addShipRange2(ship);
                quad4.addShipRange2(ship);
            }
            else if(ship.pos.y > Y_plus_25) {
                quad2.addShipRange2(ship);
                quad1.addShipRange2(ship);
            }
            else {
                quad1.addShipRange2(ship);
                quad2.addShipRange2(ship);
                quad3.addShipRange2(ship);
                quad4.addShipRange2(ship);
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
        if(divided) {
            quad1.findTargets();
            quad2.findTargets();
            quad3.findTargets();
            quad4.findTargets();
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
                axis.rotateRad( 50f * ship.gp.dt / 37f);
                axis.nor();
                axis.scl(37f);
                Vector2 impulse = axis.add(flag.getPos()).sub(ship.getPos());
                impulse.nor();
                impulse.scl(1f);
                ship.addVel(impulse);
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