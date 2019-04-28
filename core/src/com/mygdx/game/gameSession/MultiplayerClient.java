package com.mygdx.game.gameSession;

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

public class MultiplayerClient extends AbstractSession {
    private LobbyPlayer lobby;

    public MultiplayerClient(MyGdxGame game, final LobbyPlayer lobby, int playerTeamIndex) {
        super(game, playerTeamIndex);
        this.lobby = lobby;

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
                            updateUI();
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
    protected void playerClick(float x, float y) {
        int ix = Math.round( x * zoom + gameCam.position.x - game.WIDTH / 2 * zoom);
        int iy = Math.round( y * zoom + gameCam.position.y - game.HEIGHT / 2 * zoom);
        char pic = (char) playerTeamIndex;
        String packet = "t" + pic;
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
    }

    @Override
    protected void upgrade(int upgrade) {
        char pic = (char) playerTeamIndex;
        String packet = "u" + pic;
        packet += (char) upgrade ;
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
    }
}
