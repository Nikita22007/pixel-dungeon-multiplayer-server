package com.watabou.pixeldungeon;



public class Settings {
    public static final boolean IS_CLIENT =false;

    public static int maxPlayers;

    public static enum GetXPMode{
        everyone
    }

    public static GetXPMode getXPMode = GetXPMode.everyone;
}
