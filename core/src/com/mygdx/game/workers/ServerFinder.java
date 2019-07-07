package com.mygdx.game.workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

public class ServerFinder implements Runnable {
    private final DatagramSocket c;
    private final ArrayList<String> serverNames = new ArrayList<>();
    private final com.badlogic.gdx.scenes.scene2d.ui.List<String> serverList;

    public ServerFinder(DatagramSocket c, com.badlogic.gdx.scenes.scene2d.ui.List<String> serverList) {
        this.c = c;
        this.serverList = serverList;
    }

    public void run() {  //UDP
		String[] array = serverNames.toArray(new String[serverNames.size()]);
		serverList.setItems(array);

        byte[] recvBuf = new byte[256];
        DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
		try {
        	while (true) {
				c.receive(receivePacket);
				String message = new String(receivePacket.getData()).trim();

				if (message.length() > 0 && message.charAt(0) == 'r') {
					serverNames.add(receivePacket.getAddress().toString().substring(1) + "/" + message.substring(1));
					array = serverNames.toArray(new String[serverNames.size()]);
					serverList.setItems(array);
				}
			}
        }catch(IOException ex){
			ex.printStackTrace();
		}
    }
}