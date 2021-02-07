package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.windows.WndStory;

import static com.watabou.pixeldungeon.network.Server.clients;  //I'm too lazy to write "Server.clients". Never do as I do.

public class SendData {

    //---------------------------Level
    public static void sendLevelMap(Level level, int ID) {
        if (clients[ID] != null) {
            clients[ID].send(Codes.LEVEL_MAP, level.map);
        }
    }
    public static void sendLevelVisited(Level level, int ID){
        if (clients[ID] != null) {
            clients[ID].send(Codes.LEVEL_VISITED, level.visited);
        }
    }
    public static void sendLevelMapped(Level level, int ID){
        if (clients[ID] != null) {
            clients[ID].send(Codes.LEVEL_MAPPED, level.mapped);
        }
    }

    //---------------------------UI  and mechanics
    public static void sendAllBossSlain(){
        ClientThread.sendAll(Codes.BOSS_SLAIN);
    }
    public static void sendResumeButtonVisible(int ID, boolean visible) {
        if (clients[ID] != null) {
            clients[ID].send(Codes.RESUME_BUTTON, visible);
        }
    }

    public static void sendIronKeysCount(int ID, int count)
    {
        if (clients[ID] != null) {
            clients[ID].send(Codes.IRON_KEYS_COUNT,count);
        }
    }

    //---------------------------Badges
    //public static void sendBadge
    public static void sendBadgeLevelReached(int ID, int bLevel){//bLevel=BadgeLevel
        if (clients[ID] != null) {
            clients[ID].send(Codes.BADGE_LEVEL_REACHED,bLevel);
        }
    }

    public static void sendBadgeStrengthAttained(int ID, int bLevel) {
        if (clients[ID] != null) {
            clients[ID].send(Codes.BADGE_STRENGTH_ATTAINED,bLevel);
        }
    }
    public static void sendAllBadgeBossSlain(int bLevel){
        ClientThread.sendAll(Codes.BADGE_BOSS_SLAIN,bLevel);
    }
    public static void sendBadgeMastery(int ID){
        if (clients[ID]!=null){
            clients[ID].sendCode(Codes.BADGE_MASTERY);
        }
    }

    //-----------------------------Interlevel Scene
    public static void sendInterLevelSceneDescend(int ID){
        if (clients[ID] != null) {
            clients[ID].sendCode(Codes.IL_DESCEND);
        }
    }
    public static void sendInterLevelSceneAscend(int ID){
        if (clients[ID] != null) {
            clients[ID].sendCode(Codes.IL_ASCEND);
        }
    }
    public static void sendInterLevelSceneFall(int ID){
        if (clients[ID] != null) {
            clients[ID].sendCode(Codes.IL_FALL);
        }
    }
    public static void sendInterLevelSceneResurrect(int ID){
        if (clients[ID] != null) {
            clients[ID].sendCode(Codes.IL_RESURRECT);
        }
    }
    public static void sendInterLevelSceneReturn(int ID){
        if (clients[ID] != null) {
            clients[ID].sendCode(Codes.IL_RETURN);
        }
    }
    public static void sendInterLevelSceneOther(int ID,String data){
        if (clients[ID] != null) {
            clients[ID].send(Codes.IL_OTHER, data);
       }
    }
    public static void sendInterLevelSceneFadeOut(int ID) {
        if (clients[ID] != null) {
            clients[ID].sendCode(Codes.IL_FADE_OUT);
        }
    }

    //-----------------------------Windows
    public static void sendWindow(int ID, int WindowID) {
        if (clients[ID] != null) {
            clients[ID].send(Codes.SHOW_WINDOW, WindowID);
        }
    }
    public static void sendWindowStory(int storyID) {
        ClientThread.sendAll(Codes.SHOW_WINDOW, WndStory.ID());
    }
}
