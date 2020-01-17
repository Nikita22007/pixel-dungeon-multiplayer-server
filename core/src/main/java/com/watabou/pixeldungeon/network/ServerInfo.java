package com.watabou.pixeldungeon.network;

public class ServerInfo {
    public  String name;
    public  int players;
    public  int maxPlayers;
    public  String IP;
    public  int port;
    public  boolean haveChallenges;
    public  ServerInfo(String name, int players, int maxPlayers, String ip, int port, boolean haveChallenges){
        this.name=name;
        this.players=players;
        this.maxPlayers=maxPlayers;
        this.IP=ip;
        this.port=port;
        this.haveChallenges=haveChallenges;
    }
}
