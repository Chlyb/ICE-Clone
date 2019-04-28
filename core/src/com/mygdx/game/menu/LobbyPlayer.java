package com.mygdx.game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.gameSession.MultiplayerClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import static java.lang.Thread.sleep;

public class LobbyPlayer extends AbstractScreen {

    private boolean runThread;

    public final MultiplayerMenuScreen multiplayerMenu;
    private ServerSocket mySocket;

    public static final int serverPort = 8888;
    public static final int myPort = 8889;
    public final String serverIP;
    public final String groupAddress;

    private int playerIndex = -1;

    private Label flagLabel;
    private TextField messageField;
    private com.badlogic.gdx.scenes.scene2d.ui.List<String> chatList;
    private com.badlogic.gdx.scenes.scene2d.ui.List<String> playerList;

    public LobbyPlayer(final MyGdxGame game, final MultiplayerMenuScreen multiplayerMenu, final String serverIP, final String groupAddress) {
        super(game);
        this.multiplayerMenu = multiplayerMenu;
        Gdx.input.setInputProcessor(stage);
        runThread = true;
        this.serverIP = serverIP;
        this.groupAddress = groupAddress;
        
        sendMessage("j" + multiplayerMenu.getNick()); //join

        new Thread(new Runnable(){
            MulticastSocket socket = null;
            byte[] buf = new byte[256];
            @Override
            public void run() {  //UDP
                try {
                    socket = new MulticastSocket(8890);
                    InetAddress group = InetAddress.getByName( groupAddress);
                    socket.joinGroup(group);
                    //System.out.println(groupAddress);
                    while (runThread) {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive( packet);
                        processPacket( packet);
                    }
                    //socket.leaveGroup(group);
                    //socket.close();
                }catch (IOException e){}
            }
        }).start(); // And, start the thread running*/

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

        flagLabel = new Label("Number of flags  ", skin, "default");
        flagLabel.setSize(120,40);
        flagLabel.setPosition(340,340);
        flagLabel.setAlignment(Align.center);
        stage.addActor(flagLabel);

        final Label errorLabel = new Label("", skin, "default");

        final Button exitBtn = new TextButton("Back", skin, "default");
        exitBtn.setSize(150,50);
        exitBtn.setPosition(415,90);
        exitBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                mySocket.dispose();
                Gdx.input.setInputProcessor(multiplayerMenu.stage);
                game.setScreen( multiplayerMenu);
            }
        });
        stage.addActor(exitBtn);

        errorLabel.setSize(150,40);
        errorLabel.setPosition(600,170);
        errorLabel.setAlignment(Align.center);
        stage.addActor(errorLabel);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        clearScreen();
        super.render(delta);

        if(playerIndex != -1){
            game.setScreen(new MultiplayerClient(game, getThis(), playerIndex));
        }
    }

    public void processPacket(DatagramPacket packet){
        String message = new String(packet.getData(), 0, packet.getLength());

        switch (message.charAt(0)){
            case 'p': //players
                message = message.substring(1);
                String[] array = message.split("/");
                playerList.setItems(array);
                playerList.setSelectedIndex(-1);
                break;

            case 'f': //flags
                flagLabel.setText("Number of flags " + message.substring(1));
                break;
            case 'c': //chat
                updateChat(message.substring(1));
                break;
            case 's': //start
                System.out.println("taka wiadomosc " + message);
                playerIndex = message.charAt(1);
                runThread = false;
                break;
        }
    }

    public void sendMessage(String message){
        try {
            DatagramSocket c = new DatagramSocket();
            byte[] sendData = message.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName( serverIP), serverPort);
            c.send(sendPacket);
            //System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
            c.close();
        } catch (Exception e) {}
    }

    private void updateChat(String message){
        Array<String> array = chatList.getItems();
        array.insert(10, message);
        array.removeIndex(0);
        chatList.setItems( array);
        chatList.setSelectedIndex(-1);
    }

    public ServerSocket getSocket(){
        return mySocket;
    }

    public LobbyPlayer getThis(){
        return this;
    }
}

