package com.watabou.pixeldungeon.levels;

public class LobbyLevel extends DeadEndLevel {
    @Override
    protected boolean build() {
        boolean g = super.build();
        if (!g) {
            return false;
        }
        viewDistance = SIZE;
        exit = entrance + Level.WIDTH;
        map[exit] = Terrain.EXIT;

        return true;
    }
}
