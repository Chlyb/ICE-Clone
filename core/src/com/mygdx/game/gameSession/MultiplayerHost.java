package com.mygdx.game.gameSession;

import com.badlogic.gdx.Gdx;
import com.mygdx.game.CompressionUtils;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.gameClasses.Flag;
import com.mygdx.game.gameClasses.Team;
import com.mygdx.game.gameClasses.GamePacket;
import com.mygdx.game.menu.LobbyHost;
import com.mygdx.game.menu.ResumeScreen;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.mygdx.game.menu.MultiplayerMenuScreen.sessionPort;
import static com.mygdx.game.menu.MultiplayerMenuScreen.clientPort;


public class MultiplayerHost extends AbstractSession {
    private LobbyHost lobby;
    private GamePacket gp;
    private Team playerTeam;
    private AtomicLong physicsTime = new AtomicLong(0);
    private long sendTime = System.currentTimeMillis();
    private byte tick = 0;
    public static final int fragmentCount = 8;
    public static final int payload = 1471;

    private DatagramSocket receivingSocket;

    public MultiplayerHost(MyGdxGame game, LobbyHost lobby, int flagCount, int enemyCount) {
        super(game, null, 1);
        previousScreen = new ResumeScreen(game, lobby, this);
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
                    Thread.sleep(10);
                    receivingSocket = new DatagramSocket(sessionPort, InetAddress.getByName("0.0.0.0"));
                    while (true) {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        receivingSocket.receive(packet);
                        processPacket(packet);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        final Runnable copyToSendAndRender = new Runnable() {
            @Override
            public void run() {
                byte[] bytes = gp.getBytes();
				if(bytes == null) return;

                if(System.currentTimeMillis() - sendTime > 100) { //big tickrate causes nothing but problems
                    try {
                        byte[] buf = CompressionUtils.compress(bytes);
                        DatagramSocket socket = new DatagramSocket();
                        InetAddress group = InetAddress.getByName(getLobby().groupAddress);

                        DatagramPacket packet;
                        /*
                        byte[] subarr;

                        for (int i = 0; i < fragmentCount; ++i) {
                            if ((i + 1) * (payload) < buf.length) {
                                subarr = new byte[payload + 1];
                                System.arraycopy(buf, i * (payload), subarr, 0, payload);
                                subarr[payload] = tick;
                                packet = new DatagramPacket(subarr, 0, subarr.length, group, clientPort + i);
                            } else if (i * payload < buf.length) {
                                subarr = new byte[buf.length - i * payload + 1];
                                System.arraycopy(buf, i * payload, subarr, 0, buf.length - i * payload);
                                subarr[buf.length - i * payload] = tick;
                                packet = new DatagramPacket(subarr, 0, subarr.length, group, clientPort + i);

                            } else {
                                subarr = new byte[1];
                                subarr[0] = tick;
                                packet = new DatagramPacket(subarr, 0, subarr.length, group, clientPort + i);
                            }
                            socket.send(packet);
                        }*/

                        byte swap;
                        byte empty[] = new byte[1];

                        for(int i = 0; i < fragmentCount; ++i){
                            if((i+1)*payload < buf.length){//o tu zmiana
                                swap = buf[(i+1)*payload];
                                buf[(i+1)*payload] = tick;
                                DatagramPacket dp = new DatagramPacket(buf, i * payload, payload + 1, group, clientPort + i);
                                socket.send(dp);
                                buf[(i+1)*payload] = swap;
                            }
                            else if(i*payload < buf.length){
                                byte end[] = new byte[buf.length - i*payload + 1];
                                System.arraycopy( buf, i * payload, end, 0 , buf.length - i*payload );
                                end[end.length - 1] = tick;

                                DatagramPacket dp = new DatagramPacket(end, 0, end.length, group, clientPort + i);
                                socket.send(dp);
                            }
                            else{
                                empty[0] = tick;
                                DatagramPacket dp = new DatagramPacket(empty, 1, group, clientPort + i);
                                socket.send(dp);
                            }
                        }

                        socket.close();
                        sendTime = System.currentTimeMillis();
                    } catch (IOException e) {
                    }
                }
                renderedGp = GamePacket.getObject(bytes);
                updateUI();
                renderTime.set(System.currentTimeMillis());
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() { //Update thread
                while (!finished) {
                    if (!paused.get()) {
                        gp.update(physicsTime);
                        new Thread(copyToSendAndRender).start();
                        ++tick;
                    }
                }
            }
        }).start();
    }

    @Override
    void end(final boolean definitive) {
        if(definitive) {
            finishing = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finished = true;
                    dispose();
                }
            }).start();
        }
    }

    @Override
    protected void goBack(){
        paused.set(true);
        super.goBack();
    }

    @Override
    public void unpause() {
        super.unpause();
        physicsTime.set(System.currentTimeMillis());
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
        }
    }

    private LobbyHost getLobby() {
        return lobby;
    }

    @Override
    public void dispose(){
        receivingSocket.close();
        stage.dispose();
        super.dispose();

        lobby.createListener();
        Gdx.input.setInputProcessor(lobby.getInputMultiplexer());
        game.setScreen(lobby);
    }
}

