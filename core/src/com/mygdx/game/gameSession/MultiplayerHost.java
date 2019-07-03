package com.mygdx.game.gameSession;

import com.mygdx.game.CompressionUtils;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.gameClasses.Flag;
import com.mygdx.game.gameClasses.Team;
import com.mygdx.game.gameClasses.GamePacket;
import com.mygdx.game.menu.LobbyHost;

import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DataFormatException;

import static com.mygdx.game.CompressionUtils.decompress;

public class MultiplayerHost extends AbstractSession {
    private LobbyHost lobby;
    private GamePacket gp;
    private Team playerTeam;
    private AtomicLong physicsTime = new AtomicLong(0);
    private byte tick = 0;
    public static final int fragmentCount = 8;
    public static final int payload = 1471;

    public MultiplayerHost(MyGdxGame game, LobbyHost lobby, int flagCount, int enemyCount) {
        super(game, 1);
        this.lobby = lobby;
        this.gp = new GamePacket();

        List<String> existingTeams = new ArrayList<String>();

        Team.generateTeams(gp, existingTeams, enemyCount + 1);
        this.playerTeam = gp.getTeams().get(1);
        Flag.generateFlags(gp, 1920, 1080, flagCount);

        physicsTime.set(System.currentTimeMillis());

        new Thread(new Runnable() { //UDP
            byte[] buf = new byte[256];

            @Override
            public void run() {
                try {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    DatagramSocket socket = null;
                    try {
                        socket = new DatagramSocket(8833, InetAddress.getByName("0.0.0.0"));
                    } catch (SocketException e) {
                        e.getMessage();
                    }

                    while (true) {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        processPacket(packet);
                    }
                } catch (IOException ex) {
                }
            }
        }).start();

        final Runnable copyToSendAndRender = new Runnable() {
            @Override
            public void run() {
                if(tick % 5 != 0) return; //big tickrate causes nothing but problems
                byte[] bytes = gp.getBytes();
                try {
                    byte[] buf = CompressionUtils.compress(bytes);
                    DatagramSocket socket = new DatagramSocket();
                    InetAddress group = InetAddress.getByName(getLobby().groupAddress);

                    DatagramPacket packet;

                    byte[] subarr;

                    for(int i = 0; i < fragmentCount; ++i){
                        if((i+1)*(payload) < buf.length){
                            subarr = new byte[payload+1];
                            System.arraycopy(buf, i*(payload), subarr, 0, payload);
                            subarr[payload] = tick;
                            packet = new DatagramPacket(subarr, 0, subarr.length, group, 8890 + i);
                        }
                        else if(i*payload < buf.length){
                            subarr = new byte[buf.length - i*payload + 1];
                            System.arraycopy(buf, i*payload, subarr, 0, buf.length - i*payload);
                            subarr[buf.length - i*payload] = tick;
                            packet = new DatagramPacket(subarr, 0, subarr.length, group, 8890 + i);

                        }else{
                            subarr = new byte[1];
                            subarr[0] = tick;
                            packet = new DatagramPacket(subarr, 0, subarr.length, group, 8890 + i);
                        }
                        socket.send(packet);
                    }
                    socket.close();
                } catch (IOException e) {
                }
                renderedGp = GamePacket.getObject(bytes);
                updateUI();
                renderTime.set(System.currentTimeMillis());
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() { //Update thread
                while (true) {
                    gp.update(physicsTime);
                    new Thread(copyToSendAndRender).start();
                    ++tick;
                }
            }
        }).start();
    }

    @Override
    protected void playerClick(float x, float y) {
        playerTeam.onPress((int) (x * zoom + gameCam.position.x - game.WIDTH / 2 * zoom), (int) (y * zoom + gameCam.position.y - game.HEIGHT / 2 * zoom));
    }

    @Override
    protected void upgrade(int upgrade) {
        playerTeam.upgrade(upgrade);
    }

    private void processPacket(DatagramPacket packet) {
        String message = new String(packet.getData()).trim();
        int playerIndex = message.charAt(1);
        switch (message.charAt(0)) {
            case 't': //tap
                message = message.substring(2);
                String[] array = message.split("/");
                gp.getTeams().get(playerIndex).onPress(Integer.parseInt(array[0]), Integer.parseInt(array[1]));
                break;
            case 'u': //upgrade
                gp.getTeams().get(playerIndex).upgrade(message.charAt(2));
                break;
            case 'l':
                break;
        }
    }

    private LobbyHost getLobby() {
        return lobby;
    }
}

