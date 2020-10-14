package com.mygdx.game.gameClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.CompressionUtils;
import com.mygdx.game.workers.CollisionUpdate;
import com.mygdx.game.workers.RangeUpdate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class GamePacket implements Serializable{
    public static final Color[] colors = new Color[]{new Color(1, 1, 1, 1), new Color(0, 1, 0, 1),new Color(0,1,1,1),new Color(0.5f,0,0.5f,1),new Color(1,0.3f,0,1),new Color(1,0,0,1)};
    public static final Color[] darkColors = new Color[]{new Color(0.3f, 0.3f, 0.3f, 1), new Color(0, 0.3f, 0, 0.3f),new Color(0,0.3f,0.3f,1),new Color(0.25f,0,0.25f,1),new Color(0.3f,0.05f,0,1),new Color(0.3f,0,0,1)};

    private List<Team> teams;
    private List<Ship> ships;
    private List<Flag> flags;
    private List<Link> links;
    private Team neutralTeam;

    transient float dt;
    //Quadtree collisionTree;
    transient ExecutorService exe;

    public GamePacket()
    {
        teams = new LinkedList<Team>();
        ships = new LinkedList<Ship>();
        flags = new LinkedList<Flag>();
        links = new LinkedList<Link>();

        neutralTeam = new Team(this, 0);
        neutralTeam.setHealth(3);

        exe = Executors.newFixedThreadPool(2);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        Map<Team, Short> teamId = new HashMap<>();
        short tId = 0;
        for(Team team : teams) {
            teamId.put(team, tId);
            ++tId;
        }

        Map<Entity, Short> entId = new HashMap<>();
        short id = 0;
        for(Flag flag: flags) {
            entId.put(flag, id);
            ++id;
        }
        for(Ship ship : ships) {
            entId.put(ship, id);
            ++id;
        }
        entId.put(null, (short) -1);

        stream.writeShort(teams.size());
        stream.writeShort(flags.size());
        stream.writeShort(ships.size());
        stream.writeShort(links.size());

        for(Team team : teams) {
            stream.writeObject(team);
        }

        for(Flag flag : flags) {
            flag.writeObject(stream);
            stream.writeShort( teamId.get(flag.team));
        }

        for(Ship ship : ships) {
            ship.writeObject(stream);
            stream.writeShort( teamId.get(ship.team));
        }

        for(Ship ship : ships) {
            stream.writeShort( entId.get(ship.target));
        }

        for(Link link : links) {
            stream.writeShort( entId.get(link.flag1));
            stream.writeShort( entId.get(link.flag2));
        }

        stream.writeShort( teamId.get(neutralTeam));
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        short teamCount = stream.readShort();
        short flagCount = stream.readShort();
        short shipCount = stream.readShort();
        short linkCount = stream.readShort();

        Team [] teamArr = new Team[teamCount];
        Flag [] flagArr = new Flag[flagCount];
        Ship [] shipArr = new Ship[shipCount];

        for(int i = 0; i < teamCount; ++i) {
            teamArr[i] = (Team) stream.readObject();
        }

        for(int i = 0; i < flagCount; ++i) {
            float posX = CompressionUtils.toFloat32(stream.readShort());
            float posY = CompressionUtils.toFloat32(stream.readShort());
            float health = CompressionUtils.toFloat32(stream.readShort());
            float angle = stream.readFloat();
            short teamId = stream.readShort();

            Flag flag = new Flag();
            flag.pos = new Vector2(posX, posY);
            flag.angle = angle;
            flag.health = health;
            flag.team = teamArr[teamId];
            flagArr[i] = flag;
        }

        for(int i = 0; i < shipCount; ++i) {
            float posX = CompressionUtils.toFloat32(stream.readShort());
            float posY = CompressionUtils.toFloat32(stream.readShort());
            float velX = CompressionUtils.toFloat32(stream.readShort());
            float velY = CompressionUtils.toFloat32(stream.readShort());
            float angle = CompressionUtils.toFloat32(stream.readShort());
            short teamId = stream.readShort();

            Ship ship = new Ship();
            ship.pos = new Vector2(posX, posY);
            ship.vel = new Vector2(velX, velY);
            ship.angle = angle;
            ship.team = teamArr[teamId];
            shipArr[i] = ship;
        }

        for(int i = 0; i < shipCount; ++i) {
            short entId = stream.readShort();
            if(entId != -1)
                shipArr[i].target = entId < flagCount ? flagArr[entId] : shipArr[entId - flagCount];
        }

        links = new LinkedList<>();
        for(int i = 0; i < linkCount; ++i) {
            Flag flag1  = flagArr[stream.readShort()];
            Flag flag2  = flagArr[stream.readShort()];
            Link link = new Link(flag1, flag2);
            links.add(link);
        }

        teams = Arrays.asList(teamArr);
        flags = Arrays.asList(flagArr);
        ships = Arrays.asList(shipArr);

        neutralTeam = teamArr[stream.readShort()];
    }

    public void update( AtomicLong physicsTime) {
        for(Flag flag : flags){
            flag.move(dt);
        }

        for(Ship ship : ships){
            ship.move(dt);
            ship.updateShooting(dt);
        }

        List<Ship> shipsToDeletion = new LinkedList<Ship>();

        for(Ship ship : ships){
            if(ship.getHealth() <= 0)
                shipsToDeletion.add(ship);
            else
                ship.updateMoving();
        }

        for(Ship ship : shipsToDeletion){
            ship.delete();
        }

        for(Flag flag : flags){
            flag.update();
        }

        for(Team team : teams){
            team.update();
        }

        Future f1 = exe.submit(new CollisionUpdate(this));
        Future f2 = exe.submit(new RangeUpdate(this));

        try{
            f1.get();
            f2.get();
        }catch(Exception e){
            e.printStackTrace();
        }

        for(Ship ship : ships){
            Vector2 vel = ship.getVel();
            if(vel.len2() > 1.0)
                vel.nor();
        }

        dt = ( System.currentTimeMillis() - physicsTime.get())/1000.0f;
        physicsTime.set( System.currentTimeMillis());
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
                    sr.line(ship.getPos().x + ship.getVel().x * ship.team.getSpeed() * delta, ship.getPos().y + ship.getVel().y * ship.team.getSpeed() * delta, ship.getTarget().getPos().x + ship.getTarget().getVel().x * ship.team.getSpeed() * delta, ship.getTarget().getPos().y + ship.getTarget().getVel().y * ship.team.getSpeed() * delta, colors[ship.getTeam().color], colors[ship.getTeam().color]);
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

            //collisionTree.draw(sr);

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
                float x = ship.getPos().x + ship.getVel().x * ship.team.getSpeed() * delta;
                float y = ship.getPos().y + ship.getVel().y * ship.team.getSpeed() * delta;
                float sin = (float) Math.sin(Math.toRadians(-ship.getAngle()));
                float cos = (float) Math.cos(Math.toRadians(-ship.getAngle()));
                Color color = colors[ship.getTeam().color];
                sr.triangle(x + 10 * sin, y + 10 * cos, x - 4 * cos, y + 4 * sin, x + 4 * cos, y - 4 * sin, color, color, color);
            }

            sr.end();
            sb.setProjectionMatrix(cam.combined);
            sb.begin();

            for (Flag flag : getFlags()) {
                bf.setColor( colors[ flag.getTeam().color]);
                GlyphLayout glyphLayout = new GlyphLayout();
                glyphLayout.setText(bf, Integer.toString( (int)flag.getHealth()));
                bf.draw(sb, glyphLayout, flag.getPos().x - glyphLayout.width / 2, flag.getPos().y - glyphLayout.height / 2);
            }
            sb.end();
        }
    }

    public byte[] getBytes(){
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(this);
            return out.toByteArray();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static GamePacket getObject( byte[] bytes){
        if(bytes == null) return null;
        ByteArrayInputStream in = new ByteArrayInputStream( bytes,0, bytes.length);
        try {
            ObjectInputStream is = new ObjectInputStream(in);
            GamePacket gp = (GamePacket) is.readObject();
            return gp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }  
    }

    public GamePacket clone(){
        return getObject( getBytes());
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
    public Team getNeutralTeam(){ return neutralTeam;}
    public List<Ship> getShips(){ return ships;}
    public List<Link> getLinks(){ return links;}
    public List<Flag> getFlags(){ return flags;}
    public List<Team> getTeams(){ return teams;}
}
