package com.watabou.pixeldungeon.levels;

import com.watabou.pixeldungeon.BuildConfig;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.missiles.Dart;

public class LobbyLevel extends DeadEndLevel {

    public LobbyLevel() {
        super(7);
    }

    @Override
    protected boolean build() {
        boolean g = super.build();
        if (!g) {
            return false;
        }

        viewDistance = ((int) (SIZE * 1.5));
        exit = entrance + Level.WIDTH;
        map[exit] = Terrain.EXIT;

        if (BuildConfig.DEBUG) {
            {
                try {
                    Class cl = Dart.class;
                    drop((Item) cl.newInstance(),center+1);
                    drop((Item) cl.newInstance(),center+1);
                    heaps.get(center + 1).type = Heap.Type.CHEST;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }
}
