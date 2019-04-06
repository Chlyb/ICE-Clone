package com.mygdx.game.gameSession;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.gameClasses.Flag;
import com.mygdx.game.gameClasses.Team;
import com.mygdx.game.gameClasses.GamePacket;
import com.mygdx.game.menu.LobbyHost;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MultiplayerHost implements Screen, GestureDetector.GestureListener {
    private float zoom;

    private MyGdxGame game;
    private LobbyHost lobby;
    private OrthographicCamera cam = new OrthographicCamera();

    private GestureDetector gestureDetector;

    private GamePacket gp;
    private GamePacket renderedGp;
    private Team playerTeam;

    private AtomicLong physicsTime = new AtomicLong(0);
    private AtomicLong renderTime = new AtomicLong(0);

    private short shootOffset = 0;

    public MultiplayerHost(MyGdxGame game, LobbyHost lobby, int flagCount, int enemyCount) {
        this.game = game;
        this.lobby = lobby;

        cam.setToOrtho(true);
        cam.position.x = 1920/2;
        cam.position.y = 1080/2;
        cam.update();
        cam.zoom = 1;
        zoom = 1;

        this.gp = new GamePacket();
        this.renderedGp = gp;

        List<String> existingTeams  = new ArrayList<String>();

        Team.generateTeams( gp, existingTeams, enemyCount + 1);
        this.playerTeam = gp.getTeams().get(1);

        Flag.generateFlags( gp, 1920, 1080, flagCount);

        gestureDetector = new GestureDetector(this);
        Gdx.input.setInputProcessor(gestureDetector);

        physicsTime.set(System.currentTimeMillis());

        new Thread(new Runnable(){ //UDP
            byte[] buf = new byte[256];

            @Override
            public void run() {
                try {
                    try {
                        Thread.sleep(10);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    DatagramSocket socket = null;
                    try {
                        socket = new DatagramSocket(8833, InetAddress.getByName("0.0.0.0"));
                    }catch (SocketException e){
                        e.getMessage();
                    }

                    while (true) {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive( packet);
                        processPacket( packet);
                    }
                } catch (IOException ex) {}
            }
        }).start();

        final Runnable copyToSendAndRender = new Runnable(){
            @Override
            public void run() {
                byte [] buf = gp.getBytes();
                //System.out.println("size" + buf.length);
                try {
                    DatagramSocket socket = new DatagramSocket();
                    InetAddress group = InetAddress.getByName( getLobby().groupAddress);
                    DatagramPacket packet = new DatagramPacket( buf, buf.length, group, 8890);
                    socket.send(packet);
                    socket.close();
                }catch (IOException e){}

                renderedGp = GamePacket.getObject( buf);
                renderedGp.goOneTickBack();
                renderTime.set( System.currentTimeMillis());
            }
        };

        new Thread(new Runnable(){
            @Override
            public void run() { //Update thread
                while (true){
                    gp.update(physicsTime);
                    new Thread(copyToSendAndRender).start();
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

    private void processPacket(DatagramPacket packet){
        String message = new String(packet.getData()).trim();

        switch (message.charAt(0)){
            case 't': //tap
                int playerNumber = message.charAt(1);
                message = message.substring(2);
                String[] array = message.split("/");
                gp.getTeams().get( playerNumber).onPress( Integer.parseInt(array[0]), Integer.parseInt(array[1]));
                break;
            case 'c':
                break;
            case 's':
                break;
        }
    }

    private LobbyHost getLobby(){
        return lobby;
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
    public void dispose() {
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }
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
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {
        zoom = cam.zoom;
    }
}
