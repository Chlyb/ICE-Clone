package com.mygdx.game.gameSession;

import com.badlogic.gdx.Gdx;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.gameClasses.GamePacket;
import com.mygdx.game.menu.LobbyPlayer;
import com.mygdx.game.menu.ResumeScreen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;

import static com.mygdx.game.CompressionUtils.decompress;
import static com.mygdx.game.menu.MultiplayerMenuScreen.sessionPort;
import static com.mygdx.game.menu.MultiplayerMenuScreen.clientPort;

public class MultiplayerClient extends AbstractSession {
    private LobbyPlayer lobby;
    private MulticastSocket socket;

    private class packetToRender implements Runnable {
        private byte[] bytes;

        public packetToRender(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public void run() {
            try {
                byte[] decompressed = decompress(bytes);
                GamePacket receivedGP = GamePacket.getObject(decompressed);

                renderedGp = receivedGP;
                updateUI();
                renderTime.set(System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public MultiplayerClient(MyGdxGame game, final LobbyPlayer lobby, int playerTeamIndex) {
        super(game, null, playerTeamIndex);
        previousScreen = new ResumeScreen(game, lobby, this);
        this.lobby = lobby;

        try {
            InetAddress group = InetAddress.getByName(lobby.groupAddress);
            socket = new MulticastSocket(clientPort);
            socket.joinGroup(group);

            new Thread(() -> { //receiving thread
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardOldestPolicy();
                ExecutorService exe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1), handler);

                byte[] buf = new byte[5000];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    while (true) {
                        socket.receive(packet);
                        exe.execute(new packetToRender(packet.getData()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    void end(final boolean definitive) {
        if (definitive) {
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
    protected void playerClick(float x, float y) {
        int ix = Math.round(x * zoom + gameCam.position.x - game.WIDTH / 2 * zoom);
        int iy = Math.round(y * zoom + gameCam.position.y - game.HEIGHT / 2 * zoom);
        char pic = (char) playerTeamIndex;
        String packet = "t" + pic;
        packet += ix + "/" + iy;
        packet += "/\n";

        try {
            DatagramSocket c = new DatagramSocket();
            byte[] sendData = packet.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(lobby.serverIP), sessionPort);
            c.send(sendPacket);
            c.close();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    protected void upgrade(int upgrade) {
        char pic = (char) playerTeamIndex;
        String packet = "u" + pic;
        packet += (char) upgrade;
        packet += "/\n";

        try {
            DatagramSocket c = new DatagramSocket();
            byte[] sendData = packet.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(lobby.serverIP), sessionPort);
            c.send(sendPacket);
            c.close();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public void dispose() {
        socket.close();
        super.dispose();
        lobby.createListener();
        Gdx.input.setInputProcessor(lobby.getInputMultiplexer());
        game.setScreen(lobby);
    }
}