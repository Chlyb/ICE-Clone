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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static com.mygdx.game.menu.MultiplayerMenuScreen.sessionPort;
import static com.mygdx.game.menu.MultiplayerMenuScreen.clientPort;


public class MultiplayerHost extends AbstractSession {
    private LobbyHost lobby;
    private GamePacket gp;
    private Team playerTeam;
    private AtomicLong physicsTime = new AtomicLong(0);
    private byte tick = 0;

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

        new Thread(new Runnable() { //receiving thread
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
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

        class CopyToSendAndRenderRunnable implements Runnable {
            byte[] gpBytes;
            public CopyToSendAndRenderRunnable(byte[] gpBytes) {
                this.gpBytes = gpBytes;
            }

            public void run() {
                try {
                    byte[] buf = CompressionUtils.compress(gpBytes);
                    DatagramSocket socket = new DatagramSocket();
                    InetAddress group = InetAddress.getByName(getLobby().groupAddress);
                    DatagramPacket dp = new DatagramPacket(buf, 0, buf.length, group, clientPort);
                    socket.send(dp);
                }catch (Exception e) {
                    e.printStackTrace();
                }

                renderedGp = GamePacket.getObject(gpBytes);
                updateUI();
                renderTime.set(System.currentTimeMillis());
            }
        }

        new Thread(new Runnable() {
            ExecutorService exe = Executors.newSingleThreadExecutor();
            @Override
            public void run() { //Update thread
                while (!finished) {
                    if (!paused.get()) {
                        long t0 = System.currentTimeMillis();

                        gp.update(physicsTime);
                        byte[] gpBytes = gp.getBytes();

                        if(tick%3 == 0)
                            exe.execute(new CopyToSendAndRenderRunnable(gpBytes));
                        ++tick;

                        try {
                            Thread.sleep(33 - (System.currentTimeMillis() - t0));
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    void end(final boolean definitive) {
        if(definitive) {
            finishing = true;
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finished = true;
                dispose();
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
        super.dispose();

        lobby.createListener();
        Gdx.input.setInputProcessor(lobby.getInputMultiplexer());
        game.setScreen(lobby);
    }
}

