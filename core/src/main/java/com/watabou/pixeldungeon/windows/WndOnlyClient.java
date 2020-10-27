package com.watabou.pixeldungeon.windows;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.ui.Window;

public abstract class WndOnlyClient extends Window{//only while server is client, delete this class along with the descendants.
    public WndOnlyClient(){
        super();
        ownerHero=Dungeon.heroes[0];
    }
}
