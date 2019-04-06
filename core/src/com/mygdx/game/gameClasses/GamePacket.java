package com.mygdx.game.gameClasses;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.mygdx.game.workers.CollisionUpdate;
import com.mygdx.game.workers.RangeUpdate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class GamePacket implements Serializable{
    public static final Color[] colors = new Color[]{new Color(1, 1, 1, 1), new Color(0, 1, 0, 1),new Color(0,1,1,1),new Color(0.5f,0,0.5f,1),new Color(1,0.3f,0,1),new Color(1,0,0,1)};
    public static final Color[] darkColors = new Color[]{new Color(0.3f, 0.3f, 0.3f, 1), new Color(0, 0.3f, 0, 0.3f),new Color(0,0.3f,0.3f,1),new Color(0.25f,0,0.25f,1),new Color(0.3f,0.05f,0,1),new Color(0.3f,0,0,1)};

    private List<Team> teams;
    private List<Ship> ships;
    private List<Flag> flags;
    private List<Link> links;
    private Team neutralTeam;
    private float time;

    public GamePacket()
    {
        teams = new ArrayList<Team>();
        ships = new ArrayList<Ship>();
        flags = new ArrayList<Flag>();
        links = new ArrayList<Link>();

        neutralTeam = new Team(this, 0);
        neutralTeam.setHealth(3);
        time = 0;
    }

    public void update( AtomicLong physicsTime) {
        List<Ship> shipsToDeletion = new ArrayList<Ship>();

        for(Ship ship : ships){
            if(ship.getHealth() <= 0) shipsToDeletion.add(ship);
            else{
                ship.updateMoving();
            }
        }

        for(Ship ship : shipsToDeletion){
            ship.delete(); //because you cant modify list while iterating through it
        }

        for(Flag flag : flags){
            flag.update();
        }

        Thread t1 = new Thread(new CollisionUpdate(this));
        Thread t2 = new Thread(new RangeUpdate(this));

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        float dt = ( System.currentTimeMillis() - physicsTime.get())/1000.0f;
        physicsTime.set( System.currentTimeMillis());

        for(Flag flag : flags){
            flag.move(dt);
        }

        for(Ship ship : ships){
            ship.move(dt);
            ship.updateShooting(dt);
        }

        this.time = dt;
    }

    public void render(float delta, OrthographicCamera cam, ShapeRenderer sr, SpriteBatch sb, BitmapFont bf, int playerTeamIndex, short shootOffset){
        if(teams.size() > playerTeamIndex) {
            Team playerTeam = teams.get(playerTeamIndex);

            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            sr.setProjectionMatrix(cam.combined);
            sr.begin(ShapeRenderer.ShapeType.Line);

            for (Ship ship : getShips()) {
                ++shootOffset;
                shootOffset %= 4;

                if (ship.getTarget() != null && shootOffset == 0) {
                    sr.line(ship.getPos().x + ship.getVel().x * 50 * delta, ship.getPos().y + ship.getVel().y * 50 * delta, ship.getTarget().getPos().x + ship.getTarget().getVel().x * 50 * delta, ship.getTarget().getPos().y + ship.getTarget().getVel().y * delta, colors[ship.getTeam().color], colors[ship.getTeam().color]);
                }
            }

            //sr.setColor(0.3f, 0.3f, 0.3f, 1);
            for (Link link : getLinks()) {
                //if(link.flag1.getTeam() != playerTeam && link.flag2.getTeam() != playerTeam) sr.line(link.flag1.getPos(), link.flag2.getPos());
                if (link.flag1.getTeam() != playerTeam && link.flag2.getTeam() != playerTeam)
                    sr.line(link.flag1.getPos().x, link.flag1.getPos().y, link.flag2.getPos().x, link.flag2.getPos().y, darkColors[link.flag1.getTeam().color], darkColors[link.flag2.getTeam().color]);
                else {
                    sr.line(link.flag1.getPos().x, link.flag1.getPos().y, link.flag2.getPos().x, link.flag2.getPos().y, darkColors[playerTeam.color], darkColors[playerTeam.color]);
                }
            }

            sr.setColor(colors[playerTeam.color]);
            for (Objective objective : playerTeam.getObjectives()) {
                sr.circle(objective.getX(), objective.getY(), 32, 20);
            }

            Gdx.gl.glLineWidth(2 / cam.zoom);
            sr.end();
            Gdx.gl.glLineWidth(1);
            sr.begin(ShapeRenderer.ShapeType.Filled);

            for (Flag flag : getFlags()) {
                float x = flag.getPos().x;
                float y = flag.getPos().y;

                float sin = 20 * (float) Math.sin((flag.angle + delta)); //radius sin
                float cos = 20 * (float) Math.cos((flag.angle + delta)); //radius cos
                float sin2 = 1.1f * sin; //side sin
                float cos2 = 1.1f * cos; //side cos
                Color color = colors[flag.getTeam().color];

                sr.rectLine(x + sin2 + cos, y + sin - cos2, x - sin2 + cos, y + sin + cos2, 5, color, color);
                sr.rectLine(x + sin + cos, y + sin - cos, x + sin - cos, y - sin - cos, 5, color, color);
                sr.rectLine(x - sin2 - cos, y - sin + cos2, x + sin2 - cos, y - sin - cos2, 5, color, color);
                sr.rectLine(x - sin - cos, y - sin + cos, x - sin + cos, y + sin + cos, 5, color, color);
            }

            for (Ship ship : getShips()) {
                float x = ship.getPos().x + ship.getVel().x * 50 * delta;
                float y = ship.getPos().y + ship.getVel().y * 50 * delta;
                float sin = (float) Math.sin(Math.toRadians(-ship.getAngle()));
                float cos = (float) Math.cos(Math.toRadians(-ship.getAngle()));
                Color color = colors[ship.getTeam().color];
                sr.triangle(x + 10 * sin, y + 10 * cos, x - 4 * cos, y + 4 * sin, x + 4 * cos, y - 4 * sin, color, color, color);
            }

            sr.end();
            sb.setProjectionMatrix(cam.combined);
            sb.begin();

            for (Flag flag : getFlags()) {
                GlyphLayout glyphLayout = new GlyphLayout();
                glyphLayout.setText(bf, Integer.toString( (int)flag.getHealth()));
                bf.draw(sb, glyphLayout, flag.getPos().x - glyphLayout.width / 2, flag.getPos().y - glyphLayout.height / 2);
            }
            sb.end();
        }
    }

    public byte[] getBytes(){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public static GamePacket getObject( byte[] bytes){
        GamePacket gp = null;
        ByteArrayInputStream in = new ByteArrayInputStream( bytes,0, bytes.length);
        try {
            ObjectInputStream is = new ObjectInputStream(in);
            gp = (GamePacket) is.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(gp == null) return new GamePacket();
        return gp;
    }

    public GamePacket clone(){
        return getObject( getBytes());
    }

    public void goOneTickBack(){
        for(Ship ship : ships){
            ship.move( -time);
        }

        for(Flag flag : getFlags()){
            flag.move( -time);
        }
    }

    public void addTeam(Team team){
        teams.add(team);
    }
    public void addFlag(Flag flag) { flags.add(flag); }
    public void addShip(Ship ship){
        ships.add(ship);
    }
    public void removeShip(Ship ship){
        ships.remove(ship);
    }
    public void addLink(Link link){
        links.add(link);
    }
    public float getTime(){return time;}
    public Team getNeutralTeam(){ return neutralTeam;}
    public List<Ship> getShips(){ return ships;}
    public List<Link> getLinks(){ return links;}
    public List<Flag> getFlags(){ return flags;}
    public List<Team> getTeams(){ return teams;}
}
