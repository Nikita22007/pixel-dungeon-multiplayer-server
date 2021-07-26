package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.actors.hero.HeroClass;

public class SendData {
    //---------------------------Hero
    public static void SendHeroClass(HeroClass heroClass){
        Client.sendHeroClass(heroClass);

    }
}
