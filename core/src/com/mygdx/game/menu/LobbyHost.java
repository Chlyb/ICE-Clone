package com.mygdx.game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.gameSession.Client;
import com.mygdx.game.gameSession.MultiplayerHost;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class LobbyHost extends AbstractScreen {
    private boolean runThread;
    private String serverName;
    public final MultiplayerMenuScreen multiplayerMenu;
    private ServerSocket serverSocket;
    public List<Client> clients;
    public final String groupAddress;

    private SelectBox<Integer> flagcountBox;

    private TextField messageField;
    private com.badlogic.gdx.scenes.scene2d.ui.List<String> chatList;
    private com.badlogic.gdx.scenes.scene2d.ui.List<String> playerList;

    public LobbyHost(final MyGdxGame game, final MultiplayerMenuScreen multiplayerMenu) {
        super(game, multiplayerMenu);
        runThread = true;
        clients = new ArrayList<Client>();
        this.multiplayerMenu = multiplayerMenu;
        serverName = "ICE Server";
        groupAddress = "230.0.0.0";

        new Thread(new Runnable(){ //UDP BROADCAST THREAD
            DatagramSocket socket;
            @Override
            public void run() {
                try {
                    socket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
                    socket.setBroadcast(true);

                    while (runThread) {
                        byte[] recvBuf = new byte[256];
                        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                        socket.receive(packet);

                        String message = new String(packet.getData()).trim();
                        processPacket( packet);

                        if (message.equals("r")) {
                            byte[] sendData = ("r" + groupAddress).getBytes(); //response
                            //Send a response
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                            socket.send(sendPacket);
                        }
                    }
                    socket.close();
                } catch (IOException ex) {
                }
            }
        }).start();

        Label chatLabel = new Label("Chat", skin, "default");
        chatLabel.setSize(120,40);
        chatLabel.setPosition(80,390);
        chatLabel.setAlignment(Align.center);
        stage.addActor(chatLabel);

        chatList = new com.badlogic.gdx.scenes.scene2d.ui.List<String>(skin);
        chatList.setItems("","","","","","","","","","");
        chatList.setSize(250,230);
        chatList.setPosition(20, 150);
        chatList.setSelectedIndex(-1);
        chatList.setTouchable( Touchable.disabled);
        stage.addActor(chatList);

        messageField = new TextField("", skin);
        messageField.setSize(150, 50);
        messageField.setPosition(20, 50);
        stage.addActor(messageField);

        final Button sendBtn = new TextButton("Send", skin, "default");
        sendBtn.setSize(150,50);
        sendBtn.setPosition(180,50);
        sendBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                updateChat( multiplayerMenu.getNick() + ": " + messageField.getText());
                sendMessage("c" + multiplayerMenu.getNick() + ": " + messageField.getText());
                messageField.setText("");
            }
        });
        stage.addActor(sendBtn);

        Label gameTitle = new Label("Lobby ", skin, "big");
        gameTitle.setSize(120,100);
        gameTitle.setPosition(420,450);
        gameTitle.setAlignment(Align.center);
        stage.addActor(gameTitle);

        Label playersLabel = new Label("Players", skin, "default");
        playersLabel.setSize(120,40);
        playersLabel.setPosition(750,390);
        playersLabel.setAlignment(Align.center);
        stage.addActor(playersLabel);

        playerList = new com.badlogic.gdx.scenes.scene2d.ui.List<String>(skin);
        playerList.setSize(250,230);
        playerList.setPosition(695, 150);
        playerList.setTouchable( Touchable.disabled);
        stage.addActor(playerList);

        Label flagcountLabel = new Label("Number of flags", skin, "default");
        flagcountLabel.setSize(120,40);
        flagcountLabel.setPosition(340,340);
        flagcountLabel.setAlignment(Align.center);
        stage.addActor(flagcountLabel);

        flagcountBox = new SelectBox<Integer>(skin);
        flagcountBox.setItems(5,6,7,8,9,10,11,12,13,14,15);
        flagcountBox.setSize(120,40);
        flagcountBox.setPosition(500,340);

        flagcountBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) { ;
            sendMessage( "f" + flagcountBox.getSelected().toString());
            }
        });
        stage.addActor(flagcountBox);

        final Button playBtn = new TextButton("Play", skin, "default");
        playBtn.setSize(150,50);
        playBtn.setPosition(415,230);

        final Label errorLabel = new Label("", skin, "default");

        playBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                runThread = false;
                sendStartInfo();
                MultiplayerHost mh = new MultiplayerHost(game, getThis(), flagcountBox.getSelected(), clients.size());
                game.setScreen(mh);
                Gdx.input.setInputProcessor(mh.getInputMultiplexer());
            }
        });
        stage.addActor(playBtn);

        final Button exitBtn = new TextButton("Back", skin, "default");
        exitBtn.setSize(150,50);
        exitBtn.setPosition(415,90);

        exitBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                //serverSocket.dispose();
                game.setScreen(multiplayerMenu);
                Gdx.input.setInputProcessor(multiplayerMenu.getInputMultiplexer());
            }
        });
        stage.addActor(exitBtn);

        errorLabel.setSize(150,40);
        errorLabel.setPosition(600,170);
        errorLabel.setAlignment(Align.center);
        stage.addActor(errorLabel);

        refreshPlayers();
    }

    public void render(float delta) {
        clearScreen();
        super.render(delta);
    }

    public void refreshPlayers(){
        List<String> nicks = new ArrayList<String>();
        nicks.add(multiplayerMenu.getNick());
        for(Client client : clients){
            nicks.add(client.nick);
        }
        String[] array = nicks.toArray(new String[nicks.size()]);
        playerList.setItems(array);
        playerList.setSelectedIndex(-1);
    }

    private void updateChat(String message){
        Array<String> array = chatList.getItems();
        array.insert(10, message);
        array.removeIndex(0);
        chatList.setItems( array);
        chatList.setSelectedIndex(-1);
    }

    public void sendMessage(String message){
        try {
            DatagramSocket socket;
            InetAddress group;
            byte[] buf;

            socket = new DatagramSocket();
            group = InetAddress.getByName(groupAddress);
            buf = message.getBytes();

            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 8890);
            socket.send(packet);
            socket.close();
        }catch (IOException e){}
    }

    public void processPacket(DatagramPacket packet){
        String address = packet.getAddress().getHostAddress();
        int port = packet.getPort();
        String message = new String(packet.getData()).trim();

        switch (message.charAt(0)){
            case 'j': //joined
                clients.add( new Client( message.substring(1), address, port));

                String textToSend = "p" + multiplayerMenu.getNick() + "/"; //players
                for(Client client : clients) {
                    textToSend = textToSend + client.nick + "/";
                }

                try { Thread.sleep(30);} //wait for the new client
                catch (InterruptedException e) {e.printStackTrace();}

                refreshPlayers();
                sendMessage( textToSend);
                sendMessage( "f" + flagcountBox.getSelected().toString()); //flags
                break;

            case 'c': //chat
                sendMessage( message);
                updateChat( message.substring(1));
                break;
        }
    }

    public void sendStartInfo(){
        DatagramSocket c = null;
        try {c = new DatagramSocket();}
        catch (SocketException e) {e.printStackTrace();}

        byte[] sendData;
        char i = 2;
        for(Client client : clients){
            try {
                sendData = ("s" + i).getBytes();
                DatagramPacket sendPacket = new DatagramPacket( sendData, sendData.length, InetAddress.getByName( client.ip), 8890);
                c.send(sendPacket);
                //System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
            } catch (IOException e) {
                e.printStackTrace();
            }
            ++i;
        }
        c.close();
    }

    public LobbyHost getThis(){
        return this;
    }
}

