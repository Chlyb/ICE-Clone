package com.mygdx.game.gameSession;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

import com.mygdx.game.MyGdxGame;
import com.mygdx.game.gameClasses.Flag;
import com.mygdx.game.gameClasses.GamePacket;
import com.mygdx.game.gameClasses.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SinglePlayerSession implements Screen, GestureDetector.GestureListener {
    private float zoom;
    private MyGdxGame game;
    private OrthographicCamera cam = new OrthographicCamera();
    private GestureDetector gestureDetector;

    private GamePacket gp;
    private GamePacket renderedGp;

    private Team playerTeam;

    private AtomicLong physicsTime = new AtomicLong(0);
    private AtomicLong renderTime = new AtomicLong(0);

    private short shootOffset = 0;

    public SinglePlayerSession(MyGdxGame game, int flagCount, int enemyCount, String playerColor) {
        this.game = game;
        cam.setToOrtho(true);
        cam.position.x = 1920/2;
        cam.position.y = 1080/2;
        cam.zoom = 1;
        zoom = 1;
        cam.update();

        this.gp = new GamePacket();
        this.renderedGp = gp;

        List<String> existingTeams  = new ArrayList<String>();

        if(playerColor == "Green"){
            playerTeam = new Team(gp, 1);
            existingTeams.add("Green");
        }
        else if(playerColor == "Blue"){
            playerTeam = new Team(gp, 2);
            existingTeams.add("Blue");
        }
        else if(playerColor == "Purple"){
            playerTeam = new Team(gp, 3);
            existingTeams.add("Purple");
        }
        else if(playerColor == "Orange"){
            playerTeam = new Team(gp, 4);
            existingTeams.add("Orange");
        }
        else if(playerColor == "Red"){
            playerTeam = new Team(gp, 5);
            existingTeams.add("Red");
        }

        Team.generateTeams( gp, existingTeams, enemyCount);
        Flag.generateFlags( gp, 1920, 1080, flagCount);

        gestureDetector = new GestureDetector(this);
        Gdx.input.setInputProcessor(gestureDetector);

        physicsTime.set(System.currentTimeMillis());
        //playerTeam = gp.getTeams().get(0);

        final Runnable copyToRender = new Runnable(){
            @Override
            public void run() {
                renderedGp = gp.clone();
                renderedGp.goOneTickBack();
                renderTime.set( System.currentTimeMillis());
            }
        };

        new Thread(new Runnable(){
            @Override
            public void run() { //Update thread
                while (true){
                    gp.update(physicsTime);
                    for(Team team : gp.getTeams()){
                        if(team != gp.getNeutralTeam() && team != playerTeam) team.AItrigger();
                    }
                    new Thread(copyToRender).start();
                }
            }
        }).start();
    }

    @Override
    public void render(float delta){
        ++shootOffset;
        shootOffset %= 4;
        renderedGp.render( (System.currentTimeMillis() - renderTime.get())/1000.0f, cam, game.sr, game.sb, game.bf, 1, shootOffset);
    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() { }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) { return false; }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        playerTeam.onPress((int) (x * zoom + cam.position.x - game.WIDTH / 2 * zoom), (int) (y * zoom + cam.position.y - game.HEIGHT / 2 * zoom));
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        cam.translate(-deltaX*zoom, -deltaY*zoom);
        cam.update();
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        cam.zoom = zoom * initialDistance/distance;
        cam.update();
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) { return false; }

    @Override
    public void pinchStop() {
        zoom = cam.zoom;
    }
}
