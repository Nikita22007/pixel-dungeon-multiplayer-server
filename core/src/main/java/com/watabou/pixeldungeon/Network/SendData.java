package com.watabou.pixeldungeon.Network;

import com.watabou.pixeldungeon.levels.Level;

import static com.watabou.pixeldungeon.Network.Server.clients;  //I'm too lazy to write "Server.clients". Never do as I do.

public class SendData {

    //-------------------Level
    public static void sendLevelMap(Level level, int ID) {
        if (clients[ID] != null) {
            clients[ID].send(ID, Codes.LEVEL_MAP, level.map);
        }
    }
    public static void sendLevelVisited(Level level, int ID){
        if (clients[ID] != null) {
            clients[ID].send(ID, Codes.LEVEL_VISITED, level.visited);
        }
    }
    public static void sendLevelMapped(Level level, int ID){
        if (clients[ID] != null) {
            clients[ID].send(ID, Codes.LEVEL_MAPPED, level.mapped);
        }
    }

    //---------------------------UI  and mechanics
    public static void sendAllBossSlain(){
        ClientThread.sendAll(Codes.BOSS_SLAIN);
    }

}
