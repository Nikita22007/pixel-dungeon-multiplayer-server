package com.watabou.pixeldungeon;



public class Settings {
    public static final boolean IS_CLIENT =false;

    public static int maxPlayers;

    public static enum ResurrectModeEnum{
        RESET_LEVEL, RESPAWN_HERO
    }

    public static ResurrectModeEnum resurrectMode=ResurrectModeEnum.RESPAWN_HERO;
    public static enum MoveModeEnum //when fall
    {
        TO_EXIT, WITH_MOVED
    }
    public static MoveModeEnum moveMode=MoveModeEnum.WITH_MOVED;
}
