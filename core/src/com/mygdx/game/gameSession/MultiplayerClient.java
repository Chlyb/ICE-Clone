package com.mygdx.game.gameSession;

import com.badlogic.gdx.Gdx;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.gameClasses.GamePacket;
import com.mygdx.game.menu.AbstractScreen;
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
import java.util.zip.DataFormatException;

import static com.mygdx.game.CompressionUtils.decompress;
import static com.mygdx.game.gameSession.MultiplayerHost.fragmentCount;
import static com.mygdx.game.gameSession.MultiplayerHost.payload;

public class MultiplayerClient extends AbstractSession {
    private LobbyPlayer lobby;
    private LinkedList <Fragment> [] receivedFragments;
    private LinkedList <Fragment> [] preservedFragments;

    private class Fragment { //packet fragment
        final byte tick;
        final byte[] data;
        final int length;
        Fragment(byte[] data, int len) {
            this.length = len - 1;
            this.tick = data[length];
            this.data = data;
        }
    }

    private class packetToRender implements Runnable {
        private ByteArrayOutputStream os;

        public packetToRender(ByteArrayOutputStream os) {
            this.os = os;
        }

        @Override
        public void run() {
            try{
                byte[] decompressed = decompress(os.toByteArray());
                ByteArrayInputStream in = new ByteArrayInputStream(decompressed, 0, decompressed.length);
                ObjectInputStream is = new ObjectInputStream( in );

                GamePacket receivedGP = (GamePacket) is.readObject();
                receivedGP.goOneTickBack();
                renderedGp = receivedGP;
                updateUI();
                renderTime.set(System.currentTimeMillis());
            }catch (IOException e) {
                e.printStackTrace();
            }catch (DataFormatException e) {
                e.printStackTrace();
            }catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private class listener implements Runnable {
        private int i;
        public listener(int i) {
            this.i = i;
        }
        @Override
        public void run() {
            byte[] buf = new byte[payload + 1];
            try {
                MulticastSocket socket = new MulticastSocket(8890 + i);
                InetAddress group = InetAddress.getByName(lobby.groupAddress);
                socket.joinGroup(group);
                while (!finished) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    Fragment sub = new Fragment(packet.getData(), packet.getLength());
                    handleFragment(i, sub);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public MultiplayerClient(MyGdxGame game, final LobbyPlayer lobby, int playerTeamIndex) {
        super(game, null, playerTeamIndex);
        previousScreen = new ResumeScreen(game, lobby, this);
        this.lobby = lobby;

        this.receivedFragments = new LinkedList[fragmentCount];
        this.preservedFragments = new LinkedList[fragmentCount];

        for (int i = 0; i < fragmentCount; ++i) {
            this.receivedFragments[i] = new LinkedList<>();
            this.preservedFragments[i] = new LinkedList<>();
            new Thread(new listener(i)).start();
        }
    }

    @Override
    void end(final boolean definitive) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(definitive) {
                    finishing = true;
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finished = true;
                    game.setScreen(lobby);
                    Gdx.input.setInputProcessor(lobby.getInputMultiplexer());
                }
            }
        }).start();
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
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(lobby.serverIP), 8833);
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
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(lobby.serverIP), 8833);
            c.send(sendPacket);
            c.close();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private synchronized void handleFragment(int i, Fragment fra) {
        int position = 0;
        while (position < receivedFragments[i].size() && receivedFragments[i].get(position).tick < fra.tick) {
            ++position;
        }
        receivedFragments[i].add(position, fra);

        int[] positions = new int[fragmentCount];
        byte tick = fra.tick;
        boolean found = true;

        for (int j = 0; j < fragmentCount; ++j) {
            boolean contains = false;
            int ix = 0;
            if (receivedFragments[j].size() == 0) {
                found = false;
                break;
            }
            for (Fragment fr : receivedFragments[j]) {
                if (fr.tick == tick) {
                    contains = true;
                    positions[j] = ix;
                }
                ix++;
            }
            if (!contains) {
                found = false;
                break;
            }
        }

        if (found) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                for (int j = 0; j < fragmentCount; ++j) {
                    os.write(receivedFragments[j].get(positions[j]).data, 0, receivedFragments[j].get(positions[j]).length);
                }
                os.close();
                new Thread(new packetToRender(os)).start();

                if (tick > 107) { //byte overflow guard
                    for (int j = 0; j < fragmentCount; ++j) {
                        preservedFragments[j].clear();
                        for (Fragment sp: receivedFragments[j]) {
                            if (tick < sp.tick) {
                                preservedFragments[j].add(sp);
                            }
                        }
                        receivedFragments[j] = preservedFragments[j];
                    }
                } else {
                    for (int j = 0; j < fragmentCount; ++j) {
                        preservedFragments[j].clear();
                        for (Fragment sp: receivedFragments[j]) {
                            if (tick < sp.tick && sp.tick <= tick + 20) {
                                preservedFragments[j].add(sp);
                            }
                        }
                        receivedFragments[j] = preservedFragments[j];
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}