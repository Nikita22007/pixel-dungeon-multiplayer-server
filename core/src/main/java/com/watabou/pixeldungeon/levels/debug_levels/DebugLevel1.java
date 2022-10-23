package com.watabou.pixeldungeon.levels.debug_levels;

import android.annotation.SuppressLint;

import com.watabou.pixeldungeon.BuildConfig;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.missiles.Dart;
import com.watabou.pixeldungeon.levels.DeadEndLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;

public class DebugLevel1 extends DeadEndLevel {
    int depth;

    public DebugLevel1(int depth) {
        super(8);
        this.depth = depth;
    }

    @Override
    @SuppressLint("NewApi")
    protected boolean build() {
        boolean g = super.build();
        if (!g) {
            return false;
        }
        viewDistance = ((int) (SIZE * 1.5));
        exit = entrance + Level.WIDTH;
        map[exit] = Terrain.EXIT;
        int category_id = Math.abs(depth) - 1;
        Generator.Category[] categories = Generator.Category.values();
        if (category_id >= categories.length) {
            map[entrance] = Terrain.WATER;
            entrance = 0;
            return true;
        }
        if (categories.length - 1 == category_id) {
            map[entrance] = Terrain.WATER;
            entrance = 0;
            map[center] = Terrain.WATER;
        }
        Generator.Category category = categories[category_id];
        Class<?>[] classes = category.classes;
        int items_size = (int) Math.ceil(Math.sqrt(classes.length));
        int id = 0;
        int start_pos = center - (items_size/2) *WIDTH - (items_size/2);
        for (Class itemClass : classes) {
            int pos = start_pos + (Math.floorDiv(id, items_size)) * WIDTH + ((int)Math.floorMod(id,items_size));
            try {
                drop((Item) itemClass.newInstance(), pos);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            id +=1;
        }

        return true;
    }
}
