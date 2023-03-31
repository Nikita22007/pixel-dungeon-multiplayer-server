package com.watabou.pixeldungeon;


public class Settings {
    public static String defaultRelayServerAddress = "195.43.142.107";
    //public static String relayServerAddress = "192.168.1.84";
    public static int defaultRelayServerPort = 25555;
    public static boolean returnDisabled = true;

    public static int maxPlayers = 4; // todo setup while loading
    public static boolean killOnDisconnect = false;

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
