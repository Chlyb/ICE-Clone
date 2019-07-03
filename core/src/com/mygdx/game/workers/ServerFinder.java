package com.mygdx.game.workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

public class ServerFinder implements Runnable {
    public final DatagramSocket c;
    public final ArrayList<String> serverNames;

    public ServerFinder(DatagramSocket c, ArrayList<String> serverNames) {
        this.c = c;
        this.serverNames = serverNames;
    }

    public void run() {  //UDP
        byte[] recvBuf = new byte[256];
        DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

        while (!Thread.interrupted()) {
            try{
                c.receive(receivePacket);
            }catch(IOException ex){}

            String message = new String(receivePacket.getData()).trim();

            if (message.length() > 0 &&message.charAt(0) == 'r') {
                serverNames.add( receivePacket.getAddress().toString().substring(1) + "/" + message.substring(1));
            }
        }
    }
}