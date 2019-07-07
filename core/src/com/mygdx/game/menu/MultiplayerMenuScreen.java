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

    public final static int clientPort = 19910;
    public final static int serverPort = 19927;
    public final static int sessionPort = 19991;

    private String IP;
    private com.badlogic.gdx.scenes.scene2d.ui.List<String> serverList;
    private TextButton networkBtn;
    private TextField nickField;

    public MultiplayerMenuScreen(MyGdxGame game, MainMenuScreen mainMenu) {
        super(game, mainMenu);
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
                if(serverList.getSelected() != "") {
                    LobbyPlayer lp = new LobbyPlayer(game, getThis(), serverList.getSelected().split("/")[0], serverList.getSelected().split("/")[1]);
                    game.setScreen(lp);
                    Gdx.input.setInputProcessor(lp.getInputMultiplexer());
                }
            }
        });
        stage.addActor(joinBtn);

        final Button hostBtn = new TextButton("Host", skin, "default");
        hostBtn.setSize(350, 50);
        hostBtn.setPosition(320, 130);
        hostBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(IP != "") {
                    LobbyHost lh = new LobbyHost(game, getThis());
                    game.setScreen(lh);
                    Gdx.input.setInputProcessor(lh.getInputMultiplexer());
                }
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
                goBack();
            }
        });
        stage.addActor(backBtn);
    }

    public void refreshServers(){
        // Find the server using UDP broadcast
        if(IP == "") {
            refreshIP();
        }
        if(IP == "") {
            return;
        }

        try {
            final DatagramSocket c = new DatagramSocket();
            c.setBroadcast(true);

            byte[] sendData = "r".getBytes(); //request

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

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, serverPort);
                    c.send(sendPacket);
                }
            }

            Thread t = new Thread(new ServerFinder(c, serverList));
            t.start();
            Thread.sleep(100);
            c.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void refreshIP(){
        List<String> addresses = new ArrayList<String>();
        IP = "";
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
            if(addresses.size() > 1) IP  = addresses.get(1);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if(IP == "") networkBtn.setText("No Internet connection");
        else networkBtn.setText("Connected as " + IP);
    }

    @Override
    public void render(float delta) {
        clearScreen();
        super.render(delta);
    }

    public MultiplayerMenuScreen getThis(){
        return this;
    }

    public String getNick(){
        return nickField.getText();
    }
}