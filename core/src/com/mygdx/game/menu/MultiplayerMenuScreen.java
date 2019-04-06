package com.mygdx.game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.workers.ServerFinder;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class MultiplayerMenuScreen extends AbstractScreen {
    private final MainMenuScreen mainMenu;

    private String IP;
    private String arg;
    private com.badlogic.gdx.scenes.scene2d.ui.List<String> serverList;
    private TextButton networkBtn;
    private TextField nickField;

    public MultiplayerMenuScreen(MyGdxGame game, MainMenuScreen mainMenu) {
        super(game);
        this.mainMenu = mainMenu;
        IP = "";
        init();
    }

    void init() {
        Label gameTitle = new Label("Multiplayer", skin, "big");
        gameTitle.setSize(120, 100);
        gameTitle.setPosition(420, 450);
        gameTitle.setAlignment(Align.center);
        stage.addActor(gameTitle);

        nickField = new TextField("Player", skin);
        nickField.setSize(150, 50);
        nickField.setPosition(385, 405);
        nickField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                arg = nickField.getText();
                //System.out.println(arg);
            }
        });
        stage.addActor(nickField);

        Label nickLabel = new Label("Nick", skin, "default");
        nickLabel.setSize(120,40);
        nickLabel.setPosition(300,410);
        nickLabel.setAlignment(Align.center);
        stage.addActor(nickLabel);

        Label serversLabel = new Label("Servers", skin, "default");
        serversLabel.setSize(120,40);
        serversLabel.setPosition(80,390);
        serversLabel.setAlignment(Align.center);
        stage.addActor(serversLabel);

        final Button refreshBtn = new TextButton("Refresh", skin, "default");
        refreshBtn.setSize(350, 50);
        refreshBtn.setPosition(320, 270);
        refreshBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                refreshServers();
            }
        });
        stage.addActor(refreshBtn);

        final Button joinBtn = new TextButton("Join", skin, "default");
        joinBtn.setSize(350, 50);
        joinBtn.setPosition(320, 200);
        joinBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //game.setScreen(new LobbyPlayer(game, getThis(), serverBox.getSelected()));
                //game.setScreen(new LobbyPlayer(game, getThis(), serverList.getSelected()));
                game.setScreen(new LobbyPlayer(game, getThis(), serverList.getSelected().split("/")[0], serverList.getSelected().split("/")[1]));
            }
        });
        stage.addActor(joinBtn);

        final Button hostBtn = new TextButton("Host", skin, "default");
        hostBtn.setSize(350, 50);
        hostBtn.setPosition(320, 130);
        hostBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LobbyHost(game, getThis()));
            }
        });
        stage.addActor(hostBtn);

        serverList = new com.badlogic.gdx.scenes.scene2d.ui.List<String>(skin);
        serverList.setSize(250,230);
        serverList.setPosition(20, 150);
        stage.addActor(serverList);

        networkBtn = new TextButton("", skin, "small");
        networkBtn.setSize(220, 50);
        networkBtn.setPosition(720, 15);
        networkBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                refreshIP();
            }
        });
        stage.addActor(networkBtn);

        final Button backBtn = new TextButton("Back", skin, "default");
        backBtn.setSize(350, 50);
        backBtn.setPosition(320, 60);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.input.setInputProcessor(mainMenu.stage);
                game.setScreen(mainMenu);
            }
        });
        stage.addActor(backBtn);
    }

    public void refreshServers(){
        final ArrayList<String> serverNames = new ArrayList<String>();
        // Find the server using UDP broadcast
        if(networkBtn.getText() == "No Internet connection") {
            return;
        }
        try {
            //Open a random port to send the package
            final DatagramSocket c = new DatagramSocket();
            c.setBroadcast(true);

            byte[] sendData = "r".getBytes(); //request

            //Try the 255.255.255.255 first
            /*
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
                c.send(sendMessage);
                System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
            } catch (Exception e) {
            }
            */
            // Broadcast the message over all the network interfaces
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // Don't want to broadcast to the loopback interface
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
                        c.send(sendPacket);
                    } catch (Exception e) {
                    }
                }
            }

            Thread t = new Thread(new ServerFinder(c, serverNames));
            t.start();
            try
            {
                t.join(500);
                t.interrupt();
            }
            catch(InterruptedException ex){}
            c.close();
        } catch (IOException ex) {}

        String[] array = serverNames.toArray(new String[serverNames.size()]);
        serverList.setItems(array);
    }

    public void refreshIP(){
        List<String> addresses = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for(NetworkInterface ni : Collections.list(interfaces)){
                for(InetAddress address : Collections.list(ni.getInetAddresses()))
                {
                    if(address instanceof Inet4Address){
                        addresses.add(address.getHostAddress());
                    }
                }
            }
            System.out.println(addresses);
            if(addresses.size() > 1) IP  = addresses.get(1);
            else IP = "";
            //IP = IP.substring(0, IP.length() - 2);
        } catch (SocketException e) {
            e.printStackTrace();
            IP = "";
        }

        if(IP == "") networkBtn.setText("No Internet connection");
        else networkBtn.setText("Connected as " + IP);
    }

    public void render(float delta){
        super.render(delta);
    }

    public MultiplayerMenuScreen getThis(){
        return this;
    }

    public String getNick(){
        return nickField.getText();
    }
}