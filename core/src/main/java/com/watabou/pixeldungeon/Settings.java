package com.watabou.pixeldungeon;


public class Settings {
    public static final boolean IS_CLIENT = false;
    public static String relayServerAddress = "195.43.142.107";
    //public static String relayServerAddress = "192.168.1.84";
    public static int relayServerPort = 25555;
    public static boolean useRelay = false;
    public static String serverName = "some server";


    public static int maxPlayers;

    public static enum ResurrectModeEnum {
        RESET_LEVEL, RESPAWN_HERO
    }

    public static ResurrectModeEnum resurrectMode = ResurrectModeEnum.RESPAWN_HERO;

    public static enum MoveModeEnum //when fall
    {
        TO_EXIT, WITH_MOVED
    }

    public static MoveModeEnum moveMode = MoveModeEnum.WITH_MOVED;
}
