package com.watabou.pixeldungeon.levels;

import com.watabou.pixeldungeon.BuildConfig;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.PotionOfLevitation;
import com.watabou.pixeldungeon.items.potions.PotionOfLiquidFlame;
import com.watabou.pixeldungeon.items.potions.PotionOfToxicGas;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.watabou.pixeldungeon.plants.Firebloom;
import com.watabou.pixeldungeon.plants.Sungrass;

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
                    Class cl = PotionOfLevitation.class;
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