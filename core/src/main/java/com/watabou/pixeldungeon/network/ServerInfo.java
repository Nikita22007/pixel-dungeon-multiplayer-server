package com.watabou.pixeldungeon.network;

import java.net.InetAddress;

public class ServerInfo {
    public  String name;

    public  int players;
    public  int maxPlayers;

    public  InetAddress IP;
    public  int port;

    public  boolean haveChallenges;

    public  ServerInfo(String name, InetAddress ip, int port,int players, int maxPlayers, boolean haveChallenges){
        this.name=name;
        this.players=players;
        this.maxPlayers=maxPlayers;
        this.IP=ip;
        this.port=port;
        this.haveChallenges=haveChallenges;
    }
}
