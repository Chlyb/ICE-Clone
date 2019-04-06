package com.mygdx.game.gameSession;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

import com.mygdx.game.MyGdxGame;
import com.mygdx.game.gameClasses.GamePacket;
import com.mygdx.game.menu.LobbyPlayer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.atomic.AtomicLong;

public class MultiplayerClient implements Screen, GestureDetector.GestureListener {
    private float zoom;

    private MyGdxGame game;
    private LobbyPlayer lobby;
    private OrthographicCamera cam = new OrthographicCamera();

    private GestureDetector gestureDetector;

    private int playerNumber;
    private GamePacket renderedGp;
    private AtomicLong renderTime = new AtomicLong(0);

    private short shootOffset = 0;

    public MultiplayerClient(MyGdxGame game, final LobbyPlayer lobby, int playerNumber) {
        this.game = game;
        this.lobby = lobby;

        cam.setToOrtho(true);
        cam.position.x = 1920/2;
        cam.position.y = 1080/2;
        cam.zoom = 1;
        cam.update();
        zoom = 1;

        this.renderedGp = new GamePacket();
        this.playerNumber = playerNumber;

        gestureDetector = new GestureDetector(this);
        Gdx.input.setInputProcessor(gestureDetector);

        new Thread(new Runnable(){
            MulticastSocket socket = null;
            GamePacket receivedGP;
            byte[] buf = new byte[65536];
            @Override
            public void run() {  //UDP
                try {
                    socket = new MulticastSocket(8890);
                    InetAddress group = InetAddress.getByName( lobby.groupAddress);
                    socket.joinGroup(group);
                    while (true) {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive( packet);
                        ByteArrayInputStream in = new ByteArrayInputStream( packet.getData(),0, packet.getLength());
                        ObjectInputStream is = new ObjectInputStream(in);
                        try{
                            receivedGP = (GamePacket) is.readObject();
                            receivedGP.goOneTickBack();
                            renderedGp = receivedGP;
                            renderTime.set(System.currentTimeMillis());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    //socket.leaveGroup(group);
                    //socket.close();
                }catch (IOException e){}
            }
        }).start();
    }

    @Override
    public void render(float delta){
        ++shootOffset;
        shootOffset %= 4;
        renderedGp.render( (System.currentTimeMillis() - renderTime.get())/1000.0f, cam, game.sr, game.sb, game.bf, playerNumber, shootOffset);
    }

    public void setGp(GamePacket gp){
        this.renderedGp = gp;
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
        int ix = Math.round( x * zoom + cam.position.x - game.WIDTH / 2 * zoom);
        int iy = Math.round( y * zoom + cam.position.y - game.HEIGHT / 2 * zoom);
        char pnc = (char) playerNumber;
        String packet = "t" + pnc;
        packet += ix + "/" + iy;
        packet += "/\n";

        try {
            DatagramSocket c = new DatagramSocket();
            byte[] sendData = packet.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName( lobby.serverIP), 8833);
            c.send(sendPacket);
            c.close();
        } catch (Exception e) {
            e.getMessage();
        }

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
