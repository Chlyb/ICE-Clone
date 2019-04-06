package com.mygdx.game.gameSession;

import java.security.SecureRandom;

public class Client{
    //public final LobbyHost lobby;
    //public final Socket socket;
    public final String nick;
    public final String ip;

    public Client( String nick, String ip, int port){
        this.nick = nick;
        this.ip = ip;
    }
}
