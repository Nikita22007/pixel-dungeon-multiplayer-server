package com.watabou.pixeldungeon.levels;

import com.watabou.pixeldungeon.BuildConfig;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.items.weapon.melee.WarHammer;
import com.watabou.pixeldungeon.levels.painters.Painter;

import static com.watabou.pixeldungeon.levels.Terrain.ALARM_TRAP;
import static com.watabou.pixeldungeon.levels.Terrain.FIRE_TRAP;
import static com.watabou.pixeldungeon.levels.Terrain.GRIPPING_TRAP;
import static com.watabou.pixeldungeon.levels.Terrain.LIGHTNING_TRAP;
import static com.watabou.pixeldungeon.levels.Terrain.PARALYTIC_TRAP;
import static com.watabou.pixeldungeon.levels.Terrain.POISON_TRAP;
import static com.watabou.pixeldungeon.levels.Terrain.SUMMONING_TRAP;
import static com.watabou.pixeldungeon.levels.Terrain.TOXIC_TRAP;

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
        MeleeWeapon n = new WarHammer();
        n.levelKnown = true;
        n.enchant();
        this.drop(n,center+1);
        if (BuildConfig.DEBUG) {
            {
                int pos = center - 2-this.WIDTH;
                Painter.set(this, pos,TOXIC_TRAP);
                pos+=1;
                Painter.set(this, pos,FIRE_TRAP);
                pos+=1;
                Painter.set(this, pos,PARALYTIC_TRAP);
                pos+=1;
                Painter.set(this, pos,POISON_TRAP);
                pos+=1;

                pos = center - 2-this.WIDTH-this.WIDTH;

                Painter.set(this, pos,ALARM_TRAP);
                pos+=1;
                Painter.set(this, pos,LIGHTNING_TRAP);
                pos+=1;
                Painter.set(this, pos,GRIPPING_TRAP);
                pos+=1;
                Painter.set(this, pos,SUMMONING_TRAP);
                pos+=1;
            }
        }

        return true;
    }

    public Actor respawner() {
        return null;
    }
}
