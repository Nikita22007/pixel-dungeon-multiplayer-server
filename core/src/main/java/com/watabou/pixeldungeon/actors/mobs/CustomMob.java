package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.sprites.RatSprite;

public class CustomMob extends Mob{


    public CustomMob(int id)  {
        name = "unknown";
        spriteClass = RatSprite.class;

        HP = HT = 1;
        defenseSkill = 1;

        maxLvl = 1;

        this.setId(id);
    }

    public String description() {
        return
                "A creature unknown to science.";
    }
}
