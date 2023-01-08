package com.watabou.pixeldungeon.levels;

import com.watabou.pixeldungeon.BuildConfig;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.rings.RingOfSatiety;
import com.watabou.pixeldungeon.items.weapon.missiles.Dart;
import com.watabou.pixeldungeon.plants.Icecap;

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
                    Class cl = RingOfSatiety.class;
                    //drop((Item) cl.newInstance(),center+1);
                    Ring ring = (Ring) cl.newInstance();
                    ring.upgrade();
                    ring.upgrade();
                    ring.upgrade();
                    while (ring.durability() > 20) {
                        ring.use(null);
                    }
                    ring.identify();
                    drop((Item) ring,center+1);
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

    public Actor respawner() {
        return null;
    }
}
